package org.fogbowcloud.blowout.infrastructure.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class Token {

    private static final String JSON_PROPERTY_ACCESS_ID = "access_id";
    private static final String JSON_PROPERTY_USER = "user";

    private String accessId;
    private User user;

    public Token(String accessId, User user) {
        this.accessId = accessId;
        this.user = user;
    }

    public String getAccessId() {
        return accessId;
    }

    public void setAcessId(String accessId) {
        this.accessId = accessId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public JSONObject toJSON() throws JSONException {
        return new JSONObject().put(JSON_PROPERTY_ACCESS_ID, this.accessId)
                .put(JSON_PROPERTY_USER, this.user != null ? this.user.toJSON() : null);
    }

    public static Token fromJSON(String jsonStr) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonStr);
        String accessId = jsonObject.optString(JSON_PROPERTY_ACCESS_ID);
        JSONObject userJson = jsonObject.optJSONObject(JSON_PROPERTY_USER);
        return new Token(!accessId.isEmpty() ? accessId : null,
                userJson != null ? User.fromJSON(userJson.toString()) : null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Token token = (Token) o;
        return accessId.equals(token.accessId) &&
                user.equals(token.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accessId, user);
    }

    @Override
    public String toString() {
        return "Token{" +
                "accessId='" + accessId + '\'' +
                ", user=" + user +
                '}';
    }
}
