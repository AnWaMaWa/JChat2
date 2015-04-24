package ChatCommands;

import couchdb.ISendQuery;

/**
 * Created by awaigand on 10.04.2015.
 */
public abstract class AbstractCouchCommand implements ICommand {

    protected ISendQuery querySender;

    protected ISendQuery getQuerySender() {
        return querySender;
    }

    protected String buildQueryField(String field, String value) {
        return field + "=" + value;
    }

    protected void setQuerySender(ISendQuery querySender) {
        this.querySender = querySender;
    }

    public AbstractCouchCommand(ISendQuery querySender) {
        setQuerySender(querySender);
    }
}
