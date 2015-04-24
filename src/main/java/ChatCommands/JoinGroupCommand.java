package ChatCommands;

import couchdb.ISendQuery;

/**
 * Created by awaigand on 10.04.2015.
 */
public class JoinGroupCommand extends AbstractCouchCommand {

    //Identifies the CouchDB joinGroup UpdateHandler which is used for joining groups.
    private final String JOIN_GROUP_UPDATE_HANDLER = "jchat/joinGroup";
    //This is the command under which the current command can be called
    private final String COMMAND_STRING = "\\joinGroup";
    //All group filter documents in the CouchDB are prefixed with this.
    private final String FILTER_DOCUMENT_ID_PREFIX = "filter-";
    //Queryfield for updating group filter document.
    private final String FILTER_QUERY_PARAM = "filter";
    private String username;

    public JoinGroupCommand(ISendQuery sendQuery, String username) {
        super(sendQuery);
        this.username = username;
    }

    //All Group Filter Document _ids in the couchdb are filter-USERNAME, e.g. filter-user1.
    private String getFilterDocumentId() {
        return FILTER_DOCUMENT_ID_PREFIX + username;
    }

    //Checks whether or not the given CommandArgs contain only group, starting with #.
    private boolean checkCommandArgs(String commandArgs) {
        return !commandArgs.contains(" ") && commandArgs.startsWith("#");
    }

    @Override
    public String run(String commandArgs) {
        if (checkCommandArgs(commandArgs)) {
            getQuerySender().sendQuery(JOIN_GROUP_UPDATE_HANDLER, getFilterDocumentId(), buildQueryField(FILTER_QUERY_PARAM, commandArgs));
            return "Joined group " + commandArgs;
        } else {
            return "The group you want to join can not contain spaces and has to start with a #! Your input: " + commandArgs;
        }
    }

    /**
     * @return Given way to call the command in chat, i.e. \COMMAND
     */
    @Override
    public String getCommand() {
        return COMMAND_STRING;
    }
}
