package com.example.studymate.ui.settings;

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

import com.example.studymate.R;
import com.example.studymate.ui.GeneralFunctions;

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
            EditTextPreference signOutEmail = findPreference("logout");
            if (signOutEmail != null) {
                signOutEmail.setSummaryProvider(new Preference.SummaryProvider() {
                    @Override
                    public CharSequence provideSummary(Preference preference) {
                        if (GeneralFunctions.getEmail(getActivity()) == null) {
                            return "";
                        }
                        return GeneralFunctions.getEmail(getActivity());
                    }
                });
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
    public void addChildSettings() {
        getFragmentManager().beginTransaction().replace(R.id.fragment_settings,new
                ChildSettingsFragment()).commit();
    }
}