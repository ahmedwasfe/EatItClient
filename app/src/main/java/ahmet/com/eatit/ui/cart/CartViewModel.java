package ahmet.com.eatit.ui.cart;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import ahmet.com.eatit.CartDatabse.Cart;
import ahmet.com.eatit.CartDatabse.CartDataSource;
import ahmet.com.eatit.CartDatabse.CartDatabase;
import ahmet.com.eatit.CartDatabse.LocalCartDataSource;
import ahmet.com.eatit.common.Common;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class CartViewModel extends ViewModel {

    private MutableLiveData<List<Cart>> mutableLiveDataCart;

    private CompositeDisposable mDisposable;
    private CartDataSource mCartDataSource;

    public CartViewModel() {

        mDisposable = new CompositeDisposable();

    }

    public void initCartDataSource(Context mContext){
        mCartDataSource = new LocalCartDataSource(CartDatabase.getInstance(mContext).cartDAO());
    }

    public void onStop(){
        mDisposable.clear();
    }

    public MutableLiveData<List<Cart>> getMutableLiveDataCart() {
        if (mutableLiveDataCart == null)
            mutableLiveDataCart = new MutableLiveData<>();
        loadAllItemInCart();
        return mutableLiveDataCart;
    }

    private void loadAllItemInCart() {

        mDisposable.add(mCartDataSource.getAllCarts(Common.currentUser.getUid())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(carts -> {
                            mutableLiveDataCart.setValue(carts);
                        }, throwable -> {
                            mutableLiveDataCart.setValue(null);
                            Log.e("GET_ALL_CART_ITEM_ERROR", throwable.getMessage());
                        }));
    }
}


