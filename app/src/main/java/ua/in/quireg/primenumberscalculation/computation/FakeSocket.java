package ua.in.quireg.primenumberscalculation.computation;

import android.annotation.SuppressLint;
import android.util.SparseIntArray;

import ua.in.quireg.primenumberscalculation.exceptions.ConnectionUnexpectedlyClosedException;
import ua.in.quireg.primenumberscalculation.interfaces.UpdateRecyclerViewCallback;

@SuppressWarnings("WeakerAccess")
public class FakeSocket {
    private static final String LOG_TAG = FakeSocket.class.getSimpleName();

    @SuppressLint("StaticFieldLeak")
    private static FakeSocket sFakeSocket;

    private SparseIntArray mData;
    private SparseIntArray mNominatedData;

    private boolean mIsConnected;

    private UpdateRecyclerViewCallback mUpdateRVCallback;

    private FakeSocket() {
    }

    public static synchronized FakeSocket getInstance() {
        if (sFakeSocket == null) {
            sFakeSocket = new FakeSocket();
        }
        return sFakeSocket;
    }

    public void init(UpdateRecyclerViewCallback context) {
        mUpdateRVCallback = context;
    }

    public static FakeSocket connect() throws ConnectionUnexpectedlyClosedException {
        if (sFakeSocket == null) {
            throw new ConnectionUnexpectedlyClosedException("You must call init() first!");
        }

        sFakeSocket.mIsConnected = true;

        //Emulate 5% error on connect
        if (Math.random() * 100 < 5 && false) {
            sFakeSocket.mIsConnected = false;
            throw new ConnectionUnexpectedlyClosedException("Oops. Connection closed =(");
        }
        return sFakeSocket;
    }

    public void transferData(SparseIntArray array) throws ConnectionUnexpectedlyClosedException {
        if (!mIsConnected) {
            throw new ConnectionUnexpectedlyClosedException("You must call connect() first!");
        }
        mNominatedData = array;

        //Emulate 5% error on transfer
        if (Math.random() * 100 < 5 && false) {
            rollback();
            throw new ConnectionUnexpectedlyClosedException("Oops. Connection closed =( ");
        }
        commit();
    }

    public void close() {
        rollback();
    }

    private void rollback() {
        mIsConnected = false;
        mNominatedData = null;
    }

    private void commit() {
        mIsConnected = false;
        mData = mNominatedData;
        mNominatedData = null;
        updateUI();
    }

    private void updateUI() {
        mUpdateRVCallback.updateRecyclerView(mData);
    }
}
