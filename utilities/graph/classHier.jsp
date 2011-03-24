<html>

<head>
<title>Hierarchy Test</title>
<link rel="stylesheet" type="text/css" href="css/screen">
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
</head>

<body>
<div>
  <%
    String context = request.getContextPath();
    String classId = request.getParameter("classId");
if( classId == null )
    classId = request.getParameter("classid");
if (classId == null)
    classId = "1";
out.println("classId:" + classId );
out.println("<a href='"+context+"/fetch?queryspec=private_vclass&amp;postGenLimit=-1&amp;"
       +"header=null&amp;linkwhere=vclass.id%3D%27"+classId+"%27'>");
out.println("view class as table</a>");
%>

</div>
<div>
         <IMG SRC='<%=context%>/graphdata/images/<%=classId%>.png' USEMAP=#mainmap>
         <MAP NAME="mainmap">
         <jsp:include page='<%="graphdata/maps/"+classId+".map"%>' flush='true'/>
         </MAP>

</div>

<jsp:include page="footer.jsp" flush="true"/>
</body>
</html>
