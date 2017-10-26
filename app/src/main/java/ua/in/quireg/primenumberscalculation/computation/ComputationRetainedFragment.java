package ua.in.quireg.primenumberscalculation.computation;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseIntArray;

import org.reactivestreams.Publisher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.Flowable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.processors.BehaviorProcessor;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;
import ua.in.quireg.primenumberscalculation.exceptions.ConnectionUnexpectedlyClosedException;
import ua.in.quireg.primenumberscalculation.interfaces.TaskCompletedCallback;
import ua.in.quireg.primenumberscalculation.models.IntervalModel;


public class ComputationRetainedFragment extends Fragment {

    private static final String LOG_TAG = ComputationRetainedFragment.class.getSimpleName();

    private CompositeDisposable disposables = new CompositeDisposable();
    private ExecutorService executorService;
    private TaskCompletedCallback taskCompletedCallback;
    Disposable storingThreadDisposable;


    public ComputationRetainedFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        taskCompletedCallback = (TaskCompletedCallback) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        taskCompletedCallback = null;
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

        executorService = Executors.newFixedThreadPool(models.size(), new ThreadFactoryWithThreadName());
        disposables = new CompositeDisposable();

        //Storing thread implementation which receives prime numbers from "another thread"
        //and sends them to "UI thread" via FakeSocket connection.
        BehaviorProcessor<SparseIntArray> storingThreadSubject = BehaviorProcessor.create();

        storingThreadDisposable = storingThreadSubject
                .onBackpressureLatest()
                .observeOn(Schedulers.newThread())
                .subscribeWith(new DisposableSubscriber<SparseIntArray>() {
                    {
                        Log.d(LOG_TAG, "Storing thread initiated with thread name: "
                                + Thread.currentThread().getName());
                    }
                    @Override
                    protected void onStart() {
                        request(1);
                    }

                    @Override
                    public void onNext(SparseIntArray array) {
                        sendDataToUI(array);
                        request(1);
                    }

                    @Override
                    public void onError(Throwable t) {
                        Log.e(LOG_TAG, t.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        stopComputation();
                    }
                });

        disposables.add(storingThreadDisposable);

        /*
        Aggregate all flowables into single one
        that maintain dataflow represented by int[] array,
        where first element is thread ID and second one is next prime number found.
        Subscriber stores an amount of found prime numbers and sends it to "storing thread".
        */
        List<Flowable<int[]>> flowables = new ArrayList<>();

        for (IntervalModel model : models) {
            //For each interval create flowable that emits prime numbers.
            Flowable<int[]> flowable = Flowable.fromPublisher(getPrimeNumbersPublisher(model))
                    //Each observable computed on it's own thread.
                    .subscribeOn(Schedulers.from(executorService))
                    .onBackpressureDrop();
            flowables.add(flowable);
        }

        Flowable<int[]> mergedFlowables = Flowable.merge(flowables)
                //observe computation result from different threads on new thread.
                .observeOn(Schedulers.newThread());

        Disposable managerThreadDisposable = mergedFlowables
                .subscribeWith(new DisposableSubscriber<int[]>() {

                    SparseIntArray primeNumbersStorage = new SparseIntArray();

                    {
                        Log.d(LOG_TAG, "Manager thread initiated with thread name: "
                                + Thread.currentThread().getName());

//                        //Initially fill storage and send to UI
//                        for (int i = 0; i < models.size(); i++) {
//                            primeNumbersStorage.append(i + 1, 0);
//                        }
//                        storingThreadSubject.onNext(primeNumbersStorage.clone());
                    }

                    @Override
                    protected void onStart() {
                        request(1);
                    }

                    @Override
                    public void onNext(int[] integer) {

                        int key = integer[0];
                        int value = integer[1];
                        primeNumbersStorage.put(key, value);
                        storingThreadSubject.onNext(primeNumbersStorage);
                        request(1);
                    }

                    @Override
                    public void onError(Throwable e) {
                        storingThreadSubject.onError(e);
                    }

                    @Override
                    public void onComplete() {
                        //return last values and complete the sequence
                        storingThreadSubject.onNext(primeNumbersStorage.clone());
                        storingThreadSubject.onComplete();
                    }

                });

        disposables.add(managerThreadDisposable);

    }

    public void stopComputation() {
        disposables.dispose();
        executorService.shutdown();
        taskCompletedCallback.taskCompleted();
    }

    private Publisher<int[]> getPrimeNumbersPublisher(IntervalModel model) {

        return s -> {
            Log.d(LOG_TAG, "Computation started on thread number " + Thread.currentThread().getName());

            int[] primeNumbersFound = new int[]{model.id, 0};

            //implementation uses Sieve of Eratosthenes, described here: https://habrahabr.ru/post/122538/

            boolean[] primes = new boolean[model.high - model.low + 1];
            Arrays.fill(primes, true);

            for (int i = 2; i < primes.length; ++i) {
                if (primes[i]) {
                    for (int j = 2; i * j < primes.length; ++j) {
                        primes[i * j] = false;
                    }
                    //Check if we should stop computation
                    if (!disposables.isDisposed()) {
                        //increment prime numbers counter and send it.
                        primeNumbersFound[1] = primeNumbersFound[1] + 1;
                        s.onNext(primeNumbersFound);

                    } else {
                        Thread.currentThread().interrupt();
                        return;
                    }
                    //Log.d(LOG_TAG, System.currentTimeMillis() + " Thread/number " + Thread.currentThread().getName() + "/" + (model.low + i));
                }
            }

            s.onComplete();
        };

    }
}

