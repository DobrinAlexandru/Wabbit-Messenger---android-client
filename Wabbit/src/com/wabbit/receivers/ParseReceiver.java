package com.wabbit.receivers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.wabbit.abstracts.Enums;
import com.wabbit.activity.MainActivity;
import com.wabbit.imagesutils.ImageUtils;
import com.wabbit.libraries.remoting.FBMgr;
import com.wabbit.messenger.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by Bogdan Tirca on 01.03.2014.
 */
public class ParseReceiver extends BroadcastReceiver {
    private static final String TAG = "MyCustomReceiver";

    @Override
    public void onReceive(final Context context, Intent intent) {
        try {
            final JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));

            if(MainActivity.gi() != null) {//Show message in app
                MainActivity.gi().onNewMessage();
                handleSoundAndVibration(context);
            }
            else{//Show changes in notification
                final String uid = json.getString(Enums.ParseKey.MSG_FROM_ID);
                //If msg doesn't have a fbId, get user first
                if(json.has(Enums.ParseKey.MSG_FROM_FB_ID) == false) {
                    ParseQuery<ParseUser> query = ParseUser.getQuery();
                    query.whereEqualTo("objectId", uid);
                    query.getFirstInBackground(new GetCallback<ParseUser>() {
                        @Override
                        public void done(ParseUser parseUser, ParseException e) {
                            if (parseUser != null) {//User loaded
                                getFbImage(parseUser.getString(Enums.ParseKey.USER_FBID))
                                        .continueWith(new Continuation<Bitmap, Void>() {
                                            @Override
                                            public Void then(Task<Bitmap> object) throws Exception {
                                                showNottification(context, object.getResult(),
                                                        json.getString(Enums.ParseKey.MSG_FROM_NAME),
                                                        json.getString(Enums.ParseKey.MSG_BODY));
                                                return null;
                                            }
                                        });
                            } else {//User not loaded. Show launcher image
                                Log.d("notif", "not loaded");
                                try {
                                    showNottification(context, null,
                                            json.getString(Enums.ParseKey.MSG_FROM_NAME),
                                            json.getString(Enums.ParseKey.MSG_BODY));
                                } catch (Exception ee) {
                                };
                            }
                        }
                    });
                }
                else{//msg has fbid
                    final String fbId = json.getString(Enums.ParseKey.MSG_FROM_FB_ID);
                    getFbImage(fbId)
                            .continueWith(new Continuation<Bitmap, Void>() {
                                @Override
                                public Void then(Task<Bitmap> object) throws Exception {
                                    showNottification(context, object.getResult(),
                                            json.getString(Enums.ParseKey.MSG_FROM_NAME),
                                            json.getString(Enums.ParseKey.MSG_BODY));
                                    return null;
                                }
                            });
                }
            }

        } catch (JSONException e) {
            Log.d(TAG, "JSONException: " + e.getMessage());
        }
    }

    private Task<Bitmap> getFbImage(final String fbId){
        return Task.<Bitmap>callInBackground(new Callable<Bitmap>() {
            @Override
            public Bitmap call() throws Exception {
                return ImageUtils.downloadBitmap(FBMgr.gi().getLinkToProfilePictureByFbId(fbId));
            }
        });
    }

    private static int notId = 101;
    public void showNottification(Context ctx, Bitmap bmp, String from, String msg){
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ctx)
                .setAutoCancel(true)
                .setContentTitle(from)
                .setContentText(msg)
                .setSmallIcon(R.drawable.ic_launcher);
        if(bmp != null) {
            mBuilder.setLargeIcon(bmp);
            Log.d("notif", "loaded");
        }

//        NotificationCompat.BigPictureStyle bigPicStyle = new NotificationCompat.BigPictureStyle();
//        bigPicStyle.bigPicture(bmp);
//        bigPicStyle.setBigContentTitle("Dhaval Sodha Parmar");
//        mBuilder.setStyle(bigPicStyle);

        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(ctx, MainActivity.class);

        // The stack builder object will contain an artificial back stack
        // for
        // the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out
        // of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(ctx);

        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);

        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager = (NotificationManager)ctx. getSystemService(Context.NOTIFICATION_SERVICE);

        // mId allows you to update the notification later on.
        mNotificationManager.notify(notId ++, mBuilder.build());

        handleSoundAndVibration(ctx);
    }

    private void handleSoundAndVibration(Context ctx){
        AudioManager am = (AudioManager)ctx.getSystemService(Context.AUDIO_SERVICE);
        switch (am.getRingerMode()) {
            case AudioManager.RINGER_MODE_SILENT:
                Log.i("MyApp","Silent mode");
            case AudioManager.RINGER_MODE_VIBRATE:
                Log.i("MyApp","Vibrate mode");
                Vibrator v = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(100);
                break;
            case AudioManager.RINGER_MODE_NORMAL:
                Log.i("MyApp","Normal mode");
                ringtone(ctx);
                break;
        }
    }
    public void ringtone(Context ctx){
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(ctx.getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
