package pivx.org.pivxwallet.utils;

import org.bitcoinj.core.ScriptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

import pivx.org.pivxwallet.contacts.Contact;
import pivx.org.pivxwallet.module.PivxModule;
import pivx.org.pivxwallet.ui.wallet_activity.TransactionWrapper;

/**
 * Created by furszy on 8/14/17.
 */

public class TxUtils {

    private static Logger logger = LoggerFactory.getLogger(TxUtils.class);

    public static String getAddressOrContact(PivxModule pivxModule, TransactionWrapper data) {
        String text;
        if (data.getOutputLabels()!=null && !data.getOutputLabels().isEmpty()){
            Collection<Contact> contacts = data.getOutputLabels().values();
            Contact contact = contacts.iterator().next();
            if (contact!=null) {
                if (contact.getName() != null)
                    text = contact.getName();
                else
                    text = contact.getAddresses().get(0);
            }else {
                try {
                    text = data.getTransaction().getOutput(0).getScriptPubKey().getToAddress(pivxModule.getConf().getNetworkParams(), true).toBase58();
                }catch (ScriptException e){
                    text = data.getTransaction().getOutput(1).getScriptPubKey().getToAddress(pivxModule.getConf().getNetworkParams(),true).toBase58();
                }
            }
        }else {
            text = "Error";
            logger.warn(data.toString());
        }
        return text;
    }

}
