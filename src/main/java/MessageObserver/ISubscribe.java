package MessageObserver;

/**
 * Based on GOF Observer Pattern
 * Created by awaigand on 09.04.2015.
 */
public interface ISubscribe {
    public void notify(Message m);

    public void notify(String m);
}
