package pivx.org.pivxwallet.wallofcoins.selling_wizard.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import pivx.org.pivxwallet.wallofcoins.response.CaptureHoldResp;
import pivx.org.pivxwallet.wallofcoins.response.CheckAuthResp;
import pivx.org.pivxwallet.wallofcoins.response.ConfirmDepositResp;
import pivx.org.pivxwallet.wallofcoins.response.CreateHoldResp;
import pivx.org.pivxwallet.wallofcoins.response.DiscoveryInputsResp;
import pivx.org.pivxwallet.wallofcoins.response.GetAuthTokenResp;
import pivx.org.pivxwallet.wallofcoins.response.GetCurrencyResp;
import pivx.org.pivxwallet.wallofcoins.response.GetHoldsResp;
import pivx.org.pivxwallet.wallofcoins.response.GetOffersResp;
import pivx.org.pivxwallet.wallofcoins.response.OrderListResp;
import pivx.org.pivxwallet.wallofcoins.selling_wizard.models.AddressListRespVo;
import pivx.org.pivxwallet.wallofcoins.selling_wizard.models.AddressVo;
import pivx.org.pivxwallet.wallofcoins.selling_wizard.models.AuthVo;
import pivx.org.pivxwallet.wallofcoins.selling_wizard.models.CreateDeviceVo;
import pivx.org.pivxwallet.wallofcoins.selling_wizard.models.GetReceivingOptionsResp;
import pivx.org.pivxwallet.wallofcoins.selling_wizard.models.MarketsVo;
import pivx.org.pivxwallet.wallofcoins.selling_wizard.models.SendVerificationRespVo;
import pivx.org.pivxwallet.wallofcoins.selling_wizard.models.SignUpResponseVo;
import pivx.org.pivxwallet.wallofcoins.selling_wizard.models.VerifyAdResp;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created on 04-Apr-18.
 */

public interface SellingApi {

    @FormUrlEncoded
    @POST(SellingApiConstants.CREATE_AUTH)
    Call<SignUpResponseVo> signUp(@FieldMap Map<String, String> partMap);


    //crypto=PIVX   currency=USD
    @GET(SellingApiConstants.MARKETS + "{crypto}/{currency}/")
    Call<ArrayList<MarketsVo>> getMarkets(@Path("crypto") String crypto, @Path("currency") String currency);


    //create address
    @FormUrlEncoded
    @POST(SellingApiConstants.CREATE_ADDRESS)
    Call<AddressVo> createAddress(@FieldMap Map<String, Object> partMap);


    //send verification code
    @FormUrlEncoded
    @POST(SellingApiConstants.SEND_VERIFICATION)
    Call<SendVerificationRespVo> sendVerificationCode(@FieldMap Map<String, String> partMap);

    @GET(SellingApiConstants.GET_AUTH + "{phone}/")
    Call<AuthVo> getAuthToken(@Path("phone") String phone, @Query("publisherId") String publisherId);


    @FormUrlEncoded
    @POST("api/v1/auth/{phone}/authorize/")
    Call<AuthVo> authorize(@Path("phone") String username, @FieldMap Map<String, String> partMap);

    @DELETE("api/v1/auth/{phone}/")
    Call<CheckAuthResp> deleteAuth(@Path("phone") String username, @Query("publisherId") String publisherId);

    @FormUrlEncoded
    @POST("api/verifyAd/")
    Call<VerifyAdResp> verifyAd(@FieldMap Map<String, String> partMap);

    @GET("api/v1/ad/")
    Call<ArrayList<AddressListRespVo>> getAddressListing();

    //----------------------------------------------------------
    @GET("api/v1/orders/")
    Call<List<OrderListResp>> getOrders(@Query("publisherId") String publisherId);


    @DELETE("api/v1/orders/{orderId}/")
    Call<Void> cancelOrder(@Path("orderId") String orderId, @Query("publisherId") String publisherId);

    @FormUrlEncoded
    @POST("api/v1/auth/{phone}/authorize/")
    Call<GetAuthTokenResp> getAuthToken(@Path("phone") String username, @FieldMap Map<String, String> partMap);


    //--------------dash wizard
    @GET("api/v1/banks/")
    Call<List<GetReceivingOptionsResp>> getReceivingOptions(@Query("country") String country);
    //----------------------

    @GET("api/v1/currency/")
    Call<List<GetCurrencyResp>> getCurrency();

    @FormUrlEncoded
    @POST("api/v1/discoveryInputs/")
    Call<DiscoveryInputsResp> discoveryInputs(@FieldMap Map<String, String> partMap);

    @GET("api/v1/discoveryInputs/{discoveryId}/offers/")
    Call<GetOffersResp> getOffers(@Path("discoveryId") String discoveryId, @Query("publisherId") String publisherId);

    @FormUrlEncoded
    @POST("api/v1/holds/")
    Call<CreateHoldResp> createHold(@FieldMap Map<String, String> partMap);

    @GET("api/v1/holds/")
    Call<List<GetHoldsResp>> getHolds();

    @DELETE("api/v1/holds/{id}/")
    Call<Void> deleteHold(@Path("id") String id);

    @FormUrlEncoded
    @POST("api/v1/holds/{id}/capture/")
    Call<List<CaptureHoldResp>> captureHold(@Path("id") String id, @FieldMap Map<String, String> partMap);

    @FormUrlEncoded
    @POST("api/v1/orders/{holdId}/confirmDeposit/")
    Call<ConfirmDepositResp> confirmDeposit(@Path("holdId") String holdId, @Field("your_field") String yourField, @Query("publisherId") String publisherId);

    @FormUrlEncoded
    @POST("api/v1/devices/")
    Call<CreateDeviceVo> createDevice(@FieldMap Map<String, String> partMap);

    @GET("api/v1/devices/")
    Call<List<CreateDeviceVo>> getDevice();


}
