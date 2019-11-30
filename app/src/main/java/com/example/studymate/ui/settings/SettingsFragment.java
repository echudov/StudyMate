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
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import com.example.studymate.R;

public class SettingsFragment extends Fragment {

    private SettingsViewModel settingsViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        settingsViewModel =
                ViewModelProviders.of(this).get(SettingsViewModel.class);
        addChildSettings();
        View root = inflater.inflate(R.layout.fragment_settings, container, false);
        /*
        final TextView textView = root.findViewById(R.id.text_settings);
        settingsViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
         */
        return root;
    }

    // messing around with fragments for preferences n stuff
    public static class ChildSettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.settings_child, rootKey);
        }

        // no fucking clue what this is lmao
        // maybe when im on the shitter i'll eventually learn.
        // for now this will stay and perplex me and god
        /*
        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            // To get a preference
            PreferenceScreen preferenceScreen = getPreferenceScreen();
            androidx.preference.Preference preference = preferenceScreen.findPreference("preference_ key_defined_in_the_xml");

            //You can set a listener
            preference.setOnPreferenceClickListener(new androidx.preference.Preference().OnPreferenceClickListener());
            public boolean onPreferenceClick(Preference preference) {
                    return false;
                }
            }

            //change title
            preference.setTitle("my_title");

            // etc
        } */
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