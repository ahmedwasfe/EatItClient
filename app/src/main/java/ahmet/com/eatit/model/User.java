package ahmet.com.eatit.model;

public class User {

    private String uid;
    private String name;
    private String address;
    private String phone;
    private String email;
    private double lat;
    private double lng;

    public User() {
    }

    public User(String uid, String name, String address, String phone, String email, double lat, double lng) {
        this.uid = uid;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.lat = lat;
        this.lng = lng;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}
