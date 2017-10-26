package ua.in.quireg.primenumberscalculation.computation;


import android.annotation.SuppressLint;
import android.util.SparseIntArray;

import ua.in.quireg.primenumberscalculation.exceptions.ConnectionUnexpectedlyClosedException;
import ua.in.quireg.primenumberscalculation.interfaces.UpdateRecyclerViewCallback;

public class FakeSocket {

    private static final String LOG_TAG = FakeSocket.class.getSimpleName();

    @SuppressLint("StaticFieldLeak")
    private static FakeSocket fakeSocket;

    private SparseIntArray data;
    private SparseIntArray nominatedData;

    private boolean isConnected;

    private UpdateRecyclerViewCallback mCtx;

    private FakeSocket() {

    }

    public static FakeSocket getInstance(){
        if(fakeSocket == null){
            fakeSocket = new FakeSocket();
        }
        return fakeSocket;
    }

    public void init(UpdateRecyclerViewCallback context){
            mCtx = context;
    }

    public static FakeSocket connect() throws ConnectionUnexpectedlyClosedException {
        if(fakeSocket == null){
            throw new ConnectionUnexpectedlyClosedException("You must call init() first!");
        }

        fakeSocket.isConnected = true;

        //Emulate 5% error on connect
        if(Math.random()*100 < 5){
            fakeSocket.isConnected = false;
            throw new ConnectionUnexpectedlyClosedException("Oops. Connection closed =(");
        }
        return fakeSocket;
    }

    public void transferData(SparseIntArray array) throws ConnectionUnexpectedlyClosedException {
        if(!isConnected){
            throw new ConnectionUnexpectedlyClosedException("You must call connect() first!");
        }
        nominatedData = array;

        //Emulate 5% error on transfer
        if(Math.random()*100 < 5){
            rollback();
            throw new ConnectionUnexpectedlyClosedException("Oops. Connection closed =(");
        }
        commit();
    }

    public void close(){
        rollback();
    }

    private void rollback(){
        isConnected = false;
        nominatedData = null;
    }

    private void commit(){
        isConnected = false;
        data = nominatedData;
        nominatedData = null;
        updateUI();
    }

    private void updateUI(){
        mCtx.updateRecyclerView(data);
    }
}
