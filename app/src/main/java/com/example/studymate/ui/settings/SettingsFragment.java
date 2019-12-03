package com.example.studymate.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import com.example.studymate.LoginActivity;
import com.example.studymate.R;
import com.example.studymate.ui.GeneralFunctions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

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
            signOutEmail.getOnPreferenceClickListener() {
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .build();
                GoogleSignInClient currentSignIn = GoogleSignIn.getClient(getActivity(), gso);

                currentSignIn.signOut();
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