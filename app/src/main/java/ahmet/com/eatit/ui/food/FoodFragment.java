package ahmet.com.eatit.ui.food;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import ahmet.com.eatit.adapter.FoodAdapter;
import ahmet.com.eatit.common.Common;
import ahmet.com.eatit.eventBus.MenuItemBack;
import ahmet.com.eatit.model.Food.Food;
import ahmet.com.eatit.R;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.supercharge.shimmerlayout.ShimmerLayout;

public class FoodFragment extends Fragment {

    @BindView(R.id.recycler_food)
    RecyclerView mRecyclerFood;
    @BindView(R.id.shimmer_layout_food)
    ShimmerLayout mShimmerLayout;

    private LayoutAnimationController mAnimationController;

    private FoodAdapter mFoodAdapter;

    private FoodViewModel foodViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        foodViewModel = ViewModelProviders.of(this).get(FoodViewModel.class);

        View layoutView = inflater.inflate(R.layout.fragment_food, container, false);

        ButterKnife.bind(this, layoutView);

        initViews();

        foodViewModel.getListFood()
                .observe(this, foods -> {
                    if (foods != null){
                        mFoodAdapter = new FoodAdapter(getActivity(), foods);
                        mRecyclerFood.setAdapter(mFoodAdapter);
                        mRecyclerFood.setLayoutAnimation(mAnimationController);
                    }
                    mShimmerLayout.startShimmerAnimation();
                    mShimmerLayout.setVisibility(View.GONE);

                });

        return layoutView;
    }

    private void initViews() {

        setHasOptionsMenu(true);

        ((AppCompatActivity)getActivity())
                .getSupportActionBar()
                .setTitle(Common.currentCategory.getName());

        mShimmerLayout.startShimmerAnimation();

        mAnimationController = AnimationUtils.loadLayoutAnimation(getActivity(), R.anim.raw_item_from_left);

        mRecyclerFood.setHasFixedSize(true);
        mRecyclerFood.setLayoutManager(new StaggeredGridLayoutManager(1, LinearLayout.VERTICAL));
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

        inflater.inflate(R.menu.search_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.action_search);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

        // Event
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                startSearchInFood(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        // Clear text when click to clear button on Search View
        ImageView btnClose = searchView.findViewById(R.id.search_close_btn);
        btnClose.setOnClickListener(view -> {

            EditText inputSearch = searchView.findViewById(R.id.search_src_text);
            // clear text
            inputSearch.setText("");
            // clear query
            searchView.setQuery("",false);
            // collapse the action view
            searchView.onActionViewCollapsed();
            // collapse the search widget
            menuItem.collapseActionView();
            // Restore result to original
            foodViewModel.getListFood();
        });
    }

    private void startSearchInFood(String query) {

        List<Food> mListFoodResult = new ArrayList<>();
        for (int i = 0; i < Common.currentCategory.getFoods().size(); i++){
            Food food = Common.currentCategory.getFoods().get(i);
            if (food.getName().toLowerCase().contains(query.toLowerCase()))
                mListFoodResult.add(food);
        }

        // get search resylt
        foodViewModel.getListFood()
                .setValue(mListFoodResult);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new MenuItemBack());
        super.onDestroy();
    }
}
