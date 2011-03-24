<%
// Make sure the browser knows this is a stylsheet
response.setContentType("text/css");
%>
<%
String BODY_FONT_FAMILY = "Verdana, Lucida, Arial, Helvetica, sans-serif"; //\"Lucida Grande\", 
String ALT_FONT_FAMILY  = "Verdana, Geneva, Arial, Helvetica, sans-serif";

String WHITE                    = "white";
String LIGHT_GRAY               = "#DDC";        // #DEDEDF";
String MEDIUM_GRAY              = "#999999";     // #AAAAAA
String CHARCOAL_GRAY            = "#222222";     // Streeter

String LIGHT_GRAY_BLUE          = "#C8D8F8";
String GRAY_BLUE                = "#A6B6D6";     // CUGIR #B0C4DE is lighter
String MEDIUM_BLUE              = "#6A5ACD";

String LIGHT_PURPLE             = "#CCCCFF";     // or #CCCCDD
String DARK_PURPLE              = "#4B0082";     // CUGIR, #4838AB Streeter

String RED                      = "red";

String PALE_GREEN               = "#EFFBE7";     // even paler is #F5F5E1
String LIGHT_GREEN              = "#F3F9BD";

String IVORY_TINT               = "#F5F5E1";
String LIGHT_CREAM_YELLOW       = "#EEEE99";
String MEDIUM_CREAM_YELLOW      = "#EEEE66";
String ORANGE                   = "#C25D04";     //"#ff9900"; "#DF7503" (reddish)
String SEPIA                    = "#CC9933";

// pairings of colors with roles
String BODY_BACKGROUND_COLOR    = IVORY_TINT;
String BODY_DEFAULT_FONT_COLOR  = CHARCOAL_GRAY; // DARK_GRAY;
String TITLE_DEFAULT_COLOR      = DARK_PURPLE;

// link colors in body elements (other than tabbed menus)
String BODY_LINK_VISITED_COLOR  = MEDIUM_BLUE;
String BODY_DEFAULT_LINK_COLOR  = MEDIUM_BLUE;
String BODY_LINK_HOVER_COLOR    = MEDIUM_BLUE;         // ORANGE;
String BODY_LINK_ACTIVE_COLOR   = ORANGE;        // DARK_BROWN;

// menu table cell background colors
String MENU_DEFAULT_BACKGROUND_COLOR  = MEDIUM_BLUE;   // Vivo is LIGHT_TEAL;
String MENU_SELECTED_BACKGROUND_COLOR = DARK_PURPLE;   // Vivo is DARK_TEAL;

// link default, visited, hover, and active color
String MENU_SELECTED_LINK_COLOR = WHITE;

// link colors in non-selected menu table cells
String MENU_LINK_VISITED_COLOR  = WHITE;
String MENU_LINK_DEFAULT_COLOR  = WHITE;
String MENU_LINK_HOVER_COLOR    = LIGHT_CREAM_YELLOW;  // MEDIUM_CREAM_YELLOW;
String MENU_LINK_ACTIVE_COLOR   = MEDIUM_CREAM_YELLOW; // ORANGE;

// margins
String OUTER_MARGIN  = "1em";
String INNER_MARGIN  = "0.5em";
String INNER_PADDING = "2px 5px";

// font stuff
String FONT_SIZE_NORMAL   =  "85%";
String FONT_SIZE_MENU     =  "95%";
String FONT_SIZE_BIG      = "100%";
String FONT_SIZE_BIGGER   = "115%";
String FONT_SIZE_BIGGEST  = "125%";
String FONT_SIZE_SMALL    = " 80%";
String FONT_SIZE_SMALLER  =  "75%";
String FONT_SIZE_SMALLEST =  "70%";

String LINE_HEIGHT_NORMAL     = "120%";          // normal, 1.5
String LINE_HEIGHT_COMPRESSED = "90%";           // 1.2
String LINE_HEIGHT_EXPANDED   = "150%";          // 1.8 or 1.7
String TITLE_LINE_HEIGHT      = "120%";          // 2.0
%>

