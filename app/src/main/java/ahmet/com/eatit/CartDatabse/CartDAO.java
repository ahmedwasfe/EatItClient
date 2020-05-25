package ahmet.com.eatit.CartDatabse;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

@Dao
public interface CartDAO {

    // get all Items from cart
    @Query("SELECT * FROM Cart WHERE userUid=:userUid")
    Flowable<List<Cart>> getAllCarts(String userUid);

    // get count of cart
    @Query("SELECT SUM(foodQuantity) FROM Cart WHERE userUid=:userUid")
    Single<Integer> getCountOfCart(String userUid);

    // Sum Price in Cart
    @Query("SELECT SUM((foodPrice + foodExtraPrice) * foodQuantity) FROM Cart WHERE userUid=:userUid")
    Single<Double> sumPriceInCart(String userUid);

    // get item from cart by foodId and userUid
    @Query("SELECt * FROM Cart WHERE foodId=:foodId AND userUid=:userUid")
    Single<Cart> getCartByFoodId(String foodId, String userUid);

    // get item from cart by foodId and userUid and foodAddon, foodSize
    @Query("SELECt * FROM Cart WHERE categoryId=:categoryId AND foodId=:foodId AND userUid=:userUid AND foodSize=:foodSize AND foodAddon=:foodAddon")
    Single<Cart> getCartWithAllOptions(String userUid, String categoryId, String foodId,String foodSize, String foodAddon);

    // add items to cart
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable addOrReplaceAll(Cart... carts);

    // update from cart
    @Update(onConflict = OnConflictStrategy.REPLACE)
    Single<Integer> updateItemCart(Cart carts);

    // delete item from cart
    @Delete
    Single<Integer> deleteItemFromCart(Cart carts);

    // clear all item from cart
    @Query("DELETE FROM Cart WHERE userUid=:userUid")
    Single<Integer> clearCart(String userUid);
}
