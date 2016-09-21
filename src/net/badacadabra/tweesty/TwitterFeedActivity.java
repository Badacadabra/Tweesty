package net.badacadabra.tweesty;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import net.badacadabra.tweesty.TwitterFeedContract.TwitterEntry;
import net.badacadabra.tweesty.R;

/**
 * Activité gérant l'affichage et le stockage des fils de tweets.
 * 
 * @author Macky Dieng
 * @author Steeve Tsamba
 * @author Baptiste Vannesson
 */
public class TwitterFeedActivity extends ListActivity {

        /** Vue affichant les tweets */
        private ListView tweetDisplay;
        
        /** Nom d'utilisateur */
        private String username;
        
        /** Constante symbolisant le choix de l'utilisateur d'être averti pour les mises à jour (paramètres) */
        public static final String UPDATE_PREF_WARN = "0";
        
        /** Constante symbolisant le choix de l'utilisateur de ne jamais être averti pour les mises à jour (paramètres) */
        public static final String UPDATE_PREF_NEVER = "1";
        
        /** Constante symbolisant le choix de l'utilisateur d'être averti pour les mises à jour uniquement lorsqu'il a du Wi-Fi (paramètres) */
        public static final String UPDATE_PREF_WIFI = "2";
        
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // Portrait ou paysage
            if (getResources().getBoolean(R.bool.dual_pane)) {
                finish();
                return;
             }
            setContentView(R.layout.tweets_fragment);
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                username = extras.getString("USER_NAME");
            }
            ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo net = manager.getActiveNetworkInfo();
            NetworkInfo wifinet = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String option = prefs.getString("updatePreferences", "");

            // Si l'utilisateur n'a pas choisi l'option de ne jamais être averti
            if (!option.equals(UPDATE_PREF_NEVER)) {
                // Si l'utilisateur veut faire la mise à jour uniquement lorsqu'il est connecté en Wi-Fi
                if (option.equals(UPDATE_PREF_WIFI)) {
                    if (wifinet.isConnected()) {
                        new DownloadTweetsTask().execute(username);
                        Toast.makeText(this, "Données mises à jour", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Données provenant de la BDD", Toast.LENGTH_SHORT).show();
                        displayTweetsFromDb();
                    }
                } else { // Si l'utilisateur veut qu'on lui demande systématiquement s'il veut mettre à jour les données
                    // Si le réseau est disponible...
                    if (net != null && net.isConnected()) {
                        new AlertDialog.Builder(this)
                        .setTitle("Mise à jour")
                        .setMessage("Mettre à jour les tweets ?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) { 
                                // On récupère les infos sur Twitter via HTTP
                                new DownloadTweetsTask().execute(username);
                                Toast.makeText(getApplicationContext(), "Données mises à jour", Toast.LENGTH_SHORT).show();
                            }
                         })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) { 
                                // On récupère les infos dans la BDD
                                Toast.makeText(getApplicationContext(), "Données provenant de la BDD", Toast.LENGTH_SHORT).show();
                                displayTweetsFromDb();
                            }
                         })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                         .show();
                    }
                } 
            } else { // Si l'utilisateur ne veut jamais mettre à jour les données
                Toast.makeText(this, "Données provenant de la BDD", Toast.LENGTH_SHORT).show();
                displayTweetsFromDb();
            }
            
            // Si le réseau est indisponible (mode hors ligne)
            if (net == null || !net.isConnected()) {
               Toast.makeText(this, "Réseau indisponible", Toast.LENGTH_SHORT).show();
               displayTweetsFromDb();
            }
                
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
                // On redirige vers une nouvelle activité (préférences)
                Intent settings = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(settings);
            }
            return super.onOptionsItemSelected(item);
        }
        
        @Override
        protected void onListItemClick(ListView list, View view, int position, long id) {
            super.onListItemClick(list, view, position, id);
            String selectedItem = (String) getListView().getItemAtPosition(position);
            Toast.makeText(this, selectedItem, Toast.LENGTH_SHORT).show();
        }
        
        /**
         * Récupère et affiche les tweets à partir de la base de données.
         */
        public void displayTweetsFromDb() {
            DbHelper mDbHelper = new DbHelper(this);
            SQLiteDatabase db = mDbHelper.getReadableDatabase();

            String[] projection = {
                TwitterEntry._ID,
                TwitterEntry.COLUMN_NAME_ENTRY_ID,
                TwitterEntry.COLUMN_NAME_CONTENT,
                };

            String whereClause = TwitterFeedContract.TwitterEntry.COLUMN_NAME_ENTRY_ID + "=?";
            String[] whereArgs = {username};
            
            Cursor c = db.query(
                TwitterEntry.TABLE_NAME,  // Table à requêter
                projection,               // Colonnes à retourner
                whereClause,             // Colonnes pour la clause WHERE
                whereArgs,               // Valeurs pour la clause WHERE
                null,                  // ne pas grouper les lignes
                null,                  // ne pas filtrer par groupe de lignes
                null                   // L'ordre de filtrage (ASC / DESC)
                );
            
            String[] listItems = new String[c.getCount()];
            int i = 0;
            while(c.moveToNext()){
                String tweet = c.getString(c.getColumnIndex("tweet_content"));
                listItems[i] = tweet;
                i++;
            }
            TextView textView = (TextView) findViewById(R.id.username);
            textView.setText(username);
            List<String> tweets = new ArrayList<String>();
            for (String listItem : listItems) {
                tweets.add(listItem);
            }
            tweetDisplay = (ListView) findViewById(android.R.id.list);
            ArrayAdapter<String> tweetAdapter = new ArrayAdapter<String>(TwitterFeedActivity.this, 
                     R.layout.list_item, R.id.listText, tweets);
            tweetDisplay.setAdapter(tweetAdapter);
        }
        
        /** 
         * Classe interne pour télécharger des données provenant de Twitter
	 * (Merci à https://github.com/diogo-alves/leitor-tweets pour l'inspiration sur cette partie...)
         */
        private class DownloadTweetsTask extends AsyncTask<String, Void, ArrayList<Tweet>> {
            
            private static final String URL_BASE = "https://api.twitter.com";
            private static final String URL_SEARCH = URL_BASE + "/1.1/statuses/user_timeline.json?screen_name=";
            private static final String URL_AUTH = URL_BASE + "/oauth2/token";
            private static final String CONSUMER_KEY = "epLM1FvEqMmLhfVb53A3fXdZf";
            private static final String CONSUMER_SECRET = "4imPBAx08ZrQamx1pIL7FbbBTcCuWWnSX0HMWFJIZtHvPVLI57";

            private String authApp() {

                HttpURLConnection connection = null;
                OutputStream os = null;
                BufferedReader br = null;
                StringBuilder response = null;

                try {
                    URL url = new URL(URL_AUTH);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    connection.setDoInput(true);

                    String credentialsAccess = CONSUMER_KEY + ":" + CONSUMER_SECRET;
                    String authorization = "Basic " + Base64.encodeToString(credentialsAccess.getBytes(), Base64.NO_WRAP);
                    String parameter = "grant_type=client_credentials";

                    connection.addRequestProperty("Authorization", authorization);
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
                    connection.connect();
                    
                    os = connection.getOutputStream();
                    os.write(parameter.getBytes());
                    os.flush();
                    os.close();

                    br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line;
                    response = new StringBuilder();

                    while ((line = br.readLine()) != null) {            
                            response.append(line); 
                    }

                    Log.d("Code de réponse POST", String.valueOf(connection.getResponseCode()));
                    Log.d("Réponse JSON", response.toString());

                } catch (Exception e) {
                    Log.e("Error POST", Log.getStackTraceString(e));
                        
                } finally{
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
                return response.toString();
            }

            @Override
            protected ArrayList<Tweet> doInBackground(String... param) {
    
                String keyword = param[0];
                ArrayList<Tweet> tweets = new ArrayList<Tweet>();
                HttpURLConnection connection = null;
                BufferedReader br = null;

                try {
                    URL url = new URL(URL_SEARCH + keyword);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    // Utilisation d'un token d'accès (JSON)
                    String jsonString = authApp();
                    JSONObject jsonAccess = new JSONObject(jsonString);
                    String tokenHolder = jsonAccess.getString("token_type") + " " + 
                                    jsonAccess.getString("access_token");

                    connection.setRequestProperty("Authorization", tokenHolder);
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.connect();

                    // Récupération des tweets de l'API
                    br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                    String line;
                    StringBuilder response = new StringBuilder();

                    while ((line = br.readLine()) != null){            
                        response.append(line); 
                    }
                    
                    Log.d("Code de réponse GET", String.valueOf(connection.getResponseCode()));
                    Log.d("Réponse JSON", response.toString());
                    
                    JSONArray jsonArray = new JSONArray(response.toString());
                    JSONObject jsonObject;

                    for (int i = 0; i < jsonArray.length(); i++) {
                            
                        jsonObject = (JSONObject) jsonArray.get(i);
                        
                        Tweet tweet = new Tweet();
                        tweet.setUser(jsonObject.getJSONObject("user").getString("screen_name"));
                        tweet.setMessage(jsonObject.getString("text"));

                        Log.d("Tweet", tweet.toString());
                        
                        tweets.add(i, tweet);
                    }

                } catch (Exception e) {
                    Log.e("Error GET: ", Log.getStackTraceString(e));
                    
                } finally {
                    if (connection != null){
                        connection.disconnect();
                    }
                }
                return tweets;
            }
    
            @Override
            protected void onPostExecute(ArrayList<Tweet> tweets) {
                if (tweets.isEmpty()) {
                        // Si aucun tweet n'est renvoyé par l'API
                        Toast.makeText(TwitterFeedActivity.this, "Aucun tweet trouvé ! ", Toast.LENGTH_SHORT).show();
                } else {
                    // Si des tweets ont été renvoyés par l'API, on les affiche
                    TextView textView = (TextView) findViewById(R.id.username);
                    textView.setText(username);
                    tweetDisplay = (ListView) findViewById(android.R.id.list);
                    ArrayAdapter<Tweet> tweetAdapter = new ArrayAdapter<Tweet>(TwitterFeedActivity.this, 
                            R.layout.list_item, R.id.listText, tweets);
                    tweetDisplay.setAdapter(tweetAdapter);
                    // On insère les tweets en BDD
                    DbHelper mDbHelper = new DbHelper(getApplicationContext());
                    SQLiteDatabase db = mDbHelper.getWritableDatabase();
                    List<String> tweetsStrings = new ArrayList<String>();
                    // On convertit l'ArrayList de tweets en ArrayList de chaînes de caractères
                    for (Tweet tweet : tweets) {
                        tweetsStrings.add(tweet.toString());
                    }
                    // On enregistre les tweets dans la BDD
                    for (String tweetString : tweetsStrings) {
                        ContentValues values = new ContentValues();
                        values.put(TwitterEntry.COLUMN_NAME_ENTRY_ID, username);
                        values.put(TwitterEntry.COLUMN_NAME_CONTENT, tweetString);
                        db.insert(TwitterEntry.TABLE_NAME, null, values);
                    }
                    // Notification pour l'utilisateur
                    Toast.makeText(TwitterFeedActivity.this, "Tweets stockés en BDD !", Toast.LENGTH_SHORT).show();
                }
            }

        }

}
