<?xml version="1.0"?>

<xsl:stylesheet version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:import href="ARTDashBoard.xsl"    />
<xsl:import href="ARTLeftPanel.xsl" />
<xsl:import href="ARTBody.xsl"      />
<xsl:import href="ARTRightPanel.xsl"/>
<xsl:import href="ARTFooter.xsl"    />

<xsl:variable name="Payload" select="/Page/Payload"/>

<xsl:output version="1.0" method="html" encoding="ISO-8859-1" indent="yes"/>

<xsl:template match="/">
  <xsl:apply-templates select="Page"/>
</xsl:template>

<xsl:template match="Page">
  <html>
    <xsl:call-template name="setHtmlHead"/>
    <xsl:call-template name="setHtmlBody"/>
  </html>
</xsl:template>

<xsl:template name="setHtmlHead">
<head>
 <xsl:call-template name="setHtmlHeadTitle"  />
 <meta content="IE=edge" http-equiv="X-UA-Compatible"/>
 <meta name="author" content="Will Webb"/>
 <link rel="stylesheet" type="text/css" href="css/default.css" title="default" media="screen"/>
 <link rel="stylesheet" type="text/css" href="css/default.css" title="print" media="print"/>
</head>
</xsl:template>

<!--
This is the main html section where we output the body tag, along with the
primary table that provides the header/left/body/right/footer structure
-->
<xsl:template name="setHtmlBody">
<body id="artclient" class="homepage">
<div id="container">

<div id="header">
<xsl:apply-templates select="DashBoard"/>
</div>
<hr class="hide"/>
<div id="mBody">
<xsl:apply-templates select="LeftPanel"/>
<xsl:apply-templates select="Body"/>
</div>
</div>
</body>
</xsl:template>

<!-- Sets the head/title element value -->
<xsl:template name="setHtmlHeadTitle">
  <title>Pulse</title>
</xsl:template>

<xsl:template match="DashBoard">
  <xsl:call-template name="DashBoardTemplate">
      <xsl:with-param name="panelnode" select="/Page/DashBoard" />
  </xsl:call-template>
</xsl:template>
    
<xsl:template match="LeftPanel">
  <xsl:call-template name="LeftPanelTemplate">
    <xsl:with-param name="panelnode" select="/Page/LeftPanel" />
  </xsl:call-template>
</xsl:template>

<xsl:template match="Body">
  <xsl:call-template name="BodyTemplate">
    <xsl:with-param name="panelnode" select="/Page/Body" />
  </xsl:call-template>
</xsl:template>

<xsl:template match="Footer">
  <xsl:call-template name="FooterTemplate">
    <xsl:with-param name="panelnode" select="/Page/Footer" />
  </xsl:call-template>
</xsl:template>

</xsl:stylesheet>
