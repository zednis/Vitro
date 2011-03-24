<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="/">
  <html>
  <body>
    <h2>PubMed Articles</h2>
    <table border="1">
    <tr bgcolor="#9acd32">
      <th align="left">PubMed Id</th>
      <th align="left">Date</th>
      <th align="left">Title</th>
      <th align="left">First Author</th>
    </tr>
    <xsl:for-each select="PubmedArticleSet/PubmedArticle/MedlineCitation">
    <tr>
      <td><xsl:value-of select="PMID"/></td>
      <td><xsl:value-of select="DateCreated/Month"/>-<xsl:value-of select="DateCreated/Day"/>-<xsl:value-of select="DateCreated/Year"/></td>
	  <td><xsl:value-of select="Article/ArticleTitle"/></td>
	  <td>
			<xsl:value-of select="Article/AuthorList/Author"/> 
	  </td>
    </tr>
    </xsl:for-each>
    </table>
  </body>
  </html>
</xsl:template>

</xsl:stylesheet>