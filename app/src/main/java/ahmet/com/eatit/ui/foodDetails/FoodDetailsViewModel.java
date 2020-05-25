package ahmet.com.eatit.ui.foodDetails;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import ahmet.com.eatit.common.Common;
import ahmet.com.eatit.model.Comment;
import ahmet.com.eatit.model.Food.Food;

public class FoodDetailsViewModel extends ViewModel {

    private MutableLiveData<Food> mMutableFood;
    private MutableLiveData<Comment> mMutableComment;

    public void setComment(Comment comment) {
        if (mMutableComment != null)
            mMutableComment.setValue(comment);
    }

    public MutableLiveData<Comment> getmMutableComment() {
        return mMutableComment;
    }

    public FoodDetailsViewModel() {
        mMutableComment = new MutableLiveData<>();

    }

    public MutableLiveData<Food> getmMutableFood() {
        if (mMutableFood == null)
            mMutableFood = new MutableLiveData<>();
        mMutableFood.setValue(Common.currentFood);
        return mMutableFood;
    }

    public void setFood(Food food) {
        if (mMutableFood != null)
            mMutableFood.setValue(food);
    }
}