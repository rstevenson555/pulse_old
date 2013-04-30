--
-- PostgreSQL database dump
--

SET client_encoding = 'UNICODE';
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- TOC entry 1840 (class 0 OID 0)
-- Name: DUMP TIMESTAMP; Type: DUMP TIMESTAMP; Schema: -; Owner: 
--

-- Started on 2005-02-25 14:53:53 Central Standard Time


--
-- TOC entry 1842 (class 1262 OID 17233)
-- Name: artdb; Type: DATABASE; Schema: -; Owner: artadmin
--

CREATE DATABASE artdb WITH TEMPLATE = template0 ENCODING = 'UNICODE' TABLESPACE = art_default_tablespace;


ALTER DATABASE artdb OWNER TO artadmin;

\connect artdb

SET client_encoding = 'UNICODE';
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- TOC entry 1843 (class 0 OID 0)
-- Dependencies: 5
-- Name: SCHEMA public; Type: COMMENT; Schema: -; Owner: pgsql
--

COMMENT ON SCHEMA public IS 'Standard public schema';


SET search_path = public, pg_catalog;

--
-- TOC entry 17 (class 1255 OID 17234)
-- Dependencies: 5
-- Name: plpgsql_call_handler(); Type: FUNCTION; Schema: public; Owner: pgsql
--

CREATE FUNCTION plpgsql_call_handler() RETURNS language_handler
    AS '$libdir/plpgsql', 'plpgsql_call_handler'
    LANGUAGE c;


ALTER FUNCTION public.plpgsql_call_handler() OWNER TO pgsql;

--
-- TOC entry 18 (class 1255 OID 17235)
-- Dependencies: 5
-- Name: plpgsql_validator(oid); Type: FUNCTION; Schema: public; Owner: pgsql
--

CREATE FUNCTION plpgsql_validator(oid) RETURNS void
    AS '$libdir/plpgsql', 'plpgsql_validator'
    LANGUAGE c;


ALTER FUNCTION public.plpgsql_validator(oid) OWNER TO pgsql;

--
-- TOC entry 329 (class 16402 OID 17236)
-- Dependencies: 17 18
-- Name: plpgsql; Type: PROCEDURAL LANGUAGE; Schema: public; Owner: 
--

CREATE TRUSTED PROCEDURAL LANGUAGE plpgsql HANDLER plpgsql_call_handler VALIDATOR plpgsql_validator;


--
-- TOC entry 23 (class 1255 OID 195869409)
-- Dependencies: 5 329
-- Name: export_loadtest_requests(character varying, character varying, timestamp with time zone, timestamp with time zone, character varying); Type: FUNCTION; Schema: public; Owner: artadmin
--

CREATE FUNCTION export_loadtest_requests(character varying, character varying, timestamp with time zone, timestamp with time zone, character varying) RETURNS bigint
    AS $_$declare 
  fromContext   ALIAS FOR $1;
  fromMachines  ALIAS FOR $2;
  fromStartTime ALIAS FOR $3;
  fromStopTime  ALIAS FOR $4;
  toContext     ALIAS FOR $5;
  exportNumber int8;
  rec RECORD;
  machinesArray varchar[];
  counter int2;
  string varchar;
begin  

machinesArray := string_to_array(fromMachines,',');
counter:=0;

SELECT nextval('public.loadtest_export_seq'::text) into exportNumber;

INSERT INTO loadtest_export VALUES (exportNumber, now(), fromContext, fromMachines, fromStartTime, fromStopTime, '/'||toContext);

WHILE true LOOP	
	counter:=counter+1;
	string := machinesArray[counter];
	IF (string IS NULL) THEN
		EXIT;
	END IF;

	INSERT INTO loadtest_requests (select exportNumber, 
		a.session_id,
		a.requesttoken,
		a.requesttype,
		a.time,
		'/'||toContext,
		'test.boiseoffice.com',
		p.pagename,
		getqueryparams(a.recordpk),
		a.loadtime,
		a.context_id,
		a.page_id,
		a.app_id,
		a.machine_id,
		a.branch_tag_id
	from accessrecords a,
		contexts c,
		pages p,
		machines m,
		sessions s,
		users u
	where a.time > fromStartTime --to_timestamp('2005-02-16 13:00:00','YYYY-MM-DD HH24:MI:SS')
		and a.time < fromStopTime --to_timestamp('2005-02-16 13:15:00','YYYY-MM-DD HH24:MI:SS')
		and s.sessionstarttime > fromStartTime  --to_timestamp('2005-02-16 13:00:00','YYYY-MM-DD HH24:MI:SS')
		and s.sessionendtime < fromStopTime --to_timestamp('2005-02-16 13:15:00','YYYY-MM-DD HH24:MI:SS')
		and a.session_id = s.session_id
		and m.machine_id = a.machine_id
		and m.machinename = string
		and u.user_id = a.user_id
		and requesttype=0
		and c.context_id = a.context_id
		and c.contextname = fromContext
		and p.page_id = a.page_id
	order by requesttoken asc, requesttype asc);
END LOOP;

RETURN exportNumber;
end;
$_$
    LANGUAGE plpgsql;


ALTER FUNCTION public.export_loadtest_requests(character varying, character varying, timestamp with time zone, timestamp with time zone, character varying) OWNER TO artadmin;

--
-- TOC entry 19 (class 1255 OID 17237)
-- Dependencies: 5 329
-- Name: getqueryparams(integer); Type: FUNCTION; Schema: public; Owner: artadmin
--

CREATE FUNCTION getqueryparams("RecordPK" integer) RETURNS character varying
    AS $_$declare 
  queryString text:='';
  counter int:=0;
  qryparms RECORD;
begin 
  FOR qryparms in SELECT qp.queryparams 
	     FROM queryparameters qp, queryparamrecords qpr 
	     WHERE qpr.queryparameter_id = qp.queryparameter_id
	     and qpr.recordpk=$1
  LOOP
    --set temp vars
    counter := counter + 1;

    IF qryparms.queryparams IS NOT NULL AND qryparms.queryparams != '' THEN      
      IF counter = 1 THEN 
	--first param we don't need &
        queryString := qryparms.queryparams;	
      ELSE
        queryString := queryString||'&'||qryparms.queryparams;
      END IF;
    END IF;
  END LOOP;

  return queryString; 
end;$_$
    LANGUAGE plpgsql;


ALTER FUNCTION public.getqueryparams("RecordPK" integer) OWNER TO artadmin;

--
-- TOC entry 20 (class 1255 OID 17238)
-- Dependencies: 5 329
-- Name: gettesturlstrings(character varying, character varying, timestamp without time zone, timestamp without time zone, character varying, character varying); Type: FUNCTION; Schema: public; Owner: pgsql
--

CREATE FUNCTION gettesturlstrings("fromContext" character varying, "fromMachines" character varying, "fromStartTime" timestamp without time zone, "fromStopTime" timestamp without time zone, "toHost" character varying, "toContext" character varying) RETURNS SETOF text
    AS $_$declare 
  fromContext   ALIAS FOR $1;
  fromMachines  ALIAS FOR $2;
  fromStartTime ALIAS FOR $3;
  fromStopTime  ALIAS FOR $4;
  toHost        ALIAS FOR $5;
  toContext     ALIAS FOR $6;
  rec RECORD;
begin 
INSERT INTO playback_requests values (select a.session_id,
        a.requesttoken,
        a.requesttype,
        a.time,
        u.username,
        c.contextname,
        p.pagename,
        getqueryparams(a.recordpk)        
  from accessrecords a,
        contexts c,
        pages p,
        machines m,
	sessions s,
        users u
  where a.time > fromStartTime --to_timestamp('2005-02-16 13:00:00','YYYY-MM-DD HH24:MI:SS')
        and a.time < fromStopTime --to_timestamp('2005-02-16 13:15:00','YYYY-MM-DD HH24:MI:SS')
	and s.sessionstarttime > fromStartTime  --to_timestamp('2005-02-16 13:00:00','YYYY-MM-DD HH24:MI:SS')
	and s.sessionendtime < fromStopTime --to_timestamp('2005-02-16 13:15:00','YYYY-MM-DD HH24:MI:SS')
	and a.session_id = s.session_id
        and m.machine_id = a.machine_id
        and m.machinename = fromMachines --in ('prod-ec-app1', 'prod-ec-app3')
        and u.user_id = a.user_id
        and requesttype=0
        and c.context_id = a.context_id
	and c.contextname = fromContext
        and p.page_id = a.page_id
  order by requesttoken asc, requesttype asc);
/*
FOR rec IN select a.session_id,
        a.requesttoken,
        a.requesttype,
        a.time,
        u.username,
        c.contextname,
        p.pagename,
        getqueryparams(a.recordpk)        
  from accessrecords a,
        contexts c,
        pages p,
        machines m,
	sessions s,
        users u
  where a.time > fromStartTime --to_timestamp('2005-02-16 13:00:00','YYYY-MM-DD HH24:MI:SS')
        and a.time < fromStopTime --to_timestamp('2005-02-16 13:15:00','YYYY-MM-DD HH24:MI:SS')
	and s.sessionstarttime > fromStartTime  --to_timestamp('2005-02-16 13:00:00','YYYY-MM-DD HH24:MI:SS')
	and s.sessionendtime < fromStopTime --to_timestamp('2005-02-16 13:15:00','YYYY-MM-DD HH24:MI:SS')
	and a.session_id = s.session_id
        and m.machine_id = a.machine_id
        and m.machinename = fromMachines --in ('prod-ec-app1', 'prod-ec-app3')
        and u.user_id = a.user_id
        and requesttype=0
        and c.context_id = a.context_id
	and c.contextname = fromContext
        and p.page_id = a.page_id
  order by requesttoken asc, requesttype asc
LOOP
  RETURN NEXT rec;
END LOOP;
*/
RETURN;
end;
$_$
    LANGUAGE plpgsql;


ALTER FUNCTION public.gettesturlstrings("fromContext" character varying, "fromMachines" character varying, "fromStartTime" timestamp without time zone, "fromStopTime" timestamp without time zone, "toHost" character varying, "toContext" character varying) OWNER TO pgsql;

--
-- TOC entry 22 (class 1255 OID 17239)
-- Dependencies: 5 329
-- Name: populate_playback_requests(character varying, character varying, timestamp without time zone, timestamp without time zone, character varying); Type: FUNCTION; Schema: public; Owner: pgsql
--

CREATE FUNCTION populate_playback_requests(character varying, character varying, timestamp without time zone, timestamp without time zone, character varying) RETURNS bigint
    AS $_$declare 
  fromContext   ALIAS FOR $1;
  fromMachines  ALIAS FOR $2;
  fromStartTime ALIAS FOR $3;
  fromStopTime  ALIAS FOR $4;
  toContext     ALIAS FOR $5;
  testRunNumber int8;
  rec RECORD;
begin  
--DELETE FROM playback_requests;

SELECT nextval('public.playback_requests_test_number_seq'::text) into testRunNumber;

INSERT INTO playback_requests (select testRunNumber, 
	a.session_id,
        a.requesttoken,
        a.requesttype,
        a.time,
--        u.username,	
        toContext,
	'test.boiseoffice.com',
        p.pagename,
        getqueryparams(a.recordpk)	
  from accessrecords a,
        contexts c,
        pages p,
        machines m,
	sessions s,
        users u
  where a.time > fromStartTime --to_timestamp('2005-02-16 13:00:00','YYYY-MM-DD HH24:MI:SS')
        and a.time < fromStopTime --to_timestamp('2005-02-16 13:15:00','YYYY-MM-DD HH24:MI:SS')
	and s.sessionstarttime > fromStartTime  --to_timestamp('2005-02-16 13:00:00','YYYY-MM-DD HH24:MI:SS')
	and s.sessionendtime < fromStopTime --to_timestamp('2005-02-16 13:15:00','YYYY-MM-DD HH24:MI:SS')
	and a.session_id = s.session_id
        and m.machine_id = a.machine_id
        and m.machinename = fromMachines --in ('prod-ec-app1', 'prod-ec-app3')
        and u.user_id = a.user_id
        and requesttype=0
        and c.context_id = a.context_id
	and c.contextname = fromContext
        and p.page_id = a.page_id
  order by requesttoken asc, requesttype asc);

RETURN testRunNumber;
end;
$_$
    LANGUAGE plpgsql;


ALTER FUNCTION public.populate_playback_requests(character varying, character varying, timestamp without time zone, timestamp without time zone, character varying) OWNER TO pgsql;

--
-- TOC entry 21 (class 1255 OID 161335924)
-- Dependencies: 5 329
-- Name: setlastmodtime(); Type: FUNCTION; Schema: public; Owner: artadmin
--

CREATE FUNCTION setlastmodtime() RETURNS "trigger"
    AS $$
	BEGIN
	   NEW.lastmodtime = now();
	   RETURN NEW;
	END
	$$
    LANGUAGE plpgsql;


ALTER FUNCTION public.setlastmodtime() OWNER TO artadmin;

--
-- TOC entry 24 (class 1255 OID 176565581)
-- Dependencies: 5 329
-- Name: testparse(character varying); Type: FUNCTION; Schema: public; Owner: artadmin
--

CREATE FUNCTION testparse(machines character varying) RETURNS character varying
    AS $_$declare
  machines varchar;
  string varchar;
  pos int;
  nextpos int;
  counter int;
  array_element text;
  machinesArray varchar [];
begin

	-- set the counter
        counter := 0;
	machines := $1;

machinesArray := string_to_array(machines,',');
counter:=0;

