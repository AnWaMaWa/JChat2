package couchdb;

import org.lightcouch.CouchDbClient;

/**
 * Used by classes which can wrap or create
 * a CouchDBClient
 * Created by awaigand on 21.04.2015.
 */
public interface IClientHandler {
    /**
     * Signals that connection to the currently used CouchDB is no longer working
     * Classes which implement this interface should try other CouchDB servers once
     * this method is called.
     */
    public void replaceCouchDbClient();

    /**
     * Provides a Connected CouchDbClient
     * Should, if possible, only return CouchDbClients which work and block the call when
     * the CouchDbClient is currently being replaced.
     * @return Connected CouchDbClient
     */
    public CouchDbClient getCouchDbClient();
}
