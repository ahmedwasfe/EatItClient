package ahmet.com.eatit.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import ahmet.com.eatit.callback.IRecyclerItemClickLitener;
import ahmet.com.eatit.eventBus.PopularCategoryClcik;
import ahmet.com.eatit.model.PopularCategories;
import ahmet.com.eatit.R;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;


public class PopularAdapter extends RecyclerView.Adapter<PopularAdapter.PopularHolder> {

    private Context mContext;
    private List<PopularCategories> mListPopular;
    private LayoutInflater inflater;

    public PopularAdapter(Context mContext, List<PopularCategories> mListPopular) {
        this.mContext = mContext;
        this.mListPopular = mListPopular;

        inflater = LayoutInflater.from(mContext);
    }

    @NonNull
    @Override
    public PopularHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = inflater.inflate(R.layout.raw_populer_categories, parent, false);
        return new PopularHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull PopularHolder holder, int position) {

        Picasso.get()
                .load(mListPopular.get(position).getImage())
                .into(holder.mImgPopular);
        holder.mTxtPopularName.setText(new StringBuilder(mListPopular.get(position).getName()));

        holder.setRecyclerItemClickLitener((view, position1) -> {
            EventBus.getDefault().postSticky(new PopularCategoryClcik(mListPopular.get(position1)));
        });
    }

    @Override
    public int getItemCount() {
        return mListPopular.size();
    }

    class PopularHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.img_populer)
        CircleImageView mImgPopular;
        @BindView(R.id.txt_populer_name)
        TextView mTxtPopularName;

        private IRecyclerItemClickLitener recyclerItemClickLitener;

        public PopularHolder(@NonNull View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
        }

        public void setRecyclerItemClickLitener(IRecyclerItemClickLitener recyclerItemClickLitener) {
            this.recyclerItemClickLitener = recyclerItemClickLitener;
        }

        @Override
        public void onClick(View v) {
            recyclerItemClickLitener.onItemClick(v, getAdapterPosition());
        }
    }
}