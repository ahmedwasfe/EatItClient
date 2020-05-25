package ahmet.com.eatit.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.asksira.loopingviewpager.LoopingPagerAdapter;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import ahmet.com.eatit.eventBus.BestDealClick;
import ahmet.com.eatit.model.BestDeals;
import ahmet.com.eatit.R;
import butterknife.BindView;
import butterknife.ButterKnife;


public class BestDealsAdapter extends LoopingPagerAdapter<BestDeals> {

    @BindView(R.id.img_best_deals)
    ImageView mImgBestDeals;
    @BindView(R.id.txt_best_deals_name)
    TextView mTxtBestDealsName;

    public BestDealsAdapter(Context context, List<BestDeals> itemList, boolean isInfinite) {
        super(context, itemList, isInfinite);
    }


    @Override
    protected View inflateView(int viewType, ViewGroup container, int listPosition) {
        return LayoutInflater.from(context)
                .inflate(R.layout.raw_best_deals, container, false);
    }

    @Override
    protected void bindView(View convertView, int position, int viewType) {

        ButterKnife.bind(this, convertView);

        Picasso.get()
                .load(itemList.get(position).getImage())
                .into(mImgBestDeals);
        mTxtBestDealsName.setText(itemList.get(position).getName());

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().postSticky(new BestDealClick(itemList.get(position)));
            }
        });

    }
}