WHILE true LOOP	
	string := ''''||machinesArray[counter+1]||'''';
	IF (string IS NULL) THEN
		EXIT;
	END IF;
	machinesArray[counter+1]:=string;
	counter:=counter+1;
END LOOP;

	return array_to_string(machinesArray,',');

end

$_$
    LANGUAGE plpgsql;


ALTER FUNCTION public.testparse(machines character varying) OWNER TO artadmin;

SET default_tablespace = '';

SET default_with_oids = true;

--
-- TOC entry 1242 (class 1259 OID 17240)
-- Dependencies: 1630 1631 1632 5
-- Name: accessrecords; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE accessrecords (
    recordpk integer DEFAULT nextval('accessrecords_recordpk_seq'::text) NOT NULL,
    inserttime timestamp without time zone DEFAULT now() NOT NULL,
    page_id integer,
    user_id integer,
    session_id integer,
    machine_id integer,
    context_id integer,
    app_id integer,
    branch_tag_id integer,
    "time" timestamp without time zone DEFAULT '1970-01-01 01:00:00'::timestamp without time zone NOT NULL,
    loadtime integer,
    queryparameter_id integer,
    requesttype integer,
    requesttoken integer,
    userservicetime integer
);


ALTER TABLE public.accessrecords OWNER TO artadmin;

--
-- TOC entry 1243 (class 1259 OID 17245)
-- Dependencies: 5
-- Name: accessrecords_recordpk_seq; Type: SEQUENCE; Schema: public; Owner: artadmin
--

CREATE SEQUENCE accessrecords_recordpk_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.accessrecords_recordpk_seq OWNER TO artadmin;

