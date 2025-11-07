<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:tei="http://www.tei-c.org/ns/1.0"
    exclude-result-prefixes="xs"
    version="2.0">
    
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>    
    
    <xsl:template match="tei:annotationBlock">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
            <xsl:if test="tei:u/tei:pc[text()='[']">
                <tei:spanGrp type="transfer">
                    <xsl:for-each select="tei:u/tei:pc[text()='[']">
                        <tei:span>
                            <xsl:attribute name="from"><xsl:value-of select="current()/following-sibling::tei:w[1]/@xml:id"/></xsl:attribute>
                            <xsl:attribute name="to">
                                <xsl:choose>
                                    <xsl:when test="current()/following-sibling::tei:pc[text()=']']">
                                        <xsl:value-of select="current()/following-sibling::tei:pc[text()=']'][1]/preceding-sibling::tei:w[1]/@xml:id"/>                                        
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="current()/following-sibling::*[@xml:id][last()]/@xml:id"/>                                                                                
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:attribute>
                            <xsl:text>en</xsl:text>
                        </tei:span>
                    </xsl:for-each>
                </tei:spanGrp>
            </xsl:if>
            <tei:spanGrp type="norm">
                <xsl:for-each select="tei:u/tei:w[matches(text(),'[A-ZÄÖÜ]{2,}')]">
                <!-- <xsl:for-each select="u/w"> -->
                    <tei:span>
                        <xsl:attribute name="from"><xsl:value-of select="@xml:id"/></xsl:attribute>
                        <xsl:attribute name="to"><xsl:value-of select="@xml:id"/></xsl:attribute>
                        <xsl:value-of select="lower-case(text())"/>
                    </tei:span>
                </xsl:for-each>
            </tei:spanGrp>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="tei:pc[text()='[' or text()=']']"/>

    <xsl:template match="tei:pc[text()='&#x160;']"/>
        
    <xsl:template match="tei:spanGrp[@type='translation']">
        <xsl:copy>
            <xsl:attribute name="xml:lang">en</xsl:attribute>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
        
    <xsl:template match="tei:spanGrp[@type='translation']/tei:span">
        <tei:span>
            <xsl:attribute name="from" select="ancestor::tei:annotationBlock/descendant::tei:*[not(*) and @xml:id and not(text()='[') and not(text()=']')][1]/@xml:id"/>
            <xsl:attribute name="to" select="ancestor::tei:annotationBlock/descendant::tei:*[not(*) and @xml:id and not(text()='[') and not(text()=']')][last()]/@xml:id"/>
            <xsl:value-of select="text()"/>
        </tei:span>
    </xsl:template>
    
</xsl:stylesheet>