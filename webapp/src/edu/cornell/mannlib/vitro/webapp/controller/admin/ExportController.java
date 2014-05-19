/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.admin;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.TemplateProcessingHelper.TemplateProcessingException;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;

/**
 * If invoked with no path info, show the request page.
 * 
 * If invoked with /request path, parse the parameters, and redirect to a
 * /download location, with an appropriate filename so the browser will store
 * the file with a sensible extension.
 * 
 * If invoked with a /download path, parse the parameters and stream the result.
 */
public class ExportController extends FreemarkerHttpServlet {
	private static final Log log = LogFactory.getLog(ExportController.class);

	private static final AuthorizationRequest REQUIRED_ACTIONS = SimplePermission.USE_ADVANCED_DATA_TOOLS_PAGES.ACTION
			.or(SimplePermission.EDIT_ONTOLOGY.ACTION);

	private static final String DOWNLOAD_PATH = "/download";
	private static final String REQUEST_PATH = "/request";

	/**
	 * For /request or /download paths, we want full control. Otherwise, go
	 * ahead and use the doResponse() method.
	 */
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		try {
			resp.setContentType("text/plain");
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(
					resp.getOutputStream(), "UTF-8"));

			if ("/sparql".equals(req.getPathInfo())) {
				writeNamedGraphsFromSelect(req, writer);
			} else {
				writeQuadsFromSQLConnection(req, writer);
			}
		} catch (Exception e) {
			log.error(e, e);
			resp.sendError(500);
		}

	}

	private void writeNamedGraphsFromSelect(HttpServletRequest req,
			PrintWriter writer) {
		writeCommentBlock("Write named graphs from select.", writer);

		RDFService rdfService = RDFServiceUtils.getRDFService(new VitroRequest(
				req));
		ResultSet resultSet = RDFServiceUtils.sparqlSelectQuery(
				"SELECT * WHERE { GRAPH ?g {?s ?p ?o}}", rdfService);
		dumpResultSetAsQuads(resultSet, writer);
	}

	private static final String PROPERTY_DB_URL = "VitroConnection.DataSource.url";
	private static final String PROPERTY_DB_USERNAME = "VitroConnection.DataSource.username";
	private static final String PROPERTY_DB_PASSWORD = "VitroConnection.DataSource.password";
	private static final String SQL_QUERY = ""
			+ "select \n"
			+ "   s.lex as sLex, s.lang as sLang, s.datatype as sDatatype, s.type as sType, \n"
			+ "   p.lex as pLex, p.lang as pLang, p.datatype as pDatatype, p.type as pType, \n"
			+ "   o.lex as oLex, o.lang as oLang, o.datatype as oDatatype, o.type as oType, \n"
			+ "   g.lex as gLex, g.lang as gLang, g.datatype as gDatatype, g.type as gType \n"
			+ "from \n" //
			+ "   Quads q \n" //
			+ "   inner join Nodes s on q.s = s.hash \n"
			+ "   inner join Nodes p on q.p = p.hash \n"
			+ "   inner join Nodes o on q.o = o.hash \n"
			+ "   inner join Nodes g on q.g = g.hash \n";

	private void writeQuadsFromSQLConnection(HttpServletRequest req,
			PrintWriter writer) throws SQLException {
		writeCommentBlock("Write from SQL Connection.", writer);

		ConfigurationProperties props = ConfigurationProperties.getBean(req);
		String url = props.getProperty(PROPERTY_DB_URL);
		String username = props.getProperty(PROPERTY_DB_USERNAME);
		String password = props.getProperty(PROPERTY_DB_PASSWORD);

		Properties connectionProps = new Properties();
		connectionProps.put("user", username);
		connectionProps.put("password", password);

		try (Connection conn = DriverManager
				.getConnection(url, connectionProps);
				Statement stmt = createStatement(conn);
				java.sql.ResultSet rs = stmt.executeQuery(SQL_QUERY)) {
			while (rs.next()) {
				NodeInfo s = new NodeInfo(rs.getString("sLex"),
						rs.getString("sLang"), rs.getString("sDatatype"),
						rs.getInt("sType"));
				NodeInfo p = new NodeInfo(rs.getString("pLex"),
						rs.getString("pLang"), rs.getString("pDatatype"),
						rs.getInt("pType"));
				NodeInfo o = new NodeInfo(rs.getString("oLex"),
						rs.getString("oLang"), rs.getString("oDatatype"),
						rs.getInt("oType"));
				NodeInfo g = new NodeInfo(rs.getString("gLex"),
						rs.getString("gLang"), rs.getString("gDatatype"),
						rs.getInt("gType"));
				String line = composeNquadsLine(s, p, o, g);
				writer.println(line);
			}
			writer.flush();
		}
	}

	private Statement createStatement(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement(
				java.sql.ResultSet.TYPE_FORWARD_ONLY,
				java.sql.ResultSet.CONCUR_READ_ONLY);
		stmt.setFetchSize(Integer.MIN_VALUE);
		return stmt;
	}

	private String composeNquadsLine(NodeInfo s, NodeInfo p, NodeInfo o,
			NodeInfo g) {
		return s + " " + p + " " + o + " " + g + " .";
	}

	private static class NodeInfo {
		public final static int ANON = 1;
		public final static int URI_RESOURCE = 2;
		private final String lex;
		private final String lang;
		private final String datatype;
		private final int type;

		public NodeInfo(String lex, String lang, String datatype, int type) {
			this.lex = lex;
			this.lang = lang;
			this.datatype = datatype;
			this.type = type;
		}

		@Override
		public String toString() {
			if (type == ANON) {
				return "_:" + lex;
			} else if (type == URI_RESOURCE) {
				return "<" + lex + ">";
			} else { // LITERAL
				return literalToString();
			}
		}

		private String literalToString() {
			String s = '"' + escapeLex() + '"';
			if (datatype != null && !datatype.isEmpty()) {
				s += "^^" + datatype;
			} else if (lang != null && !lang.isEmpty()) {
				s += "@" + lang;
			}
			return s;
		}

		private String escapeLex() {
			return lex.replace("\\", "\\\\").replace("\n", "\\n")
					.replace("\r", "\\r").replace("\"", "\\\"");
		}

	}

	private void writeCommentBlock(String message, PrintWriter writer) {
		writer.println();
		writer.println("# -------------------------------------------");
		writer.println("# " + message);
		writer.println("# -------------------------------------------");
		writer.println();
	}

	private void dumpResultSetAsQuads(ResultSet resultSet, PrintWriter writer) {
		while (resultSet.hasNext()) {
			QuerySolution solution = resultSet.next();
			String s = nodeToString(solution.get("s"));
			String p = nodeToString(solution.get("p"));
			String o = nodeToString(solution.get("o"));
			String g = nodeToString(solution.get("g"));
			if (g.isEmpty()) {
				writer.println(s + " " + p + " " + o + " .");
			} else {
				writer.println(s + " " + p + " " + o + " " + g + " .");
			}
		}
	}

	private String nodeToString(RDFNode node) {
		if (node == null) {
			return "";
		} else if (node.isLiteral()) {
			Literal l = node.asLiteral();
			return literalToString(l.getLexicalForm(), l.getLanguage(),
					l.getDatatypeURI());
		} else if (node.isURIResource()) {
			Resource resource = node.asResource();
			return "<" + resource.getURI() + ">";
		} else if (node.isAnon()) {
			Resource resource = node.asResource();
			return "_:" + resource.getId().getLabelString(); // get b-node id
		} else {
			return "";
		}
	}

	private String literalToString(String lexicalForm, String language,
			String datatypeURI) {
		StringBuilder buffer = new StringBuilder();
		buffer.append('"').append(escape(lexicalForm)).append('"');
		if (datatypeURI != null) {
			buffer.append("^^").append(datatypeURI).append("");
		} else if (StringUtils.isNotEmpty(language)) {
			buffer.append("@").append(language);
		}
		return buffer.toString();
	}

	private String escape(String raw) {
		return raw.replace("\\", "\\\\").replace("\n", "\\n")
				.replace("\r", "\\r").replace("\"", "\\\"");
	}

	@Override
	protected AuthorizationRequest requiredActions(VitroRequest vreq) {
		// TODO Auto-generated method stub
		throw new RuntimeException(
				"ExportController.requiredActions() not implemented.");
	}

	@Override
	protected void doResponse(VitroRequest vreq, HttpServletResponse response,
			ResponseValues values) throws TemplateProcessingException {
		// TODO Auto-generated method stub
		throw new RuntimeException(
				"ExportController.doResponse() not implemented.");
	}

}
