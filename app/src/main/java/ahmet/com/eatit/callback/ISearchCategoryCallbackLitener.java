package ahmet.com.eatit.callback;

import ahmet.com.eatit.CartDatabse.Cart;
import ahmet.com.eatit.model.Category;
import ahmet.com.eatit.model.Food.Food;

public interface ISearchCategoryCallbackLitener {

    void onSearchCategoryFound(Category category, Cart cart);
    void onSearchCategoryNotFound(String error);

}
