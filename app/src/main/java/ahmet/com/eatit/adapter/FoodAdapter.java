package ahmet.com.eatit.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import ahmet.com.eatit.callback.IRecyclerItemClickLitener;
import ahmet.com.eatit.CartDatabse.CartDataSource;
import ahmet.com.eatit.CartDatabse.CartDatabase;
import ahmet.com.eatit.CartDatabse.LocalCartDataSource;
import ahmet.com.eatit.CartDatabse.Cart;
import ahmet.com.eatit.common.Common;
import ahmet.com.eatit.eventBus.CounterCartEvent;
import ahmet.com.eatit.eventBus.FoodClick;
import ahmet.com.eatit.model.Food.Food;
import ahmet.com.eatit.R;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodHolder> {

    private Context mContext;
    private List<Food> mListFood;
    private LayoutInflater inflater;

    private CompositeDisposable mDisposable;
    private CartDataSource mCartDataSource;

    public FoodAdapter(Context mContext, List<Food> mListFood) {
        this.mContext = mContext;
        this.mListFood = mListFood;

        inflater = LayoutInflater.from(mContext);

        this.mDisposable = new CompositeDisposable();
        this.mCartDataSource = new LocalCartDataSource(CartDatabase.getInstance(mContext).cartDAO());
    }

    @NonNull
    @Override
    public FoodHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = inflater.inflate(R.layout.raw_food, parent, false);
        return new FoodHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodHolder holder, int position) {

        Picasso.get()
                .load(mListFood.get(position).getImage())
                .into(holder.mImgFood);
        holder.mTxtFoodPrice.setText(new StringBuilder("$")
                                    .append(mListFood.get(position).getPrice()));
        holder.mTxtFoodName.setText(new StringBuilder("")
                                    .append(mListFood.get(position).getName()));

        if (mListFood.get(position).getRateValue() != null) {
            float ratingValue = (float) (mListFood.get(position).getRateValue() / mListFood.get(position).getRateCount());
           // holder.mTxtFoodRatingValue.setText(String.valueOf(ratingValue));
            holder.mRatingFood.setRating(ratingValue);
        }
        holder.setRecyclerItemClickLitener((view, position1) -> {
            Common.currentFood = mListFood.get(position1);
            Common.currentFood.setKey(String.valueOf(position1));
            EventBus.getDefault().postSticky(new FoodClick(true, mListFood.get(position)));
        });

        holder.mImgAddToCart.setOnClickListener(view ->{
            Cart cartItem = new Cart();

            // User
            cartItem.setUserPhone(Common.currentUser.getPhone());
            cartItem.setUserUid(Common.currentUser.getUid());
            // Food
            cartItem.setCategoryId(Common.currentCategory.getMenu_id());
            cartItem.setFoodId(mListFood.get(position).getId());
            cartItem.setFoodName(mListFood.get(position).getName());
            cartItem.setFoodImage(mListFood.get(position).getImage());
            cartItem.setFoodPrice(Double.valueOf(mListFood.get(position).getPrice()));
            cartItem.setFoodQuantity(1);
            cartItem.setFoodExtraPrice(0.0); // Because default not choose size and addon so exrta price is 0
            cartItem.setFoodAddon("Default");
            cartItem.setFoodSize("Default");

            mCartDataSource.getCartWithAllOptions(Common.currentUser.getUid(),
                            Common.currentCategory.getMenu_id(),
                            cartItem.getFoodId(),
                            cartItem.getFoodSize(),
                            cartItem.getFoodAddon())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Cart>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(Cart cart) {

                            if (cart.equals(cartItem)){
                                // Already in database, Just update
                                cart.setFoodExtraPrice(cartItem.getFoodExtraPrice());
                                cart.setFoodAddon(cartItem.getFoodAddon());
                                cart.setFoodSize(cartItem.getFoodSize());
                                cart.setFoodQuantity(cart.getFoodQuantity() + cartItem.getFoodQuantity());

                                mCartDataSource.updateItemCart(cart)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new SingleObserver<Integer>() {
                                            @Override
                                            public void onSubscribe(Disposable d) {

                                            }

                                            @Override
                                            public void onSuccess(Integer integer) {
                                                Toast.makeText(mContext, "Update Cart Success", Toast.LENGTH_SHORT).show();
                                                EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                            }

                                            @Override
                                            public void onError(Throwable e) {
                                                Log.e("UPDate_TO_CAERT_ERROR", e.getMessage());
                                            }
                                        });
                            }else{
                                // Item not availabe in cart before inser new
                                mDisposable.add(mCartDataSource.addOrReplaceAll(cartItem)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(()->{
                                            Toast.makeText(mContext, "Add to cart success", Toast.LENGTH_SHORT).show();
                                            // Sned a notify to HomeActivity to update counter
                                            EventBus.getDefault().postSticky(new CounterCartEvent(true));

                                        }, throwable1 -> {
                                            Log.e("ADD_CART_ERROR", throwable1.getMessage());
                                        }));
                            }

                        }

                        @Override
                        public void onError(Throwable e) {
                            if (e.getMessage().contains("empty")){
                                // Default, if Cart is empty this code will be fired
                                mDisposable.add(mCartDataSource.addOrReplaceAll(cartItem)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(()->{
                                            Toast.makeText(mContext, "Add to cart success", Toast.LENGTH_SHORT).show();
                                            // Sned a notify to HomeActivity to update counter
                                            EventBus.getDefault().postSticky(new CounterCartEvent(true));

                                        }, throwable12 -> {
                                            Log.e("ADD_CART_ERROR", throwable12.getMessage());
                                        }));
                            }else
                                Log.e("CHECK_CART_ERROR", e.getMessage());
                        }
                    });
        });

    }

    @Override
    public int getItemCount() {
        return mListFood.size();
    }

    class FoodHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.txt_food_name)
        TextView mTxtFoodName;
        @BindView(R.id.txt_food_price)
        TextView mTxtFoodPrice;
        @BindView(R.id.txt_rating_value)
        TextView mTxtFoodRatingValue;

        @BindView(R.id.img_food_image)
        ImageView mImgFood;
        @BindView(R.id.img_food_add_cart)
        ImageView mImgAddToCart;
        @BindView(R.id.img_food_add_favorite)
        ImageView mImgToFavorite;
        @BindView(R.id.rating_food)
        RatingBar mRatingFood;

        private IRecyclerItemClickLitener recyclerItemClickLitener;


        public FoodHolder(@NonNull View itemView) {
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