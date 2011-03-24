
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.Format;

/********************* Vitro specific database related *****************/

public class VitroDbUtils{
    /**
    This is useful for making a method like
    <code>
     boolean isDuplicateDeptId(String entityid, Connection vivoCon) throws SQLException{
        String query="SELECT ID FROM externalids WHERE ENTITYID=" + entityid + " AND " +
            "EXTERNALIDTYPE=" + vivoDeptLinkId ;
        return hasRows(vivoCon,  query );
    </code>

    @returns true if query result has some rows
    */
    public static final boolean hasRows(Connection con, String query)
        throws SQLException{
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        boolean hadRows = rs.next();
        close(rs);close(stmt);
        return hadRows;
    }

    /** close ResultSet with without exception */
    private static final void close(ResultSet in){try{ if(in!=null) in.close(); }catch (Throwable ex){}}
    /** close Statement with without exception */
    private static final void close(Statement in){try{ if(in!=null) in.close(); }catch (Throwable ex){}}

    /**
     * Prompts the user on System.out, reads in a line and returns it.
     */
    public static final String readEntry(String prompt)    {
        try {
        StringBuffer buffer = new StringBuffer();
        System.out.print(prompt);
        System.out.flush();
        int c = System.in.read();
        while (c != '\n' && c != -1) {
            buffer.append((char)c);
            c = System.in.read();
        }
        return buffer.toString().trim();
        }catch(IOException e){
            return "";
        }
    }

    /**
     * Returns an array where array[i] = row[i].getString(columnNum).
     */
    public static final String[] getColumnSlice(ResultSet rs, int columnNum)
            throws SQLException {
        rs.last();
        int count = rs.getRow();
        String retv[] = new String[count];
        rs.beforeFirst();
        while (rs.next()) {
            retv[rs.getRow() - 1] = rs.getString(columnNum);
        }
        return retv;
    }

    /**
       It is likely that this isn't what you want, take a look at quoteChar,
       quoteDate and quoteNumber.

       Replace any substrings that need to be escaped when they go to into an sql
       query with the necessary escape codes. Right now this only escapes
       single quotes to double single-quotes for MySQL.
       @returns null if strIn is null
    */
    public static final String escapeForSql(String strIn){
        if( strIn != null ){
            String res = strIn.replaceAll("'", "''");
            //backslash is escaped for java string, then for regexp, wow.
            return res.replaceAll("\\\\", "\\\\\\\\"); //<-- repalces a backslash with two bkslashes
        } else
            return null;
    }

    public static final String dateFormat = "yyyy-MM-dd";
    public static final String quoteDate(Date in){
        if( in == null ) return "NULL";
        Format formatter= new SimpleDateFormat (dateFormat);
        String s = formatter.format(in);
        return quoteDate(s);
    }

    /*
     * These are useful if you want to quote Strings going into an sql query.
     */
    public static final String quoteChar(String in){ return (in != null?"'"+escapeForSql(in)+"'":"NULL"); }
    public static final String quoteDate(String in){return (in != null?"'"+in+"'":"NULL"); }
    public static final String quoteNumeric(String in){ return (in != null?in:"NULL"); }

    /*
     * These methods are useful if you want to get a column value and immediatly
     * use it in another sql query.
     */
    public static final String quoteChar(ResultSet rs, String columnName)
        throws SQLException{ return quoteChar(rs.getString(columnName)); }
    public static final String quoteDate(ResultSet rs, String columnName)
        throws SQLException{return quoteDate(rs.getString(columnName));}
    public static final String quoteNumeric(ResultSet rs, String columnName)
        throws SQLException{ return quoteNumeric(rs.getString(columnName)); }

}
