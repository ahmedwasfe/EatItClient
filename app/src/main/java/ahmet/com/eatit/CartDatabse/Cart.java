package ahmet.com.eatit.CartDatabse;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Cart", primaryKeys = {"userUid","categoryId", "foodId", "foodAddon", "foodSize"})
public class Cart {

    @NonNull
    @ColumnInfo(name = "categoryId")
    private String categoryId;

    @NonNull
    @ColumnInfo(name = "foodId")
    private String foodId;

    @ColumnInfo(name = "foodName")
    private String foodName;

    @ColumnInfo(name = "foodImage")
    private String foodImage;

    @ColumnInfo(name = "foodPrice")
    private Double foodPrice;

    @ColumnInfo(name = "foodExtraPrice")
    private Double foodExtraPrice;

    @ColumnInfo(name = "foodQuantity")
    private int foodQuantity;

    @ColumnInfo(name = "userPhone")
    private String userPhone;

    @NonNull
    @ColumnInfo(name = "userUid")
    private String userUid;

    @NonNull
    @ColumnInfo(name = "foodSize")
    private String foodSize;

    @NonNull
    @ColumnInfo(name = "foodAddon")
    private String foodAddon;


    @NonNull
    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(@NonNull String categoryId) {
        this.categoryId = categoryId;
    }

    @NonNull
    public String getFoodId() {
        return foodId;
    }

    public void setFoodId(@NonNull String foodId) {
        this.foodId = foodId;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public String getFoodImage() {
        return foodImage;
    }

    public void setFoodImage(String foodImage) {
        this.foodImage = foodImage;
    }

    public Double getFoodPrice() {
        return foodPrice;
    }

    public void setFoodPrice(Double foodPrice) {
        this.foodPrice = foodPrice;
    }

    public Double getFoodExtraPrice() {
        return foodExtraPrice;
    }

    public void setFoodExtraPrice(Double foodExtraPrice) {
        this.foodExtraPrice = foodExtraPrice;
    }

    public int getFoodQuantity() {
        return foodQuantity;
    }

    public void setFoodQuantity(int foodQuantity) {
        this.foodQuantity = foodQuantity;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    @NonNull
    public String getUserUid() {
        return userUid;
    }

    @NonNull
    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }

    @NonNull
    public String getFoodSize() {
        return foodSize;
    }

    @NonNull
    public void setFoodSize(String foodSize) {
        this.foodSize = foodSize;
    }

    @NonNull
    public String getFoodAddon() {
        return foodAddon;
    }

    @NonNull
    public void setFoodAddon(String foodAddon) {
        this.foodAddon = foodAddon;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof Cart))
            return false;
        Cart cart = (Cart) obj;
        return cart.getFoodId().equals(this.foodId) &&
               cart.getFoodAddon().equals(this.foodAddon) &&
               cart.getFoodSize().equals(this.foodSize);
    }
}
