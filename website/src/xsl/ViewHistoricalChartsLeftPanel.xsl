<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:import href="ARTLeftPanel.xsl" />
    <xsl:template name="drawFirstCalendarWeek">
        <xsl:param name="previousMonthLastDate" />
        <xsl:param name="previousMonthLastDayOfWeek" />
        <xsl:param name="currentDate" />
    <!--NEW WDW-->
        <xsl:param name="todaysDate" />
    <!--NEW WDW-->
        <xsl:param name="weekDayNumber" />
    <!--NEW WDW-->
        <xsl:choose>
            <xsl:when test="$previousMonthLastDate='7'">
        <!-- create week with normal template -->
            </xsl:when>
            <xsl:otherwise>
                <tr>
                    <xsl:call-template name="drawFirstCalendarWeekDay">
                        <xsl:with-param name="weekDayNumber" select="1" />
                        <xsl:with-param name="previousMonthLastDate" select="$previousMonthLastDate" />
                        <xsl:with-param name="previousMonthLastDayOfWeek" select="$previousMonthLastDayOfWeek" />
                        <xsl:with-param name="previousYYYYMM" select="concat($leftpaneldatanode/Calendar/CalendarBean/PreviousMonthYear, format-number($leftpaneldatanode/Calendar/CalendarBean/PreviousMonthNumber, '00'))" />
                        <xsl:with-param name="currentYYYYMM" select="concat($leftpaneldatanode/Calendar/CalendarBean/CurrentYear, format-number($leftpaneldatanode/Calendar/CalendarBean/CurrentMonthNumber, '00'))" />
                        <xsl:with-param name="todaysDate" select="$todaysDate" />
                    </xsl:call-template>
                    <xsl:call-template name="drawFirstCalendarWeekDay">
                        <xsl:with-param name="weekDayNumber" select="2" />
                        <xsl:with-param name="previousMonthLastDate" select="$previousMonthLastDate" />
                        <xsl:with-param name="previousMonthLastDayOfWeek" select="$previousMonthLastDayOfWeek" />
                        <xsl:with-param name="previousYYYYMM" select="concat($leftpaneldatanode/Calendar/CalendarBean/PreviousMonthYear, format-number($leftpaneldatanode/Calendar/CalendarBean/PreviousMonthNumber, '00'))" />
                        <xsl:with-param name="currentYYYYMM" select="concat($leftpaneldatanode/Calendar/CalendarBean/CurrentYear, format-number($leftpaneldatanode/Calendar/CalendarBean/CurrentMonthNumber, '00'))" />
                        <xsl:with-param name="todaysDate" select="$todaysDate" />
                    </xsl:call-template>
                    <xsl:call-template name="drawFirstCalendarWeekDay">
                        <xsl:with-param name="weekDayNumber" select="3" />
                        <xsl:with-param name="previousMonthLastDate" select="$previousMonthLastDate" />
                        <xsl:with-param name="previousMonthLastDayOfWeek" select="$previousMonthLastDayOfWeek" />
                        <xsl:with-param name="previousYYYYMM" select="concat($leftpaneldatanode/Calendar/CalendarBean/PreviousMonthYear, format-number($leftpaneldatanode/Calendar/CalendarBean/PreviousMonthNumber, '00'))" />
                        <xsl:with-param name="currentYYYYMM" select="concat($leftpaneldatanode/Calendar/CalendarBean/CurrentYear, format-number($leftpaneldatanode/Calendar/CalendarBean/CurrentMonthNumber, '00'))" />
                        <xsl:with-param name="todaysDate" select="$todaysDate" />
                    </xsl:call-template>
                    <xsl:call-template name="drawFirstCalendarWeekDay">
                        <xsl:with-param name="weekDayNumber" select="4" />
                        <xsl:with-param name="previousMonthLastDate" select="$previousMonthLastDate" />
                        <xsl:with-param name="previousMonthLastDayOfWeek" select="$previousMonthLastDayOfWeek" />
                        <xsl:with-param name="previousYYYYMM" select="concat($leftpaneldatanode/Calendar/CalendarBean/PreviousMonthYear, format-number($leftpaneldatanode/Calendar/CalendarBean/PreviousMonthNumber, '00'))" />
                        <xsl:with-param name="currentYYYYMM" select="concat($leftpaneldatanode/Calendar/CalendarBean/CurrentYear, format-number($leftpaneldatanode/Calendar/CalendarBean/CurrentMonthNumber, '00'))" />
                        <xsl:with-param name="todaysDate" select="$todaysDate" />
                    </xsl:call-template>
                    <xsl:call-template name="drawFirstCalendarWeekDay">
                        <xsl:with-param name="weekDayNumber" select="5" />
                        <xsl:with-param name="previousMonthLastDate" select="$previousMonthLastDate" />
                        <xsl:with-param name="previousMonthLastDayOfWeek" select="$previousMonthLastDayOfWeek" />
                        <xsl:with-param name="previousYYYYMM" select="concat($leftpaneldatanode/Calendar/CalendarBean/PreviousMonthYear, format-number($leftpaneldatanode/Calendar/CalendarBean/PreviousMonthNumber, '00'))" />
                        <xsl:with-param name="currentYYYYMM" select="concat($leftpaneldatanode/Calendar/CalendarBean/CurrentYear, format-number($leftpaneldatanode/Calendar/CalendarBean/CurrentMonthNumber, '00'))" />
                        <xsl:with-param name="todaysDate" select="$todaysDate" />
                    </xsl:call-template>
                    <xsl:call-template name="drawFirstCalendarWeekDay">
                        <xsl:with-param name="weekDayNumber" select="6" />
                        <xsl:with-param name="previousMonthLastDate" select="$previousMonthLastDate" />
                        <xsl:with-param name="previousMonthLastDayOfWeek" select="$previousMonthLastDayOfWeek" />
                        <xsl:with-param name="previousYYYYMM" select="concat($leftpaneldatanode/Calendar/CalendarBean/PreviousMonthYear, format-number($leftpaneldatanode/Calendar/CalendarBean/PreviousMonthNumber, '00'))" />
                        <xsl:with-param name="currentYYYYMM" select="concat($leftpaneldatanode/Calendar/CalendarBean/CurrentYear, format-number($leftpaneldatanode/Calendar/CalendarBean/CurrentMonthNumber, '00'))" />
                        <xsl:with-param name="todaysDate" select="$todaysDate" />
                    </xsl:call-template>
                    <xsl:call-template name="drawFirstCalendarWeekDay">
                        <xsl:with-param name="weekDayNumber" select="7" />
                        <xsl:with-param name="previousMonthLastDate" select="$previousMonthLastDate" />
                        <xsl:with-param name="previousMonthLastDayOfWeek" select="$previousMonthLastDayOfWeek" />
                        <xsl:with-param name="previousYYYYMM" select="concat($leftpaneldatanode/Calendar/CalendarBean/PreviousMonthYear, format-number($leftpaneldatanode/Calendar/CalendarBean/PreviousMonthNumber, '00'))" />
                        <xsl:with-param name="currentYYYYMM" select="concat($leftpaneldatanode/Calendar/CalendarBean/CurrentYear, format-number($leftpaneldatanode/Calendar/CalendarBean/CurrentMonthNumber, '00'))" />
                        <xsl:with-param name="todaysDate" select="$todaysDate" />
                    </xsl:call-template>
                </tr>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template name="drawFirstCalendarWeekDay">
        <xsl:param name="weekDayNumber" />
        <xsl:param name="previousMonthLastDate" />
        <xsl:param name="previousMonthLastDayOfWeek" />
        <xsl:param name="previousYYYYMM" />
        <xsl:param name="currentYYYYMM" />
        <xsl:param name="todaysDate" />
        <xsl:choose>
            <xsl:when test="$previousMonthLastDayOfWeek &gt;= $weekDayNumber">

                <td align="center">
                    <a>
                        <xsl:attribute name="href">?selectedDate=
                            <xsl:value-of select="concat($previousYYYYMM, ($previousMonthLastDate - ($previousMonthLastDayOfWeek - $weekDayNumber)))" />
                        </xsl:attribute>
                        <xsl:attribute name="class">cal_pastmonth</xsl:attribute>
                        <xsl:value-of select="$previousMonthLastDate - ($previousMonthLastDayOfWeek - $weekDayNumber)" />
                    </a>
                </td>
            </xsl:when>
            <xsl:otherwise>
        <!-- Note: I have to get absolute value of the number from the math expression ($previousMonthLastDayOfWeek - $weekDayNumber) that is why we take the result * -1. -->
        <!-- may have to test here for todaysDate for sytle reasons -->
                <td align="center">
                    <a>
                        <xsl:attribute name="href">?selectedDate=
                            <xsl:value-of select="concat($currentYYYYMM, format-number(-1 * ($previousMonthLastDayOfWeek - $weekDayNumber),'00'))" />
                        </xsl:attribute>
                        <xsl:attribute name="class">cal_pastdate</xsl:attribute>
                        <xsl:value-of select="-1 * ($previousMonthLastDayOfWeek - $weekDayNumber)" />
                    </a>
                </td>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template name="drawCalendarWeekDay">
        <xsl:param name="weekDayNumber" />
        <xsl:param name="currentYYYYMM" />
        <xsl:param name="nextYYYYMM" />
        <xsl:param name="currentMonthNumber" />
        <xsl:param name="currentMonthLastDate" />
        <xsl:param name="currentDate" />
        <xsl:param name="currentYear" />
        <xsl:param name="todaysDate" />
        <xsl:param name="todaysMonth" />
        <xsl:param name="todaysYear" />
        <xsl:choose>
      <!--
        <xsl:when test="($weekDayNumber &gt; $todaysDate) and ($currentMonthNumber &gt;= $todaysMonth) and ($currentYear &gt;= $todaysYear)"> 
        <xsl:when test="(2) = (1)"> 
        -->
            <xsl:when test="($currentYear &gt; $todaysYear)">
                <xsl:choose>
                    <xsl:when test="$weekDayNumber &lt;= $currentMonthLastDate">

                        <td align="center">
                            <a>
                                <xsl:attribute name="class">cal_futuredate</xsl:attribute>
                                <xsl:value-of select="$weekDayNumber" />
                            </a>
                        </td>
                    </xsl:when>
                    <xsl:otherwise>
                        <td align="center">
                            <a>
                                <xsl:attribute name="class">cal_futuredate</xsl:attribute>
                                <xsl:value-of select="$weekDayNumber - $currentMonthLastDate" />
                            </a>
                        </td>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:when test="($currentMonthNumber &gt; $todaysMonth)">
                <xsl:choose>
                    <xsl:when test="$weekDayNumber &lt;= $currentMonthLastDate">

                        <td align="center">
                            <a>
                                <xsl:attribute name="class">cal_futuredate</xsl:attribute>
                                <xsl:value-of select="$weekDayNumber" />
                            </a>
                        </td>
                    </xsl:when>
                    <xsl:otherwise>
                        <td align="center">
                            <a>
                                <xsl:attribute name="class">cal_futuredate</xsl:attribute>
                                <xsl:value-of select="$weekDayNumber - $currentMonthLastDate" />
                            </a>
                        </td>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:when test="($currentMonthNumber = $todaysMonth)">
                <xsl:choose>
                    <xsl:when test="($weekDayNumber &gt; $todaysDate)">
                        <xsl:choose>
                            <xsl:when test="$weekDayNumber &lt;= $currentMonthLastDate">

                                <td align="center">
                                    <a>
                                        <xsl:attribute name="class">cal_futuredate</xsl:attribute>
                                        <xsl:value-of select="$weekDayNumber" />
                                    </a>
                                </td>
                            </xsl:when>
                            <xsl:otherwise>
                                <td align="center">
                                    <a>
                                        <xsl:attribute name="class">cal_futuredate</xsl:attribute>
                                        <xsl:value-of select="$weekDayNumber - $currentMonthLastDate" />:
                                    </a>
                                </td>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:choose>
                            <xsl:when test="$weekDayNumber &lt;= $currentMonthLastDate">

                                <td align="center">
                                    <a>
                                        <xsl:attribute name="href">?selectedDate=
                                            <xsl:value-of select="concat($currentYYYYMM, format-number($weekDayNumber,'00'))" />&amp;tY=
                                            <xsl:value-of select="$todaysYear" />&amp;cY=
                                            <xsl:value-of select="$currentYear" />
                                        </xsl:attribute>
                                        <xsl:attribute name="class">cal_pastdate</xsl:attribute>
                                        <xsl:value-of select="$weekDayNumber" />
                                    </a>
                                </td>
                            </xsl:when>
                            <xsl:otherwise>
                                <td align="center">
                                    <a>
                                        <xsl:attribute name="href">?selectedDate=
                                            <xsl:value-of select="concat($nextYYYYMM, format-number($weekDayNumber - $currentMonthLastDate,'00'))" />&amp;tY=
                                            <xsl:value-of select="$todaysYear" />&amp;cY=
                                            <xsl:value-of select="$currentYear" />
                                        </xsl:attribute>
                                        <xsl:attribute name="class">cal_pastdate</xsl:attribute>
                                        <xsl:value-of select="$weekDayNumber - $currentMonthLastDate" />
                                    </a>
                                </td>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:when test="$weekDayNumber = $todaysDate">
                <td align="center">
                    <a>
                        <xsl:attribute name="href">?selectedDate=
                            <xsl:value-of select="concat($currentYYYYMM, format-number($todaysDate, '00'))" />
                        </xsl:attribute>
                        <xsl:attribute name="class">cal_todayDate</xsl:attribute>
                        <xsl:value-of select="$weekDayNumber" />
                    </a>
                </td>
            </xsl:when>
            <xsl:otherwise>
                <xsl:choose>
                    <xsl:when test="$weekDayNumber &lt;= $currentMonthLastDate">

                        <td align="center">
                            <a>
                                <xsl:attribute name="href">?selectedDate=
                                    <xsl:value-of select="concat($currentYYYYMM, format-number($weekDayNumber,'00'))" />&amp;tY=
                                    <xsl:value-of select="$todaysYear" />&amp;cY=
                                    <xsl:value-of select="$currentYear" />
                                </xsl:attribute>
                                <xsl:attribute name="class">cal_pastdate</xsl:attribute>
                                <xsl:value-of select="$weekDayNumber" />
                            </a>
                        </td>
                    </xsl:when>
                    <xsl:otherwise>
                        <td align="center">
                            <a>
                                <xsl:attribute name="href">?selectedDate=
                                    <xsl:value-of select="concat($nextYYYYMM, format-number($weekDayNumber - $currentMonthLastDate,'00'))" />&amp;tY=
                                    <xsl:value-of select="$todaysYear" />&amp;cY=
                                    <xsl:value-of select="$currentYear" />
                                </xsl:attribute>
                                <xsl:attribute name="class">cal_pastdate</xsl:attribute>
                                <xsl:value-of select="$weekDayNumber - $currentMonthLastDate" />
                            </a>
                        </td>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template name="drawCalendarFinalWeekDay">
        <xsl:param name="weekDateNumber" />
        <xsl:param name="currentMonthLastDate" />
        <xsl:param name="currentYYYYMM" />
        <xsl:param name="nextYYYYMM" />
        <xsl:choose>
            <xsl:when test="$weekDateNumber &lt;= $currentMonthLastDate">
        <!-- <xsl:when test="($weekDateNumber &lt; $todaysDate) or ($currentMonthNumber &lt; $todaysMonth) or ($currentYear &lt; $todaysYear)"> -->
                <td align="center">
                    <a>
                        <xsl:attribute name="class">cal_futuredate</xsl:attribute>
                        <xsl:value-of select="$weekDateNumber" />
                    </a>
                </td>
            </xsl:when>
            <xsl:otherwise>
                <td align="center">
                    <a>
                        <xsl:attribute name="class">cal_futuredate</xsl:attribute>
                        <xsl:value-of select="$weekDateNumber - $currentMonthLastDate" />
                    </a>
                </td>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template name="drawMidCalendarWeeks">
        <xsl:param name="firstDateOfWeek" />
        <xsl:param name="currentDate" /> <!--NEW WDW-->
        <xsl:param name="currentMonthNumber" /> <!--NEW WDW-->
        <xsl:param name="currentYear" /> <!--NEW WDW-->
        <xsl:param name="todaysDate" /> <!--NEW WDW-->
        <xsl:param name="todaysMonth" /> <!--NEW WDW-->
        <xsl:param name="todaysYear" /> <!--NEW WDW-->
        <tr>
      <!-- need to recursivly call this for every day of week -->
            <xsl:call-template name="drawCalendarWeekDay">
                <xsl:with-param name="weekDayNumber"
                        select="$firstDateOfWeek" />
                <xsl:with-param name="currentYYYYMM"
                        select="concat($leftpaneldatanode/Calendar/CalendarBean/CurrentYear, format-number($leftpaneldatanode/Calendar/CalendarBean/CurrentMonthNumber,'00'))" />
                <xsl:with-param name="nextYYYYMM"
                        select="concat($leftpaneldatanode/Calendar/CalendarBean/NextMonthYear, format-number($leftpaneldatanode/Calendar/CalendarBean/NextMonthNumber,'00'))" />
                <xsl:with-param name="currentDate" select="$currentDate" />
                <xsl:with-param name="currentMonthNumber"
                        select="$currentMonthNumber" />
                <xsl:with-param name="currentMonthLastDate"
                        select="$leftpaneldatanode/Calendar/CalendarBean/CurrentMonthLastDate" />
                <xsl:with-param name="currentYear" select="$currentYear" />
                <xsl:with-param name="todaysDate" select="$todaysDate" />
                <xsl:with-param name="todaysMonth" select="$todaysMonth" />
                <xsl:with-param name="todaysYear" select="$todaysYear" />
            </xsl:call-template>
            <xsl:call-template name="drawCalendarWeekDay">
                <xsl:with-param name="weekDayNumber"
                        select="$firstDateOfWeek + 1" />
                <xsl:with-param name="currentYYYYMM"
                        select="concat($leftpaneldatanode/Calendar/CalendarBean/CurrentYear, format-number($leftpaneldatanode/Calendar/CalendarBean/CurrentMonthNumber,'00'))" />
                <xsl:with-param name="nextYYYYMM"
                        select="concat($leftpaneldatanode/Calendar/CalendarBean/NextMonthYear, format-number($leftpaneldatanode/Calendar/CalendarBean/NextMonthNumber,'00'))" />
                <xsl:with-param name="currentDate" select="$currentDate" />
                <xsl:with-param name="currentMonthNumber"
                        select="$currentMonthNumber" />
                <xsl:with-param name="currentMonthLastDate"
                        select="$leftpaneldatanode/Calendar/CalendarBean/CurrentMonthLastDate" />
                <xsl:with-param name="currentYear" select="$currentYear" />
                <xsl:with-param name="todaysDate" select="$todaysDate" />
                <xsl:with-param name="todaysMonth" select="$todaysMonth" />
                <xsl:with-param name="todaysYear" select="$todaysYear" />
            </xsl:call-template>
            <xsl:call-template name="drawCalendarWeekDay">
                <xsl:with-param name="weekDayNumber"
                        select="$firstDateOfWeek + 2" />
                <xsl:with-param name="currentYYYYMM"
                        select="concat($leftpaneldatanode/Calendar/CalendarBean/CurrentYear, format-number($leftpaneldatanode/Calendar/CalendarBean/CurrentMonthNumber,'00'))" />
                <xsl:with-param name="nextYYYYMM"
                        select="concat($leftpaneldatanode/Calendar/CalendarBean/NextMonthYear, format-number($leftpaneldatanode/Calendar/CalendarBean/NextMonthNumber,'00'))" />
                <xsl:with-param name="currentDate" select="$currentDate" />
                <xsl:with-param name="currentMonthNumber"
                        select="$currentMonthNumber" />
                <xsl:with-param name="currentMonthLastDate"
                        select="$leftpaneldatanode/Calendar/CalendarBean/CurrentMonthLastDate" />
                <xsl:with-param name="currentYear" select="$currentYear" />
                <xsl:with-param name="todaysDate" select="$todaysDate" />
                <xsl:with-param name="todaysMonth" select="$todaysMonth" />
                <xsl:with-param name="todaysYear" select="$todaysYear" />
            </xsl:call-template>
            <xsl:call-template name="drawCalendarWeekDay">
                <xsl:with-param name="weekDayNumber"
                        select="$firstDateOfWeek + 3" />
                <xsl:with-param name="currentYYYYMM"
                        select="concat($leftpaneldatanode/Calendar/CalendarBean/CurrentYear, format-number($leftpaneldatanode/Calendar/CalendarBean/CurrentMonthNumber,'00'))" />
                <xsl:with-param name="nextYYYYMM"
                        select="concat($leftpaneldatanode/Calendar/CalendarBean/NextMonthYear, format-number($leftpaneldatanode/Calendar/CalendarBean/NextMonthNumber,'00'))" />
                <xsl:with-param name="currentDate" select="$currentDate" />
                <xsl:with-param name="currentMonthNumber"
                        select="$currentMonthNumber" />
                <xsl:with-param name="currentMonthLastDate"
                        select="$leftpaneldatanode/Calendar/CalendarBean/CurrentMonthLastDate" />
                <xsl:with-param name="currentYear" select="$currentYear" />
                <xsl:with-param name="todaysDate" select="$todaysDate" />
                <xsl:with-param name="todaysMonth" select="$todaysMonth" />
                <xsl:with-param name="todaysYear" select="$todaysYear" />
            </xsl:call-template>
            <xsl:call-template name="drawCalendarWeekDay">
                <xsl:with-param name="weekDayNumber"
                        select="$firstDateOfWeek + 4" />
                <xsl:with-param name="currentYYYYMM"
                        select="concat($leftpaneldatanode/Calendar/CalendarBean/CurrentYear, format-number($leftpaneldatanode/Calendar/CalendarBean/CurrentMonthNumber,'00'))" />
                <xsl:with-param name="nextYYYYMM"
                        select="concat($leftpaneldatanode/Calendar/CalendarBean/NextMonthYear, format-number($leftpaneldatanode/Calendar/CalendarBean/NextMonthNumber,'00'))" />
                <xsl:with-param name="currentDate" select="$currentDate" />
                <xsl:with-param name="currentMonthNumber"
                        select="$currentMonthNumber" />
                <xsl:with-param name="currentMonthLastDate"
                        select="$leftpaneldatanode/Calendar/CalendarBean/CurrentMonthLastDate" />
                <xsl:with-param name="currentYear" select="$currentYear" />
                <xsl:with-param name="todaysDate" select="$todaysDate" />
                <xsl:with-param name="todaysMonth" select="$todaysMonth" />
                <xsl:with-param name="todaysYear" select="$todaysYear" />
            </xsl:call-template>
            <xsl:call-template name="drawCalendarWeekDay">
                <xsl:with-param name="weekDayNumber"
                        select="$firstDateOfWeek + 5" />
                <xsl:with-param name="currentYYYYMM"
                        select="concat($leftpaneldatanode/Calendar/CalendarBean/CurrentYear, format-number($leftpaneldatanode/Calendar/CalendarBean/CurrentMonthNumber,'00'))" />
                <xsl:with-param name="nextYYYYMM"
                        select="concat($leftpaneldatanode/Calendar/CalendarBean/NextMonthYear, format-number($leftpaneldatanode/Calendar/CalendarBean/NextMonthNumber,'00'))" />
                <xsl:with-param name="currentDate" select="$currentDate" />
                <xsl:with-param name="currentMonthNumber"
                        select="$currentMonthNumber" />
                <xsl:with-param name="currentMonthLastDate"
                        select="$leftpaneldatanode/Calendar/CalendarBean/CurrentMonthLastDate" />
                <xsl:with-param name="currentYear" select="$currentYear" />
                <xsl:with-param name="todaysDate" select="$todaysDate" />
                <xsl:with-param name="todaysMonth" select="$todaysMonth" />
                <xsl:with-param name="todaysYear" select="$todaysYear" />
            </xsl:call-template>
            <xsl:call-template name="drawCalendarWeekDay">
                <xsl:with-param name="weekDayNumber"
                        select="$firstDateOfWeek + 6" />
                <xsl:with-param name="currentYYYYMM"
                        select="concat($leftpaneldatanode/Calendar/CalendarBean/CurrentYear, format-number($leftpaneldatanode/Calendar/CalendarBean/CurrentMonthNumber,'00'))" />
                <xsl:with-param name="nextYYYYMM"
                        select="concat($leftpaneldatanode/Calendar/CalendarBean/NextMonthYear, format-number($leftpaneldatanode/Calendar/CalendarBean/NextMonthNumber,'00'))" />
                <xsl:with-param name="currentDate" select="$currentDate" />
                <xsl:with-param name="currentMonthNumber"
                        select="$currentMonthNumber" />
                <xsl:with-param name="currentMonthLastDate"
                        select="$leftpaneldatanode/Calendar/CalendarBean/CurrentMonthLastDate" />
                <xsl:with-param name="currentYear" select="$currentYear" />
                <xsl:with-param name="todaysDate" select="$todaysDate" />
                <xsl:with-param name="todaysMonth" select="$todaysMonth" />
                <xsl:with-param name="todaysYear" select="$todaysYear" />
            </xsl:call-template>
        </tr>
        <xsl:choose>
      <!-- test to see if we have a FULL week to draw -->
            <xsl:when test="$firstDateOfWeek + 7 -1 &lt; $leftpaneldatanode/Calendar/CalendarBean/CurrentMonthLastDate">

                <xsl:call-template name="drawMidCalendarWeeks">
                    <xsl:with-param name="firstDateOfWeek"
                          select="$firstDateOfWeek + 7" />
                    <xsl:with-param name="currentDate"
                          select="$currentDate" />
                    <xsl:with-param name="currentMonthNumber"
                          select="$currentMonthNumber" />
          <!--NEW WDW-->
                    <xsl:with-param name="currentYear"
                          select="$currentYear" />
          <!--NEW WDW-->
                    <xsl:with-param name="todaysDate" select="$todaysDate" />
          <!--NEW WDW-->
                    <xsl:with-param name="todaysMonth"
                          select="$todaysMonth" />
          <!--NEW WDW-->
                    <xsl:with-param name="todaysYear" select="$todaysYear" />
          <!--NEW WDW-->
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise></xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template name="drawFinalCalendarWeek">
        <xsl:param name="firstDateOfWeek" />
        <xsl:param name="currentMonthLastDate" />
        <xsl:param name="currentMonthNumber" /><!--NEW WDW-->
        <xsl:param name="currentYear" /> <!--NEW WDW-->
        <xsl:param name="todaysDate" /> <!--NEW WDW-->
        <xsl:param name="todaysMonth" /> <!--NEW WDW-->
        <xsl:param name="todaysYear" /> <!--NEW WDW-->
    <!--
