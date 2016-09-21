package net.badacadabra.tweesty;

import android.provider.BaseColumns;

/**
 * Classe définissant le contrat avec la BDD pour le stockage des tweets
 * 
 * @author Macky Dieng
 * @author Steeve Tsamba
 * @author Baptiste Vannesson
 */
public final class TwitterFeedContract {
    
    /**
     * Structure de la table stockant les tweets en BDD
     */
    public static abstract class TwitterEntry implements BaseColumns {
        
        public static final String TABLE_NAME = "twitter_feed";
        public static final String COLUMN_NAME_ENTRY_ID = "username";
        public static final String COLUMN_NAME_CONTENT = "tweet_content";
        
    }
    
}
