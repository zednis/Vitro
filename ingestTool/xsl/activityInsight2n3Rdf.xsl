<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:dm="http://www.digitalmeasures.com/schema/data" 
    xmlns:dmd="http://www.digitalmeasures.com/schema/data-metadata" 
    xmlns:mf="http://vitro.mannlib.cornell.edu/XSL/functions"
    version="2.0">
<xsl:output method="text"/>

<!--
 XSLT to produce N3 RDF from Activity Insight XML.

 This uses XSLT 2.0 and XPath 2.0 so use Saxon.
 Here is an example of how to do use XSLT 2.0 in ant:

<xslt style="${ingest.dir}/xsl/activityInsight2n3Rdf.xsl" 
   in ="${ingest.dir}/ai_xml/document1.xml"
   out="${ingest.dir}/ai_xml/document1.n3"      
   <classpath location="${ingest.dir}/lib/saxon8.jar" /> 

 Here is an example of how to run saxon from the command line:
 java -Xmx512m -jar /home/bdc34/bin/saxon9he.jar \
 -s:filteredbjltest1.xml  -xsl:xsl/activityInsight2n3Rdf.xsl  -o:filteredbjltest2.rdf
 
 2009 Mann Library, Brian Caruso.
-->

<xsl:template match="/|*">
    <xsl:apply-templates select="dm:Data"/>
</xsl:template>                
    
<xsl:template match="dm:Data">
@prefix rdf:  &lt;http://www.w3.org/1999/02/22-rdf-syntax-ns#&gt; .
@prefix rdfs: &lt;http://www.w3.org/2000/01/rdf-schema#&gt; .
@prefix owl:  &lt;http://www.w3.org/2002/07/owl#&gt; .
@prefix ar:   &lt;http://vitro.mannlib.cornell.edu/ns/reporting#&gt; .

<xsl:apply-templates select="dm:Record"/>        
</xsl:template>
    
<xsl:template match="dm:Record">
    <xsl:variable name="recordUri" select="concat('ar:',@username,@dmd:surveyId)"/>
    <xsl:value-of select="$recordUri"/> rdf:type ar:ResponseRootDataRecord .
    <xsl:apply-templates mode="allAttributesAsDataprops" select="@*">
        <xsl:with-param name="subjectUri" select="$recordUri"/>
    </xsl:apply-templates>
    <xsl:apply-templates select="*">
        <xsl:with-param name="subjectUri" select="$recordUri"/>
    </xsl:apply-templates>
</xsl:template>    

<!-- ************* General use templates for creating statements and bnodes ************* -->

    
<!-- take all leaf elements and turn them into data properties -->
<xsl:template mode="allElementsAsDataprops" match="*">
    <xsl:param name="subjectUri"/>    
    <xsl:value-of select="$subjectUri"/> <xsl:value-of select="mf:makePropertyName(namespace-uri(),local-name())"/> "<xsl:value-of select="mf:escapeForN3(text())"/>" .
</xsl:template>
        
<!-- take all attribues and turn them into data properties -->
<xsl:template mode="allAttributesAsDataprops" match="@*">
    <xsl:param name="subjectUri"/>    
    <xsl:value-of select="$subjectUri"/> <xsl:value-of select="mf:makePropertyName(namespace-uri(),local-name())"/> "<xsl:value-of select="mf:escapeForN3(.)"/>" .
</xsl:template>
    
<!-- works for a basic element with an @id and simple sub-elements -->
<xsl:template match="*[@id]">
    <xsl:param name="subjectUri"/>
    <xsl:variable name="uri" select="mf:makeIndividualUri(namespace-uri(),local-name(),@id)"/>
    <xsl:value-of select="concat($subjectUri, mf:makePropertyName(namespace-uri(),local-name()) ,$uri, ' . ')"/>    
    <xsl:apply-templates mode="allAttributesAsDataprops" select="@*">
        <xsl:with-param name="subjectUri" select="$uri"/>
    </xsl:apply-templates>      
    <xsl:apply-templates mode="allElementsAsDataprops" select="*[not(*) and string-length(text())>0]">        
        <xsl:with-param name="subjectUri" select="$uri"/>
    </xsl:apply-templates>      
    <xsl:apply-templates  select="*[*]">
        <xsl:with-param name="subjectUri" select="$uri"/>
    </xsl:apply-templates>    
    <xsl:value-of select="$uri"/> ar:positionInSequence <xsl:value-of select="position()"/> .
