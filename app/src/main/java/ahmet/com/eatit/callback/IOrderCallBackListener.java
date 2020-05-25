package ahmet.com.eatit.callback;

import java.util.List;

import ahmet.com.eatit.model.Order;

public interface IOrderCallBackListener {

    void onLoadOrderSuccess(List<Order> mListOrders);
    void onLoadOrderFaield(String error);

}
