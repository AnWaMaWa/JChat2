package couchdb;

import MessageObserver.Message;

import java.util.*;

/**
 * Created by awaigand on 10.04.2015.
 */
public class MessageFilter implements IExtendReceiverFilter, IFilterMessage, IReplaceFilter{

    HashSet<String> filter = new HashSet<String>();
    String username;

    public MessageFilter(String username){
        this.username = username;
        addAcceptedReceiver(username);
    }

    @Override
    public void addAcceptedReceiver(String receiverName) {
        filter.add(receiverName);
    }

    @Override
    public void removeAcceptedReceiver(String receiverName) {
        filter.remove(receiverName);
    }

    @Override
    public boolean checkIfMessageIsForUser(Message m) {
        return username.equals(m.getUser()) || filter.contains(m.getReceiver());
    }

    @Override
    public void replaceFilter(String[] filters) {
        filter =  new HashSet<String>(Arrays.asList(filters));
        addAcceptedReceiver(username);
    }
}
