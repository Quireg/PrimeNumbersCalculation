package ua.in.quireg.primenumberscalculation.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ua.in.quireg.primenumberscalculation.R;
import ua.in.quireg.primenumberscalculation.adapters.PrimeNumbersRecyclerViewAdapter;

public class ComputationFragment extends Fragment {

    private PrimeNumbersRecyclerViewAdapter mAdapter;

    public ComputationFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.fragment_intervalmodel_list, container, false);

        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;

            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                    recyclerView.getContext(), DividerItemDecoration.VERTICAL);
            recyclerView.addItemDecoration(dividerItemDecoration);
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            if (mAdapter == null) {
                mAdapter = new PrimeNumbersRecyclerViewAdapter(getContext());
            }
            recyclerView.setAdapter(mAdapter);
        }
        return view;
    }

    public void updateDataInRecyclerView(SparseIntArray data) {
        mAdapter.setData(data);
        mAdapter.notifyDataSetChanged();
    }
}
