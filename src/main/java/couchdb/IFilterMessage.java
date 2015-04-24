package couchdb;

import MessageObserver.Message;

/**
 * Used by classes which can check whether or not a message should be displayed to the user
 * Created by awaigand on 10.04.2015.
 */
public interface IFilterMessage {
    /**
     * Returns true if the message should be displayed to the user and false if not
     * @param m Message to be checked
     * @return boolean which represents if the message should be displayed.
     */
    public boolean checkIfMessageIsForUser(Message m);
}
