package net.badacadabra.tweesty;

/**
 * Classe définissant l'entité Tweet
 * 
 * @author Macky Dieng
 * @author Steeve Tsamba
 * @author Baptiste Vannesson
 */
public class Tweet {
        
    /** Nom du twitto */
    private String user;
        
    /** Contenu du tweet */
    private String message;
        
    /**
     * Accesseur pour le nom du twitto
     * 
     * @return Nom du twitto
     */
    public String getUser() {
        return user;
    }
    
    /**
     * Mutateur pour le nom du twitto
     * 
     * @param user Nouveau nom du twitto
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * Accesseur pour le contenu du tweet
     * 
     * @return Contenu du tweet
     */
    public String getMessage() {
            return message;
    }
    
    /**
     * Mutateur pour le contenu du tweet
     * 
     * @param message Nouveau contenu du tweet
     */
    public void setMessage(String message) {
            this.message = message;
    }
    
    /**
     * Rendu du tweet pour affichage
     * 
     * @return Contenu du tweet
     */
    public String toString() {
        return message;
    }

}
