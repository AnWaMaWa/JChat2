package ChatCommands;

/**
 * Interface based on the Command GOF Pattern.
 * Created by awaigand on 10.04.2015.
 */
public interface ICommand {
    public String run(String commandArgs);

    public String getCommand();
}
