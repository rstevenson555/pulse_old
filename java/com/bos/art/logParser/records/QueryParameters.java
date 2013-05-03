/*
 * Created on Aug 23, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.bos.art.logParser.records;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;



import com.bos.art.logParser.collector.QueryParameterWriteQueue;
import com.bos.art.logParser.db.ForeignKeyStore;
import com.bos.art.logParser.db.PersistanceStrategy;
import com.bos.art.logParser.db.AccessRecordPersistanceStrategy;
import org.apache.commons.codec.binary.Base64;

import org.apache.log4j.Logger;

/**
 * @author I0360D3
 *
 * To change the template for this generated type comment go to Window>Preferences>Java>Code Generation>Code and Comments
 */
public class QueryParameters {

    private static final Logger logger = (Logger) Logger.getLogger(QueryParameters.class.getName());
    private String queryParameters;
    private Integer recordPK;
    private HashMap hashParameters;

    private QueryParameters() {
    }

    public QueryParameters(String qp, int rPK) {
        recordPK = new Integer(rPK);
        if (qp != null && qp.indexOf("#P#") < 0) {
            queryParameters = decode(qp);
        } else {
            queryParameters = qp;
        }
    }

    private String decode(String s) {
        if (s != null) {
            try {
                byte[] decodeBA = Base64.decodeBase64(s.getBytes());
                return new String(decodeBA);
            } catch (Exception e) {
                return s;
            }
        }
        return null;
    }
    private static String AMP_SEP = "&";
    private static String AMP_ESCAPED = "&amp;";
    private static String PARAM_MARKER = "#P#";

   
/**
     * process query parameters, tokenize
     */
    public void processQueryParameters() {
        HashSet<String> set = new HashSet();
        if (queryParameters != null) {
            System.out.println("before: "+queryParameters);
            if (queryParameters.indexOf('?') > -1) {
                queryParameters = queryParameters.substring(queryParameters.indexOf('?'));
            }
            if (queryParameters.indexOf(PARAM_MARKER) > -1) {
                int sep = queryParameters.indexOf(PARAM_MARKER);
                queryParameters = queryParameters.substring(0, sep) + "&" + queryParameters.substring(sep + 3);
            }
            int seplength = AMP_SEP.length();
            int sep = 0,asep = 0;
            int start = 0;
            while (queryParameters != null) {
                if ((sep = queryParameters.indexOf(AMP_SEP,start)) > -1 ) {
                    asep = queryParameters.indexOf(AMP_ESCAPED);
                    if ( asep == sep) {
                        // this was a encoded amp and not just a &
                        start = asep+AMP_ESCAPED.length();
                        continue; // skip this & because it's an encoded amp;
                    }
                    seplength = AMP_SEP.length();
                    String currentParameter = queryParameters.substring(0, sep);
                    queryParameters = queryParameters.substring(sep + seplength);
                    set.add(currentParameter);
                } else {
                    String currentParameter = queryParameters;
                    queryParameters = null;
                    set.add(currentParameter);
                }
            }
            for (String s : set) {
                if (s != null && s.length() > 1024) {
                    System.out.println("QueryParam too long for DB QP => " + s);
                    s = s.substring(0, 1024);
                    System.out.println("QueryParam stored as => " + s);
                }
            }
            System.out.println("paramset: " + set);
        }
    }


    private static final PersistanceStrategy pStrat = AccessRecordPersistanceStrategy.getInstance();

    private Integer getQueryParameterId(String s) {
        int result = ForeignKeyStore.getInstance().getForeignKey(null, s, ForeignKeyStore.FK_QUERY_PARAMETER_ID, pStrat);
        return new Integer(result);
    }

    public static class DBQueryParamRecord {

        public DBQueryParamRecord(Integer quid, Integer rpk) {
            QueryParameterID = quid;
            RecordPK = rpk;
        }
        private Integer RecordPK;
        private Integer QueryParameterID;

        public Integer getQueryParameterID() {
            return QueryParameterID;
        }

        public Integer getRecordPK() {
            return RecordPK;
        }
    }
}
