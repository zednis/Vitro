<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%  if (securityLevel >= loginHandler.CURATOR) { %>

        <c:set var="verbosePropertyListing" value="${verbosePropertyListing == true ? true : false}" />
        <div class="pageBodyGroup" id="sessionPreferences">
            <form action="${Controllers.SITE_ADMIN}" method="get">
                <input type="hidden" name="verbose" value="${!verbosePropertyListing}" />
                <span>Verbose property display for this session: </span>
                <input type="submit" value="${verbosePropertyListing == true ? 'Off' : 'On'}" />
            </form>   
        </div>             
<%  } %>      
