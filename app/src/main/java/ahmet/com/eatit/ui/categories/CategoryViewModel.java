package ahmet.com.eatit.ui.categories;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import ahmet.com.eatit.callback.ICategoriesCallBackListener;
import ahmet.com.eatit.common.Common;
import ahmet.com.eatit.model.Category;

public class CategoryViewModel extends ViewModel implements ICategoriesCallBackListener {

    private MutableLiveData<List<Category>> listCategory;
    private MutableLiveData<String> messageError;
    private ICategoriesCallBackListener categoriesCallBackListener;


    public CategoryViewModel() {
       categoriesCallBackListener = this;
    }

    public MutableLiveData<List<Category>> getListCategory() {

        if (listCategory == null){
            listCategory = new MutableLiveData<>();
            messageError = new MutableLiveData<>();
            loadCategorise();
        }
        return listCategory;
    }

    public void loadCategorise() {

        FirebaseDatabase.getInstance().getReference()
                .child(Common.KEY_CAEGORIES_REFERANCE)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<Category> mListCategoty = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Category category = snapshot.getValue(Category.class);
                            category.setMenu_id(snapshot.getKey());
                            Log.d("MENU_ID", snapshot.getKey());
                            mListCategoty.add(category);
                        }
                        categoriesCallBackListener.onLoadCategoriseSuccess(mListCategoty);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        categoriesCallBackListener.onLoadCategoriseFaield(databaseError.getMessage());
                    }
                });
    }

    @Override
    public void onLoadCategoriseSuccess(List<Category> mListCategory) {
        listCategory.setValue(mListCategory);
    }

    @Override
    public void onLoadCategoriseFaield(String error) {
        messageError.setValue(error);
    }
}