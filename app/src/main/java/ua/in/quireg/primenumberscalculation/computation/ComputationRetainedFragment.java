package ua.in.quireg.primenumberscalculation.computation;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseIntArray;

import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableSubscriber;
import io.reactivex.Notification;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;
import ua.in.quireg.primenumberscalculation.exceptions.ConnectionUnexpectedlyClosedException;
import ua.in.quireg.primenumberscalculation.interfaces.TaskCompletedCallback;
import ua.in.quireg.primenumberscalculation.models.IntervalModel;

public class ComputationRetainedFragment extends Fragment {

    private static final String LOG_TAG = ComputationRetainedFragment.class.getSimpleName();

    private ExecutorService mExecutorService;
    private CompositeDisposable mCompositeDisposable;
    private TaskCompletedCallback mTaskCompletedCallback;

    public ComputationRetainedFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public void setOnCompleteCallback(TaskCompletedCallback c) {
        mTaskCompletedCallback = c;
    }

    private void sendDataToUI(SparseIntArray array) {
        int retryCount = 0;
        while (retryCount < 5) {
            try {
                FakeSocket socket = FakeSocket.connect();
                socket.transferData(array);
                socket.close();
                return;
            } catch (ConnectionUnexpectedlyClosedException e) {
                Log.e(LOG_TAG, e.getMessage());
                retryCount++;
            }
        }
    }

    public void startComputation(List<IntervalModel> models) {
        mCompositeDisposable = new CompositeDisposable();
        mExecutorService = Executors.newFixedThreadPool(
                models.size(), new ThreadFactoryWithThreadName());

        //Storing thread implementation which receives prime numbers from manager thread
        //and sends them to UI via FakeSocket connection.
        PublishProcessor<SparseIntArray> storingThreadProcessor = PublishProcessor.create();

        FlowableSubscriber<SparseIntArray> fs1 =
                new FlowableSubscriber<SparseIntArray>() {
                    Subscription subscription;
                    @Override
                    public void onSubscribe(Subscription s) {
                        subscription = s;
                        s.request(1);
                    }

                    @Override
                    public void onNext(SparseIntArray sparseIntArray) {
                        if (mCompositeDisposable.isDisposed()) {
                            subscription.cancel();
                        }
                        Log.d(LOG_TAG, sparseIntArray.toString());
                        sendDataToUI(sparseIntArray);
                        subscription.request(1);
                    }

                    @Override
                    public void onError(Throwable t) {
                        Log.e(LOG_TAG, t.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.d(LOG_TAG, "Storage thread: " + Thread.currentThread().getName());
                        completeComputation();
                    }
                };

        storingThreadProcessor
                .observeOn(Schedulers.newThread())
                .subscribe(fs1);


        FlowableSubscriber<Notification<int[]>> flowableSubscriber =
                new FlowableSubscriber<Notification<int[]>>() {
                    SparseIntArray sparseIntArray = new SparseIntArray();
                    Subscription subscription;

                    @Override
                    public void onSubscribe(Subscription s) {
                        subscription = s;
                        s.request(1);
                    }

                    @Override
                    public void onNext(Notification<int[]> notification) {
                        if (mCompositeDisposable.isDisposed()) {
                            subscription.cancel();
                        }

                        if (notification.isOnNext()) {

                            int totalForKey = sparseIntArray.get(
                                    notification.getValue()[0], 0);

                            sparseIntArray.put(
                                    notification.getValue()[0], ++totalForKey);

                            storingThreadProcessor.offer(sparseIntArray);
                        } else if (notification.isOnError()) {
                            Log.e(LOG_TAG, notification.getError().getMessage());
                        } else if (notification.isOnComplete()) {
                            storingThreadProcessor.onComplete();
                        }

                        subscription.request(1);
                    }

                    @Override
                    public void onError(Throwable t) {

                    }

                    @Override
                    public void onComplete() {
                        Log.d(LOG_TAG, "Manager thread: " + Thread.currentThread().getName());
                    }
                };

        List<Flowable<int[]>> workerFlowables = new ArrayList<>();
        for (IntervalModel model : models) {
            //For each interval create flowable that emits prime numbers.
            workerFlowables.add(getPrimeNumbersFlowableForModel(model));
        }
         Flowable.merge(workerFlowables)
                .observeOn(Schedulers.newThread())
                .materialize()
                .subscribe(flowableSubscriber);
    }

    private Flowable<int[]> getPrimeNumbersFlowableForModel(IntervalModel model) {

        Flowable<int[]> f = Flowable.create(emitter -> {
            Log.d(LOG_TAG, "Computation started on thread number "
                    + Thread.currentThread().getName());

            int[] threadNumberModel = new int[]{model.id, 0};

            // implementation uses Sieve of Eratosthenes
            boolean[] primes = new boolean[model.high - model.low + 1];
            Arrays.fill(primes, true);

            for (int i = 2; i < primes.length; ++i) {
                if (primes[i]) {
                    threadNumberModel[1] = i;
                    if (emitter.requested() > 0) {
                        emitter.onNext(threadNumberModel);
                        //sieve other non-prime numbers
                        for (int j = 2; i * j < primes.length; ++j) {
                            primes[i * j] = false;
                        }
                    } else if (emitter.isCancelled()) {
                        break;
                    } else {
                        --i;
                        TimeUnit.MILLISECONDS.sleep(10);
                    }
                }
            }
            emitter.onComplete();
        }, BackpressureStrategy.MISSING);

        return f.subscribeOn(Schedulers.from(mExecutorService));
    }

    public void completeComputation() {
        mCompositeDisposable.dispose();
        mExecutorService.shutdown();
        mTaskCompletedCallback.taskCompleted();
    }

    public void cancelComputation() {
        mCompositeDisposable.dispose();
        mExecutorService.shutdown();
    }
}