<xsl:value-of select="$firstDateOfWeek"/>
<xsl:value-of select="$lastDateOfWeek"/>
-->
    <!-- begin -->
        <tr>
            <xsl:call-template name="drawCalendarFinalWeekDay">
                <xsl:with-param name="weekDateNumber"
                        select="$firstDateOfWeek" />
                <xsl:with-param name="currentMonthLastDate"
                        select="$currentMonthLastDate" />
                <xsl:with-param name="currentYYYYMM"
                        select="concat($leftpaneldatanode/Calendar/CalendarBean/CurrentYear, $leftpaneldatanode/Calendar/CalendarBean/CurrentMonthNumber)" />
                <xsl:with-param name="nextYYYYMM"
                        select="concat($leftpaneldatanode/Calendar/CalendarBean/NextMonthYear, $leftpaneldatanode/Calendar/CalendarBean/NextMonthNumber)" />
            </xsl:call-template>
            <xsl:call-template name="drawCalendarFinalWeekDay">
                <xsl:with-param name="weekDateNumber"
                        select="$firstDateOfWeek + 1" />
                <xsl:with-param name="currentMonthLastDate"
                        select="$currentMonthLastDate" />
                <xsl:with-param name="currentYYYYMM"
                        select="concat($leftpaneldatanode/Calendar/CalendarBean/CurrentYear, $leftpaneldatanode/Calendar/CalendarBean/CurrentMonthNumber)" />
                <xsl:with-param name="nextYYYYMM"
                        select="concat($leftpaneldatanode/Calendar/CalendarBean/NextMonthYear, $leftpaneldatanode/Calendar/CalendarBean/NextMonthNumber)" />
            </xsl:call-template>
            <xsl:call-template name="drawCalendarFinalWeekDay">
                <xsl:with-param name="weekDateNumber"
                        select="$firstDateOfWeek + 2" />
                <xsl:with-param name="currentMonthLastDate"
                        select="$currentMonthLastDate" />
                <xsl:with-param name="currentYYYYMM"
                        select="concat($leftpaneldatanode/Calendar/CalendarBean/CurrentYear, $leftpaneldatanode/Calendar/CalendarBean/CurrentMonthNumber)" />
                <xsl:with-param name="nextYYYYMM"
                        select="concat($leftpaneldatanode/Calendar/CalendarBean/NextMonthYear, $leftpaneldatanode/Calendar/CalendarBean/NextMonthNumber)" />
            </xsl:call-template>
            <xsl:call-template name="drawCalendarFinalWeekDay">
                <xsl:with-param name="weekDateNumber"
                        select="$firstDateOfWeek + 3" />
                <xsl:with-param name="currentMonthLastDate"
                        select="$currentMonthLastDate" />
                <xsl:with-param name="currentYYYYMM"
                        select="concat($leftpaneldatanode/Calendar/CalendarBean/CurrentYear, $leftpaneldatanode/Calendar/CalendarBean/CurrentMonthNumber)" />
                <xsl:with-param name="nextYYYYMM"
                        select="concat($leftpaneldatanode/Calendar/CalendarBean/NextMonthYear, $leftpaneldatanode/Calendar/CalendarBean/NextMonthNumber)" />
            </xsl:call-template>
            <xsl:call-template name="drawCalendarFinalWeekDay">
                <xsl:with-param name="weekDateNumber"
                        select="$firstDateOfWeek + 4" />
                <xsl:with-param name="currentMonthLastDate"
                        select="$currentMonthLastDate" />
                <xsl:with-param name="currentYYYYMM"
                        select="concat($leftpaneldatanode/Calendar/CalendarBean/CurrentYear, $leftpaneldatanode/Calendar/CalendarBean/CurrentMonthNumber)" />
                <xsl:with-param name="nextYYYYMM"
                        select="concat($leftpaneldatanode/Calendar/CalendarBean/NextMonthYear, $leftpaneldatanode/Calendar/CalendarBean/NextMonthNumber)" />
            </xsl:call-template>
            <xsl:call-template name="drawCalendarFinalWeekDay">
                <xsl:with-param name="weekDateNumber"
                        select="$firstDateOfWeek + 5" />
                <xsl:with-param name="currentMonthLastDate"
                        select="$currentMonthLastDate" />
                <xsl:with-param name="currentYYYYMM"
                        select="concat($leftpaneldatanode/Calendar/CalendarBean/CurrentYear, $leftpaneldatanode/Calendar/CalendarBean/CurrentMonthNumber)" />
                <xsl:with-param name="nextYYYYMM"
                        select="concat($leftpaneldatanode/Calendar/CalendarBean/NextMonthYear, $leftpaneldatanode/Calendar/CalendarBean/NextMonthNumber)" />
            </xsl:call-template>
            <xsl:call-template name="drawCalendarFinalWeekDay">
                <xsl:with-param name="weekDateNumber"
                        select="$firstDateOfWeek + 6" />
                <xsl:with-param name="currentMonthLastDate"
                        select="$currentMonthLastDate" />
                <xsl:with-param name="currentYYYYMM"
                        select="concat($leftpaneldatanode/Calendar/CalendarBean/CurrentYear, $leftpaneldatanode/Calendar/CalendarBean/CurrentMonthNumber)" />
                <xsl:with-param name="nextYYYYMM"
                        select="concat($leftpaneldatanode/Calendar/CalendarBean/NextMonthYear, $leftpaneldatanode/Calendar/CalendarBean/NextMonthNumber)" />
            </xsl:call-template>
        </tr>
    <!-- end   -->
    </xsl:template>
    <xsl:template match="LeftPanel">
        <script>
            var date_params_url = ""
        </script>
        <div id="side">
            <link href="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8.18/themes/base/jquery-ui.css" rel="stylesheet" type="text/css"/>
            <script src="http://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js"></script>
            <script src="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8.18/jquery-ui.min.js"></script>
            <script>
                function getQueryStrings() { 
                    var assoc  = {};
                    var decode = function (s) { return decodeURIComponent(s.replace(/\+/g, " ")); };
                    var queryString = location.search.substring(1); 
                    var keyValues = queryString.split('&amp;'); 

                    for(var i in keyValues) { 
                        var key = keyValues[i].split('=');
                        if (key.length > 1) {
                        assoc[decode(key[0])] = decode(key[1]);
                        }
                    } 

                    return assoc; 
                } 
                $(document).ready(function(){                                                         
                   
                    $('#datepicker').datepicker( { 
                        dateFormat: 'yymmdd',  
                        changeMonth: true,
                        changeYear: true,
                        showOtherMonths: true,
                        showButtonPanel: true,
                        defaultDate: +0,
                        onSelect: function(dateText, inst) { 
                            //alert("Working"+dateText); 
                            date_params_url =  "?selectedDate=" + dateText + "&amp;tY=" + dateText.substring(0,4) + "&amp;cY=" + dateText.substring(0,4);
                            window.location.href = date_params_url;
                        } 
                    });
                    var myDate;
                    var sd = getQueryStrings()["selectedDate"]                   
                    if (typeof sd == "undefined" || sd == "") {
                        //myDate = new Date();
                        //sd = $("#datepicker").datepicker('getDate');
                    } else {
                        //var dstr = $.datepicker.formatDate('M d, yy',
                        //    $.datepicker.parseDate('yymmdd', sd
                        //).toString());                        
                        //20120409
                        var year = sd.substring(0,4);
                        var month = sd.substring(4,6);
                        var day = sd.substring(6,8);
                        // month in javascript is base-0
                        myDate = new Date(year, month -1, day);
                        $('#datepicker').datepicker('setDate', myDate);                     
                    }
                    
                });
             
            
            </script>
            <style type="text/css">
                .ui-datepicker table {
                box-shadow: none!important;
                }
            </style>
            <div id="datepicker" style="font-size:62.5%;"></div>          
            <h2>Other Graphs</h2>
            <p>Navigate to other graphs and information...</p>
            <ul id="oN">
                <li>
                    <a href="ViewRealTimeCharts.web">Realtime Charts</a> 
                </li>
                <li>
                    <script >
                        function view_daily_session_summary() {
                        params = location.search;
                        window.location = "ViewDailySessionSummary.web" + params;
                        }
                    </script>
                    <a href="javascript:view_daily_session_summary()" >Daily Snapshot</a> 
                </li>
                <li>
                <script >
                    function view_click_stream_page_view() {
                    params = location.search;
                    window.location = "ClickStreamPageView.web" + params;
                    }
                </script>
                <a href="javascript:view_click_stream_page_view()" >Click Stream</a> 
            </li>
                <li>
                    <script >
                        function view_daily_page_load() {
                        params = location.search;
                        window.location = "ViewDailyPageLoadCharts.web" + params;
                        }
                    </script>
                    <a href="javascript:view_daily_page_load()">Page Load Details</a>
                </li>
                <li>
                    <script >
                        function view_current_deployments() {
                        params = location.search;
                        window.location = "ViewCurrentDeployments.web" + params;
                        }
                    </script>
                    <a href="javascript:view_current_deployments()">Current Deployments</a>
                </li>
                <li>
                    <script >
                        function view_online_report_usage() {
                        params = location.search;
                        window.location = "OnlineReportUsageDetail.web" + params;
                        }
                    </script>
                    <a href="javascript:view_online_report_usage()">Online Reporting</a>
                </li>
                <li>
                    <script >
                        function view_exceptions() {
                        params = location.search;
                        window.location = "ViewExceptions.web" + params;
                        }
                    </script>
                    <a href="javascript:view_exceptions()">Exceptions (LIVE!)</a>
                </li>
                <li>
                    <script >
                        function view_historical() {
                        params = location.search;
                        window.location = "ViewHistoricalCharts.web" + params;
                        }
                    </script>
                    <a href="javascript:view_historical()">Financial (24hr-delay)</a>
                </li>
                <li>
                    <script >
                        function view_time_slice() {
                        params = location.search;
                        window.location = "ViewTimeSliceDetail.web" + params;
                        }
                    </script>
                    <a href="javascript:view_time_slice()">Time Slice Report</a>
                </li>
                <li>
                    <a href="http://java.sun.com/webapps/getjava/BrowserRedirect?locale=en&amp;host=www.java.com:80">
                        Install Java
                    </a>
                </li>
            </ul>
            <br />
        </div>
    <!-- closes #side -->
    </xsl:template>
</xsl:stylesheet>
