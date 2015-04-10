package couchdb;

import MessageObserver.Message;
import org.lightcouch.CouchDbClient;

import java.util.UUID;

/**
 * Handels sending messages to couchdb via an update handler.
 */
public class MessageSender implements ISendMessage {

    CouchDbClient couchDbclient;
    private final String MESSAGE_UPDATE_HANDLER = "jchat/addMessage";
    private final String JOIN_GROUP_UPDATE_HANDLER = "jchat/joinGroup";
    private final String MESSAGE_QUERY_FIELD = "message";
    private final String TO_QUERY_FIELD = "to";

    private CouchDbClient getCouchDbclient() {
        return couchDbclient;
    }

    private void setCouchDbclient(CouchDbClient couchDbclient) {
        this.couchDbclient = couchDbclient;
    }

    public MessageSender(CouchDbClient cdb){
        setCouchDbclient(cdb);
    }

    private String buildQuery(String messageWithReciever){
        Message message = Message.FromMessageAndReceiverFactory(messageWithReciever);
        if(message.getReceiver()!=null)
            return buildQueryField(MESSAGE_QUERY_FIELD,message.getBody())+"&"+buildQueryField(TO_QUERY_FIELD,message.getReceiver());
        else
            return buildQueryField(MESSAGE_QUERY_FIELD,message.getBody());
    }

    private String buildQueryField(String field, String value){
        return field + "=" +value;
    }

    @Override
    public void sendMessage(String messageWithReceiver) {
        if(messageWithReceiver.startsWith("\\"))
            executeCommand(messageWithReceiver);
        String query = buildQuery(messageWithReceiver);
        getCouchDbclient().invokeUpdateHandler(MESSAGE_UPDATE_HANDLER, UUID.randomUUID().toString(), query);
    }

    private void executeCommand(String messageWithReceiver) {
        getCouchDbclient().invokeUpdateHandler(JOIN_GROUP_UPDATE_HANDLER, "filter-sabrina","filter=Vertrieb");
    }

}
