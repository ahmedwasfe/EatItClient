package ahmet.com.eatit;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import ahmet.com.eatit.adapter.MarkerAdapter;
import ahmet.com.eatit.common.Common;
import ahmet.com.eatit.model.ShippingOrder;
import ahmet.com.eatit.remote.IGoogleAPI;
import ahmet.com.eatit.remote.RetrofitClient;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class TrackingOrderActivity extends FragmentActivity implements OnMapReadyCallback, ValueEventListener {

    private GoogleMap mMap;

    // Routs
    private Marker shipperMarker;

    private PolylineOptions polylineOptions, blackPolylineOptions;
    private List<LatLng> polylineList;
    private Polyline yellowPolyline, grayPolyline, blackPolyline;

    private IGoogleAPI mIGoogleAPI;
    private CompositeDisposable mDisposable;


    private DatabaseReference mShipperReference;
    // Move marker
    private Handler handler;
    private int index, next;
    private LatLng start, end;
    private float v;
    private double lat, lng;
    private boolean isInit = false;

    @OnClick(R.id.btn_call) void onCallShipperClick(){


            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

                Dexter.withActivity(this)
                        .withPermission(Manifest.permission.CALL_PHONE)
                        .withListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse response) {

                                Intent intent = new Intent(Intent.ACTION_CALL);
                                intent.setData(Uri.parse(new StringBuilder("tel:")
                                        .append(Common.currentShippingOrder.getShipperPhone())
                                        .toString()));

                                startActivity(intent);
                            }

                            @Override
                            public void onPermissionDenied(PermissionDeniedResponse response) {
                                Toast.makeText(TrackingOrderActivity.this, getString(R.string.enable_permission) +
                                        " " + response.getPermissionName(), Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                            }
                        }).check();
                return;
            }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking_order);

        ButterKnife.bind(this);

        init();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        subscribeShipperMove();
    }

    private void subscribeShipperMove() {

        mShipperReference = FirebaseDatabase.getInstance().getReference()
                .child(Common.KEY_SHIPPING_ORDER_REFERANCE)
                .child(Common.currentShippingOrder.getKey());
        mShipperReference.addListenerForSingleValueEvent(this);

    }


    private void init() {

        mDisposable = new CompositeDisposable();
        mIGoogleAPI = RetrofitClient.getRetrofit().create(IGoogleAPI.class);
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setInfoWindowAdapter(new MarkerAdapter(getLayoutInflater()));

        mMap.getUiSettings().setZoomControlsEnabled(true);


        try {
            boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.uber_light_with_label));
            if (!success)
                Log.e("MAP_STYLE_ERROR", "Style parsing failed");

        } catch (Resources.NotFoundException ex) {
            Log.e("RESOURSE_FILE_ERROR", "Style parsing failed"+ex.getMessage());
        }

        drawRoutes(Common.currentShippingOrder);
    }

    private void drawRoutes(ShippingOrder currentShippingOrder) {

        LatLng locationOrder = new LatLng(currentShippingOrder.getOrder().getLat(),
                currentShippingOrder.getOrder().getLng());

        LatLng locationShipper = new LatLng(currentShippingOrder.getCurrentLat(),
                currentShippingOrder.getCurrentLng());

        // Add order box
        mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.box))
                    .title(currentShippingOrder.getOrder().getUserName())
                    .snippet(currentShippingOrder.getOrder().getShippingAddress())
                    .position(locationOrder));

        // Add shipper
        if (shipperMarker == null){

            int height, width;
            height = width = 80;
            BitmapDrawable bitmapDrawable = (BitmapDrawable) ContextCompat
                    .getDrawable(this,R.drawable.shipper);
            Bitmap resized = Bitmap.createScaledBitmap(bitmapDrawable.getBitmap(), width, height,false);

           shipperMarker = mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromBitmap(resized))
                    .title(new StringBuilder("Shipper: ")
                            .append(Common.currentShippingOrder.getShipperName())
                            .toString())
                    .snippet(new StringBuilder("Phone: ")
                            .append(currentShippingOrder.getShipperPhone())
                            .append("\n")
                            .append("Estimate Time Delivery: ")
                            .append(Common.currentShippingOrder.getEstimateTime())
                            .toString())
                    .position(locationShipper));

           // Always show information
           shipperMarker.showInfoWindow();
           mMap.setOnMarkerClickListener(marker -> {
               Log.d("ITEM", marker.getTitle());
               return false;
           });

           mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationShipper, 18));
        }else{
            shipperMarker.setPosition(locationShipper);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationShipper, 18));
        }

        // Draw routes
        String to = new StringBuilder()
                .append(currentShippingOrder.getOrder().getLat())
                .append(",")
                .append(currentShippingOrder.getOrder().getLng())
                .toString();
        String from = new StringBuilder()
                .append(currentShippingOrder.getCurrentLat())
                .append(",")
                .append(currentShippingOrder.getCurrentLng())
                .toString();

        mDisposable.add(mIGoogleAPI.getDirections("driving",
                "less_driving",
                from,to,
                getString(R.string.google_maps_key))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    try{
                        // Parse json
                        JSONObject jsonObject = new JSONObject(s);
                        JSONArray jsonArray = jsonObject.getJSONArray("routes");
                        for (int i = 0; i < jsonArray.length(); i++){
                            JSONObject route = jsonArray.getJSONObject(i);
                            JSONObject poly = route.getJSONObject("overview_polyline");
                            String polyline = poly.getString("points");
                            polylineList = Common.decodePoly(polyline);
                        }

                        polylineOptions = new PolylineOptions();
                        polylineOptions.color(Color.RED);
                        polylineOptions.width(12);
                        polylineOptions.startCap(new SquareCap());
                        polylineOptions.jointType(JointType.ROUND);
                        polylineOptions.addAll(polylineList);
                        yellowPolyline = mMap.addPolyline(polylineOptions);

                    }catch (Exception e){
                        Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                }, throwable -> {
                    Toast.makeText(this, ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                }));

    }

    @Override
    protected void onStop() {
        mDisposable.clear();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mShipperReference.removeEventListener(this);
        isInit = false;
        super.onDestroy();
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

        // Save old position
        String from = new StringBuilder()
                .append(Common.currentShippingOrder.getCurrentLat())
                .append(",")
                .append(Common.currentShippingOrder.getCurrentLng())
                .toString();
        // Update position
        Common.currentShippingOrder = dataSnapshot.getValue(ShippingOrder.class);
        Common.currentShippingOrder.setKey(dataSnapshot.getKey());
        // Save new position
        String to = new StringBuilder()
                .append(Common.currentShippingOrder.getCurrentLat())
                .append(",")
                .append(Common.currentShippingOrder.getCurrentLng())
                .toString();



        if (dataSnapshot.exists()){
            if (isInit)
                moveMarkerAnimation(shipperMarker, from, to);
            else
                isInit = true;
        }
    }

    private void moveMarkerAnimation(Marker shipperMarker, String from, String to) {

        mDisposable.add(mIGoogleAPI.getDirections("driving",
                "less_driving",
                from, to,
                getString(R.string.google_maps_key))
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(returnResult -> {

            try {

                // Parse json
                JSONObject jsonObject = new JSONObject(returnResult);
                JSONArray jsonArray = jsonObject.getJSONArray("routes");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject route = jsonArray.getJSONObject(i);
                    JSONObject poly = route.getJSONObject("overview_polyline");
                    String polyline = poly.getString("points");
                    polylineList = Common.decodePoly(polyline);
                }

                polylineOptions = new PolylineOptions();
                polylineOptions.color(Color.GRAY);
                polylineOptions.width(12);
                polylineOptions.startCap(new SquareCap());
                polylineOptions.jointType(JointType.ROUND);
                polylineOptions.addAll(polylineList);
                grayPolyline = mMap.addPolyline(polylineOptions);

                blackPolylineOptions = new PolylineOptions();
                blackPolylineOptions.color(Color.BLACK);
                blackPolylineOptions.width(5);
                blackPolylineOptions.startCap(new SquareCap());
                blackPolylineOptions.jointType(JointType.ROUND);
                blackPolylineOptions.addAll(polylineList);
                blackPolyline = mMap.addPolyline(blackPolylineOptions);

                // Animator
                ValueAnimator polylineAnimator = ValueAnimator.ofInt(0, 100);
                polylineAnimator.setDuration(2000);
                polylineAnimator.setInterpolator(new LinearInterpolator());
                polylineAnimator.addUpdateListener(valueAnimator -> {

                    List<LatLng> points = grayPolyline.getPoints();
                    int percentValue = (int) valueAnimator.getAnimatedValue();
                    int size = points.size();
                    int newPoints = (int) (size * (percentValue/100.0f));
                    List<LatLng> p = points.subList(0, newPoints);
                    blackPolyline.setPoints(p);
                });
                polylineAnimator.start();

                // Bike moving
                handler = new Handler();
                index = -1;
                next = 1;
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (index < polylineList.size() -1){
                            index++;
                            next = index +1;
                            start = polylineList.get(index);
                            end = polylineList.get(next);
                        }
                        ValueAnimator valueAnimator = ValueAnimator.ofInt(0,1);
                        valueAnimator.setDuration(1500);
                        valueAnimator.setInterpolator(new LinearInterpolator());
                        valueAnimator.addUpdateListener(valueAnimator1 -> {
                            v = valueAnimator1.getAnimatedFraction();
                            lng = v*end.longitude+(1-v)
                                    *start.longitude;
                            lat = v*end.latitude+(1-v)
                                    *start.latitude;
                            LatLng newPos = new LatLng(lat,lng);
                            shipperMarker.setPosition(newPos);
                            shipperMarker.setAnchor(0.5f,0.5f);
                            shipperMarker.setRotation(Common.getBearing(start, newPos));

                            mMap.moveCamera(CameraUpdateFactory.newLatLng(newPos));
                        });
                        valueAnimator.start();
                        // Reach destination
                        if (index < polylineList.size() -2)
                            handler.postDelayed(this,1500);
                    }
                },1500);

            }catch (Exception e){
                   Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
        },throwable -> {
            Toast.makeText(this, ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();
        }));
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }
}
