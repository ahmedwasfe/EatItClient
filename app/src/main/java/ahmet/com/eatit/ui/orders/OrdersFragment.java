package ahmet.com.eatit.ui.orders;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.androidwidgets.formatedittext.widgets.FormatEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import ahmed.com.swiperecyclerview.MButton;
import ahmed.com.swiperecyclerview.SwipeRecyclerViewHelper;
import ahmet.com.eatit.CartDatabse.Cart;
import ahmet.com.eatit.CartDatabse.CartDataSource;
import ahmet.com.eatit.CartDatabse.CartDatabase;
import ahmet.com.eatit.CartDatabse.LocalCartDataSource;
import ahmet.com.eatit.TrackingOrderActivity;
import ahmet.com.eatit.adapter.OrdersAdapter;
import ahmet.com.eatit.callback.IOrderCallBackListener;
import ahmet.com.eatit.common.Common;
import ahmet.com.eatit.eventBus.CounterCartEvent;
import ahmet.com.eatit.model.Order;
import ahmet.com.eatit.R;
import ahmet.com.eatit.model.RefundRequest;
import ahmet.com.eatit.model.ShippingOrder;
import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.supercharge.shimmerlayout.ShimmerLayout;

public class OrdersFragment extends Fragment implements IOrderCallBackListener {

    @BindView(R.id.recycler_orders)
    RecyclerView mRecyclerOrders;
    @BindView(R.id.shimmer_layout_orders)
    ShimmerLayout mShimmerLayout;

    private OrdersViewModel mOrdersViewModel;

    private IOrderCallBackListener mOrderCallBackListener;

    private CartDataSource mCartDataSource;
    CompositeDisposable mDisposable;

    private android.app.AlertDialog mDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mOrdersViewModel = ViewModelProviders.of(this).get(OrdersViewModel.class);

        View layoutView = inflater.inflate(R.layout.fragment_orders, container, false);

        ButterKnife.bind(this, layoutView);

        initViews();

        loadOrdersFromDatabase();
        mOrdersViewModel.getmMutableLiveDataListOrders()
                .observe(this, mListOrder -> {

                    Collections.reverse(mListOrder);

                    OrdersAdapter ordersAdapter = new OrdersAdapter(getActivity(), mListOrder);
                    mRecyclerOrders.setAdapter(ordersAdapter);

                    mShimmerLayout.stopShimmerAnimation();
                    mShimmerLayout.setVisibility(View.GONE);
                });