body, div, h2, p, td {
	font-family     : <%=BODY_FONT_FAMILY%>;
	background-color: <%=BODY_BACKGROUND_COLOR%>;
	color           : <%=BODY_DEFAULT_FONT_COLOR%>; // #222222;
	font-size       : <%=FONT_SIZE_NORMAL%>         // 11px;
	line-height     : <%=LINE_HEIGHT_NORMAL%>;      // 15px;
	}

p.small {
	font-size       : <%=FONT_SIZE_SMALL%>;
	font-weight     : normal;
	}

p.warning {
	font-size       : <%=FONT_SIZE_SMALL%>;
	font-weight     : normal;
	font-style      : italic;
	color           : <%=ORANGE%>;
	}


p.smaller {
	font-size       : <%=FONT_SIZE_SMALLER%>;
	font-weight     : normal;
	}

p.smallerbold {
	font-size       : <%=FONT_SIZE_SMALLER%>;
	font-weight     : bold;
	}


#main {
	margin-top      :  10px;
	margin-left     :   5px;
	margin-right    :  10px;
	margin-bottom   :  10px;
	}
      

h1 {
	font-size       : <%=FONT_SIZE_BIGGEST%>;       // 18px;
	font-weight     : bold;
	line-height     : <%=TITLE_LINE_HEIGHT%>;       // 1.4;
	}

h2 {
	font-size       : <%=FONT_SIZE_BIGGER%>;        // 16px;
	font-weight     : bold;
	color           : <%=BODY_DEFAULT_FONT_COLOR%>;
	line-height     : <%=LINE_HEIGHT_EXPANDED%>;
	margin-top      : 0.5em;
	margin-bottom   : 0.5em;
	}

h3 {	
	background      : <%=LIGHT_GRAY%>;
	color           : <%=BODY_DEFAULT_FONT_COLOR%>;
/*	display         : block; */
	font-size       : <%=FONT_SIZE_SMALL%>;
	line-height     : <%=LINE_HEIGHT_NORMAL%>;
	margin-top      : 1em;
	margin-bottom   : 1em;
	padding-bottom  : 2px;
/*	text-transform  : none; */
	vertical-align  : middle;
	}

/*
h3:first-letter {
	text-transform  : uppercase;
	} */

h3.centered {
	text-align      : center;
	}
	
h3.celltop {	
	margin-top      : 0;
	}

h3.celltopcentered {	
	margin-top      : 0;
	text-align      : center;
	}

h3.filler {	
	background      : <%=LIGHT_GRAY%>;
	color           : <%=BODY_DEFAULT_FONT_COLOR%>;
/*	display         : block; */
	font-size       : <%=FONT_SIZE_SMALLER%>;         // 9px;
	font-weight     : normal;
	font-style      : italic;
	margin-top      : 1em;
	margin-bottom   : 1em;
	padding-bottom  : 2px;
/*	text-transform  : none; */
	}

h3.celltopfiller {	
	font-size       : <%=FONT_SIZE_SMALL%>;         // 9px;
	font-weight     : normal;
	font-style      : italic;
	margin-top      : 0;
/*	text-transform  : none; */
	}

h3 img {
/*	margin          : 0 0 2px 4px; */
	vertical-align  : middle;
	}
	

h3 a:visited {
	font-weight     : bold; 
	text-decoration : none;
	color           : <%=BODY_LINK_VISITED_COLOR%>; // #6a5acd;
	background      : transparent; 
	}

h3 a:link {
	font-weight     : bold; 
	text-decoration : none;
	color           : <%=BODY_DEFAULT_LINK_COLOR%>; // #6a5acd;
	background      : transparent; 
	}

h3 a:hover {
	font-weight     : bold; 
	text-decoration : underline;
	color           : <%=BODY_LINK_HOVER_COLOR%>;   // #cc9933;
	background      : transparent; 
	}

h3 a:active {
	font-weight     : bold; 
	text-decoration : none;
	color           : <%=BODY_LINK_ACTIVE_COLOR%>;  // #f90;
	background      : transparent;  
	}

h4 {	
	color           : <%=BODY_DEFAULT_FONT_COLOR%>; // #333;
	font-size       : <%=FONT_SIZE_NORMAL%>;        // 12px;
	margin-top      : 0.5em;
	margin-bottom   : 0.5em;
	}

a:visited {
	font-weight     : normal; 
	text-decoration : none;
	color           : <%=BODY_LINK_VISITED_COLOR%>; // #6a5acd;
	background      : transparent; 
	}

