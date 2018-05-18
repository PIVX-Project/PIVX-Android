package pivx.org.pivxwallet.wallofcoins.response;

public class GetCurrencyResp {

    public String code;
    public String name;
    public String symbol;

    @Override
    public String toString() {
//        return name + " (" + symbol + ")";
        return symbol;
    }


}
