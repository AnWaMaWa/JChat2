package couchdb;

import MessageObserver.Message;

/**
 * Created by awaigand on 10.04.2015.
 */
public interface IFilterMessage {
    public boolean checkIfMessageIsForUser(Message m);
}
