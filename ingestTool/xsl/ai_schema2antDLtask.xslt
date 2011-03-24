<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"  
    xmlns:xlink="http://www.w3.org/1999/xlink" 
    version="2.0">
    
    <xsl:output method="xml" indent="yes"/>
    
    <!-- 
     This is a XSLT to transform the result of the Activity Insight SchemaIndex document
     into a ant build.xml with a task to download all of the departments into seperate files.
    -->
    
    <!-- these are supplied by the ant build.xml -->
    <xsl:param name="username"/>
    <xsl:param name="password"/>
    <xsl:param name="outdir"/>
    
    <xsl:template match="/">
        <project>
            <property name='site' value='http://www.digitalmeasures.com'/>
            <xsl:element name="property">
                <xsl:attribute name="name">ai_username</xsl:attribute>
                <xsl:attribute name="value"><xsl:value-of select="$username"/></xsl:attribute>
            </xsl:element>
            <xsl:element name="property">
                <xsl:attribute name="name">ai_password</xsl:attribute>
                <xsl:attribute name="value"><xsl:value-of select="$password"/></xsl:attribute>
            </xsl:element>
            <xsl:element name="property">
                <xsl:attribute name="name">output_dir</xsl:attribute>
                <xsl:attribute name="value"><xsl:value-of select="$outdir"/></xsl:attribute>
            </xsl:element>            

            <xsl:element name="property">
                <xsl:attribute name="name">deptDir</xsl:attribute>
                <xsl:attribute name="value"><xsl:value-of select="$outdir"/>bydept/</xsl:attribute>
            </xsl:element>            

            <xsl:element name="property">
                <xsl:attribute name="name">collegeDir</xsl:attribute>
                <xsl:attribute name="value"><xsl:value-of select="$outdir"/>bycollege/</xsl:attribute>
            </xsl:element>                        

            <target name="download_ai_departments">
                <mkdir dir="${{deptDir}}"/>
                <mkdir dir="${{collegeDir}}"/>
                <xsl:apply-templates select="/Indexes/Index[@indexKey = 'DEPARTMENT']/IndexEntry"/>
                <xsl:apply-templates select="/Indexes/Index[@indexKey = 'COLLEGE']/IndexEntry"/>
            </target>
        </project>
    </xsl:template>
    
    <xsl:template match="/Indexes/Index[@indexKey = 'DEPARTMENT']/IndexEntry" >
        <xsl:element name="get" namespace="">
            <xsl:attribute name="src">${site}<xsl:value-of select="Data/@xlink:href"/></xsl:attribute>
            <xsl:attribute name="dest">${deptDir}<xsl:value-of select="replace( @entryKey, '\W','')"/>.xml</xsl:attribute>
            <xsl:attribute name="username">${ai_username}</xsl:attribute>
            <xsl:attribute name="password">${ai_password}</xsl:attribute>
            <xsl:attribute name="ignoreerrors">true</xsl:attribute>
        </xsl:element>                   
    </xsl:template>   

    <xsl:template match="/Indexes/Index[@indexKey = 'COLLEGE']/IndexEntry" >
        <xsl:element name="get" namespace="">
            <xsl:attribute name="src">${site}<xsl:value-of select="Data/@xlink:href"/></xsl:attribute>
            <xsl:attribute name="dest">${collegeDir}<xsl:value-of select="replace( @entryKey, '\W','')"/>.xml</xsl:attribute>
            <xsl:attribute name="username">${ai_username}</xsl:attribute>
            <xsl:attribute name="password">${ai_password}</xsl:attribute>
            <xsl:attribute name="ignoreerrors">true</xsl:attribute>
        </xsl:element>                   
    </xsl:template>   
    
</xsl:stylesheet>