</xsl:template>    

<!--Deal with elements without @id attribute and no text.
    These are the dmd:IndexEntry elements.
    Just make bnodes for them.  -->
<xsl:template match="*[not(*) and not(@id) and string-length(text())=0]">
    <xsl:param name="subjectUri"/>
    <xsl:value-of select="$subjectUri"/> <xsl:value-of select="mf:makePropertyName(namespace-uri(),local-name())"/> [
      <xsl:apply-templates mode="bnodeStatements" select="@*"/>
    ] .
</xsl:template>
    
<xsl:template mode="bnodeStatements" match="@*">
    ar:<xsl:value-of select="name()"/> "<xsl:value-of select="mf:escapeForN3(.)"/>" ;
</xsl:template> 
    

<!-- funciton for use in XPath to make property names --> 
<xsl:function name="mf:makePropertyName" >
    <xsl:param name="ns" />
    <xsl:param name="localname" />
    <xsl:choose>
        <xsl:when test="string-length($ns) > 0 and string-length($localname) > 0"><xsl:value-of select="concat(' &lt;',$ns,'/',$localname,'&gt; ')"/></xsl:when>
        <xsl:when test="string-length($ns) = 0 and string-length($localname) > 0"><xsl:value-of select="concat(' ar:',$localname,' ')"/></xsl:when>
        <xsl:otherwise> ar:malformedElement </xsl:otherwise>
    </xsl:choose>
</xsl:function>

<!-- funciton for use in XPath to make URIs for individuals --> 
    <xsl:function name="mf:makeIndividualUri" >
        <xsl:param name="ns" />
        <xsl:param name="localname" />
        <xsl:param name="id" />
        <xsl:choose>
            <xsl:when test="string-length($ns) > 0 and string-length($localname) > 0"><xsl:value-of select="concat(' &lt;',$ns,'/',$localname,$id,'&gt; ')"/></xsl:when>
            <xsl:when test="string-length($ns) = 0 and string-length($localname) > 0"><xsl:value-of select="concat(' ar:',$localname,$id,' ')"/></xsl:when>
            <xsl:otherwise> ar:malformedElement </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

<!-- N3 spect on string escaping:
    Escaping in strings uses the same conventions as Python strings except 
    for a \U extension introduced by NTriples spec. N3 strings represent 
    ordered sequences of Unicode characters.
    
    Some escapes (\a, \b, \f, \v) should be avoided because the 
    corresponding characters are not allowed in RDF.
    
    Escape Sequence  	Meaning 
    \newline 	        Ignored
    \\ 	                Backslash (\)
    \' 	                Single quote (')
    \" 	                Double quote (")
    \n 	                ASCII Linefeed (LF)
    \r 	                ASCII Carriage Return (CR)
    \t 	                ASCII Horizontal Tab (TAB)
    \uhhhh 	            character in BMP with Unicode value U+hhhh
    \U00hhhhhh 	        character in plane 1-16 with Unicode value U+hhhhhh
    
    In N3, the double quote character is used for strings. The single quote character 
    is reserved for future use. The single quote character does not need to be 
    escaped in an N3 string. -->
    
<!-- python string literal documentation: http://docs.python.org/reference/lexical_analysis.html#strings -->
    
<!-- function to escape strings for N3 -->
<xsl:function name="mf:escapeForN3">
    <xsl:param name="value"/>
    <xsl:value-of select="replace(replace (replace(replace(replace(replace($value, 
        '\\','\\\\'),
        '''','\\'''),
        '&quot;','\\&quot;'),
        '\n','\\n'),
        '\r','\\r'),
        '\t', '\\t')" />     
</xsl:function>
</xsl:stylesheet>
