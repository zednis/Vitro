/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchindex.documentBuilding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ContextModelAccess;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchInputDocument;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;
import edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.ContextModelsUser;

/**
 * DocumentModifier that will run SPARQL queries for an
 * Individual and add all the columns from all the rows
 * in the solution set to the ALLTEXT field.
 *  
 * @author bdc34
 *
 */
public class ContextNodeFields implements DocumentModifier, ContextModelsUser{
    protected List<String> queries = new ArrayList<String>();
    protected boolean shutdown = false;    
    protected Log log = LogFactory.getLog(ContextNodeFields.class);   
    //Subclasses may want to utilize rdfService directly (for example, to execute queries that yielding multiple variables mapped to different fields)
    protected RDFService rdfService;
	
	@Override
	public void setContextModels(ContextModelAccess models) {
		this.rdfService = models.getRDFService();
	}

    /**
     * Construct this with a model to query when building search documents and
     * a list of the SPARQL queries to run.
     */
    protected ContextNodeFields(List<String> queries){   
        this.queries = queries;
    }        
    

    public StringBuffer getValues( Individual individual ){
        return executeQueryForValues( individual, queries );        
    }                
    
    @Override
    public void modifyDocument(Individual individual, SearchInputDocument doc) {        
        if( individual == null )
            return;
        
        log.debug( "processing context nodes for: " +  individual.getURI());
        log.debug( "queries are ");
        for(String q:queries) {
        	log.debug("Query: " + q);
        }
        /* get text from the context nodes and add the to ALLTEXT */        
        StringBuffer values = executeQueryForValues(individual, queries);        
        doc.addField(VitroSearchTermNames.ALLTEXT, values);
    }
    
    /**
     * this method gets values that will be added to ALLTEXT 
     * field of the search index Document for each individual.
     * 
     * @param individual
     * @return StringBuffer with text values to add to ALLTEXT field of the search index Document.
     */
    protected StringBuffer executeQueryForValues( Individual individual, Collection<String> queries){
    	  /* execute all the queries on the list and concat the values to add to all text */        
        StringBuffer allValues = new StringBuffer("");                

        for(String query : queries ){    
            StringBuffer valuesForQuery = new StringBuffer();
        	
            String subInUriQuery = 
        		query.replaceAll("\\?uri", "<" + individual.getURI() + "> ");
        	log.debug("Subbed in URI query: " + subInUriQuery);
            try{
            	
            	ResultSet results = RDFServiceUtils.sparqlSelectQuery(subInUriQuery, rdfService);               
            	while(results.hasNext()){                                                                               
                    valuesForQuery.append( 
                            getTextForRow( results.nextSolution(), true ) ) ; 
            	}
            	
            }catch(Throwable t){
                if( ! shutdown )                    
                    log.error("problem while running query '" + subInUriQuery + "'",t);                
            } 
            
            if(log.isDebugEnabled()){
                log.debug("query: '" + subInUriQuery+ "'");
                log.debug("text for query: '" + valuesForQuery.toString() + "'");
            }
            allValues.append(valuesForQuery);
        }
        return allValues;    
    }       
    
    protected String getTextForRow( QuerySolution row, boolean addSpace){
        if( row == null )
            return "";

        StringBuffer text = new StringBuffer();
        Iterator<String> iter =  row.varNames() ;
        while( iter.hasNext()){
            String name = iter.next();
            RDFNode node = row.get( name );
            if( node != null ){
            	String value = (node.isLiteral()) ? 
            			node.asLiteral().getString(): 
            			node.toString();
            	if (StringUtils.isNotBlank(value)) {
	            	if(addSpace) {
	            		text.append(" ").append( value );
	            	} else {
	            		text.append(value);
	            	}
            	}
            }else{
                log.debug(name + " is null");
            }                        
        }        
        return text.toString();
    }
    
    public void shutdown(){
        shutdown=true;  
    }


	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "[]";
	}

}
