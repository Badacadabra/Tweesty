package net.badacadabra.tweesty;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import net.badacadabra.tweesty.TwitterFeedContract.TwitterEntry;
import net.badacadabra.tweesty.UsersListContract.UserEntry;

/**
 * Classe gérant le cœur de la base de données.
 * 
 * @author Macky Dieng
 * @author Steeve Tsamba
 * @author Baptiste Vannesson
 */
public class DbHelper extends SQLiteOpenHelper {
    
    /**
     * Constante représentant la version de la base de données.
     * Penser à incrémenter ce chiffre en cas de modification du schéma (contrat).
     */
    public static final int DATABASE_VERSION = 2;
    
    /** Constante représentant le nom de la base de données */
    public static final String DATABASE_NAME = "Tweesty.db";

    /** Constante représentant le type de données TEXT en SQLite */
    private static final String TEXT_TYPE = " TEXT";
    
    /** Constante contenant la requête pour créer la table des utilisateurs */
    private static final String SQL_CREATE_USERS_LIST =
        "CREATE TABLE " + UserEntry.TABLE_NAME + " (" +
        UserEntry._ID + " INTEGER PRIMARY KEY," +
        UserEntry.COLUMN_NAME_CONTENT + TEXT_TYPE +
        " )";
    
    /** Constante contenant la requête pour créer la table des tweets */
    private static final String SQL_CREATE_TWITTER_FEED =
        "CREATE TABLE " + TwitterEntry.TABLE_NAME + " (" +
        TwitterEntry._ID + " INTEGER PRIMARY KEY," +
        TwitterEntry.COLUMN_NAME_ENTRY_ID + TEXT_TYPE + "," +
        TwitterEntry.COLUMN_NAME_CONTENT + TEXT_TYPE +
        " )";
    
    /** Constante contenant la requête pour supprimer la table des utilisateurs */
    private static final String SQL_DELETE_USERS_LIST =
        "DROP TABLE IF EXISTS " + UserEntry.TABLE_NAME;
    
    /** Constante contenant la requête pour supprimer la table des tweets */
    private static final String SQL_DELETE_TWITTER_FEED =
        "DROP TABLE IF EXISTS " + TwitterEntry.TABLE_NAME;
    
    /**
     * Unique constructeur de la classe
     * 
     * @param context Contexte
     */
    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Méthode de création de toutes les tables (utilisateurs et tweets)
     * 
     * @param db Instance de BDD
     */
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_USERS_LIST);
        db.execSQL(SQL_CREATE_TWITTER_FEED);
    }

    /**
     * Méthode de mise à jour de la base de données
     * 
     * @param db Instance de BDD
     * @param oldVersion Ancienne version
     * @param newVersion Nouvelle version
     */
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_USERS_LIST);
        db.execSQL(SQL_DELETE_TWITTER_FEED);
        onCreate(db);
    }
    
    /**
     * Méthode de mise à jour de la base de données
     * 
     * @param db Instance de BDD
     * @param oldVersion Ancienne version
     * @param newVersion Nouvelle version
     */
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
    
}
