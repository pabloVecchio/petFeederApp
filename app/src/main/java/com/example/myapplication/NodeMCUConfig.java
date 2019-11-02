package com.example.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class NodeMCUConfig extends AppCompatActivity  {

    private WebView wvConfig;
    private boolean isReceiverRegistered = false;
    BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_node_mcuconfig);
        wvConfig = findViewById(R.id.wv_Config);

        String url = "http://192.168.4.1";
        wvConfig.getSettings().setJavaScriptEnabled(true);
        wvConfig.setWebViewClient(new WebViewClient());
        wvConfig.loadUrl(url);
        Toast.makeText(this, "Abriendo PeetFeed server..." , Toast.LENGTH_LONG).show();


        receiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                NetworkInfo info = getNetworkInfo(context);
                if (info != null && info.isConnected()) {
                    //Todo code to execute if wifi connected
                }
                else {
                    Toast.makeText(getApplicationContext(), "Se perdió la conexión PetFeedWifi" , Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        };
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (!isReceiverRegistered) {
            isReceiverRegistered = true;
            registerReceiver(receiver, new IntentFilter("android.net.wifi.STATE_CHANGE")); // IntentFilter to wifi state change is "android.net.wifi.STATE_CHANGE"
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (isReceiverRegistered) {
            isReceiverRegistered = false;
            unregisterReceiver(receiver);
        }
    }

    NetworkInfo getNetworkInfo(Context context) {
        ConnectivityManager connManager = (ConnectivityManager)
        context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    }

}
