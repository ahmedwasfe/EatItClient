package ahmet.com.eatit.ui.home;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import ahmet.com.eatit.callback.IBestDealsCallbackListener;
import ahmet.com.eatit.callback.IPopularCallbackListener;
import ahmet.com.eatit.common.Common;
import ahmet.com.eatit.model.BestDeals;
import ahmet.com.eatit.model.PopularCategories;

public class HomeViewModel extends ViewModel implements IPopularCallbackListener, IBestDealsCallbackListener {

    private MutableLiveData<List<PopularCategories>> listPopular;
    private MutableLiveData<String> messageError;
    private IPopularCallbackListener popularCallbackListener;

    private MutableLiveData<List<BestDeals>> listBestDeal;
    private IBestDealsCallbackListener bestDealsCallbackListener;

    public HomeViewModel() {
        popularCallbackListener = this;
        bestDealsCallbackListener = this;
    }

    public MutableLiveData<List<PopularCategories>> getListPopular() {
        if (listPopular == null){
            listPopular = new MutableLiveData<>();
            messageError = new MutableLiveData<>();
            loadPopularCaegories();
        }
        return listPopular;
    }

    public MutableLiveData<List<BestDeals>> getListBestDeal() {
        if (listBestDeal == null){
            listBestDeal = new MutableLiveData<>();
            messageError = new MutableLiveData<>();
            loadBestDeals();
        }
        return listBestDeal;
    }

    private void loadBestDeals() {

        FirebaseDatabase.getInstance().getReference()
                .child(Common.KEY_BEST_DEALS_REFERANCE)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        List<BestDeals> mLIstBestDeal = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                            BestDeals bestDeals = snapshot.getValue(BestDeals.class);
                            mLIstBestDeal.add(bestDeals);
                        }

                        bestDealsCallbackListener.onLoadBestDealsSuccess(mLIstBestDeal);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        bestDealsCallbackListener.onLoadBestDealsFaield(databaseError.getMessage());
                    }
                });
    }

    private void loadPopularCaegories() {

        FirebaseDatabase.getInstance().getReference()
                .child(Common.KEY_POPULAR_REFERANCE)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<PopularCategories> mListPopular = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                            PopularCategories popularCategories = snapshot.getValue(PopularCategories.class);
                            mListPopular.add(popularCategories);
                        }
                        popularCallbackListener.onLoadPopilarSuccess(mListPopular);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        popularCallbackListener.onLoadPopilarFaield(databaseError.getMessage());
                    }
                });
    }

    @Override
    public void onLoadPopilarSuccess(List<PopularCategories> mListPopular) {
        listPopular.setValue(mListPopular);
    }

    @Override
    public void onLoadPopilarFaield(String error) {
        messageError.setValue(error);
    }

    @Override
    public void onLoadBestDealsSuccess(List<BestDeals> mListBestDeals) {
        listBestDeal.setValue(mListBestDeals);
    }

    @Override
    public void onLoadBestDealsFaield(String error) {
        messageError.setValue(error);
    }
}