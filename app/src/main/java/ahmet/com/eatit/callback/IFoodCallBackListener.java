package ahmet.com.eatit.callback;

import java.util.List;

import ahmet.com.eatit.model.Food.Food;

public interface IFoodCallBackListener {

    void onLoadFoodSuccess(List<Food> mListFood);
    void onLoadFoodFaield(String error);

}
