package com.wabbit.libraries;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by Bogdan Tirca on 01.04.2014.
 */
public class Email {
    public static void sendEmail(Context context, String to, String subject, String body){
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"contact@getwabbit.com"});
        i.putExtra(Intent.EXTRA_SUBJECT, subject);
        i.putExtra(Intent.EXTRA_TEXT   , body);
        try {
            context.startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(context, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }

    public static void sendFeedbackEmail(Context context){
        sendEmail(context,
                "contact@getwabbit.com",
                "Feedback",
                "");
    }
}
