package ahmet.com.eatit.callback;

import java.util.List;

import ahmet.com.eatit.model.PopularCategories;

public interface IPopularCallbackListener {

    void onLoadPopilarSuccess(List<PopularCategories> mListPopular);
    void onLoadPopilarFaield(String error);

}
