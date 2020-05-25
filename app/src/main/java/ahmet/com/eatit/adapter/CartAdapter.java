package ahmet.com.eatit.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import ahmet.com.eatit.CartDatabse.Cart;
import ahmet.com.eatit.CartDatabse.CartDataSource;
import ahmet.com.eatit.CartDatabse.CartDatabase;
import ahmet.com.eatit.CartDatabse.LocalCartDataSource;
import ahmet.com.eatit.common.Common;
import ahmet.com.eatit.eventBus.UpdateFoodQuantityInCart;
import ahmet.com.eatit.R;
import ahmet.com.eatit.model.Food.Addon;
import ahmet.com.eatit.model.Food.FoodSize;
import butterknife.BindView;
import butterknife.ButterKnife;


public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartTestHolder> {

    private Context mContext;
    private List<Cart> mListCart;
    private LayoutInflater inflater;

    private CartDataSource mCartDataSource;

    private Gson gson;

    public CartAdapter(Context mContext, List<Cart> mListCart) {
        this.mContext = mContext;
        this.mListCart = mListCart;

        inflater = LayoutInflater.from(mContext);

        mCartDataSource = new LocalCartDataSource(CartDatabase.getInstance(mContext)
                                .cartDAO());

        gson = new Gson();
    }

    @NonNull
    @Override
    public CartTestHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = inflater.inflate(R.layout.raw_cart, parent, false);
        return new CartTestHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull CartTestHolder holder, int position) {

        Picasso.get()
                .load(mListCart.get(position).getFoodImage())
                .into(holder.mImg);
        holder.mTxtName.setText(mListCart.get(position).getFoodName());
        double sumPrice = (mListCart.get(position).getFoodPrice() + mListCart.get(position).getFoodExtraPrice())
                            * mListCart.get(position).getFoodQuantity();
        holder.mTxtPrice.setText(new StringBuilder("$ ")
                            .append(sumPrice)
                            .toString());

        // Food Size
        if (mListCart.get(position).getFoodSize() != null){
            if (mListCart.get(position).getFoodSize().equals("Default"))
                holder.mTxtFoodSize.setText(new StringBuilder("Size: ").append("Default"));
            else{
                FoodSize foodSize = gson.fromJson(mListCart.get(position).getFoodSize(),
                        new TypeToken<FoodSize>(){}.getType());
                holder.mTxtFoodSize.setText(new StringBuilder("Size: ").append(foodSize.getName()));
            }
        }

        // Food Addon
        if (mListCart.get(position).getFoodAddon() != null){
            if (mListCart.get(position).getFoodAddon().equals("Default"))
                holder.mTxtFoodAddon.setText(new StringBuilder("Addon: ").append("Default"));
            else{
                List<Addon> listAddon = gson.fromJson(mListCart.get(position).getFoodAddon(),
                        new TypeToken<List<Addon>>(){}.getType());

                if (listAddon != null) {

                    holder.mTxtFoodAddon.setText(new StringBuilder("Addon: ")
                            .append(Common.getListAddon(listAddon)));
                }
            }
        }


        holder.mFoodQuantity.setNumber(String.valueOf(mListCart.get(position).getFoodQuantity()));

        holder.mFoodQuantity.setOnValueChangeListener((view, oldValue, newValue) -> {

            // When user click this btn will update cart database
            mListCart.get(position).setFoodQuantity(newValue);
            EventBus.getDefault().postSticky(new UpdateFoodQuantityInCart(mListCart.get(position)));
        });

    }

    @Override
    public int getItemCount() {
        return mListCart.size();
    }

    public Cart getItemAtPosition(int position) {
        return mListCart.get(position);
    }

    class CartTestHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.img_cart_food)
        ImageView mImg;
        @BindView(R.id.txt_cart_food_name)
        TextView mTxtName;
        @BindView(R.id.txt_cart_food_size)
        TextView mTxtFoodSize;
        @BindView(R.id.txt_cart_food_addon)
        TextView mTxtFoodAddon;
        @BindView(R.id.txt_cart_food_price)
        TextView mTxtPrice;
        @BindView(R.id.btn_cart_elegant_quantity)
        ElegantNumberButton mFoodQuantity;

        public CartTestHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}