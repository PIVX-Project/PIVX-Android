package pivx.org.pivxwallet.ui.qr_activity;

import android.content.Intent;
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

import org.pivxj.core.Address;
import org.pivxj.uri.PivxURI;

import pivx.org.pivxwallet.PivxApplication;
import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.module.PivxModule;
import pivx.org.pivxwallet.wallofcoins.buyingwizard.BuyDashBaseActivity;

import static android.graphics.Color.WHITE;
import static pivx.org.pivxwallet.utils.AndroidUtils.copyToClipboard;
import static pivx.org.pivxwallet.utils.QrUtils.encodeAsBitmap;

/**
 * Created by furszy on 6/8/17.
 */

public class MyAddressFragment extends Fragment implements View.OnClickListener {

    private PivxModule module;

    private View root;
    private TextView txt_address;
    private Button btn_share;
    private Button btn_copy,btn_buy;
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
        btn_copy = (Button) root.findViewById(R.id.btn_copy);
        btn_copy.setOnClickListener(this);
        img_qr = (ImageView) root.findViewById(R.id.img_qr);
        btn_buy = (Button) root.findViewById(R.id.btn_buy);
        btn_share.setOnClickListener(this);
        btn_buy.setOnClickListener(this);
        img_qr.setOnClickListener(this);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            // check if the address is already used
            boolean flag = false;
            if (address == null || module.isAddressUsed(address)) {
                address = module.getReceiveAddress();
                // todo: cleanup this
                //module.getKeyPairForAddress(address);
                flag = true;
            }
            if (flag) {
                String pivxUri = PivxURI.convertToBitcoinURI(address,null,"Receive address",null);
                loadAddress(pivxUri,address.toBase58());
            }
        }catch (WriterException e){
            e.printStackTrace();
            Toast.makeText(getActivity(),"Problem loading qr",Toast.LENGTH_LONG).show();
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    public void setModule(PivxModule module) {
        this.module = module;
    }

    private void loadAddress(String uri,String addressStr) throws WriterException {
        Bitmap qrBitmap = null;//Cache.getQrBigBitmapCache();
        if (qrBitmap == null) {
            Resources r = getResources();
            int px = convertDpToPx(r,225);
            Log.i("Util",uri);
            qrBitmap = encodeAsBitmap(uri, px, px, Color.parseColor("#1A1A1A"), WHITE );
        }
        img_qr.setImageBitmap(qrBitmap);
        txt_address.setText(addressStr);
    }


    public static int convertDpToPx(Resources resources, int dp){
        return Math.round(dp*(resources.getDisplayMetrics().xdpi/ DisplayMetrics.DENSITY_DEFAULT));
    }

    private void share(String address){
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, address);
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.share_address_text)));
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_share){
            share(address.toBase58());
        }else if(id == R.id.img_qr){
            copyToClipboard(getActivity(),address.toBase58());
            Toast.makeText(v.getContext(), R.string.copy_message,Toast.LENGTH_LONG).show();
        }else if (id == R.id.btn_copy){
            copyToClipboard(getActivity(),address.toBase58());
            Toast.makeText(v.getContext(), R.string.copy_message, Toast.LENGTH_LONG).show();
        }
        else if (id == R.id.btn_buy) {
            startActivity(new Intent(getActivity(), BuyDashBaseActivity.class));
        }
    }
}
