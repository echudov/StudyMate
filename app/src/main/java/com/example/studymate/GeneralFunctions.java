package com.example.studymate;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.InputStream;

public class GeneralFunctions {
    public static String getEmail(Activity context) {
        FirebaseUser acct = FirebaseAuth.getInstance().getCurrentUser();
        try {
            return acct.getEmail();
        } catch (Exception e) {
            System.out.println("Could not return email of user");
            e.printStackTrace();
            return "";
        }
    }
    public static Bitmap getProfilePic(Activity context) {
        FirebaseUser acct = FirebaseAuth.getInstance().getCurrentUser();
        Bitmap profilePic = null;
        String url = acct.getPhotoUrl().toString();
        System.out.println(url);
        try {
            InputStream inputStream = new java.net.URL(url).openStream();
            profilePic = BitmapFactory.decodeStream(inputStream);
        } catch (Exception e) {
            Log.e("Error: ", e.getMessage());
            e.printStackTrace();
        }
        return profilePic;
    }

    /**
     * obtained from https://gist.github.com/jewelzqiu/c0633c9f3089677ecf85
     * @param bitmap bitmap to turn into a circle
     * @return circle for bitmap
     */
    public static Bitmap getCircledBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2, bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }
}
