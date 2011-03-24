package edu.cornell.mannlib.vitro.biosis.beans;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.Period;

import edu.cornell.mannlib.vitro.beans.Individual;

public class ArticleEntity extends Individual {
    public String monikerOverride = "recent journal article";

    private boolean vclassSet = true;
    
//  id of journal article vclass, hardcoded, it will change and break.
    public final int articleVClassIdDefault= 318; 
    
    public String fullTextLinks;
    public String fullTextSources;
    public int pubId;    
    
    @Override
    public String getMoniker(){return monikerOverride; }
    
    @Override
    public int getVClassId(){
        if( vclassSet )
            return super.getVClassId();
        else
            return articleVClassIdDefault; 
    }
    
    @Override
    public void setVClassId(int i){        
        vclassSet = true;
        super.setVClassId(i);        
    }
    
    /** returns the day on which the article should be taken down */
    public Date calculateSunset( int daysArticleCanBeShown ){
        DateTime artPublished = null;        
        if( getTimekey() == null ) //if no publish date, use today
            artPublished = new DateTime();
        else
            artPublished = new DateTime( getTimekey());
                
        artPublished.plus( Period.days( daysArticleCanBeShown ) );        
        return artPublished.toDate();
    }
}
