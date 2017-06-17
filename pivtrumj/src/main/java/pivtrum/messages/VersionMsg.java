package pivtrum.messages;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by furszy on 6/12/17.
 */

public class VersionMsg extends BaseMsg<VersionMsg> {

    private String name;
    private String maxVersion;
    private String minVersion;

    /**
     * version("2.7.11", "1.0")
     * @param name
     * @param maxVersion
     * @param minVersion
     */
    public VersionMsg(String name, String maxVersion, String minVersion) {
        this();
        this.name = name;
        this.maxVersion = maxVersion;
        this.minVersion = minVersion;
    }

    public VersionMsg() {
        super(Method.VERSION.getMethod());
    }



    @Override
    public void toJson(JSONObject jsonObject) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(maxVersion);
        jsonArray.put(minVersion);
        jsonObject.put("params",jsonArray);
    }

    @Override
    public VersionMsg fromJson(JSONObject jsonObject) throws JSONException {
        JSONArray jsonArray = jsonObject.getJSONArray("params");
        String maxVersion = jsonArray.get(0).toString();
        String minVersion = jsonArray.get(1).toString();
        return new VersionMsg(null,maxVersion,minVersion);
    }
}
