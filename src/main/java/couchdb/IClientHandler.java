package couchdb;

import org.lightcouch.CouchDbClient;

/**
 * Created by awaigand on 21.04.2015.
 */
public interface IClientHandler {
    public void replaceCouchDbClient();
    public CouchDbClient getCouchDbClient();
}
