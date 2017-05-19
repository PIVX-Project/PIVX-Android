package pivx.org.pivxwallet.ui.wallet_activity;

/**
 * Created by Neoperol on 5/3/17.
 */

public class TransactionData {
    public String title;
    public String description;
    public String amount;
    public String amountLocal;
    public int imageId;

    public TransactionData(String title, String description, int imageId, String amount, String amountLocal) {
        this.title = title;
        this.description = description;
        this.imageId = imageId;
        this.amount = amount;
        this.amountLocal = amountLocal;
    }
}
