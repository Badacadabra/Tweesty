package net.badacadabra.tweesty;

import java.util.HashSet;
import java.util.Set;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import net.badacadabra.tweesty.UsersListContract.UserEntry;
import net.badacadabra.tweesty.R;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Activité principale permettant à l'utilisateur de gérer une liste de Twittos.
 * 
 * @author Macky Dieng
 * @author Steeve Tsamba
 * @author Baptiste Vannesson
 */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean dual_pane = getResources().getBoolean(R.bool.dual_pane);
        if (dual_pane) {
            setContentView(R.layout.fragments);
        } else {
            setContentView(R.layout.users_fragment);
        }
        // Insertion de quelques données en BDD
        DbHelper mDbHelper = new DbHelper(this);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Set<String> basicUsers = new HashSet<String>();
        basicUsers.add("SteeveTs");
        basicUsers.add("dieng445");
        basicUsers.add("Badacadabra");
        for (String user : basicUsers) {
            if (!isUserInDb(user)) {
                ContentValues values = new ContentValues();
                values.put(UserEntry.COLUMN_NAME_CONTENT, user);
                db.insert(UserEntry.TABLE_NAME, null, values);
            }
        }
        // Initialisation des préférences
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        // On affiche la liste par défaut
        displayUsersList();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.about) {
         // On redirige vers une nouvelle activité (à propos)
            Intent about = new Intent(getApplicationContext(), AboutActivity.class);
            startActivity(about);
        }
        if (id == R.id.settings) {
            // On redirige vers une nouvelle activité (paramètres)
            Intent settings = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(settings);
        }
        return super.onOptionsItemSelected(item);
    }
    
    /**
     * Méthode gérant l'action sur le bouton permettant d'ajouter un utilisateur.
     * Si l'utilisateur existe déjà dans la liste, on affiche un message d'alerte.
     * On veille aussi à traiter le cas où la saisie de l'utilisateur contient déjà un « @ ».
     * 
     * @param view Une vue
     */
    public void addUserHandler(View view) {
        DbHelper mDbHelper = new DbHelper(this);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        EditText editText = (EditText) findViewById(R.id.enterUser);
        String username = editText.getText().toString();
        username = username.replace('@', '\0');
        if (!isUserInDb(username)) {
            values.put(UserEntry.COLUMN_NAME_CONTENT, username);
            db.insert(UserEntry.TABLE_NAME, null, values);
        } else {
            Toast.makeText(this, "L'utilisateur existe déjà !", Toast.LENGTH_SHORT).show();
        }
        editText.setText("");
        displayUsersList();
    }
    
    /**
     * Méthode gérant l'action sur le bouton permettant de supprimer un utilisateur.
     * Si l'utilisateur n'existe pas dans la liste, on affiche un message d'alerte.
     * 
     * @param view Une vue
     */
    public void removeUserHandler(View view) {
        DbHelper mDbHelper = new DbHelper(this);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        EditText editText = (EditText) findViewById(R.id.enterUser);
        String username = editText.getText().toString();
        String whereClause = UserEntry.COLUMN_NAME_CONTENT + " = '" + username + "'"; 
        if (isUserInDb(username)) {
            db.delete(UserEntry.TABLE_NAME, whereClause, null);
        } else {
            Toast.makeText(this, "L'utilisateur n'existe pas !", Toast.LENGTH_SHORT).show();
        }
        editText.setText("");
        displayUsersList();
    }
    
    /**
     * Récupère tous les utilisateurs en base de données.
     * 
     * @return Liste des utilisateurs en BDD
     */
    public Cursor readUsersFromDb() {
        DbHelper mDbHelper = new DbHelper(this);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] projection = {
            UserEntry._ID,
            UserEntry.COLUMN_NAME_CONTENT,
            };

        Cursor c = db.query(
            UserEntry.TABLE_NAME,  // Table à requêter
            projection,             // Colonnes à retourner
            null,                  // Colonnes pour la clause WHERE
            null,                  // Valeurs pour la clause WHERE
            null,                  // ne pas grouper les lignes
            null,                  // ne pas filtrer par groupe de lignes
            null                   // L'ordre de filtrage (ASC / DESC)
            );
        return c;
    }
    
    /**
     * Teste la présence d'un utilisateur en base de données.
     * 
     * @param username Nom de l'utilisateur recherché
     * @return Vrai ou faux
     */
    public boolean isUserInDb(String username) {
        Cursor c = readUsersFromDb();
        boolean isUserInDb = false;
        while(c.moveToNext()){
            String uname = c.getString(c.getColumnIndex("user_name"));
            if (uname.equals(username)) {
                isUserInDb = true;
            }
        }
        return isUserInDb;
    }
    
    /**
     * Affiche la liste de tous les utilisateurs
     */
    public void displayUsersList() {
        Cursor c = readUsersFromDb();
        ListView listView = (ListView) findViewById(R.id.listView);
        String[] listItems = new String[c.getCount()];
        int i = 0;
        while(c.moveToNext()){
            String uname = "@" + c.getString(c.getColumnIndex("user_name"));
            listItems[i] = uname;
            i++;
        }
        int id_layout = android.R.layout.simple_list_item_1;
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, id_layout, listItems);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adView, View target, int position, long id) {
                String username = (String) adView.getItemAtPosition(position);
                boolean dual_pane = getResources().getBoolean(R.bool.dual_pane);
                if (dual_pane) {
                    TwitterFeedFragment twitterFeedFragment = (TwitterFeedFragment) getFragmentManager().findFragmentById(R.id.twitterFeedFragment);
                    twitterFeedFragment.displayTweets(username);
                } else {
                    Intent twitterFeed = new Intent(getApplicationContext(), TwitterFeedActivity.class);
                    twitterFeed.putExtra("USER_NAME", username);
                    startActivity(twitterFeed);
                }
            }
        });
    }
    
}
