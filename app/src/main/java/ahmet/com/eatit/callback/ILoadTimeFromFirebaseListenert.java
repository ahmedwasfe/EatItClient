package ahmet.com.eatit.callback;

import ahmet.com.eatit.model.Order;

public interface ILoadTimeFromFirebaseListenert {

    void onLoadTimeSuccess(Order order, long estimateTimeInMs);
    void onLoadTimeFailed(String error);
}
