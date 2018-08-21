package com.holiday.barcodefinder.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.holiday.barcodefinder.app.activities.QrCodeScannerActivity;
import com.holiday.barcodefinder.app.model.ItemTO;
import com.holiday.barcodefinder.app.model.PersianDigitConverter;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.NumberFormat;


public class MainActivity extends Activity implements View.OnClickListener {

    ItemTO itemTO;
    ParseJSON parseJSON;
    Button scan;
    Button search;
    EditText etBarcode;
    TextView itemName;
    TextView itemPrice;
    TextView itemDiscount;
    TextView itemNetPrice;
    View progress;

    String barcode;
    String result;
    NumberFormat formatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        itemTO = new ItemTO();

        scan = (Button) findViewById(R.id.scan_barcode);
        search = (Button) findViewById(R.id.search_button);
        etBarcode = (EditText) findViewById(R.id.scan_content);
        itemName = (TextView) findViewById(R.id.item_name);
        itemPrice = (TextView) findViewById(R.id.item_price);
        itemDiscount = (TextView) findViewById(R.id.item_discount);
        itemNetPrice = (TextView) findViewById(R.id.item_net);
        progress =  findViewById(R.id.progress_layout);


       /* Typeface typeface = Typeface.createFromAsset(getAssets(),"fonts/yekan.ttf");
        etBarcode.setTypeface(typeface);
        scan.setTypeface(typeface);
        search.setTypeface(typeface);
        itemPrice.setTypeface(typeface);
        itemDiscount.setTypeface(typeface);
        itemNetPrice.setTypeface(typeface);*/

        formatter = NumberFormat.getInstance();
        if (formatter instanceof DecimalFormat) {
            ((DecimalFormat)formatter).setDecimalSeparatorAlwaysShown(false);
        }

        scan.setOnClickListener(this);
        search.setOnClickListener(this);
        etBarcode.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View view, int keyCode, KeyEvent keyevent) {
                //If the keyevent is a key-down event on the "enter" button
                if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    itemName.setText("");
                    itemPrice.setText("");
                    itemDiscount.setText("");
                    itemNetPrice.setText("");

                    if(isNetworkConnected()){
                        barcode = faToEn(etBarcode.getText().toString());
                        if (barcode.matches("")){
                            Toast.makeText(getApplicationContext(), "فیلد بارکد خالی میباشد!", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            progress.setVisibility(View.VISIBLE);
                            parseJSON = new ParseJSON();
                            parseJSON.execute();
                        }
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "دسترسی به سرور مقدور نمیباشد."+"\n"+"از اتصال دستگاه خود به اینترنت مطمئن شوید.", Toast.LENGTH_LONG).show();
                    }
                    return true;
                }
                return false;
            }
        });
    }


    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.scan_barcode:
                itemName.setText("");
                itemPrice.setText("");
                itemDiscount.setText("");
                itemNetPrice.setText("");
                Intent intent = new Intent(MainActivity.this, QrCodeScannerActivity.class);
                startActivityForResult(intent, 1);
                break;

            case R.id.search_button:
                InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                itemName.setText("");
                itemPrice.setText("");
                itemDiscount.setText("");
                itemNetPrice.setText("");

                if(isNetworkConnected()){
                    barcode = faToEn(etBarcode.getText().toString());
                    if (barcode.matches("")){
                        Toast.makeText(getApplicationContext(), "فیلد بارکد خالی میباشد!", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        progress.setVisibility(View.VISIBLE);
                        new ParseJSON().execute();
                    }
                }
                else{
                    Toast.makeText(this.getApplicationContext(), "دسترسی به سرور مقدور نمیباشد،"+"\n"+"از اتصال دستگاه خود به اینترنت مطمئن شوید.", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

            try {
                super.onActivityResult(requestCode, resultCode, data);

                if (requestCode == 1 && resultCode == RESULT_OK) {

                    barcode = data.getStringExtra("result");
                    etBarcode.setText(faToEn(barcode));
                    if(isNetworkConnected()){
                            progress.setVisibility(View.VISIBLE);
                            parseJSON = new ParseJSON();
                            parseJSON.execute();
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "دسترسی به سرور مقدور نمیباشد،"+"\n"+"از اتصال دستگاه خود به اینترنت مطمئن شوید.", Toast.LENGTH_LONG).show();
                    }
                }
            } catch (Exception ex) {
                Toast.makeText(MainActivity.this, ex.toString(),
                        Toast.LENGTH_SHORT).show();
            }
    }

    public String getJSON(String url) {
        try {

            InputStream inputStream = null;
            DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
            HttpResponse httpResponse = defaultHttpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();
            inputStream = httpEntity.getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            inputStream.close();
            String result = sb.toString();

            return result;
        } catch (Exception ex) {

            Log.e("ppp", ex + "")
            ;
            return null;

        }
    }


    public class ParseJSON extends AsyncTask<String, Void, ItemTO> {

        @Override
        protected ItemTO doInBackground(String... strings) {
            result = getJSON("http://37.32.11.96:8080/it/code?code=" + barcode);
            try {
                JSONObject jsonObject = new JSONObject(result);
                itemTO.setState(jsonObject.getInt("success"));
                JSONObject itemObject = jsonObject.getJSONObject("result");

                itemTO.setName(itemObject.getString("Name"));
                itemTO.setPrice(itemObject.getInt("Price"));
                itemTO.setDiscount(itemObject.getString("DiscountPercent"));
                itemTO.setNetPrice(itemObject.getInt("NetPrice"));

            } catch (Exception e) {
                Log.e("EXCEPTION :", e + "Get json error");
            }
            return itemTO;
        }

        @Override
        protected void onPostExecute(ItemTO itemTO) {
            super.onPostExecute(itemTO);
            progress.setVisibility(View.GONE);

            if (itemTO.getState() == 1) {
                itemName.setText(itemTO.getName());
                itemPrice.setText(PersianDigitConverter.PersianNumber(formatter.format(itemTO.getPrice()).toString()));
                itemDiscount.setText(PersianDigitConverter.PersianNumber(itemTO.getDiscount()));
                itemNetPrice.setText(PersianDigitConverter.PersianNumber(formatter.format(itemTO.getNetPrice()).toString()));
            }
            else {
                Toast.makeText(getApplicationContext(), "کالایی با این مشخصات یافت نشد!", Toast.LENGTH_SHORT).show();
            }
        }}

    public static String faToEn(String barcode) {
        return barcode
                .replace("۰", "0")
                .replace("۱", "1")
                .replace("۲", "2")
                .replace("۳", "3")
                .replace("۴", "4")
                .replace("۵", "5")
                .replace("۶", "6")
                .replace("۷", "7")
                .replace("۸", "8")
                .replace("۹", "9");
    }
    private boolean isNetworkConnected() { // check internet connection
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {
            // There are no active networks.
            return false;
        } else
            return true;
    }

    public void checkConnection(){

    }
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
