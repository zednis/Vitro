<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" >
    <xsl:output indent="yes"/>
    
    <xsl:template match="/">
        <xsl:apply-templates mode="copy"/>
    </xsl:template>
    
    <!-- copy everything -->
    <xsl:template match="@*|node()" mode="copy">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates mode="copy"/>
        </xsl:copy>        
    </xsl:template>
       
    <!-- add the username/netid from a parent element's attribute -->
    <xsl:template match="IMPACT_STATEMENT" mode="copy">
        <IMPACT_STATEMENT>
            <xsl:copy-of select="parent::Survey/@username"/>
            <xsl:copy-of select="./attribute::*"/>
            <xsl:copy-of select="preceding-sibling::IndexValue"/>
            <xsl:copy-of select="./child::node()"/>            
        </IMPACT_STATEMENT>
    </xsl:template>    
    
</xsl:stylesheet>
