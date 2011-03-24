<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    xmlns:mf="http://vitro.mannlib.cornell.edu/XSL/functions"
    xmlns:wos="http://ingest.mannlib.cornell.edu/wos/0.1/"
    xmlns:vitro="http://vitro.mannlib.cornell.edu/ns/vitro/0.7#"
    version="2.0">    
<xsl:output method="xml" indent="yes"/>    
    <!--
        XSLT to produce RDF/XML from XML.  This template does not handle
        narative content.
        
        This uses XSLT 2.0 and XPath 2.0 so use Saxon.
        Here is an example of how to do use XSLT 2.0 in ant:
        
        <xslt style="${ingest.dir}/xsl/activityInsight2n3Rdf.xsl" 
            in ="${ingest.dir}/ai_xml/document1.xml"
            out="${ingest.dir}/ai_xml/document1.n3"      
            <classpath location="${ingest.dir}/lib/saxon8.jar" /> 
        
        2009 Mann Library, Brian Caruso.
    -->
    
    <!-- if you use a prefix here it should exist in the NS declerations -->
    <xsl:variable name="defaultNS">wos:</xsl:variable>
    
    <xsl:template match="/">
      <rdf:RDF
         xmlns:wos="http://ingest.mannlib.cornell.edu/wos/0.1/"
         xmlns:vitro="http://vitro.mannlib.cornell.edu/ns/vitro/0.7#">
        <xsl:apply-templates mode="rdfDescription" select="*"/>
      </rdf:RDF>
    </xsl:template>
    
    <!-- create description elements -->
    <xsl:template mode="rdfDescription" match="*">        
      <rdf:Description>        
        <rdf:type><xsl:value-of select="concat($defaultNS,local-name())"/></rdf:type>
        <xsl:apply-templates select="*|@*"/>
      </rdf:Description>
    </xsl:template>   
    
    <!-- recursive tempate to turn elements into bnodes -->
    <xsl:template match="*">
        <xsl:element name="{$defaultNS}{local-name()}" >
            <rdf:Description>
            <xsl:apply-templates select="*|@*"/>
                
            <xsl:if test="not(*) and string-length(text())>0">
                    <vitro:value><xsl:copy-of select="text()"/></vitro:value>
            </xsl:if>
                        
            <xsl:if test="parent::authors">
                <wos:authorPosition><xsl:value-of select="position()"/></wos:authorPosition>
            </xsl:if>
            </rdf:Description>
        </xsl:element>
    </xsl:template>
    
    <!-- Match all leaf elements that have attributes and turn them into bnodes -->
    <xsl:template  match="*[not(*) and @* and string-length(text())>0]">        
        <xsl:element name="{$defaultNS}{name()}">
            <rdf:Description>
                <xsl:apply-templates  select="@*"/>
                <vitro:value>
                    <xsl:value-of select="."/>
                </vitro:value>
            </rdf:Description>
        </xsl:element>
    </xsl:template>
    
    <!-- Match all leaf elements and attributes and turn them into data properties. -->
    <xsl:template match="@*|*[not(*) and not(@*) and string-length(text())>0]">        
        <xsl:element name="{$defaultNS}{name()}">
            <xsl:value-of select="."/>
        </xsl:element>
    </xsl:template>
        
</xsl:stylesheet>
