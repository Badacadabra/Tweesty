package net.badacadabra.tweesty;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Activité gérant l'affichage des informations sur le projet.
 * 
 * @author Macky Dieng
 * @author Steeve Tsamba
 * @author Baptiste Vannesson
 */
public class AboutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        List<String> authors = new ArrayList<String>();
        authors.add("Macky Dieng");
        authors.add("Steeve Tsamba");
        authors.add("Baptiste Vannesson");
        int id_layout = android.R.layout.simple_list_item_1;
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, id_layout, authors);
        ListView listAuthors = (ListView) findViewById(R.id.listAuthors);
        listAuthors.setAdapter(adapter);
    }
    
}
