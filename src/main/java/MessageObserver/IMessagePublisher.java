package MessageObserver;

/**
 * Created by awaigand on 09.04.2015.
 */
public interface IMessagePublisher {

    public void subscribe(ISubscribe subscriber);
    public void unsubscribe(ISubscribe subscriber);
    public void publish(String s);
}
