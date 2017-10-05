package pivtrum.messages;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by furszy on 6/14/17.
 */

public class GetHistoryMsg extends BaseMsg<GetHistoryMsg> {

    private String address;

    public GetHistoryMsg(String addressBase58) {
        super(Method.GET_ADDRESS_HISTORY.getMethod());
        this.address = addressBase58;
    }

    @Override
    public void toJson(JSONObject jsonObject) throws JSONException {
        JSONObject addressJson = new JSONObject();
        addressJson.put("address",address);
        jsonObject.put("params",addressJson);
    }

    @Override
    public GetHistoryMsg fromJson(JSONObject jsonObject) throws JSONException {
        return super.fromJson(jsonObject);
    }

    public String getAddress() {
        return address;
    }
}
