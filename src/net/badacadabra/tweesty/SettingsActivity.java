package net.badacadabra.tweesty;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.view.MenuItem;
import android.preference.PreferenceActivity;
import android.widget.Toast;
import net.badacadabra.tweesty.UsersListContract.UserEntry;
import net.badacadabra.tweesty.R;
import net.badacadabra.tweesty.TwitterFeedContract.TwitterEntry;

/**
 * Activité permettant à l'utilisateur de paramétrer son application (settings).
 * 
 * @author Macky Dieng
 * @author Steeve Tsamba
 * @author Baptiste Vannesson
 */
public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

    /** Constante représentant la clé identifiant la liste des préférences de mise à jour */
    public static final String UPDATE_OPTION = "updatePreferences";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        Preference deleteDatabase = (Preference) findPreference("deleteDatabase");
        deleteDatabase.setOnPreferenceClickListener(new OnPreferenceClickListener() {
             public boolean onPreferenceClick(Preference preference) {
                 // Vider la BDD
                 DbHelper mDbHelper = new DbHelper(getApplicationContext());
                 SQLiteDatabase db = mDbHelper.getWritableDatabase();
                 db.delete(UserEntry.TABLE_NAME, null, null);
                 db.delete(TwitterEntry.TABLE_NAME, null, null);
                 Toast.makeText(getApplicationContext(), "BDD vidée avec succès !", Toast.LENGTH_SHORT).show();
                 Intent main = new Intent(getApplicationContext(), MainActivity.class);
                 startActivity(main);
                 return true;
             }
         });
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(UPDATE_OPTION)) {
            Preference updatePref = findPreference(key);
        }
    }
    
}
