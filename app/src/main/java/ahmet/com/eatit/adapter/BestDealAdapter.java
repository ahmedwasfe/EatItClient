package ahmet.com.eatit.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.smarteist.autoimageslider.SliderViewAdapter;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import ahmet.com.eatit.callback.ISliderClickListener;
import ahmet.com.eatit.common.Common;
import ahmet.com.eatit.eventBus.BestDealClick;
import ahmet.com.eatit.model.BestDeals;
import ahmet.com.eatit.R;
import butterknife.BindView;
import butterknife.ButterKnife;


public class BestDealAdapter extends SliderViewAdapter<BestDealAdapter.BestDealHolder> {

    private Context mContext;
    private List<BestDeals> mListBestDeals;
    private LayoutInflater inflater;

    public BestDealAdapter(Context mContext, List<BestDeals> mListBestDeals) {
        this.mContext = mContext;
        this.mListBestDeals = mListBestDeals;
        inflater = LayoutInflater.from(mContext);
    }

    @Override
    public BestDealHolder onCreateViewHolder(ViewGroup parent) {
        View layoutView = inflater.inflate(R.layout.raw_best_deals, parent, false);
        return new BestDealHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(BestDealHolder holder, int position) {

        Picasso.get()
                .load(mListBestDeals.get(position).getImage())
                .into(holder.mImgBestDeals);
        holder.mTxtBestDealsName.setText(mListBestDeals.get(position).getName());

        holder.setSliderClickListener(new ISliderClickListener() {
            @Override
            public void onItemClick(View view) {
               // Common.currentCategory = mListBestDeals.get(position);
                EventBus.getDefault().postSticky(new BestDealClick(mListBestDeals.get(position)));
            }
        });


    }

    @Override
    public int getCount() {
        return mListBestDeals.size();
    }

    class BestDealHolder extends SliderViewAdapter.ViewHolder implements View.OnClickListener {

        @BindView(R.id.img_best_deals)
        ImageView mImgBestDeals;
        @BindView(R.id.txt_best_deals_name)
        TextView mTxtBestDealsName;

        private ISliderClickListener sliderClickListener;

        public BestDealHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
        }

        public void setSliderClickListener(ISliderClickListener sliderClickListener) {
            this.sliderClickListener = sliderClickListener;
        }

        @Override
        public void onClick(View v) {
            sliderClickListener.onItemClick(v);
        }
    }

}
