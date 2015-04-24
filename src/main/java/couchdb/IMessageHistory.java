package couchdb;

import MessageObserver.Message;

import java.util.List;

/**
 * Created by awaigand on 10.04.2015.
 */
public interface IMessageHistory {
    /**
     * Gets limit number of messages at most, starting from offset message in the past, backwards
     *
     * @param offset
     * @param limit
     */
    public List<Message> getMessageHistory(int offset, int limit);
}