a:link {
	font-weight     : normal; 
	text-decoration : none;
	color           : <%=BODY_DEFAULT_LINK_COLOR%>; // #6a5acd;
	background      : transparent; 
	}

a:hover {
	font-weight     : normal; 
	text-decoration : underline;
	color           : <%=BODY_LINK_HOVER_COLOR%>;   // #cc9933;
	background      : transparent; 
	}

a:active {
	font-weight     : normal; 
	text-decoration : none;
	color           : <%=BODY_LINK_ACTIVE_COLOR%>;  // #f90;
	background      : transparent;  
	}

#login a {
	font-size       : <%=FONT_SIZE_SMALLER%>;
	margin          : 0 3px 0 0;
	padding         : 0 3px 0 0;
	}

#primarytabs {
	margin          : 2px 0;
	font-size       : <%=FONT_SIZE_SMALL%>;        // 11px;
	}

#primarytabs td {
	padding         : 2px 0.33em;
	text-align      : left;
	vertical-align  : middle;
	}

#primarytabs td.headerlink {
	background-color: <%=MENU_DEFAULT_BACKGROUND_COLOR%>; // #6a5acd;
	}
	
#primarytabs td.headerlinkOn {
	background-color: <%=MENU_SELECTED_BACKGROUND_COLOR%>; // #4838ab; 
	}
	
#primarytabs a:visited {
	font-weight     : normal; 
	text-decoration : none;
	color           : <%=MENU_LINK_VISITED_COLOR%>;
	background      : transparent; 
	}
	
	
#primarytabs a:link {
	font-weight     : normal; 
	text-decoration : none;
	color           : <%=MENU_LINK_VISITED_COLOR%>;
	background      : transparent; 
	}
	
#primarytabs a:hover {
	font-weight     : normal; 
	text-decoration : underline;
	color           : <%=MENU_LINK_HOVER_COLOR%>;   // #eeee99;
	background      : transparent; 
	}

#primarytabs a:active {
	font-weight     : normal; 
	text-decoration : none;
	color           : <%=MENU_LINK_ACTIVE_COLOR%>;  // #eeee66;
	background      : transparent;  
	}

#register td {
	font-size       : <%=FONT_SIZE_SMALLER%>;
	font-weight     : normal;
	vertical-align  : top;
	}
	
.hide	{
	display         : none;
	}
	
img {
	border          : 0;
/*	display         : block; */
	margin          : 0 3px 3px 0;
	}

img.publicsample {
	border          : 2px solid red;
	margin          : 0 3px 3px 0;
	}
	
img.iconify {
	margin-top      :3px;
	}

li {
	margin-left     : 0;
	}

hr.thin {
	color           : <%=LIGHT_GRAY%>;
	height          : 3px;
	}

hr.thinner {
	color           : <%=LIGHT_GRAY%>;
	height          : 2px;
	margin          : 1px;
	padding         : 0;
	}
	
#footer a {
	font-size       : <%=FONT_SIZE_SMALL%>;
	margin-top      : 15px;
	}
	
#footer hr.fat {
	align           : center;
	color           : <%=LIGHT_GRAY%>;
	height          : 9px;
/*	noshade         : noshade; */
	}

#leftsidebar {
	margin          : 0 10px 0 0;
	width           : 100%;
	}

tr.shaded {
	background-color: <%=LIGHT_GRAY_BLUE%>;         // #c8d8f8;
	}
	
td.disabled {
	color           : <%=LIGHT_GRAY%>;
	font-size       : <%=FONT_SIZE_SMALL%>;
	}
	
td.topTopicButton {
	background-color: <%=LIGHT_GRAY_BLUE%>;
	border          : 1px solid <%=MEDIUM_BLUE%>;
	font-size       : <%=FONT_SIZE_SMALL%>;         // 11px;
	font-weight     : bold;
	}

td.topTopicButtonSelected {
	background-color: <%=GRAY_BLUE%>;
	border	        : 1px solid <%=MEDIUM_BLUE%>;
	font-size       : <%=FONT_SIZE_SMALL%>;         // 11px;
	font-weight     : bold;
	}

