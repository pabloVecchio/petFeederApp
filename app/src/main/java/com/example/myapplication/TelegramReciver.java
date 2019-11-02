package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;


public class TelegramReciver extends AsyncTask <Void, Void, String> {

    private WeakReference<TextView> tv_lastFeedHr, tv_lastFeedCnt, tv_lastUpdate;
    private String lastFeedHr, lastFeedCnt, lastUpdate;
    private int bowlFeed, dayFeed, stock;
    private boolean pendingFeed, refreshStatus;
    private WeakReference<ProgressBar> pb_bowlFeed, pb_dayFeed, pb_stock, pb_pendingFeed, pb_RefreshStatus;

    private Context myContext;
    private WeakReference <lastMsgRcvClass> lastMsg;

    private final int MSG_TYPE_FEED = 0;
    private final int MSG_TYPE_STATUS= 1;

    public static final String MSG_NO_NEW = "No hay nuevos mensajes";

    public static final String REQUEST_METHOD = "GET";
    public static final int READ_TIMEOUT = 15000;
    public static final int CONNECTION_TIMEOUT = 15000;

    private JSONObject lastMsgRcvJson;

    TelegramReciver(Context  context, TextView lastFeedHr, TextView lastFeedCnt, TextView lastUpdate, ProgressBar bowlFeed, ProgressBar dayFeed, ProgressBar stock, ProgressBar refreshStatus,
                       ProgressBar pendingFeed, lastMsgRcvClass msg) {
        myContext = context;
        lastMsg = new WeakReference<>(msg);
        tv_lastFeedHr = new WeakReference<>(lastFeedHr);
        tv_lastFeedCnt = new WeakReference<>(lastFeedCnt);
        pb_pendingFeed = new WeakReference<>(pendingFeed);
        tv_lastUpdate = new WeakReference<>(lastUpdate);
        pb_bowlFeed = new WeakReference<>(bowlFeed);
        pb_dayFeed = new WeakReference<>(dayFeed);
        pb_stock = new WeakReference<>(stock);
        pb_RefreshStatus = new WeakReference<>(refreshStatus);
    }



