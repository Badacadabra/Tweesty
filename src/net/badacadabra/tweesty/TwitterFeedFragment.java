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

import android.app.ListFragment;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import net.badacadabra.tweesty.TwitterFeedContract.TwitterEntry;

/**
 * Fragment associé à l'affichage des fils de tweets
 * 
 * @author Macky Dieng
 * @author Steeve Tsamba
 * @author Baptiste Vannesson
 */
public class TwitterFeedFragment extends ListFragment {
    
    /** Nom d'utilisateur */
    private String username;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.twitter_feed, container, false);
        return view;
    }
    
    /**
     * Méthode téléchargeant et affichant dans le fragment de droite les tweets d'un utilisateur donné
     * 
     * @param username
     */
    public void displayTweets(String username) {
        this.username = username;
        new DownloadTweetsTask().execute(username);
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
                    Toast.makeText(getActivity(), "Aucun tweet trouvé ! ", Toast.LENGTH_SHORT).show();
            } else {
                // Si des tweets ont été renvoyés par l'API, on les affiche
                TextView textView = (TextView) getView().findViewById(R.id.username);
                textView.setText(username);
                ListView tweetDisplay = (ListView) getView().findViewById(android.R.id.list);
                ArrayAdapter<Tweet> tweetAdapter = new ArrayAdapter<Tweet>(getActivity(), 
                        R.layout.list_item, R.id.listText, tweets);
                tweetDisplay.setAdapter(tweetAdapter);
                // On insère les tweets en BDD
                DbHelper mDbHelper = new DbHelper(getActivity());
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
            }
        }
  
    }
    
}


