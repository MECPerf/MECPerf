package it.unipi.dii.mecperfng.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import it.unipi.dii.mecperfng.R;


public class PreferencesFragment extends PreferenceFragmentCompat {

    private SharedPreferences preferences;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        FrameLayout fl = getActivity().findViewById(R.id.fragment_container);
        fl.setBackground(getResources().getDrawable(R.drawable.rounded_menubar));
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Preference pref = findPreference("Direction");
        ListPreference listPref = (ListPreference) pref;
        if(listPref.getValue().toString().equals("0"))
            listPref.setSummary("Receiver");
        else
            listPref.setSummary("Sender");


        preferences.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                Preference pref = findPreference(key);
                if (pref instanceof ListPreference) {
                    ListPreference listPref = (ListPreference) pref;
                    pref.setSummary(listPref.getEntry());

                }
            }
        });

    }
}