--
-- TOC entry 1244 (class 1259 OID 17247)
-- Dependencies: 1633 1634 5
-- Name: accumulator; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE accumulator (
    accumulatorstat_id integer DEFAULT nextval('accumulator_accumulatorstat_'::text) NOT NULL,
    accumulatorname character varying(40),
    accumulatordescription text,
    accumulatortype character varying(20),
    dataunits character varying(40),
    lastmodtime timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.accumulator OWNER TO artadmin;

--
-- TOC entry 1245 (class 1259 OID 17254)
-- Dependencies: 5
-- Name: accumulator_accumulatorstat_; Type: SEQUENCE; Schema: public; Owner: artadmin
--

CREATE SEQUENCE accumulator_accumulatorstat_
    START WITH 4000
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.accumulator_accumulatorstat_ OWNER TO artadmin;

--
-- TOC entry 1246 (class 1259 OID 17256)
-- Dependencies: 1635 1636 1637 1638 5
-- Name: accumulatorevent; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE accumulatorevent (
    accumulatorevent_id integer DEFAULT nextval('accumulatorevent_accumulator'::text) NOT NULL,
    accumulatorstat_id integer,
    inserttime timestamp without time zone DEFAULT now() NOT NULL,
    machine_id integer,
    context_id integer,
    branch_id integer,
    app_id integer,
    "time" timestamp without time zone DEFAULT '1970-01-01 01:00:00'::timestamp without time zone NOT NULL,
    intvalue integer,
    doublevalue double precision,
    stringvalue character varying(40),
    datatype character varying(60),
    CONSTRAINT accumulatorevent_accumulatorstat_id_check CHECK ((accumulatorstat_id >= 0))
);


ALTER TABLE public.accumulatorevent OWNER TO artadmin;

--
-- TOC entry 1247 (class 1259 OID 17262)
-- Dependencies: 5
-- Name: accumulatorevent_accumulator; Type: SEQUENCE; Schema: public; Owner: artadmin
--

CREATE SEQUENCE accumulatorevent_accumulator
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.accumulatorevent_accumulator OWNER TO artadmin;

--
-- TOC entry 1248 (class 1259 OID 17264)
-- Dependencies: 1639 1640 5
-- Name: accumulatorstats; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE accumulatorstats (
    accumulatorstat_id integer,
    context_id integer,
    lastmoddate timestamp without time zone DEFAULT '1970-01-01 01:00:00'::timestamp without time zone NOT NULL,
    "time" timestamp without time zone DEFAULT '1970-01-01 01:00:00'::timestamp without time zone NOT NULL,
    value integer,
    count integer
);


ALTER TABLE public.accumulatorstats OWNER TO artadmin;

--
-- TOC entry 1249 (class 1259 OID 17268)
-- Dependencies: 1641 1642 5
-- Name: apps; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE apps (
    app_id integer DEFAULT nextval('apps_app_id_seq'::text) NOT NULL,
    appname character varying(50),
    lastmodtime timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.apps OWNER TO artadmin;

--
-- TOC entry 1250 (class 1259 OID 17272)
-- Dependencies: 5
-- Name: apps_app_id_seq; Type: SEQUENCE; Schema: public; Owner: artadmin
--

CREATE SEQUENCE apps_app_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.apps_app_id_seq OWNER TO artadmin;

--
-- TOC entry 1251 (class 1259 OID 17274)
-- Dependencies: 1643 1644 5
-- Name: branches; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE branches (
    branch_tag_id integer DEFAULT nextval('branches_branch_tag_id_seq'::text) NOT NULL,
    branchname character varying(50),
    lastmodtime timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.branches OWNER TO artadmin;

--
-- TOC entry 1252 (class 1259 OID 17278)
-- Dependencies: 5
-- Name: branches_branch_tag_id_seq; Type: SEQUENCE; Schema: public; Owner: artadmin
--

CREATE SEQUENCE branches_branch_tag_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.branches_branch_tag_id_seq OWNER TO artadmin;

--
-- TOC entry 1253 (class 1259 OID 17280)
-- Dependencies: 1645 1646 5
-- Name: browsers; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE browsers (
    browser_id integer DEFAULT nextval('browsers_browser_id_seq'::text) NOT NULL,
    patternmatchstring character varying(40),
    description character varying(50),
    inserttime timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.browsers OWNER TO artadmin;

--
-- TOC entry 1254 (class 1259 OID 17284)
-- Dependencies: 5
-- Name: browsers_browser_id_seq; Type: SEQUENCE; Schema: public; Owner: artadmin
--

CREATE SEQUENCE browsers_browser_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.browsers_browser_id_seq OWNER TO artadmin;

--
-- TOC entry 1255 (class 1259 OID 17286)
-- Dependencies: 1647 1648 5
-- Name: browserstats; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE browserstats (
    "day" date,
    browser_id integer,
    count integer,
    state character(1) DEFAULT 'O'::bpchar,
    inserttime timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.browserstats OWNER TO artadmin;

--
-- TOC entry 1256 (class 1259 OID 17290)
-- Dependencies: 1649 1650 5
-- Name: contexts; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE contexts (
    context_id integer DEFAULT nextval('contexts_context_id_seq'::text) NOT NULL,
    contextname character varying(50),
    lastmodtime timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.contexts OWNER TO artadmin;

--
-- TOC entry 1257 (class 1259 OID 17294)
-- Dependencies: 5
-- Name: contexts_context_id_seq; Type: SEQUENCE; Schema: public; Owner: artadmin
--

CREATE SEQUENCE contexts_context_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.contexts_context_id_seq OWNER TO artadmin;

--
-- TOC entry 1258 (class 1259 OID 17296)
-- Dependencies: 1651 1652 5
-- Name: dailycontextstats; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE dailycontextstats (
    "day" date,
    context_id integer,
    count integer,
    state character(1) DEFAULT 'O'::bpchar,
    lastmodtime timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.dailycontextstats OWNER TO artadmin;

--
-- TOC entry 1259 (class 1259 OID 17300)
-- Dependencies: 1653 1654 1655 5
-- Name: dailypageloadtimes; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE dailypageloadtimes (
    dailyloadtime_id integer DEFAULT nextval('dailypageloadtimes_dailyload'::text) NOT NULL,
    "day" date,
    page_id integer,
    context_id integer,
    totalloads integer,
    machinetype character(2),
    averageloadtime integer,
    ninetiethpercentile integer,
    twentyfifthpercentile integer,
    fiftiethpercentile integer,
    seventyfifthpercentile integer,
    maxloadtime integer,
    minloadtime integer,
    distinctusers integer,
    errorpages integer,
    thirtysecondloads integer,
    twentysecondloads integer,
    fifteensecondloads integer,
    tensecondloads integer,
    fivesecondloads integer,
    state character(1) DEFAULT 'O'::bpchar,
    lastmodtime timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.dailypageloadtimes OWNER TO artadmin;

--
-- TOC entry 1260 (class 1259 OID 17305)
-- Dependencies: 5
-- Name: dailypageloadtimes_dailyload; Type: SEQUENCE; Schema: public; Owner: artadmin
--

CREATE SEQUENCE dailypageloadtimes_dailyload
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.dailypageloadtimes_dailyload OWNER TO artadmin;

--
-- TOC entry 1261 (class 1259 OID 17307)
-- Dependencies: 1656 1657 1658 5
-- Name: dailysummary; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE dailysummary (
    "day" date DEFAULT '0001-01-01'::date NOT NULL,
    totalloads integer,
    averageloadtime integer,
    ninetiethpercentile integer,
    gomezpageninetyeightpercentile integer,
    gomezninetyeightpercentile integer,
    ninetyeightpercentile integer,
    twentyfifthpercentile integer,
    fiftiethpercentile integer,
    seventyfifthpercentile integer,
    maxloadtime integer,
    minloadtime integer DEFAULT 0,
    distinctusers integer,
    errorpages integer,
    thirtysecondloads integer,
    twentysecondloads integer,
    fifteensecondloads integer,
    tensecondloads integer,
    fivesecondloads integer,
    maxloadtime_page_id integer,
    maxloadtime_user_id integer,
    state character varying(255),
    lastmodtime timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.dailysummary OWNER TO artadmin;

--
-- TOC entry 1262 (class 1259 OID 17312)
-- Dependencies: 1659 5
-- Name: deployments; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE deployments (
    product character varying(20),
    machine character varying(30),
    server_group character varying(10),
    properties_file character varying(20),
    release_tag character varying(30),
    application_context character varying(20),
    deploy_time character varying(15) DEFAULT ''::character varying NOT NULL,
    iscurrent character(1),
    somecomment character varying(200),
    novelluserid character varying(10),
    changecontrollnumber character varying(20)
);


ALTER TABLE public.deployments OWNER TO artadmin;

--
-- TOC entry 1263 (class 1259 OID 17315)
-- Dependencies: 1660 1661 1662 5
-- Name: externalaccessrecords; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE externalaccessrecords (
    recordpk integer DEFAULT nextval('externalaccessrecords_record'::text) NOT NULL,
    lastmodtime timestamp without time zone DEFAULT now() NOT NULL,
    page_id integer,
    user_id integer,
    session_id integer,
    machine_id integer,
    context_id integer,
    app_id integer,
    branch_tag_id integer,
    classification_id integer,
    "time" timestamp without time zone DEFAULT '1970-01-01 01:00:00'::timestamp without time zone NOT NULL,
    loadtime integer
);


ALTER TABLE public.externalaccessrecords OWNER TO artadmin;

--
-- TOC entry 1264 (class 1259 OID 17320)
-- Dependencies: 5
-- Name: externalaccessrecords_record; Type: SEQUENCE; Schema: public; Owner: artadmin
--

CREATE SEQUENCE externalaccessrecords_record
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.externalaccessrecords_record OWNER TO artadmin;

--
-- TOC entry 1265 (class 1259 OID 17322)
-- Dependencies: 1663 1664 1665 5
-- Name: externalminutestatistics; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE externalminutestatistics (
    classification_id integer,
    machine_id integer,
    lastmodtime timestamp without time zone DEFAULT now() NOT NULL,
    "time" timestamp without time zone DEFAULT '1970-01-01 01:00:00'::timestamp without time zone NOT NULL,
    totalloads integer,
    averageloadtime integer,
    ninetiethpercentile integer,
    twentyfifthpercentile integer,
    fiftiethpercentile integer,
    seventyfifthpercentile integer,
    maxloadtime integer,
    minloadtime integer,
    distinctusers integer,
    errorpages integer,
    thirtysecondloads integer,
    twentysecondloads integer,
    fifteensecondloads integer,
    tensecondloads integer,
    fivesecondloads integer,
    state character(1) DEFAULT 'O'::bpchar
);


ALTER TABLE public.externalminutestatistics OWNER TO artadmin;

--
-- TOC entry 1266 (class 1259 OID 17327)
-- Dependencies: 1666 1667 5
-- Name: externalstats; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE externalstats (
    classification_id integer DEFAULT nextval('externalstats_classification'::text) NOT NULL,
    destination character varying(75),
    description character varying(255),
    inserttime timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.externalstats OWNER TO artadmin;

--
-- TOC entry 1267 (class 1259 OID 17331)
-- Dependencies: 5
-- Name: externalstats_classification; Type: SEQUENCE; Schema: public; Owner: artadmin
--

CREATE SEQUENCE externalstats_classification
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.externalstats_classification OWNER TO artadmin;

--
-- TOC entry 1268 (class 1259 OID 17333)
-- Dependencies: 1668 1669 1670 5
-- Name: fivesecondloads; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE fivesecondloads (
    recordpk integer DEFAULT nextval('fivesecondloads_recordpk_seq'::text) NOT NULL,
    lastmodtime timestamp without time zone DEFAULT now() NOT NULL,
    page_id integer,
    user_id integer,
    session_id integer,
    machine_id integer,
    context_id integer,
    app_id integer,
    "time" timestamp without time zone DEFAULT '1970-01-01 01:00:00'::timestamp without time zone NOT NULL,
    loadtime integer
);


ALTER TABLE public.fivesecondloads OWNER TO artadmin;

--
-- TOC entry 1269 (class 1259 OID 17338)
-- Dependencies: 5
-- Name: fivesecondloads_recordpk_seq; Type: SEQUENCE; Schema: public; Owner: artadmin
--

CREATE SEQUENCE fivesecondloads_recordpk_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.fivesecondloads_recordpk_seq OWNER TO artadmin;

--
-- TOC entry 1270 (class 1259 OID 17340)
-- Dependencies: 1671 1672 1673 5
-- Name: historical_external_statistics; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE historical_external_statistics (
    statistics_id integer,
    inserttime timestamp without time zone DEFAULT now() NOT NULL,
    starttime timestamp without time zone DEFAULT '1970-01-01 01:00:00'::timestamp without time zone NOT NULL,
    summaryperiodminutes integer,
    count integer,
    averageloadtime integer,
    maximumloadtime integer,
    minimumloadtime integer,
    state character(1) DEFAULT 'O'::bpchar
);


ALTER TABLE public.historical_external_statistics OWNER TO artadmin;

--
-- TOC entry 1271 (class 1259 OID 17345)
-- Dependencies: 1674 1675 1676 5
-- Name: hourlystatistics; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE hourlystatistics (
    machine_id integer,
    inserttime timestamp without time zone DEFAULT now() NOT NULL,
    totalloads integer,
    averageloadtime integer,
    ninetiethpercentile integer,
    twentyfifthpercentile integer,
    fiftiethpercentile integer,
    seventyfifthpercentile integer,
    maxloadtime integer,
    minloadtime integer,
    distinctusers integer,
    errorpages integer,
    thirtysecondloads integer,
    twentysecondloads integer,
    fifteensecondloads integer,
    tensecondloads integer,
    fivesecondloads integer,
    state character(1) DEFAULT 'O'::bpchar,
    "time" timestamp without time zone DEFAULT '1970-01-01 01:00:00'::timestamp without time zone NOT NULL
);


ALTER TABLE public.hourlystatistics OWNER TO artadmin;

SET default_with_oids = false;

--
-- TOC entry 1315 (class 1259 OID 156474982)
-- Dependencies: 1737 5
-- Name: loadtest_export; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE loadtest_export (
    loadtest_export_id bigint NOT NULL,
    export_time timestamp without time zone DEFAULT now(),
    from_context character varying,
    from_machine character varying,
    from_start_time timestamp without time zone,
    from_stop_time timestamp without time zone,
    to_context character varying
);


ALTER TABLE public.loadtest_export OWNER TO artadmin;

--
-- TOC entry 1316 (class 1259 OID 156841776)
-- Dependencies: 5
-- Name: loadtest_export_seq; Type: SEQUENCE; Schema: public; Owner: artadmin
--

CREATE SEQUENCE loadtest_export_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.loadtest_export_seq OWNER TO artadmin;

--
-- TOC entry 1314 (class 1259 OID 156199565)
-- Dependencies: 5
-- Name: loadtest_requests; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE loadtest_requests (
    loadtest_export_id bigint,
    session_id integer,
    request_token integer,
    request_type smallint,
    "time" timestamp without time zone,
    context character varying(50),
    host character varying(80),
    page character varying(120),
    query_parameters text,
    loadtime integer,
    context_id integer,
    page_id integer,
    app_id integer,
    machine_id integer,
    branch_tag_id integer
);


ALTER TABLE public.loadtest_requests OWNER TO artadmin;

SET default_with_oids = true;

--
-- TOC entry 1272 (class 1259 OID 17350)
-- Dependencies: 1677 1678 1679 1680 1681 5
-- Name: loadtests; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE loadtests (
    loadtest_id integer DEFAULT nextval('loadtests_loadtest_id_seq'::text) NOT NULL,
    testname character varying(255),
    context_id integer,
    branch_id integer,
    lastmodtime timestamp without time zone DEFAULT now() NOT NULL,
    starttime timestamp without time zone DEFAULT '1970-01-01 01:00:00'::timestamp without time zone NOT NULL,
    endtime timestamp without time zone DEFAULT '1970-01-01 01:00:00'::timestamp without time zone NOT NULL,
    "basePlaybackOffsetDate" timestamp without time zone,
    status character varying(10) DEFAULT 'UNKNOWN'::character varying,
    loadtest_export_id bigint,
    baseoffset bigint
);


ALTER TABLE public.loadtests OWNER TO artadmin;

--
-- TOC entry 1273 (class 1259 OID 17356)
-- Dependencies: 5
-- Name: loadtests_loadtest_id_seq; Type: SEQUENCE; Schema: public; Owner: artadmin
--

CREATE SEQUENCE loadtests_loadtest_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.loadtests_loadtest_id_seq OWNER TO artadmin;

--
-- TOC entry 1274 (class 1259 OID 17358)
-- Dependencies: 1682 1683 5
-- Name: loadtestscript; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE loadtestscript (
    script_id integer DEFAULT nextval('loadtestscript_script_id_seq'::text) NOT NULL,
    scriptname character varying(20),
    lastmodtime timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.loadtestscript OWNER TO artadmin;

--
-- TOC entry 1275 (class 1259 OID 17362)
-- Dependencies: 5
-- Name: loadtestscript_script_id_seq; Type: SEQUENCE; Schema: public; Owner: artadmin
--

CREATE SEQUENCE loadtestscript_script_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.loadtestscript_script_id_seq OWNER TO artadmin;

--
-- TOC entry 1276 (class 1259 OID 17364)
-- Dependencies: 1684 1685 5
-- Name: loadtestsummary; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE loadtestsummary (
    loadtest_id integer DEFAULT 0 NOT NULL,
    inserttime timestamp without time zone DEFAULT now() NOT NULL,
    script_id integer,
    count integer,
    avg integer,
    max integer,
    min integer,
    ninetiethpercentile integer,
    fiftiethpercentile integer
);


ALTER TABLE public.loadtestsummary OWNER TO artadmin;

--
-- TOC entry 1277 (class 1259 OID 17368)
-- Dependencies: 1686 1687 5
-- Name: loadtesttransactionminuterecords; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE loadtesttransactionminuterecords (
    loadtest_id integer DEFAULT 0 NOT NULL,
    inserttime timestamp without time zone DEFAULT now() NOT NULL,
    "time" timestamp without time zone NOT NULL,
    transaction_id integer,
    count integer,
    avg integer,
    max integer,
    min integer,
    ninetiethpercentile integer,
    fiftiethpercentile integer
);


ALTER TABLE public.loadtesttransactionminuterecords OWNER TO artadmin;

--
-- TOC entry 1278 (class 1259 OID 17372)
-- Dependencies: 1688 1689 1690 1691 5
-- Name: loadtesttransactions; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE loadtesttransactions (
    transaction_id integer DEFAULT nextval('loadtesttransactions_transac'::text) NOT NULL,
    script_id integer DEFAULT 0 NOT NULL,
    transactionname character varying(20),
    transactiondesc character varying(50),
    lastmodtime timestamp without time zone DEFAULT now() NOT NULL,
    CONSTRAINT loadtesttransactions_script_id_check CHECK ((script_id >= 0))
);


ALTER TABLE public.loadtesttransactions OWNER TO artadmin;

--
-- TOC entry 1279 (class 1259 OID 17378)
-- Dependencies: 5
-- Name: loadtesttransactions_transac; Type: SEQUENCE; Schema: public; Owner: artadmin
--

CREATE SEQUENCE loadtesttransactions_transac
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.loadtesttransactions_transac OWNER TO artadmin;

--
-- TOC entry 1280 (class 1259 OID 17380)
-- Dependencies: 1692 1693 5
-- Name: loadtesttransactionsummary; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE loadtesttransactionsummary (
    loadtest_id integer DEFAULT 0 NOT NULL,
    inserttime timestamp without time zone DEFAULT now() NOT NULL,
    transaction_id integer,
    count integer,
    avg integer,
    max integer,
    min integer,
    ninetiethpercentile integer,
    fiftiethpercentile integer
);


ALTER TABLE public.loadtesttransactionsummary OWNER TO artadmin;

--
-- TOC entry 1281 (class 1259 OID 17384)
-- Dependencies: 1694 1695 5
-- Name: machines; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE machines (
    machine_id integer DEFAULT nextval('machines_machine_id_seq'::text) NOT NULL,
    machinename character varying(50),
    shortname character varying(10),
    machinetype character(1),
    lastmodtime timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.machines OWNER TO artadmin;

--
-- TOC entry 1282 (class 1259 OID 17388)
-- Dependencies: 5
-- Name: machines_machine_id_seq; Type: SEQUENCE; Schema: public; Owner: artadmin
--

CREATE SEQUENCE machines_machine_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.machines_machine_id_seq OWNER TO artadmin;

--
-- TOC entry 1283 (class 1259 OID 17390)
-- Dependencies: 1696 1697 1698 5
-- Name: minutestatistics; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE minutestatistics (
    machine_id integer,
    inserttime timestamp without time zone DEFAULT now() NOT NULL,
    "time" timestamp without time zone DEFAULT '1970-01-01 01:00:00'::timestamp without time zone NOT NULL,
    totalloads integer,
    averageloadtime integer,
    ninetiethpercentile integer,
    twentyfifthpercentile integer,
    fiftiethpercentile integer,
    seventyfifthpercentile integer,
    maxloadtime integer,
    minloadtime integer,
    distinctusers integer,
    errorpages integer,
    thirtysecondloads integer,
    twentysecondloads integer,
    fifteensecondloads integer,
    tensecondloads integer,
    fivesecondloads integer,
    state character(1) DEFAULT 'O'::bpchar
);


ALTER TABLE public.minutestatistics OWNER TO artadmin;

--
-- TOC entry 1284 (class 1259 OID 17395)
-- Dependencies: 1699 1700 1701 5
-- Name: pages; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE pages (
    page_id integer DEFAULT nextval('pages_page_id_seq'::text) NOT NULL,
    pagename character varying(75),
    iserrorpage character(1) DEFAULT 'N'::bpchar,
    lastmodtime timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.pages OWNER TO artadmin;

--
-- TOC entry 1285 (class 1259 OID 17400)
-- Dependencies: 5
-- Name: pages_page_id_seq; Type: SEQUENCE; Schema: public; Owner: artadmin
--

CREATE SEQUENCE pages_page_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.pages_page_id_seq OWNER TO artadmin;

--
-- TOC entry 1286 (class 1259 OID 17407)
-- Dependencies: 5
-- Name: playback_requests_test_number_seq; Type: SEQUENCE; Schema: public; Owner: artadmin
--

CREATE SEQUENCE playback_requests_test_number_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.playback_requests_test_number_seq OWNER TO artadmin;

--
-- TOC entry 1287 (class 1259 OID 17409)
-- Dependencies: 1702 1703 5
-- Name: queryparameters; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE queryparameters (
    queryparameter_id integer DEFAULT nextval('queryparameters_queryparamet'::text) NOT NULL,
    queryparams text,
    lastmodtime timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.queryparameters OWNER TO artadmin;

--
-- TOC entry 1288 (class 1259 OID 17416)
-- Dependencies: 5
-- Name: queryparameters_queryparamet; Type: SEQUENCE; Schema: public; Owner: artadmin
--

CREATE SEQUENCE queryparameters_queryparamet
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.queryparameters_queryparamet OWNER TO artadmin;

--
-- TOC entry 1289 (class 1259 OID 17418)
-- Dependencies: 5
-- Name: queryparamrecords; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE queryparamrecords (
    queryparameter_id integer,
    recordpk integer
);


ALTER TABLE public.queryparamrecords OWNER TO artadmin;

--
-- TOC entry 1290 (class 1259 OID 17420)
-- Dependencies: 1704 1705 5
-- Name: rload_accessrecords; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE rload_accessrecords (
    recordpk integer DEFAULT nextval('rload_accessrecords_recordpk'::text) NOT NULL,
    inserttime timestamp without time zone DEFAULT now() NOT NULL,
    page_id integer,
    user_id integer,
    session_id integer,
    machine_id integer,
    context_id integer,
    app_id integer,
    "time" timestamp without time zone NOT NULL,
    loadtime integer
);


ALTER TABLE public.rload_accessrecords OWNER TO artadmin;

--
-- TOC entry 1291 (class 1259 OID 17424)
-- Dependencies: 5
-- Name: rload_accessrecords_recordpk; Type: SEQUENCE; Schema: public; Owner: artadmin
--

CREATE SEQUENCE rload_accessrecords_recordpk
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.rload_accessrecords_recordpk OWNER TO artadmin;

--
-- TOC entry 1292 (class 1259 OID 17426)
-- Dependencies: 1706 1707 5
-- Name: rload_apps; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE rload_apps (
    app_id integer DEFAULT nextval('rload_apps_app_id_seq'::text) NOT NULL,
    appname character varying(50),
    inserttime timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.rload_apps OWNER TO artadmin;

--
-- TOC entry 1293 (class 1259 OID 17430)
-- Dependencies: 5
-- Name: rload_apps_app_id_seq; Type: SEQUENCE; Schema: public; Owner: artadmin
--

CREATE SEQUENCE rload_apps_app_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.rload_apps_app_id_seq OWNER TO artadmin;

--
-- TOC entry 1294 (class 1259 OID 17432)
-- Dependencies: 1708 1709 5
-- Name: rload_contexts; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE rload_contexts (
    context_id integer DEFAULT nextval('rload_contexts_context_id_se'::text) NOT NULL,
    contextname character varying(50),
    inserttime timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.rload_contexts OWNER TO artadmin;

--
-- TOC entry 1295 (class 1259 OID 17436)
-- Dependencies: 5
-- Name: rload_contexts_context_id_se; Type: SEQUENCE; Schema: public; Owner: artadmin
--

CREATE SEQUENCE rload_contexts_context_id_se
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.rload_contexts_context_id_se OWNER TO artadmin;

--
-- TOC entry 1296 (class 1259 OID 17438)
-- Dependencies: 1710 1711 5
-- Name: rload_machines; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE rload_machines (
    machine_id integer DEFAULT nextval('rload_machines_machine_id_se'::text) NOT NULL,
    machinename character varying(50),
    shortname character varying(10),
    machinetype character(1),
    inserttime timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.rload_machines OWNER TO artadmin;

--
-- TOC entry 1297 (class 1259 OID 17442)
-- Dependencies: 5
-- Name: rload_machines_machine_id_se; Type: SEQUENCE; Schema: public; Owner: artadmin
--

CREATE SEQUENCE rload_machines_machine_id_se
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.rload_machines_machine_id_se OWNER TO artadmin;

--
-- TOC entry 1298 (class 1259 OID 17444)
-- Dependencies: 1712 1713 1714 5
-- Name: rload_pages; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE rload_pages (
    page_id integer DEFAULT nextval('rload_pages_page_id_seq'::text) NOT NULL,
    pagename character varying(75),
    iserrorpage character(1) DEFAULT 'N'::bpchar,
    inserttime timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.rload_pages OWNER TO artadmin;

--
-- TOC entry 1299 (class 1259 OID 17449)
-- Dependencies: 5
-- Name: rload_pages_page_id_seq; Type: SEQUENCE; Schema: public; Owner: artadmin
--

CREATE SEQUENCE rload_pages_page_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.rload_pages_page_id_seq OWNER TO artadmin;

--
-- TOC entry 1300 (class 1259 OID 17451)
-- Dependencies: 1715 1716 5
-- Name: rload_sessions; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE rload_sessions (
    session_id integer DEFAULT nextval('rload_sessions_session_id_se'::text) NOT NULL,
    ipaddress character varying(20),
    sessiontxt character varying(50),
    browsertype character varying(125),
    user_id integer,
    inserttime timestamp without time zone DEFAULT '1970-01-01 01:00:00'::timestamp without time zone NOT NULL
);


ALTER TABLE public.rload_sessions OWNER TO artadmin;

--
-- TOC entry 1301 (class 1259 OID 17455)
-- Dependencies: 5
-- Name: rload_sessions_session_id_se; Type: SEQUENCE; Schema: public; Owner: artadmin
--

CREATE SEQUENCE rload_sessions_session_id_se
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.rload_sessions_session_id_se OWNER TO artadmin;

--
-- TOC entry 1302 (class 1259 OID 17457)
-- Dependencies: 1717 1718 5
-- Name: rload_users; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE rload_users (
    user_id integer DEFAULT nextval('rload_users_user_id_seq'::text) NOT NULL,
    username character varying(25),
    fullname character varying(64),
    companyname character varying(50),
    inserttime timestamp without time zone DEFAULT '1970-01-01 01:00:00'::timestamp without time zone NOT NULL
);


ALTER TABLE public.rload_users OWNER TO artadmin;

--
-- TOC entry 1303 (class 1259 OID 17461)
-- Dependencies: 5
-- Name: rload_users_user_id_seq; Type: SEQUENCE; Schema: public; Owner: artadmin
--

CREATE SEQUENCE rload_users_user_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.rload_users_user_id_seq OWNER TO artadmin;

--
-- TOC entry 1304 (class 1259 OID 17463)
-- Dependencies: 1719 5
-- Name: sequencetable; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE sequencetable (
    sequencename character varying(255) DEFAULT ''::character varying NOT NULL,
    count integer
);


ALTER TABLE public.sequencetable OWNER TO artadmin;

--
-- TOC entry 1305 (class 1259 OID 17466)
-- Dependencies: 1720 1721 1722 1723 1724 1725 1726 1727 5
-- Name: sessions; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE sessions (
    session_id integer DEFAULT nextval('sessions_session_id_seq'::text) NOT NULL,
    ipaddress character varying(20),
    sessiontxt character varying(50),
    browsertype character varying(255),
    lastmodtime timestamp without time zone DEFAULT now() NOT NULL,
    inserttime timestamp without time zone DEFAULT '1970-01-01 01:00:00'::timestamp without time zone NOT NULL,
    user_id integer,
    context_id integer,
    sessionstarttime timestamp without time zone DEFAULT '1970-01-01 01:00:00'::timestamp without time zone NOT NULL,
    sessionendtime timestamp without time zone DEFAULT '1970-01-01 01:00:00'::timestamp without time zone NOT NULL,
    sessionhits integer,
    sessionduration bigint DEFAULT 0,
    CONSTRAINT sessions_context_id_check CHECK ((context_id >= 0)),
    CONSTRAINT sessions_sessionduration_check CHECK ((sessionduration >= 0))
);


ALTER TABLE public.sessions OWNER TO artadmin;

--
-- TOC entry 1306 (class 1259 OID 17476)
-- Dependencies: 5
-- Name: sessions_session_id_seq; Type: SEQUENCE; Schema: public; Owner: artadmin
--

CREATE SEQUENCE sessions_session_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.sessions_session_id_seq OWNER TO artadmin;

--
-- TOC entry 1307 (class 1259 OID 17478)
-- Dependencies: 1728 5
-- Name: stacktracebeancontainers; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE stacktracebeancontainers (
    trace_id integer DEFAULT 0 NOT NULL,
    jspbeancontainer text
);


ALTER TABLE public.stacktracebeancontainers OWNER TO artadmin;

--
-- TOC entry 1308 (class 1259 OID 17484)
-- Dependencies: 1729 1730 5
-- Name: stacktracedetails; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE stacktracedetails (
    trace_id integer DEFAULT 0 NOT NULL,
    row_id integer DEFAULT 0 NOT NULL,
    stack_depth integer
);


ALTER TABLE public.stacktracedetails OWNER TO artadmin;

--
-- TOC entry 1309 (class 1259 OID 17488)
-- Dependencies: 1731 5
-- Name: stacktracerows; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE stacktracerows (
    row_id integer DEFAULT nextval('stacktracerows_row_id_seq'::text) NOT NULL,
    row_message character varying(250)
);


ALTER TABLE public.stacktracerows OWNER TO artadmin;

--
-- TOC entry 1310 (class 1259 OID 17491)
-- Dependencies: 5
-- Name: stacktracerows_row_id_seq; Type: SEQUENCE; Schema: public; Owner: artadmin
--

CREATE SEQUENCE stacktracerows_row_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.stacktracerows_row_id_seq OWNER TO artadmin;

--
-- TOC entry 1311 (class 1259 OID 17493)
-- Dependencies: 1732 1733 1734 5
-- Name: stacktraces; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE stacktraces (
    trace_id integer DEFAULT 0 NOT NULL,
    trace_key character varying(50),
    trace_message character varying(250),
    trace_time timestamp without time zone DEFAULT '1970-01-01 01:00:00'::timestamp without time zone NOT NULL,
    art_user_id integer,
    CONSTRAINT stacktraces_trace_id_check CHECK ((trace_id >= 0))
);


ALTER TABLE public.stacktraces OWNER TO artadmin;

--
-- TOC entry 1312 (class 1259 OID 17498)
-- Dependencies: 1735 1736 5
-- Name: users; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE users (
    user_id integer DEFAULT nextval('users_user_id_seq'::text) NOT NULL,
    username character varying(25),
    fullname character varying(64),
    companyname character varying(50),
    lastmodtime timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.users OWNER TO artadmin;

--
-- TOC entry 1313 (class 1259 OID 17502)
-- Dependencies: 5
-- Name: users_user_id_seq; Type: SEQUENCE; Schema: public; Owner: artadmin
--

CREATE SEQUENCE users_user_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.users_user_id_seq OWNER TO artadmin;

--
-- TOC entry 1818 (class 16386 OID 156199573)
-- Dependencies: 1314 1314 1314 1314
-- Name: SESSION_AND_REQUEST_TOKEN_UK; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY loadtest_requests
    ADD CONSTRAINT "SESSION_AND_REQUEST_TOKEN_UK" UNIQUE (loadtest_export_id, session_id, request_token);


ALTER INDEX public."SESSION_AND_REQUEST_TOKEN_UK" OWNER TO artadmin;

--
-- TOC entry 1739 (class 16386 OID 145655305)
-- Dependencies: 1242 1242
-- Name: accessrecords_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY accessrecords
    ADD CONSTRAINT accessrecords_pkey PRIMARY KEY (recordpk);


ALTER INDEX public.accessrecords_pkey OWNER TO artadmin;

--
-- TOC entry 1742 (class 16386 OID 145655307)
-- Dependencies: 1244 1244
-- Name: accumulator_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY accumulator
    ADD CONSTRAINT accumulator_pkey PRIMARY KEY (accumulatorstat_id);


ALTER INDEX public.accumulator_pkey OWNER TO artadmin;

--
-- TOC entry 1746 (class 16386 OID 145655309)
-- Dependencies: 1246 1246
-- Name: accumulatorevent_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY accumulatorevent
    ADD CONSTRAINT accumulatorevent_pkey PRIMARY KEY (accumulatorevent_id);


ALTER INDEX public.accumulatorevent_pkey OWNER TO artadmin;

--
-- TOC entry 1749 (class 16386 OID 145655311)
-- Dependencies: 1249 1249
-- Name: apps_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY apps
    ADD CONSTRAINT apps_pkey PRIMARY KEY (app_id);


ALTER INDEX public.apps_pkey OWNER TO artadmin;

--
-- TOC entry 1751 (class 16386 OID 145655313)
-- Dependencies: 1251 1251
-- Name: branches_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY branches
    ADD CONSTRAINT branches_pkey PRIMARY KEY (branch_tag_id);


ALTER INDEX public.branches_pkey OWNER TO artadmin;

--
-- TOC entry 1753 (class 16386 OID 145655315)
-- Dependencies: 1253 1253
-- Name: browsers_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY browsers
    ADD CONSTRAINT browsers_pkey PRIMARY KEY (browser_id);


ALTER INDEX public.browsers_pkey OWNER TO artadmin;

--
-- TOC entry 1756 (class 16386 OID 145655317)
-- Dependencies: 1256 1256
-- Name: contexts_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY contexts
    ADD CONSTRAINT contexts_pkey PRIMARY KEY (context_id);


ALTER INDEX public.contexts_pkey OWNER TO artadmin;

--
-- TOC entry 1759 (class 16386 OID 145655319)
-- Dependencies: 1259 1259
-- Name: dailypageloadtimes_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY dailypageloadtimes
    ADD CONSTRAINT dailypageloadtimes_pkey PRIMARY KEY (dailyloadtime_id);


ALTER INDEX public.dailypageloadtimes_pkey OWNER TO artadmin;

--
-- TOC entry 1761 (class 16386 OID 145655321)
-- Dependencies: 1261 1261
-- Name: dailysummary_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY dailysummary
    ADD CONSTRAINT dailysummary_pkey PRIMARY KEY ("day");


ALTER INDEX public.dailysummary_pkey OWNER TO artadmin;

--
-- TOC entry 1763 (class 16386 OID 145655323)
-- Dependencies: 1263 1263
-- Name: externalaccessrecords_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY externalaccessrecords
    ADD CONSTRAINT externalaccessrecords_pkey PRIMARY KEY (recordpk);


ALTER INDEX public.externalaccessrecords_pkey OWNER TO artadmin;

--
-- TOC entry 1766 (class 16386 OID 145655325)
-- Dependencies: 1266 1266
-- Name: externalstats_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY externalstats
    ADD CONSTRAINT externalstats_pkey PRIMARY KEY (classification_id);


ALTER INDEX public.externalstats_pkey OWNER TO artadmin;

--
-- TOC entry 1768 (class 16386 OID 145655327)
-- Dependencies: 1268 1268
-- Name: fivesecondloads_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY fivesecondloads
    ADD CONSTRAINT fivesecondloads_pkey PRIMARY KEY (recordpk);


ALTER INDEX public.fivesecondloads_pkey OWNER TO artadmin;

--
-- TOC entry 1770 (class 16386 OID 145655329)
-- Dependencies: 1272 1272
-- Name: loadtests_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY loadtests
    ADD CONSTRAINT loadtests_pkey PRIMARY KEY (loadtest_id);


ALTER INDEX public.loadtests_pkey OWNER TO artadmin;

--
-- TOC entry 1772 (class 16386 OID 145655331)
-- Dependencies: 1274 1274
-- Name: loadtestscript_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY loadtestscript
    ADD CONSTRAINT loadtestscript_pkey PRIMARY KEY (script_id);


ALTER INDEX public.loadtestscript_pkey OWNER TO artadmin;

--
-- TOC entry 1774 (class 16386 OID 145655333)
-- Dependencies: 1278 1278
-- Name: loadtesttransactions_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY loadtesttransactions
    ADD CONSTRAINT loadtesttransactions_pkey PRIMARY KEY (transaction_id);


ALTER INDEX public.loadtesttransactions_pkey OWNER TO artadmin;

--
-- TOC entry 1776 (class 16386 OID 145655335)
-- Dependencies: 1281 1281
-- Name: machines_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY machines
    ADD CONSTRAINT machines_pkey PRIMARY KEY (machine_id);


ALTER INDEX public.machines_pkey OWNER TO artadmin;

--
-- TOC entry 1780 (class 16386 OID 145655337)
-- Dependencies: 1284 1284
-- Name: pages_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY pages
    ADD CONSTRAINT pages_pkey PRIMARY KEY (page_id);


ALTER INDEX public.pages_pkey OWNER TO artadmin;

--
-- TOC entry 1820 (class 16386 OID 156475023)
-- Dependencies: 1315 1315
-- Name: pk_loadtest_export; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY loadtest_export
    ADD CONSTRAINT pk_loadtest_export PRIMARY KEY (loadtest_export_id);


ALTER INDEX public.pk_loadtest_export OWNER TO artadmin;

--
-- TOC entry 1782 (class 16386 OID 145655339)
-- Dependencies: 1287 1287
-- Name: queryparameters_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY queryparameters
    ADD CONSTRAINT queryparameters_pkey PRIMARY KEY (queryparameter_id);


ALTER INDEX public.queryparameters_pkey OWNER TO artadmin;

--
-- TOC entry 1786 (class 16386 OID 145655341)
-- Dependencies: 1290 1290
-- Name: rload_accessrecords_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY rload_accessrecords
    ADD CONSTRAINT rload_accessrecords_pkey PRIMARY KEY (recordpk);


ALTER INDEX public.rload_accessrecords_pkey OWNER TO artadmin;

--
-- TOC entry 1788 (class 16386 OID 145655343)
-- Dependencies: 1292 1292
-- Name: rload_apps_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY rload_apps
    ADD CONSTRAINT rload_apps_pkey PRIMARY KEY (app_id);


ALTER INDEX public.rload_apps_pkey OWNER TO artadmin;

--
-- TOC entry 1790 (class 16386 OID 145655345)
-- Dependencies: 1294 1294
-- Name: rload_contexts_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY rload_contexts
    ADD CONSTRAINT rload_contexts_pkey PRIMARY KEY (context_id);


ALTER INDEX public.rload_contexts_pkey OWNER TO artadmin;

--
-- TOC entry 1792 (class 16386 OID 145655347)
-- Dependencies: 1296 1296
-- Name: rload_machines_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY rload_machines
    ADD CONSTRAINT rload_machines_pkey PRIMARY KEY (machine_id);


ALTER INDEX public.rload_machines_pkey OWNER TO artadmin;

--
-- TOC entry 1794 (class 16386 OID 145655349)
-- Dependencies: 1298 1298
-- Name: rload_pages_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY rload_pages
    ADD CONSTRAINT rload_pages_pkey PRIMARY KEY (page_id);


ALTER INDEX public.rload_pages_pkey OWNER TO artadmin;

--
-- TOC entry 1796 (class 16386 OID 145655351)
-- Dependencies: 1300 1300
-- Name: rload_sessions_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY rload_sessions
    ADD CONSTRAINT rload_sessions_pkey PRIMARY KEY (session_id);


ALTER INDEX public.rload_sessions_pkey OWNER TO artadmin;

--
-- TOC entry 1798 (class 16386 OID 145655353)
-- Dependencies: 1302 1302
-- Name: rload_users_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY rload_users
    ADD CONSTRAINT rload_users_pkey PRIMARY KEY (user_id);


ALTER INDEX public.rload_users_pkey OWNER TO artadmin;

--
-- TOC entry 1800 (class 16386 OID 145655355)
-- Dependencies: 1304 1304
-- Name: sequencetable_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY sequencetable
    ADD CONSTRAINT sequencetable_pkey PRIMARY KEY (sequencename);


ALTER INDEX public.sequencetable_pkey OWNER TO artadmin;

--
-- TOC entry 1802 (class 16386 OID 145655357)
-- Dependencies: 1305 1305
-- Name: sessions_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY sessions
    ADD CONSTRAINT sessions_pkey PRIMARY KEY (session_id);


ALTER INDEX public.sessions_pkey OWNER TO artadmin;

--
-- TOC entry 1810 (class 16386 OID 145655359)
-- Dependencies: 1309 1309
-- Name: stacktracerows_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY stacktracerows
    ADD CONSTRAINT stacktracerows_pkey PRIMARY KEY (row_id);


ALTER INDEX public.stacktracerows_pkey OWNER TO artadmin;

--
-- TOC entry 1813 (class 16386 OID 145655361)
-- Dependencies: 1311 1311
-- Name: stacktraces_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY stacktraces
    ADD CONSTRAINT stacktraces_pkey PRIMARY KEY (trace_id);


ALTER INDEX public.stacktraces_pkey OWNER TO artadmin;

--
-- TOC entry 1815 (class 16386 OID 145655363)
-- Dependencies: 1312 1312
-- Name: users_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY users
    ADD CONSTRAINT users_pkey PRIMARY KEY (user_id);


ALTER INDEX public.users_pkey OWNER TO artadmin;

--
-- TOC entry 1740 (class 1259 OID 145655364)
-- Dependencies: 1242
-- Name: accessrecords_time2_accessrecords_index; Type: INDEX; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE INDEX accessrecords_time2_accessrecords_index ON accessrecords USING btree ("time");


ALTER INDEX public.accessrecords_time2_accessrecords_index OWNER TO artadmin;

--
-- TOC entry 1743 (class 1259 OID 145655365)
-- Dependencies: 1246 1246 1246
-- Name: accumulator_context_accumulatorstat_accumulatorevent_index; Type: INDEX; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE INDEX accumulator_context_accumulatorstat_accumulatorevent_index ON accumulatorevent USING btree ("time", accumulatorstat_id, context_id);


ALTER INDEX public.accumulator_context_accumulatorstat_accumulatorevent_index OWNER TO artadmin;

--
-- TOC entry 1744 (class 1259 OID 145655366)
-- Dependencies: 1246
-- Name: accumulator_time_index_accumulatorevent_index; Type: INDEX; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE INDEX accumulator_time_index_accumulatorevent_index ON accumulatorevent USING btree ("time");


ALTER INDEX public.accumulator_time_index_accumulatorevent_index OWNER TO artadmin;

--
-- TOC entry 1747 (class 1259 OID 145655385)
-- Dependencies: 1248 1248
-- Name: accumulatorstats_id_time_index_accumulatorstats_index; Type: INDEX; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE UNIQUE INDEX accumulatorstats_id_time_index_accumulatorstats_index ON accumulatorstats USING btree (accumulatorstat_id, "time");


ALTER INDEX public.accumulatorstats_id_time_index_accumulatorstats_index OWNER TO artadmin;

--
-- TOC entry 1754 (class 1259 OID 145655386)
-- Dependencies: 1255 1255
-- Name: browserstats_id_day_browser_id_browserstats_index; Type: INDEX; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE UNIQUE INDEX browserstats_id_day_browser_id_browserstats_index ON browserstats USING btree ("day", browser_id);


ALTER INDEX public.browserstats_id_day_browser_id_browserstats_index OWNER TO artadmin;

--
-- TOC entry 1757 (class 1259 OID 145655387)
-- Dependencies: 1259 1259 1259
-- Name: dailyloadtimes_id_day_page_context_dailypageloadtimes_index; Type: INDEX; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE UNIQUE INDEX dailyloadtimes_id_day_page_context_dailypageloadtimes_index ON dailypageloadtimes USING btree ("day", page_id, context_id);


ALTER INDEX public.dailyloadtimes_id_day_page_context_dailypageloadtimes_index OWNER TO artadmin;

--
-- TOC entry 1764 (class 1259 OID 145655388)
-- Dependencies: 1265 1265
-- Name: external_minute_statistics_time_classification_index; Type: INDEX; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE INDEX external_minute_statistics_time_classification_index ON externalminutestatistics USING btree ("time", classification_id);


ALTER INDEX public.external_minute_statistics_time_classification_index OWNER TO artadmin;

--
-- TOC entry 1777 (class 1259 OID 145655389)
-- Dependencies: 1283
-- Name: minute_statistics_time_index_minutestatistics_index; Type: INDEX; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE INDEX minute_statistics_time_index_minutestatistics_index ON minutestatistics USING btree ("time");


ALTER INDEX public.minute_statistics_time_index_minutestatistics_index OWNER TO artadmin;

--
-- TOC entry 1778 (class 1259 OID 145655390)
-- Dependencies: 1284
-- Name: pages_page_name_index_pages_index; Type: INDEX; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE INDEX pages_page_name_index_pages_index ON pages USING btree (pagename);


ALTER INDEX public.pages_page_name_index_pages_index OWNER TO artadmin;

--
-- TOC entry 1783 (class 1259 OID 145655391)
-- Dependencies: 1287
-- Name: queryparams_index_queryparameters_index; Type: INDEX; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE INDEX queryparams_index_queryparameters_index ON queryparameters USING btree (queryparams);


ALTER INDEX public.queryparams_index_queryparameters_index OWNER TO artadmin;

--
-- TOC entry 1784 (class 1259 OID 145655392)
-- Dependencies: 1289
-- Name: recordpk_id_queryparamrecords_index; Type: INDEX; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE INDEX recordpk_id_queryparamrecords_index ON queryparamrecords USING btree (recordpk);


ALTER INDEX public.recordpk_id_queryparamrecords_index OWNER TO artadmin;

--
-- TOC entry 1806 (class 1259 OID 145655393)
-- Dependencies: 1308
-- Name: row_id_ind_stacktracedetails_index; Type: INDEX; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE INDEX row_id_ind_stacktracedetails_index ON stacktracedetails USING btree (row_id);


ALTER INDEX public.row_id_ind_stacktracedetails_index OWNER TO artadmin;

--
-- TOC entry 1803 (class 1259 OID 145655394)
-- Dependencies: 1305 1305
-- Name: sessions_session_txt_index_sessions_index; Type: INDEX; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE INDEX sessions_session_txt_index_sessions_index ON sessions USING btree (sessiontxt, ipaddress);


ALTER INDEX public.sessions_session_txt_index_sessions_index OWNER TO artadmin;

--
-- TOC entry 1804 (class 1259 OID 145655395)
-- Dependencies: 1305 1305
-- Name: sessions_time_indenx_sessions_index; Type: INDEX; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE INDEX sessions_time_indenx_sessions_index ON sessions USING btree (sessionstarttime, sessionendtime);


ALTER INDEX public.sessions_time_indenx_sessions_index OWNER TO artadmin;

--
-- TOC entry 1811 (class 1259 OID 145655396)
-- Dependencies: 1311
-- Name: stacktrace_trace_kay_stacktraces_index; Type: INDEX; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE INDEX stacktrace_trace_kay_stacktraces_index ON stacktraces USING btree (trace_key);


ALTER INDEX public.stacktrace_trace_kay_stacktraces_index OWNER TO artadmin;

--
-- TOC entry 1805 (class 1259 OID 145655397)
-- Dependencies: 1307
-- Name: trace_id_ind_stacktracebeancontainers_index; Type: INDEX; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE INDEX trace_id_ind_stacktracebeancontainers_index ON stacktracebeancontainers USING btree (trace_id);


ALTER INDEX public.trace_id_ind_stacktracebeancontainers_index OWNER TO artadmin;

--
-- TOC entry 1807 (class 1259 OID 145655398)
-- Dependencies: 1308
-- Name: trace_id_ind_stacktracedetails_index; Type: INDEX; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE INDEX trace_id_ind_stacktracedetails_index ON stacktracedetails USING btree (trace_id);


ALTER INDEX public.trace_id_ind_stacktracedetails_index OWNER TO artadmin;

--
-- TOC entry 1808 (class 1259 OID 145655399)
-- Dependencies: 1308 1308
-- Name: trace_id_stacktracedetails_index; Type: INDEX; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE UNIQUE INDEX trace_id_stacktracedetails_index ON stacktracedetails USING btree (trace_id, stack_depth);


ALTER INDEX public.trace_id_stacktracedetails_index OWNER TO artadmin;

--
-- TOC entry 1816 (class 1259 OID 145655400)
-- Dependencies: 1312
-- Name: users_user_name_index_users_index; Type: INDEX; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE INDEX users_user_name_index_users_index ON users USING btree (username);


ALTER INDEX public.users_user_name_index_users_index OWNER TO artadmin;

--
-- TOC entry 1827 (class 16412 OID 161459172)
-- Dependencies: 21 1259
-- Name: update_lastmodtime; Type: TRIGGER; Schema: public; Owner: artadmin
--

CREATE TRIGGER update_lastmodtime
    BEFORE UPDATE ON dailypageloadtimes
    FOR EACH ROW
    EXECUTE PROCEDURE setlastmodtime();


--
-- TOC entry 1826 (class 16412 OID 161594900)
-- Dependencies: 21 1258
-- Name: update_lastmodtime; Type: TRIGGER; Schema: public; Owner: artadmin
--

CREATE TRIGGER update_lastmodtime
    BEFORE UPDATE ON dailycontextstats
    FOR EACH ROW
    EXECUTE PROCEDURE setlastmodtime();


--
-- TOC entry 1825 (class 16412 OID 161600817)
-- Dependencies: 21 1256
-- Name: update_lastmodtime; Type: TRIGGER; Schema: public; Owner: artadmin
--

CREATE TRIGGER update_lastmodtime
    BEFORE UPDATE ON contexts
    FOR EACH ROW
    EXECUTE PROCEDURE setlastmodtime();


--
-- TOC entry 1824 (class 16412 OID 161600818)
-- Dependencies: 21 1251
-- Name: update_lastmodtime; Type: TRIGGER; Schema: public; Owner: artadmin
--

CREATE TRIGGER update_lastmodtime
    BEFORE UPDATE ON branches
    FOR EACH ROW
    EXECUTE PROCEDURE setlastmodtime();


--
-- TOC entry 1823 (class 16412 OID 161600877)
-- Dependencies: 21 1249
-- Name: update_lastmodtime; Type: TRIGGER; Schema: public; Owner: artadmin
--

CREATE TRIGGER update_lastmodtime
    BEFORE UPDATE ON apps
    FOR EACH ROW
    EXECUTE PROCEDURE setlastmodtime();


--
-- TOC entry 1822 (class 16412 OID 161602958)
-- Dependencies: 21 1244
-- Name: update_lastmodtime; Type: TRIGGER; Schema: public; Owner: artadmin
--

CREATE TRIGGER update_lastmodtime
    BEFORE UPDATE ON accumulator
    FOR EACH ROW
    EXECUTE PROCEDURE setlastmodtime();


--
-- TOC entry 1828 (class 16412 OID 161619840)
-- Dependencies: 21 1261
-- Name: update_lastmodtime; Type: TRIGGER; Schema: public; Owner: artadmin
--

CREATE TRIGGER update_lastmodtime
    BEFORE UPDATE ON dailysummary
    FOR EACH ROW
    EXECUTE PROCEDURE setlastmodtime();


--
-- TOC entry 1829 (class 16412 OID 161633916)
-- Dependencies: 21 1263
-- Name: update_lastmodtime; Type: TRIGGER; Schema: public; Owner: artadmin
--

CREATE TRIGGER update_lastmodtime
    BEFORE UPDATE ON externalaccessrecords
    FOR EACH ROW
    EXECUTE PROCEDURE setlastmodtime();


--
-- TOC entry 1830 (class 16412 OID 161642675)
-- Dependencies: 21 1265
-- Name: update_lastmodtime; Type: TRIGGER; Schema: public; Owner: artadmin
--

CREATE TRIGGER update_lastmodtime
    BEFORE UPDATE ON externalminutestatistics
    FOR EACH ROW
    EXECUTE PROCEDURE setlastmodtime();


--
-- TOC entry 1831 (class 16412 OID 161652789)
-- Dependencies: 21 1268
-- Name: update_lastmodtime; Type: TRIGGER; Schema: public; Owner: artadmin
--

CREATE TRIGGER update_lastmodtime
    BEFORE UPDATE ON fivesecondloads
    FOR EACH ROW
    EXECUTE PROCEDURE setlastmodtime();


--
-- TOC entry 1832 (class 16412 OID 161673102)
-- Dependencies: 21 1272
-- Name: update_lastmodtime; Type: TRIGGER; Schema: public; Owner: artadmin
--

CREATE TRIGGER update_lastmodtime
    BEFORE UPDATE ON loadtests
    FOR EACH ROW
    EXECUTE PROCEDURE setlastmodtime();


--
-- TOC entry 1833 (class 16412 OID 161681203)
-- Dependencies: 21 1274
-- Name: update_lastmodtime; Type: TRIGGER; Schema: public; Owner: artadmin
--

CREATE TRIGGER update_lastmodtime
    BEFORE UPDATE ON loadtestscript
    FOR EACH ROW
    EXECUTE PROCEDURE setlastmodtime();


--
-- TOC entry 1834 (class 16412 OID 161693559)
-- Dependencies: 21 1278
-- Name: update_lastmodtime; Type: TRIGGER; Schema: public; Owner: artadmin
--

CREATE TRIGGER update_lastmodtime
    BEFORE UPDATE ON loadtesttransactions
    FOR EACH ROW
    EXECUTE PROCEDURE setlastmodtime();


--
-- TOC entry 1835 (class 16412 OID 161703740)
-- Dependencies: 21 1281
-- Name: update_lastmodtime; Type: TRIGGER; Schema: public; Owner: artadmin
--

CREATE TRIGGER update_lastmodtime
    BEFORE UPDATE ON machines
    FOR EACH ROW
    EXECUTE PROCEDURE setlastmodtime();


--
-- TOC entry 1836 (class 16412 OID 161822155)
-- Dependencies: 21 1284
-- Name: update_lastmodtime; Type: TRIGGER; Schema: public; Owner: artadmin
--

CREATE TRIGGER update_lastmodtime
    BEFORE UPDATE ON pages
    FOR EACH ROW
    EXECUTE PROCEDURE setlastmodtime();


--
-- TOC entry 1837 (class 16412 OID 161830496)
-- Dependencies: 21 1287
-- Name: update_lastmodtime; Type: TRIGGER; Schema: public; Owner: artadmin
--

CREATE TRIGGER update_lastmodtime
    BEFORE UPDATE ON queryparameters
    FOR EACH ROW
    EXECUTE PROCEDURE setlastmodtime();


--
-- TOC entry 1838 (class 16412 OID 161853980)
-- Dependencies: 21 1305
-- Name: update_lastmodtime; Type: TRIGGER; Schema: public; Owner: artadmin
--

CREATE TRIGGER update_lastmodtime
    BEFORE UPDATE ON sessions
    FOR EACH ROW
    EXECUTE PROCEDURE setlastmodtime();


--
-- TOC entry 1839 (class 16412 OID 161865242)
-- Dependencies: 21 1312
-- Name: update_lastmodtime; Type: TRIGGER; Schema: public; Owner: artadmin
--

CREATE TRIGGER update_lastmodtime
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE PROCEDURE setlastmodtime();


--
-- TOC entry 1821 (class 16386 OID 157158930)
-- Dependencies: 1314 1315 1819
-- Name: loadtest_export; Type: FK CONSTRAINT; Schema: public; Owner: artadmin
--

ALTER TABLE ONLY loadtest_requests
    ADD CONSTRAINT loadtest_export FOREIGN KEY (loadtest_export_id) REFERENCES loadtest_export(loadtest_export_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- TOC entry 1921 (class 0 OID 0)
-- Name: DUMP TIMESTAMP; Type: DUMP TIMESTAMP; Schema: -; Owner: 
--

-- Completed on 2005-02-25 14:53:54 Central Standard Time


--
-- TOC entry 1844 (class 0 OID 0)
-- Dependencies: 5
-- Name: public; Type: ACL; Schema: -; Owner: pgsql
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM pgsql;
GRANT ALL ON SCHEMA public TO pgsql;
GRANT ALL ON SCHEMA public TO GROUP "artUsers";


--
-- TOC entry 1845 (class 0 OID 0)
-- Dependencies: 19
-- Name: getqueryparams(integer); Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON FUNCTION getqueryparams("RecordPK" integer) FROM PUBLIC;
REVOKE ALL ON FUNCTION getqueryparams("RecordPK" integer) FROM artadmin;
GRANT ALL ON FUNCTION getqueryparams("RecordPK" integer) TO artadmin;
GRANT ALL ON FUNCTION getqueryparams("RecordPK" integer) TO PUBLIC;
GRANT ALL ON FUNCTION getqueryparams("RecordPK" integer) TO GROUP "artUsers";


--
-- TOC entry 1846 (class 0 OID 0)
-- Dependencies: 20
-- Name: gettesturlstrings(character varying, character varying, timestamp without time zone, timestamp without time zone, character varying, character varying); Type: ACL; Schema: public; Owner: pgsql
--

REVOKE ALL ON FUNCTION gettesturlstrings("fromContext" character varying, "fromMachines" character varying, "fromStartTime" timestamp without time zone, "fromStopTime" timestamp without time zone, "toHost" character varying, "toContext" character varying) FROM PUBLIC;
REVOKE ALL ON FUNCTION gettesturlstrings("fromContext" character varying, "fromMachines" character varying, "fromStartTime" timestamp without time zone, "fromStopTime" timestamp without time zone, "toHost" character varying, "toContext" character varying) FROM pgsql;
GRANT ALL ON FUNCTION gettesturlstrings("fromContext" character varying, "fromMachines" character varying, "fromStartTime" timestamp without time zone, "fromStopTime" timestamp without time zone, "toHost" character varying, "toContext" character varying) TO pgsql;
GRANT ALL ON FUNCTION gettesturlstrings("fromContext" character varying, "fromMachines" character varying, "fromStartTime" timestamp without time zone, "fromStopTime" timestamp without time zone, "toHost" character varying, "toContext" character varying) TO GROUP "artUsers";


--
-- TOC entry 1847 (class 0 OID 0)
-- Dependencies: 1242
-- Name: accessrecords; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE accessrecords FROM PUBLIC;
REVOKE ALL ON TABLE accessrecords FROM artadmin;
GRANT ALL ON TABLE accessrecords TO artadmin;
GRANT ALL ON TABLE accessrecords TO GROUP "artUsers";


--
-- TOC entry 1848 (class 0 OID 0)
-- Dependencies: 1243
-- Name: accessrecords_recordpk_seq; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE accessrecords_recordpk_seq FROM PUBLIC;
REVOKE ALL ON TABLE accessrecords_recordpk_seq FROM artadmin;
GRANT ALL ON TABLE accessrecords_recordpk_seq TO artadmin;
GRANT ALL ON TABLE accessrecords_recordpk_seq TO GROUP "artUsers";


--
-- TOC entry 1849 (class 0 OID 0)
-- Dependencies: 1244
-- Name: accumulator; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE accumulator FROM PUBLIC;
REVOKE ALL ON TABLE accumulator FROM artadmin;
GRANT ALL ON TABLE accumulator TO artadmin;
GRANT ALL ON TABLE accumulator TO GROUP "artUsers";


--
-- TOC entry 1850 (class 0 OID 0)
-- Dependencies: 1245
-- Name: accumulator_accumulatorstat_; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE accumulator_accumulatorstat_ FROM PUBLIC;
REVOKE ALL ON TABLE accumulator_accumulatorstat_ FROM artadmin;
GRANT ALL ON TABLE accumulator_accumulatorstat_ TO artadmin;
GRANT ALL ON TABLE accumulator_accumulatorstat_ TO GROUP "artUsers";


--
-- TOC entry 1851 (class 0 OID 0)
-- Dependencies: 1246
-- Name: accumulatorevent; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE accumulatorevent FROM PUBLIC;
REVOKE ALL ON TABLE accumulatorevent FROM artadmin;
GRANT ALL ON TABLE accumulatorevent TO artadmin;
GRANT ALL ON TABLE accumulatorevent TO GROUP "artUsers";


--
-- TOC entry 1852 (class 0 OID 0)
-- Dependencies: 1247
-- Name: accumulatorevent_accumulator; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE accumulatorevent_accumulator FROM PUBLIC;
REVOKE ALL ON TABLE accumulatorevent_accumulator FROM artadmin;
GRANT ALL ON TABLE accumulatorevent_accumulator TO artadmin;
GRANT ALL ON TABLE accumulatorevent_accumulator TO GROUP "artUsers";


--
-- TOC entry 1853 (class 0 OID 0)
-- Dependencies: 1248
-- Name: accumulatorstats; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE accumulatorstats FROM PUBLIC;
REVOKE ALL ON TABLE accumulatorstats FROM artadmin;
GRANT ALL ON TABLE accumulatorstats TO artadmin;
GRANT ALL ON TABLE accumulatorstats TO GROUP "artUsers";


--
-- TOC entry 1854 (class 0 OID 0)
-- Dependencies: 1249
-- Name: apps; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE apps FROM PUBLIC;
REVOKE ALL ON TABLE apps FROM artadmin;
GRANT ALL ON TABLE apps TO artadmin;
GRANT ALL ON TABLE apps TO GROUP "artUsers";


--
-- TOC entry 1855 (class 0 OID 0)
-- Dependencies: 1250
-- Name: apps_app_id_seq; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE apps_app_id_seq FROM PUBLIC;
REVOKE ALL ON TABLE apps_app_id_seq FROM artadmin;
GRANT ALL ON TABLE apps_app_id_seq TO artadmin;
GRANT ALL ON TABLE apps_app_id_seq TO GROUP "artUsers";


--
-- TOC entry 1856 (class 0 OID 0)
-- Dependencies: 1251
-- Name: branches; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE branches FROM PUBLIC;
REVOKE ALL ON TABLE branches FROM artadmin;
GRANT ALL ON TABLE branches TO artadmin;
GRANT ALL ON TABLE branches TO GROUP "artUsers";


--
-- TOC entry 1857 (class 0 OID 0)
-- Dependencies: 1252
-- Name: branches_branch_tag_id_seq; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE branches_branch_tag_id_seq FROM PUBLIC;
REVOKE ALL ON TABLE branches_branch_tag_id_seq FROM artadmin;
GRANT ALL ON TABLE branches_branch_tag_id_seq TO artadmin;
GRANT ALL ON TABLE branches_branch_tag_id_seq TO GROUP "artUsers";


--
-- TOC entry 1858 (class 0 OID 0)
-- Dependencies: 1253
-- Name: browsers; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE browsers FROM PUBLIC;
REVOKE ALL ON TABLE browsers FROM artadmin;
GRANT ALL ON TABLE browsers TO artadmin;
GRANT ALL ON TABLE browsers TO GROUP "artUsers";


--
-- TOC entry 1859 (class 0 OID 0)
-- Dependencies: 1254
-- Name: browsers_browser_id_seq; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE browsers_browser_id_seq FROM PUBLIC;
REVOKE ALL ON TABLE browsers_browser_id_seq FROM artadmin;
GRANT ALL ON TABLE browsers_browser_id_seq TO artadmin;
GRANT ALL ON TABLE browsers_browser_id_seq TO GROUP "artUsers";


--
-- TOC entry 1860 (class 0 OID 0)
-- Dependencies: 1255
-- Name: browserstats; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE browserstats FROM PUBLIC;
REVOKE ALL ON TABLE browserstats FROM artadmin;
GRANT ALL ON TABLE browserstats TO artadmin;
GRANT ALL ON TABLE browserstats TO GROUP "artUsers";


--
-- TOC entry 1861 (class 0 OID 0)
-- Dependencies: 1256
-- Name: contexts; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE contexts FROM PUBLIC;
REVOKE ALL ON TABLE contexts FROM artadmin;
GRANT ALL ON TABLE contexts TO artadmin;
GRANT ALL ON TABLE contexts TO GROUP "artUsers";


--
-- TOC entry 1862 (class 0 OID 0)
-- Dependencies: 1257
-- Name: contexts_context_id_seq; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE contexts_context_id_seq FROM PUBLIC;
REVOKE ALL ON TABLE contexts_context_id_seq FROM artadmin;
GRANT ALL ON TABLE contexts_context_id_seq TO artadmin;
GRANT ALL ON TABLE contexts_context_id_seq TO GROUP "artUsers";


--
-- TOC entry 1863 (class 0 OID 0)
-- Dependencies: 1258
-- Name: dailycontextstats; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE dailycontextstats FROM PUBLIC;
REVOKE ALL ON TABLE dailycontextstats FROM artadmin;
GRANT ALL ON TABLE dailycontextstats TO artadmin;
GRANT ALL ON TABLE dailycontextstats TO GROUP "artUsers";


--
-- TOC entry 1864 (class 0 OID 0)
-- Dependencies: 1259
-- Name: dailypageloadtimes; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE dailypageloadtimes FROM PUBLIC;
REVOKE ALL ON TABLE dailypageloadtimes FROM artadmin;
GRANT ALL ON TABLE dailypageloadtimes TO artadmin;
GRANT ALL ON TABLE dailypageloadtimes TO GROUP "artUsers";


--
-- TOC entry 1865 (class 0 OID 0)
-- Dependencies: 1260
-- Name: dailypageloadtimes_dailyload; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE dailypageloadtimes_dailyload FROM PUBLIC;
REVOKE ALL ON TABLE dailypageloadtimes_dailyload FROM artadmin;
GRANT ALL ON TABLE dailypageloadtimes_dailyload TO artadmin;
GRANT ALL ON TABLE dailypageloadtimes_dailyload TO GROUP "artUsers";


--
-- TOC entry 1866 (class 0 OID 0)
-- Dependencies: 1261
-- Name: dailysummary; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE dailysummary FROM PUBLIC;
REVOKE ALL ON TABLE dailysummary FROM artadmin;
GRANT ALL ON TABLE dailysummary TO artadmin;
GRANT ALL ON TABLE dailysummary TO GROUP "artUsers";


--
-- TOC entry 1867 (class 0 OID 0)
-- Dependencies: 1262
-- Name: deployments; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE deployments FROM PUBLIC;
REVOKE ALL ON TABLE deployments FROM artadmin;
GRANT ALL ON TABLE deployments TO artadmin;
GRANT ALL ON TABLE deployments TO GROUP "artUsers";


--
-- TOC entry 1868 (class 0 OID 0)
-- Dependencies: 1263
-- Name: externalaccessrecords; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE externalaccessrecords FROM PUBLIC;
REVOKE ALL ON TABLE externalaccessrecords FROM artadmin;
GRANT ALL ON TABLE externalaccessrecords TO artadmin;
GRANT ALL ON TABLE externalaccessrecords TO GROUP "artUsers";


--
-- TOC entry 1869 (class 0 OID 0)
-- Dependencies: 1264
-- Name: externalaccessrecords_record; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE externalaccessrecords_record FROM PUBLIC;
REVOKE ALL ON TABLE externalaccessrecords_record FROM artadmin;
GRANT ALL ON TABLE externalaccessrecords_record TO artadmin;
GRANT ALL ON TABLE externalaccessrecords_record TO GROUP "artUsers";


--
-- TOC entry 1870 (class 0 OID 0)
-- Dependencies: 1265
-- Name: externalminutestatistics; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE externalminutestatistics FROM PUBLIC;
REVOKE ALL ON TABLE externalminutestatistics FROM artadmin;
GRANT ALL ON TABLE externalminutestatistics TO artadmin;
GRANT ALL ON TABLE externalminutestatistics TO GROUP "artUsers";


--
-- TOC entry 1871 (class 0 OID 0)
-- Dependencies: 1266
-- Name: externalstats; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE externalstats FROM PUBLIC;
REVOKE ALL ON TABLE externalstats FROM artadmin;
GRANT ALL ON TABLE externalstats TO artadmin;
GRANT ALL ON TABLE externalstats TO GROUP "artUsers";


--
-- TOC entry 1872 (class 0 OID 0)
-- Dependencies: 1267
-- Name: externalstats_classification; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE externalstats_classification FROM PUBLIC;
REVOKE ALL ON TABLE externalstats_classification FROM artadmin;
GRANT ALL ON TABLE externalstats_classification TO artadmin;
GRANT ALL ON TABLE externalstats_classification TO GROUP "artUsers";


--
-- TOC entry 1873 (class 0 OID 0)
-- Dependencies: 1268
-- Name: fivesecondloads; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE fivesecondloads FROM PUBLIC;
REVOKE ALL ON TABLE fivesecondloads FROM artadmin;
GRANT ALL ON TABLE fivesecondloads TO artadmin;
GRANT ALL ON TABLE fivesecondloads TO GROUP "artUsers";


--
-- TOC entry 1874 (class 0 OID 0)
-- Dependencies: 1269
-- Name: fivesecondloads_recordpk_seq; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE fivesecondloads_recordpk_seq FROM PUBLIC;
REVOKE ALL ON TABLE fivesecondloads_recordpk_seq FROM artadmin;
GRANT ALL ON TABLE fivesecondloads_recordpk_seq TO artadmin;
GRANT ALL ON TABLE fivesecondloads_recordpk_seq TO GROUP "artUsers";


--
-- TOC entry 1875 (class 0 OID 0)
-- Dependencies: 1270
-- Name: historical_external_statistics; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE historical_external_statistics FROM PUBLIC;
REVOKE ALL ON TABLE historical_external_statistics FROM artadmin;
GRANT ALL ON TABLE historical_external_statistics TO artadmin;
GRANT ALL ON TABLE historical_external_statistics TO GROUP "artUsers";


--
-- TOC entry 1876 (class 0 OID 0)
-- Dependencies: 1271
-- Name: hourlystatistics; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE hourlystatistics FROM PUBLIC;
REVOKE ALL ON TABLE hourlystatistics FROM artadmin;
GRANT ALL ON TABLE hourlystatistics TO artadmin;
GRANT ALL ON TABLE hourlystatistics TO GROUP "artUsers";


--
-- TOC entry 1877 (class 0 OID 0)
-- Dependencies: 1316
-- Name: loadtest_export_seq; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE loadtest_export_seq FROM PUBLIC;
REVOKE ALL ON TABLE loadtest_export_seq FROM artadmin;
GRANT ALL ON TABLE loadtest_export_seq TO artadmin;
GRANT ALL ON TABLE loadtest_export_seq TO GROUP "artUsers";


--
-- TOC entry 1878 (class 0 OID 0)
-- Dependencies: 1314
-- Name: loadtest_requests; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE loadtest_requests FROM PUBLIC;
REVOKE ALL ON TABLE loadtest_requests FROM artadmin;
GRANT ALL ON TABLE loadtest_requests TO artadmin;
GRANT ALL ON TABLE loadtest_requests TO GROUP "artUsers";


--
-- TOC entry 1879 (class 0 OID 0)
-- Dependencies: 1272
-- Name: loadtests; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE loadtests FROM PUBLIC;
REVOKE ALL ON TABLE loadtests FROM artadmin;
GRANT ALL ON TABLE loadtests TO artadmin;
GRANT ALL ON TABLE loadtests TO GROUP "artUsers";


--
-- TOC entry 1880 (class 0 OID 0)
-- Dependencies: 1273
-- Name: loadtests_loadtest_id_seq; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE loadtests_loadtest_id_seq FROM PUBLIC;
REVOKE ALL ON TABLE loadtests_loadtest_id_seq FROM artadmin;
GRANT ALL ON TABLE loadtests_loadtest_id_seq TO artadmin;
GRANT ALL ON TABLE loadtests_loadtest_id_seq TO GROUP "artUsers";


--
-- TOC entry 1881 (class 0 OID 0)
-- Dependencies: 1274
-- Name: loadtestscript; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE loadtestscript FROM PUBLIC;
REVOKE ALL ON TABLE loadtestscript FROM artadmin;
GRANT ALL ON TABLE loadtestscript TO artadmin;
GRANT ALL ON TABLE loadtestscript TO GROUP "artUsers";


--
-- TOC entry 1882 (class 0 OID 0)
-- Dependencies: 1275
-- Name: loadtestscript_script_id_seq; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE loadtestscript_script_id_seq FROM PUBLIC;
REVOKE ALL ON TABLE loadtestscript_script_id_seq FROM artadmin;
GRANT ALL ON TABLE loadtestscript_script_id_seq TO artadmin;
GRANT ALL ON TABLE loadtestscript_script_id_seq TO GROUP "artUsers";


--
-- TOC entry 1883 (class 0 OID 0)
-- Dependencies: 1276
-- Name: loadtestsummary; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE loadtestsummary FROM PUBLIC;
REVOKE ALL ON TABLE loadtestsummary FROM artadmin;
GRANT ALL ON TABLE loadtestsummary TO artadmin;
GRANT ALL ON TABLE loadtestsummary TO GROUP "artUsers";


--
-- TOC entry 1884 (class 0 OID 0)
-- Dependencies: 1277
-- Name: loadtesttransactionminuterecords; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE loadtesttransactionminuterecords FROM PUBLIC;
REVOKE ALL ON TABLE loadtesttransactionminuterecords FROM artadmin;
GRANT ALL ON TABLE loadtesttransactionminuterecords TO artadmin;
GRANT ALL ON TABLE loadtesttransactionminuterecords TO GROUP "artUsers";


--
-- TOC entry 1885 (class 0 OID 0)
-- Dependencies: 1278
-- Name: loadtesttransactions; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE loadtesttransactions FROM PUBLIC;
REVOKE ALL ON TABLE loadtesttransactions FROM artadmin;
GRANT ALL ON TABLE loadtesttransactions TO artadmin;
GRANT ALL ON TABLE loadtesttransactions TO GROUP "artUsers";


--
-- TOC entry 1886 (class 0 OID 0)
-- Dependencies: 1279
-- Name: loadtesttransactions_transac; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE loadtesttransactions_transac FROM PUBLIC;
REVOKE ALL ON TABLE loadtesttransactions_transac FROM artadmin;
GRANT ALL ON TABLE loadtesttransactions_transac TO artadmin;
GRANT ALL ON TABLE loadtesttransactions_transac TO GROUP "artUsers";


--
-- TOC entry 1887 (class 0 OID 0)
-- Dependencies: 1280
-- Name: loadtesttransactionsummary; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE loadtesttransactionsummary FROM PUBLIC;
REVOKE ALL ON TABLE loadtesttransactionsummary FROM artadmin;
GRANT ALL ON TABLE loadtesttransactionsummary TO artadmin;
GRANT ALL ON TABLE loadtesttransactionsummary TO GROUP "artUsers";


--
-- TOC entry 1888 (class 0 OID 0)
-- Dependencies: 1281
-- Name: machines; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE machines FROM PUBLIC;
REVOKE ALL ON TABLE machines FROM artadmin;
GRANT ALL ON TABLE machines TO artadmin;
GRANT ALL ON TABLE machines TO GROUP "artUsers";


--
-- TOC entry 1889 (class 0 OID 0)
-- Dependencies: 1282
-- Name: machines_machine_id_seq; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE machines_machine_id_seq FROM PUBLIC;
REVOKE ALL ON TABLE machines_machine_id_seq FROM artadmin;
GRANT ALL ON TABLE machines_machine_id_seq TO artadmin;
GRANT ALL ON TABLE machines_machine_id_seq TO GROUP "artUsers";


--
-- TOC entry 1890 (class 0 OID 0)
-- Dependencies: 1283
-- Name: minutestatistics; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE minutestatistics FROM PUBLIC;
REVOKE ALL ON TABLE minutestatistics FROM artadmin;
GRANT ALL ON TABLE minutestatistics TO artadmin;
GRANT ALL ON TABLE minutestatistics TO GROUP "artUsers";


--
-- TOC entry 1891 (class 0 OID 0)
-- Dependencies: 1284
-- Name: pages; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE pages FROM PUBLIC;
REVOKE ALL ON TABLE pages FROM artadmin;
GRANT ALL ON TABLE pages TO artadmin;
GRANT ALL ON TABLE pages TO GROUP "artUsers";


--
-- TOC entry 1892 (class 0 OID 0)
-- Dependencies: 1285
-- Name: pages_page_id_seq; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE pages_page_id_seq FROM PUBLIC;
REVOKE ALL ON TABLE pages_page_id_seq FROM artadmin;
GRANT ALL ON TABLE pages_page_id_seq TO artadmin;
GRANT ALL ON TABLE pages_page_id_seq TO GROUP "artUsers";


--
-- TOC entry 1893 (class 0 OID 0)
-- Dependencies: 1286
-- Name: playback_requests_test_number_seq; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE playback_requests_test_number_seq FROM PUBLIC;
REVOKE ALL ON TABLE playback_requests_test_number_seq FROM artadmin;
GRANT ALL ON TABLE playback_requests_test_number_seq TO artadmin;
GRANT ALL ON TABLE playback_requests_test_number_seq TO GROUP "artUsers";


--
-- TOC entry 1894 (class 0 OID 0)
-- Dependencies: 1287
-- Name: queryparameters; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE queryparameters FROM PUBLIC;
REVOKE ALL ON TABLE queryparameters FROM artadmin;
GRANT ALL ON TABLE queryparameters TO artadmin;
GRANT ALL ON TABLE queryparameters TO GROUP "artUsers";


--
-- TOC entry 1895 (class 0 OID 0)
-- Dependencies: 1288
-- Name: queryparameters_queryparamet; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE queryparameters_queryparamet FROM PUBLIC;
REVOKE ALL ON TABLE queryparameters_queryparamet FROM artadmin;
GRANT ALL ON TABLE queryparameters_queryparamet TO artadmin;
GRANT ALL ON TABLE queryparameters_queryparamet TO GROUP "artUsers";


--
-- TOC entry 1896 (class 0 OID 0)
-- Dependencies: 1289
-- Name: queryparamrecords; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE queryparamrecords FROM PUBLIC;
REVOKE ALL ON TABLE queryparamrecords FROM artadmin;
GRANT ALL ON TABLE queryparamrecords TO artadmin;
GRANT ALL ON TABLE queryparamrecords TO GROUP "artUsers";


--
-- TOC entry 1897 (class 0 OID 0)
-- Dependencies: 1290
-- Name: rload_accessrecords; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE rload_accessrecords FROM PUBLIC;
REVOKE ALL ON TABLE rload_accessrecords FROM artadmin;
GRANT ALL ON TABLE rload_accessrecords TO artadmin;
GRANT ALL ON TABLE rload_accessrecords TO GROUP "artUsers";


--
-- TOC entry 1898 (class 0 OID 0)
-- Dependencies: 1291
-- Name: rload_accessrecords_recordpk; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE rload_accessrecords_recordpk FROM PUBLIC;
REVOKE ALL ON TABLE rload_accessrecords_recordpk FROM artadmin;
GRANT ALL ON TABLE rload_accessrecords_recordpk TO artadmin;
GRANT ALL ON TABLE rload_accessrecords_recordpk TO GROUP "artUsers";


--
-- TOC entry 1899 (class 0 OID 0)
-- Dependencies: 1292
-- Name: rload_apps; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE rload_apps FROM PUBLIC;
REVOKE ALL ON TABLE rload_apps FROM artadmin;
GRANT ALL ON TABLE rload_apps TO artadmin;
GRANT ALL ON TABLE rload_apps TO GROUP "artUsers";


--
-- TOC entry 1900 (class 0 OID 0)
-- Dependencies: 1293
-- Name: rload_apps_app_id_seq; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE rload_apps_app_id_seq FROM PUBLIC;
REVOKE ALL ON TABLE rload_apps_app_id_seq FROM artadmin;
GRANT ALL ON TABLE rload_apps_app_id_seq TO artadmin;
GRANT ALL ON TABLE rload_apps_app_id_seq TO GROUP "artUsers";


--
-- TOC entry 1901 (class 0 OID 0)
-- Dependencies: 1294
-- Name: rload_contexts; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE rload_contexts FROM PUBLIC;
REVOKE ALL ON TABLE rload_contexts FROM artadmin;
GRANT ALL ON TABLE rload_contexts TO artadmin;
GRANT ALL ON TABLE rload_contexts TO GROUP "artUsers";


--
-- TOC entry 1902 (class 0 OID 0)
-- Dependencies: 1295
-- Name: rload_contexts_context_id_se; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE rload_contexts_context_id_se FROM PUBLIC;
REVOKE ALL ON TABLE rload_contexts_context_id_se FROM artadmin;
GRANT ALL ON TABLE rload_contexts_context_id_se TO artadmin;
GRANT ALL ON TABLE rload_contexts_context_id_se TO GROUP "artUsers";


--
-- TOC entry 1903 (class 0 OID 0)
-- Dependencies: 1296
-- Name: rload_machines; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE rload_machines FROM PUBLIC;
REVOKE ALL ON TABLE rload_machines FROM artadmin;
GRANT ALL ON TABLE rload_machines TO artadmin;
GRANT ALL ON TABLE rload_machines TO GROUP "artUsers";


--
-- TOC entry 1904 (class 0 OID 0)
-- Dependencies: 1297
-- Name: rload_machines_machine_id_se; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE rload_machines_machine_id_se FROM PUBLIC;
REVOKE ALL ON TABLE rload_machines_machine_id_se FROM artadmin;
GRANT ALL ON TABLE rload_machines_machine_id_se TO artadmin;
GRANT ALL ON TABLE rload_machines_machine_id_se TO GROUP "artUsers";


--
-- TOC entry 1905 (class 0 OID 0)
-- Dependencies: 1298
-- Name: rload_pages; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE rload_pages FROM PUBLIC;
REVOKE ALL ON TABLE rload_pages FROM artadmin;
GRANT ALL ON TABLE rload_pages TO artadmin;
GRANT ALL ON TABLE rload_pages TO GROUP "artUsers";


--
-- TOC entry 1906 (class 0 OID 0)
-- Dependencies: 1299
-- Name: rload_pages_page_id_seq; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE rload_pages_page_id_seq FROM PUBLIC;
REVOKE ALL ON TABLE rload_pages_page_id_seq FROM artadmin;
GRANT ALL ON TABLE rload_pages_page_id_seq TO artadmin;
GRANT ALL ON TABLE rload_pages_page_id_seq TO GROUP "artUsers";


--
-- TOC entry 1907 (class 0 OID 0)
-- Dependencies: 1300
-- Name: rload_sessions; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE rload_sessions FROM PUBLIC;
REVOKE ALL ON TABLE rload_sessions FROM artadmin;
GRANT ALL ON TABLE rload_sessions TO artadmin;
GRANT ALL ON TABLE rload_sessions TO GROUP "artUsers";


--
-- TOC entry 1908 (class 0 OID 0)
-- Dependencies: 1301
-- Name: rload_sessions_session_id_se; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE rload_sessions_session_id_se FROM PUBLIC;
REVOKE ALL ON TABLE rload_sessions_session_id_se FROM artadmin;
GRANT ALL ON TABLE rload_sessions_session_id_se TO artadmin;
GRANT ALL ON TABLE rload_sessions_session_id_se TO GROUP "artUsers";


--
-- TOC entry 1909 (class 0 OID 0)
-- Dependencies: 1302
-- Name: rload_users; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE rload_users FROM PUBLIC;
REVOKE ALL ON TABLE rload_users FROM artadmin;
GRANT ALL ON TABLE rload_users TO artadmin;
GRANT ALL ON TABLE rload_users TO GROUP "artUsers";


--
-- TOC entry 1910 (class 0 OID 0)
-- Dependencies: 1303
-- Name: rload_users_user_id_seq; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE rload_users_user_id_seq FROM PUBLIC;
REVOKE ALL ON TABLE rload_users_user_id_seq FROM artadmin;
GRANT ALL ON TABLE rload_users_user_id_seq TO artadmin;
GRANT ALL ON TABLE rload_users_user_id_seq TO GROUP "artUsers";


--
-- TOC entry 1911 (class 0 OID 0)
-- Dependencies: 1304
-- Name: sequencetable; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE sequencetable FROM PUBLIC;
REVOKE ALL ON TABLE sequencetable FROM artadmin;
GRANT ALL ON TABLE sequencetable TO artadmin;
GRANT ALL ON TABLE sequencetable TO GROUP "artUsers";


--
-- TOC entry 1912 (class 0 OID 0)
-- Dependencies: 1305
-- Name: sessions; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE sessions FROM PUBLIC;
REVOKE ALL ON TABLE sessions FROM artadmin;
GRANT ALL ON TABLE sessions TO artadmin;
GRANT ALL ON TABLE sessions TO GROUP "artUsers";


--
-- TOC entry 1913 (class 0 OID 0)
-- Dependencies: 1306
-- Name: sessions_session_id_seq; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE sessions_session_id_seq FROM PUBLIC;
REVOKE ALL ON TABLE sessions_session_id_seq FROM artadmin;
GRANT ALL ON TABLE sessions_session_id_seq TO artadmin;
GRANT ALL ON TABLE sessions_session_id_seq TO GROUP "artUsers";


--
-- TOC entry 1914 (class 0 OID 0)
-- Dependencies: 1307
-- Name: stacktracebeancontainers; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE stacktracebeancontainers FROM PUBLIC;
REVOKE ALL ON TABLE stacktracebeancontainers FROM artadmin;
GRANT ALL ON TABLE stacktracebeancontainers TO artadmin;
GRANT ALL ON TABLE stacktracebeancontainers TO GROUP "artUsers";


--
-- TOC entry 1915 (class 0 OID 0)
-- Dependencies: 1308
-- Name: stacktracedetails; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE stacktracedetails FROM PUBLIC;
REVOKE ALL ON TABLE stacktracedetails FROM artadmin;
GRANT ALL ON TABLE stacktracedetails TO artadmin;
GRANT ALL ON TABLE stacktracedetails TO GROUP "artUsers";


--
-- TOC entry 1916 (class 0 OID 0)
-- Dependencies: 1309
-- Name: stacktracerows; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE stacktracerows FROM PUBLIC;
REVOKE ALL ON TABLE stacktracerows FROM artadmin;
GRANT ALL ON TABLE stacktracerows TO artadmin;
GRANT ALL ON TABLE stacktracerows TO GROUP "artUsers";


--
-- TOC entry 1917 (class 0 OID 0)
-- Dependencies: 1310
-- Name: stacktracerows_row_id_seq; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE stacktracerows_row_id_seq FROM PUBLIC;
REVOKE ALL ON TABLE stacktracerows_row_id_seq FROM artadmin;
GRANT ALL ON TABLE stacktracerows_row_id_seq TO artadmin;
GRANT ALL ON TABLE stacktracerows_row_id_seq TO GROUP "artUsers";


--
-- TOC entry 1918 (class 0 OID 0)
-- Dependencies: 1311
-- Name: stacktraces; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE stacktraces FROM PUBLIC;
REVOKE ALL ON TABLE stacktraces FROM artadmin;
GRANT ALL ON TABLE stacktraces TO artadmin;
GRANT ALL ON TABLE stacktraces TO GROUP "artUsers";


--
-- TOC entry 1919 (class 0 OID 0)
-- Dependencies: 1312
-- Name: users; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE users FROM PUBLIC;
REVOKE ALL ON TABLE users FROM artadmin;
GRANT ALL ON TABLE users TO artadmin;
GRANT ALL ON TABLE users TO GROUP "artUsers";


--
-- TOC entry 1920 (class 0 OID 0)
-- Dependencies: 1313
-- Name: users_user_id_seq; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE users_user_id_seq FROM PUBLIC;
REVOKE ALL ON TABLE users_user_id_seq FROM artadmin;
GRANT ALL ON TABLE users_user_id_seq TO artadmin;
GRANT ALL ON TABLE users_user_id_seq TO GROUP "artUsers";



--  
-- 
-- HtmlPlayback pages....
-- HtmlPageResponse_ID | int(10) unsigned |      | PRI | NULL    | auto_increment |
-- insertTime          | timestamp        | YES  |     | NULL    |                |
-- Branch_ID           | int(11)          | YES  |     | NULL    |                |
-- Machine_ID          | int(11)          | YES  |     | NULL    |                |
-- Context_ID          | int(11)          | YES  |     | NULL    |                |
-- Page_ID             | int(11)          | YES  |     | NULL    |                |
-- Time                | timestamp        | YES  |     | NULL    |                |
-- sessionTXT          | varchar(100)     | YES  | MUL | NULL    |                |
-- requestToken        | int(11)          | YES  |     | NULL    |                |
-- requestTokenCount   | int(11)          | YES  |     | NULL    |                |
-- encodedPage         | mediumtext       | YES  |     | NULL    |                |

CREATE TABLE HtmlPageResponse (
    HtmlPageResponse_ID integer DEFAULT nextval('htmlpageresponse_htmlpageresponse_id_seq'::text) NOT NULL,
    insertTime timestamp without time zone DEFAULT now() NOT NULL,
    Branch_ID integer,
    Machine_ID integer,
    Context_ID integer ,
    Page_ID integer ,
    Time timestamp without time zone NOT NULL,
    sessionTXT character varying(50),
    requestToken integer,
    requestTokenCount integer ,
    encodedPage text 
);


ALTER TABLE public.HtmlPageResponse OWNER TO artadmin;

--
-- TOC entry 1306 (class 1259 OID 17476)
-- Dependencies: 5
-- Name: sessions_session_id_seq; Type: SEQUENCE; Schema: public; Owner: artadmin
--

CREATE SEQUENCE htmlpageresponse_htmlpageresponse_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.htmlpageresponse_htmlpageresponse_id_seq OWNER TO artadmin;


