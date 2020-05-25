package ahmet.com.eatit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.syd.oden.circleprogressdialog.view.RotateLoading;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ahmet.com.eatit.common.Common;
import ahmet.com.eatit.model.User;
import ahmet.com.eatit.services.ICloudFunctions;
import ahmet.com.eatit.services.RetrofitICloudClient;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.paperdb.Paper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.btn_login_with_mobile)
    Button mBtnLoginWithMobile;
    @BindView(R.id.btn_login_with_facebook)
    Button mBtnLoginWithFacebook;


    @BindView(R.id.progress_loading_add_data)
    RotateLoading mLoadingAddData;
    @BindView(R.id.progress_loading_check_user)
    RotateLoading mLoadingCheckUser;

    private CompositeDisposable mDisposable;
    private ICloudFunctions mICloudFunctions;

    private List<AuthUI.IdpConfig> mListProviderPhone;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    // Place
    private AutocompleteSupportFragment mAutocompletePlacesFragment;
    private Place mPlacesSelected;
    private PlacesClient mPlacesClient;
    private List<Place.Field> mListPlaceFields;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        Paper.init(this);
        String currentUser = Paper.book().read(Common.KEY_LOOGED);
        if (currentUser != null)
            startActivity(new Intent(this, HomeActivity.class));

        init();
        initPlace();


    }

    private void init() {

        mDisposable = new CompositeDisposable();
        mICloudFunctions = RetrofitICloudClient.getRetrofit().create(ICloudFunctions.class);

        mListProviderPhone = Arrays.asList(new AuthUI.IdpConfig.PhoneBuilder().build(),
                new AuthUI.IdpConfig.EmailBuilder().build());
        mFirebaseAuth = FirebaseAuth.getInstance();

        mAuthStateListener = firebaseAuth -> {

            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null)
                checkUserIsExsist(user);
            else
                showUIPhonenumber();

        };

    }

    private void initPlace() {

        Places.initialize(this, getString(R.string.google_maps_key));
        mPlacesClient = Places.createClient(this);
        mListPlaceFields = Arrays.asList(Place.Field.ID, Place.Field.NAME,
                Place.Field.ADDRESS, Place.Field.LAT_LNG);

    }

    private void showUIPhonenumber() {

        startActivityForResult(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setLogo(R.drawable.logo)
                .setTheme(R.style.LoginTheme)
                .setAvailableProviders(mListProviderPhone)
                .build(), Common.CODE_REQUEST_PHONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Common.CODE_REQUEST_PHONE) {
            IdpResponse idpResponse = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
               // Log.d("USER_MOBILE_NUMBER", user.getPhoneNumber());
            }
        }
    }


    private void checkUserIsExsist(FirebaseUser user) {

        FirebaseDatabase.getInstance().getReference()
                .child(Common.KEY_USER_REFERANCE)
                .child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {

                            Snackbar.make(mLoadingCheckUser, "This account is used by someone else", Snackbar.LENGTH_SHORT).show();
                            mLoadingCheckUser.start();
                            User user = dataSnapshot.getValue(User.class);
                            goToHomeActivity(user, null);

                        } else {
                            mLoadingCheckUser.stop();
                            showDialogSignUp(user);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("CHECK_USER_ERROR", databaseError.getMessage());
                        mLoadingAddData.stop();
                    }
                });


    }

    private void showDialogSignUp(FirebaseUser user) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.sign_up);
        // .setMessage(R.string.sign_up_message);

        View layoutView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_sign_up, null);
        MaterialEditText mInputName = layoutView.findViewById(R.id.input_add_user_name);
        MaterialEditText mInputEmail = layoutView.findViewById(R.id.input_add_user_email);
        MaterialEditText mInputPhone = layoutView.findViewById(R.id.input_add_user_phone);
        MaterialEditText mInputAddress = layoutView.findViewById(R.id.input_add_user_address);
       // TextView mTxtAddressDetails = layoutView.findViewById(R.id.txt_add_address_details);
