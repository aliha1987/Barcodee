package com.holiday.barcodefinder.app;


import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.holiday.barcodefinder.app.activities.QrCodeScannerActivity;
import com.holiday.barcodefinder.app.model.ItemTO;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;


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

    String barcode;
    String result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        itemTO = new ItemTO();
        parseJSON = new ParseJSON();
        scan = (Button) findViewById(R.id.scan_barcode);
        search = (Button) findViewById(R.id.search_button);
        etBarcode = (EditText) findViewById(R.id.scan_content);
        itemName = (TextView) findViewById(R.id.item_name);
        itemPrice = (TextView) findViewById(R.id.item_price);
        itemDiscount = (TextView) findViewById(R.id.item_discount);
        itemNetPrice = (TextView) findViewById(R.id.item_net);

        Intent i = getIntent();
        Bundle bundle = i.getExtras();
        if (bundle != null) {
            barcode = bundle.getString("result");}
        etBarcode.setText(barcode);

        scan.setOnClickListener(this);
        search.setOnClickListener(this);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.scan_barcode:
                Intent intent = new Intent(MainActivity.this, QrCodeScannerActivity.class);
                startActivity(intent);

                parseJSON.execute();
                break;

            case R.id.search_button:
                barcode = etBarcode.getText().toString();
                parseJSON.execute();

                itemName.setText(itemTO.getName());
                itemPrice.setText(itemTO.getPrice());
                itemDiscount.setText(itemTO.getDiscount());
                itemNetPrice.setText(itemTO.getNetPrice());

                break;
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
            result = getJSON("http://192.168.20.2:8080/it/code?code=" + barcode);
            try {
                JSONObject jsonObject = new JSONObject(result);
                itemTO.setState(jsonObject.getInt("success"));

                if (itemTO.getState() == 1) {
                    JSONObject itemObject = jsonObject.getJSONObject("result");


                    itemTO.setName(itemObject.getString("Name"));
                    itemTO.setPrice(itemObject.getString("Price"));
                    itemTO.setDiscount(itemObject.getString("DiscountPercent"));
                    itemTO.setNetPrice(itemObject.getString("NetPrice"));

                } else {

                    Toast.makeText(getApplicationContext(), "کالایی با این مشخصات یافت نشد!", Toast.LENGTH_SHORT);

                }

            } catch (Exception e) {
                Log.e("EXCEPTION :", e + "Get json error");
            }
            return itemTO;
        }
    }
}
