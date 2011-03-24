<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<%@ page language="java" %>
<%@ taglib uri='WEB-INF/tlds/database.tld' prefix='database'%>
<%@ page import="java.util.*" %>
<%
String MENU_COLOR                    = "#DCDCE9"; //"#E6E6FA";
String MENU_HL                       = "#DCDCE9";
%>
<jsp:useBean id="loginHandler" class="formbeans.LoginFormBean" scope="session"/>
<jsp:useBean id="searchproc" class="search.SearchTermProcessor" scope="session" />
<html>
<head>
<title>Business and Entrepreneurship</title>
<link rel="stylesheet" type="text/css" href="css/home.css.jsp">
<%
String currentStr=(currentStr=(String)request.getAttribute("current"))==null ? ((currentStr=request.getParameter("current"))==null ? "none":currentStr) : currentStr;
int securityLevel=-1;
%>
<script type="text/javascript" language="JavaScript">
var dlgAlert=null;
function showLoginDialog( width, height ) {
	var isOpen = (dlgAlert && !dlgAlert.closed );
	if ( !isOpen ) {
		var x = ( screen.width - width  ) / 2;
		var y = ( screen.height - height ) / 2;
		var chrome = "scrollbars=yes,width=" + width + ",height=" + height + ",left=" + x + ",top=" + y;
		dlgAlert = open("login.jsp", "_addNew", chrome );
	}
	dlgAlert.document.open();
	dlgAlert.document.location="login.jsp";
	dlgAlert.document.close();
	dlgAlert.focus();
}

function showMenuFirst(menuID, offset){
 var menu = document.getElementById(menuID); 
 menu.style.left = offset;
 menu.style.visibility = "visible"; 
 return true;
}

function showMenu(menuID, left){
 var menu = document.getElementById(menuID); 
 menu.style.visibility = "visible"; 
 return true;
}

function hideMenu(menuID){
 var menu = document.getElementById(menuID);
 menu.style.visibility = "hidden";
 return true;
}

//highlight-unhighlight
function HL(bgcolor, elem){
 elem.style.backgroundColor = bgcolor;
 return true;
}

</script>
</head>
<body>

<table width="100%" border="0" cellspacing="0" cellpadding="0" align="center">
<tr>
	<td><img src="icons/culogo_65.gif" width="65" height="65" border="0"></td>
	<td align="center" ><h1>Cornell's Collection of Digital Video Clips on Business and Entrepreneurship</h1></td>
	<td align="right" >
		<table id="login" border="0" cellpadding="0" cellspacing="0">
<%		if ( loginHandler.getLoginStatus().equals("authenticated")) { /* test if session is still valid */
 			String currentSessionId = session.getId();
			String storedSessionId = loginHandler.getSessionId();
			if ( currentSessionId.equals( storedSessionId ) ) {
				String currentRemoteAddrStr = request.getRemoteAddr();
				String storedRemoteAddr = loginHandler.getLoginRemoteAddr();
				securityLevel = Integer.parseInt( loginHandler.getLoginRole() );
				if ( currentRemoteAddrStr.equals( storedRemoteAddr ) ) {  
					String loginName=loginHandler.getLoginName(); %>
					<tr><td align="center" valign="bottom" ><i><jsp:getProperty name="loginHandler" property="loginName"/></i></td></tr>
					<tr><td align="center" valign="top" >
						<form name="logout" action="login_process.jsp" method="post" >
						<input type="submit" name="loginSubmitMode" value="log out" class="form-button" />
						</form>
					</td></tr>
<%				} else {
					if (currentStr.equalsIgnoreCase("log in")) { %>
						<tr><td align="center" valign="bottom" class="disabled" >logging in</td></tr>
<%					} else { %>
						<tr><td align="center" valign="bottom" ><!--a href="javascript:showLoginDialog(300,300);" --><a href="login.jsp">log in</a></td></tr>
<%					} %>
					<tr><td align="center" valign="top" ><i>(IP address has changed)</i></td></tr>
<%					loginHandler.setLoginStatus("logged out");
	 			} 
			} else { 
				loginHandler.setLoginStatus("logged out");
				if (currentStr.equalsIgnoreCase("log in")) { %>
					<tr><td align="center" valign="bottom" class="disabled" >logging in</td></tr>
<%				} else { %>
					<tr><td align="center" valign="bottom" ><!--a href="javascript:showLoginDialog(300,300);" --><a href="login.jsp">log in</a></td></tr>
<%				} %>
				<tr><td align="center" valign="top" ><i>(session has expired)</i></td></tr>
<%			}
		} else { /* not thrown out by coming from different IP address or expired session; check login status returned by authenticate.java */ 
			if (currentStr.equalsIgnoreCase("log in")) { %>
				<tr><td align="center" valign="bottom" class="disabled" >logging in</td></tr>
<%			} else { %>
				<tr><td align="center" valign="bottom" ><!--a href="javascript:showLoginDialog(300,300);" --><a href="login.jsp">log in</a></td></tr>
<%			} %>
			<tr><td><hr /></td></tr>
<%			if (currentStr.equalsIgnoreCase("register")) { %>
				<tr><td align="center" valign="top" class="disabled" >registering</td></tr>
<%			} else { %>
				<tr><td align="center" valign="top" ><a href="register.jsp">register</a></td></tr>
<%			}
		} %>
		</table>
	</td>
