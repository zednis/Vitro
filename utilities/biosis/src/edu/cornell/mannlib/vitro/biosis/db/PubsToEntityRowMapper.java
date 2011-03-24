package edu.cornell.mannlib.vitro.biosis.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.joda.time.DateTime;

import edu.cornell.mannlib.vitro.biosis.beans.ArticleEntity;
import edu.cornell.mannlib.vitro.dao.db.VitroRowMapper;

/**
 * This is a class that takes a row from the pubs table and
 * creates an in memoery entity bean.
 * 
 * @author bdc34
 *
 */
public class PubsToEntityRowMapper implements VitroRowMapper {

    /** Note: these are columns from the pubs table */
    public String getColumns() { 
        return "id, updateCode, title, authors, source, abstract, " +
                "fullTextLink, fullTextSource ";
    }
    
    public String getFrom() { return "pubs "; }

    /**  Returns an Entity object.     */
    public Object mapRow(ResultSet rs, int arg1) throws SQLException {
        ArticleEntity ent = new ArticleEntity();
        // name, vclass, moniker, timekkey, sunset, blurb, citation, description,
        ent.pubId = rs.getInt("id");        
        ent.setName(rs.getString("title"));
        ent.setDescription(rs.getString("abstract"));
        
        //blurb gest authors and maybe source
        ent.setBlurb( rs.getString("authors"));        
        String  source = rs.getString("source");
        if( source != null && source.equals(""));
            ent.setBlurb( ent.getBlurb() + " | " + source);              
        
        ent.setTimekey(rs.getDate("updateCode"));        
        
        ent.fullTextLinks = rs.getString("fullTextLink");
        ent.fullTextSources = rs.getString("fullTextSource");
                       
        return ent;
    }


}
