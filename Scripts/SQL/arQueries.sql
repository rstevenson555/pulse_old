select Time, LoadTime/1000, pageName, userName, sessionTXT, ipAddress,
    shortName, contextName, appName
    from AccessRecords ar, Pages p, Users u, 
        Sessions s, Machines m, Contexts c, Apps a
        Where
            ar.Page_ID=p.Page_ID and
            ar.User_ID=u.User_ID and
            ar.Session_ID=s.Session_ID and
            ar.Machine_ID=m.Machine_ID and
            ar.Context_ID=c.Context_ID and
            ar.App_ID=a.App_ID and
            u.userName="010689ABeeker277"
    order by
        Time, shortName;
        