//
//        mAutocompletePlacesFragment = (AutocompleteSupportFragment) getSupportFragmentManager()
//                .findFragmentById(R.id.autocomplete_places_fragment);
//        mAutocompletePlacesFragment.setPlaceFields(mListPlaceFields);
//        mAutocompletePlacesFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
//            @Override
//            public void onPlaceSelected(@NonNull Place place) {
//                mPlacesSelected = place;
//                mTxtAddressDetails.setText(place.getAddress());
//            }
//
//            @Override
//            public void onError(@NonNull Status status) {
//                Log.e("PLACES_ERROR", status.getStatusMessage());
//                Toast.makeText(MainActivity.this, ""+status.getStatusMessage(), Toast.LENGTH_SHORT).show();
//
//            }
//        });

        if (user.getPhoneNumber() != null || !TextUtils.isEmpty(user.getPhoneNumber()))
            mInputPhone.setText(user.getPhoneNumber());
        else if (user.getEmail() != null || !TextUtils.isEmpty(user.getEmail())) {
            mInputEmail.setText(user.getEmail());
            mInputName.setText(user.getDisplayName());
        }



        builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
            dialog.dismiss();
        }).setPositiveButton(R.string.sign_up, (dialog, which) -> {

            mLoadingCheckUser.start();

                    String userName = mInputName.getText().toString();
                    String userPhone = mInputPhone.getText().toString();
                    String userEmail = mInputEmail.getText().toString();
                    String userAddress = mInputAddress.getText().toString();

                   // if (mPlacesSelected != null){

                        if (TextUtils.isEmpty(userName)) {
                            mInputName.setError(getString(R.string.please_enter_name));
                            return;
                        }

//                    if (TextUtils.isEmpty(userPhone)) {
//                        mInputPhone.setError(getString(R.string.please_enter_phone));
//                        return;
//                    }
//                }else
//                        Toast.makeText(this, getString(R.string.please_select_address), Toast.LENGTH_SHORT).show();
            if (TextUtils.isEmpty(userAddress)){
                mInputAddress.setError(getString(R.string.please_enter_address));
                return;
            }

            signUp(user, userName, userPhone, userEmail, userAddress);
        });

        builder.setView(layoutView);
        AlertDialog dialog = builder.create();

//        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
//        fragmentTransaction.remove(mAutocompletePlacesFragment);
//        fragmentTransaction.commit();

        dialog.show();
    }

    private void signUp(FirebaseUser firebaseUser, String name, String phone, String email, String address) {


        User user = new User(firebaseUser.getUid(), name, address, phone, email,-1.0,-1.0);
//        user.setUid(firebaseUser.getUid());
//        user.setName(name);
//        user.setAddress(address);
//        user.setPhone(phone);
//        user.setEmail(email);
//        user.setLat(-1.0);
//        user.setLng(-1.0);

        FirebaseDatabase.getInstance().getReference()
                .child(Common.KEY_USER_REFERANCE)
                .child(firebaseUser.getUid())
                .setValue(user)
                .addOnFailureListener(e -> {
                    mLoadingCheckUser.stop();
                    Log.e("ERROR_ADD_SERVER", e.getMessage());
                }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                mLoadingCheckUser.stop();
                Toast.makeText(this, getString(R.string.sign_up_success), Toast.LENGTH_SHORT).show();
                goToHomeActivity(user,null);
            }

        });
    }

    private void goToHomeActivity(User user, String token) {

        FirebaseInstanceId.getInstance()
                .getInstanceId()
                .addOnFailureListener(e -> {
                    Common.currentUser = user;
                    Common.currentToken = token;
                    startActivity(new Intent(MainActivity.this, HomeActivity.class));
                    finish();
                    Log.e("UPDATE_TOKEN_ERROR", e.getMessage());
                    Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }).addOnCompleteListener(task -> {
            Common.currentUser = user;
            Common.updateToken(MainActivity.this, task.getResult().getToken());

            Intent intent = new Intent(this, HomeActivity.class);
            intent.putExtra(Common.IS_OPRN_ACTIVITY_NEW_ORDER,
                    getIntent().getBooleanExtra(Common.IS_OPRN_ACTIVITY_NEW_ORDER, false));
            startActivity(intent);
            finish();
        });


    }

    private void getTokenToPayment() {

        FirebaseAuth.getInstance().getCurrentUser()
                .getIdToken(true)
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }).addOnCompleteListener(task -> {
            Common.authorizeKey = task.getResult().getToken();

            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", Common.buildToken(Common.authorizeKey));
            mDisposable.add(mICloudFunctions.getToken(headers)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(braintreeToken -> {

                        mLoadingCheckUser.stop();
                        Snackbar.make(mLoadingCheckUser, "This account is used by someone else", Snackbar.LENGTH_SHORT).show();
                        mLoadingCheckUser.start();
                        // User user = dataSnapshot.getValue(User.class);
                        // goToHomeActivity(user, braintreeToken.getToken());
                        Log.d("TOKEN_FROM_BRAINTREE", braintreeToken.getToken());
                    }, e -> {
                        mLoadingCheckUser.stop();
                        Toast.makeText(MainActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }));

        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onStop() {
        if (mAuthStateListener != null)
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        mDisposable.dispose();
        super.onStop();
    }
}
