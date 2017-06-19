package pivtrum.messages;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by furszy on 6/18/17.
 */

public class GetHeader extends BaseMsg<GetHeader> {

    private long height;

    public GetHeader(long height) {
        super(Method.GET_HEADER.getMethod());
        this.height = height;
    }

    @Override
    public void toJson(JSONObject jsonObject) throws JSONException {
        JSONObject addressJson = new JSONObject();
        addressJson.put("height",height);
        jsonObject.put("params",addressJson);
    }

}
