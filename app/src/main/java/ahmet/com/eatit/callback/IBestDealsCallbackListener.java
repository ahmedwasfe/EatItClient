package ahmet.com.eatit.callback;

import java.util.List;

import ahmet.com.eatit.model.BestDeals;

public interface IBestDealsCallbackListener {

    void onLoadBestDealsSuccess(List<BestDeals> mListBestDeals);
    void onLoadBestDealsFaield(String error);

}
