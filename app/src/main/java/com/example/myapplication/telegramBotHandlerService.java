package com.example.myapplication;

import android.app.ActivityManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class telegramBotHandlerService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_RCV = "com.example.myapplication.action.RCV";
    private static final String ACTION_SND = "com.example.myapplication.action.SND";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.example.myapplication.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.example.myapplication.extra.PARAM2";

    public String telegramRensponseOut = "";
    public static final String MSG_NO_NEW               = "No hay nuevos mensajes";
    public static final String READ_NEW_MSG             = "Leyendo nuevos mensajes";
    public static final String END_READ_NEW_MSG         = "Fin, ya no hay nuevos mensajes";
    public static final String NOTIFICATION_CHANNEL_ID  = "channel_id";
    private static final String API_BOT                 = "https://api.telegram.org/bot803736308:AAGf53HkKds8jQaamO3F-rMFaK1mOIM1KfI/";
    private static Context launcherContext;

    public telegramBotHandlerService() {
        super("telegramBotHandlerService");
    }

    public static void startActionRcv(Context context, String updateID) {
        Intent intent = new Intent(context, telegramBotHandlerService.class);
        intent.setAction(ACTION_RCV);
        intent.putExtra(EXTRA_PARAM1, updateID);
        context.startService(intent);
        launcherContext = context;
    }

    public static void startActionSend(Context context, String msg, String updateID) {
        Intent intent = new Intent(context, telegramBotHandlerService.class);
        intent.setAction(ACTION_SND);
        intent.putExtra(EXTRA_PARAM1, msg);
        intent.putExtra(EXTRA_PARAM2, updateID);
        context.startService(intent);
        launcherContext = context;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_RCV.equals(action)) {
                final String updateID = intent.getStringExtra(EXTRA_PARAM1);
                handleActionRcv(updateID);
            } else if (ACTION_SND.equals(action)) {
                final String msg = intent.getStringExtra(EXTRA_PARAM1);
                final String updateID = intent.getStringExtra(EXTRA_PARAM2);
                handleActionSend(msg, updateID);
            }
        }
    }

    private void handleActionRcv(String updateID) {

        JSONObject Jresponse = null;
        String msgOut = READ_NEW_MSG;;
        telegramRensponseOut = READ_NEW_MSG;

        int id = 1;
        String url = null;
        if (!updateID.equals(""))
            id = Integer.parseInt(updateID) + 1;
        while (telegramRensponseOut.equals(READ_NEW_MSG)) {
            url = API_BOT + "getUpdates?limit=1&offset=" + Integer.toString(id);

            try {
                Jresponse = sendTelegramMsg(url);

                if (Boolean.parseBoolean(Jresponse.getString("ok"))) {
                    JSONArray result = new JSONArray(Jresponse.getString("result"));
                    if (result.length() != 0) {
                        JSONObject channel_post = new JSONObject(result.getJSONObject(0).getString("channel_post"));
                        JSONObject chat = new JSONObject(channel_post.getString("chat"));

                        //lastMsg.get().setValues(Jresponse.getString("ok"), result.getJSONObject(0).getString("update_id"), channel_post.getString("message_id"),
                        //        channel_post.getString("author_signature"), chat.getString("id"), chat.getString("title"), chat.getString("type"),
                        //        channel_post.getString("date"), channel_post.getString("text"));

                        String notificationTitle = "", notificationText = "";

                        Intent intentBroadcast = new Intent();
                        Intent intentNotificationLauncher = new Intent(this, LauncActivity.class);

                        intentBroadcast.setAction("telegramMsg");
                        intentBroadcast.putExtra("update_id", result.getJSONObject(0).getString("update_id"));
                        String [] split =  channel_post.getString("text").split(",");
                        if (split[1].contains("Feed")) {
                            intentBroadcast.putExtra("Cmd", split[1]);
                            intentBroadcast.putExtra("Date", channel_post.getString("date"));
                            intentBroadcast.putExtra("Cnt", split[2]);
                            intentNotificationLauncher.putExtra("Cmd", split[1]);
                            intentNotificationLauncher.putExtra("Date", channel_post.getString("date"));
                            intentNotificationLauncher.putExtra("Cnt", split[2]);
                            notificationTitle = "Alimento dado";
                            notificationText =  split[2]  + " gr.";
                        }
                        else if (split[1].contains("Status"))
                        {
                            intentBroadcast.putExtra("Cmd", split[1]);
                            intentBroadcast.putExtra("Data1", split[2]);
                            intentBroadcast.putExtra("Data2", split[3]);
                            intentBroadcast.putExtra("Data3", split[4]);
                            intentBroadcast.putExtra("Date", channel_post.getString("date"));
                            intentNotificationLauncher.putExtra("Cmd",  split[1]);
                            intentNotificationLauncher.putExtra("Data1", split[2]);
                            intentNotificationLauncher.putExtra("Data2", split[3]);
                            intentNotificationLauncher.putExtra("Data3", split[4]);
                            intentNotificationLauncher.putExtra("Date", channel_post.getString("date"));
                            notificationTitle = "Actualización de estado";
                        }
                        sendBroadcast(intentBroadcast);

                        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
                        builder.setContentTitle(notificationTitle);
                        builder.setContentText(notificationText);
                        builder.setSmallIcon(R.drawable.ic_pet_launcher_web);
                        //builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.icon));
                        builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
                        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);

                        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1001, intentNotificationLauncher, 0);
                        builder.setContentIntent(pendingIntent);

                        ActivityManager activityManager = (ActivityManager) launcherContext.getSystemService(Context.ACTIVITY_SERVICE);
                        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
                        if (appProcesses != null) {
                            final String packageName = launcherContext.getPackageName();
                            for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
                                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE && appProcess.processName.equals(packageName)) {
                                    Notification notification = builder.build();
                                    notification.flags |= Notification.FLAG_AUTO_CANCEL;
                                    NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
                                    notificationManagerCompat.notify(101, notification);
                                }
                            }
                        }
                        id++;
                    }
                    else{
                        msgOut = END_READ_NEW_MSG;
                        Intent intentBroadcast = new Intent();
                        intentBroadcast.setAction("telegramMsg");
                        intentBroadcast.putExtra("Cmd", "EndChkNewMsg");
                        sendBroadcast(intentBroadcast);
                    }
                }
                telegramRensponseOut = msgOut;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleActionSend(String msg, String updateID) {
        String msgOut = MSG_NO_NEW;;
        telegramRensponseOut = MSG_NO_NEW;

        JSONObject Jresponse = null;
        try {
            Jresponse = sendTelegramMsg(API_BOT + "sendMessage?chat_id=-1001412098591&text=" + msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            if (!Boolean.parseBoolean(Jresponse.getString("ok"))) {
                //Fallo la consulta a API
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        while (telegramRensponseOut.equals(MSG_NO_NEW)) { // Espero la respuesta
            String url = null;
            if (!updateID.equals("")) {
                int id = Integer.parseInt(updateID) + 1;
                url = API_BOT + "getUpdates?limit=1&offset=" + Integer.toString(id);
            }
            else {
                url = API_BOT + "getUpdates?limit=1&offset=1";
            }

            try {
                Jresponse = sendTelegramMsg(url);

                if (Boolean.parseBoolean(Jresponse.getString("ok"))) {
                    JSONArray result = new JSONArray(Jresponse.getString("result"));
                    if (result.length() != 0) {
                        JSONObject channel_post = new JSONObject(result.getJSONObject(0).getString("channel_post"));
                        JSONObject chat = new JSONObject(channel_post.getString("chat"));

                        //lastMsg.get().setValues(Jresponse.getString("ok"), result.getJSONObject(0).getString("update_id"), channel_post.getString("message_id"),
                        //        channel_post.getString("author_signature"), chat.getString("id"), chat.getString("title"), chat.getString("type"),
                        //        channel_post.getString("date"), channel_post.getString("text"));

                        msgOut = "";//Guardar datos en alamcenamiento interno

                        String notificationTitle = "", notificationText = "";

                        Intent intentBroadcast = new Intent();
                        Intent intentNotificationLauncher = new Intent(this, LauncActivity.class);

                        intentBroadcast.setAction("telegramMsg");
                        intentBroadcast.putExtra("update_id", result.getJSONObject(0).getString("update_id"));
                        String [] split =  channel_post.getString("text").split(",");
                        if (msg.contains("Feed")) {
                            intentBroadcast.putExtra("Cmd", split[1]);
                            intentBroadcast.putExtra("Date", channel_post.getString("date"));
                            intentBroadcast.putExtra("Cnt", split[2]);
                            intentNotificationLauncher.putExtra("Cmd", split[1]);
                            intentNotificationLauncher.putExtra("Date", channel_post.getString("date"));
                            intentNotificationLauncher.putExtra("Cnt", split[2]);
                            notificationTitle = "Alimento dado" ;
                            notificationText =  split[2]  + " gr.";
                        }
                        else if (msg.contains("Status"))
                        {
                            intentBroadcast.putExtra("Cmd", split[1]);
                            intentBroadcast.putExtra("Data1", split[2]);
                            intentBroadcast.putExtra("Data2", split[3]);
                            intentBroadcast.putExtra("Data3", split[4]);
                            intentBroadcast.putExtra("Date", channel_post.getString("date"));
                            intentNotificationLauncher.putExtra("Cmd", split[1]);
                            intentNotificationLauncher.putExtra("Data1", split[2]);
                            intentNotificationLauncher.putExtra("Data2", split[3]);
                            intentNotificationLauncher.putExtra("Data3", split[4]);
                            intentNotificationLauncher.putExtra("Date", channel_post.getString("date"));
                            notificationTitle = "Actualización de estado";
                        }
                        sendBroadcast(intentBroadcast);

                        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
                        builder.setContentTitle(notificationTitle);
                        builder.setContentText(notificationText);
                        builder.setSmallIcon(R.drawable.ic_pet_launcher_web);
                        //builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.icon));
                        builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
                        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
                        builder.setAutoCancel(true);

                        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1001, intentNotificationLauncher, 0);
                        builder.setContentIntent(pendingIntent);

                        ActivityManager activityManager = (ActivityManager) launcherContext.getSystemService(Context.ACTIVITY_SERVICE);
                        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
                        if (appProcesses != null) {
                            final String packageName = launcherContext.getPackageName();
                            for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
                                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE && appProcess.processName.equals(packageName)) {
                                    Notification notification = builder.build();
                                    notification.flags |= Notification.FLAG_AUTO_CANCEL;
                                    NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
                                    notificationManagerCompat.notify(101, notification);
                                }
                            }
                        }
                    }
                }

                telegramRensponseOut = msgOut;
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(2000);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public static final String REQUEST_METHOD = "GET";
    public static final int READ_TIMEOUT = 15000;
    public static final int CONNECTION_TIMEOUT = 15000;

    JSONObject sendTelegramMsg (String myUrlString) throws JSONException {
        JSONObject Jresult ;
        String result = "";
        String inputLine;

        try {
            URL myUrl = new URL(myUrlString);

            HttpURLConnection connection =(HttpURLConnection) myUrl.openConnection();

            connection.setRequestMethod(REQUEST_METHOD);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setConnectTimeout(CONNECTION_TIMEOUT);

            connection.connect();

            InputStreamReader streamReader = new  InputStreamReader(connection.getInputStream());

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

}
