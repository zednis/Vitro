package edu.cornell.mannlib.ingest.kwinter;


import java.net.*;
import java.sql.*;
import java.io.*;
import java.util.*;
import com.novell.ldap.*;

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

public class LdapNetidLookup {





    /**

       from RFC2254:
       String Search Filter Definition

       The string representation of an LDAP search filter is defined by the
       following grammar, following the ABNF notation defined in [5].  The
       filter format uses a prefix notation.

       filter     = "(" filtercomp ")"
       filtercomp = and / or / not / item
       and        = "&" filterlist
       or         = "|" filterlist
       not        = "!" filter
       filterlist = 1*filter
       item       = simple / present / substring / extensible
       simple     = attr filtertype value
       filtertype = equal / approx / greater / less
       equal      = "="
       approx     = "~="
       greater    = ">="
       less       = "<="
       extensible = attr [":dn"] [":" matchingrule] ":=" value
       / [":dn"] ":" matchingrule ":=" value
       present    = attr "=*"
       substring  = attr "=" [initial] any [final]
       initial    = value
       any        = "*" *(value "*")
       final      = value
       attr       = AttributeDescription from Section 4.1.5 of [1]
       matchingrule = MatchingRuleId from Section 4.1.9 of [1]
       value      = AttributeValue from Section 4.1.6 of [1]

       The attr, matchingrule, and value constructs are as described in the
       corresponding section of [1] given above.

       If a value should contain any of the following characters

       Character       ASCII value
       ---------------------------
       *               0x2a
       (               0x28
       )               0x29
       \               0x5c
       NUL             0x00

       the character must be encoded as the backslash '\' character (ASCII
       0x5c) followed by the two hexadecimal digits representing the ASCII
       value of the encoded character. The case of the two hexadecimal
       digits is not significant.

       This simple escaping mechanism eliminates filter-parsing ambiguities
       and allows any filter that can be represented in LDAP to be
       represented as a NUL-terminated string. Other characters besides the
       ones listed above may be escaped using this mechanism, for example,
       non-printing characters.

       For example, the filter checking whether the "cn" attribute contained
       a value with the character "*" anywhere in it would be represented as
       "(cn=*\2a*)".

       Note that although both the substring and present productions in the
       grammar above can produce the "attr=*" construct, this construct is
       used only to denote a presence filter.

       5. Examples

       This section gives a few examples of search filters written using
       this notation.

       (cn=Babs Jensen)
       (!(cn=Tim Howes))
       (&(objectClass=Person)(|(sn=Jensen)(cn=Babs J*)))
       (o=univ*of*mich*)

       example of a ldap searchfilter for directory.cornell.edu:
       (&(sn=*Caruso)(|(&(givenname=*brian*)(cornelledumiddlename=d*))(givenname=brian)))

       These are all of the attribute names that directory.cornell.edu knows about:
       cornelleduunivtitle1
       homePhone
       homePostalAddress
       displayName
       cn
       cornelleduregtemp2
       cornelleduregtemp1
       cornelledudeptname2
       cornelledumiddlename
       givenName
       sn
       cornelledudeptname1
       cornelledunetid
       edupersonprimaryaffiliation
       cornelledutype
       cornelleduunivtitle2
       edupersonnickname
       labeledUri
       description
       cornelledulocaladdress
       cornelledulocalphone
       facsimileTelephoneNumber
       cornelleduwrkngtitle2
       cornelleduwrkngtitle1
       pager
       mobile
       cornelleducampusphone
       cornelleducampusaddress
       cornelledudefaultpo
       mail
       mailRoutingAddress
       objectClass
       o
       c
       uid
       eduPersonOrgDN
       eduPersonPrincipalName


    */


    static final int DEPT_HRCODE_EXTERNAL_ID_TYPE=202;
    static final boolean NOISY=true;
    static ArrayList keywordList=null, collabList=null;
    static TreeMap deptTreeMap=null;

    static int flagUpdateCount=0;

    public static void main (String args[]) {
        try {
            System.out.println( lookupByNedit("kwg8"));
        } catch (LDAPException ex){
            System.out.println("LDAP exception: " + ex);
        }
    }

