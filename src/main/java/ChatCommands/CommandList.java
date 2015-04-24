package ChatCommands;

import couchdb.ISendQuery;

import java.util.HashMap;

/**
 * Created by awaigand on 10.04.2015.
 */
public class CommandList {

    HashMap<String, ICommand> commandHashMap = new HashMap<String, ICommand>();

    public CommandList(ISendQuery cdb, String username) {
        JoinGroupCommand jgc = new JoinGroupCommand(cdb, username);

        commandHashMap.put(jgc.getCommand(), jgc);
    }

    private String getCommand(String command) {
        int index = command.indexOf(' ');
        if (index > -1)
            return command.substring(0, command.indexOf(' '));
        else
            return command;
    }

    private String getCommandArgs(String command) {
        int index = command.indexOf(' ');
        if (index > -1)
            return command.substring(command.indexOf(' ') + 1);
        else
            return "";
    }

    public String runCommand(String completeCommand) {
        String command = getCommand(completeCommand);
        if (commandHashMap.containsKey(command)) {
            return commandHashMap.get(command).run(getCommandArgs(completeCommand));
        } else {
            return "Unknown command " + completeCommand;
        }
    }

}
