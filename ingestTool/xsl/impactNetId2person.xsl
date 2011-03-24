<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
    <xsl:output indent="yes"/>
    
    <xsl:template match="/">    
        <impact_reports>
            <!-- Get all depth 2 nodes with non-empty <NetId> elements -->                  
            <xsl:apply-templates select="//child::node()/child::node()[not ( empty (./NetId))]" />
        </impact_reports>
    </xsl:template>

    <!-- used to match all the elements with the netid as the local name -->
    <xsl:template match="*">
        <xsl:call-template name="report"/>
    </xsl:template>    

    <!-- Just put in a <report> element and copy everything in this node to the output -->
    <xsl:template name="report">
        <report>
            <xsl:copy-of select="./child::node()"/>            
        </report>
    </xsl:template>    

</xsl:stylesheet>