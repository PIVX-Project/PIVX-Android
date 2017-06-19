package pivtrum.messages;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by furszy on 6/14/17.
 */

public class GetBalanceMsg extends BaseMsg<GetBalanceMsg> {

    String address;

    public GetBalanceMsg(String addressBase58) {
        super(Method.GET_BALANCE.getMethod());
        this.address = addressBase58;
    }

    @Override
    public void toJson(JSONObject jsonObject) throws JSONException {
        JSONObject addressJson = new JSONObject();
        addressJson.put("address",address);
        jsonObject.put("params",addressJson);
    }

    @Override
    public GetBalanceMsg fromJson(JSONObject jsonObject) throws JSONException {
        return super.fromJson(jsonObject);
    }

    public String getAddress() {
        return address;
    }
}