        return layoutView;
    }

    private void loadOrdersFromDatabase() {

        FirebaseDatabase.getInstance().getReference()
                .child(Common.KEY_ORDER_REFERANCE)
                .orderByChild("userId")
                .equalTo(Common.currentUser.getUid())
                .limitToLast(100)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        List<Order> mListOrders = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                            Order order = snapshot.getValue(Order.class);
                            order.setOrderNumber(snapshot.getKey());
                            mListOrders.add(order);
                        }
                        mOrderCallBackListener.onLoadOrderSuccess(mListOrders);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        mOrderCallBackListener.onLoadOrderFaield(databaseError.getMessage());
                    }
                });
    }

    private void initViews() {

        mShimmerLayout.startShimmerAnimation();

        mOrderCallBackListener = this;

        mRecyclerOrders.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerOrders.setLayoutManager(layoutManager);
        mRecyclerOrders.addItemDecoration(new DividerItemDecoration(getActivity(), layoutManager.getOrientation()));

        mCartDataSource = new LocalCartDataSource(CartDatabase.getInstance(getActivity()).cartDAO());
        mDisposable = new CompositeDisposable();

        SwipeRecyclerViewHelper recyclerViewHelper = new SwipeRecyclerViewHelper(
                getActivity(), mRecyclerOrders, 200) {
            @Override
            public void instantiateButton(RecyclerView.ViewHolder viewHolder, List<MButton> mListMButton) {

                mListMButton.add(new MButton(getActivity(), getString(R.string.cancel_order),30,0,
                        getActivity().getColor(R.color.colorAccent), position -> {

                    Order order =((OrdersAdapter) mRecyclerOrders.getAdapter()).getItemAtPosition(position);
                    if (order.getOrderStatus() == 0){
                        if (order.isCod())
                            cancelOrder(order, position);
                        else
                            refundRequestMony(order, position);
                    }else{
                        String orderStatus = new StringBuilder("Your order was changed to ")
                                .append(Common.convertStatus(order.getOrderStatus()))
                                .append(", so you can,t cancel it!")
                                .toString();
                        Toast.makeText(getActivity(), orderStatus, Toast.LENGTH_SHORT).show();
                    }

                }));

                mListMButton.add(new MButton(getActivity(), getString(R.string.traking_order),30,0,
                        getActivity().getColor(R.color.colorYellow), position -> {

                    Order order =((OrdersAdapter) mRecyclerOrders.getAdapter()).getItemAtPosition(position);
                    // Fetch from Firebase database
                    FirebaseDatabase.getInstance().getReference()
                            .child(Common.KEY_SHIPPING_ORDER_REFERANCE)
                            .child(order.getOrderNumber())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    if (dataSnapshot.exists()){
                                        Common.currentShippingOrder = dataSnapshot.getValue(ShippingOrder.class);
                                        Common.currentShippingOrder.setKey(dataSnapshot.getKey());
                                        if (Common.currentShippingOrder.getCurrentLat() != -1 &&
                                                Common.currentShippingOrder.getCurrentLng() != 1){
                                            startActivity(new Intent(getActivity(), TrackingOrderActivity.class));
                                        }else{
                                            Toast.makeText(getActivity(), getString(R.string.shipper_not_start), Toast.LENGTH_SHORT).show();
                                        }
                                    }else{
                                        Toast.makeText(getActivity(), getString(R.string.order_just_placed), Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Log.e("GET_SHIPPING_ORDER_ERROR", databaseError.getMessage());
                                }
                            });
                }));

                mListMButton.add(new MButton(getActivity(), getString(R.string.repeat_order),30,0,
                        getActivity().getColor(R.color.colorButton), position -> {

                    Order order = ((OrdersAdapter) mRecyclerOrders.getAdapter()).getItemAtPosition(position);

                    mDialog.show();
                    // First clear all item in cart
                    mCartDataSource.clearCart(Common.currentUser.getUid())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new SingleObserver<Integer>() {
                                @Override
                                public void onSubscribe(Disposable d) {

                                }

                                @Override
                                public void onSuccess(Integer integer) {
                                    // After clear cart just add new
                                    Cart[] cart = order.getCarts().toArray(new Cart[order.getCarts().size()]);
                                    // insert to cart
                                    mDisposable.add(mCartDataSource.addOrReplaceAll(cart)
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(() -> {
                                                    mDialog.dismiss();
                                                    Toast.makeText(getActivity(), getString(R.string.repeat_order_to_cart), Toast.LENGTH_SHORT).show();
                                                    // Count in fab
                                                    EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                                }, throwable -> {
                                                    mDialog.dismiss();
                                                    Log.e("REAPET_ORDER_ERROR", throwable.getMessage());
                                                }));
                                }

                                @Override
                                public void onError(Throwable e) {
                                    mDialog.dismiss();
                                }
                            });
                }));
            }
        };

        mDialog = new SpotsDialog.Builder()
                .setContext(getActivity())
                .build();
    }

    private void refundRequestMony(Order order, int position) {

        View layoutView = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_refund_request, null);

        EditText mInputCardName = layoutView.findViewById(R.id.input_card_name);
        FormatEditText mInputCardNumber = layoutView.findViewById(R.id.input_card_number);
        FormatEditText mInputCardExp = layoutView.findViewById(R.id.input_card_exp);

        // Format credit card
        mInputCardNumber.setFormat("---- ---- ---- ----");
        mInputCardExp.setFormat("--/--");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.cancel_order)
                .setMessage(R.string.messgae_cancel_order)
                .setView(layoutView)
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    dialog.dismiss();;
                })
                .setPositiveButton(R.string.cancel_order, (dialog, which) -> {

                    RefundRequest refundRequest = new RefundRequest();
                    refundRequest.setCardName(Common.currentUser.getName());
                    refundRequest.setPhone(Common.currentUser.getPhone());
                    refundRequest.setCardName(mInputCardName.getText().toString());
                    refundRequest.setCardNumber(mInputCardNumber.getText().toString());
                    refundRequest.setCardExp(mInputCardExp.getText().toString());
                    refundRequest.setAmount(order.getFinalPayment());


                    FirebaseDatabase.getInstance().getReference()
                            .child(Common.KEY_REFUND_REQUEST_REFERANCE)
                            .child(order.getOrderNumber())
                            .setValue(refundRequest)
                            .addOnFailureListener(e -> Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show())
                            .addOnSuccessListener(aVoid -> {
                               // Update status
                                Map<String, Object> mapCancelOrder = new HashMap<>();
                                mapCancelOrder.put("orderStatus", -1);

                                FirebaseDatabase.getInstance().getReference()
                                        .child(Common.KEY_ORDER_REFERANCE)
                                        .child(order.getOrderNumber())
                                        .updateChildren(mapCancelOrder)
                                        .addOnFailureListener(e -> Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show())
                                        .addOnSuccessListener(avoid -> {
                                            order.setOrderStatus(-1);
                                            ((OrdersAdapter)mRecyclerOrders.getAdapter()).setItemAtPosition(position, order);
                                            mRecyclerOrders.getAdapter().notifyItemChanged(position);
                                            Toast.makeText(getActivity(), getString(R.string.cancel_order_success), Toast.LENGTH_SHORT).show();
                                        });
                            });
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void cancelOrder(Order order, int position) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.cancel_order)
                .setMessage(R.string.messgae_cancel_order)
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    dialog.dismiss();;
                }).setPositiveButton(R.string.cancel_order, (dialog, which) -> {

                    Map<String, Object> mapCancelOrder = new HashMap<>();
                    mapCancelOrder.put("orderStatus", -1);

                    FirebaseDatabase.getInstance().getReference()
                            .child(Common.KEY_ORDER_REFERANCE)
                            .child(order.getOrderNumber())
                            .updateChildren(mapCancelOrder)
                            .addOnFailureListener(e -> Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show())
                            .addOnSuccessListener(aVoid -> {
                                order.setOrderStatus(-1);
                                ((OrdersAdapter)mRecyclerOrders.getAdapter()).setItemAtPosition(position, order);
                                mRecyclerOrders.getAdapter().notifyItemChanged(position);
                                Toast.makeText(getActivity(), getString(R.string.cancel_order_success), Toast.LENGTH_SHORT).show();
                            });
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onLoadOrderSuccess(List<Order> mListOrders) {
        mShimmerLayout.stopShimmerAnimation();
        mShimmerLayout.setVisibility(View.GONE);
        mOrdersViewModel.setmMutableLiveDataListOrders(mListOrders);
    }

    @Override
    public void onLoadOrderFaield(String error) {
        mShimmerLayout.stopShimmerAnimation();
        mShimmerLayout.setVisibility(View.GONE);
        Log.e("LOAD_ORDERS_ERROR", error);
    }

    @Override
    public void onStop() {
        mDisposable.clear();
        super.onStop();
    }
}
