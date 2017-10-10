package pivtrum.messages;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by furszy on 6/12/17.
 */

public class BaseMsg<T>{

    private long id;
    private String method;

    public BaseMsg(String method) {
        this.method = method;
    }

    public final JSONObject toJson() throws JSONException {
        JSONObject jsonObject = new JSONObject().put("method",method);
        jsonObject.put("id",id);
        toJson(jsonObject);
        return jsonObject;
    }

    public final T fromJson(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        if (!jsonObject.getString("Method").equals(method)) throw new IllegalArgumentException("json object is not a "+method);
        return fromJson(jsonObject);
    }

    /**
     * Method to override
     */
    public void toJson(JSONObject jsonObject) throws JSONException{

    }
    /**
     * Method to override
     */
    public T fromJson(JSONObject jsonObject) throws JSONException{
        throw new UnsupportedOperationException("method not implemented");
    }

    public final void setId(long id) {
        this.id = id;
    }

    public final long getId() {
        return id;
    }

    public final String getMethod() {
        return method;
    }
}
