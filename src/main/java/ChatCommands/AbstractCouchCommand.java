package ChatCommands;

import couchdb.ISendQuery;

/**
 * This class is the base for all commands which need to communicate with CouchDB, for example to update
 * user configuration.
 * Commands can usually used in chat by typing \COMMAND
 * It uses the ISendQuery interface to communicate with CouchDB.
 * Created by awaigand on 10.04.2015.
 */
public abstract class AbstractCouchCommand implements ICommand {

    protected ISendQuery querySender;

    protected ISendQuery getQuerySender() {
        return querySender;
    }

    protected void setQuerySender(ISendQuery querySender) {
        this.querySender = querySender;
    }

    /**
     * Convenience Method for creating query parameter strings like "message=Hi!".
     * @param field Field, e.g message
     * @param value Value, e.g. Hi!
     * @return
     */
    protected String buildQueryField(String field, String value) {
        return field + "=" + value;
    }

    public AbstractCouchCommand(ISendQuery querySender) {
        setQuerySender(querySender);
    }
}
