package pivx.org.pivxwallet.wallofcoins.response;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;



public class PayFieldsDeserializer implements JsonDeserializer<GetReceivingOptionsResp.PayFieldsBeanX> {

    private static final String TAG = PayFieldsDeserializer.class.getSimpleName();

    @Override
    public GetReceivingOptionsResp.PayFieldsBeanX deserialize(JsonElement jsonElement, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (jsonElement.isJsonPrimitive()) {
            GetReceivingOptionsResp.PayFieldsBeanX payFieldsBeanX = new GetReceivingOptionsResp.PayFieldsBeanX();
            payFieldsBeanX.payFieldsB = jsonElement.getAsBoolean();
            return payFieldsBeanX;
        }

        return context.deserialize(jsonElement, GetReceivingOptionsResp.JsonPayFieldsBeanX.class);
    }
}
