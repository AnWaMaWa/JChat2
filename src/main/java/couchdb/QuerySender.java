package couchdb;

import MessageObserver.Message;
import org.lightcouch.CouchDbClient;
import org.lightcouch.CouchDbException;

import java.util.UUID;

/**
 * Handels sending messages to couchdb via an update handler.
 * See the offical CouchDB Documentation for explanations about the CouchDB update handler.
  */
public class QuerySender implements ISendMessage, ISendQuery {

    private final String MESSAGE_UPDATE_HANDLER = "jchat/addMessage";
    private final String MESSAGE_QUERY_FIELD = "message";
    private final String TO_QUERY_FIELD = "to";
    private DBClientWrapper dbClientWrapper;

    private CouchDbClient getCouchDbclient() {
        return dbClientWrapper.getCouchDbClient();
    }


    public QuerySender(DBClientWrapper dbClientWrapper) {
        this.dbClientWrapper = dbClientWrapper;
    }

    /**
     * Builds GET Query Parameter like message=Hi&receiver=user1
     * @param messageJson A raw input message, as the user types it
     * @return A query parameter string usable for message update handler
     */
    private String buildMessageQuery(String messageJson) {
        Message message = Message.FromMessageAndReceiverFactory(messageJson);
        if (message.getReceiver() != null)
            return buildQueryField(MESSAGE_QUERY_FIELD, message.getBody()) + "&" + buildQueryField(TO_QUERY_FIELD, getCouchDbclient().getGson().toJson(message.getReceiver()));
        else
            return buildQueryField(MESSAGE_QUERY_FIELD, message.getBody());
    }

    /**
     * Builds a single query field value pair
     * @param field The field e.g. message
     * @param value The value e.g. hi!
     * @return A Query field value pair e.g. message=hi
     */
    private String buildQueryField(String field, String value) {
        return field + "=" + value;
    }

    /**
     * Sends a query to the message_update_handler.
     * This, in fact, creates a message document in couchdb which also sets the owner to the current user and
     * sets the creation date on the server side
     * Provides a UUID for the message document in couchdb
     * @param messageWithReceiver raw user input
     */
    @Override
    public void sendMessage(String messageWithReceiver) {
        String query = buildMessageQuery(messageWithReceiver);
        sendQuery(MESSAGE_UPDATE_HANDLER, UUID.randomUUID().toString(), query);
    }


    /**
     * Sends a query to an couchdb update handler couchDB.
     * It can be any updaate handler present in the JChat CouchDB Database.
     * @param handler The couchdb handler
     * @param id The document id
     * @param query the query string, can be null
     */
    @Override
    public void sendQuery(String handler, String id, String query) {
        try {
            getCouchDbclient().invokeUpdateHandler(handler, id, query);
        } catch (CouchDbException ex) {
            dbClientWrapper.replaceCouchDbClient();
            sendQuery(handler, id, query);
        }
    }
}