</tr>
<tr>	<td colspan="3" >
<%		String aboutOffSet=null, usingOffSet=null, adminOffSet=null;
		if (securityLevel>0) {
		  aboutOffSet = "6.5%";
		  usingOffSet = "27%";
		  adminOffSet = "68%";
		} 
		else { 
		  aboutOffSet = "7.5%";
		  usingOffSet = "30%";
		  adminOffSet = "68%";
		}
%>
	<table id="primarytabs" border="0" width="100%" cellpadding="0" cellspacing="0" >
	<tr><td class="<%=currentStr.equals("home") ? "headerlinkOn":"headerlink"%>" ><a class="<%=currentStr.equals("home") ? "headerlinkOn":"headerlink"%>" href="index.jsp" >home</a></td>
		<td class="<%=currentStr.equals("about") ? "headerlinkOn":"headerlink"%>" onMouseOver="showMenuFirst('about','<%=aboutOffSet%>'); HL('#4B0082',this)" onMouseOut="hideMenu('about');  HL('#6A5ACD',this)"><a href="about">about the collection</a></td>
		<td class="<%=currentStr.equals("using") ? "headerlinkOn":"headerlink"%>" onMouseOver="showMenuFirst('using','<%=usingOffSet%>'); HL('#4B0082',this)" onMouseOut="hideMenu('using');  HL('#6A5ACD',this)"><a href="using.jsp">using video clips</a></td>
		<td class="<%=currentStr.equals("who") ? "headerlinkOn":"headerlink"%>" ><a href="who.jsp">who we are</a></td>
		<td class="<%=currentStr.equals("comments") ? "headerlinkOn":"headerlink"%>" ><a href="comments.jsp">contact us</a></td>
<%		if (securityLevel>0) {%>
			<td class="<%=currentStr.equals("admin") ? "headerlinkOn":"headerlink"%>" onMouseOver="showMenuFirst('admin','<%=adminOffSet%>'); HL('#4B0082',this)" onMouseOut="hideMenu('admin'); HL('#6A5ACD',this)"><a href="<%=(securityLevel>=3)?"admin.jsp":"index.jsp"%>">admin</a></td>
<%		} %>
		<td align="right"  class="<%=currentStr.equals("search") ? "headerlinkOn":"headerlink"%>" >
<%			if ( currentStr.equals("search")) { %>
				(search below)
<%			} else { %>
				<form name="searchform" action="fedsearch" method="post" >
				<input type="hidden" name="minstatus" value="5" />
				<input type="hidden" name="searchmethod" value="fulltext" />
<%				if (searchproc!=null && searchproc.getSessionId()==request.getSession().getId()) { %>
					<input type="text" name="querytext" class="form-item" value="<%=searchproc.getQueryTerms()%>" size="15" />
					<input type="hidden" name="inclusion" value="<%=searchproc.getInclusion()%>" />
<%				} else { %>
					<input type="text" name="querytext" class="form-item" value="" size="15" />
<%				} %>
				<a href="javascript:document.searchform.submit()" >search</a>
				</form>
<%			} %>
		</td>
	</tr>
   </table>
 </td>
</tr>

