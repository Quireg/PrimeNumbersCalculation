package ua.in.quireg.primenumberscalculation.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import ua.in.quireg.primenumberscalculation.R;
import ua.in.quireg.primenumberscalculation.Utils;
import ua.in.quireg.primenumberscalculation.computation.ComputationRetainedFragment;
import ua.in.quireg.primenumberscalculation.computation.FakeSocket;
import ua.in.quireg.primenumberscalculation.interfaces.TaskCompletedCallback;
import ua.in.quireg.primenumberscalculation.interfaces.UpdateRecyclerViewCallback;
import ua.in.quireg.primenumberscalculation.models.IntervalModel;

public class MainActivity extends AppCompatActivity implements UpdateRecyclerViewCallback, TaskCompletedCallback {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String TAG_TASK_FRAGMENT = "task_fragment";

    private ComputationRetainedFragment mTaskFragment;
    private ComputationResultFragment computationResultsFragment;

    private Unbinder unbinder;


    private boolean isComputing = false;

    @BindView(R.id.button_start_computation)
    protected Button startButton;

    @BindView(R.id.button_resume_computation)
    protected Button resumeButton;

    @BindView(R.id.button_stop_computation)
    protected Button stopButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        unbinder = ButterKnife.bind(this);
        FakeSocket.getInstance().init(this);


        FragmentManager fm = getSupportFragmentManager();
        mTaskFragment = (ComputationRetainedFragment) fm.findFragmentByTag(TAG_TASK_FRAGMENT);
        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (mTaskFragment == null) {
            mTaskFragment = new ComputationRetainedFragment();
            fm.beginTransaction().add(mTaskFragment, TAG_TASK_FRAGMENT).commit();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }


    @OnClick(R.id.button_start_computation)
    void startComputation() {


        try {
            //Obtain list of models for computation
            List<IntervalModel> models = Utils.parseXMLfromAssets(this);

            showComputationFragment();

            //start computation
            mTaskFragment.startComputation(models);

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        isComputing = true;


        startButton.setVisibility(View.GONE);
        resumeButton.setVisibility(View.VISIBLE);
        stopButton.setVisibility(View.VISIBLE);


    }

    @OnClick(R.id.button_stop_computation)
    void stopComputation() {
        mTaskFragment.stopComputation();
        isComputing = false;
        startButton.setVisibility(View.VISIBLE);
        resumeButton.setVisibility(View.GONE);
        stopButton.setVisibility(View.GONE);
    }

    @OnClick(R.id.button_resume_computation)
    void resumeComputation() {
        showComputationFragment();
    }

    @Override
    public void updateRecyclerView(SparseIntArray array) {
        runOnUiThread(() -> {
            if(computationResultsFragment != null && computationResultsFragment.isAdded())
            computationResultsFragment.updateDataInRecyclerView(array);
        });
    }

    private void showComputationFragment(){
        if(computationResultsFragment == null){
            computationResultsFragment = new ComputationResultFragment();
        }
        getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(computationResultsFragment.getClass().toString())
                .replace(android.R.id.content, computationResultsFragment, computationResultsFragment.getClass().toString())
                .commit();
    }

    @Override
    public void taskCompleted() {
        runOnUiThread(() -> {
            isComputing = false;
            startButton.setVisibility(View.VISIBLE);
            resumeButton.setVisibility(View.GONE);
            stopButton.setVisibility(View.GONE);
        });
    }
}
