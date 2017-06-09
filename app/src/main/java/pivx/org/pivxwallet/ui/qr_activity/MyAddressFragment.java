package pivx.org.pivxwallet.ui.qr_activity;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.WriterException;

import org.bitcoinj.core.Address;
import org.bitcoinj.uri.BitcoinURI;

import pivx.org.pivxwallet.PivxApplication;
import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.module.PivxModule;

import static android.graphics.Color.WHITE;
import static pivx.org.pivxwallet.utils.QrUtils.encodeAsBitmap;

/**
 * Created by furszy on 6/8/17.
 */

public class MyAddressFragment extends Fragment{

    private PivxModule module;

    private View root;
    private TextView txt_address;
    private Button btn_share;
    private ImageView img_qr;

    private Address address;

    public static MyAddressFragment newInstance(PivxModule pivxModule) {
        MyAddressFragment f = new MyAddressFragment();
        f.setModule(pivxModule);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        module = PivxApplication.getInstance().getModule();
        root = inflater.inflate(R.layout.my_address,null);
        txt_address = (TextView) root.findViewById(R.id.txt_address);
        btn_share = (Button) root.findViewById(R.id.btn_share);
        img_qr = (ImageView) root.findViewById(R.id.img_qr);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            // check if the address is already used
            boolean flag = false;
            if (address == null || module.isAddressUsed(address)) {
                address = module.freshNewAddress();
                flag = true;
            }
            if (flag) {
                String pivxUri = BitcoinURI.convertToBitcoinURI(address,null,"Receive address",null);
                loadAddress(pivxUri,address.toBase58());
            }
        }catch (WriterException e){
            e.printStackTrace();
            Toast.makeText(getActivity(),"Problem loading qr",Toast.LENGTH_LONG).show();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void loadAddress(String uri,String addressStr) throws WriterException {
        Bitmap qrBitmap = null;//Cache.getQrBigBitmapCache();
        if (qrBitmap == null) {
            Resources r = getResources();
            int px = convertDpToPx(r,225);
            Log.i("Util",uri);
            qrBitmap = encodeAsBitmap(uri, px, px, Color.parseColor("#1A1A1A"), WHITE );
            //Cache.setQrBigBitmapCache(qrBitmap);
        }
        img_qr.setImageBitmap(qrBitmap);
        txt_address.setText(addressStr);
    }

    public void setModule(PivxModule module) {
        this.module = module;
    }

    public static int convertDpToPx(Resources resources, int dp){
        return Math.round(dp*(resources.getDisplayMetrics().xdpi/ DisplayMetrics.DENSITY_DEFAULT));
    }

}
