package ChatCommands;

import couchdb.ISendQuery;

import java.util.HashMap;

/**
 * Created by awaigand on 10.04.2015.
 */
public class CommandList {

    /**
     * Map of commands the user must type in chat to the commands which will be run.
     * This uses the GOF Command Pattern.
     */
    HashMap<String, ICommand> commandHashMap = new HashMap<String, ICommand>();

    /**
     * All Commands should be created in this constructor.
     * @param sendQuery Given to Commands which are based on AbstractCouchCommand so they can communicate with CouchDB
     * @param username Username of the current user. Used to find user specific documents in CouchDB.
     */
    public CommandList(ISendQuery sendQuery, String username) {
        JoinGroupCommand jgc = new JoinGroupCommand(sendQuery, username);

        commandHashMap.put(jgc.getCommand(), jgc);
    }

    /**
     * Returns the first word of the command, usually someting like \COMMAND
     * If the command is only one word, the entire command is returned. This is usually the case if
     * the command does not need any parameters.
     * @param command
     * @return
     */
    private String getCommand(String command) {
        int index = command.indexOf(' ');
        if (index > -1)
            return command.substring(0, command.indexOf(' '));
        else
            return command;
    }

    /**
     * Returns the command args if there are some. The command args are everything after the first word of
     * the command. E.g. in "\JoinGroup #Verteiler" "#Verteiler" would be the command arg.
     * @param command
     * @return
     */
    private String getCommandArgs(String command) {
        int index = command.indexOf(' ');
        if (index > -1)
            return command.substring(command.indexOf(' ') + 1);
        else
            return "";
    }

    /**
     * Runs the given command if it can be found in the commandHashMap.
     * If not, the Command is unknown.
     * @param completeCommand
     * @return
     */
    public String runCommand(String completeCommand) {
        String command = getCommand(completeCommand);
        if (commandHashMap.containsKey(command)) {
            return commandHashMap.get(command).run(getCommandArgs(completeCommand));
        } else {
            return "Unknown command " + completeCommand;
        }
    }

}
