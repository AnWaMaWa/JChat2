package couchdb;

import MessageObserver.Message;
import org.lightcouch.CouchDbClient;
import org.lightcouch.CouchDbException;

import java.io.IOException;
import java.util.UUID;

/**
 * Handels sending messages to couchdb via an update handler.
 */
public class QuerySender implements ISendMessage, ISendQuery {

    CouchDbClient couchDbclient;
    private final String MESSAGE_UPDATE_HANDLER = "jchat/addMessage";
    private final String MESSAGE_QUERY_FIELD = "message";
    private final String TO_QUERY_FIELD = "to";

    private CouchDbClient getCouchDbclient() {
        return couchDbclient;
    }

    private void setCouchDbclient(CouchDbClient couchDbclient) {
        this.couchDbclient = couchDbclient;
    }

    public QuerySender(CouchDbClient cdb){
        setCouchDbclient(cdb);
    }

    private String buildMessageQuery(String messageWithReciever){
        Message message = Message.FromMessageAndReceiverFactory(messageWithReciever);
        if(message.getReceiver()!=null)
            return buildQueryField(MESSAGE_QUERY_FIELD,message.getBody())+"&"+buildQueryField(TO_QUERY_FIELD,getCouchDbclient().getGson().toJson(message.getReceiver()));
        else
            return buildQueryField(MESSAGE_QUERY_FIELD,message.getBody());
    }

    private String buildQueryField(String field, String value){
        return field + "=" +value;
    }

    @Override
    public void sendMessage(String messageWithReceiver) {
        String query = buildMessageQuery(messageWithReceiver);
        sendQuery(MESSAGE_UPDATE_HANDLER, UUID.randomUUID().toString(),query);
    }

    @Override
    public void sendQuery(String handler, String id, String query) {
        try {
            getCouchDbclient().invokeUpdateHandler(handler, id, query);
        }catch(CouchDbException ex){
            System.out.println(ex.getMessage());
        }
    }
}
