<%
// Make sure the browser knows this is a stylsheet
response.setContentType("text/css");
// Netscape 4.x?
boolean browserNetscape4=false;
String userAgent=request.getHeader("User-Agent");
	if ( userAgent != null && userAgent.startsWith( "Mozilla/4" ) && userAgent.indexOf("MSIE") < 0 ) {
	browserNetscape4=true;
}
%>

BODY {
	margin: 0;
	leftmargin: 0;
	topmargin: 0;
	marginheight: 0;
	marginwidth: 0;
	background-color: #FFFFFF;
}

A {
	/* color: #BC6015; */
	/* color: #111188; */
	/* font-weight: bold; */
	/* color: #9E66CC; */
	color: #6A5ACD;
	text-decoration: none;
}

A:hover {
	color: #CC9933;
}

BODY, P, LI, TD {
	color: #444444;
	font-family : Verdana, Geneva, Arial, Helvetica, sans-serif;
	font-size: 10px;
	line-height : 16px;
}

P {
	margin-top: 1px;
	margin-bottom: 1px;
	padding-bottom: 8px; /* was 12px */
}

P.rednormal {
	color: red;
	}
	
P.redbold {
	color       : red;
	font-weight : bold;
	}

TH {
	color: #444444;
	font-family : Verdana, Geneva, Arial, Helvetica, sans-serif;
	font-size: 12px;
	line-height : 16px;
}

H1 {
	color: #4B0082;
	font-family : Arial, Helvetica, sans-serif;
	font-size: 20px;
	line-height: 20px;
	font-weight: bold;
	margin-top: 1px;
	margin-bottom: 1px;
	padding-bottom: 1px; /* was 8*/
}

H2, H3, H4, H5 {
	font-family : Verdana, Geneva, Arial, Helvetica, sans-serif;
	color: #4B0082;
	}

H2 {
	font-size : 16px;
	font-weight: bold;
}

H3 {
	font-size : 12px;
	font-weight: bold;
	margin-top: 1px;
	margin-bottom: 1px;
	padding-top: 3px;     /* was 4 */
	padding-bottom: 6px;  /* was 8 */
}

H4 {
	font-size : 11px;
	font-weight: bold;
}

H5 {
	font-size: 12px;
	line-height: 16px;
	font-weight: bold;
	margin-top: 1px;
	margin-bottom: 1px;
<% if ( browserNetscape4 ) { %>
	padding-top: 15px;
	padding-bottom: 0px;
<% } else { %>
	padding-top: 0px;
	padding-bottom: 6px;
<% } %>
}

H6 {
	font-family: Georgia, "Times New Roman", Times, serif;
	font-size : 16px;
	font-weight: normal;
	font-style: italic;
	line-height: 24px;
	color: #353535;
	margin-top: 1px;
	margin-bottom: 1px;
	padding-bottom: 32px;
}

H7 {
	color: #F5F5E1;
	font-family: Georgia, "Times New Roman", Times, serif;
	font-size: 12px;
	line-height: 20px;
	font-weight: bold;
	margin-top: 1px;
	margin-bottom: 1px;
	padding-bottom: 8px;
}

A.headerlink {
	font-size: 11px;
	font-weight : bold;
	color : #F5F5E1;
	text-decoration: none;
}

A.headerlink:HOVER {
	color : #EADD6C;
}

A.headerlinkOn {
	font-size: 11px;
	font-weight : bold;
	color : #EADD6C;
	text-decoration: none;
}

A.headerlinkOn:HOVER {
	color : #F5F5E1;
}

A.subNav {
	color: #191970;
	font-size: 9px;
	font-weight: bold;
	line-height: 12px;
	text-decoration: none;
	margin-top: 4px;
	margin-bottom: 1px;
	padding-bottom: 1px;
}

A.subNav:HOVER {
	color: #333333;
}

A.subNavOn {
	color: #333333;
	font-size: 9px;
	font-weight: bold;
	line-height: 11px;
	text-decoration: none;
	margin-top: 4px;
	margin-bottom: 1px;
	padding-bottom: 1px;
}

.footerlink {
	color : #F5F5E1;
	font-family: arial, verdana, geneva, sans-serif;
	font-size: 10px;
	margin-top: 0px;
	margin-bottom: 0px;
	padding-bottom: 0px;
}

