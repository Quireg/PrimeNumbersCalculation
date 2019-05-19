package ua.in.quireg.primenumberscalculation.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

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

public class MainActivity extends AppCompatActivity implements UpdateRecyclerViewCallback,
        TaskCompletedCallback {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String TAG_TASK_FRAGMENT = "task_fragment";
    private ComputationRetainedFragment mTaskFragment;
    private ComputationFragment mComputationResultsFragment;
    private Unbinder mUnbinder;
    private boolean mIsComputing = false;
    @BindView(R.id.button_start_computation)
    protected Button mStartButton;
    @BindView(R.id.button_resume_computation)
    protected Button mResumeButton;
    @BindView(R.id.button_stop_computation)
    protected Button mStopButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mUnbinder = ButterKnife.bind(this);
        FakeSocket.getInstance().init(this);

        FragmentManager fm = getSupportFragmentManager();
        mTaskFragment = (ComputationRetainedFragment) fm.findFragmentByTag(TAG_TASK_FRAGMENT);
        if (mTaskFragment == null) {
            mTaskFragment = new ComputationRetainedFragment();
        }
        mTaskFragment.setOnCompleteCallback(this);
        fm.beginTransaction().add(mTaskFragment, TAG_TASK_FRAGMENT).commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
    }

    @OnClick(R.id.button_start_computation)
    void startComputation() {
        if (mIsComputing) {
            return;
        }
        mIsComputing = true;
        try {
            //Obtain list of models for computation
            List<IntervalModel> models = Utils.parseXMLfromAssets(this);
            showComputationFragment();
            //start computation
            mTaskFragment.startComputation(models);
            mStartButton.setVisibility(View.GONE);
            mResumeButton.setVisibility(View.VISIBLE);
            mStopButton.setVisibility(View.VISIBLE);
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getLocalizedMessage());
        }
    }

    @OnClick(R.id.button_stop_computation)
    void cancelComputation() {
        mTaskFragment.cancelComputation();
        mIsComputing = false;
        mStartButton.setVisibility(View.VISIBLE);
        mResumeButton.setVisibility(View.GONE);
        mStopButton.setVisibility(View.GONE);
        Toast.makeText(this, R.string.cancel_computation_toast, Toast.LENGTH_SHORT).show();

    }

    @OnClick(R.id.button_resume_computation)
    void resumeComputation() {
        showComputationFragment();
    }

    @Override
    public void updateRecyclerView(SparseIntArray array) {
        runOnUiThread(() -> {
            if (mComputationResultsFragment != null && mComputationResultsFragment.isAdded())
                mComputationResultsFragment.updateDataInRecyclerView(array);
        });
    }

    private void showComputationFragment() {
        if (mComputationResultsFragment == null) {
            mComputationResultsFragment = new ComputationFragment();
        }
        getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(null)
                .replace(android.R.id.content, mComputationResultsFragment, null)
                .commit();
    }

    @Override
    public void taskCompleted() {
        runOnUiThread(() -> {
            mIsComputing = false;
            mComputationResultsFragment = null;
            mStartButton.setVisibility(View.VISIBLE);
            mResumeButton.setVisibility(View.GONE);
            mStopButton.setVisibility(View.GONE);
            Toast.makeText(this, R.string.complete_computation_toast, Toast.LENGTH_SHORT).show();
        });
    }
}
