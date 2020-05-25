package ahmet.com.eatit.eventBus;

import ahmet.com.eatit.model.BestDeals;

public class BestDealClick {

    private BestDeals bestDeals;

    public BestDealClick(BestDeals bestDeals) {
        this.bestDeals = bestDeals;
    }

    public BestDeals getBestDeals() {
        return bestDeals;
    }

    public void setBestDeals(BestDeals bestDeals) {
        this.bestDeals = bestDeals;
    }
}