.footerlink:HOVER {
	color : #EADD6C;
	font-family: arial, verdana, geneva, sans-serif;
	font-size: 10px;
	margin-top: 0px;
	margin-bottom: 0px;
	padding-bottom: 0px;
}

.footertext {
	color: #62812E;
	font-family: arial, verdana, geneva, sans-serif;
	font-size: 9px;
}

form 	{color:#000000; font-size:9px; font-family:arial,helvetica,"sans serif";font-weight:normal;margin: 2px;}

.form-item {
	background-color : #FAFAD2; /*#EADD6C;*/
	border-color : #2E440C;
	font-family : Verdana, Geneva, Arial, Helvetica, sans-serif;
	font-size: 10px;
	border-width : 1px;
}

.form-button {
	background-color : #CCCCFF; /* #DEB887; */
	border-color : #CCFFFF; /*#2E440C; */
	font-family : Verdana, Geneva, Arial, Helvetica, sans-serif;
	font-size: 10px;
	border-width : 1px;
	margin-top : 2px;
}

.form-background {
	background-color : #C8D8F8; /* #DEB887; */
	border-color : #CCCCFF; /*#2E440C; */
	font-family : Verdana, Geneva, Arial, Helvetica, sans-serif;
	font-size: 10px;
	border-width : 1px;
	margin-top : 2px;
}

.cornflowerblue { background-color	: #C8D8F8;
		color			: black;
		font-family		: Arial, Helvetica, sans-serif;
		font-size		: 9pt;
		font-style		: normal;
}

.medpurple {	background-color	: #CCCCFF;
		color			: black;
		font-family		: Arial, Helvetica, sans-serif;
		font-size		: 9pt;
		font-style		: normal;
}		

.pulldown {
	font-size: 9px;
	background-color : #EADD6C;
	border-style: none;
	font-family : Arial, Helvetica, "sans-serif";
}

.newsItem {
	font-family : Verdana, Geneva, Arial, Helvetica, sans-serif;
	font-size: 10px;
	line-height: 15px;
}

.sidebarlink {
	font-family : Verdana, Geneva, Arial, Helvetica, sans-serif;
	font-size: 10px;
	line-height: 10px;
}


UL {
	margin-top: 1px;
	padding-top: 0px;
	margin-bottom: 1px;
	padding-bottom: 18px;
	margin-left: 17px;
	padding-left: 0px;
	list-style-type : square;
}


.popup {
	font-family : Verdana, Geneva, Arial, Helvetica, sans-serif;
	color: #F5F5E1;
	font-size: 10px;
}

A.popup {
	font-family : Verdana, Geneva, Arial, Helvetica, sans-serif;
	color: #F5F5E1;
	font-size: 10px;
}

.bluebutton {
	background-color : #C8D8F8;
	border-color : #CCCCDD;
	color: #6655CD;
	font-family : Verdana, Geneva, Arial, Helvetica, sans-serif;
	font-size: 11px;
	line-height : 13px;
}

.yellowbutton {
	background-color : #FAFAD2;
	border-color : #CCCCDD;
	color: #0000CD;
	font-family : Verdana, Geneva, Arial, Helvetica, sans-serif;
	font-size: 11px;
	line-height : 14px;
}

.plainbutton {
	background-color : #EEEEEE;
	border-color : #CCCCDD;
	color: #0000CD;
	font-family : Verdana, Geneva, Arial, Helvetica, sans-serif;
	font-size: 11px;
	line-height : 14px;
}

.captions {
	font-family : Verdana, Geneva, Arial, Helvetica, sans-serif;
	font-size: 9px;
	line-height: 11px;
}

.credits {
	font-family : Verdana, Geneva, Arial, Helvetica, sans-serif;
	font-size: 9px;
	line-height: 11px;
	color: #666666;
}

.darkbackground {
	background-color	: #444466;
}

.lightbackground {
	background-color	: #E0FFFF;
}

.whitebackground {
	background-color : #FFFFFF;
	color		: black;
	font-family	: Verdana, Arial, Helvetica, sans-serif;
	font-size	: 8pt;
	font-style	: normal;
	vertical-align  : top;
}

.header {	background-color	: #B0C4DE;
		color 			: black;
		font-family		: Arial, Helvetica, sans-serif;
		font-size		: 10pt;
		font-style		: normal;
		text-align		: left;
}

.database_header {	background-color	: #B0C4DE;
		color 			: black;
		font-family		: Arial, Helvetica, sans-serif;
		font-size		: 10pt;
		font-style		: normal;
		text-align		: left;
}

.database_upperleftcorner {	background-color	: #B0C4DE;
		color 			: black;
		font-family		: Arial, Helvetica, sans-serif;
		font-size		: 10pt;
		font-style		: normal;
		text-align		: left;
}

.database_upperleftcenter {	
		background-color	: #B0C4DE;
		color 			: black;
		font-family		: Arial, Helvetica, sans-serif;
		font-size		: 10pt;
		font-style		: normal;
		text-align		: center;
}

.rownumheader {	background-color	: #B0C4DE;
		color			: black;
		font-family		: Arial, Helvetica, sans-serif;
		font-size		: 10pt;
		font-style		: normal;
		text-align		: center;
}


.headercenter {
		background-color	: #9370DB;
		color 			:  black;
		font-family		:  Arial, Helvetica, sans-serif;
		font-size		:  10pt;
		font-style		:  normal;
		text-align		:  center;
}

.postheader {	background-color	: #E6E6FA;
		/*color			: #4682B4;*/
		color			: #777777;
		font-family		: Arial, Helvetica, sans-serif;
		font-size		: 10pt;
		font-style		: normal;
		text-align		: left;
}

.database_postheader {	background-color	: #E6E6FA;
		/*color			: #4682B4;*/
		color			: #777777;
		font-family		: Arial, Helvetica, sans-serif;
		font-size		: 10pt;
		font-style		: normal;
		text-align		: left;
}

.postheadercenter {
		background-color	: #E6E6FA;
		color			: #4682B4;
		font-family		: Arial, Helvetica, sans-serif;
		font-size		: 10pt;
		font-style		: normal;
		text-align		: center;
}

.postheaderright {
		background-color	: #E6E6FA;
		color			: #4682B4;
		font-family		: Arial, Helvetica, sans-serif;
		font-size		: 10pt;
		font-style		: normal;
		text-align		: right;
}

.verticalfieldlabel {
		background-color        : #DEDEDF;
		color			: black;
		font-family		: Verdana, Arial, Helvetica, sans-serif;
		font-size		: 8pt;
		font-style		: normal;
		text-align		: right;
		vertical-align		: top;
}

.row, .rowvert {		background-color	: #F0FFFF;
		color			: black;
		font-family		: Arial, Helvetica, sans-serif;
		font-size		: 10pt;
		font-style		: normal;
		text-align		: left;
}

.rowalternate {	background-color	: #F8F8FF;
		color			: black;
		font-family		: Arial, Helvetica, sans-serif;
		font-size		: 10pt;
		font-style		: normal;
		text-align		: left;
}

.rowbold {	background-color	: #FFFAFA;
		color			: black;
		font-family		: Arial, Helvetica, sans-serif;
		font-size		: 10pt;
		font-style		: normal;
		font-weight	: bold;
		text-align		: left;
}

.rownum {	background-color	: #87CEFA;
		color			: black;
		font-family		: Arial, Helvetica, sans-serif;
		font-size		: 10pt;
		font-style		: normal;
		text-align		: center;
}

.firstcol {	background-color	: #90EE90;
		color			: black;
		font-family		: Arial, Helvetica, sans-serif;
		font-size		: 10pt;
		font-style		: normal;
		text-align		: left;
}

.postrow {	background-color	: #E6E6BB;
		color			: black;
		font-family		: Arial, Helvetica, sans-serif;
		font-size		: 10pt;
		font-style		: normal;
		text-align		: right;
}

.ltyellow {	background-color	: #FAFAD2;
		color			: black;
		font-family		: Arial, Helvetica, sans-serif;
		font-size		: 10pt;
		font-style		: normal;
}

.people,.awards,.orgs,.cases,.companies,.owners,.interviewers,.offices,.notes,.users,.formats,.clips,.keywords,.discs,.related {
		/*background-color	: #87CEFA;*/
		/*background-color	: #D3D3D3;*/
		background-color        : #DEDEDF;
		color			: black;
		font-family		: Verdana, Arial, Helvetica, sans-serif;
		font-size		: 8pt;
		font-style		: normal;
}

/*---- Styles for HTMLArea -----*/
.style1 {background-color:#33FF00; color:#336600;}
.style2 {background-color:#0000CC; color:#CCFFFF;}
.style3 {background-color:#FFFF33; color:#660000;}

