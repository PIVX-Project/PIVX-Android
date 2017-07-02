package wallet.exceptions;

public class CantRestoreEncryptedWallet extends Exception {

    public CantRestoreEncryptedWallet(String message, Exception cause) {
        super(message, cause);
    }

    public CantRestoreEncryptedWallet(Exception x) {
        super(x);
    }
}