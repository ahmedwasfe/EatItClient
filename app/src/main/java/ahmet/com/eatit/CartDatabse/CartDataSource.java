package ahmet.com.eatit.CartDatabse;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

public interface CartDataSource {

    // get all Items from cart
    Flowable<List<Cart>> getAllCarts(String userUid);

    // get count of cart
    Single<Integer> getCountOfCart(String userUid);

    // Sum Price in Cart
    Single<Double> sumPriceInCart(String userUid);

    // get item from cart by foodId and userUid
    Single<Cart> getCartByFoodId(String foodId, String userUid);

    // get item from cart by foodId and userUid and foodAddon, foodSize
    Single<Cart> getCartWithAllOptions(String userUid, String categoryId, String foodId,String foodSize, String foodAddon);

    // add items to cart
    Completable addOrReplaceAll(Cart... carts);

    // update from cart
    Single<Integer> updateItemCart(Cart carts);

    // delete item from cart
    Single<Integer> deleteItemFromCart(Cart carts);

    // clear all item from cart
    Single<Integer> clearCart(String userUid);

}
