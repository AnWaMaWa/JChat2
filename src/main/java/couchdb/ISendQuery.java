package couchdb;

/**
 * Used by classes which can send any type of query to the Database
 * Created by awaigand on 10.04.2015.
 */
public interface ISendQuery {
    /**
     * Sends queries to the database
     * @param handler queryHandler
     * @param id Id for document to be created
     * @param query Query parameters
     */
    public void sendQuery(String handler, String id, String query);
}
