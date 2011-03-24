package edu.cornell.mannlib.ingest.fieldHandlers;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactoryConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;

import edu.cornell.mannlib.ingest.interfaces.StringProcessor;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

/**
 * This will look up an Individual with the uri of the "node" and make
 * the given relation between the target and the individual with the 
 * uri.  
 * 
 * This is intended to be used with string processors to convert values from
 * xml into URIs.
 * 
 * @author bdc34
 *
 */
public class RelationResolverWithUri extends BaseXPath2Handler {

    private String propertyURI;
    private WebappDaoFactory wdf;

    public RelationResolverWithUri(String xpath2, String propertyUri, List<StringProcessor> strProcessors, WebappDaoFactory wdf) 
    throws XPathFactoryConfigurationException, XPathExpressionException{
        super(xpath2);
        addAllStrProcessors(strProcessors);
        this.propertyURI = propertyUri;                
        this.wdf = wdf;
    }
    
    @Override
    public void handleNode(String uriOfRelatedEnt, Element doc, Object targetEnt) {
        if( uriOfRelatedEnt == null || "".equals(uriOfRelatedEnt) ){
            log.debug("Did not create relation of type " + propertyURI + "  empty uri of related entity.");
            return;
        }
        Individual otherEnt = wdf.getIndividualDao().getIndividualByURI(uriOfRelatedEnt);
        if( otherEnt == null ){
            log.error("Did not create relation of type " + propertyURI + " could not find individual for " + uriOfRelatedEnt);
            return;
        }
        if( targetEnt == null )
            log.debug("targetEnt was null");

        if( !( targetEnt instanceof Individual )){
            log.error("targetEnt was not an Individual; it was of type " + targetEnt.getClass().getName());
        }
        Individual target = (Individual)targetEnt;
        
        ObjectPropertyStatement e2e = new ObjectPropertyStatementImpl();
        e2e.setPropertyURI(propertyURI);        
       
        e2e.setSubjectURI(target.getURI());
        e2e.setObjectURI(otherEnt.getURI());
        List <ObjectPropertyStatement> zz = target.getObjectPropertyStatements();
        if( zz == null ){
            zz = new ArrayList<ObjectPropertyStatement>();
            target.setObjectPropertyStatements(zz);
        }
        zz.add(e2e);
        if( log.isDebugEnabled())
            log.debug("added stmt: " + e2e);
    }

    private static final Log log = LogFactory.getLog(RelationResolverWithUri.class.getName());
}
