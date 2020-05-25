package ahmet.com.eatit.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.asksira.loopingviewpager.LoopingViewPager;
import com.smarteist.autoimageslider.IndicatorAnimations;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;

import ahmet.com.eatit.adapter.BestDealAdapter;
import ahmet.com.eatit.adapter.PopularAdapter;
import ahmet.com.eatit.R;
import butterknife.BindView;
import butterknife.ButterKnife;

public class HomeFragment extends Fragment {

    @BindView(R.id.recycler_populer)
    RecyclerView mRecyclerVPopular;
    @BindView(R.id.looing_pager)
    LoopingViewPager mLoopingPager;
    @BindView(R.id.slider_best_deals)
    SliderView mSliderBestSeals;

    private HomeViewModel homeViewModel;

    private LayoutAnimationController animationController;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);

        View layoutVIew = inflater.inflate(R.layout.fragment_home, container, false);

        ButterKnife.bind(this, layoutVIew);

        init();

        homeViewModel.getListPopular()
                .observe(this, popularCategories ->{

                    PopularAdapter popularAdapter = new PopularAdapter(getActivity(), popularCategories);
                    mRecyclerVPopular.setAdapter(popularAdapter);
                    mRecyclerVPopular.setLayoutAnimation(animationController);
                });

        homeViewModel.getListBestDeal()
                .observe(this, bestDeals -> {
                    BestDealAdapter bestDealAdapter = new BestDealAdapter(getActivity(), bestDeals);
                    mSliderBestSeals.setSliderAdapter(bestDealAdapter);
                   // mLoopingPager.setAdapter(bestDealAdapter);
                });

        return layoutVIew;
    }

    private void init() {

        animationController = AnimationUtils.loadLayoutAnimation(getActivity(), R.anim.raw_item_from_left);

        mRecyclerVPopular.setHasFixedSize(true);
        mRecyclerVPopular.setLayoutManager(new LinearLayoutManager(getActivity(),
                RecyclerView.HORIZONTAL,false));

        mSliderBestSeals.startAutoCycle();
        mSliderBestSeals.setAutoCycle(true);
        mSliderBestSeals.setIndicatorAnimation(IndicatorAnimations.WORM);
        mSliderBestSeals.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
    }
}
