package ahmet.com.eatit.eventBus;

import ahmet.com.eatit.CartDatabse.Cart;

public class UpdateFoodQuantityInCart {

    private Cart cart;

    public UpdateFoodQuantityInCart(Cart cart) {
        this.cart = cart;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }
}
