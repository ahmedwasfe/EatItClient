package ahmet.com.eatit.callback;

import java.util.List;

import ahmet.com.eatit.model.Category;

public interface ICategoriesCallBackListener {

    void onLoadCategoriseSuccess(List<Category> mListCategory);
    void onLoadCategoriseFaield(String error);

}
