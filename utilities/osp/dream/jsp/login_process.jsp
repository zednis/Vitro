<%@ page isThreadSafe="false" %>
<%@ page import="java.util.*" %>
<jsp:useBean id="loginHandler" class="formbeans.LoginFormBean" scope="session" />
<jsp:setProperty name="loginHandler" property="*" />
<!--/jsp:useBean-->
<% 
	String submitModeStr = request.getParameter("loginSubmitMode");
	if ( submitModeStr == null )
		submitModeStr = "unknown";
	if ( submitModeStr.equalsIgnoreCase("Log Out")) {
%>
		<jsp:forward page="/logout"/>
<% 
	} else if ( submitModeStr.equalsIgnoreCase("Log In")) {
		String loginNameStr = request.getParameter("loginName");
		String loginPasswordStr = request.getParameter("loginPassword");
%>
		<!--jsp:setProperty name="loginHandler" property="loginName" value="<%=loginNameStr%>" /-->
		<!--jsp:setProperty name="loginHandler" property="loginPassword" value="<%=loginPasswordStr%>" /-->
		<jsp:setProperty name="loginHandler" property="loginRemoteAddr" value="<%=request.getRemoteAddr()%>" />
<%		if ( loginHandler.validateLoginForm() ) {%>
			<jsp:forward page="/authenticate"/>
<%		} else { %>
			<jsp:forward page="/login.jsp"/>
<%		}
	}
%>
