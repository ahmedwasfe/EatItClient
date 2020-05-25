package ahmet.com.eatit.eventBus;

import ahmet.com.eatit.model.PopularCategories;

public class PopularCategoryClcik {

    private PopularCategories popularCategories;

    public PopularCategoryClcik(PopularCategories popularCategories) {
        this.popularCategories = popularCategories;
    }

    public PopularCategories getPopularCategories() {
        return popularCategories;
    }

    public void setPopularCategories(PopularCategories popularCategories) {
        this.popularCategories = popularCategories;
    }
}
