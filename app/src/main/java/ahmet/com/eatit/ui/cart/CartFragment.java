package ahmet.com.eatit.ui.cart;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.braintreepayments.api.dropin.DropInRequest;
import com.braintreepayments.api.dropin.DropInResult;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ahmet.com.eatit.adapter.CartAdapter;
import ahmet.com.eatit.callback.ILoadTimeFromFirebaseListenert;
import ahmet.com.eatit.CartDatabse.Cart;
import ahmet.com.eatit.CartDatabse.CartDataSource;
import ahmet.com.eatit.CartDatabse.CartDatabase;
import ahmet.com.eatit.CartDatabse.LocalCartDataSource;
import ahmet.com.eatit.callback.ISearchCategoryCallbackLitener;
import ahmet.com.eatit.common.Common;
import ahmet.com.eatit.common.SwipeRecyclerHelper;
import ahmet.com.eatit.eventBus.CounterCartEvent;
import ahmet.com.eatit.eventBus.HideFABCart;
import ahmet.com.eatit.eventBus.MenuItemBack;
import ahmet.com.eatit.eventBus.UpdateFoodQuantityInCart;
import ahmet.com.eatit.model.Category;
import ahmet.com.eatit.model.Food.Addon;
import ahmet.com.eatit.model.Food.Food;
import ahmet.com.eatit.model.Food.FoodSize;
import ahmet.com.eatit.model.Order;
import ahmet.com.eatit.model.ServiceModel.FCMSendData;
import ahmet.com.eatit.R;
import ahmet.com.eatit.services.ICloudFunctions;
import ahmet.com.eatit.services.IFCMService;
import ahmet.com.eatit.services.RetrofitFCMClient;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

import static android.app.Activity.RESULT_OK;

public class CartFragment extends Fragment implements ILoadTimeFromFirebaseListenert, ISearchCategoryCallbackLitener, TextWatcher {


    private static final String TAG = "UPDATE_TOTAL_ERROR";

    @BindView(R.id.recycler_cart)
    RecyclerView mRecyclerCart;
    @BindView(R.id.constraint_meesage_cart_empty)
    ConstraintLayout mConstraintCartEmpty;
    @BindView(R.id.card_place_order)
    CardView mCardPlaceOrder;
    @BindView(R.id.txt_total_price)
    TextView mTxtTotalPrice;

    private String address, comment;

    private Parcelable mParcelableRecyclerState;

    private CompositeDisposable mDisposable;
    private CartDataSource mCartDataSource;

    private CartViewModel mCartViewModel;

    private CartAdapter cartAdapter;

    private IFCMService mIfcmService;
    private ICloudFunctions mICloudFunctions;

    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private FusedLocationProviderClient mFusedClient;
    private Location mCurrentLocation;

    private AutocompleteSupportFragment mAutocompleteFragment;
    private Place mPlaceSelcted;
    private PlacesClient mPlacesClient;
    private List<Place.Field> mListPlaceFields;


    private ILoadTimeFromFirebaseListenert mTimeListenert;
    private ISearchCategoryCallbackLitener searchCategoryLitener;

    private BottomSheetDialog mAddonBottomSheetDialog;
    private ChipGroup mChipGroupAddon, mChipGroupUserSelectedAddon;
    private EditText mInputSearchAddon;

    @OnClick(R.id.btn_place_order) void onPlaceOrderClick(){
        showDialoToPlaceOrder();
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mCartViewModel = ViewModelProviders.of(this).get(CartViewModel.class);

        View layoutView = inflater.inflate(R.layout.fragment_cart, container, false);


        ButterKnife.bind(this, layoutView);

        initViews();
        initLocation();

        mCartViewModel.getMutableLiveDataCart()
                .observe(this, carts -> {
                    if (carts == null || carts.isEmpty()){
                        mRecyclerCart.setVisibility(View.GONE);
                        mCardPlaceOrder.setVisibility(View.GONE);
                        mConstraintCartEmpty.setVisibility(View.VISIBLE);
                    }else {
                        mRecyclerCart.setVisibility(View.VISIBLE);
                        mCardPlaceOrder.setVisibility(View.VISIBLE);
                        mConstraintCartEmpty.setVisibility(View.GONE);

                        cartAdapter = new CartAdapter(getActivity(), carts);
                        mRecyclerCart.setAdapter(cartAdapter);
                    }
                });


        return layoutView;
    }

