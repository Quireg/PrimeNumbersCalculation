package ua.in.quireg.primenumberscalculation.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import ua.in.quireg.primenumberscalculation.R;
import ua.in.quireg.primenumberscalculation.models.ComputationResultModel;


public class PrimeNumbersRecyclerViewAdapter extends RecyclerView.Adapter<PrimeNumbersRecyclerViewAdapter.ViewHolder> {

    private final int LEFT_VIEW = 0;
    private final int RIGHT_VIEW = 1;

    private SparseIntArray mValues = new SparseIntArray();

    private Context mCtx;

    public PrimeNumbersRecyclerViewAdapter(Context context) {
        mCtx = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {


        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_intervalmodel, parent, false);
        TextView header = view.findViewById(R.id.header);
        TextView content = view.findViewById(R.id.content);
        if (viewType == LEFT_VIEW) {
            //set header params
            RelativeLayout.LayoutParams headerParams = (RelativeLayout.LayoutParams) header.getLayoutParams();
            headerParams.addRule(RelativeLayout.ALIGN_PARENT_START);
            header.setLayoutParams(headerParams);

            header.setBackground(ContextCompat.getDrawable(mCtx, R.drawable.right_round_corner));

            //set content params
            content.setGravity(Gravity.START);


        } else if (viewType == RIGHT_VIEW) {
            //set header params
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) header.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
            header.setLayoutParams(layoutParams);

            header.setBackground(ContextCompat.getDrawable(mCtx, R.drawable.left_round_corner));

            //set content params
            content.setGravity(Gravity.END);
        }

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        position = position + 1;
        holder.mItem = new ComputationResultModel(position, mValues.get(position));
        holder.mIdView.setText("Thread number " + position);
        holder.mContentView.setText("prime numbers generated: " + mValues.get(position));

    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 || position % 2 == 0) {
            return LEFT_VIEW;
        } else {
            return RIGHT_VIEW;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;
        public ComputationResultModel mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = view.findViewById(R.id.header);
            mContentView = view.findViewById(R.id.content);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }

    public void setData(SparseIntArray data) {
        mValues = data;
    }
}
