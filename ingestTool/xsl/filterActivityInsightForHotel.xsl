<?xml version="1.0" encoding="iso-8859-1"?>
<!-- Example of a XSLT that filters out nodes -->
<!-- for use with Activity Insight data -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:dm="http://www.digitalmeasures.com/schema/data"
    xmlns:dmd="http://www.digitalmeasures.com/schema/data-metadata" version="2.0">
    <xsl:output indent="yes"/>

    <xsl:template match="/">
        <dm:Data>
        <xsl:apply-templates select="dm:Data/dm:Record"/>
        </dm:Data>
    </xsl:template>

    <xsl:template match="dm:Data/dm:Record">        
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates select="*" mode="topLevel"/>
        </xsl:copy>        
    </xsl:template>

    <!-- 2009-07-02: NOT copying the following node types: 
            ACADVISE, ADMIN_ASSIGNMENTS, ADMIN_CAL, ADMIN_PERM, 
            CONSULT, DSL, FACDEV, FARNARRATIVE, LICCERT, PASTHIST, SCHTEACH -->
    <!-- 2009-07-02: other likely candidates to remove: EXTENSION_GOAL, INTELLPROP -->
    
    <!-- note there can only be 1 of these elements -->
    <xsl:template mode="topLevel"
        match="dmd:IndexEntry | dm:ADMIN | dm:AWARDHONOR | dm:CHRESEARCH | dm:CONGRANT | dm:EDITCHAIR | dm:EDUCATION |
               dm:IMPACT_STATEMENT | dm:INTELLCONT | dm:INTELLCONT_JOURNAL | dm:MEDCONT | dm:MEMBER | dm:NCTEACH |
               dm:OUTREACH_STATEMENT | dm:PCI | dm:PERFORM_EXHIBIT | dm:POSITIONS | dm:PRESENT | dm:RESEARCH_STATEMENT |
               dm:RESPROG | dm:SERVICE_COLLEGE | dm:SERVICE_DEPARTMENT | dm:SERVICE_PROFESSIONAL | dm:SERVICE_PUBLIC |
               dm:SERVICE_STATEMENT | dm:SERVICE_UNIVERSITY | dm:TEACHING_STATEMENT">
        <xsl:if test="dm:PUBLIC_VIEW eq 'Yes'">
            <xsl:element name="{name()}" namespace="{namespace-uri()}">
                <!-- Here we select which attributes to include -->
                <xsl:copy-of select="@id | @dmd:lastModified | @dmd:primaryKey"/>
                <!-- Here we select which nodes to include -->
                <xsl:copy-of select="node()"/>
            </xsl:element>
        </xsl:if>
        <xsl:if test="dm:PUBLIC_VIEW ne 'Yes'">
            <!--xsl:element name="{name()}" namespace="{namespace-uri()}">
                <xsl:copy-of select="@id | @dmd:lastModified | @dmd:primaryKey"/>
                <xsl:copy-of select="dm:PUBLIC_VIEW"/>
            </xsl:element-->
        </xsl:if>
        <xsl:if test="(not(dm:PUBLIC_VIEW))">
            <xsl:element name="{name()}" namespace="{namespace-uri()}">
                <xsl:copy-of select="@*"/>
                <xsl:copy-of select="node()"/>
                <dm:PUBLIC_VIEW>element missing</dm:PUBLIC_VIEW>
            </xsl:element>
        </xsl:if>
    </xsl:template>
    
    <!-- selectively copy only parts of a node -->
    <xsl:template mode="topLevel" match="dm:ADMIN[dm:PUBLIC_VIEW eq 'Yes' and dmd:primaryKey eq '2008-2009']">
        <dm:ADMIN>
            <!-- Here we select which nodes to include -->
            <xsl:copy-of select="dm:AC_YEAR | dm:ADMIN_DEP | dm:PRIORITY_AREA | dm:DISCIPLINE"/>
        </dm:ADMIN>
    </xsl:template>
    
    <xsl:template mode="topLevel" match="dm:AWARDHONOR[dm:PUBLIC_VIEW eq 'Yes']">
        <dm:AWARDHONOR>
            <!-- Here we select which attributes to include -->
            <xsl:copy-of select="@id | @dmd:lastModified"/>
            
            <xsl:copy-of select="dm:DTD_DATE | dm:DTM_DATE | dm:DTY_DATE"/>
            <xsl:copy-of select="dm:NAME | dm:ORG "/>
            <xsl:copy-of select="dm:PUBLIC_VIEW" />
        </dm:AWARDHONOR>
    </xsl:template>
    
    <xsl:template mode="topLevel" match="dm:CHRESEARCH[dm:PUBLIC_VIEW eq 'Yes' and dm:STATUS eq 'Published']">
        <dm:CHRESEARCH>
            <!-- Here we select which attributes to include -->
            <xsl:copy-of select="@id | @dmd:lastModified"/>
            
            <xsl:copy-of select="dm:PUB_TITLE"/>
            <xsl:copy-of select="dm:TYPE"/> <!-- 3 blank, 1 Case Study, 120 Center Report, 16 Other, 16 Tool, 8 Working Paper -->
            <xsl:copy-of select="dm:TYPE_OTHER"/> <!-- 145 blank, 1 Funded Research, Funded research project, 1 Grant Awarded, 1 Roundtable, 4 Roundtable Proceedings, etc. -->
            <xsl:copy-of select="dm:STATUS | dm:VOLUME | dm:NBR"/>
            <xsl:copy-of select="dm:DTY_PUB"/>
            <xsl:copy-of select="CHRESEARCH_AUTH"/>          
        </dm:CHRESEARCH>
    </xsl:template>
    
    <xsl:template mode="topLevel" match="dm:CONGRANT[dm:PUBLIC_VIEW eq 'Yes']">
        <dm:CONGRANT>
            <!-- Here we select which attributes to include -->
            <xsl:copy-of select="@id | @dmd:lastModified"/>
            
            <xsl:copy-of select="dm:DTY_START | dm:DTY_END"/>
            <xsl:copy-of select="dm:TYPE"/> <!-- 1 Contract, 105 Grant, 68 Sponsored Research -->
            <xsl:copy-of select="dm:AWARDORG"/> <!-- 170 blank, 17 Cornell University, 2 Federal, 2 Private -->
            <xsl:copy-of select="dm:TITLE | dm:SPONORG | dm:PUBLIC_VIEW"/>
            <xsl:copy-of select="dm:CONGRANT_INVEST"/>
        </dm:CONGRANT>
    </xsl:template>
    
    <xsl:template mode="topLevel" match="dm:EDITCHAIR[dm:PUBLIC_VIEW eq 'Yes']">
        <dm:EDITCHAIR>
            <!-- Here we select which attributes to include -->
            <xsl:copy-of select="@id | @dmd:lastModified"/>
            
            <!-- Here we select which nodes to include -->
            <xsl:copy-of select="dm:DTY_START | dm:DTY_END"/>
            <xsl:copy-of select="dm:TYPE | dm:TITLE | dm:ROLE | dm:ROLEOTHER | dm:DESC | dm:CONF_TOPIC | dm:PUBLIC_VIEW"/>
        </dm:EDITCHAIR>
    </xsl:template>
    
    <!-- selectively copy only parts of a node -->
    <xsl:template mode="topLevel" match="dm:EDUCATION[dm:PUBLIC_VIEW eq 'Yes']">
        <dm:EDUCATION>
            <!-- Here we select which attributes to include -->
            <xsl:copy-of select="@id | @dmd:lastModified"/>
            
            <!-- Here we select which nodes to include -->
            <xsl:copy-of select="dm:DTY_START | DTY_END"/>
            <xsl:copy-of select="dm:DEG | dm:DEGOTHER | dm:SCHOOL | dm:LOCATION | dm:MAJOR | dm:SUPPAREA"/>
            <xsl:copy-of select="dm:HIGHEST | dm:YR_COMP | dm:PUBLIC_VIEW"/>
        </dm:EDUCATION>
    </xsl:template>

    <xsl:template mode="topLevel" match="dm:IMPACT_STATEMENT[dm:PUBLIC_VIEW eq 'Yes']">
        <dm:IMPACT_STATEMENT>
            <!-- Here we select which attributes to include -->
            <xsl:copy-of select="@id | @dmd:lastModified | @dmd:primaryKey"/>
            
            <!-- Here we select which nodes to include -->
            <xsl:copy-of select="dm:DTM_START | dm:DTD_START| dm:DTY_START | dm:DTM_END | dm:DTD_END | dm:DTY_END"/>
            <xsl:copy-of select="dm:START_START | END_END"/>
            <xsl:copy-of select="dm:TITLE | dm:CONAREA | dm:RCHTYPE | dm:PRIORITY_AREA | dm:PRIORITY_AREA_OTHER"/>
            <xsl:copy-of select="dm:INVOLVED_STATE | dm:INVOLVED_COUNTY | dm:INVOLVED_COUNTRY | dm:OTHER_COUNTRIES"/>
            <xsl:copy-of select="dm:USDA_AREA | dm:USDA_AREAOTHER"/>
            <xsl:copy-of select="dm:SUMMARY | dm:ISSUE | dm:RESPONSE | dm:IMPACT"/>
            <xsl:copy-of select="dm:FUNDING_FEDRCH | dm:FUNDING_FEDRCHOTHER | dm:FUNDING_FEDEXT | dm:FUNDING_FEDEXTOTHER | dm:FUNDING_ACAD | dm:FUNDING_ACADOTHER"/>
            <xsl:copy-of select="dm:FUNDING_USDA | dm:FUNDING_OTHERFED | dm:FUNDING_STATE | dm:FUNDING_PRIVATE | dm:FUNDING_PRIVATEOTHER"/>
            <xsl:copy-of select="dm:IMPACT_STATEMENT_ENTITY | dm:IMPACT_STATEMENT_INVEST"/>
            <xsl:copy-of select="dm:PUBLIC_VIEW"/>
        </dm:IMPACT_STATEMENT>
    </xsl:template>
    
    <xsl:template mode="topLevel" match="dm:INTELLCONT[dm:PUBLIC_VIEW eq 'Yes' and dm:STATUS eq 'Published']">
        <dm:INTELLCONT>
            <!-- Here we select which attributes to include -->
            <xsl:copy-of select="@id | @dmd:lastModified"/>
            
            <!-- Here we select which nodes to include -->
            <xsl:copy-of select="dm:DTM_PUB | dm:DTD_PUB| dm:DTY_PUB | dm:PUB_START | dm:PUB_END"/>
            <xsl:copy-of select="dm:CONAREA | dm:CONTYPE | dm:CONTYPEOTHER | dm:CLASSIFICATION"/>
            <xsl:copy-of select="dm:TITLE | dm:BOOK_TITLE | dm:EDITORS | dm:PUBLISHER | dm:PUBCTYST | dm:PUBCNTRY"/>
            <xsl:copy-of select="dm:STATUS | dm:REFEREEED | dm:PUBLIC_VIEW | dm:PUBLICAVAIL | dm:VOLUME | dm:NBR | dm:ISSUE"/>
            <xsl:copy-of select="dm:INTELLCONT_AUTH"/>          
        </dm:INTELLCONT>
    </xsl:template>
    
    <xsl:template mode="topLevel" match="dm:INTELLCONT_JOURNAL[dm:PUBLIC_VIEW eq 'Yes' and dm:STATUS eq 'Published']">
        <dm:INTELLCONT_JOURNAL>
            <!-- Here we select which attributes to include -->
            <xsl:copy-of select="@id | @dmd:lastModified"/>
            
            <!-- Here we select which nodes to include -->
            <xsl:copy-of select="dm:DTM_PUB | dm:DTD_PUB| dm:DTY_PUB | dm:PUB_START | dm:PUB_END"/>
            <xsl:copy-of select="dm:CONAREA | dm:CONTYPE | dm:CONTYPEOTHER | dm:CLASSIFICATION"/>
            <xsl:copy-of select="dm:TITLE | dm:JOURNAL_NAME | dm:JOURNAL_NAME_OTHER | dm:ISBNISSN | dm:EDITORS | dm:PUBLISHER | dm:PUBCTYST | dm:PUBCNTRY"/>
            <xsl:copy-of select="dm:STATUS | dm:REFEREEED | dm:PUBLIC_VIEW | dm:PUBLICAVAIL | dm:VOLUME | dm:ISSUE | dm:PAGENUM"/>
            <xsl:copy-of select="dm:INTELLCONT_JOURNAL_AUTH"/>   
        </dm:INTELLCONT_JOURNAL>
    </xsl:template>

    <xsl:template mode="topLevel" match="dm:MEDCONT[dm:PUBLIC_VIEW eq 'Yes']">
        <dm:MEDCONT>
            <!-- Here we select which attributes to include -->
            <xsl:copy-of select="@id | @dmd:lastModified"/>
            
            <!-- Here we select which nodes to include -->
            <xsl:copy-of select="dm:DTD_DATE | dm:DTM_DATE | dm:DTY_DATE"/>
            <xsl:copy-of select="dm:DESC | dm:NAME | dm:TYPE | dm:WEBSITE | dm:PUBLIC_VIEW"/>
        </dm:MEDCONT>
    </xsl:template>
    
    <xsl:template mode="topLevel" match="dm:MEMBER[dm:PUBLIC_VIEW eq 'Yes']">
        <dm:MEMBER>
            <!-- Here we select which attributes to include -->
            <xsl:copy-of select="@id | @dmd:lastModified"/>
            
            <!-- Here we select which nodes to include -->
            <xsl:copy-of select="dm:DTY_START | dm:DTY_END"/>
            <xsl:copy-of select="dm:NAME | dm:ORGABBR | dm:LEADERSHIP | dm:PUBLIC_VIEW"/>
        </dm:MEMBER>
    </xsl:template>
    
    <xsl:template mode="topLevel" match="dm:NCTEACH[dm:PUBLIC_VIEW eq 'Yes']">
        <dm:NCTEACH>
            <!-- Here we select which attributes to include -->
            <xsl:copy-of select="@id | @dmd:lastModified"/>
            
            <!-- Here we select which nodes to include -->
            <xsl:copy-of select="dm:DTY_START | dm:DTY_END"/>
            <xsl:copy-of select="dm:TITLE | dm:ORG | dm:PROGRAM | dm:PROGRAM_OTHER | dm:ROLE | dm:PUBLIC_VIEW"/>
        </dm:NCTEACH>
    </xsl:template>
    
    
    <!-- no special filtering needed for OUTREACH_STATEMENT -->
    
    <xsl:template mode="topLevel" match="dm:PCI[dm:PUBLIC_VIEW eq 'Yes']">
        <dm:PCI>
            <!-- Here we select which attributes to include -->
            <xsl:copy-of select="@id | @dmd:lastModified"/>
            
            <!-- Here we select which nodes to include -->
            <xsl:copy-of select="dm:PREFIX | dm:FNAME | dm:PFNAME | dm:MNAME | dm:LNAME | dm:SUFFIX"/>
            <xsl:copy-of select="dm:ALT_NAME | dm:ENDPOS"/>
            <xsl:copy-of select="dm:PCI_WEBSITE | dm:WEBSITE"/>
            <xsl:copy-of select="dm:PUBLIC_VIEW"/>
            
            <!-- Example of adding a slightly modified node to output
                <dm:OPHONE><xsl:value-of select="dm:OPHONE1"/>-<xsl:value-of select="dm:OPHONE2"/>-<xsl:value-of
                select="dm:OPHONE3"/></dm:OPHONE> -->
        </dm:PCI>
    </xsl:template>
    
    <xsl:template mode="topLevel" match="dm:PERFORM_EXHIBIT[dm:PUBLIC_VIEW eq 'Yes']">
        <dm:PERFORM_EXHIBIT>
            <!-- Here we select which attributes to include -->
            <xsl:copy-of select="@id | @dmd:lastModified"/>
            
            <!-- Here we select which nodes to include -->
            <xsl:copy-of select="dm:DTM_START | dm:DTD_START| dm:DTY_START | dm:DTM_END | dm:DTD_END | dm:DTY_END"/>
            <xsl:copy-of select="dm:START_START | END_END"/>
            <xsl:copy-of select="dm:TYPE | dm:TYPEOTHER | dm:TITLE | dm:NAME | dm:SPONSOR | dm:LOCATION"/>
            <xsl:copy-of select="dm:ACADEMIC | dm:SCOPE | dm:ACADEMIC | dm:REFEREED | dm:INVACC | dm:DELIVERY_TYPE | dm:DESC"/>
            <xsl:copy-of select="dm:PERFORM_EXHIBIT_CONTRIBUTORS"/>
        </dm:PERFORM_EXHIBIT>
    </xsl:template>

    <xsl:template mode="topLevel" match="dm:POSITIONS[dm:PUBLIC_VIEW eq 'Yes']">
        <dm:POSITIONS>
            <!-- Here we select which attributes to include -->
            <xsl:copy-of select="@id | @dmd:lastModified"/>
            
            <!-- Here we select which nodes to include -->
            <xsl:copy-of select="dm:TITLE | dm:ORG | dm:EXPTYPE"/>
            <xsl:copy-of select="dm:CITY | dm:STATE | dm:COUNTRY"/>
            <xsl:copy-of select="dm:DTM_START | dm:DTD_START | dm:DTY_START"/>
            <xsl:copy-of select="dm:DTM_END | dm:DTD_END | dm:DTY_END"/>          
        </dm:POSITIONS>
    </xsl:template>

    <xsl:template mode="topLevel" match="dm:PRESENT[dm:PUBLIC_VIEW eq 'Yes']">
        <dm:PRESENT>
            <!-- Here we select which attributes to include -->
            <xsl:copy-of select="@id | @dmd:lastModified"/>
            
            <!-- Here we select which nodes to include -->
            <xsl:copy-of select="dm:DTM_DATE | dm:DTD_DATE| dm:DTY_DATE"/>
            <xsl:copy-of select="dm:START_START | END_END"/>
            <xsl:copy-of select="dm:CONAREA | dm:NAME | dm:ORG | dm:LOCATION | dm:TITLE | dm:PRESTYPE | dm:PRESTYPE_OTHER | dm:REFEREED"/>
            <xsl:copy-of select="dm:PRESENT_AUTH"/>
        </dm:PRESENT>
    </xsl:template>
    
    <!-- no special filtering needed for RESEARCH_STATEMENT -->
    
    <xsl:template mode="topLevel" match="dm:RESPROG[dm:PUBLIC_VIEW eq 'Yes']">
        <dm:RESPROG>
            <!-- Here we select which attributes to include -->
            <xsl:copy-of select="@id | @dmd:lastModified"/>
            
            <!-- Here we select which nodes to include -->
            <xsl:copy-of select="dm:TITLE | dm:DESC | dm:STATUS | dm:RESEARCH_TYPE | dm:PUBLIC_VIEW"/>
            <xsl:copy-of select="dm:RESPROG_COLL"/>
        </dm:RESPROG>
    </xsl:template>

    <!--xsl:template mode="topLevel" match="dm:SCHTEACH[dm:PUBLIC_VIEW eq 'Yes']">
        <dm:SCHTEACH>
        <!- - Here we select which attributes to include - - >
        <xsl:copy-of select="@*"/>
        
        <!- - Here we select which nodes to include - - >
        <xsl:copy-of select="dm:TITLE | dm:DELIVERY_MODE | dm:TYPE | dm:CHOURS | dm:LEVEL"/>
        <xsl:copy-of select="dm:TYT_TERM | dm:TYY_TERM | dm:TERM_START | dm:TERM_END | dm:COURSEPRE| dm:COURSENUM"/>
        </dm:SCHTEACH>
    </xsl:template-->
    
    <xsl:template mode="topLevel" match="dm:SERVICE_COLLEGE[dm:PUBLIC_VIEW eq 'Yes']">
        <dm:SERVICE_COLLEGE>
            <!-- Here we select which attributes to include -->
            <xsl:copy-of select="@id | @dmd:lastModified"/>
            
            <!-- Here we select which nodes to include -->
            <xsl:copy-of select="dm:DTM_START | dm:DTD_START| dm:DTY_START | dm:DTM_END | dm:DTD_END | dm:DTY_END"/>
            <xsl:copy-of select="dm:START_START | END_END"/>
            <xsl:copy-of select="dm:ORG | dm:ROLE | dm:ROLEOTHER | dm:DESC | dm:PUBLIC_VIEW"/>
        </dm:SERVICE_COLLEGE>
    </xsl:template>

    <xsl:template mode="topLevel" match="dm:SERVICE_DEPARTMENT[dm:PUBLIC_VIEW eq 'Yes']">
        <dm:SERVICE_DEPARTMENT>
            <!-- Here we select which attributes to include -->
            <xsl:copy-of select="@id | @dmd:lastModified"/>
            
            <!-- Here we select which nodes to include -->
            <xsl:copy-of select="dm:DTM_START | dm:DTD_START| dm:DTY_START | dm:DTM_END | dm:DTD_END | dm:DTY_END"/>
            <xsl:copy-of select="dm:START_START | END_END"/>
            <xsl:copy-of select="dm:ORG | dm:ROLE | dm:ROLEOTHER | dm:DESC | dm:PUBLIC_VIEW"/>
        </dm:SERVICE_DEPARTMENT>
    </xsl:template>
    
    <xsl:template mode="topLevel" match="dm:SERVICE_PROFESSIONAL[dm:PUBLIC_VIEW eq 'Yes']">
        <dm:SERVICE_PROFESSIONAL>
            <!-- Here we select which attributes to include -->
            <xsl:copy-of select="@id | @dmd:lastModified"/>
            
            <!-- Here we select which nodes to include -->
            <xsl:copy-of select="dm:DTM_START | dm:DTD_START| dm:DTY_START | dm:DTM_END | dm:DTD_END | dm:DTY_END"/>
            <xsl:copy-of select="dm:START_START | END_END"/>
            <xsl:copy-of select="dm:CITY | dm:STATE | dm:COUNTRY"/>
            <xsl:copy-of select="dm:ORG | dm:ROLE | dm:ROLEOTHER | dm:DESC | dm:PUBLIC_VIEW"/>
        </dm:SERVICE_PROFESSIONAL>
    </xsl:template>

    <xsl:template mode="topLevel" match="dm:SERVICE_UNIVERSITY[dm:PUBLIC_VIEW eq 'Yes']">
        <dm:SERVICE_UNIVERSITY>
            <!-- Here we select which attributes to include -->
            <xsl:copy-of select="@id | @dmd:lastModified"/>
            
            <!-- Here we select which nodes to include -->
            <xsl:copy-of select="dm:DTM_START | dm:DTD_START| dm:DTY_START | dm:DTM_END | dm:DTD_END | dm:DTY_END"/>
            <xsl:copy-of select="dm:START_START | END_END"/>
            <xsl:copy-of select="dm:ORG | dm:ROLE | dm:DESC | dm:PUBLIC_VIEW"/>
        </dm:SERVICE_UNIVERSITY>
    </xsl:template>

    <!-- no filtering needed for TEACHING_STATEMENT -->
    
    <!-- replace default template so that we don't get all of the text() floating in the output -->
    <xsl:template match="*"/>
    <xsl:template mode="topLevel" match="*"/>
    <xsl:template mode="publicView" match="*"/>
</xsl:stylesheet>
	
	