    private void initLocation() {

        buildLocationRequest();
        buildLocationCallback();
        mFusedClient = LocationServices.getFusedLocationProviderClient(getActivity());
        mFusedClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.getMainLooper());

        Places.initialize(getActivity(), getString(R.string.google_maps_key));
        mPlacesClient = Places.createClient(getActivity());
        mListPlaceFields = Arrays.asList(Place.Field.ID, Place.Field.NAME,
                Place.Field.ADDRESS, Place.Field.LAT_LNG);

    }

    private void buildLocationCallback() {

        mLocationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                mCurrentLocation = locationResult.getLastLocation();
            }
        };
    }

    private void buildLocationRequest() {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setSmallestDisplacement(10f);
    }

    private void initViews() {

        setHasOptionsMenu(true);

        mIfcmService = RetrofitFCMClient.getRetrofit().create(IFCMService.class);
        mICloudFunctions = RetrofitFCMClient.getRetrofit().create(ICloudFunctions.class);

        mDisposable = new CompositeDisposable();
        mCartDataSource = new LocalCartDataSource(CartDatabase.getInstance(getActivity()).cartDAO());

        mCartViewModel.initCartDataSource(getActivity());

        EventBus.getDefault().postSticky(new HideFABCart(true));

        mTimeListenert = this;
        searchCategoryLitener = this;

        mRecyclerCart.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerCart.setLayoutManager(layoutManager);
        mRecyclerCart.addItemDecoration(new DividerItemDecoration(getActivity(), layoutManager.getOrientation()));


        SwipeRecyclerHelper swipeRecyclerView = new SwipeRecyclerHelper(
                getActivity(), mRecyclerCart, 200) {
            @Override
            public void instantiateButton(RecyclerView.ViewHolder viewHolder, List<MButton> mListMButton) {

                mListMButton.add(new MButton(getActivity(), getString(R.string.delete),
                        30, 0, Color.parseColor("#FF3C30"), position -> {

                    Cart cart = cartAdapter.getItemAtPosition(position);
                    mCartDataSource.deleteItemFromCart(cart)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new SingleObserver<Integer>() {
                                @Override
                                public void onSubscribe(Disposable d) {

                                }

                                @Override
                                public void onSuccess(Integer integer) {
                                    cartAdapter.notifyItemRemoved(position);
                                    sumAllItemInCart();
                                    EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                    Toast.makeText(getActivity(), "Delete item from cart successful", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onError(Throwable e) {
                                    Log.e("DELETE_FROM_CART_ERROR", e.getMessage());
                                }
                            });

                }));

                mListMButton.add(new MButton(getActivity(),getString(R.string.size_addon),30,0,
                        getActivity().getColor(R.color.colorGreen), position -> {

                    Cart cart = cartAdapter.getItemAtPosition(position);
                    FirebaseDatabase.getInstance().getReference()
                            .child(Common.KEY_CAEGORIES_REFERANCE)
                            .child(cart.getCategoryId())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()){
                                        Category category = dataSnapshot.getValue(Category.class);
                                        searchCategoryLitener.onSearchCategoryFound(category, cart);

                                    }else{
                                        searchCategoryLitener.onSearchCategoryNotFound(getString(R.string.category_not_found));
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    searchCategoryLitener.onSearchCategoryNotFound(databaseError.getMessage());
                                }
                            });

                }));
            }
        };



        sumAllItemInCart();

        // Addon
        mAddonBottomSheetDialog = new BottomSheetDialog(getActivity(), R.style.DialogStyle);
        View layoutAddonView = getLayoutInflater().inflate(R.layout.layout_addon, null);
        mChipGroupAddon = layoutAddonView.findViewById(R.id.chip_group_show_addon);
        mInputSearchAddon = layoutAddonView.findViewById(R.id.input_search_addon);
        mAddonBottomSheetDialog.setContentView(layoutAddonView);

        mAddonBottomSheetDialog.setDismissWithAnimation(true);
        mAddonBottomSheetDialog.setOnDismissListener(dialog -> {
            displayUserSelectedAddon(mChipGroupUserSelectedAddon);
            calculateTotalPrice();
        });
    }

    private void displayUserSelectedAddon(ChipGroup mChipGroupUserSelectedAddon) {

        if (Common.currentFood.getUserSelectedAddon() != null &&
                Common.currentFood.getUserSelectedAddon().size() > 0){
            mChipGroupUserSelectedAddon.removeAllViews();
            for (Addon addon : Common.currentFood.getUserSelectedAddon()){
                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.layout_chip_with_delete_icon_addon, null);
                chip.setText(new StringBuilder(addon.getName()).append("(+$")
                        .append(addon.getPrice()).append(")"));
                chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked){
                        if (Common.currentFood.getUserSelectedAddon() == null)
                            Common.currentFood.setUserSelectedAddon(new ArrayList<>());
                        Common.currentFood.getUserSelectedAddon().add(addon);
                    }
                });
                mChipGroupUserSelectedAddon.addView(chip);
            }
        }else
            mChipGroupUserSelectedAddon.removeAllViews();
    }

    private void sumAllItemInCart() {

        mCartDataSource.sumPriceInCart(Common.currentUser.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Double>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Double aDouble) {
                        mTxtTotalPrice.setText(new StringBuilder("Total: $").append(aDouble));
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (!e.getMessage().contains("Query returned empty"))
                            Log.e("SUM_ALL_PRICE_ERROR", e.getMessage());
                    }
                });
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        // Hide Home menu already inflate
        menu.findItem(R.id.action_clear_cart).setVisible(true);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.cart_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void showDialoToPlaceOrder(){

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.on_more_step);
        View layoutView = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_place_order, null);

        TextView mTxtAddress = layoutView.findViewById(R.id.text_address_details);
       // MaterialEditText mInputAddress = layoutView.findViewById(R.id.input_address);
        MaterialEditText mInputComment = layoutView.findViewById(R.id.input_comment);
        RadioButton mRadioHomeAddress = layoutView.findViewById(R.id.radio_home_address);
        RadioButton mRadioOtherAddress = layoutView.findViewById(R.id.radio_other_address);
        RadioButton mRadioShipThisAddress = layoutView.findViewById(R.id.radio_ship_this_address);
        RadioButton mRadioCod = layoutView.findViewById(R.id.radio_cod);
        RadioButton mRadioBraintree = layoutView.findViewById(R.id.radio_braintree);

        mAutocompleteFragment = (AutocompleteSupportFragment) getActivity().getSupportFragmentManager()
                .findFragmentById(R.id.autocomplete_places_fragment_palce_order);
        mAutocompleteFragment.setPlaceFields(mListPlaceFields);
        mAutocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                mPlaceSelcted = place;
                mTxtAddress.setText(place.getAddress());
            }

            @Override
            public void onError(@NonNull Status status) {
               // Log.e("PLACES_ERROR", status.getStatusMessage());
                Toast.makeText(getActivity(), ""+status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        });


        // Data
        // By Default we select home address, so user's address wil display
        mTxtAddress.setText(Common.currentUser.getAddress());

        // Event
        mRadioHomeAddress.setOnCheckedChangeListener(((buttonView, isChecked) -> {
                if (isChecked) {
                    mTxtAddress.setText(Common.currentUser.getAddress());
                    mTxtAddress.setVisibility(View.VISIBLE);
                    mAutocompleteFragment.setHint(Common.currentUser.getAddress());
                }
        }));

        mRadioOtherAddress.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            if (isChecked) {
                mTxtAddress.setVisibility(View.VISIBLE);
            }
        }));

        mRadioShipThisAddress.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            if (isChecked)
                getCurrentAddress(mTxtAddress);
        }));

        builder.setView(layoutView);
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
            dialog.dismiss();
        }).setPositiveButton(R.string.place_order, (dialog, which) -> {
           // Toast.makeText(getActivity(), "Implement late", Toast.LENGTH_SHORT).show();
            if (mRadioCod.isChecked()) {
                String address = mTxtAddress.getText().toString();
                String comment = mInputComment.getText().toString();
                paymentCOD(address, comment);
            }else if (mRadioBraintree.isChecked()){
                address = mTxtAddress.getText().toString();
                comment = mInputComment.getText().toString();
                if (!TextUtils.isEmpty(Common.currentToken)){
                    DropInRequest dropInRequest = new DropInRequest().clientToken(Common.currentToken);
                    startActivityForResult(dropInRequest.getIntent(getActivity()), Common.CODE_REQUEST_PAYMENT_BRAINTREE);
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void paymentCOD(String address, String comment) {

        mDisposable.add(mCartDataSource.getAllCarts(Common.currentUser.getUid())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(carts -> {

                            // When we have all Cart Items we will get total price
                            mCartDataSource.sumPriceInCart(Common.currentUser.getUid())
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new SingleObserver<Double>() {
                                        @Override
                                        public void onSubscribe(Disposable d) {

                                        }

                                        @Override
                                        public void onSuccess(Double totalPrice) {
                                            // we will modify this formiula for discount late
                                            double finalPrice = totalPrice;
                                            Order order = new Order();
                                            // User
                                            order.setUserId(Common.currentUser.getUid());
                                            order.setUserName(Common.currentUser.getName());
                                            order.setUserPhone(Common.currentUser.getPhone());
                                            // Shipping
                                            order.setShippingAddress(address);
                                            order.setComment(comment);
                                            // Location
                                            if (mCurrentLocation != null){
                                                order.setLat(mCurrentLocation.getLatitude());
                                                order.setLng(mCurrentLocation.getLongitude());
                                            }else {
                                                order.setLat(-0.1f);
                                                order.setLng(-0.1f);
                                            }
                                            // Cart
                                            order.setCarts(carts);
                                            // Price
                                            order.setTotalPayment(totalPrice);
                                            order.setDiscount(0); // Modify with discount late
                                            order.setFinalPayment(finalPrice);
                                            order.setCod(true);
                                            // Payment Method
                                            order.setTransactionId("Cash On Delivery");

                                            // Submit this order object to firebase
                                            syncLocalTimeWithGlobalTime(order);

                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            if (!e.getMessage().contains("Query returned empty result set"))
                                                Log.e("GET_SUM_PRICE_TO_ORDER", e.getMessage());
                                        }
                                    });

                        }, throwable -> {

                        }));
    }

    private void syncLocalTimeWithGlobalTime(Order order) {

        DatabaseReference offsetRef = FirebaseDatabase.getInstance()
                .getReference(".info/serverTimeOffset");
        offsetRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long offset = dataSnapshot.getValue(Long.class);
                // offset is missing time between your local time and server time
                long estimateTimeInMs = System.currentTimeMillis() + offset;
                SimpleDateFormat format = new SimpleDateFormat("MMM dd,yyyy HH:mm");
                Date resDate = new Date(estimateTimeInMs);
                Log.d("FORMAT_DATE", format.format(resDate));

                mTimeListenert.onLoadTimeSuccess(order, estimateTimeInMs);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                mTimeListenert.onLoadTimeFailed(databaseError.getMessage());
            }
        });
    }

    private void writeOrderToDatabase(Order order) {

        FirebaseDatabase.getInstance().getReference()
                .child(Common.KEY_ORDER_REFERANCE)
                // Create order key with only digit
                .child(Common.createOrderKey())
                .setValue(order)
                .addOnFailureListener(e -> {
                    Log.e("WRITE_ORDER_TO_DATABASE", e.getMessage());
                }).addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        mCartDataSource.clearCart(Common.currentUser.getUid())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new SingleObserver<Integer>() {
                                    @Override
                                    public void onSubscribe(Disposable d) {

                                    }

                                    @Override
                                    public void onSuccess(Integer integer) {

                                        Map<String, String> mMapNotificationData = new HashMap<>();
                                        mMapNotificationData.put(Common.KEY_NOTFI_TITLE, getString(R.string.title_order));
                                        mMapNotificationData.put(Common.KEY_NOTFI_CONTENT, new StringBuilder(getString(R.string.content_order))
                                                            .append(" ").append(Common.currentUser.getPhone()).toString());
                                        FCMSendData fcmSendData = new FCMSendData(Common.createTopicOrder(), mMapNotificationData);
                                        mDisposable.add(mIfcmService.sendNotification(fcmSendData)
                                                    .subscribeOn(Schedulers.io())
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribe(fcmResponse -> {
                                                        Toast.makeText(getActivity(), "Order placed successfully", Toast.LENGTH_SHORT).show();
                                                        EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                                    }, throwable -> {
                                                        Toast.makeText(getActivity(), "Order was sent but failure to send notification", Toast.LENGTH_SHORT).show();
                                                        EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                                    }));

                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        Log.e("CLEAR_CART_ERROR", e.getMessage());
                                    }
                                });
                    }
                });

    }

    private void getCurrentAddress(TextView mTxtAddress) {

        mFusedClient.getLastLocation()
                .addOnFailureListener(e -> {
                    Log.e("CURRENT_LOCATION_ERROR", e.getMessage());
                    mTxtAddress.setVisibility(View.GONE);
                }).addOnCompleteListener(task -> {
                    String coordinates = new StringBuilder()
                            .append(task.getResult().getLatitude())
                            .append(",")
                            .append(task.getResult().getLongitude())
                            .toString();

                    Single<String> singleAddress = Single.just(getAddressFromLatLng(task.getResult().getLatitude(),
                            task.getResult().getLongitude()));
                    Disposable disposable = singleAddress.subscribeWith(new DisposableSingleObserver<String>(){

                        @Override
                        public void onSuccess(String s) {
                           // mTxtAddress.setText(coordinates);
                            mTxtAddress.setText(s);
                            mTxtAddress.setVisibility(View.VISIBLE);
                            mAutocompleteFragment.setHint(s);
                        }

                        @Override
                        public void onError(Throwable e) {
                            //mInputAddress.setText(coordinates);
                            mTxtAddress.setText(e.getMessage());
                            mTxtAddress.setVisibility(View.VISIBLE);
                            Log.e("SINGLE_ADDRESS_ERROR", e.getMessage());
                        }
                    });


                });
    }

    private String getAddressFromLatLng(double latitude, double longitude) {

        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
        String result = "";
        try{
            List<Address> mListAddress = geocoder.getFromLocation(latitude, longitude,1);
            if (mListAddress != null && mListAddress.size() > 0){
                // Always get first item
                Address address = mListAddress.get(0);
                StringBuilder strBuilder = new StringBuilder(address.getAddressLine(0))
                            .append("\n")
                            .append(address.getCountryName());
                result = strBuilder.toString();
                Log.e("COUNTRY", address.getCountryName());
            }else
                result = "Address not found";
        } catch (IOException e) {
            e.printStackTrace();
            result = e.getMessage();
            Log.e("GET_ADDRESS_ERROR", e.getMessage());
        }
        return result;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_clear_cart){

            clearAllCart();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void clearAllCart() {

        mCartDataSource.clearCart(Common.currentUser.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Integer integer) {
                        Toast.makeText(getActivity(), "Clear cart success", Toast.LENGTH_SHORT).show();
                        EventBus.getDefault().postSticky(new CounterCartEvent(true));
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("CLEAR_ALL_CART_ERROR", e.getMessage());
                    }
                });
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onUpdateQuantity(UpdateFoodQuantityInCart event) {
        if (event.getCart() != null) {
            // First save recyclerView state
            mParcelableRecyclerState = mRecyclerCart.getLayoutManager().onSaveInstanceState();
            mCartDataSource.updateItemCart(event.getCart())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Integer>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(Integer integer) {
                            calculateTotalPrice();
                            // Fix error refresh recycler VIew after update
                            mRecyclerCart.getLayoutManager().onRestoreInstanceState(mParcelableRecyclerState);
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e(TAG, e.getMessage());
                        }
                    });
        }
    }


    private void calculateTotalPrice() {

        mCartDataSource.sumPriceInCart(Common.currentUser.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Double>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Double totalPrice) {
                        mTxtTotalPrice.setText(new StringBuilder("Total: $")
                                .append(Common.formatFoodPrice(totalPrice)));
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (!e.getMessage().contains("Query returned empty result set"))
                            Log.e("SUM_TOTAL_PRICE", e.getMessage());
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Common.CODE_REQUEST_PAYMENT_BRAINTREE)
            if (resultCode == RESULT_OK){
                DropInResult dropInResult = data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
                PaymentMethodNonce nonce = dropInResult.getPaymentMethodNonce();

                // calculat sum cart
                mCartDataSource.sumPriceInCart(Common.currentUser.getUid())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new SingleObserver<Double>() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onSuccess(Double totalPrice) {
                                // get all item in cart to create order
                                mDisposable.add(mCartDataSource.getAllCarts(Common.currentUser.getUid())
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(carts -> {
                                                // submit payment
                                                Map<String, String> headers = new HashMap<>();
                                                headers.put("Authorization", Common.buildToken(Common.authorizeKey));

                                                mDisposable.add(mICloudFunctions.submitPayment(headers, totalPrice, nonce.getNonce())
                                                            .subscribeOn(Schedulers.io())
                                                            .observeOn(AndroidSchedulers.mainThread())
                                                            .subscribe(braintreeTransaction -> {
                                                                if (braintreeTransaction.isSuccess()){

                                                                    double finalPrice = totalPrice;
                                                                    Order order = new Order();
                                                                    // User
                                                                    order.setUserId(Common.currentUser.getUid());
                                                                    order.setUserName(Common.currentUser.getName());
                                                                    order.setUserPhone(Common.currentUser.getPhone());
                                                                    // Shipping
                                                                    order.setShippingAddress(address);
                                                                    order.setComment(comment);
                                                                    // Location
                                                                    if (mCurrentLocation != null){
                                                                        order.setLat(mCurrentLocation.getLatitude());
                                                                        order.setLng(mCurrentLocation.getLongitude());
                                                                    }else {
                                                                        order.setLat(-0.1f);
                                                                        order.setLng(-0.1f);
                                                                    }
                                                                    // Cart
                                                                    order.setCarts(carts);
                                                                    // Price
                                                                    order.setTotalPayment(totalPrice);
                                                                    order.setDiscount(0); // Modify with discount late
                                                                    order.setFinalPayment(finalPrice);
                                                                    order.setCod(false);
                                                                    // Payment Method
                                                                    order.setTransactionId(braintreeTransaction.getTransaction().getId());

                                                                    // Submit this order object to firebase
                                                                    syncLocalTimeWithGlobalTime(order);

                                                                }
                                                            }, throwable -> {
                                                                Toast.makeText(getActivity(), ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                            }));
                                            }, throwable -> {
                                                Toast.makeText(getActivity(), ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                            }));

                            }

                            @Override
                            public void onError(Throwable e) {
                                Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mFusedClient != null)
            mFusedClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.getMainLooper());
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().postSticky(new HideFABCart(false));
        mCartViewModel.onStop();

        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);

        if (mFusedClient != null)
            mFusedClient.removeLocationUpdates(mLocationCallback);

        super.onStop();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new MenuItemBack());
        mDisposable.dispose();
        super.onDestroy();
    }


    @Override
    public void onLoadTimeSuccess(Order order, long estimateTimeInMs) {
        order.setDate(estimateTimeInMs);
        order.setOrderStatus(0);
        writeOrderToDatabase(order);
    }

    @Override
    public void onLoadTimeFailed(String error) {
        Log.e("LOAD_TIME_ORDER_ERROR", error);
    }

    @Override
    public void onSearchCategoryFound(Category category, Cart cart) {

        Food food = Common.findFoodListById(category, cart.getFoodId());
        if (food != null){
            showUpdateDialog(cart, food);
        }else
            Toast.makeText(getActivity(), getString(R.string.food_id_not_found), Toast.LENGTH_SHORT).show();
    }

    private void showUpdateDialog(Cart cart, Food food) {

        Common.currentFood = food;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View dialogView = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_update_addon_size, null);
        builder.setView(dialogView);

        RadioGroup mRadioGroupSize = dialogView.findViewById(R.id.g_update_cart_size);
        mChipGroupUserSelectedAddon = dialogView.findViewById(R.id.chip_group_selected_update_addon);


        dialogView.findViewById(R.id.icon_update_cart_addon)
                .setOnClickListener(v -> {
                    if (food.getAddon() != null){
                        displayAddonList();
                        mAddonBottomSheetDialog.show();
                    }
                });

        // Size
        if (food.getSize() != null){
            for (FoodSize foodSize : food.getSize()){
                RadioButton radioButton = new RadioButton(getActivity());
                radioButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked)
                        Common.currentFood.setUserSelectedSize(foodSize);
                    calculateTotalPrice();
                });
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT,1.0f);
                radioButton.setLayoutParams(params);
                radioButton.setText(foodSize.getName());
                radioButton.setTag(foodSize.getPrice());

                mRadioGroupSize.addView(radioButton);
            }

            if (mRadioGroupSize.getChildCount() > 0){
                // get first ite,
                RadioButton radioButton = (RadioButton) mRadioGroupSize.getChildAt(0);
                // Set default at first item
                radioButton.setChecked(true);
            }
        }

        // Addon
        displayAlreadySelectedAddon(mChipGroupUserSelectedAddon, cart);

        // Show dialog
        AlertDialog dialog = builder.create();
        dialog.show();
        // Coustm dialog
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.CENTER);
        //Event

        dialog.findViewById(R.id.btn_ok_update_cart_addon_size)
                .setOnClickListener(v -> {

                    // First, delete item in cart
                    mCartDataSource.deleteItemFromCart(cart)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new SingleObserver<Integer>() {
                                @Override
                                public void onSubscribe(Disposable d) {

                                }

                                @Override
                                public void onSuccess(Integer integer) {
                                    // After that, update information, and add new
                                    // Update price and info

                                    // Addon
                                    if (Common.currentFood.getUserSelectedAddon() != null)
                                        cart.setFoodAddon(new Gson().toJson(Common.currentFood.getUserSelectedAddon()));
                                    else
                                        cart.setFoodAddon(getString(R.string.default_addon));

                                    // Size
                                    if (Common.currentFood.getUserSelectedSize() != null)
                                        cart.setFoodSize(new Gson().toJson(Common.currentFood.getUserSelectedSize()));
                                    else
                                        cart.setFoodSize(getString(R.string.default_addon));

                                    cart.setFoodExtraPrice(Common.calcuateExtraPrice(Common.currentFood.getUserSelectedSize(),
                                            Common.currentFood.getUserSelectedAddon()));

                                    // Insert new to cart
                                    mDisposable.add(mCartDataSource.addOrReplaceAll(cart)
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(() -> {
                                                    EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                                    calculateTotalPrice();
                                                    dialog.dismiss();
                                                    Toast.makeText(getActivity(), getString(R.string.update_cart_success), Toast.LENGTH_SHORT).show();
                                                }, throwable -> {
                                                    Log.e("UPDATE_CART_ERROR", throwable.getMessage());
                                                }));
                                }

                                @Override
                                public void onError(Throwable e) {
                                    Log.e("DELETE_ITEM_ERROR", e.getMessage());
                                }
                            });
                });

        dialog.findViewById(R.id.btn_cancel_update_cart_addon_size)
                .setOnClickListener(v -> {
                    dialog.dismiss();
                });

    }

    private void displayAlreadySelectedAddon(ChipGroup mChipGroupUserSelectedAddon, Cart cart) {
        // This function will display all addon we already selected before add to cart and display on layout
        if (cart.getFoodAddon() != null && !cart.getFoodAddon().equals(getString(R.string.default_addon))){

            List<Addon> listAddon = new Gson().fromJson(cart.getFoodAddon(),
                    new TypeToken<List<Addon>>(){}.getType());
            Common.currentFood.setUserSelectedAddon(listAddon);
            mChipGroupUserSelectedAddon.removeAllViews();
            // Add all view
            // Get addon from alrady waht user have select in cart
            for (Addon addon : listAddon){
                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.layout_chip_with_delete_icon_addon, null);
                chip.setText(new StringBuilder(addon.getName()).append("(+$")
                        .append(addon.getPrice()).append(")"));
                chip.setChecked(false);
                chip.setOnCloseIconClickListener(v -> {
                    // Reomve when user seletc delete icon
                    mChipGroupUserSelectedAddon.removeView(v);
                    Common.currentFood.getUserSelectedAddon().remove(addon);
                    calculateTotalPrice();
                });
                mChipGroupUserSelectedAddon.addView(chip);
            }
        }
    }

    private void displayAddonList() {

        if (Common.currentFood.getAddon() != null && Common.currentFood.getAddon().size() > 0){
            mChipGroupAddon.clearCheck();
            mChipGroupAddon.removeAllViews();

            mInputSearchAddon.addTextChangedListener(this);

            // Add all view
            for (Addon addon : Common.currentFood.getAddon()){
                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.layout_addon_item, null);
                chip.setText(new StringBuilder(addon.getName()).append("(+$")
                        .append(addon.getPrice()).append(")"));
                chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked){
                        if (Common.currentFood.getUserSelectedAddon() == null)
                            Common.currentFood.setUserSelectedAddon(new ArrayList<>());
                        Common.currentFood.getUserSelectedAddon().add(addon);
                    }
                });
                mChipGroupAddon.addView(chip);
            }
        }
    }

    @Override
    public void onSearchCategoryNotFound(String error) {
        Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        mChipGroupAddon.clearCheck();
        mChipGroupAddon.removeAllViews();

        for (Addon addon : Common.currentFood.getAddon()){
            if (addon.getName().toLowerCase().contains(s.toString().toLowerCase())){
                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.layout_addon_item, null);
                chip.setText(new StringBuilder(addon.getName()).append("(+$")
                        .append(addon.getPrice()).append(")"));
                chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked){
                        if (Common.currentFood.getUserSelectedAddon() == null)
                            Common.currentFood.setUserSelectedAddon(new ArrayList<>());
                        Common.currentFood.getUserSelectedAddon().add(addon);
                    }
                });
                mChipGroupAddon.addView(chip);
            }
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
