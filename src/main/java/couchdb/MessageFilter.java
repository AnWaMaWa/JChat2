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
        addDefaultReceivers();
    }

    private void addDefaultReceivers(){
        addAcceptedReceiver("@"+username);
        addAcceptedReceiver("@global");
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
        if(m.getReceiver().length == 0)
            return true; //global
        for(String receiver : m.getReceiver()){
            if(filter.contains(receiver))
                return true;
        }
        return username.equals(m.getUser());
    }

    @Override
    public void replaceFilter(String[] filters) {
        filter =  new HashSet<String>(Arrays.asList(filters));
        addDefaultReceivers();
    }
}
