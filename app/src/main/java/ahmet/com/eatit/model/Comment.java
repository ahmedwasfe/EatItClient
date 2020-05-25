package ahmet.com.eatit.model;

import java.util.Map;

public class Comment {

    private float rateValue;
    private String comment, name, uid;
    private Map<String, Object> serverTimeStamp;

    public Comment() {
    }

    public float getRateValue() {
        return rateValue;
    }

    public void setRateValue(float rateValue) {
        this.rateValue = rateValue;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Map<String, Object> getServerTimeStamp() {
        return serverTimeStamp;
    }

    public void setServerTimeStamp(Map<String, Object> serverTimeStamp) {
        this.serverTimeStamp = serverTimeStamp;
    }
}
