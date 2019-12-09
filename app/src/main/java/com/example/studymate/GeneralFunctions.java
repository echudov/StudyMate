package com.example.studymate;

import android.app.Activity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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

    /**
     * Saves any data type to a specified location in the current database.
     * @param reference The name of the location in the database
     * @param value The data to save
     */
    public static void writeToDatabase(String reference, Object value) {
        FirebaseDatabase currentDatabase = FirebaseDatabase.getInstance();
        DatabaseReference referenceToWrite = currentDatabase.getReference(reference);
        referenceToWrite.setValue(value);
    }

    /**
     * Deletes all the data at the specified location in the database
     * @param reference The name of the location in the database
     */
    public static void deleteDataFromDatabase(String reference) {
        FirebaseDatabase currentDatabase = FirebaseDatabase.getInstance();
        DatabaseReference referenceToClear = currentDatabase.getReference(reference);
        referenceToClear.removeValue();
    }
}
