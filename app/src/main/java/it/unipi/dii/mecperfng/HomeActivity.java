package it.unipi.dii.mecperfng;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.preference.Preference;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import it.unipi.dii.mecperfng.Fragment.*;

public class HomeActivity extends AppCompatActivity {

    //BOTTOM MENU
    BottomNavigationView menuNavigazione;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //---------------------------------------------MENU NAVIGAZIONE-----------------------------------------------
        menuNavigazione = findViewById(R.id.menu);
        menuNavigazione.setOnNavigationItemSelectedListener(navigationEventListener);
        //di default scegliamo il testing fragment
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new TestingFragment()).commit();
        menuNavigazione.setSelectedItemId(R.id.testing_icon);


    }

    private BottomNavigationView.OnNavigationItemSelectedListener navigationEventListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {

                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                    Fragment selectedFragment = null;
                    Fragment actualFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

                    switch (menuItem.getItemId()) {
                        case R.id.preferences_icon:
                            selectedFragment = new PreferencesFragment();
                            break;
                        case R.id.storage_icon:
                            selectedFragment = new StorageFragment();
                            break;
                        default:
                            selectedFragment = new TestingFragment();
                            break;
                    }
                    //mostriamo il fragment selezionato se è diverso da quello corrente
                   if(actualFragment instanceof PreferencesFragment && selectedFragment instanceof PreferencesFragment)
                       return true;
                    if(actualFragment instanceof StorageFragment && selectedFragment instanceof StorageFragment)
                        return true;
                    if(actualFragment instanceof TestingFragment && selectedFragment instanceof TestingFragment)
                        return true;
                   // if(actualFragment.getClass().toString().equals(selectedFragment.getClass().toString()))
                         getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
                    return true;
                }


            };



}