td.spacerButton {
	background-color: <%=BODY_BACKGROUND_COLOR%>;
	font-size       : <%=FONT_SIZE_SMALL%>;
	font-weight     : bold;
	}

td.topicButton {
	background-color: <%=LIGHT_GRAY_BLUE%>;
	border-bottom   : 1px solid <%=MEDIUM_BLUE%>;
	border-left     : 1px solid <%=MEDIUM_BLUE%>;
	border-right    : 1px solid <%=MEDIUM_BLUE%>;
	font-size       : <%=FONT_SIZE_SMALL%>; 
	font-weight     : bold;
	}

td.topicButtonSelected {
	background-color: <%=GRAY_BLUE%>;
	border-bottom   : 1px solid <%=MEDIUM_BLUE%>;
	border-left     : 1px solid <%=MEDIUM_BLUE%>;
	border-right    : 1px solid <%=MEDIUM_BLUE%>;
	font-size       : <%=FONT_SIZE_SMALL%>;
	font-weight     : bold;
	}
	
td.topicButtonSelected a:link {
	color           : <%=MENU_LINK_DEFAULT_COLOR%>;
	}

td.attName {
	color           : <%=BODY_DEFAULT_FONT_COLOR%>;
	font-size       : <%=FONT_SIZE_NORMAL%>;
	font-weight     : normal;
	line-height     : <%=LINE_HEIGHT_EXPANDED%>;     // 18px;
	text-align      : left;
	vertical-align  : top;
	}

td.attNumber {
	color           : <%=BODY_DEFAULT_FONT_COLOR%>;
	font-size       : <%=FONT_SIZE_SMALL%>;         // 10px;
	font-weight     : normal;
	line-height     : 18px;
	text-align      : left;
	vertical-align  : top;
	}

td.attData {
	color           : <%=BODY_DEFAULT_FONT_COLOR%>;
	font-size       : <%=FONT_SIZE_SMALL%>;         // 10px;
	font-weight     : normal;
	line-height     : 18px;
	text-align      : center;
	vertical-align  : top;
	}

td.missingFormat {
	color           : <%=LIGHT_GRAY%>;
	font-size       : <%=FONT_SIZE_SMALLER%>;
	font-weight     : normal;
	line-height     : 18px;
	padding         : 0 2px 2px 0;
	text-align      : center;
	vertical-align  : top;
	}
	
td.vFormat {
	background      : <%=PALE_GREEN%>;
	color           : <%=BODY_DEFAULT_FONT_COLOR%>;
	font-size       : <%=FONT_SIZE_SMALLER%>;
	font-weight     : normal;
	line-height     : 18px;
	padding         : 0 2px 2px 0;
	text-align      : center;
	vertical-align  : top;
	}

td.vFormatLeft {
	background      : <%=PALE_GREEN%>;
	color           : <%=BODY_DEFAULT_FONT_COLOR%>;
	font-size       : <%=FONT_SIZE_SMALLER%>;
	font-weight     : normal;
	font-style      : italic;
	line-height     : 18px;
	padding         : 0 2px 2px 0;
	text-align      : left;
	vertical-align  : top;
	}

/*
td.attValue {
	background-color: transparent;
	color           : <%=BODY_DEFAULT_FONT_COLOR%>;
	font-size       : <%=FONT_SIZE_NORMAL%>;
	font-weight     : normal;
	line-height     : <%=LINE_HEIGHT_EXPANDED%>;
	text-align      : left;
	vertical-align  : top;
	}
*/

.attValue {
	background-color: transparent;
	color           : <%=BODY_DEFAULT_FONT_COLOR%>;
	font-size       : <%=FONT_SIZE_NORMAL%>;
	font-weight     : normal;
	line-height     : <%=LINE_HEIGHT_EXPANDED%>;
	text-align      : left;
	vertical-align  : top;
	}

.attValueLink {
	font-weight     : normal; 
	text-decoration : none;
	color           : <%=BODY_DEFAULT_LINK_COLOR%>;
	background      : transparent; 
	}

.attValueHighlight {
	background-color: transparent;
	color           : <%=RED%>;
	font-size       : <%=FONT_SIZE_NORMAL%>;
	font-weight     : normal;
	line-height     : <%=LINE_HEIGHT_EXPANDED%>;
	text-align      : left;
	vertical-align  : top;
	}

