package ahmet.com.eatit.CartDatabse;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

public class LocalCartDataSource implements CartDataSource {

    private CartDAO cartDAO;

    public LocalCartDataSource(CartDAO cartDAO) {
        this.cartDAO = cartDAO;
    }

    @Override
    public Flowable<List<Cart>> getAllCarts(String userUid) {
        return cartDAO.getAllCarts(userUid);
    }

    @Override
    public Single<Integer> getCountOfCart(String userUid) {
        return cartDAO.getCountOfCart(userUid);
    }

    @Override
    public Single<Double> sumPriceInCart(String userUid) {
        return cartDAO.sumPriceInCart(userUid);
    }

    @Override
    public Single<Cart> getCartByFoodId(String foodId, String userUid) {
        return cartDAO.getCartByFoodId(foodId, userUid);
    }

    @Override
    public Single<Cart> getCartWithAllOptions(String userUid, String categoryId, String foodId, String foodSize, String foodAddon) {
        return cartDAO.getCartWithAllOptions(userUid, categoryId, foodId, foodSize, foodAddon);
    }

    @Override
    public Completable addOrReplaceAll(Cart... carts) {
        return cartDAO.addOrReplaceAll(carts);
    }

    @Override
    public Single<Integer> updateItemCart(Cart carts) {
        return cartDAO.updateItemCart(carts);
    }

    @Override
    public Single<Integer> deleteItemFromCart(Cart carts) {
        return cartDAO.deleteItemFromCart(carts);
    }

    @Override
    public Single<Integer> clearCart(String userUid) {
        return cartDAO.clearCart(userUid);
    }
}
