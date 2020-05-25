package ahmet.com.eatit.ui.categories;

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


import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import ahmet.com.eatit.adapter.CategoriesAdapter;
import ahmet.com.eatit.common.Common;
import ahmet.com.eatit.common.SpaceItemDecoration;
import ahmet.com.eatit.eventBus.MenuItemBack;
import ahmet.com.eatit.model.Category;
import ahmet.com.eatit.R;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.supercharge.shimmerlayout.ShimmerLayout;

public class CategoryFragment extends Fragment {

    @BindView(R.id.recycler_categoeies)
    RecyclerView mRecyclerCategory;
    @BindView(R.id.shimmer_layout)
    ShimmerLayout mShimmerLayout;

    private CategoriesAdapter categoriesAdapter;

    private CategoryViewModel categoryViewModel;
    private LayoutAnimationController mAnimationController;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        categoryViewModel = ViewModelProviders.of(this).get(CategoryViewModel.class);

        View layoutView = inflater.inflate(R.layout.fragment_category, container, false);

        ButterKnife.bind(this, layoutView);

        initViews();



        categoryViewModel.getListCategory()
                .observe(this, categories -> {

                    categoriesAdapter = new CategoriesAdapter(getActivity(), categories);
                    mRecyclerCategory.setAdapter(categoriesAdapter);
                    mRecyclerCategory.setLayoutAnimation(mAnimationController);

                    mShimmerLayout.stopShimmerAnimation();
                    mShimmerLayout.setVisibility(View.GONE);
                });
        return layoutView;
    }

    private void initViews() {

        setHasOptionsMenu(true);

        mShimmerLayout.startShimmerAnimation();

        mAnimationController = AnimationUtils.loadLayoutAnimation(getActivity(), R.anim.raw_item_from_left);

        mRecyclerCategory.setHasFixedSize(true);
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {

                if (categoriesAdapter != null){
                    switch (categoriesAdapter.getItemViewType(position)){
                        case Common.DEFAULT_COLUMN_COUNT: return 1;
                        case Common.FULL_WIDTH_COLUMN: return 2;
                        default: return -1;
                    }
                }
                return -1;
            }
        });

        mRecyclerCategory.setLayoutManager(layoutManager);
        mRecyclerCategory.addItemDecoration(new SpaceItemDecoration(8));
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
                startSearchInCategories(query);
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
            categoryViewModel.loadCategorise();
        });
    }

    private void startSearchInCategories(String query) {

        List<Category> mListCategoryResult = new ArrayList<>();
        for (int i = 0; i < categoriesAdapter.getListCategory().size(); i++){
            Category category = categoriesAdapter.getListCategory().get(i);
            if (category.getName().toLowerCase().contains(query.toLowerCase()))
                mListCategoryResult.add(category);
        }
        // get search result
        categoryViewModel.getListCategory()
                .setValue(mListCategoryResult);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new MenuItemBack());
        super.onDestroy();
    }
}
