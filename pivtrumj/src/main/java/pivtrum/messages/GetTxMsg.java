package pivtrum.messages;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by furszy on 6/14/17.
 */

public class GetTxMsg extends BaseMsg<GetTxMsg> {

    private String txHash;

    public GetTxMsg(String txHash) {
        super(Method.GET_TX.getMethod());
        this.txHash = txHash;
    }

    @Override
    public void toJson(JSONObject jsonObject) throws JSONException {
        JSONObject addressJson = new JSONObject();
        addressJson.put("tx_hash",txHash);
        jsonObject.put("params",addressJson);
    }

    @Override
    public GetTxMsg fromJson(JSONObject jsonObject) throws JSONException {
        return super.fromJson(jsonObject);
    }

    public String getAddress() {
        return txHash;
    }
}
