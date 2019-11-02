package com.example.myapplication;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;

public class LauncActivity extends AppCompatActivity {

    private Button b_feedManual, b_RefreshStatus;
    private TextView tv_petName, tv_lastFeedHr, tv_lastFeedCnt, tv_lastUpdate;
    private ImageView iv_petPhoto;
    private ProgressBar pb_bowlFeed, pb_dayFeed, pb_stock, pb_Feed, pb_RefreshStatus;
    private lastMsgRcvClass lastMsg;
    private ProgressDialog progress;

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String Cmd = intent.getStringExtra("Cmd");

            if (Cmd.equals("EndChkNewMsg"))
            {
                progress.cancel();
            }
            else {
                Date date = new Date(Integer.parseInt(intent.getStringExtra("Date")) * 1000L);
                String strDate = String.format("%02d", date.getDate()) + "/" + String.format("%02d", date.getMonth() + 1) + " " + String.format("%02d", date.getHours()) + ":" + String.format("%02d", date.getMinutes());

                lastMsg.update_id = intent.getStringExtra("update_id");
                setSharedPreferences("updateID", lastMsg.update_id);

                if (Cmd.equals("Feed")) {
                    tv_lastFeedHr.setText(strDate);
                    tv_lastFeedCnt.setText("( " + intent.getStringExtra("Cnt") + " gr )");

                    setSharedPreferences("lastFeedHr", tv_lastFeedHr.getText().toString());
                    setSharedPreferences("lastFeedCnt", tv_lastFeedCnt.getText().toString());

                    pb_Feed.setVisibility(View.INVISIBLE);
                } else if (Cmd.equals("Status")) {
                    tv_lastUpdate.setText(strDate);
                    pb_bowlFeed.setProgress(Integer.parseInt(intent.getStringExtra("Data1")));
                    pb_dayFeed.setProgress(Integer.parseInt(intent.getStringExtra("Data2")));
                    pb_stock.setProgress(Integer.parseInt(intent.getStringExtra("Data3")));

                    setSharedPreferences("bowlFeed", Integer.toString(pb_bowlFeed.getProgress()));
                    setSharedPreferences("dayFeed", Integer.toString(pb_dayFeed.getProgress()));
                    setSharedPreferences("stock", Integer.toString(pb_stock.getProgress()));
                    setSharedPreferences("lastUpdate", tv_lastUpdate.getText().toString());

                    pb_RefreshStatus.setVisibility(View.INVISIBLE);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_launc);

        final int grPorcion = 25;

        b_feedManual    = findViewById(R.id.button_feedManual);
        tv_petName      = findViewById(R.id.textView_petName);
        tv_lastFeedHr   = findViewById(R.id.textView_lastFeedHr);
        tv_lastFeedCnt  = findViewById(R.id.textView_lastFeedCnt);
        tv_lastUpdate   = findViewById(R.id.textView_lastUpdate);
        iv_petPhoto     = findViewById(R.id.imageView_petPhoto);
        pb_bowlFeed     = findViewById(R.id.progressBar_bowlFeed);
        pb_dayFeed      = findViewById(R.id.progressBar_dayFeed);
        pb_stock        = findViewById(R.id.progressBar_stockFeed);
        pb_Feed         = findViewById(R.id.progressBar_Feed);
        b_RefreshStatus = findViewById(R.id.button_RefreshStatus);
        pb_RefreshStatus= findViewById(R.id.progressBar_RefreshStatus);

        pb_Feed.setVisibility(View.INVISIBLE);
        pb_RefreshStatus.setVisibility(View.INVISIBLE);

        lastMsg =  new lastMsgRcvClass();

        tv_petName.setText(getSharedPrefences("petName"));
        if (tv_petName.getText().equals(""))
            tv_petName.setText("petName");
        tv_lastFeedHr.setText(getSharedPrefences("lastFeedHr"));
        tv_lastFeedCnt.setText(getSharedPrefences("lastFeedCnt"));
        tv_lastUpdate.setText(getSharedPrefences("lastUpdate"));
        pb_bowlFeed.setProgress((getSharedPrefencesInt("bowlFeed")));
        pb_dayFeed.setProgress((getSharedPrefencesInt("dayFeed")));
        pb_stock.setProgress((getSharedPrefencesInt("stock")));
        lastMsg.update_id = getSharedPrefences("updateID");


        b_feedManual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder feedDialog = new AlertDialog.Builder(LauncActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                View layoutFromInflater = inflater.inflate(R.layout.manual_feed_alert_dialog, null);
                final TextView tv_manualFeed = layoutFromInflater.findViewById(R.id.textView_manualFeed);
                final SeekBar seek= layoutFromInflater.findViewById(R.id.seekBar_manualFeed);

                feedDialog.setView(layoutFromInflater);

                seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
                {
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        progress++;
                        String porciones = "Porciones: " ;
                        if (progress / 4 != 0) {
                            porciones += Integer.toString(progress / 4);
                            if (progress % 4 != 0)
                                porciones += " y ";
                        }

                        switch (progress%4)
                        {
                            case 1:   porciones += "1/4"; break;
                            case 2:   porciones += "1/2"; break;
                            case 3:   porciones += "3/4"; break;
                            default:  break;
                        }

                        porciones += " (" + progress*grPorcion  + " gr )";
                        tv_manualFeed.setText( porciones);
                    }

                    public void onStartTrackingTouch(SeekBar arg0) { }
                    public void onStopTrackingTouch(SeekBar seekBar) { }
                });

