<HTML>
<font>
<link rel="stylesheet" type="text/css" href="css/global.css.jsp"/>
</head>
<body>
<%
String pageTitle = ((String)request.getAttribute("pagetitle") == null ) ? ( request.getParameter("pagetitle") == null ? "" : request.getParameter("pagetitle") ) : (String) request.getAttribute("pagetitle"); %>
<table border="0" cellpadding="0" cellspacing="0" width="100%" >
<tr>
	<td width="1"><img src="icons/transparent.gif" width="1" height="1" border="0"></td>
	<td bgcolor="#444466">
		<table cellspacing="0" cellpadding="6" border="0">
		<tr><td>&nbsp;</td>
			<td align="center"><a href="index.jsp" class="headerlink">Home</a></td><td>&nbsp;</td>
			<td align="center"><a href="fetch?queryspec=simple.awards"class="headerlink">Awards</a></td><td>&nbsp;</td>
			<td align="center"><a href="fetch?queryspec=simple.sponsors"class="headerlink">Sponsors' Awards</a></td><td>&nbsp;</td>
			<td align="center"><a href="fetch?queryspec=list.l1.subsponsors"class="headerlink">Sponsor Hierarchy</a></td><td>&nbsp;</td>
			<td align="center"><a href="fetch?queryspec=departments"class="headerlink">Departments</a></td><td>&nbsp;</td>
			<td align="center"><a href="fetch?queryspec=deptcount"class="headerlink">Department Awards</a></td><td>&nbsp;</td>
			<td align="center"><a href="fetch?queryspec=investigators"class="headerlink">Investigators list</a></td><td>&nbsp;</td>
			<td align="center"><a href="fetch?queryspec=simple.investigators"class="headerlink">Investigators' Awards</a></td><td>&nbsp;</td>

		</tr>
		</table>
	</td>
</tr>
<tr>
	<td width="1"><img src="icons/transparent.gif" width="1" height="1" border="0"></td>
	<td height="45" valign="top" bgcolor="#444466" >
		<table width="" border="0" cellspacing="0" cellpadding="0">
		<tr>
			<td colspan="2"><img src="icons/transparent.gif" width="1" height="5"></td>
		</tr>
		<tr>
			<td><img src="icons/transparent.gif" height="1" width="20" border="0"></td>
			<td><h1><font color="#FFFFFF"><%=pageTitle%></font></h1></td>
		</tr>
		</table>
	</td>
</tr>
</table>
</body>
</HTML>

