package couchdb;

/**
 * Used by filter classes which can swap out all filters at once
 * Created by awaigand on 10.04.2015.
 */
public interface IReplaceFilter {
    /**
     * Swaps out all filters with the given array of filters.
     * @param filters Filters to be swapped in
     */
    public void replaceFilter(String[] filters);
}
