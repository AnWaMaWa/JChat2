package ChatCommands;

import couchdb.ISendQuery;
import org.lightcouch.CouchDbClient;

/**
 * Created by awaigand on 10.04.2015.
 */
public class JoinGroupCommand extends AbstractCouchCommand {

    private final String JOIN_GROUP_UPDATE_HANDLER = "jchat/joinGroup";
    private final String COMMAND_STRING = "\\joinGroup";
    private final String FILTER_DOCUMENT_ID_PREFIX = "filter-";
    private final String FILTER_QUERY_PARAM = "filter";
    private String username;

    public JoinGroupCommand(ISendQuery sendQuery, String username) {
        super(sendQuery);
        this.username = username;
    }

    private String getFilterDocumentId(){
        return FILTER_DOCUMENT_ID_PREFIX +username;
    }

    private boolean checkCommandArgs(String commandArgs){
        return !commandArgs.contains(" ");
    }

    @Override
    public String run(String commandArgs) {
         if(checkCommandArgs(commandArgs)) {
             getQuerySender().sendQuery(JOIN_GROUP_UPDATE_HANDLER,getFilterDocumentId(),buildQueryField(FILTER_QUERY_PARAM,commandArgs));
             return "Joined group " + commandArgs;
         }else{
             return "The group you want to join can not contain spaces! Your input: " + commandArgs;
         }
    }

    @Override
    public String getCommand() {
        return COMMAND_STRING;
    }
}
