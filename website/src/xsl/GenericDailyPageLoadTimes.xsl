<?xml version="1.0"?>

<xsl:stylesheet version="1.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="Payload" mode="applyFirst">
  <xsl:variable name="sMonth">
    <xsl:value-of select="format-number(Calendar/CalendarBean/CurrentMonthNumber,'00')"/>
  </xsl:variable>
  <xsl:variable name="sDay">
    <xsl:value-of select="format-number(Calendar/CalendarBean/CurrentDate,'00')"/>
  </xsl:variable>
  <xsl:variable name="sCurrentDate">
    <xsl:value-of select="concat(Calendar/CalendarBean/CurrentYear,$sMonth,$sDay)"/>
  </xsl:variable>

              <TR> 
                <TH>
                  Rank 
                </TH>
                <TH>
                    <a>
                        <xsl:attribute name="href"><xsl:value-of select="concat('./ViewDailyPageLoadChartsContext.web?orderby=Context&amp;selectedDate=',$sCurrentDate)"/>
                        </xsl:attribute>
                        Context
                    </a>
                </TH>
                <TH>
                    <a>
                        <xsl:attribute name="href"><xsl:value-of select="concat('./ViewDailyPageLoadChartsPage.web?orderby=Page&amp;selectedDate=',$sCurrentDate)"/>
                        </xsl:attribute>
                        Page
                    </a>
                </TH>
                <TH>
                <B>
                    <a>
                        <xsl:attribute name="href"><xsl:value-of select="concat('./ViewDailyPageLoadCharts.web?orderby=CPUTime&amp;selectedDate=',$sCurrentDate)"/>
                        </xsl:attribute>
                       Total CPU/Wait-Queue Minutes 
                    </a>
                </B>
                </TH>
                <TH>
                    <a>
                        <xsl:attribute name="href"><xsl:value-of select="concat('./ViewDailyPageLoadChartsLoads.web?orderby=Loads&amp;selectedDate=',$sCurrentDate)"/>
                        </xsl:attribute>
                       Total Loads 
                    </a>
                </TH>
                <TH>
                    <a>
                        <xsl:attribute name="href"><xsl:value-of select="concat('./ViewDailyPageLoadChartsAvg.web?orderby=Avg&amp;selectedDate=',$sCurrentDate)"/>
                        </xsl:attribute>
                       Avg Load Time 
                    </a>
                </TH>
                <TH>
                    <a>
                        <xsl:attribute name="href"><xsl:value-of select="concat('./ViewDailyPageLoadChartsMax.web?orderby=&amp;selectedDate=',$sCurrentDate)"/>
                        </xsl:attribute>
                       Max Load Time 
                    </a>
                </TH>
                <TH>
                    <a>
                        <xsl:attribute name="href"><xsl:value-of select="concat('./ViewDailyPageLoadCharts90percentile.web?orderby=90percentile&amp;selectedDate=',$sCurrentDate)"/>
                        </xsl:attribute>
                       90th % 
                    </a>
                </TH>
                <TH>
                    <a>
                        <xsl:attribute name="href"><xsl:value-of select="concat('./ViewDailyPageLoadCharts75percentile.web?orderby=75percentile&amp;selectedDate=',$sCurrentDate)"/>
                        </xsl:attribute>
                       75th % 
                    </a>
                </TH>
                <TH>
                    <a>
                        <xsl:attribute name="href"><xsl:value-of select="concat('./ViewDailyPageLoadCharts50percentile.web?orderby=50percentile&amp;selectedDate=',$sCurrentDate)"/>
                        </xsl:attribute>
                       50th % 
                    </a>
                </TH>
                <TH>
                    <a>
                        <xsl:attribute name="href"><xsl:value-of select="concat('./ViewDailyPageLoadCharts25pecentile.web?orderby=25pecentile&amp;selectedDate=',$sCurrentDate)"/>
                        </xsl:attribute>
                       25th % 
                    </a>
                </TH>
              </TR>
  </xsl:template>

  <xsl:template match="DailyPageSummary/hibernate-custom" mode="data">
       <xsl:if test="position() mod 2 =0">
       <tbody class="two">
            <TR>
                <TD>
                <xsl:value-of select="position()"/>
                </TD>
                <TD>
                <xsl:value-of select="ContextBean/contextName"/>
                </TD>
                <TD>
                <xsl:value-of select="PagBean/pageName"/>
                </TD>
                <TD>
                <xsl:value-of select="format-number(DailyPageLoadTimBean/totalLoads * DailyPageLoadTimBean/averageLoadTime div 60000, '###,###.0')"/>
                </TD>
                <TD>
                <xsl:value-of select="format-number(DailyPageLoadTimBean/totalLoads, '##,###,###')"/>
                </TD>
                <TD>
                <xsl:value-of select="format-number(DailyPageLoadTimBean/averageLoadTime div 1000, '###,###.00')"/>
                </TD>
                <TD>
                <xsl:value-of select="format-number(DailyPageLoadTimBean/maxLoadTime div 1000, '###,##0.00')"/>
                </TD>
                <TD>
                <xsl:value-of select="format-number(DailyPageLoadTimBean/ninetiethPercentile div 1000, '###,##0.00')"/>
                </TD>
                <TD>
                <xsl:value-of select="format-number(DailyPageLoadTimBean/seventyFifthPercentile div 1000, '###,##0.00')"/>
                </TD>
                <TD>
                <xsl:value-of select="format-number(DailyPageLoadTimBean/fiftiethPercentile div 1000, '###,##0.00')"/>
                </TD>
                <TD>
                <xsl:value-of select="format-number(DailyPageLoadTimBean/twentyFifthPercentile div 1000, '###,##0.00')"/>
                </TD>
           </TR>
       </tbody>
              </xsl:if>
       <xsl:if test="position() mod 2 =1">
       <tbody class="one">
            <TR>
                <TD>
                <xsl:value-of select="position()"/>
                </TD>
                <TD>
                <xsl:value-of select="ContextBean/contextName"/>
                </TD>
                <TD>
                <xsl:value-of select="PagBean/pageName"/>
                </TD>
                <TD>
                <xsl:value-of select="format-number(DailyPageLoadTimBean/totalLoads * DailyPageLoadTimBean/averageLoadTime div 60000, '###,###.0')"/>
                </TD>
                <TD>
                <xsl:value-of select="format-number(DailyPageLoadTimBean/totalLoads, '##,###,###')"/>
                </TD>
                <TD>
                <xsl:value-of select="format-number(DailyPageLoadTimBean/averageLoadTime div 1000, '###,##0.00')"/>
                </TD>
                <TD>
                <xsl:value-of select="format-number(DailyPageLoadTimBean/maxLoadTime div 1000, '###,##0.00')"/>
                </TD>
                <TD>
                <xsl:value-of select="format-number(DailyPageLoadTimBean/ninetiethPercentile div 1000, '###,##0.00')"/>
                </TD>
                <TD>
                <xsl:value-of select="format-number(DailyPageLoadTimBean/seventyFifthPercentile div 1000, '###,##0.00')"/>
                </TD>
                <TD>
                <xsl:value-of select="format-number(DailyPageLoadTimBean/fiftiethPercentile div 1000, '###,##0.00')"/>
                </TD>
                <TD>
                <xsl:value-of select="format-number(DailyPageLoadTimBean/twentyFifthPercentile div 1000, '###,##0.00')"/>
                </TD>
           </TR>
       </tbody>
              </xsl:if>
  </xsl:template>
</xsl:stylesheet>
