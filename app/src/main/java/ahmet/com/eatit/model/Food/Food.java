package ahmet.com.eatit.model.Food;

import java.util.List;

public class Food {

    private String id, name, image, description;
    private String key;
    private Long price;
    private List<Addon> addon;
    private List<FoodSize> size;
    private Double rateValue;
    private Long rateCount;

    // For Cart
    private List<Addon> userSelectedAddon;
    private FoodSize userSelectedSize;

    public Food() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public List<Addon> getAddon() {
        return addon;
    }

    public void setAddon(List<Addon> addon) {
        this.addon = addon;
    }

    public List<FoodSize> getSize() {
        return size;
    }

    public void setSize(List<FoodSize> size) {
        this.size = size;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Double getRateValue() {
        return rateValue;
    }

    public void setRateValue(Double rateValue) {
        this.rateValue = rateValue;
    }

    public Long getRateCount() {
        return rateCount;
    }

    public void setRateCount(Long rateCount) {
        this.rateCount = rateCount;
    }

    public List<Addon> getUserSelectedAddon() {
        return userSelectedAddon;
    }

    public void setUserSelectedAddon(List<Addon> mListAddonUserSelected) {
        this.userSelectedAddon = mListAddonUserSelected;
    }

    public FoodSize getUserSelectedSize() {
        return userSelectedSize;
    }

    public void setUserSelectedSize(FoodSize userSelectedSize) {
        this.userSelectedSize = userSelectedSize;
    }
}
