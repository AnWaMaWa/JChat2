package MessageObserver;

/**
 * Created by awaigand on 09.04.2015.
 */
public class Message implements IMessage {

    private String body;
    private String user;

    public Message(String body, String user){
        this.body = body;
        this.user = user;
    }

    @Override
    public String getBody() {
        return body;
    }

    @Override
    public String getUser() {
        return user;
    }
}
