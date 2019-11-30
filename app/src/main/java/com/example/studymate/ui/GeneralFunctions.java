package com.example.studymate.ui;

import android.app.Activity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

public class GeneralFunctions {
    public static String getEmail(Activity context) {
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(context);
        return acct.getEmail();
    }
}
