package com.example.studymate.ui;

import android.app.Activity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

public class GeneralFunctions {
    public static String getEmail(Activity context) {
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(context);
        try {
            return acct.getEmail();
        } catch (Exception e) {
            System.out.println("Could not return email of user");
            e.printStackTrace();
            return "";
        }
    }
}
