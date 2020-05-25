package ahmet.com.eatit;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;
import com.syd.oden.circleprogressdialog.view.RotateLoading;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ahmet.com.eatit.CartDatabse.CartDataSource;
import ahmet.com.eatit.CartDatabse.CartDatabase;
import ahmet.com.eatit.CartDatabse.LocalCartDataSource;
import ahmet.com.eatit.common.Common;
import ahmet.com.eatit.eventBus.BestDealClick;
import ahmet.com.eatit.eventBus.CategoryClick;
import ahmet.com.eatit.eventBus.CounterCartEvent;
import ahmet.com.eatit.eventBus.FoodClick;
import ahmet.com.eatit.eventBus.HideFABCart;
import ahmet.com.eatit.eventBus.MenuItemBack;
import ahmet.com.eatit.eventBus.PopularCategoryClcik;
import ahmet.com.eatit.model.Category;
import ahmet.com.eatit.model.Food.Food;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import io.paperdb.Paper;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawer;
    @BindView(R.id.nav_view)
    NavigationView mNavigation;
    @BindView(R.id.fab)
    CounterFab mCounterCart;

    private CartDataSource mCartDataSource;

    private NavController mNavController;

    private int menuClickId = -1;

    private AutocompleteSupportFragment mAutocompletePlacesFragment;
    private Place mPlaceSelected;
    private PlacesClient mPlacesClient;
    private List<Place.Field> mListPlaceFields;

    @OnClick(R.id.fab)
    void clickedFab() {
        mNavController.navigate(R.id.nav_cart);
    }

    private AppBarConfiguration mAppBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ButterKnife.bind(this);

        init();
        initPlace();

        initNavigation();

        checkIsOpenNotificationFromActivity();
    }


    private void checkIsOpenNotificationFromActivity() {

        boolean isOpenFromNewOrder = getIntent().getBooleanExtra(Common.IS_OPRN_ACTIVITY_NEW_ORDER, false);
        if (isOpenFromNewOrder){
            mNavController.popBackStack();
            mNavController.navigate(R.id.nav_orders);
            menuClickId = R.id.nav_orders;
        }
    }

    private void initPlace() {

        Places.initialize(this, getString(R.string.google_maps_key));
        mPlacesClient = Places.createClient(this);
        mListPlaceFields = Arrays.asList(Place.Field.ID, Place.Field.NAME,
                Place.Field.ADDRESS, Place.Field.LAT_LNG);


    }

    private void init() {

        requestPermission();

        mCartDataSource = new LocalCartDataSource(CartDatabase.getInstance(this).cartDAO());

    }

    private void initNavigation() {

        setSupportActionBar(mToolbar);
       // getSupportActionBar().setTitle(Common.currentUser.getName());


        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_category,
                R.id.nav_food, R.id.nav_food_details,
                R.id.nav_cart, R.id.nav_orders)
                .setDrawerLayout(mDrawer)
                .build();
        mNavController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, mNavController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(mNavigation, mNavController);

        mNavigation.setNavigationItemSelectedListener(this);
        mNavigation.bringToFront();

        View navHeader = mNavigation.getHeaderView(0);

        TextView mTxtUserName = navHeader.findViewById(R.id.txt_header_user_name);
        CircleImageView mImgUser = navHeader.findViewById(R.id.img_heder_user_avater);
        Common.setSpanString(getString(R.string.welcome)+" ", Common.currentUser.getName(), mTxtUserName);
        Picasso.get()
                .load("https://i.postimg.cc/ZqMZ3KJ5/man-character-face-avatar-in-glasses-vector-17074986.jpg")
                .into(mImgUser);

       countItemsCart();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.nav, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        item.setChecked(true);
        mDrawer.closeDrawers();

        switch (item.getItemId()) {
            case R.id.nav_home:
                if (item.getItemId() != menuClickId)
                    mNavController.navigate(R.id.nav_home);
                break;
            case R.id.nav_category:
                if (item.getItemId() != menuClickId)
                    mNavController.navigate(R.id.nav_category);
                break;
            case R.id.nav_food:
                if (item.getItemId() != menuClickId)
                    mNavController.navigate(R.id.nav_food);
                break;
            case R.id.nav_cart:
                if (item.getItemId() != menuClickId) {
                    mNavController.navigate(R.id.nav_cart);
                    mCounterCart.hide();
                }
                break;
            case R.id.nav_orders:
                if (item.getItemId() != menuClickId)
                    mNavController.navigate(R.id.nav_orders);
                break;
            case R.id.nav_news:
                showsubscribeNews();
                mDrawer.closeDrawers();
                break;
            case R.id.nav_user:
                showUpdateInfoUser();
                mDrawer.closeDrawers();
                break;
            case R.id.nav_sign_out:
                signOut();
                mDrawer.closeDrawers();
                break;
        }

        menuClickId = item.getItemId();

        return true;
    }

    private void showsubscribeNews() {

        Paper.init(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(R.string.news_system)
                .setMessage(R.string.message_news_system);

        View layoutView = LayoutInflater.from(this).inflate(R.layout.layout_subscribe_news, null);

        CheckBox checkSubscribeNews = layoutView.findViewById(R.id.check_subscribe_news);
        boolean isSubscribe = Paper.book().read(Common.KEY_SUBSCRIBE_NEWS,false);
        if (isSubscribe)
            checkSubscribeNews.setChecked(true);

        builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
           dialog.dismiss();
        }).setPositiveButton(R.string.send, (dialog, which) -> {
            if (checkSubscribeNews.isChecked()){
                Paper.book().write(Common.KEY_SUBSCRIBE_NEWS, true);
                FirebaseMessaging.getInstance()
                        .subscribeToTopic(Common.KEY_NEWS_TOPIC)
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        })
                        .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, getString(R.string.subscribe_success), Toast.LENGTH_SHORT).show();
                });
            }else{
                Paper.book().delete(Common.KEY_SUBSCRIBE_NEWS);
                FirebaseMessaging.getInstance()
                        .unsubscribeFromTopic(Common.KEY_NEWS_TOPIC)
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        })
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, getString(R.string.unsubscribe_success), Toast.LENGTH_SHORT).show();
                        });
            }
        });

        builder.setView(layoutView);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showUpdateInfoUser() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.update_user_info);
        // .setMessage(R.string.sign_up_message);

        View layoutView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_update_info, null);
        MaterialEditText mInputName = layoutView.findViewById(R.id.input_update_user_name);
        MaterialEditText mInputPhone = layoutView.findViewById(R.id.input_update_user_phone);
        MaterialEditText mInputEmail = layoutView.findViewById(R.id.input_update_user_email);
        // MaterialEditText mInputAddress = layoutView.findViewById(R.id.input_add_user_address);
        TextView mTxtAddressDetails = layoutView.findViewById(R.id.txt_update_address_details);
        RotateLoading rotateLoading = layoutView.findViewById(R.id.progress_loading_sign_up);



        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME,
                Place.Field.ADDRESS,Place.Field.LAT_LNG));
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i("TAG_PLACE", "Place: " + place.getName() + ", " + place.getId());
                mPlaceSelected = place;
                mTxtAddressDetails.setText(place.getAddress());
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i("TAG_PLACE_ERROR", "An error occurred: " + status);
                Toast.makeText(HomeActivity.this, ""+status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        rotateLoading.stop();


        // set user data
        mInputName.setText(Common.currentUser.getName());
        mTxtAddressDetails.setText(Common.currentUser.getAddress());
        mInputPhone.setText(Common.currentUser.getPhone());
        mInputEmail.setText(Common.currentUser.getEmail());


        builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
            dialog.dismiss();
        }).setPositiveButton(R.string.update, (dialog, which) -> {
            String userName = mInputName.getText().toString();
            String userPhone = mInputPhone.getText().toString();
            String userEmail = mInputEmail.getText().toString();
            String userAddress = mTxtAddressDetails.getText().toString();

            if (mPlaceSelected != null) {

                if (TextUtils.isEmpty(userName)) {
                    mInputName.setError(getString(R.string.please_enter_name));
                    return;
                }

//                if (TextUtils.isEmpty(userPhone)) {
//                    mInputPhone.setError(getString(R.string.please_enter_phone));
//                    return;
//                }
            }else{
                Toast.makeText(this, getString(R.string.please_select_address), Toast.LENGTH_SHORT).show();

            }

            updateUserInfo(userName, userAddress, userPhone, userEmail ,dialog);
//            mInputEmail.setText(Common.currentUser.getEmail());
//            mInputPhone.setText(Common.currentUser.getPhone());
        });

        builder.setView(layoutView);
        AlertDialog dialog = builder.create();

        dialog.setOnDismissListener(dialog1 -> {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.remove(autocompleteFragment);
            fragmentTransaction.commit();

        });

        dialog.show();
    }

    private void updateUserInfo(String userName, String userAddress,String phone, String email, DialogInterface dialog) {

        Map<String, Object> mapUpdateData = new HashMap();
        mapUpdateData.put("name", userName);
        mapUpdateData.put("address", userAddress);
        mapUpdateData.put("phone", phone);
        mapUpdateData.put("email", email);
        if (mPlaceSelected == null){
            mapUpdateData.put("lat", Common.currentUser.getLat());
            mapUpdateData.put("lng", Common.currentUser.getLng());
        }else {
            mapUpdateData.put("lat", mPlaceSelected.getLatLng().latitude);
            mapUpdateData.put("lng", mPlaceSelected.getLatLng().longitude);
        }


        FirebaseDatabase.getInstance().getReference()
                .child(Common.KEY_USER_REFERANCE)
                .child(Common.currentUser.getUid())
                .updateChildren(mapUpdateData)
                .addOnFailureListener(e -> {
                    dialog.dismiss();
                    Log.e("UPDATE_USER_INFO_ERROR", e.getMessage());
                    Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }).addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        dialog.dismiss();
                        Toast.makeText(this, getString(R.string.update_user_info_success), Toast.LENGTH_SHORT).show();
                        Common.currentUser.setName(mapUpdateData.get("name").toString());
                        Common.currentUser.setAddress(mapUpdateData.get("address").toString());
                        Common.currentUser.setLat(Double.parseDouble(mapUpdateData.get("lat").toString()));
                        Common.currentUser.setLng(Double.parseDouble(mapUpdateData.get("lng").toString()));
                    }
                });
    }

    private void signOut() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.sign_out)
                .setMessage(R.string.message_sign_out)
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setPositiveButton("SIGN OUT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Common.currentFood = null;
                Common.currentCategory = null;
                Common.currentUser = null;

                FirebaseAuth.getInstance().signOut();;
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();


    }

    private void countItemsCart() {
        mCartDataSource.getCountOfCart(Common.currentUser.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Integer counterCart) {

                        mCounterCart.setCount(counterCart);
                        Log.d("COUNTER_CART", counterCart.toString());
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (!e.getMessage().contains("Query returned empty"))
                            Log.d("Counter_Cart_ERROR", e.getMessage());
                        else
                            mCounterCart.setCount(0);
                    }
                });
    }

    private void requestPermission(){
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(HomeActivity.this, "Please enable permssion to use app", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();
    }


    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onCategorySelected(CategoryClick event) {
        if (event.isSuccess()) {
            mNavController.navigate(R.id.nav_food);
            // Toast.makeText(this, event.getCategory().getName(), Toast.LENGTH_SHORT).show();
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onFoodSelected(FoodClick event) {
        if (event.isSuccess()) {
            mNavController.navigate(R.id.nav_food_details);
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onCountCart(CounterCartEvent event) {

        if (event.isSuccess()) {
            countItemsCart();
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onCountCartAgain(CounterCartEvent event) {
        if (event.isSuccess())
            countItemsCart();

    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onHideFABCart(HideFABCart event) {

        if (event.isHidden())
            mCounterCart.hide();
        else
            mCounterCart.show();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onPopularItemClick(PopularCategoryClcik event){
        if (event.getPopularCategories() != null){

            FirebaseDatabase.getInstance().getReference()
                    .child(Common.KEY_CAEGORIES_REFERANCE)
                    .child(event.getPopularCategories().getMenu_id())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()){
                                Common.currentCategory = dataSnapshot.getValue(Category.class);
                                Common.currentCategory.setMenu_id(dataSnapshot.getKey());
                                FirebaseDatabase.getInstance().getReference()
                                        .child(Common.KEY_CAEGORIES_REFERANCE)
                                        .child(event.getPopularCategories().getMenu_id())
                                        .child(Common.KEY_FOOD_CHILD)
                                        .orderByChild("id")
                                        .equalTo(event.getPopularCategories().getFood_id())
                                        .limitToLast(1)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.exists()){
                                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                        Common.currentFood = snapshot.getValue(Food.class);
                                                        Common.currentFood.setKey(snapshot.getKey());
                                                    }

                                                    mNavController.navigate(R.id.nav_food_details);
                                                }else
                                                    Toast.makeText(HomeActivity.this, "Popular Category not exixts", Toast.LENGTH_SHORT).show();
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                Log.e("POPULAR_CATEGORY_ERROR", databaseError.getMessage());
                                            }
                                        });

                            }else
                                Toast.makeText(HomeActivity.this, "Popular not exixts", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e("POPULAR_ERROR", databaseError.getMessage());
                        }
                    });
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onBestDealItemClick(BestDealClick event){
        if (event.getBestDeals() != null){

            FirebaseDatabase.getInstance().getReference()
                    .child(Common.KEY_CAEGORIES_REFERANCE)
                    .child(event.getBestDeals().getMenu_id())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()){
                                Common.currentCategory = dataSnapshot.getValue(Category.class);
                                Common.currentCategory.setMenu_id(dataSnapshot.getKey());
                                FirebaseDatabase.getInstance().getReference()
                                        .child(Common.KEY_CAEGORIES_REFERANCE)
                                        .child(event.getBestDeals().getMenu_id())
                                        .child(Common.KEY_FOOD_CHILD)
                                        .orderByChild("id")
                                        .equalTo(event.getBestDeals().getFood_id())
                                        .limitToLast(1)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.exists()){
                                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                        Common.currentFood = snapshot.getValue(Food.class);
                                                        Common.currentFood.setKey(snapshot.getKey());
                                                    }
                                                    mNavController.navigate(R.id.nav_food_details);
                                                }else
                                                    Toast.makeText(HomeActivity.this, "Popular Category not exixts", Toast.LENGTH_SHORT).show();
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                Log.e("POPULAR_CATEGORY_ERROR", databaseError.getMessage());
                                            }
                                        });

                            }else
                                Toast.makeText(HomeActivity.this, "Popular not exixts", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e("POPULAR_ERROR", databaseError.getMessage());
                        }
                    });
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onMenuItemBack(MenuItemBack event){
        menuClickId = -1;
        if (getSupportFragmentManager().getBackStackEntryCount() > 0)
            getSupportFragmentManager().popBackStack();
    }

    @Override
    protected void onResume() {
        super.onResume();
        countItemsCart();
    }


    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }
}