<tr><td colspan="3">
<div style="position:relative; z-index: 100;">
	<div id="about" class="menu" style="width:25em" onMouseOver="showMenu('about')" onMouseOut="hideMenu('about')">
	    <div class="item" onMouseOver="HL('<%=MENU_HL%>', this)" onMouseOut="HL('<%=MENU_COLOR%>', this)"><a href="about#whatIs">What is the collection?</a></div>
	    <div class="item" onMouseOver="HL('<%=MENU_HL%>', this)" onMouseOut="HL('<%=MENU_COLOR%>', this)"><a href="about#howUsed">How is it used?</a></div>
	    <div class="item" onMouseOver="HL('<%=MENU_HL%>', this)" onMouseOut="HL('<%=MENU_COLOR%>', this)"><a href="about#howWhy">How and why was it started?</a></div>
	    <div class="item" onMouseOver="HL('<%=MENU_HL%>', this)" onMouseOut="HL('<%=MENU_COLOR%>', this)"><a href="about#whatOther">What other material is in the collection?</a></div>
	    <div class="item" onMouseOver="HL('<%=MENU_HL%>', this)" onMouseOut="HL('<%=MENU_COLOR%>', this)"><a href="about#howCan">How can other educators use the clips?</a></div>
	</div>
	<div id="using" class="menu" style="width:22em" onMouseOver="showMenu('using')" onMouseOut="hideMenu('using')">
	    <div class="item" onMouseOver="HL('<%=MENU_HL%>', this)" onMouseOut="HL('<%=MENU_COLOR%>', this)"><a href="using.jsp#value">The value proposition</a></div>
	    <div class="item" onMouseOver="HL('<%=MENU_HL%>', this)" onMouseOut="HL('<%=MENU_COLOR%>', this)"><a href="using.jsp#whatDo">What do educators want?</a></div>
	    <div class="item" onMouseOver="HL('<%=MENU_HL%>', this)" onMouseOut="HL('<%=MENU_COLOR%>', this)"><a href="using.jsp#whyIS">Why is editing into clips so important?</a></div>
	    <div class="item" onMouseOver="HL('<%=MENU_HL%>', this)" onMouseOut="HL('<%=MENU_COLOR%>', this)"><a href="using.jsp#whyUse">Why use Cornell's Business and Entrepreneurship Collection?</a></div>
	    <div class="item" onMouseOver="HL('<%=MENU_HL%>', this)" onMouseOut="HL('<%=MENU_COLOR%>', this)"><a href="using.jsp#howDo">How do video clips improve learning?</a></div>
		<div class="item" onMouseOver="HL('<%=MENU_HL%>', this)" onMouseOut="HL('<%=MENU_COLOR%>', this)"><a href="using.jsp#using">Using video clips in class</a></div>
	</div>
	<div id="admin" class="menu" style="width:15em" onMouseOver="showMenu('admin')" onMouseOut="hideMenu('admin')">
		<div class="item" onMouseOver="HL('<%=MENU_HL%>', this)" onMouseOut="HL('<%=MENU_COLOR%>', this)"><a href="fetch?queryspec=public_downloads&linkwhere=clipdownloads.userId=<%=loginHandler.getUserId()%>">Clips I've downloaded</a></div>
