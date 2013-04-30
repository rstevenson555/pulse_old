

package com.bos.art.logParser.broadcast.history;
import org.apache.log4j.Logger;
public class QueryBroadcastFactory {


    public static QueryBroadcast getQueryBroadcast(
            String chart,
            java.util.Date time,
            String direction,
            int points,
            String precision,
            org.jgroups.Message msg
            ){
            if(chart != null && chart.equals("AVG_CHART")){
                return new AvgChartQueryBroadcast(
                        time,
                        direction,
                        points,
                        precision,
                        msg);

            }else{
                return null;
            }
    }
}
