package ahmet.com.eatit.model;

public class Token {

    private String phone, token;

    public Token() {
    }

    public Token(String phone, String token) {
        this.phone = phone;
        this.token = token;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
