<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 3.2//EN">
<%@ taglib uri="WEB-INF/tlds/database.tld" prefix="database"%>
<%@ page import="java.util.*" %>
<html>
<head>
<title>Help Selecting Formats</title>
<link rel="stylesheet" type="text/css" href="css/home.css.jsp"/>
</head>
<body>
<%
int imageWidth=100;
%>
<div id="main" >

<jsp:include page="head.jsp" flush="true" ><jsp:param name="current" value="using" /></jsp:include>

<div id="bodyContainer">
<table width="100%" border="0" cellspacing="0" cellpadding="0" align="center">
<tr><td width="18%" align="left" valign="top">
	<jsp:include page="left.jsp" flush="true" />
	</td>
	<td width="64%" valign="top">
  	 <center><h2>Help Selecting Formats</h2></center>
<p class="small">Clips are available in 3 formats:
<ul>
<li><a target="_new" href="http://www.chiariglione.org/mpeg/"><strong>MPEG 1</strong></a> was released in 1991 by the Moving Picture Experts Group and is supported across multiple platforms.  MPEG 1 has been commonly used for posting video on the Web, and MPEG 1 clips are viewable on almost any computer.
For any given clip, the MPEG 1 format file will be the largest of the three formats supported on this site.</li>
<li><a target="_new" href="http://www.apple.com/quicktime"><strong>Quicktime 4</strong></a>,
			developed originally by Apple Computer for the Macintosh, is now also supported for viewing on Windows computers.
			The Quicktime clips on this site display at twice the size of MPEG 1 clips, so are an excellent format to select for previewing content.
			However, recent versions of Microsoft Powerpoint on Windows PCs do not support importing QuickTime video clips, limiting this
			format's utility in preparing lectures using Powerpoint for Windows users.</a></li>
<li><a target="_new" href="http://www.microsoft.com/windows/windowsmedia/default.aspx"><strong>Windows Media Video</strong></a> is supported only on Microsoft Windows platforms. Windows Media Video is a proprietary compression format using MPEG-4 encoding, designed for both streaming and downloadable content. The Windows Media Video format is very space-efficient on disk, an advantage when storage and portability are important.</li>
</ul></p>
<p class="small">Note that not all clips are available in all three formats.</p>
<p class="small">Other resources:
		<ul><li>The Crutchfield Advisor's <a target="_new" href="http://www.crutchfieldadvisor.com/ISEO-rgbtcspd/learningcenter/home/fileformats_glossary.html">audio and video file formats glossary</a></li>
</ul></p>

</td>
	
	<td width="18%" align="right" valign="top">
		<span id="rightsidebar" >
		<table border="0" width="90%" cellspacing="0" cellpadding="0">
		<tr><td><h3 class="celltopcentered">Players</h3></td></tr>
		<tr><td align="center">
				<a target="_new" href="http://www.apple.com/quicktime/download" >
				<img src="images/logos/QT4.gif" width="<%=imageWidth%>" alt="Quicktime" /></a><br />
				<a target="_new" href="http://www.apple.com/quicktime/download" >Get QuickTime</a>		
			</td></tr>
		<tr><td align="center"><hr /></td></tr>
		<tr><td align="center">
			<a target="_new" href="http://www.real.com" ><img src="images/logos/RealPlayer.gif" width="<%=imageWidth%>" alt="Real Player" /></a><br />
			<a target="_new" href="http://www.real.com" >Get Real Player</a>		
			</td></tr>
		<tr><td align="center"><hr /></td></tr>
		<tr><td align="center">
			<a target="_new" href="http://www.microsoft.com/windows/windowsmedia/download/default.asp" >
				<img src="images/logos/WindowsMediaPlayer.gif" width="<%=imageWidth%>" alt="Windows Media Player" /></a><br />
			<a target="_new" href="http://www.microsoft.com/windows/windowsmedia/download/default.asp" >Get Windows Media Player</a>	
			</td></tr>
		<tr><td align="center"><hr /></td></tr>
		</table>
		</span>
	</td>
</tr>
<jsp:include page="foot.jsp" flush="true" />
</table>
</div>
</div>
</body>
</html>