    public static String lookupByNedit(String netid)throws LDAPException {
        String out= "notFound";
        netid = cleanInput(netid);
        System.out.println("searcing for netid  '" + netid + "'");

        String ldapQuery = "(uid=" + netid + ")";
        LDAPSearchResults lsr = searchLdap(ldapQuery);
        out = ldapResult2String(lsr, "", "");
        

        return out;
    }



//  private static String makeLdapSearchFilter(String fullname){
//      int comma = fullname.indexOf(','),space1=-1,space2=-1;
//      if(comma<0){
//          System.out.print("name lacks a comma: " + fullname);
//          return null;
//      }
//      StringBuffer filter=new StringBuffer("(&(!(type=student*))"); //no students from ldap
//      String cn=null, strictGivenNames=null, looseGivenNames=null;
//      String first=null,middle=null,last=null;
//      last=fullname.substring(0,comma).trim();
//      space1=fullname.indexOf(' ',comma+1);
//      space2=fullname.indexOf(' ',space1+1);
//      if(space2 < 0){ //nothing after first name
//          first=fullname.substring(space1).trim();
//      }else{ //there is a middle name there
//          first=fullname.substring(space1,space2).trim();
//          middle=fullname.substring(space2+1).trim();
//      }
//
//      if(first!=null && first.indexOf('(')>0)
//          first=first.replace('(',' ');
//      if(first!=null && first.indexOf(')')>0)
//          first=first.replace(')',' ');
//
//      if(middle!=null && middle.indexOf('(')>0)
//          middle=middle.replace('(',' ');
//      if(middle!=null && middle.indexOf(')')>0)
//          middle=middle.replace(')',' ');
//
//      if(first!=null) //check for initials
//          if(first.indexOf('.')>0)
//              first=first.replace('.','*');
//          else
//              first=first+"*";
//
//      if(middle!=null) //check for initials
//          if( middle.indexOf('.')>0)
//              middle=middle.replace('.','*');
//          else
//              middle=middle+"*";
//
//      cn="(cn="; //put together common name query
//      if(first!=null){
//          if(middle!=null)
//              cn=cn+first+middle;
//          else
//              cn=cn+first;
//      }
//      cn=cn+last+")";
//      filter.append(cn);
//
//      filter.append(")");
//      return filter.toString();
//  }


    public static LDAPSearchResults searchLdap(String searchFilter)throws LDAPException{

        int ldapPort = LDAPConnection.DEFAULT_PORT;
        int searchScope = LDAPConnection.SCOPE_SUB;
        int ldapVersion  = LDAPConnection.LDAP_V3;
        String ldapHost = "directory.cornell.edu";
        String loginDN  = ""; //no login id needed
        String password = "";// no password needed

        String searchBase = "o=Cornell University, c=US";
        String attributes[]={LDAPConnection.ALL_USER_ATTRS,"cn"};

        LDAPConnection lc = new LDAPConnection();
        LDAPSearchResults thisResult = null;
        try {
            lc.connect( ldapHost, ldapPort );

            LDAPConstraints constraints = new LDAPConstraints(0,true,null,0);
            lc.bind( ldapVersion, loginDN, password, constraints );

            thisResult = lc.search(  searchBase, searchScope, searchFilter, attributes, false);
        } catch( LDAPException e ) {
            System.out.println( "error: " + e );
            String serverError = null;
            if( (serverError = e.getLDAPErrorMessage()) != null)
                System.out.println("Server: " + serverError);
            return null;
        }
        return thisResult;
    }

    /**
       tab delimited output string fomrat:
       name netId   deptHRcode  type    moniker keywords    URL anchor
    */
    private static String ldapResult2String(LDAPSearchResults res, String orgName,String ldapFilter){
        /*the strings are ldap attribute names for tab field i*/
        String map[][]= {
            {"cn","displayName"}, //we'll use the original vivo name // KARL: USE THIS NAME!
            {"mail","uid"},
            {"cornelledudeptname1","cornelledudeptname2"},
            {"cornelledutype","edupersonprimaryaffiliation"},
            {"cornelleduwrkngtitle1","cornelleduwrkngtitle2"},
            {},
            {"labeledUri"},
            {"description"},
            {"cornelledudeptname2"}};
        StringBuffer output=new StringBuffer("");
        output.append(orgName).append("\t"); //just stick the original name on the front.
        while(res.hasMoreElements()){
            LDAPEntry entry=(LDAPEntry)res.nextElement();
            //for tab field i look in map[i] for ldap attribute names, output first non-null value
            for(int iField=0;iField<map.length;iField++){

                for(int iName=0;iName< map[iField].length; iName++){
                    LDAPAttribute lAtt=entry.getAttribute(map[iField][iName]);

                    if(lAtt!=null){
                        String value=lAtt.getStringValue();
                        if(value!=null && value.length()>0 ){
                            output.append(value);
                            break;
                        }
                    }
                }
                output.append("\t");
            }
            output.append(ldapFilter);
            if(res.hasMoreElements()){
                output.append("\n").append(orgName).append("\t");
            }
        }
        return output.toString();
    }

    private static String cleanInput( String input_str ) {
        return escapeApostrophes(input_str.trim().replaceAll("\"",""));
    }
//  private static String stripQuotes( String termStr ) {
//      int characterPositrunsion= -1;
//      while ((characterPosition=termStr.indexOf(34,characterPosition+1))==0) {
//          termStr = termStr.substring(characterPosition+1);
//      }
//      return termStr;
//  }

    private static String escapeApostrophes( String termStr ) {
        int characterPosition= -1;
        if (termStr==null || termStr.equals("")) {
            return termStr;
        }
        while ( ( characterPosition = termStr.indexOf( 32, characterPosition+1 ) ) == 0 ) {
            termStr = termStr.substring(characterPosition+1);
        }
        characterPosition=-1;
        while ( ( characterPosition = termStr.indexOf( 39, characterPosition+1 ) ) >= 0 ) {
            if ( characterPosition == 0 ) // just drop it
                termStr = termStr.substring( characterPosition+1 );
            else if ( termStr.charAt(characterPosition-1) != 92 ) // already escaped
                termStr = termStr.substring(0,characterPosition) + "\\" + termStr.substring(characterPosition);
            ++characterPosition;
        }
        return termStr;
    }


}
