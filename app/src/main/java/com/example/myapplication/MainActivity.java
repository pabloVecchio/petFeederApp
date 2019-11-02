package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private EditText etMsgSend;
    private Button bMsgSend, bMsgSendRb, bMsgRcv;
    private RadioButton rbDIG_ON, rbDIG_OFF, rbIP, rbFEED, rbOTHER;
    private ImageButton ibTime, ibDate;
    private RadioGroup radioGroup;
    private ScrollView svMsgRcv;
    private WebView wvMsgSend;
    private TextView tvMsgRcv;
    private JSONObject lastMsgRcvJson;
    private lastMsgRcvClass lastMsgRcv;
    private SeekBar mySeekBar;
    private ProgressBar myProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etMsgSend = findViewById(R.id.et_MsgSend);
        bMsgRcv = findViewById(R.id.r_MsgRcv);
        //svMsgRcv = findViewById(R.id.sv_MsgRcv);
        wvMsgSend = findViewById(R.id.wv_MsgSend);
        //tvMsgRcv = findViewById(R.id.tv_MsgRcv);
        bMsgSendRb = findViewById(R.id.b_MsgSendRb);
        rbDIG_ON = findViewById(R.id.rb_DIG_ON);
        rbDIG_OFF = findViewById(R.id.rb_DIG_OFF);
        rbIP = findViewById(R.id.rb_IP);
        rbFEED= findViewById(R.id.rb_FEED);
        rbOTHER = findViewById(R.id.rb_OTHER);
        radioGroup = findViewById(R.id.radioGroup);
        ibDate =  findViewById(R.id.ib_Date);
        ibTime =  findViewById(R.id.ib_Time);


        mySeekBar = findViewById(R.id.seekBar);
        myProgressBar = findViewById(R.id.progressBar);

        WebViewClient yourWebClient = new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                //wvMsgSend.loadUrl("javascript:HtmlViewer.showHTML" + "('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
                wvMsgSend.loadUrl("javascript:HtmlViewer.showHTML" + "(document.getElementsByTagName('html')[0].innerText);");
            }
        };
        wvMsgSend.setWebViewClient(yourWebClient);
        wvMsgSend.getSettings().setJavaScriptEnabled(true);

        lastMsgRcv = new lastMsgRcvClass();


        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId)
                {
                    case R.id.rb_DIG_OFF:
                        Toast.makeText(MainActivity.this, "DIG_OFF" , Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.rb_DIG_ON:
                        Toast.makeText(MainActivity.this, "DIG_ON", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.rb_FEED:
                        Toast.makeText(MainActivity.this, "FEED", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.rb_IP:
                        Toast.makeText(MainActivity.this, "IP", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
            }
        });


        mySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                myProgressBar.setProgress(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //Toast.makeText(MainActivity.this, "StartTrack", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //Toast.makeText(MainActivity.this, "StopTrack", Toast.LENGTH_SHORT).show();
            }
        });

        registerForContextMenu(myProgressBar); // Digo que este elemento tiene un menu contextual asociado

    }

    // Cada vez que se vaya a mostrar un menu contextual se recurre a este metodo.
    // Puedo seleccionar que menu mostrar filtrando por la View que llega
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.menu_context, menu);
        super.onCreateContextMenu(menu, v, menuInfo);
    }
    @Override
    public boolean onContextItemSelected(MenuItem item){
        Toast.makeText(MainActivity.this, Integer.toString(myProgressBar.getProgress()) + "%", Toast.LENGTH_SHORT).show();
        super.onContextItemSelected(item);
        return true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_config, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.c_NodeMCUWifi:
                showNodeMCUWifiConfig();
                return true;
            default:
                Toast.makeText(MainActivity.this, "Item vacio", Toast.LENGTH_SHORT).show();
                return super.onOptionsItemSelected(item);
        }
    }

    public void showNodeMCUWifiConfig ()
    {
        String SSID = "";
        ConnectivityManager connManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (networkInfo.isConnected()) {
            final WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            if (connectionInfo != null && !TextUtils.isEmpty(connectionInfo.getSSID())) {
                SSID = connectionInfo.getSSID();
            }
        }

        if (SSID.equals( "\"PetFeedWifi\"")) {
            Intent intent = new Intent(this, NodeMCUConfig.class);
            startActivity(intent);
        }
        else{
            Toast.makeText(this, "No es posible abrir la configuración Wifi ya que no se encuentra conectado a la red 'PetFeedWifi'" , Toast.LENGTH_LONG).show();
        }
    }

    class MyJavaScriptInterface {
        private Context ctx;
        MyJavaScriptInterface(Context ctx) {
            this.ctx = ctx;
        }
        @JavascriptInterface
        public void showHTML(String html) {
            //tvMsgRcv.setText(html);
            String msgOut = "";
            try {
                lastMsgRcvJson = new JSONObject(html);
                try {
                        if ( Boolean.parseBoolean( lastMsgRcvJson.getString("ok")))
                        {
                            JSONArray result = new JSONArray( lastMsgRcvJson.getString("result"));
                            if (result.length() != 0) {

                             JSONObject channel_post = new JSONObject(result.getJSONObject(0).getString("channel_post"));
                             JSONObject chat = new JSONObject(channel_post.getString("chat"));

                             lastMsgRcv.setValues(lastMsgRcvJson.getString("ok"), result.getJSONObject(0).getString("update_id"), channel_post.getString("message_id"),
                                     channel_post.getString("author_signature"), chat.getString("id"), chat.getString("title"), chat.getString("type"),
                                     channel_post.getString("date"), channel_post.getString("text"));

                             msgOut = lastMsgRcv.author_signature + " dice '" + lastMsgRcv.text + "' en " + lastMsgRcv.chat_title + "(" + lastMsgRcv.chat_type + ")";

                             Toast.makeText(getApplicationContext(), msgOut, Toast.LENGTH_LONG).show();
                             lastMsgRcv.update_id = Integer.toString(Integer.parseInt(lastMsgRcv.update_id) + 1);
                            }
                            else
                            {
                                msgOut = "No hay nuevos mensajes";
                                Toast.makeText(getApplicationContext(), msgOut, Toast.LENGTH_LONG).show();
                            }

                        }
                        else {
                            lastMsgRcv.setValues("false", "", "", "", "", "", "", "", "");
                        }

                }
                catch (Throwable t){
                    msgOut = "'Ok' not found";
                    Toast.makeText(getApplicationContext(),msgOut,Toast.LENGTH_LONG).show();
                    //tvMsgRcv.setText(msgOut);
                }
                //Log.d("My App", obj.toString());
            } catch (Throwable t) {
                msgOut = "No hay conexión a internet";
                Toast.makeText(getApplicationContext(),msgOut,Toast.LENGTH_LONG).show();
                //tvMsgRcv.setText(msgOut);
               // Log.e("My App", "Could not parse malformed JSON: \"" + json + "\"");
            }
        }
    }


    public void msgSendRb(View view){
        String url = "";

        int sel = radioGroup.getCheckedRadioButtonId();
        RadioButton selButton = findViewById(sel);

        if (selButton != rbOTHER) {
            url = "https://api.telegram.org/bot803736308:AAGf53HkKds8jQaamO3F-rMFaK1mOIM1KfI/sendMessage?chat_id=-1001412098591&text=" + selButton.getText();
        }
        else
        {
            url = "https://api.telegram.org/bot803736308:AAGf53HkKds8jQaamO3F-rMFaK1mOIM1KfI/sendMessage?chat_id=-1001412098591&text=" + etMsgSend.getText().toString();
        }
        wvMsgSend.loadUrl(url);
    }


    @SuppressLint("JavascriptInterface")
    public void msgRecive(View view){
        String url = null;
        if (lastMsgRcv.update_id != "")
            url = "https://api.telegram.org/bot803736308:AAGf53HkKds8jQaamO3F-rMFaK1mOIM1KfI/getUpdates?limit=1&offset=" + lastMsgRcv.update_id ;
        else
            url = "https://api.telegram.org/bot803736308:AAGf53HkKds8jQaamO3F-rMFaK1mOIM1KfI/getUpdates?limit=1&offset=1";

        wvMsgSend.loadUrl(url);
        wvMsgSend.addJavascriptInterface(new MyJavaScriptInterface(this), "HtmlViewer");
    }

    public void onTimePicker(View v){
        TimePickerFragment newFragment =  new TimePickerFragment();
        newFragment.show(getFragmentManager(),"TimePicker");
    }

    public void onDatePicker(View v) {
        DatePickerFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "date picker");
    }

    public void onLauchButton  (View view)
    {
        Intent intent = new Intent(this, LauncActivity.class);
        startActivity(intent);
    }

}