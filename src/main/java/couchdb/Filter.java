package couchdb;

import org.lightcouch.Document;

/**
 * Object representation of a filter-user document in CouchDB.
 * Created by awaigand on 10.04.2015.
 */
public class Filter extends Document {
    private String owner;
    private String[] filter;

    public String[] getFilter() {
        return filter;
    }
}
