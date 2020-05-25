package ahmet.com.eatit.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.util.List;

import ahmet.com.eatit.CartDatabse.Cart;
import ahmet.com.eatit.R;
import ahmet.com.eatit.common.Common;
import ahmet.com.eatit.model.Food.Addon;
import ahmet.com.eatit.model.Food.FoodSize;
import butterknife.BindView;
import butterknife.ButterKnife;


public class OrderDetailsAdapter extends RecyclerView.Adapter<OrderDetailsAdapter.OrderDetailsHolder> {

    private Context mContext;
    private List<Cart> mListOrderDetails;
    private LayoutInflater inflater;

    private Gson gson;

    public OrderDetailsAdapter(Context mContext, List<Cart> mListOrderDetails) {
        this.mContext = mContext;
        this.mListOrderDetails = mListOrderDetails;

        inflater = LayoutInflater.from(mContext);

        gson = new Gson();
    }

    @NonNull
    @Override
    public OrderDetailsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = inflater.inflate(R.layout.raw_order_details, parent, false);
        return new OrderDetailsHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderDetailsHolder holder, int position) {

        Picasso.get().load(mListOrderDetails.get(position).getFoodImage()).into(holder.mImgOrderFood);

        holder.mTxtOrderFoodName.setText(mListOrderDetails.get(position).getFoodName());
        holder.mTxtOrderFoodQuantity.setText(new StringBuilder(mContext.getString(R.string.quantitiy)+" ")
                                    .append(mListOrderDetails.get(position).getFoodQuantity()));
        // Food Size
        FoodSize foodSize = gson.fromJson(mListOrderDetails.get(position).getFoodSize(),
                new TypeToken<FoodSize>(){}.getType());
        if (foodSize != null)
            holder.mTxtOrderFoodSize.setText(new StringBuilder("Size: ")
                                    .append(foodSize.getName()));
        else
            holder.mTxtOrderFoodSize.setText("Default");

        // Food Addon
        if (!mListOrderDetails.get(position).getFoodAddon().equals("Default")){
            List<Addon> listAddon = gson.fromJson(mListOrderDetails.get(position).getFoodAddon(),
                    new TypeToken<List<Addon>>(){}.getType());
            if (listAddon != null){

                holder.mTxtOrderFoodAddon.setText(new StringBuilder("Addon: ")
                        .append(Common.getListAddon(listAddon)));
            }
        }else
            holder.mTxtOrderFoodAddon.setText(new StringBuilder("Addon Default"));

    }

    @Override
    public int getItemCount() {
        return mListOrderDetails.size();
    }

    class OrderDetailsHolder extends RecyclerView.ViewHolder {


        @BindView(R.id.img_order_details_food)
        ImageView mImgOrderFood;

        @BindView(R.id.txt_order_details_food_name)
        TextView mTxtOrderFoodName;
        @BindView(R.id.txt_order_details_food_addon)
        TextView mTxtOrderFoodAddon;
        @BindView(R.id.txt_order_details_food_size)
        TextView mTxtOrderFoodSize;
        @BindView(R.id.txt_order_details_food_quantity)
        TextView mTxtOrderFoodQuantity;

        public OrderDetailsHolder(@NonNull View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }
}