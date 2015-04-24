package couchdb;

/**
 * Used by classes which can send raw user input to the database
 * Created by awaigand on 09.04.2015.
 */
public interface ISendMessage {
    /**
     * Creates a message document from raw user input and send it to the database
     * @param s Raw user input
     */
    public void sendMessage(String s);
}
