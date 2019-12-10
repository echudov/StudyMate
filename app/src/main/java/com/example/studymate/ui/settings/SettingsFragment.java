package com.example.studymate.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.studymate.LoginActivity;
import com.example.studymate.R;
import com.example.studymate.GeneralFunctions;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsFragment extends Fragment {

    private SettingsViewModel settingsViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        settingsViewModel =
                ViewModelProviders.of(this).get(SettingsViewModel.class);
        addChildSettings();
        View root = inflater.inflate(R.layout.fragment_settings, container, false);
        return root;
    }

    // messing around with fragments for preferences n stuff
    public static class ChildSettingsFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.settings_child, rootKey);

            // For putting the email the user logged in with
            Preference signOutEmail = findPreference("logout");
            Intent signOutSendToLogin = new Intent(getActivity(), LoginActivity.class);

            // Make a click on sign out launch LoginActivity
            signOutEmail.setIntent(signOutSendToLogin);
            // Make a click actually SIGN OUT the user
            Preference.OnPreferenceClickListener signedOut = signOutEmail.getOnPreferenceClickListener();
            if (signedOut != null) {
                FirebaseAuth.getInstance().signOut();

                // Google sign out
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();
                GoogleSignIn.getClient(getContext(), gso).signOut()
                        .addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Intent intent = new Intent(getContext(), LoginActivity.class);
                                startActivity(intent);
                            }
                        });
            }

            if (signOutEmail != null) {
                String email = GeneralFunctions.getEmail(getActivity());
                System.out.println(email);
                if (email != null) {
                    signOutEmail.setSummary(email);
                }
            }
        }
    }


    /**
     * Jesus christ i wanted to kill myself but it's finally done
     * does exactly what you think it does, swaps fragment_settings (since it's empty)
     * with a a preference fragment of ChildSettingsFragment
     * will most likely keep preference xml static to keep myself from killing myself
     * (deep I know)
     */
    private void addChildSettings() {
        try {
            getFragmentManager().beginTransaction().replace(R.id.fragment_settings,new
                    ChildSettingsFragment()).commit();
        } catch (Exception e) {
            System.out.println("An error occured.");
            e.printStackTrace();
        }
    }
}