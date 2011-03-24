<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" 
                xmlns:pm="http://vivo.library.cornell.edu/ns/0.1/pubmed/">
  <!-- Namespace prefix for predicates. Needs a corresponding xmlns
       declaration in the xsl:stylesheet start-tag above. If your set
       of predicates come from more than one namespace, than this
       stylesheet is too simple for your needs. -->
  <xsl:variable name="nsPrefix">pm</xsl:variable>

 <xsl:variable name="resourceURL">
    <xsl:text>http://vivo.library.cornell.edu/ns/0.1/pubmed/100</xsl:text>
  </xsl:variable>

  <!-- Elements to suppress. priority attribute necessary 
       because of template that adds rdf:parseType above. -->
  <xsl:template priority="1" match="Request|TotalResults|TotalPages"/>

  <!-- Just pass along contents without tags.  -->
  <xsl:template match="ProductInfo|Details">
    <xsl:apply-templates/>
  </xsl:template>

  <!-- try to fix dates up just a little -->
  <xsl:template priority="1" match="PubMedPubDate">
    <xsl:element name="{$nsPrefix}:{@PubStatus}Date">
      <xsl:attribute name="rdf:parseType">Resource</xsl:attribute>
      <xsl:apply-templates/>
      <xsl:element name="{$nsPrefix}:reconstructedDate">
        <xsl:value-of select="Year"/>-<xsl:value-of select="Month"/>-<xsl:value-of select="Day"/>
      </xsl:element>
    </xsl:element>   
  </xsl:template>

  <xsl:template priority="2" select="MedlineCitation[count(@*) > 0 and count(*) > 0]">
    <xsl:element name="{$nsPrefix}:{name()}">
      <xsl:attribute name="rdf:about"><xsl:value-of select="PMID"/></xsl:attribute>
      <xsl:apply-templates select="@*[name() != 'PMID']"/>
      <xsl:apply-templates select="node()"/>
    </xsl:element>
  </xsl:template>

  <!-- ========================================================
       End of template rules addressing specific element types.
       Remaining template rules are generic xml2rdf template rules. 
       ======================================================== -->

  <xsl:template match="/">
    <rdf:RDF>
      <rdf:Description
       rdf:about="{$resourceURL}">
        <xsl:apply-templates/>
      </rdf:Description>
    </rdf:RDF>
  </xsl:template>


  <!-- Container elements: if the element has children and an element parent 
       (i.e. it isn't the root element) and it has no attributes, add
       rdf:parseType = "Resource". -->

  <xsl:template match="*[* and ../../* and not(@*)]">
    <xsl:element name="{$nsPrefix}:{name()}">
      <xsl:attribute name="rdf:parseType">Resource</xsl:attribute>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:element>
  </xsl:template>


  <!-- Copy remaining elements but no attributes, putting them in a namespace. -->
  <xsl:template match="*[count(@*) = 0  and count(@*) = 0]">
    <xsl:element name="{$nsPrefix}:{name()}">
      <xsl:apply-templates select="node()"/>
    </xsl:element>
  </xsl:template>


  <!-- deal with elements with no subElements but more than zero attributes. -->
  <xsl:template match="*[count(@*) > 0 and count(*)=0]">
    <xsl:element name="{$nsPrefix}:{name()}">
      <xsl:attribute name="rdf:parseType">Resource</xsl:attribute>

      <xsl:for-each select=".">
        <xsl:element name="rdf:value">
          <xsl:value-of select="."/>
        </xsl:element>
      </xsl:for-each>

      <xsl:for-each select="@*">
        <xsl:element name="{$nsPrefix}:{name()}">
          <xsl:value-of select="."/>
        </xsl:element>
      </xsl:for-each>

    </xsl:element>
  </xsl:template>

     
  <!-- deal with elements with subElements and attributes. -->
  <xsl:template priority="3" match="*[count(@*) > 0 and count(*) > 0 ]">
    <xsl:element name="{$nsPrefix}:{name()}">      
      <xsl:attribute name="rdf:parseType">Resource</xsl:attribute>
      <xsl:apply-templates select="node()"/>

      <xsl:for-each select="@*">
        <xsl:element name="{$nsPrefix}:{name()}">
          <xsl:value-of select="."/>
        </xsl:element>
      </xsl:for-each>          

    </xsl:element>
  </xsl:template>


  <!-- deal with elements with only subElements and no attributes. -->
  <xsl:template priority="3" match="*[count(@*) = 0 and count(*) > 0 ]">
    <xsl:element name="{$nsPrefix}:{name()}">      
      <xsl:attribute name="rdf:parseType">Resource</xsl:attribute>
      <xsl:apply-templates select="node()"/>
    </xsl:element>
  </xsl:template>

</xsl:stylesheet>
