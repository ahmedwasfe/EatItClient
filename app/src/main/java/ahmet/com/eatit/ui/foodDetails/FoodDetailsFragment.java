package ahmet.com.eatit.ui.foodDetails;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.andremion.counterfab.CounterFab;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;
import com.syd.oden.circleprogressdialog.view.RotateLoading;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ahmet.com.eatit.CartDatabse.Cart;
import ahmet.com.eatit.CartDatabse.CartDataSource;
import ahmet.com.eatit.CartDatabse.CartDatabase;
import ahmet.com.eatit.CartDatabse.LocalCartDataSource;
import ahmet.com.eatit.common.Common;
import ahmet.com.eatit.eventBus.CounterCartEvent;
import ahmet.com.eatit.eventBus.MenuItemBack;
import ahmet.com.eatit.model.Comment;
import ahmet.com.eatit.model.Food.Addon;
import ahmet.com.eatit.model.Food.Food;
import ahmet.com.eatit.model.Food.FoodSize;
import ahmet.com.eatit.R;
import ahmet.com.eatit.ui.comment.CommentBottomSheetFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class FoodDetailsFragment extends Fragment implements TextWatcher {

    @BindView(R.id.img_detals_food_image)
    ImageView mImgFood;

    @BindView(R.id.txt_details_food_name)
    TextView mTxtFoodName;
    @BindView(R.id.txt_details_food_price)
    TextView mTxtFoodPrice;
    @BindView(R.id.txt_details_food_description)
    TextView mTxtFoodDescription;

    @BindView(R.id.rating_bar)
    RatingBar mRatingBar;

    @BindView(R.id.btn_fab_counter_cart)
    CounterFab mCounterCart;

    @BindView(R.id.radio_group_size)
    RadioGroup mRadioGroupSize;


    @BindView(R.id.btn_elegant_quantity)
    ElegantNumberButton mNumQuantity;


    @BindView(R.id.chip_group_user_select_addon)
    ChipGroup mChipGroupUserSelectedAddon;


    // BootomSheet Addon
    private ChipGroup mChipGroupShowAddon;
    private MaterialEditText mInputSearch;

    private BottomSheetDialog mSheetDialog;

    private CompositeDisposable mDisposable;
    private CartDataSource mCartDataSource;

    @OnClick(R.id.icon_add_addon) void onAddonClick(){
        if (Common.currentFood.getAddon() != null){
            // Show all addon options
            showAddonList();
            mSheetDialog.show();
        }
    }

    private void showAddonList() {

        if (Common.currentFood.getAddon().size() > 0){
            // Clear check all views
            mChipGroupShowAddon.clearCheck();
            mChipGroupShowAddon.removeAllViews();

            mInputSearch.addTextChangedListener(this);

            // Add all views
            for (Addon addon : Common.currentFood.getAddon()){

                    Chip chip = (Chip) getLayoutInflater().inflate(R.layout.layout_addon_item, null);
                    chip.setText(new StringBuilder(addon.getName())
                            .append("(+$")
                            .append(addon.getPrice())
                            .append(")"));
                    chip.setOnCheckedChangeListener((compoundButton, isChecked) ->{
                        if (isChecked)
                            if (Common.currentFood.getUserSelectedAddon() == null)
                                Common.currentFood.setUserSelectedAddon(new ArrayList<>());
                        Common.currentFood.getUserSelectedAddon().add(addon);
                    });
                    mChipGroupShowAddon.addView(chip);
                }
        }
    }

    @OnClick(R.id.btn_fab_rating) void onClickeReating(){
        showSheetDialog();
    }

    @OnClick(R.id.btn_fab_counter_cart) void onAddToCart(){
        addItemToCart();
    }

    @OnClick(R.id.btn_show_comment) void showBottomSheetComment(){
        CommentBottomSheetFragment mCommentFragment = CommentBottomSheetFragment.getInstance();
        mCommentFragment.show(getActivity().getSupportFragmentManager(), "CommentBottomSheetFragment");
    }

    private void addItemToCart() {

        Cart cartItem = new Cart();

        // User
        cartItem.setUserPhone(Common.currentUser.getPhone());
        cartItem.setUserUid(Common.currentUser.getUid());
        // Food
        cartItem.setCategoryId(Common.currentCategory.getMenu_id());
        cartItem.setFoodId(Common.currentFood.getId());
        cartItem.setFoodName(Common.currentFood.getName());
        cartItem.setFoodImage(Common.currentFood.getImage());
        cartItem.setFoodPrice(Double.valueOf(Common.currentFood.getPrice()));
        cartItem.setFoodQuantity(Integer.valueOf(mNumQuantity.getNumber()));

        // Because default not choose size and addon so exrta price is 0
        cartItem.setFoodExtraPrice(Common.calcuateExtraPrice(Common.currentFood.getUserSelectedSize(),
                            Common.currentFood.getUserSelectedAddon()));

        if (Common.currentFood.getUserSelectedAddon() != null)
            cartItem.setFoodAddon(new Gson().toJson(Common.currentFood.getUserSelectedAddon()));
        else
            cartItem.setFoodAddon("Default");

        if (Common.currentFood.getUserSelectedSize() != null)
            cartItem.setFoodSize(new Gson().toJson(Common.currentFood.getUserSelectedSize()));
        else
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
                                            Toast.makeText(getActivity(), "Update Cart Success", Toast.LENGTH_SHORT).show();
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
                                        Toast.makeText(getActivity(), "Add to cart success", Toast.LENGTH_SHORT).show();
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
                                        Toast.makeText(getActivity(), "Add to cart success", Toast.LENGTH_SHORT).show();
                                        // Sned a notify to HomeActivity to update counter
                                        EventBus.getDefault().postSticky(new CounterCartEvent(true));

                                    }, throwable12 -> {
                                        Log.e("ADD_CART_ERROR", throwable12.getMessage());
                                    }));
                        }else
                            Log.e("CHECK_CART_ERROR", e.getMessage());
                    }
                });

    }


    private void showSheetDialog() {

        mSheetDialog = new BottomSheetDialog(getActivity());
        mSheetDialog.setCancelable(false);
        mSheetDialog.setCanceledOnTouchOutside(true);
        mSheetDialog.setTitle("Rate Food");

        View sheetView = LayoutInflater.from(getActivity())
                .inflate(R.layout.layout_rating_food, null);

        RatingBar mRateFood = sheetView.findViewById(R.id.add_rating);
        MaterialEditText mInputComment = sheetView.findViewById(R.id.input_comment_food);
        RotateLoading mLoadingAddComment = sheetView.findViewById(R.id.progress_loading_add_comment);
        Button mBtnAddComment = sheetView.findViewById(R.id.btn_add_comment);


        sheetView.findViewById(R.id.btn_add_comment)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mLoadingAddComment.start();
                        mBtnAddComment.setVisibility(View.GONE);


                        String comment = mInputComment.getText().toString();
                        float rate = mRateFood.getRating();

                        Comment userComment = new Comment();


                        userComment.setComment(comment);
                        userComment.setRateValue(rate);
                        userComment.setName(Common.currentUser.getName());
                        userComment.setUid(Common.currentUser.getUid());
                        Map<String, Object> mMapServerTimeStamp = new HashMap<>();
                        mMapServerTimeStamp.put("timeStamp", ServerValue.TIMESTAMP);
                        userComment.setServerTimeStamp(mMapServerTimeStamp);

                        foodDetailsViewModel.setComment(userComment);
                        mSheetDialog.dismiss();
                    }
                });

        mLoadingAddComment.stop();
        mBtnAddComment.setVisibility(View.VISIBLE);

        mSheetDialog.setContentView(sheetView);
        mSheetDialog.show();
    }

    private FoodDetailsViewModel foodDetailsViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        foodDetailsViewModel = ViewModelProviders.of(this).get(FoodDetailsViewModel.class);

        View layoutView = inflater.inflate(R.layout.fragment_food_details, container, false);
        ButterKnife.bind(this, layoutView);

        foodDetailsViewModel.getmMutableFood()
                .observe(this, food -> {
                    loadFoodDetails(food);
                });

        foodDetailsViewModel.getmMutableComment()
                .observe(this, comment -> {
                    submmitReting(comment);
                });



        init();

        return layoutView;
    }

    private void submmitReting(Comment comment) {

        // Will submit to Comment ref
        FirebaseDatabase.getInstance().getReference()
                .child(Common.KEY_COMMENT_REFERANCE)
                .child(Common.currentFood.getId())
                .push()
                .setValue(comment)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        // After submit to comment ref will update value avarge in food
                        addRateingToFood(comment.getRateValue());
                    }
                }).addOnFailureListener(e -> Log.e("COMMENT_ERROR", e.getMessage()));
    }

    private void addRateingToFood(float rateValue) {

        FirebaseDatabase.getInstance().getReference(Common.KEY_CAEGORIES_REFERANCE)
                .child(Common.currentCategory.getMenu_id())
                .child(Common.KEY_FOOD_CHILD)
                .child(Common.currentFood.getKey())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            Food food = dataSnapshot.getValue(Food.class);
                            food.setKey(Common.currentFood.getKey());
                            // Apply reting
                            if (food.getRateValue() == null)
                                food.setRateValue(0d);
                            if (food.getRateCount() == null)
                                food.setRateCount(0l);

                            double sumRate = food.getRateValue() + rateValue;
                            long rateCount = food.getRateCount() + 1;
                           // double rateResult = sumRate/rateCount;

                            Map<String, Object> mMapRate = new HashMap<>();
                            mMapRate.put("rateValue", sumRate);
                            mMapRate.put("rateCount", rateCount);

                            // Update datain variable
                            food.setRateValue(sumRate);
                            food.setRateCount(rateCount);

                            dataSnapshot.getRef()
                                    .updateChildren(mMapRate)
                                    .addOnCompleteListener((task) -> {
                                        if (task.isSuccessful()){
                                            Toast.makeText(getActivity(), getString(R.string.thank_you), Toast.LENGTH_SHORT).show();
                                            Common.currentFood = food;
                                            foodDetailsViewModel.setFood(food); // Call refresh
                                        }
                                    }).addOnFailureListener(e -> Log.e("ADD_TO_FOOD_ERROR", e.getMessage()));
                        }else{
                            Log.e("ADD_TO_FOOD_ERROR", dataSnapshot.getKey());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("RATE_FOOD", databaseError.getMessage());
                    }
                });
    }

    private void init() {

        ((AppCompatActivity)getActivity())
                .getSupportActionBar()
                .setTitle(Common.currentFood.getName());

        mCartDataSource = new LocalCartDataSource(CartDatabase.getInstance(getActivity()).cartDAO());
        mDisposable = new CompositeDisposable();

        mSheetDialog = new BottomSheetDialog(getActivity(), R.style.DialogStyle);
        View sheetView = getLayoutInflater().inflate(R.layout.layout_addon, null);

        mChipGroupShowAddon = sheetView.findViewById(R.id.chip_group_show_addon);
        mInputSearch = sheetView.findViewById(R.id.input_search_addon);

        mSheetDialog.setContentView(sheetView);

        mSheetDialog.setOnDismissListener(dialog -> {
            showUserSelectedAddon();
            calculatTotalPrice();
        });

        mNumQuantity.setOnValueChangeListener((view, oldValue, newValue) -> calculatTotalPrice());

    }

    private void showUserSelectedAddon() {

        if (Common.currentFood.getUserSelectedAddon() != null &&
            Common.currentFood.getUserSelectedAddon().size() > 0){
            // Clear all view already added
            mChipGroupUserSelectedAddon.removeAllViews();
            // Add all available addon to list
            for (Addon addon : Common.currentFood.getUserSelectedAddon()){
                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.layout_chip_with_delete_icon_addon, null);
                chip.setText(new StringBuilder(addon.getName())
                            .append("(+$")
                            .append(addon.getPrice())
                            .append(")"));
                chip.setChecked(false);
                chip.setOnCloseIconClickListener(view -> {
                    // Remove when user select delete
                    mChipGroupUserSelectedAddon.removeView(view);
                    Common.currentFood.getUserSelectedAddon().remove(addon);
                    calculatTotalPrice();
                });
                mChipGroupUserSelectedAddon.addView(chip);
            }
        }else{
            mChipGroupUserSelectedAddon.removeAllViews();

        }


    }

    private void loadFoodDetails(Food food) {

        Picasso.get()
                .load(food.getImage())
                .into(mImgFood);
        mTxtFoodName.setText(new StringBuilder(food.getName()));
        mTxtFoodPrice.setText(new StringBuilder(food.getPrice().toString()));
        mTxtFoodDescription.setText(new StringBuilder(food.getDescription()));

        if (food.getRateValue() != null)
            mRatingBar.setRating(Common.currentFood.getRateValue().floatValue() / food.getRateCount());

        // load size food
        for(FoodSize foodSize : Common.currentFood.getSize()){
            RadioButton mRadioButton = new RadioButton(getActivity());
            mRadioButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked)
                    Common.currentFood.setUserSelectedSize(foodSize);
                // Update price
                calculatTotalPrice();
            });

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1.0f);
            mRadioButton.setLayoutParams(params);
            mRadioButton.setText(foodSize.getName());
            mRadioButton.setTag(foodSize.getPrice());
            mRadioGroupSize.addView(mRadioButton);
        }

        if (mRadioGroupSize.getChildCount() > 0){
            RadioButton radioButton = (RadioButton) mRadioGroupSize.getChildAt(0);
            radioButton.setChecked(true);  // Default first select
        }

        calculatTotalPrice();
    }

    private void calculatTotalPrice() {

        double totalPrice = Double.parseDouble(Common.currentFood.getPrice().toString());
        double displayPrice = 0.0;

        // Addon
        if (Common.currentFood.getUserSelectedAddon() != null &&
            Common.currentFood.getUserSelectedAddon().size() > 0)
            for (Addon addon : Common.currentFood.getUserSelectedAddon())
                totalPrice += Double.parseDouble(addon.getPrice().toString());

        // Size
        if (Common.currentFood.getUserSelectedSize() != null)
            totalPrice += Double.parseDouble(Common.currentFood.getUserSelectedSize().getPrice().toString());

        displayPrice = totalPrice * (Integer.parseInt(mNumQuantity.getNumber()));
        displayPrice = Math.round(displayPrice*100.0/100.0);

        mTxtFoodPrice.setText(new StringBuilder("")
                            .append(Common.formatFoodPrice(displayPrice))
                            .toString());

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        ///
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
        mChipGroupShowAddon.clearCheck();
        mChipGroupShowAddon.removeAllViews();

        for (Addon addon : Common.currentFood.getAddon()){
            if (addon.getName().toLowerCase().contains(charSequence.toString().toLowerCase())){
                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.layout_addon_item, null);
                chip.setText(new StringBuilder(addon.getName())
                        .append("(+$")
                        .append(addon.getPrice())
                        .append(")"));
                chip.setOnCheckedChangeListener((compoundButton, isChecked) ->{
                    if (isChecked)
                        if (Common.currentFood.getUserSelectedAddon() == null)
                            Common.currentFood.setUserSelectedAddon(new ArrayList<>());
                        Common.currentFood.getUserSelectedAddon().add(addon);
                });
                mChipGroupShowAddon.addView(chip);
            }
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        ///
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new MenuItemBack());
        mDisposable.dispose();
        super.onDestroy();
    }
}