    @Override
    protected String doInBackground(Void... voids) {

        String Sresult = "";
        String msgOut = "";

        while (true) {
            ConnectivityManager connManager = (ConnectivityManager) myContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo wifiNetworkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            NetworkInfo mobileNetworkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if (wifiNetworkInfo.isConnected() || mobileNetworkInfo.isConnected()) {


                String url = null;
                if (lastMsg.get().update_id != "")
                    url = "https://api.telegram.org/bot803736308:AAGf53HkKds8jQaamO3F-rMFaK1mOIM1KfI/getUpdates?limit=1&offset=" + lastMsg.get().update_id;
                else {
                    url = "https://api.telegram.org/bot803736308:AAGf53HkKds8jQaamO3F-rMFaK1mOIM1KfI/getUpdates?limit=1&offset=1";
                    lastMsg.get().update_id = "0";
                }

                try {
                    JSONObject Jresponse = sendTelegramMsg(url);

                    if (Boolean.parseBoolean(Jresponse.getString("ok"))) {
                        JSONArray result = new JSONArray(Jresponse.getString("result"));
                        if (result.length() != 0) {
                            JSONObject channel_post = new JSONObject(result.getJSONObject(0).getString("channel_post"));
                            JSONObject chat = new JSONObject(channel_post.getString("chat"));

                            lastMsg.get().setValues(Jresponse.getString("ok"), result.getJSONObject(0).getString("update_id"), channel_post.getString("message_id"),
                                    channel_post.getString("author_signature"), chat.getString("id"), chat.getString("title"), chat.getString("type"),
                                    channel_post.getString("date"), channel_post.getString("text"));

                            msgOut = lastMsg.get().author_signature + " dice '" + lastMsg.get().text + "' en " + lastMsg.get().chat_title + "(" + lastMsg.get().chat_type + ")";

                            break;

                        } else {
                            msgOut = MSG_NO_NEW;
                        }

                    } else {
                        setValues("false", "", "", "", "", "", "", "", "");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    Thread.sleep(2000);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }

            } else {
                Sresult = "<Revise la conexión a internet>";
                try {
                    Thread.sleep(5000);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }

        }
        return Sresult;
    }

    private void processTelegramMsg()
    {
        Date date = new Date ( Integer.parseInt(lastMsg.get().date)*1000L);
        String [] split = lastMsg.get().text.split(",");

        switch (split[0])
        {
            case "FEED":
                tv_lastFeedHr.get().setText( String.format( "%02d",  date.getDay() )+"/"+ String.format( "%02d",  date.getMonth()  ) +" "+  String.format( "%02d",  date.getHours()   ) +":"+ String.format( "%02d",  date.getMinutes()   ));
                tv_lastFeedCnt.get().setText("( "+ split[1] + " gr )");
                pb_pendingFeed.get().setVisibility(View.INVISIBLE);

                setSharedPreferences("lastFeedHr", tv_lastFeedHr.get().getText().toString() );
                setSharedPreferences("lastFeedCnt", tv_lastFeedCnt.get().getText().toString() );
                break;
            case "STATUS":
                tv_lastUpdate.get().setText(String.format( "%02d",  date.getDay() )+"/"+ String.format( "%02d",  date.getMonth()  ) +" "+  String.format( "%02d",  date.getHours()   ) +":"+ String.format( "%02d",  date.getMinutes()   ));
                pb_RefreshStatus.get().setVisibility(View.INVISIBLE);

                pb_bowlFeed.get().setProgress(Integer.parseInt(split[1]));
                pb_dayFeed.get().setProgress(Integer.parseInt(split[2]));
                pb_stock.get().setProgress(Integer.parseInt(split[3]));

                setSharedPreferences("lastUpdate", tv_lastUpdate.get().getText().toString() );
                setSharedPreferences("bowlFeed",  Integer.toString(pb_bowlFeed.get().getProgress()) );
                setSharedPreferences("dayFeed", Integer.toString(pb_dayFeed.get().getProgress()) );
                setSharedPreferences("stock", Integer.toString(pb_stock.get().getProgress()) );
                break;
        }
    }

    protected void onPostExecute(String result) {

        processTelegramMsg();
        TelegramReciver myListenerBot = new TelegramReciver(myContext, tv_lastFeedHr.get(), tv_lastFeedCnt.get(), tv_lastUpdate.get(), pb_bowlFeed.get(), pb_dayFeed.get(), pb_stock.get(), pb_RefreshStatus.get(), pb_pendingFeed.get(), lastMsg.get() );
        myListenerBot.execute();
    }

    JSONObject sendTelegramMsg (String myUrlString) throws JSONException {
        JSONObject Jresult ;
        String result = "";
        String inputLine;

        try {
            URL myUrl = new URL(myUrlString);

            HttpURLConnection connection =(HttpURLConnection)
                    myUrl.openConnection();

            connection.setRequestMethod(REQUEST_METHOD);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setConnectTimeout(CONNECTION_TIMEOUT);

            connection.connect();

            InputStreamReader streamReader = new
                    InputStreamReader(connection.getInputStream());

            BufferedReader reader = new BufferedReader(streamReader);
            StringBuilder stringBuilder = new StringBuilder();

            while((inputLine = reader.readLine()) != null){
                stringBuilder.append(inputLine);
            }

            reader.close();

            result = stringBuilder.toString();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Jresult =  new JSONObject(result);
        return Jresult;
    }



    void setValues(String okC,String update_idC, String message_idC, String author_signatureC, String chat_idC, String chat_titleC, String chat_typeC, String dateC, String textC ) {
        lastMsg.get().ok = okC;
        lastMsg.get().update_id = update_idC;
        lastMsg.get().message_id = message_idC;
        lastMsg.get().author_signature = author_signatureC;
        lastMsg.get().chat_id = chat_idC;
        lastMsg.get().chat_title = chat_titleC;
        lastMsg.get().chat_type = chat_typeC;
        lastMsg.get().date = dateC;
        lastMsg.get().text = textC;

        setSharedPreferences ("updateID",update_idC );
    }

    void setSharedPreferences (String key, String value)
    {
        SharedPreferences preferences = myContext.getSharedPreferences("datos", Context.MODE_PRIVATE);
        SharedPreferences.Editor editPref = preferences.edit();
        editPref.putString(key,value);
        editPref.commit();
    }
}

