package pivx.org.pivxwallet.wallofcoins.buyingwizard.models;

import java.io.Serializable;

/**
 * Created on 12-Mar-18.
 */

public class PhoneListVO implements Serializable {

    private String phoneNumber = "";
    private String deviceId = "";


    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

}
