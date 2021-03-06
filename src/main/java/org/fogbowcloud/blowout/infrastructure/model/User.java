package org.fogbowcloud.blowout.infrastructure.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class User {

    private static final String JSON_PROPERTY_ID = "id";
    private static final String JSON_PROPERTY_NAME = "name";
    private static final String JSON_PROPERTY_PASSWORD = "password";

    private String id;
    private String name;
    private String password;

    public User(String id, String name, String password) {
        this.name = name;
        this.password = password;
        this.id = id;
    }

    public JSONObject toJSON() throws JSONException {
        return new JSONObject().put(JSON_PROPERTY_ID, this.id)
                        .put(JSON_PROPERTY_NAME, this.name)
                        .put(JSON_PROPERTY_PASSWORD, this.password);
    }

    public static User fromJSON(String jsonStr) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonStr);
        return new User( jsonObject.optString(JSON_PROPERTY_ID), jsonObject.optString(JSON_PROPERTY_NAME),
                jsonObject.optString(JSON_PROPERTY_PASSWORD));
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", password='" + password + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(name, user.name) &&
                Objects.equals(password, user.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, password);
    }
}
