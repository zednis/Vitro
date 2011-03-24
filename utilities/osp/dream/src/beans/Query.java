package beans;

import java.sql.ResultSet;
import java.sql.Statement;

/*
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
*/
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.JspException;

import edu.cornell.mannlib.vitro.webapp.beans.Query;

public class Query {
   public static final String QUERY_PREFIX = "query-";
   private static final int UNASSIGNED = -1;

   private final Statement statement;
   private final ResultSet result;
   private final int updateCount;

   public Query( Statement statement, ResultSet result ) {
      this.statement = statement;
      this.result = result;
      this.updateCount = UNASSIGNED;
   }

   public Query( Statement statement, int updateCount ) {
      this.statement = statement;
      this.updateCount = updateCount;
      this.result = null;
   }

   public Statement getStatement() { return statement; }
   public ResultSet getResult()    { return result; }
   public int getUpdateCount()     { return updateCount; }

   public static void save( Query query, PageContext pageContext,
                            String name, String scope ) {
      pageContext.setAttribute( QUERY_PREFIX + name, query,
                                      getConstantForScope( scope ));
   }

   public static ResultSet getResult( PageContext pageContext,
                           String name ) throws JspException {
      Query query = findQuery( pageContext, name );
      return query.getResult();
   }

   public static int getUpdateCount( PageContext pageContext,
                                     String name ) throws JspException {
      Query query = findQuery( pageContext, name );
      return query.getUpdateCount();
   }

   public static Query findQuery( PageContext pageContext,
                                  String name ) throws JspException {
      Query query = (Query)pageContext.findAttribute( QUERY_PREFIX + name );

      if( query == null ) { // session invalidated?
         throw new JspException( "Query " + name + " not found." +
                                 " Please retry the query" );
      }
      return query;
   }

   private static int getConstantForScope( String scope ) {
      int constant = PageContext.PAGE_SCOPE;

      if( scope.equalsIgnoreCase( "page" ) )
         constant = PageContext.PAGE_SCOPE;
      else if ( scope.equalsIgnoreCase( "request" ) )
         constant = PageContext.REQUEST_SCOPE;
      else if ( scope.equalsIgnoreCase( "session" ) )
         constant = PageContext.SESSION_SCOPE;
      else if ( scope.equalsIgnoreCase( "application" ) )
         constant = PageContext.APPLICATION_SCOPE;

      return constant;
   }
}
