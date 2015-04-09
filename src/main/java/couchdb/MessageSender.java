package couchdb;

import org.lightcouch.CouchDbClient;

import java.util.UUID;

/**
 * Handels sending messages to couchdb via an update handler.
 */
public class MessageSender implements ISendMessage {

    CouchDbClient couchDbclient;
    private final String MESSAGE_UPDATE_HANDLER = "jchat/addMessage";
    private final String MESSAGE_QUERY_FIELD = "message=";

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
        getCouchDbclient().invokeUpdateHandler(MESSAGE_UPDATE_HANDLER, UUID.randomUUID().toString(), MESSAGE_QUERY_FIELD +s);
    }

}
