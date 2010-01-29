package edu.cornell.mannlib.vitro.webapp.search.beans;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.flags.PortalFlag;
import edu.cornell.mannlib.vitro.webapp.search.SearchException;
import org.joda.time.DateTime;

public abstract class VitroQuery {
    /**
     * The parameter name for http requests.
     */
    public static final String QUERY_PARAMETER_NAME = "querytext";
    public static final String QUERY_PARAMETER_EARLIEST = "earliest";
    public static final String QUERY_PARAMETER_LATEST = "latest";
    public static final String QUERY_PARAMETER_IGNORE_TIMESTAMP= "ignore_timestamp";

    DateTime earliest;
    DateTime latest;
    Map parameters = null;
    PortalFlag portalState = null;

    /**
     * Make a VitroQuery with the request parameters and portalState
     * saves for when the query needs to be created.
     */
    public VitroQuery(VitroRequest request, PortalFlag portalState){
        this.portalState = portalState;
        parameters =request.getParameterMap();
        if( parameters == null )
            parameters = Collections.EMPTY_MAP;
    }

    /**
     * Gets the parameters that were passed into this query from the
     * HttpRequest so they can be displayed to the user on return to a
     * search form.
     *
     * @return
     */
    public Map getParameters(){
        return parameters;
    }

    public PortalFlag getPortalState(){
        return portalState;
    }

    public abstract String getTerms();

    /**
     * This is intended to return an specilized query object
     * for your implementation of the search.
     * @return
     */
    public abstract Object getQuery() throws SearchException;
}