<%		if (securityLevel >= 3) {%>
			<div class="item" onMouseOver="HL('<%=MENU_HL%>', this)" onMouseOut="HL('<%=MENU_COLOR%>', this)"><a href="fetch?queryspec=public_quotes">Quotes</a></div>
			<div class="item" onMouseOver="HL('<%=MENU_HL%>', this)" onMouseOut="HL('<%=MENU_COLOR%>', this)"><a href="fetch?queryspec=public_interviewers">Interviewers</a></div>
<%			if (securityLevel >= 4) {%>
				<div class="item">Uploads:</div>
					<div class="item" onMouseOver="HL('<%=MENU_HL%>', this)" onMouseOut="HL('<%=MENU_COLOR%>', this)"><a href="uploadppts.jsp">upload Powerpoint</a></div>
					<div class="item" onMouseOver="HL('<%=MENU_HL%>', this)" onMouseOut="HL('<%=MENU_COLOR%>', this)"><a href="uploadfiles.jsp">upload headshots</a></div>
					<div class="item" onMouseOver="HL('<%=MENU_HL%>', this)" onMouseOut="HL('<%=MENU_COLOR%>', this)"><a href="batchloadclipnames.jsp">upload clip names</a></div>
					<div class="item" onMouseOver="HL('<%=MENU_HL%>', this)" onMouseOut="HL('<%=MENU_COLOR%>', this)"><a href="fetch?queryspec=private_clipSandboxes">clip sandbox</a></div>
					<div class="item" onMouseOver="HL('<%=MENU_HL%>', this)" onMouseOut="HL('<%=MENU_COLOR%>', this)"><a href="batchloadclips.jsp">upload completed clip metadata</a></div>
				<div class="item">Data Processing:</div>
					<div class="item" onMouseOver="HL('<%=MENU_HL%>', this)" onMouseOut="HL('<%=MENU_COLOR%>', this)"><a href="setclipsizes.jsp">review status of clips on server</a></div>				
					<div class="item" onMouseOver="HL('<%=MENU_HL%>', this)" onMouseOut="HL('<%=MENU_COLOR%>', this)"><a href="datainventory.jsp">inventory clips on server by case directory</a></div>				
					<div class="item" onMouseOver="HL('<%=MENU_HL%>', this)" onMouseOut="HL('<%=MENU_COLOR%>', this)"><a href="metafiles.jsp">update windows media video metafiles</a></div>				
					<div class="item" onMouseOver="HL('<%=MENU_HL%>', this)" onMouseOut="HL('<%=MENU_COLOR%>', this)"><a href="quicktimes.jsp">update quicktime and mpeg 1 metafiles</a></div>
				<div class="item">User Reporting:</div>
					<div class="item" onMouseOver="HL('<%=MENU_HL%>', this)" onMouseOut="HL('<%=MENU_COLOR%>', this)"><a href="fetch?queryspec=private_searchFailures">failed searches</a></div>			
					<div class="item" onMouseOver="HL('<%=MENU_HL%>', this)" onMouseOut="HL('<%=MENU_COLOR%>', this)"><a href="fetch?queryspec=private_clipdownloads">clip downloads</a></div>
<%				if ( securityLevel >=5 ) { %>
					<div class="item">Reserved functionality:</div>
						<div class="item" onMouseOver="HL('<%=MENU_HL%>', this)" onMouseOut="HL('<%=MENU_COLOR%>', this)"><a href="fetch?queryspec=private_titlesMatched">clip titles starting with names</a></div>			
						<div class="item" onMouseOver="HL('<%=MENU_HL%>', this)" onMouseOut="HL('<%=MENU_COLOR%>', this)"><a href="fetch?queryspec=private_titlesUnmatched">clip titles not starting with names</a></div>
						<div class="item" onMouseOver="HL('<%=MENU_HL%>', this)" onMouseOut="HL('<%=MENU_COLOR%>', this)"><a href="schema.gif">database schema</a></div>
						<!--div class="item" onMouseOver="HL('<%=MENU_HL%>', this)" onMouseOut="HL('<%=MENU_COLOR%>', this)"><a href="cliptitles.jsp">re-process clip titles</a></div-->
						<div class="item" onMouseOver="HL('<%=MENU_HL%>', this)" onMouseOut="HL('<%=MENU_COLOR%>', this)"><a href="addkeyword.jsp">add keyword</a></div>
						<div class="item" onMouseOver="HL('<%=MENU_HL%>', this)" onMouseOut="HL('<%=MENU_COLOR%>', this)"><a href="fetch?queryspec=public_clips_without_cases">clips without cases</a></div>
						<div class="item" onMouseOver="HL('<%=MENU_HL%>', this)" onMouseOut="HL('<%=MENU_COLOR%>', this)"><a href="fetch?queryspec=public_clips_without_keywords">clips without keywords</a></div>
						<div class="item" onMouseOver="HL('<%=MENU_HL%>', this)" onMouseOut="HL('<%=MENU_COLOR%>', this)"><a href="fetch?queryspec=public_clips_without_formats">clips without formats</a></div>
						<div class="item" onMouseOver="HL('<%=MENU_HL%>', this)" onMouseOut="HL('<%=MENU_COLOR%>', this)"><a href="fetch?queryspec=public_clips_without_people">clips without people</a></div>
						<div class="item" onMouseOver="HL('<%=MENU_HL%>', this)" onMouseOut="HL('<%=MENU_COLOR%>', this)"><a href="fetch?queryspec=public_headings">all subject headings</a></div>
						<div class="item" onMouseOver="HL('<%=MENU_HL%>', this)" onMouseOut="HL('<%=MENU_COLOR%>', this)"><a href="addkeyword.jsp">add keyword</a></div>
<%				}
			}
		} %>
	</div>
</div>
</td></tr>
</table>
<!-- <comment>DO NOT CLOSE TABLE OR MAIN BODY TABLE WILL HAVE DIFFERENT WIDTH</comment> -->
</body>
</html>