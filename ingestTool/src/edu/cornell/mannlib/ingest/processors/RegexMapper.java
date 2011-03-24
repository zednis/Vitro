package edu.cornell.mannlib.ingest.processors;

/*
Copyright © 2003-2008 by the Cornell University and the Cornell
Research Foundation, Inc.  All Rights Reserved.

Permission to use, copy, modify and distribute any part of VITRO
("WORK") and its associated copyrights for educational, research and
non-profit purposes, without fee, and without a written agreement is
hereby granted, provided that the above copyright notice, this
paragraph and the following three paragraphs appear in all copies.

Those desiring to incorporate WORK into commercial products or use
WORK and its associated copyrights for commercial purposes should
contact the Cornell Center for Technology Enterprise and
Commercialization at 395 Pine Tree Road, Suite 310, Ithaca, NY 14850;
email:cctecconnect@cornell.edu; Tel: 607-254-4698; FAX: 607-254-5454
for a commercial license.

IN NO EVENT SHALL THE CORNELL RESEARCH FOUNDATION, INC. AND CORNELL
UNIVERSITY BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL,
INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS, ARISING
OUT OF THE USE OF WORK AND ITS ASSOCIATED COPYRIGHTS, EVEN IF THE
CORNELL RESEARCH FOUNDATION, INC. AND CORNELL UNIVERSITY MAY HAVE BEEN
ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.  */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.ingest.interfaces.StringProcessor;

public class RegexMapper implements StringProcessor{
    //use the keys list to order the regex checks
    List<String> keys;
    Map<String,Pattern>patternMap;
    Map<String,String>valueMap;

    private static final Log log = LogFactory.getLog(RegexMapper.class.getName());

    public RegexMapper(){
        keys = new ArrayList<String>();
        patternMap = new HashMap<String,Pattern>();
        valueMap = new HashMap<String,String>();
    }

    public void addRegex(String regex, String value){
        keys.add(regex);
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        patternMap.put(regex, pattern );
        valueMap.put(regex,   value );
    }

    public String process(String in) {
        if( in == null ) return in;
        for( String regex : keys ){
            Pattern p = patternMap.get(regex);
            Matcher m = p.matcher( in );
            if( m.matches() ){
                String out = valueMap.get(regex);
                if( log.isDebugEnabled() )
                    log.debug("successfully mapped " +in + " to " + out);
                return out;
            }
        }
        if( log.isDebugEnabled() )
            log.debug("unable to map " + in + " to any regex" );
        return in;
    }

}
