package ahmet.com.eatit.ui.orders;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import ahmet.com.eatit.model.Order;

public class OrdersViewModel extends ViewModel {

    private MutableLiveData<List<Order>> mMutableLiveDataListOrders;

    public OrdersViewModel() {
        mMutableLiveDataListOrders = new MutableLiveData<>();
    }

    public MutableLiveData<List<Order>> getmMutableLiveDataListOrders() {
        return mMutableLiveDataListOrders;
    }

    public void setmMutableLiveDataListOrders(List<Order> mListOrders) {
        mMutableLiveDataListOrders.setValue(mListOrders);
    }
}
