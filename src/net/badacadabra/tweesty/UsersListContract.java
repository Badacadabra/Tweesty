package net.badacadabra.tweesty;

import android.provider.BaseColumns;

/**
 * Classe définissant le contrat avec la BDD pour le stockage des utilisateurs
 * 
 * @author Macky Dieng
 * @author Steeve Tsamba
 * @author Baptiste Vannesson
 */
public final class UsersListContract {
    
    /**
     * Structure de la table stockant les utilisateurs en BDD
     */
    public static abstract class UserEntry implements BaseColumns {
        
        public static final String TABLE_NAME = "users";
        public static final String COLUMN_NAME_CONTENT = "user_name";
        
    }
    
}
