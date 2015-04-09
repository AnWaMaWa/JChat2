package couchdb;

import MessageObserver.Message;
import org.lightcouch.CouchDbClient;

import java.util.UUID;

/**
 * Handels sending messages to couchdb via an update handler.
 */
public class MessageSender implements ISendMessage {

    CouchDbClient couchDbclient;
    private final String messageUpdateHandler = "jchat/addMessage";

    private CouchDbClient getCouchDbclient() {
        return couchDbclient;
    }

    private void setCouchDbclient(CouchDbClient couchDbclient) {
        this.couchDbclient = couchDbclient;
    }

    public MessageSender(CouchDbClient cdb){
        setCouchDbclient(cdb);
    }

    @Override
    public void sendMessage(String s) {
        getCouchDbclient().invokeUpdateHandler(messageUpdateHandler, UUID.randomUUID().toString(), "message="+s);
    }

}