.attValueHighlightLink {
	font-weight     : normal; 
	text-decoration : none;
	color           : <%=RED%>;
	background      : transparent; 
	}

td.attValueCompact {
	background-color: transparent;
	color           : <%=BODY_DEFAULT_FONT_COLOR%>;
	font-size       : <%=FONT_SIZE_NORMAL%>;
	font-weight     : normal;
	line-height     : <%=LINE_HEIGHT_NORMAL%>;
	text-align      : left;
	vertical-align  : top;
	}

td.attValueCompactOdd {
	background-color: transparent;
	color           : <%=BODY_DEFAULT_FONT_COLOR%>;
	font-size       : <%=FONT_SIZE_NORMAL%>;
	font-weight     : normal;
	line-height     : <%=LINE_HEIGHT_NORMAL%>;
	text-align      : left;
	vertical-align  : top;
	}

td.attValueCompactEven {
	background-color: <%=PALE_GREEN%>;
	color           : <%=BODY_DEFAULT_FONT_COLOR%>;
	font-size       : <%=FONT_SIZE_NORMAL%>;
	font-weight     : normal;
	line-height     : <%=LINE_HEIGHT_NORMAL%>;
	text-align      : left;
	vertical-align  : top;
	}

td.attValueShaded {
	background-color: <%=PALE_GREEN%>;
	color           : <%=BODY_DEFAULT_FONT_COLOR%>;
	font-size       : <%=FONT_SIZE_NORMAL%>;
	font-weight     : normal;
	line-height     : <%=LINE_HEIGHT_EXPANDED%>;
	text-align      : left;
	vertical-align  : top;
	}
	
td.attValueCenteredShaded {
	background-color: <%=PALE_GREEN%>;
	color           : <%=BODY_DEFAULT_FONT_COLOR%>;
	font-size       : <%=FONT_SIZE_NORMAL%>;
	font-weight     : normal;
	line-height     : <%=LINE_HEIGHT_EXPANDED%>;
	text-align      : center;
	vertical-align  : top;
	}

td.attValueCentered {
	background-color: transparent;
	color           : <%=BODY_DEFAULT_FONT_COLOR%>;
	font-size       : <%=FONT_SIZE_NORMAL%>;
	font-weight     : normal;
	line-height     : <%=LINE_HEIGHT_EXPANDED%>;    // 18px;
	text-align      : center;
	vertical-align  : top;
	}

td.attValueCenteredShaded {
	background-color: <%=PALE_GREEN%>;              // #EFFBE7;
	color           : <%=BODY_DEFAULT_FONT_COLOR%>;
	font-size       : <%=FONT_SIZE_NORMAL%>;
	font-weight     : normal;
	line-height     : <%=LINE_HEIGHT_EXPANDED%>;    // 18px;
	text-align      : center;
	vertical-align  : top;
	}

td.attBoldValue {
	color           : <%=BODY_DEFAULT_FONT_COLOR%>;
	font-size       : <%=FONT_SIZE_NORMAL%>;
	font-weight     : bold;
	line-height     : <%=LINE_HEIGHT_EXPANDED%>;    // 18px;
	text-align      : left;
	vertical-align  : top;
	}

td.attTitleValue {
	background      : <%=LIGHT_GREEN%>;             // #F3F9BD;
	color           : <%=BODY_DEFAULT_FONT_COLOR%>;
	font-size       : <%=FONT_SIZE_NORMAL%>;
	font-weight     : bold;
	text-align      : left;
	vertical-align  : middle;
	}

.attTitleValue {
	background      : <%=LIGHT_GREEN%>;
	color           : <%=BODY_DEFAULT_FONT_COLOR%>;
	font-size       : <%=FONT_SIZE_NORMAL%>;
	font-weight     : bold;
	text-align      : left;
	vertical-align  : middle;
	}
	
.attTitleValueHighlight {
	background      : <%=LIGHT_GREEN%>;
	color           : <%=RED%>;
	font-size       : <%=FONT_SIZE_NORMAL%>;
	font-weight     : bold;
	text-align      : left;
	vertical-align  : middle;
	}
	