                feedDialog.setPositiveButton("OK", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int which) {
                        pb_Feed.setVisibility(View.VISIBLE);
                        telegramBotHandlerService.startActionSend(LauncActivity.this, "Node,Feed," + Integer.toString((seek.getProgress() +1 ) * grPorcion ), lastMsg.update_id );
                        dialog.dismiss();
                    }
                });

                feedDialog.create();
                seek.setProgress(3);
                feedDialog.show();
                Toast.makeText(LauncActivity.this, "Selecione la cantidad" , Toast.LENGTH_SHORT).show();
            }

        });

        registerForContextMenu(tv_petName); // Digo que este elemento tiene un menu contextual asociado
        registerForContextMenu(iv_petPhoto); // Digo que este elemento tiene un menu contextual asociado
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("telegramMsg");
        registerReceiver(broadcastReceiver, intentFilter);

        progress = new ProgressDialog(this);
        progress.setMessage("Sincronizando información");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.show();

        telegramBotHandlerService.startActionRcv( this, lastMsg.update_id );
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(broadcastReceiver);
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
                Toast.makeText(getApplication(), "Item vacio", Toast.LENGTH_SHORT).show();
                return super.onOptionsItemSelected(item);
        }
    }
    public void showNodeMCUWifiConfig (){
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


    int lastContexMenuCreated = 0;
    @Override // Cada vez que se vaya a mostrar un menu contextual se recurre a este metodo. Puedo seleccionar que menu mostrar filtrando por la View que llega
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        switch (v.getId()){
            case R.id.textView_petName: getMenuInflater().inflate(R.menu.menu_main_change, menu);   break;
            case R.id.imageView_petPhoto: getMenuInflater().inflate(R.menu.menu_main_change, menu); break;
            default:   break;
        }
        lastContexMenuCreated = v.getId();
        super.onCreateContextMenu(menu, v, menuInfo);
    }
    @Override
    public boolean onContextItemSelected(MenuItem item){

        switch (item.getItemId())
        {
            case R.id.contextMenu_change:
                switch (lastContexMenuCreated)
                {
                    case R.id.textView_petName:
                        final AlertDialog.Builder changeName = new AlertDialog.Builder(LauncActivity.this);
                        LayoutInflater inflater = getLayoutInflater();
                        View layoutFromInflater = inflater.inflate(R.layout.change_context_menu, null);
                        final EditText etChangeContexMenu = layoutFromInflater.findViewById(R.id.et_changeContexMenu);
                        changeName.setTitle("Ingrese nuevo nombre");
                        changeName.setView(layoutFromInflater);

                        changeName.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                tv_petName.setText(etChangeContexMenu.getText().toString()) ;
                                setSharedPreferences ("petName",etChangeContexMenu.getText().toString() );
                                dialog.dismiss();
                            }
                        });

                        etChangeContexMenu.setText(tv_petName.getText().toString());
                        changeName.create();
                        changeName.show();

                        break;
                    case R.id.imageView_petPhoto:
                            // To do, ver de como poner una foto
                        break;

                }
        }
        lastContexMenuCreated = 0;
        super.onContextItemSelected(item);
        return true;
    }


    public void refreshStatus (View view){
        pb_RefreshStatus.setVisibility(View.VISIBLE);
        telegramBotHandlerService.startActionSend(LauncActivity.this, "Node,Status", lastMsg.update_id );
    }

    void setSharedPreferences (String key, String value){
        SharedPreferences preferences = getSharedPreferences("datos", Context.MODE_PRIVATE);
        SharedPreferences.Editor editPref = preferences.edit();
        editPref.putString(key,value);
        editPref.commit();
    }

    String getSharedPrefences (String key){
        SharedPreferences preferences = getSharedPreferences("datos", Context.MODE_PRIVATE);
        String result = preferences.getString(key, "");
        return result;
    }

    Integer getSharedPrefencesInt (String key){
        SharedPreferences preferences = getSharedPreferences("datos", Context.MODE_PRIVATE);
        Integer result = Integer.parseInt(preferences.getString(key, "0"));
        return result;
    }


}
