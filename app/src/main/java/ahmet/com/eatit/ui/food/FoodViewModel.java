package ahmet.com.eatit.ui.food;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import ahmet.com.eatit.common.Common;
import ahmet.com.eatit.model.Food.Food;

public class FoodViewModel extends ViewModel {

    private MutableLiveData<List<Food>> listFood;
    private MutableLiveData<String> messageError;


    public FoodViewModel() {

    }

    public MutableLiveData<List<Food>> getListFood() {
        if (listFood == null)
            listFood = new MutableLiveData<>();
        listFood.setValue(Common.currentCategory.getFoods());
        return listFood;
    }
}