.profile {
	color           : <%=BODY_DEFAULT_FONT_COLOR%>;
	display         : inline;
	font-size       : <%=FONT_SIZE_SMALL%>;
	font-weight     : normal;
	line-height     : <%=LINE_HEIGHT_EXPANDED%>;
	}

.profileHighlight {
	color           : <%=RED%>;
	display         : inline;
	font-size       : <%=FONT_SIZE_SMALL%>;
	font-weight     : bold;
	line-height     : <%=LINE_HEIGHT_EXPANDED%>;
	}

.nohighlight {
	color           : <%=BODY_DEFAULT_FONT_COLOR%>;
	font-weight     : normal;
	}
	
.highlight {
	color           : <%=RED%>;
	font-weight     : bold;
	}
	
#rightsidebar {
	vertical-align  : top;
	}
	
#rightsidebar img.padded	{
	border          : 0;
	display         : block;
	margin          : 5px;
/*	padding         : 10px; */
	text-align      : center;
	}

#rightsidebar a {
	font-size       : <%=FONT_SIZE_SMALL%>;
	font-style      : normal;
	line-height     : <%=LINE_HEIGHT_NORMAL%>;   
	margin          : 2px 3px 0 0;
/*	padding         : 0 3px 2px 0; */
	text-align      : center;
	}

#rightsidebar .topmost {
	margin-top      : 30px;
	}
	
#rightsidebar h5	{
	font-size       : <%=FONT_SIZE_SMALLER%>;
	font-weight     : normal;
	font-style      : italic;
	margin          : 0 3px 0 0;
	padding         : 0 3px 0 0;
	text-align      : right;
	}

#rightsidebar ul {
	margin          : 25px 1em 1em 15px;
	line-height     : 1.5;
}

form {
	margin: 0 0 2px 0;
	}

form p {
	font-size       : <%=FONT_SIZE_SMALLER%>;
	font-weight     : normal;
	margin          : 0 0 1em 0;
	}

.form-button {
	background-color: <%=LIGHT_PURPLE%>;            // #ccccff;
	border-color    : <%=LIGHT_PURPLE%>;
	font-size       : <%=FONT_SIZE_SMALL%>;         // 10px;
	font-weight     : normal;
	border-width    : 1px;
	margin-top      : 2px;
	}

.form-background {
	background-color: <%=LIGHT_GRAY_BLUE%>;         // formerly CCRP SAND
	border-color    : <%=LIGHT_PURPLE%>;            // formerly CCRP DARK_OLIVE
	font-family     : <%=ALT_FONT_FAMILY%>;         // Verdana, Geneva, Arial, Helvetica, sans-serif;
	font-size       : <%=FONT_SIZE_SMALL%>;         // 10px;
	border-width    : 1px;
	margin-top      : 2px;
	}

ol, ul {
	font-size       : <%=FONT_SIZE_SMALL%>;
	}

ul.suggestions {
	color           : <%=MEDIUM_GRAY%>;
	font-style      : italic;
	font-size       : <%=FONT_SIZE_SMALL%>;
	}


#bodyContainer {
	position		: absolute;
	top				: 135;
	z-index			: 1;
}	

	
.menu {
  	font-family	: Arial, Helvetica, sans-serif;
  	font-size		: 0.8em;
  	font-style		: normal;
  	font-weight	: normal;
  	visibility		: hidden;
  	position		: absolute;
  	z-index			: 100;
  	top				: -2;
 	outline-width	: 1px; 
	outline-style	: solid; 
	outline-color	: <%=MEDIUM_BLUE%>; 
  	border			: 1 solid  <%=MEDIUM_BLUE%>;
}

.item {
	outline-width	: 1; 
	outline-style	: solid; 
	outline-color	: <%=MEDIUM_BLUE%>; 
	border			: 1 solid  <%=MEDIUM_BLUE%>;
	background-color: #E6E6FA;
	padding			: 2 2 2 2;
	margin			: 0 0 0 0;
}

.item_right {
	outline-width	: 1; 
	outline-style	: solid; 
	outline-color	: <%=MEDIUM_BLUE%>; 
	border			: 1 solid  <%=MEDIUM_BLUE%>;
	background-color: #E6E6FA;
	padding			: 2 2 2 2;
	margin			: 0 0 0 0;
	text-align    : right;
}


