--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = public, pg_catalog;

--
-- Name: export_loadtest_requests(character varying, character varying, timestamp with time zone, timestamp with time zone, character varying); Type: FUNCTION; Schema: public; Owner: artadmin
--

CREATE FUNCTION export_loadtest_requests(character varying, character varying, timestamp with time zone, timestamp with time zone, character varying) RETURNS bigint
    LANGUAGE plpgsql
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
$_$;


ALTER FUNCTION public.export_loadtest_requests(character varying, character varying, timestamp with time zone, timestamp with time zone, character varying) OWNER TO artadmin;

--
-- Name: getqueryparams(integer); Type: FUNCTION; Schema: public; Owner: artadmin
--

CREATE FUNCTION getqueryparams("RecordPK" integer) RETURNS character varying
    LANGUAGE plpgsql
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
end;$_$;


ALTER FUNCTION public.getqueryparams("RecordPK" integer) OWNER TO artadmin;

--
-- Name: gettesturlstrings(character varying, character varying, timestamp without time zone, timestamp without time zone, character varying, character varying); Type: FUNCTION; Schema: public; Owner: pgsql
--

CREATE FUNCTION gettesturlstrings("fromContext" character varying, "fromMachines" character varying, "fromStartTime" timestamp without time zone, "fromStopTime" timestamp without time zone, "toHost" character varying, "toContext" character varying) RETURNS SETOF text
    LANGUAGE plpgsql
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
$_$;


ALTER FUNCTION public.gettesturlstrings("fromContext" character varying, "fromMachines" character varying, "fromStartTime" timestamp without time zone, "fromStopTime" timestamp without time zone, "toHost" character varying, "toContext" character varying) OWNER TO pgsql;

--
-- Name: html_error_detect(); Type: FUNCTION; Schema: public; Owner: artadmin
--

CREATE FUNCTION html_error_detect() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
	decodedHTML text:='';
    BEGIN
        --
        -- Create a row in emp_audit to reflect the operation performed on emp,
        -- make use of the special variable TG_OP to work out the operation.
        IF (TG_OP = 'INSERT') THEN
	    --decodedHTML := decode(NEW.encodedpage,'base64');
            if ( regexp_matches(NEW.encodedpage,'We''re sorry|Sorry unexpected') is not null = TRUE) THEN
		update sessions set error_experience=TRUE where sessions.sessiontxt = NEW.sessiontxt;
		--update htmlpageresponse set error_experience=TRUE where htmlpageresponse.sessiontxt = NEW.sessiontxt;
		NEW.error_experience = TRUE ;
            END IF;
            RETURN NEW;
        END IF;
        RETURN NEW; -- result is ignored since this is an AFTER trigger
    END;
$$;


ALTER FUNCTION public.html_error_detect() OWNER TO artadmin;

--
-- Name: plpgsql_call_handler(); Type: FUNCTION; Schema: public; Owner: pgsql
--

CREATE FUNCTION plpgsql_call_handler() RETURNS language_handler
    LANGUAGE c
    AS '$libdir/plpgsql', 'plpgsql_call_handler';


ALTER FUNCTION public.plpgsql_call_handler() OWNER TO pgsql;

--
-- Name: plpgsql_validator(oid); Type: FUNCTION; Schema: public; Owner: pgsql
--

CREATE FUNCTION plpgsql_validator(oid) RETURNS void
    LANGUAGE c
    AS '$libdir/plpgsql', 'plpgsql_validator';


ALTER FUNCTION public.plpgsql_validator(oid) OWNER TO pgsql;

--
-- Name: populate_playback_requests(character varying, character varying, timestamp without time zone, timestamp without time zone, character varying); Type: FUNCTION; Schema: public; Owner: pgsql
--

CREATE FUNCTION populate_playback_requests(character varying, character varying, timestamp without time zone, timestamp without time zone, character varying) RETURNS bigint
    LANGUAGE plpgsql
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
$_$;


ALTER FUNCTION public.populate_playback_requests(character varying, character varying, timestamp without time zone, timestamp without time zone, character varying) OWNER TO pgsql;

--
-- Name: setlastmodtime(); Type: FUNCTION; Schema: public; Owner: artadmin
--

CREATE FUNCTION setlastmodtime() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
	BEGIN
	   NEW.lastmodtime = now();
	   RETURN NEW;
	END
	$$;


ALTER FUNCTION public.setlastmodtime() OWNER TO artadmin;

--
-- Name: testparse(character varying); Type: FUNCTION; Schema: public; Owner: artadmin
--

CREATE FUNCTION testparse(machines character varying) RETURNS character varying
    LANGUAGE plpgsql
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

$_$;


ALTER FUNCTION public.testparse(machines character varying) OWNER TO artadmin;

--
-- Name: accessrecords_recordpk_seq; Type: SEQUENCE; Schema: public; Owner: artadmin
--

CREATE SEQUENCE accessrecords_recordpk_seq
    START WITH 64022282
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
    CYCLE;


ALTER TABLE public.accessrecords_recordpk_seq OWNER TO artadmin;

SET default_tablespace = '';

SET default_with_oids = true;

--
-- Name: accessrecords; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE accessrecords (
    recordpk bigint DEFAULT nextval('accessrecords_recordpk_seq'::regclass) NOT NULL,
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
    userservicetime integer,
    instance_id integer
);


ALTER TABLE public.accessrecords OWNER TO artadmin;

--
-- Name: accumulator; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE accumulator (
    accumulatorstat_id integer DEFAULT nextval(('accumulator_accumulatorstat_'::text)::regclass) NOT NULL,
    accumulatorname character varying(40),
    accumulatordescription text,
    accumulatortype character varying(20),
    dataunits character varying(40),
    lastmodtime timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.accumulator OWNER TO artadmin;

--
-- Name: accumulator_accumulatorstat_; Type: SEQUENCE; Schema: public; Owner: artadmin
--

CREATE SEQUENCE accumulator_accumulatorstat_
    START WITH 4000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.accumulator_accumulatorstat_ OWNER TO artadmin;

--
-- Name: accumulatorevent; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE accumulatorevent (
    accumulatorevent_id integer DEFAULT nextval(('accumulatorevent_accumulator'::text)::regclass) NOT NULL,
    accumulatorstat_id integer,
    inserttime timestamp without time zone DEFAULT now() NOT NULL,
    machine_id integer,
    context_id integer,
    branch_id integer,
    app_id integer,
    "time" timestamp without time zone DEFAULT '1970-01-01 01:00:00'::timestamp without time zone NOT NULL,
    intvalue integer,
    doublevalue double precision,
    stringvalue text,
    datatype character varying(60),
    instance_id integer,
    CONSTRAINT accumulatorevent_accumulatorstat_id_check CHECK ((accumulatorstat_id >= 0))
);


ALTER TABLE public.accumulatorevent OWNER TO artadmin;

--
-- Name: accumulatorevent_accumulator; Type: SEQUENCE; Schema: public; Owner: artadmin
--

CREATE SEQUENCE accumulatorevent_accumulator
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.accumulatorevent_accumulator OWNER TO artadmin;

--
-- Name: accumulatorstats; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE accumulatorstats (
    accumulatorstat_id integer NOT NULL,
    context_id integer NOT NULL,
    lastmoddate timestamp without time zone DEFAULT '1970-01-01 01:00:00'::timestamp without time zone NOT NULL,
    "time" timestamp without time zone DEFAULT '1970-01-01 01:00:00'::timestamp without time zone NOT NULL,
    value integer,
    count integer
);


ALTER TABLE public.accumulatorstats OWNER TO artadmin;

--
-- Name: apps; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE apps (
    app_id integer DEFAULT nextval(('apps_app_id_seq'::text)::regclass) NOT NULL,
    appname character varying(50),
    lastmodtime timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.apps OWNER TO artadmin;

--
-- Name: apps_app_id_seq; Type: SEQUENCE; Schema: public; Owner: artadmin
--

CREATE SEQUENCE apps_app_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.apps_app_id_seq OWNER TO artadmin;

--
-- Name: bloat; Type: VIEW; Schema: public; Owner: artadmin
--

CREATE VIEW bloat AS
    SELECT sml.schemaname, sml.tablename, (sml.reltuples)::bigint AS reltuples, (sml.relpages)::bigint AS relpages, sml.otta, round(CASE WHEN (sml.otta = (0)::double precision) THEN 0.0 ELSE ((sml.relpages)::numeric / (sml.otta)::numeric) END, 1) AS tbloat, (((sml.relpages)::bigint)::double precision - sml.otta) AS wastedpages, (sml.bs * ((((sml.relpages)::double precision - sml.otta))::bigint)::numeric) AS wastedbytes, pg_size_pretty((((sml.bs)::double precision * ((sml.relpages)::double precision - sml.otta)))::bigint) AS wastedsize, sml.iname, (sml.ituples)::bigint AS ituples, (sml.ipages)::bigint AS ipages, sml.iotta, round(CASE WHEN ((sml.iotta = (0)::double precision) OR (sml.ipages = 0)) THEN 0.0 ELSE ((sml.ipages)::numeric / (sml.iotta)::numeric) END, 1) AS ibloat, CASE WHEN ((sml.ipages)::double precision < sml.iotta) THEN (0)::double precision ELSE (((sml.ipages)::bigint)::double precision - sml.iotta) END AS wastedipages, CASE WHEN ((sml.ipages)::double precision < sml.iotta) THEN (0)::double precision ELSE ((sml.bs)::double precision * ((sml.ipages)::double precision - sml.iotta)) END AS wastedibytes, CASE WHEN ((sml.ipages)::double precision < sml.iotta) THEN pg_size_pretty((0)::bigint) ELSE pg_size_pretty((((sml.bs)::double precision * ((sml.ipages)::double precision - sml.iotta)))::bigint) END AS wastedisize FROM (SELECT rs.schemaname, rs.tablename, cc.reltuples, cc.relpages, rs.bs, ceil(((cc.reltuples * (((((rs.datahdr + (rs.ma)::numeric) - CASE WHEN ((rs.datahdr % (rs.ma)::numeric) = (0)::numeric) THEN (rs.ma)::numeric ELSE (rs.datahdr % (rs.ma)::numeric) END))::double precision + rs.nullhdr2) + (4)::double precision)) / ((rs.bs)::double precision - (20)::double precision))) AS otta, COALESCE(c2.relname, '?'::name) AS iname, COALESCE(c2.reltuples, (0)::real) AS ituples, COALESCE(c2.relpages, 0) AS ipages, COALESCE(ceil(((c2.reltuples * ((rs.datahdr - (12)::numeric))::double precision) / ((rs.bs)::double precision - (20)::double precision))), (0)::double precision) AS iotta FROM (((((SELECT foo.ma, foo.bs, foo.schemaname, foo.tablename, ((foo.datawidth + (((foo.hdr + foo.ma) - CASE WHEN ((foo.hdr % foo.ma) = 0) THEN foo.ma ELSE (foo.hdr % foo.ma) END))::double precision))::numeric AS datahdr, (foo.maxfracsum * (((foo.nullhdr + foo.ma) - CASE WHEN ((foo.nullhdr % (foo.ma)::bigint) = 0) THEN (foo.ma)::bigint ELSE (foo.nullhdr % (foo.ma)::bigint) END))::double precision) AS nullhdr2 FROM (SELECT s.schemaname, s.tablename, constants.hdr, constants.ma, constants.bs, sum((((1)::double precision - s.null_frac) * (s.avg_width)::double precision)) AS datawidth, max(s.null_frac) AS maxfracsum, (constants.hdr + (SELECT (1 + (count(*) / 8)) FROM pg_stats s2 WHERE (((s2.null_frac <> (0)::double precision) AND (s2.schemaname = s.schemaname)) AND (s2.tablename = s.tablename)))) AS nullhdr FROM pg_stats s, (SELECT (SELECT (current_setting('block_size'::text))::numeric AS current_setting) AS bs, CASE WHEN ("substring"(foo.v, 12, 3) = ANY (ARRAY['8.0'::text, '8.1'::text, '8.2'::text])) THEN 27 ELSE 23 END AS hdr, CASE WHEN (foo.v ~ 'mingw32'::text) THEN 8 ELSE 4 END AS ma FROM (SELECT version() AS v) foo) constants GROUP BY s.schemaname, s.tablename, constants.hdr, constants.ma, constants.bs) foo) rs JOIN pg_class cc ON ((cc.relname = rs.tablename))) JOIN pg_namespace nn ON (((cc.relnamespace = nn.oid) AND (nn.nspname = rs.schemaname)))) LEFT JOIN pg_index i ON ((i.indrelid = cc.oid))) LEFT JOIN pg_class c2 ON ((c2.oid = i.indexrelid)))) sml WHERE ((((sml.relpages)::double precision - sml.otta) > (0)::double precision) OR (((sml.ipages)::double precision - sml.iotta) > (10)::double precision)) ORDER BY (sml.bs * ((((sml.relpages)::double precision - sml.otta))::bigint)::numeric) DESC, CASE WHEN ((sml.ipages)::double precision < sml.iotta) THEN (0)::double precision ELSE ((sml.bs)::double precision * ((sml.ipages)::double precision - sml.iotta)) END DESC;


ALTER TABLE public.bloat OWNER TO artadmin;

--
-- Name: branches; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE branches (
    branch_tag_id integer DEFAULT nextval(('branches_branch_tag_id_seq'::text)::regclass) NOT NULL,
    branchname character varying(50),
    lastmodtime timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.branches OWNER TO artadmin;

--
-- Name: branches_branch_tag_id_seq; Type: SEQUENCE; Schema: public; Owner: artadmin
--

CREATE SEQUENCE branches_branch_tag_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.branches_branch_tag_id_seq OWNER TO artadmin;

--
-- Name: browsers; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE browsers (
    browser_id integer DEFAULT nextval(('browsers_browser_id_seq'::text)::regclass) NOT NULL,
    patternmatchstring character varying(132),
    description character varying(50),
    inserttime timestamp without time zone DEFAULT now() NOT NULL,
    recordtype character varying(2)
);


ALTER TABLE public.browsers OWNER TO artadmin;

--
-- Name: browsers_browser_id_seq; Type: SEQUENCE; Schema: public; Owner: artadmin
--

CREATE SEQUENCE browsers_browser_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.browsers_browser_id_seq OWNER TO artadmin;

--
-- Name: browserstats; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE browserstats (
    day date,
    browser_id integer,
    count integer,
    state character(1) DEFAULT 'O'::bpchar,
    inserttime timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.browserstats OWNER TO artadmin;

--
-- Name: contexts; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE contexts (
    context_id integer DEFAULT nextval(('contexts_context_id_seq'::text)::regclass) NOT NULL,
    contextname character varying(50),
    lastmodtime timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.contexts OWNER TO artadmin;

--
-- Name: contexts_context_id_seq; Type: SEQUENCE; Schema: public; Owner: artadmin
--

CREATE SEQUENCE contexts_context_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.contexts_context_id_seq OWNER TO artadmin;

--
-- Name: dailycontextstats; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE dailycontextstats (
    day date,
    context_id integer,
    count integer,
    state character(1) DEFAULT 'O'::bpchar,
    lastmodtime timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.dailycontextstats OWNER TO artadmin;

--
-- Name: dailypageloadtimes; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE dailypageloadtimes (
    dailyloadtime_id integer DEFAULT nextval(('dailypageloadtimes_dailyload'::text)::regclass) NOT NULL,
    day date,
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
    lastmodtime timestamp without time zone DEFAULT now() NOT NULL,
    instance_id integer
);


ALTER TABLE public.dailypageloadtimes OWNER TO artadmin;

--
-- Name: dailypageloadtimes_dailyload; Type: SEQUENCE; Schema: public; Owner: artadmin
--

CREATE SEQUENCE dailypageloadtimes_dailyload
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.dailypageloadtimes_dailyload OWNER TO artadmin;

--
-- Name: dailysummary; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE dailysummary (
    day date DEFAULT '0001-01-01'::date NOT NULL,
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

SET default_with_oids = false;

--
-- Name: deployment_map; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE deployment_map (
    key character(90) NOT NULL,
    value character(90)
);


ALTER TABLE public.deployment_map OWNER TO artadmin;

--
-- Name: deploymentjobs; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE deploymentjobs (
    createtime timestamp without time zone DEFAULT now() NOT NULL,
    jobdetail bytea,
    environment character varying(30),
    job_id character varying(85) NOT NULL
);


ALTER TABLE public.deploymentjobs OWNER TO artadmin;

SET default_with_oids = true;

--
-- Name: deployments; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE deployments (
    product character varying(20),
    machine character varying(35),
    server_group character varying(10),
    properties_file character varying(20),
    release_tag character varying(100),
    application_context character varying(20),
    deploy_time character varying(15) DEFAULT ''::character varying NOT NULL,
    iscurrent character(1),
    somecomment character varying(200),
    novelluserid character varying(120),
    changecontrollnumber character varying(20),
    deploy_timestamp timestamp without time zone DEFAULT now()
);


ALTER TABLE public.deployments OWNER TO artadmin;

SET default_with_oids = false;

--
-- Name: deployments_backup; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE deployments_backup (
    product character varying(20),
    machine character varying(35),
    server_group character varying(10),
    properties_file character varying(20),
    release_tag character varying(100),
    application_context character varying(20),
    deploy_time character varying(15),
    iscurrent character(1),
    somecomment character varying(200),
    novelluserid character varying(120),
    changecontrollnumber character varying(20),
    deploy_timestamp timestamp without time zone
);


ALTER TABLE public.deployments_backup OWNER TO artadmin;

--
-- Name: deploymenttagnames; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE deploymenttagnames (
    createtime timestamp without time zone DEFAULT now() NOT NULL,
    tagname character varying(50) NOT NULL,
    environment character varying(30)
);


ALTER TABLE public.deploymenttagnames OWNER TO artadmin;

SET default_with_oids = true;

--
-- Name: externalaccessrecords; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE externalaccessrecords (
    recordpk numeric(19,0) DEFAULT nextval(('externalaccessrecords_record'::text)::regclass) NOT NULL,
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
    loadtime integer,
    instance_id integer
);


ALTER TABLE public.externalaccessrecords OWNER TO artadmin;

--
-- Name: externalaccessrecords_record; Type: SEQUENCE; Schema: public; Owner: artadmin
--

CREATE SEQUENCE externalaccessrecords_record
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
    CYCLE;


ALTER TABLE public.externalaccessrecords_record OWNER TO artadmin;

--
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
    state character(1) DEFAULT 'O'::bpchar,
    instance_id integer
)
WITH (autovacuum_enabled=true, autovacuum_vacuum_threshold=10000);


ALTER TABLE public.externalminutestatistics OWNER TO artadmin;

--
-- Name: externalstats; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE externalstats (
    classification_id integer DEFAULT nextval(('externalstats_classification'::text)::regclass) NOT NULL,
    destination character varying(75),
    description character varying(255),
    inserttime timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.externalstats OWNER TO artadmin;

--
-- Name: externalstats_classification; Type: SEQUENCE; Schema: public; Owner: artadmin
--

CREATE SEQUENCE externalstats_classification
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.externalstats_classification OWNER TO artadmin;

--
-- Name: fivesecondloads; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE fivesecondloads (
    recordpk integer DEFAULT nextval(('fivesecondloads_recordpk_seq'::text)::regclass) NOT NULL,
    lastmodtime timestamp without time zone DEFAULT now() NOT NULL,
    page_id integer,
    user_id integer,
    session_id integer,
    machine_id integer,
    context_id integer,
    app_id integer,
    "time" timestamp without time zone DEFAULT '1970-01-01 01:00:00'::timestamp without time zone NOT NULL,
    loadtime integer,
    instance_id integer
);


ALTER TABLE public.fivesecondloads OWNER TO artadmin;

--
-- Name: fivesecondloads_recordpk_seq; Type: SEQUENCE; Schema: public; Owner: artadmin
--

CREATE SEQUENCE fivesecondloads_recordpk_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.fivesecondloads_recordpk_seq OWNER TO artadmin;

--
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
    "time" timestamp without time zone DEFAULT '1970-01-01 01:00:00'::timestamp without time zone NOT NULL,
    instance_id integer
);


ALTER TABLE public.hourlystatistics OWNER TO artadmin;

SET default_with_oids = false;

--
-- Name: htmlpageresponse; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE htmlpageresponse (
    htmlpageresponse_id bigint DEFAULT nextval(('htmlpageresponse_id_seq'::text)::regclass) NOT NULL,
    inserttime timestamp without time zone,
    branch_id integer,
    machine_id integer,
    context_id integer,
    page_id integer,
    "time" timestamp without time zone,
    sessiontxt character(100),
    requesttoken integer,
    requesttokencount integer,
    encodedpage text,
    instance_id integer,
    experience character(40)
);


ALTER TABLE public.htmlpageresponse OWNER TO artadmin;

--
-- Name: htmlpageresponse_id_seq; Type: SEQUENCE; Schema: public; Owner: artadmin
--

CREATE SEQUENCE htmlpageresponse_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.htmlpageresponse_id_seq OWNER TO artadmin;

SET default_with_oids = true;

--
-- Name: instances; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE instances (
    instance_id integer DEFAULT nextval(('instances_instance_id_seq'::text)::regclass) NOT NULL,
    instancename character varying(50),
    lastmodtime timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.instances OWNER TO artadmin;

--
-- Name: instances_instance_id_seq; Type: SEQUENCE; Schema: public; Owner: artadmin
--

CREATE SEQUENCE instances_instance_id_seq
    START WITH 43
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.instances_instance_id_seq OWNER TO artadmin;

SET default_with_oids = false;

--
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
-- Name: loadtest_export_seq; Type: SEQUENCE; Schema: public; Owner: artadmin
--

CREATE SEQUENCE loadtest_export_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.loadtest_export_seq OWNER TO artadmin;

--
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
-- Name: loadtests; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE loadtests (
    loadtest_id integer DEFAULT nextval(('loadtests_loadtest_id_seq'::text)::regclass) NOT NULL,
    testname character varying(255),
    context_id integer,
    branch_id integer,
    lastmodtime timestamp without time zone DEFAULT now() NOT NULL,
    starttime timestamp without time zone DEFAULT '1970-01-01 01:00:00'::timestamp without time zone NOT NULL,
    endtime timestamp without time zone DEFAULT '1970-01-01 01:00:00'::timestamp without time zone NOT NULL,
    status character varying(10) DEFAULT 'UNKNOWN'::character varying,
    loadtest_export_id bigint,
    baseoffset bigint,
    baseplaybackoffsetdate timestamp without time zone DEFAULT '1970-01-01 01:00:00'::timestamp without time zone NOT NULL
);


ALTER TABLE public.loadtests OWNER TO artadmin;

--
-- Name: loadtests_loadtest_id_seq; Type: SEQUENCE; Schema: public; Owner: artadmin
--

CREATE SEQUENCE loadtests_loadtest_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.loadtests_loadtest_id_seq OWNER TO artadmin;

--
-- Name: loadtestscript; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE loadtestscript (
    script_id integer DEFAULT nextval(('loadtestscript_script_id_seq'::text)::regclass) NOT NULL,
    scriptname character varying(20),
    lastmodtime timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.loadtestscript OWNER TO artadmin;

--
-- Name: loadtestscript_script_id_seq; Type: SEQUENCE; Schema: public; Owner: artadmin
--

CREATE SEQUENCE loadtestscript_script_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.loadtestscript_script_id_seq OWNER TO artadmin;

--
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
-- Name: loadtesttransactions; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE loadtesttransactions (
    transaction_id integer DEFAULT nextval(('loadtesttransactions_transac'::text)::regclass) NOT NULL,
    script_id integer DEFAULT 0 NOT NULL,
    transactionname character varying(20),
    transactiondesc character varying(50),
    lastmodtime timestamp without time zone DEFAULT now() NOT NULL,
    CONSTRAINT loadtesttransactions_script_id_check CHECK ((script_id >= 0))
);


ALTER TABLE public.loadtesttransactions OWNER TO artadmin;

--
-- Name: loadtesttransactions_transac; Type: SEQUENCE; Schema: public; Owner: artadmin
--

CREATE SEQUENCE loadtesttransactions_transac
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.loadtesttransactions_transac OWNER TO artadmin;

--
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
-- Name: machines; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE machines (
    machine_id integer DEFAULT nextval(('machines_machine_id_seq'::text)::regclass) NOT NULL,
    machinename character varying(50),
    shortname character varying(10),
    machinetype character(1),
    lastmodtime timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.machines OWNER TO artadmin;

--
-- Name: machines_machine_id_seq; Type: SEQUENCE; Schema: public; Owner: artadmin
--

CREATE SEQUENCE machines_machine_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.machines_machine_id_seq OWNER TO artadmin;

--
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
    state character(1) DEFAULT 'O'::bpchar,
    instance_id integer
);


ALTER TABLE public.minutestatistics OWNER TO artadmin;

--
-- Name: pages; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE pages (
    page_id integer DEFAULT nextval(('pages_page_id_seq'::text)::regclass) NOT NULL,
    pagename character varying(250),
    iserrorpage character(1) DEFAULT 'N'::bpchar,
    lastmodtime timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.pages OWNER TO artadmin;

--
-- Name: pages_page_id_seq; Type: SEQUENCE; Schema: public; Owner: artadmin
--

CREATE SEQUENCE pages_page_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.pages_page_id_seq OWNER TO artadmin;

--
-- Name: playback_requests_test_number_seq; Type: SEQUENCE; Schema: public; Owner: artadmin
--

CREATE SEQUENCE playback_requests_test_number_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.playback_requests_test_number_seq OWNER TO artadmin;

--
-- Name: queryparameters; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE queryparameters (
    queryparameter_id numeric(19,0) DEFAULT nextval(('queryparameters_queryparamet'::text)::regclass) NOT NULL,
    queryparams text,
    lastmodtime timestamp without time zone DEFAULT now() NOT NULL,
    value_hash character(32)
);


ALTER TABLE public.queryparameters OWNER TO artadmin;

--
-- Name: queryparameters_queryparamet; Type: SEQUENCE; Schema: public; Owner: artadmin
--

CREATE SEQUENCE queryparameters_queryparamet
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.queryparameters_queryparamet OWNER TO artadmin;

SET default_with_oids = false;

--
-- Name: queryparamrecord_indexlist; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE queryparamrecord_indexlist (
    startrecordpk bigint,
    endrecordpk bigint,
    createstatement text,
    indexname character varying(75),
    createtime timestamp without time zone,
    deletetime timestamp without time zone
);


ALTER TABLE public.queryparamrecord_indexlist OWNER TO artadmin;

SET default_with_oids = true;

--
-- Name: queryparamrecords; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE queryparamrecords (
    queryparameter_id numeric(19,0),
    recordpk numeric(19,0)
);


ALTER TABLE public.queryparamrecords OWNER TO artadmin;

--
-- Name: rload_accessrecords; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE rload_accessrecords (
    recordpk integer DEFAULT nextval(('rload_accessrecords_recordpk'::text)::regclass) NOT NULL,
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
-- Name: rload_accessrecords_recordpk; Type: SEQUENCE; Schema: public; Owner: artadmin
--

CREATE SEQUENCE rload_accessrecords_recordpk
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.rload_accessrecords_recordpk OWNER TO artadmin;

--
-- Name: rload_apps; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE rload_apps (
    app_id integer DEFAULT nextval(('rload_apps_app_id_seq'::text)::regclass) NOT NULL,
    appname character varying(50),
    inserttime timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.rload_apps OWNER TO artadmin;

--
-- Name: rload_apps_app_id_seq; Type: SEQUENCE; Schema: public; Owner: artadmin
--

CREATE SEQUENCE rload_apps_app_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.rload_apps_app_id_seq OWNER TO artadmin;

--
-- Name: rload_contexts; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE rload_contexts (
    context_id integer DEFAULT nextval(('rload_contexts_context_id_se'::text)::regclass) NOT NULL,
    contextname character varying(50),
    inserttime timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.rload_contexts OWNER TO artadmin;

--
-- Name: rload_contexts_context_id_se; Type: SEQUENCE; Schema: public; Owner: artadmin
--

CREATE SEQUENCE rload_contexts_context_id_se
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.rload_contexts_context_id_se OWNER TO artadmin;

--
-- Name: rload_machines; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE rload_machines (
    machine_id integer DEFAULT nextval(('rload_machines_machine_id_se'::text)::regclass) NOT NULL,
    machinename character varying(50),
    shortname character varying(10),
    machinetype character(1),
    inserttime timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.rload_machines OWNER TO artadmin;

--
-- Name: rload_machines_machine_id_se; Type: SEQUENCE; Schema: public; Owner: artadmin
--

CREATE SEQUENCE rload_machines_machine_id_se
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.rload_machines_machine_id_se OWNER TO artadmin;

--
-- Name: rload_pages; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE rload_pages (
    page_id integer DEFAULT nextval(('rload_pages_page_id_seq'::text)::regclass) NOT NULL,
    pagename character varying(75),
    iserrorpage character(1) DEFAULT 'N'::bpchar,
    inserttime timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.rload_pages OWNER TO artadmin;

--
-- Name: rload_pages_page_id_seq; Type: SEQUENCE; Schema: public; Owner: artadmin
--

CREATE SEQUENCE rload_pages_page_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.rload_pages_page_id_seq OWNER TO artadmin;

--
-- Name: rload_sessions; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE rload_sessions (
    session_id integer DEFAULT nextval(('rload_sessions_session_id_se'::text)::regclass) NOT NULL,
    ipaddress character varying(20),
    sessiontxt character varying(50),
    browsertype character varying(125),
    user_id integer,
    inserttime timestamp without time zone DEFAULT '1970-01-01 01:00:00'::timestamp without time zone NOT NULL
);


ALTER TABLE public.rload_sessions OWNER TO artadmin;

--
-- Name: rload_sessions_session_id_se; Type: SEQUENCE; Schema: public; Owner: artadmin
--

CREATE SEQUENCE rload_sessions_session_id_se
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.rload_sessions_session_id_se OWNER TO artadmin;

--
-- Name: rload_users; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE rload_users (
    user_id integer DEFAULT nextval(('rload_users_user_id_seq'::text)::regclass) NOT NULL,
    username character varying(25),
    fullname character varying(64),
    companyname character varying(50),
    inserttime timestamp without time zone DEFAULT '1970-01-01 01:00:00'::timestamp without time zone NOT NULL
);


ALTER TABLE public.rload_users OWNER TO artadmin;

--
-- Name: rload_users_user_id_seq; Type: SEQUENCE; Schema: public; Owner: artadmin
--

CREATE SEQUENCE rload_users_user_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.rload_users_user_id_seq OWNER TO artadmin;

--
-- Name: sequencetable; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE sequencetable (
    sequencename character varying(255) DEFAULT ''::character varying NOT NULL,
    count integer
);


ALTER TABLE public.sequencetable OWNER TO artadmin;

--
-- Name: sessions; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE sessions (
    session_id numeric(19,0) DEFAULT nextval(('sessions_session_id_seq'::text)::regclass) NOT NULL,
    ipaddress character varying(20),
    sessiontxt character varying(70),
    browsertype character varying(255),
    lastmodtime timestamp without time zone DEFAULT now() NOT NULL,
    inserttime timestamp without time zone DEFAULT now() NOT NULL,
    user_id integer,
    context_id integer,
    sessionstarttime timestamp without time zone DEFAULT '1970-01-01 01:00:00'::timestamp without time zone NOT NULL,
    sessionendtime timestamp without time zone DEFAULT '1970-01-01 01:00:00'::timestamp without time zone NOT NULL,
    sessionhits integer,
    sessionduration bigint DEFAULT 0,
    experience integer,
    CONSTRAINT sessions_context_id_check CHECK ((context_id >= 0)),
    CONSTRAINT sessions_sessionduration_check CHECK ((sessionduration >= 0))
)
WITH (autovacuum_enabled=true, autovacuum_analyze_threshold=1000000);


ALTER TABLE public.sessions OWNER TO artadmin;

--
-- Name: sessions_session_id_seq; Type: SEQUENCE; Schema: public; Owner: artadmin
--

CREATE SEQUENCE sessions_session_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.sessions_session_id_seq OWNER TO artadmin;

--
-- Name: stacktracebeancontainers; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE stacktracebeancontainers (
    trace_id integer DEFAULT 0 NOT NULL,
    jspbeancontainer text
);


ALTER TABLE public.stacktracebeancontainers OWNER TO artadmin;

--
-- Name: stacktracedetails; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE stacktracedetails (
    trace_id integer DEFAULT 0 NOT NULL,
    row_id integer DEFAULT 0 NOT NULL,
    stack_depth integer NOT NULL
);


ALTER TABLE public.stacktracedetails OWNER TO artadmin;

--
-- Name: stacktracerows; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE stacktracerows (
    row_id integer DEFAULT nextval(('stacktracerows_row_id_seq'::text)::regclass) NOT NULL,
    row_message character varying(250)
);


ALTER TABLE public.stacktracerows OWNER TO artadmin;

--
-- Name: stacktracerows_row_id_seq; Type: SEQUENCE; Schema: public; Owner: artadmin
--

CREATE SEQUENCE stacktracerows_row_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.stacktracerows_row_id_seq OWNER TO artadmin;

--
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
-- Name: users; Type: TABLE; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE TABLE users (
    user_id integer DEFAULT nextval(('users_user_id_seq'::text)::regclass) NOT NULL,
    username character varying(25),
    fullname character varying(64),
    companyname character varying(50),
    lastmodtime timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.users OWNER TO artadmin;

--
-- Name: users_user_id_seq; Type: SEQUENCE; Schema: public; Owner: artadmin
--

CREATE SEQUENCE users_user_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.users_user_id_seq OWNER TO artadmin;

--
-- Name: SESSION_AND_REQUEST_TOKEN_UK; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY loadtest_requests
    ADD CONSTRAINT "SESSION_AND_REQUEST_TOKEN_UK" UNIQUE (loadtest_export_id, session_id, request_token);


--
-- Name: accessrecords_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY accessrecords
    ADD CONSTRAINT accessrecords_pkey PRIMARY KEY (recordpk);


--
-- Name: accumulator_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY accumulator
    ADD CONSTRAINT accumulator_pkey PRIMARY KEY (accumulatorstat_id);


--
-- Name: accumulatorevent_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY accumulatorevent
    ADD CONSTRAINT accumulatorevent_pkey PRIMARY KEY (accumulatorevent_id);


--
-- Name: accumulatorstats_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY accumulatorstats
    ADD CONSTRAINT accumulatorstats_pkey PRIMARY KEY (accumulatorstat_id, context_id, "time");


--
-- Name: apps_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY apps
    ADD CONSTRAINT apps_pkey PRIMARY KEY (app_id);


--
-- Name: branches_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY branches
    ADD CONSTRAINT branches_pkey PRIMARY KEY (branch_tag_id);


--
-- Name: browsers_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY browsers
    ADD CONSTRAINT browsers_pkey PRIMARY KEY (browser_id);


--
-- Name: contexts_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY contexts
    ADD CONSTRAINT contexts_pkey PRIMARY KEY (context_id);


--
-- Name: dailypageloadtimes_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY dailypageloadtimes
    ADD CONSTRAINT dailypageloadtimes_pkey PRIMARY KEY (dailyloadtime_id);


--
-- Name: dailysummary_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY dailysummary
    ADD CONSTRAINT dailysummary_pkey PRIMARY KEY (day);


--
-- Name: deployment_map_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY deployment_map
    ADD CONSTRAINT deployment_map_pkey PRIMARY KEY (key);


--
-- Name: deploymentjobs_job_id_key; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY deploymentjobs
    ADD CONSTRAINT deploymentjobs_job_id_key UNIQUE (job_id);


--
-- Name: deploymentjobs_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY deploymentjobs
    ADD CONSTRAINT deploymentjobs_pkey PRIMARY KEY (createtime);


--
-- Name: deploymenttagnames_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY deploymenttagnames
    ADD CONSTRAINT deploymenttagnames_pkey PRIMARY KEY (createtime);


--
-- Name: deploymenttagnames_tagname_key; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY deploymenttagnames
    ADD CONSTRAINT deploymenttagnames_tagname_key UNIQUE (tagname);


--
-- Name: externalaccessrecords_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY externalaccessrecords
    ADD CONSTRAINT externalaccessrecords_pkey PRIMARY KEY (recordpk);


--
-- Name: externalstats_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY externalstats
    ADD CONSTRAINT externalstats_pkey PRIMARY KEY (classification_id);


--
-- Name: fivesecondloads_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY fivesecondloads
    ADD CONSTRAINT fivesecondloads_pkey PRIMARY KEY (recordpk);


--
-- Name: htmlpageresponse_pk; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY htmlpageresponse
    ADD CONSTRAINT htmlpageresponse_pk PRIMARY KEY (htmlpageresponse_id);


--
-- Name: instances_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY instances
    ADD CONSTRAINT instances_pkey PRIMARY KEY (instance_id);


--
-- Name: loadtests_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY loadtests
    ADD CONSTRAINT loadtests_pkey PRIMARY KEY (loadtest_id);


--
-- Name: loadtestscript_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY loadtestscript
    ADD CONSTRAINT loadtestscript_pkey PRIMARY KEY (script_id);


--
-- Name: loadtesttransactions_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY loadtesttransactions
    ADD CONSTRAINT loadtesttransactions_pkey PRIMARY KEY (transaction_id);


--
-- Name: machines_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY machines
    ADD CONSTRAINT machines_pkey PRIMARY KEY (machine_id);


--
-- Name: pages_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY pages
    ADD CONSTRAINT pages_pkey PRIMARY KEY (page_id);


--
-- Name: pk_loadtest_export; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY loadtest_export
    ADD CONSTRAINT pk_loadtest_export PRIMARY KEY (loadtest_export_id);


--
-- Name: queryparameters_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY queryparameters
    ADD CONSTRAINT queryparameters_pkey PRIMARY KEY (queryparameter_id);


--
-- Name: rload_accessrecords_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY rload_accessrecords
    ADD CONSTRAINT rload_accessrecords_pkey PRIMARY KEY (recordpk);


--
-- Name: rload_apps_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY rload_apps
    ADD CONSTRAINT rload_apps_pkey PRIMARY KEY (app_id);


--
-- Name: rload_contexts_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY rload_contexts
    ADD CONSTRAINT rload_contexts_pkey PRIMARY KEY (context_id);


--
-- Name: rload_machines_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY rload_machines
    ADD CONSTRAINT rload_machines_pkey PRIMARY KEY (machine_id);


--
-- Name: rload_pages_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY rload_pages
    ADD CONSTRAINT rload_pages_pkey PRIMARY KEY (page_id);


--
-- Name: rload_sessions_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY rload_sessions
    ADD CONSTRAINT rload_sessions_pkey PRIMARY KEY (session_id);


--
-- Name: rload_users_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY rload_users
    ADD CONSTRAINT rload_users_pkey PRIMARY KEY (user_id);


--
-- Name: sequencetable_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY sequencetable
    ADD CONSTRAINT sequencetable_pkey PRIMARY KEY (sequencename);


--
-- Name: sessions_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY sessions
    ADD CONSTRAINT sessions_pkey PRIMARY KEY (session_id);


--
-- Name: stacktracedetails_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY stacktracedetails
    ADD CONSTRAINT stacktracedetails_pkey PRIMARY KEY (trace_id, stack_depth);


--
-- Name: stacktracerows_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY stacktracerows
    ADD CONSTRAINT stacktracerows_pkey PRIMARY KEY (row_id);


--
-- Name: stacktraces_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY stacktraces
    ADD CONSTRAINT stacktraces_pkey PRIMARY KEY (trace_id);


--
-- Name: user_users; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY users
    ADD CONSTRAINT user_users UNIQUE (username);


--
-- Name: users_pkey; Type: CONSTRAINT; Schema: public; Owner: artadmin; Tablespace: 
--

ALTER TABLE ONLY users
    ADD CONSTRAINT users_pkey PRIMARY KEY (user_id);


--
-- Name: TIME_CONTEXT; Type: INDEX; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE INDEX "TIME_CONTEXT" ON accumulatorstats USING btree ("time", context_id);


--
-- Name: accessrecords_sessionid; Type: INDEX; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE INDEX accessrecords_sessionid ON accessrecords USING btree (session_id);


--
-- Name: accessrecords_time2_accessrecords_index; Type: INDEX; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE INDEX accessrecords_time2_accessrecords_index ON accessrecords USING btree ("time");


--
-- Name: accessrecords_time_as_date_index; Type: INDEX; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE INDEX accessrecords_time_as_date_index ON accessrecords USING btree (date("time"));


--
-- Name: accumulatorevent_time; Type: INDEX; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE INDEX accumulatorevent_time ON accumulatorevent USING btree ("time");


--
-- Name: browserstats_id_day_browser_id_browserstats_index; Type: INDEX; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE UNIQUE INDEX browserstats_id_day_browser_id_browserstats_index ON browserstats USING btree (day, browser_id);


--
-- Name: contexts_name_index; Type: INDEX; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE UNIQUE INDEX contexts_name_index ON contexts USING btree (contextname);


--
-- Name: dailyloadtimes_id_day_page_context_dailypageloadtimes_index; Type: INDEX; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE UNIQUE INDEX dailyloadtimes_id_day_page_context_dailypageloadtimes_index ON dailypageloadtimes USING btree (day, page_id, context_id, instance_id);


--
-- Name: date_index_20060106-2; Type: INDEX; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE INDEX "date_index_20060106-2" ON sessions USING btree (sessionstarttime, context_id, sessionhits) WHERE ((sessionhits > 1) AND (context_id IS NOT NULL));


--
-- Name: deployments_iscurrent; Type: INDEX; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE INDEX deployments_iscurrent ON deployments USING btree (iscurrent);


--
-- Name: external_minute_statistics_time_classification_index; Type: INDEX; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE INDEX external_minute_statistics_time_classification_index ON externalminutestatistics USING btree ("time", classification_id);


--
-- Name: externalaccess_time_pk; Type: INDEX; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE INDEX externalaccess_time_pk ON externalaccessrecords USING btree ("time");


--
-- Name: htmlpageresponse_experience; Type: INDEX; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE INDEX htmlpageresponse_experience ON htmlpageresponse USING btree (experience);


--
-- Name: htmlpageresponse_pageid; Type: INDEX; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE INDEX htmlpageresponse_pageid ON htmlpageresponse USING btree (page_id);


--
-- Name: htmlpageresponse_requesttoken; Type: INDEX; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE INDEX htmlpageresponse_requesttoken ON htmlpageresponse USING btree (requesttoken);


--
-- Name: htmlpageresponse_sessiontxt; Type: INDEX; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE INDEX htmlpageresponse_sessiontxt ON htmlpageresponse USING btree (sessiontxt);


--
-- Name: htmlpageresponse_time; Type: INDEX; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE INDEX htmlpageresponse_time ON htmlpageresponse USING btree ("time");


--
-- Name: minute_statistics_time_index_minutestatistics_index; Type: INDEX; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE INDEX minute_statistics_time_index_minutestatistics_index ON minutestatistics USING btree ("time");


--
-- Name: queryparameters_lastmodtime; Type: INDEX; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE INDEX queryparameters_lastmodtime ON queryparameters USING btree (lastmodtime);


--
-- Name: queryparameters_queryparams_md5; Type: INDEX; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE UNIQUE INDEX queryparameters_queryparams_md5 ON queryparameters USING btree (value_hash);


--
-- Name: queryparamrecords_queryparameter; Type: INDEX; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE INDEX queryparamrecords_queryparameter ON queryparamrecords USING btree (queryparameter_id);


--
-- Name: queryparamrecords_recordpk; Type: INDEX; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE INDEX queryparamrecords_recordpk ON queryparamrecords USING btree (recordpk);


--
-- Name: row_id_ind_stacktracedetails_index; Type: INDEX; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE INDEX row_id_ind_stacktracedetails_index ON stacktracedetails USING btree (row_id);


--
-- Name: session_id_loadtest_export_id; Type: INDEX; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE INDEX session_id_loadtest_export_id ON loadtest_requests USING btree (session_id, loadtest_export_id);


--
-- Name: sessions_session_txt_context_index_sessions_index; Type: INDEX; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE INDEX sessions_session_txt_context_index_sessions_index ON sessions USING btree (sessiontxt, context_id);


--
-- Name: sessions_session_txt_index_sessions_index; Type: INDEX; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE INDEX sessions_session_txt_index_sessions_index ON sessions USING btree (sessiontxt, ipaddress);


--
-- Name: sessions_time_indenx_sessions_index; Type: INDEX; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE INDEX sessions_time_indenx_sessions_index ON sessions USING btree (sessionstarttime, sessionendtime);


--
-- Name: stacktrace_time__message_stacktraces_index; Type: INDEX; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE INDEX stacktrace_time__message_stacktraces_index ON stacktraces USING btree (trace_time, trace_message);


--
-- Name: stacktrace_time_stacktraces_index; Type: INDEX; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE INDEX stacktrace_time_stacktraces_index ON stacktraces USING btree (trace_time);


--
-- Name: stacktrace_trace_kay_stacktraces_index; Type: INDEX; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE INDEX stacktrace_trace_kay_stacktraces_index ON stacktraces USING btree (trace_key);


--
-- Name: trace_stack_row_stacktracedetails_index; Type: INDEX; Schema: public; Owner: artadmin; Tablespace: 
--

CREATE INDEX trace_stack_row_stacktracedetails_index ON stacktracedetails USING btree (trace_id, stack_depth, row_id);


--
-- Name: check_error_conditions_before; Type: TRIGGER; Schema: public; Owner: artadmin
--

CREATE TRIGGER check_error_conditions_before BEFORE INSERT ON htmlpageresponse FOR EACH ROW EXECUTE PROCEDURE html_error_detect();

ALTER TABLE htmlpageresponse DISABLE TRIGGER check_error_conditions_before;


--
-- Name: public; Type: ACL; Schema: -; Owner: pgsql
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM pgsql;
GRANT ALL ON SCHEMA public TO pgsql;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- Name: export_loadtest_requests(character varying, character varying, timestamp with time zone, timestamp with time zone, character varying); Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON FUNCTION export_loadtest_requests(character varying, character varying, timestamp with time zone, timestamp with time zone, character varying) FROM PUBLIC;
REVOKE ALL ON FUNCTION export_loadtest_requests(character varying, character varying, timestamp with time zone, timestamp with time zone, character varying) FROM artadmin;
GRANT ALL ON FUNCTION export_loadtest_requests(character varying, character varying, timestamp with time zone, timestamp with time zone, character varying) TO artadmin;
GRANT ALL ON FUNCTION export_loadtest_requests(character varying, character varying, timestamp with time zone, timestamp with time zone, character varying) TO PUBLIC;
GRANT ALL ON FUNCTION export_loadtest_requests(character varying, character varying, timestamp with time zone, timestamp with time zone, character varying) TO "artUsers";


--
-- Name: getqueryparams(integer); Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON FUNCTION getqueryparams("RecordPK" integer) FROM PUBLIC;
REVOKE ALL ON FUNCTION getqueryparams("RecordPK" integer) FROM artadmin;
GRANT ALL ON FUNCTION getqueryparams("RecordPK" integer) TO artadmin;
GRANT ALL ON FUNCTION getqueryparams("RecordPK" integer) TO PUBLIC;
GRANT ALL ON FUNCTION getqueryparams("RecordPK" integer) TO "artUsers";


--
-- Name: gettesturlstrings(character varying, character varying, timestamp without time zone, timestamp without time zone, character varying, character varying); Type: ACL; Schema: public; Owner: pgsql
--

REVOKE ALL ON FUNCTION gettesturlstrings("fromContext" character varying, "fromMachines" character varying, "fromStartTime" timestamp without time zone, "fromStopTime" timestamp without time zone, "toHost" character varying, "toContext" character varying) FROM PUBLIC;
REVOKE ALL ON FUNCTION gettesturlstrings("fromContext" character varying, "fromMachines" character varying, "fromStartTime" timestamp without time zone, "fromStopTime" timestamp without time zone, "toHost" character varying, "toContext" character varying) FROM pgsql;
GRANT ALL ON FUNCTION gettesturlstrings("fromContext" character varying, "fromMachines" character varying, "fromStartTime" timestamp without time zone, "fromStopTime" timestamp without time zone, "toHost" character varying, "toContext" character varying) TO pgsql;
GRANT ALL ON FUNCTION gettesturlstrings("fromContext" character varying, "fromMachines" character varying, "fromStartTime" timestamp without time zone, "fromStopTime" timestamp without time zone, "toHost" character varying, "toContext" character varying) TO PUBLIC;
GRANT ALL ON FUNCTION gettesturlstrings("fromContext" character varying, "fromMachines" character varying, "fromStartTime" timestamp without time zone, "fromStopTime" timestamp without time zone, "toHost" character varying, "toContext" character varying) TO "artUsers";


--
-- Name: populate_playback_requests(character varying, character varying, timestamp without time zone, timestamp without time zone, character varying); Type: ACL; Schema: public; Owner: pgsql
--

REVOKE ALL ON FUNCTION populate_playback_requests(character varying, character varying, timestamp without time zone, timestamp without time zone, character varying) FROM PUBLIC;
REVOKE ALL ON FUNCTION populate_playback_requests(character varying, character varying, timestamp without time zone, timestamp without time zone, character varying) FROM pgsql;
GRANT ALL ON FUNCTION populate_playback_requests(character varying, character varying, timestamp without time zone, timestamp without time zone, character varying) TO pgsql;
GRANT ALL ON FUNCTION populate_playback_requests(character varying, character varying, timestamp without time zone, timestamp without time zone, character varying) TO PUBLIC;
GRANT ALL ON FUNCTION populate_playback_requests(character varying, character varying, timestamp without time zone, timestamp without time zone, character varying) TO "artUsers";


--
-- Name: setlastmodtime(); Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON FUNCTION setlastmodtime() FROM PUBLIC;
REVOKE ALL ON FUNCTION setlastmodtime() FROM artadmin;
GRANT ALL ON FUNCTION setlastmodtime() TO artadmin;
GRANT ALL ON FUNCTION setlastmodtime() TO PUBLIC;
GRANT ALL ON FUNCTION setlastmodtime() TO "artUsers";


--
-- Name: testparse(character varying); Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON FUNCTION testparse(machines character varying) FROM PUBLIC;
REVOKE ALL ON FUNCTION testparse(machines character varying) FROM artadmin;
GRANT ALL ON FUNCTION testparse(machines character varying) TO artadmin;
GRANT ALL ON FUNCTION testparse(machines character varying) TO PUBLIC;
GRANT ALL ON FUNCTION testparse(machines character varying) TO "artUsers";


--
-- Name: accessrecords_recordpk_seq; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON SEQUENCE accessrecords_recordpk_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE accessrecords_recordpk_seq FROM artadmin;
GRANT ALL ON SEQUENCE accessrecords_recordpk_seq TO artadmin;
GRANT SELECT,UPDATE ON SEQUENCE accessrecords_recordpk_seq TO "artUsers";


--
-- Name: accessrecords; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE accessrecords FROM PUBLIC;
REVOKE ALL ON TABLE accessrecords FROM artadmin;
GRANT ALL ON TABLE accessrecords TO artadmin;
GRANT SELECT,INSERT,UPDATE ON TABLE accessrecords TO "artUsers";


--
-- Name: accumulator; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE accumulator FROM PUBLIC;
REVOKE ALL ON TABLE accumulator FROM artadmin;
GRANT ALL ON TABLE accumulator TO artadmin;
GRANT SELECT,INSERT,UPDATE ON TABLE accumulator TO "artUsers";


--
-- Name: accumulator_accumulatorstat_; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON SEQUENCE accumulator_accumulatorstat_ FROM PUBLIC;
REVOKE ALL ON SEQUENCE accumulator_accumulatorstat_ FROM artadmin;
GRANT ALL ON SEQUENCE accumulator_accumulatorstat_ TO artadmin;
GRANT SELECT,UPDATE ON SEQUENCE accumulator_accumulatorstat_ TO "artUsers";


--
-- Name: accumulatorevent; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE accumulatorevent FROM PUBLIC;
REVOKE ALL ON TABLE accumulatorevent FROM artadmin;
GRANT ALL ON TABLE accumulatorevent TO artadmin;
GRANT SELECT,INSERT,UPDATE ON TABLE accumulatorevent TO "artUsers";


--
-- Name: accumulatorevent_accumulator; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON SEQUENCE accumulatorevent_accumulator FROM PUBLIC;
REVOKE ALL ON SEQUENCE accumulatorevent_accumulator FROM artadmin;
GRANT ALL ON SEQUENCE accumulatorevent_accumulator TO artadmin;
GRANT SELECT,UPDATE ON SEQUENCE accumulatorevent_accumulator TO "artUsers";


--
-- Name: accumulatorstats; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE accumulatorstats FROM PUBLIC;
REVOKE ALL ON TABLE accumulatorstats FROM artadmin;
GRANT ALL ON TABLE accumulatorstats TO artadmin;
GRANT SELECT,INSERT,UPDATE ON TABLE accumulatorstats TO "artUsers";


--
-- Name: apps; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE apps FROM PUBLIC;
REVOKE ALL ON TABLE apps FROM artadmin;
GRANT ALL ON TABLE apps TO artadmin;
GRANT SELECT,INSERT,UPDATE ON TABLE apps TO "artUsers";


--
-- Name: apps_app_id_seq; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON SEQUENCE apps_app_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE apps_app_id_seq FROM artadmin;
GRANT ALL ON SEQUENCE apps_app_id_seq TO artadmin;
GRANT SELECT,UPDATE ON SEQUENCE apps_app_id_seq TO "artUsers";


--
-- Name: branches; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE branches FROM PUBLIC;
REVOKE ALL ON TABLE branches FROM artadmin;
GRANT ALL ON TABLE branches TO artadmin;
GRANT SELECT,INSERT,UPDATE ON TABLE branches TO "artUsers";


--
-- Name: branches_branch_tag_id_seq; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON SEQUENCE branches_branch_tag_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE branches_branch_tag_id_seq FROM artadmin;
GRANT ALL ON SEQUENCE branches_branch_tag_id_seq TO artadmin;
GRANT SELECT,UPDATE ON SEQUENCE branches_branch_tag_id_seq TO "artUsers";


--
-- Name: browsers; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE browsers FROM PUBLIC;
REVOKE ALL ON TABLE browsers FROM artadmin;
GRANT ALL ON TABLE browsers TO artadmin;
GRANT SELECT,INSERT,UPDATE ON TABLE browsers TO "artUsers";


--
-- Name: browsers_browser_id_seq; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON SEQUENCE browsers_browser_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE browsers_browser_id_seq FROM artadmin;
GRANT ALL ON SEQUENCE browsers_browser_id_seq TO artadmin;
GRANT SELECT,UPDATE ON SEQUENCE browsers_browser_id_seq TO "artUsers";


--
-- Name: browserstats; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE browserstats FROM PUBLIC;
REVOKE ALL ON TABLE browserstats FROM artadmin;
GRANT ALL ON TABLE browserstats TO artadmin;
GRANT SELECT,INSERT,UPDATE ON TABLE browserstats TO "artUsers";


--
-- Name: contexts; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE contexts FROM PUBLIC;
REVOKE ALL ON TABLE contexts FROM artadmin;
GRANT ALL ON TABLE contexts TO artadmin;
GRANT SELECT,INSERT,UPDATE ON TABLE contexts TO "artUsers";


--
-- Name: contexts_context_id_seq; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON SEQUENCE contexts_context_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE contexts_context_id_seq FROM artadmin;
GRANT ALL ON SEQUENCE contexts_context_id_seq TO artadmin;
GRANT SELECT,UPDATE ON SEQUENCE contexts_context_id_seq TO "artUsers";


--
-- Name: dailycontextstats; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE dailycontextstats FROM PUBLIC;
REVOKE ALL ON TABLE dailycontextstats FROM artadmin;
GRANT ALL ON TABLE dailycontextstats TO artadmin;
GRANT SELECT,INSERT,UPDATE ON TABLE dailycontextstats TO "artUsers";


--
-- Name: dailypageloadtimes; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE dailypageloadtimes FROM PUBLIC;
REVOKE ALL ON TABLE dailypageloadtimes FROM artadmin;
GRANT ALL ON TABLE dailypageloadtimes TO artadmin;
GRANT SELECT,INSERT,UPDATE ON TABLE dailypageloadtimes TO "artUsers";


--
-- Name: dailypageloadtimes_dailyload; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON SEQUENCE dailypageloadtimes_dailyload FROM PUBLIC;
REVOKE ALL ON SEQUENCE dailypageloadtimes_dailyload FROM artadmin;
GRANT ALL ON SEQUENCE dailypageloadtimes_dailyload TO artadmin;
GRANT SELECT,UPDATE ON SEQUENCE dailypageloadtimes_dailyload TO "artUsers";


--
-- Name: dailysummary; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE dailysummary FROM PUBLIC;
REVOKE ALL ON TABLE dailysummary FROM artadmin;
GRANT ALL ON TABLE dailysummary TO artadmin;
GRANT SELECT,INSERT,UPDATE ON TABLE dailysummary TO "artUsers";


--
-- Name: deployments; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE deployments FROM PUBLIC;
REVOKE ALL ON TABLE deployments FROM artadmin;
GRANT ALL ON TABLE deployments TO artadmin;
GRANT SELECT,INSERT,UPDATE ON TABLE deployments TO "artUsers";


--
-- Name: externalaccessrecords; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE externalaccessrecords FROM PUBLIC;
REVOKE ALL ON TABLE externalaccessrecords FROM artadmin;
GRANT ALL ON TABLE externalaccessrecords TO artadmin;
GRANT SELECT,INSERT,UPDATE ON TABLE externalaccessrecords TO "artUsers";


--
-- Name: externalaccessrecords_record; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON SEQUENCE externalaccessrecords_record FROM PUBLIC;
REVOKE ALL ON SEQUENCE externalaccessrecords_record FROM artadmin;
GRANT ALL ON SEQUENCE externalaccessrecords_record TO artadmin;
GRANT SELECT,UPDATE ON SEQUENCE externalaccessrecords_record TO "artUsers";


--
-- Name: externalminutestatistics; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE externalminutestatistics FROM PUBLIC;
REVOKE ALL ON TABLE externalminutestatistics FROM artadmin;
GRANT ALL ON TABLE externalminutestatistics TO artadmin;
GRANT SELECT,INSERT,UPDATE ON TABLE externalminutestatistics TO "artUsers";


--
-- Name: externalstats; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE externalstats FROM PUBLIC;
REVOKE ALL ON TABLE externalstats FROM artadmin;
GRANT ALL ON TABLE externalstats TO artadmin;
GRANT SELECT,INSERT,UPDATE ON TABLE externalstats TO "artUsers";


--
-- Name: externalstats_classification; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON SEQUENCE externalstats_classification FROM PUBLIC;
REVOKE ALL ON SEQUENCE externalstats_classification FROM artadmin;
GRANT ALL ON SEQUENCE externalstats_classification TO artadmin;
GRANT SELECT,UPDATE ON SEQUENCE externalstats_classification TO "artUsers";


--
-- Name: fivesecondloads; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE fivesecondloads FROM PUBLIC;
REVOKE ALL ON TABLE fivesecondloads FROM artadmin;
GRANT ALL ON TABLE fivesecondloads TO artadmin;
GRANT SELECT,INSERT,UPDATE ON TABLE fivesecondloads TO "artUsers";


--
-- Name: fivesecondloads_recordpk_seq; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON SEQUENCE fivesecondloads_recordpk_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE fivesecondloads_recordpk_seq FROM artadmin;
GRANT ALL ON SEQUENCE fivesecondloads_recordpk_seq TO artadmin;
GRANT SELECT,UPDATE ON SEQUENCE fivesecondloads_recordpk_seq TO "artUsers";


--
-- Name: historical_external_statistics; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE historical_external_statistics FROM PUBLIC;
REVOKE ALL ON TABLE historical_external_statistics FROM artadmin;
GRANT ALL ON TABLE historical_external_statistics TO artadmin;
GRANT SELECT,INSERT,UPDATE ON TABLE historical_external_statistics TO "artUsers";


--
-- Name: hourlystatistics; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE hourlystatistics FROM PUBLIC;
REVOKE ALL ON TABLE hourlystatistics FROM artadmin;
GRANT ALL ON TABLE hourlystatistics TO artadmin;
GRANT SELECT,INSERT,UPDATE ON TABLE hourlystatistics TO "artUsers";


--
-- Name: htmlpageresponse; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE htmlpageresponse FROM PUBLIC;
REVOKE ALL ON TABLE htmlpageresponse FROM artadmin;
GRANT ALL ON TABLE htmlpageresponse TO artadmin;
GRANT SELECT,INSERT,UPDATE ON TABLE htmlpageresponse TO "artUsers";


--
-- Name: instances; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE instances FROM PUBLIC;
REVOKE ALL ON TABLE instances FROM artadmin;
GRANT ALL ON TABLE instances TO artadmin;
GRANT SELECT,INSERT,UPDATE ON TABLE instances TO "artUsers";


--
-- Name: instances_instance_id_seq; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON SEQUENCE instances_instance_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE instances_instance_id_seq FROM artadmin;
GRANT ALL ON SEQUENCE instances_instance_id_seq TO artadmin;
GRANT SELECT,UPDATE ON SEQUENCE instances_instance_id_seq TO "artUsers";


--
-- Name: loadtest_export; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE loadtest_export FROM PUBLIC;
REVOKE ALL ON TABLE loadtest_export FROM artadmin;
GRANT ALL ON TABLE loadtest_export TO artadmin;
GRANT SELECT,INSERT,UPDATE ON TABLE loadtest_export TO "artUsers";


--
-- Name: loadtest_export_seq; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON SEQUENCE loadtest_export_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE loadtest_export_seq FROM artadmin;
GRANT ALL ON SEQUENCE loadtest_export_seq TO artadmin;
GRANT SELECT,UPDATE ON SEQUENCE loadtest_export_seq TO "artUsers";


--
-- Name: loadtest_requests; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE loadtest_requests FROM PUBLIC;
REVOKE ALL ON TABLE loadtest_requests FROM artadmin;
GRANT ALL ON TABLE loadtest_requests TO artadmin;
GRANT SELECT,INSERT,UPDATE ON TABLE loadtest_requests TO "artUsers";


--
-- Name: loadtests; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE loadtests FROM PUBLIC;
REVOKE ALL ON TABLE loadtests FROM artadmin;
GRANT ALL ON TABLE loadtests TO artadmin;
GRANT SELECT,INSERT,UPDATE ON TABLE loadtests TO "artUsers";


--
-- Name: loadtests_loadtest_id_seq; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON SEQUENCE loadtests_loadtest_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE loadtests_loadtest_id_seq FROM artadmin;
GRANT ALL ON SEQUENCE loadtests_loadtest_id_seq TO artadmin;
GRANT SELECT,UPDATE ON SEQUENCE loadtests_loadtest_id_seq TO "artUsers";


--
-- Name: loadtestscript; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE loadtestscript FROM PUBLIC;
REVOKE ALL ON TABLE loadtestscript FROM artadmin;
GRANT ALL ON TABLE loadtestscript TO artadmin;
GRANT SELECT,INSERT,UPDATE ON TABLE loadtestscript TO "artUsers";


--
-- Name: loadtestscript_script_id_seq; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON SEQUENCE loadtestscript_script_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE loadtestscript_script_id_seq FROM artadmin;
GRANT ALL ON SEQUENCE loadtestscript_script_id_seq TO artadmin;
GRANT SELECT,UPDATE ON SEQUENCE loadtestscript_script_id_seq TO "artUsers";


--
-- Name: loadtestsummary; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE loadtestsummary FROM PUBLIC;
REVOKE ALL ON TABLE loadtestsummary FROM artadmin;
GRANT ALL ON TABLE loadtestsummary TO artadmin;
GRANT SELECT,INSERT,UPDATE ON TABLE loadtestsummary TO "artUsers";


--
-- Name: loadtesttransactionminuterecords; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE loadtesttransactionminuterecords FROM PUBLIC;
REVOKE ALL ON TABLE loadtesttransactionminuterecords FROM artadmin;
GRANT ALL ON TABLE loadtesttransactionminuterecords TO artadmin;
GRANT SELECT,INSERT,UPDATE ON TABLE loadtesttransactionminuterecords TO "artUsers";


--
-- Name: loadtesttransactions; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE loadtesttransactions FROM PUBLIC;
REVOKE ALL ON TABLE loadtesttransactions FROM artadmin;
GRANT ALL ON TABLE loadtesttransactions TO artadmin;
GRANT SELECT,INSERT,UPDATE ON TABLE loadtesttransactions TO "artUsers";


--
-- Name: loadtesttransactions_transac; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON SEQUENCE loadtesttransactions_transac FROM PUBLIC;
REVOKE ALL ON SEQUENCE loadtesttransactions_transac FROM artadmin;
GRANT ALL ON SEQUENCE loadtesttransactions_transac TO artadmin;
GRANT SELECT,UPDATE ON SEQUENCE loadtesttransactions_transac TO "artUsers";


--
-- Name: loadtesttransactionsummary; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE loadtesttransactionsummary FROM PUBLIC;
REVOKE ALL ON TABLE loadtesttransactionsummary FROM artadmin;
GRANT ALL ON TABLE loadtesttransactionsummary TO artadmin;
GRANT SELECT,INSERT,UPDATE ON TABLE loadtesttransactionsummary TO "artUsers";


--
-- Name: machines; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE machines FROM PUBLIC;
REVOKE ALL ON TABLE machines FROM artadmin;
GRANT ALL ON TABLE machines TO artadmin;
GRANT SELECT,INSERT,UPDATE ON TABLE machines TO "artUsers";


--
-- Name: machines_machine_id_seq; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON SEQUENCE machines_machine_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE machines_machine_id_seq FROM artadmin;
GRANT ALL ON SEQUENCE machines_machine_id_seq TO artadmin;
GRANT SELECT,UPDATE ON SEQUENCE machines_machine_id_seq TO "artUsers";


--
-- Name: minutestatistics; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE minutestatistics FROM PUBLIC;
REVOKE ALL ON TABLE minutestatistics FROM artadmin;
GRANT ALL ON TABLE minutestatistics TO artadmin;
GRANT SELECT,INSERT,UPDATE ON TABLE minutestatistics TO "artUsers";


--
-- Name: pages; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE pages FROM PUBLIC;
REVOKE ALL ON TABLE pages FROM artadmin;
GRANT ALL ON TABLE pages TO artadmin;
GRANT SELECT,INSERT,UPDATE ON TABLE pages TO "artUsers";


--
-- Name: pages_page_id_seq; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON SEQUENCE pages_page_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE pages_page_id_seq FROM artadmin;
GRANT ALL ON SEQUENCE pages_page_id_seq TO artadmin;
GRANT SELECT,UPDATE ON SEQUENCE pages_page_id_seq TO "artUsers";


--
-- Name: playback_requests_test_number_seq; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON SEQUENCE playback_requests_test_number_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE playback_requests_test_number_seq FROM artadmin;
GRANT ALL ON SEQUENCE playback_requests_test_number_seq TO artadmin;
GRANT SELECT,UPDATE ON SEQUENCE playback_requests_test_number_seq TO "artUsers";


--
-- Name: queryparameters; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE queryparameters FROM PUBLIC;
REVOKE ALL ON TABLE queryparameters FROM artadmin;
GRANT ALL ON TABLE queryparameters TO artadmin;
GRANT SELECT,INSERT,UPDATE ON TABLE queryparameters TO "artUsers";


--
-- Name: queryparameters_queryparamet; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON SEQUENCE queryparameters_queryparamet FROM PUBLIC;
REVOKE ALL ON SEQUENCE queryparameters_queryparamet FROM artadmin;
GRANT ALL ON SEQUENCE queryparameters_queryparamet TO artadmin;
GRANT SELECT,UPDATE ON SEQUENCE queryparameters_queryparamet TO "artUsers";


--
-- Name: queryparamrecord_indexlist; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE queryparamrecord_indexlist FROM PUBLIC;
REVOKE ALL ON TABLE queryparamrecord_indexlist FROM artadmin;
GRANT ALL ON TABLE queryparamrecord_indexlist TO artadmin;
GRANT SELECT,INSERT,UPDATE ON TABLE queryparamrecord_indexlist TO "artUsers";


--
-- Name: queryparamrecords; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE queryparamrecords FROM PUBLIC;
REVOKE ALL ON TABLE queryparamrecords FROM artadmin;
GRANT ALL ON TABLE queryparamrecords TO artadmin;
GRANT SELECT,INSERT,UPDATE ON TABLE queryparamrecords TO "artUsers";


--
-- Name: rload_accessrecords; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE rload_accessrecords FROM PUBLIC;
REVOKE ALL ON TABLE rload_accessrecords FROM artadmin;
GRANT ALL ON TABLE rload_accessrecords TO artadmin;
GRANT SELECT,INSERT,UPDATE ON TABLE rload_accessrecords TO "artUsers";


--
-- Name: rload_accessrecords_recordpk; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON SEQUENCE rload_accessrecords_recordpk FROM PUBLIC;
REVOKE ALL ON SEQUENCE rload_accessrecords_recordpk FROM artadmin;
GRANT ALL ON SEQUENCE rload_accessrecords_recordpk TO artadmin;
GRANT SELECT,UPDATE ON SEQUENCE rload_accessrecords_recordpk TO "artUsers";


--
-- Name: rload_apps; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE rload_apps FROM PUBLIC;
REVOKE ALL ON TABLE rload_apps FROM artadmin;
GRANT ALL ON TABLE rload_apps TO artadmin;
GRANT SELECT,INSERT,UPDATE ON TABLE rload_apps TO "artUsers";


--
-- Name: rload_apps_app_id_seq; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON SEQUENCE rload_apps_app_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE rload_apps_app_id_seq FROM artadmin;
GRANT ALL ON SEQUENCE rload_apps_app_id_seq TO artadmin;
GRANT SELECT,UPDATE ON SEQUENCE rload_apps_app_id_seq TO "artUsers";


--
-- Name: rload_contexts; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE rload_contexts FROM PUBLIC;
REVOKE ALL ON TABLE rload_contexts FROM artadmin;
GRANT ALL ON TABLE rload_contexts TO artadmin;
GRANT SELECT,INSERT,UPDATE ON TABLE rload_contexts TO "artUsers";


--
-- Name: rload_contexts_context_id_se; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON SEQUENCE rload_contexts_context_id_se FROM PUBLIC;
REVOKE ALL ON SEQUENCE rload_contexts_context_id_se FROM artadmin;
GRANT ALL ON SEQUENCE rload_contexts_context_id_se TO artadmin;
GRANT SELECT,UPDATE ON SEQUENCE rload_contexts_context_id_se TO "artUsers";


--
-- Name: rload_machines; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE rload_machines FROM PUBLIC;
REVOKE ALL ON TABLE rload_machines FROM artadmin;
GRANT ALL ON TABLE rload_machines TO artadmin;
GRANT SELECT,INSERT,UPDATE ON TABLE rload_machines TO "artUsers";


--
-- Name: rload_machines_machine_id_se; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON SEQUENCE rload_machines_machine_id_se FROM PUBLIC;
REVOKE ALL ON SEQUENCE rload_machines_machine_id_se FROM artadmin;
GRANT ALL ON SEQUENCE rload_machines_machine_id_se TO artadmin;
GRANT SELECT,UPDATE ON SEQUENCE rload_machines_machine_id_se TO "artUsers";


--
-- Name: rload_pages; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE rload_pages FROM PUBLIC;
REVOKE ALL ON TABLE rload_pages FROM artadmin;
GRANT ALL ON TABLE rload_pages TO artadmin;
GRANT SELECT,INSERT,UPDATE ON TABLE rload_pages TO "artUsers";


--
-- Name: rload_pages_page_id_seq; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON SEQUENCE rload_pages_page_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE rload_pages_page_id_seq FROM artadmin;
GRANT ALL ON SEQUENCE rload_pages_page_id_seq TO artadmin;
GRANT SELECT,UPDATE ON SEQUENCE rload_pages_page_id_seq TO "artUsers";


--
-- Name: rload_sessions; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE rload_sessions FROM PUBLIC;
REVOKE ALL ON TABLE rload_sessions FROM artadmin;
GRANT ALL ON TABLE rload_sessions TO artadmin;
GRANT SELECT,INSERT,UPDATE ON TABLE rload_sessions TO "artUsers";


--
-- Name: rload_sessions_session_id_se; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON SEQUENCE rload_sessions_session_id_se FROM PUBLIC;
REVOKE ALL ON SEQUENCE rload_sessions_session_id_se FROM artadmin;
GRANT ALL ON SEQUENCE rload_sessions_session_id_se TO artadmin;
GRANT SELECT,UPDATE ON SEQUENCE rload_sessions_session_id_se TO "artUsers";


--
-- Name: rload_users; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE rload_users FROM PUBLIC;
REVOKE ALL ON TABLE rload_users FROM artadmin;
GRANT ALL ON TABLE rload_users TO artadmin;
GRANT SELECT,INSERT,UPDATE ON TABLE rload_users TO "artUsers";


--
-- Name: rload_users_user_id_seq; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON SEQUENCE rload_users_user_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE rload_users_user_id_seq FROM artadmin;
GRANT ALL ON SEQUENCE rload_users_user_id_seq TO artadmin;
GRANT SELECT,UPDATE ON SEQUENCE rload_users_user_id_seq TO "artUsers";


--
-- Name: sequencetable; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE sequencetable FROM PUBLIC;
REVOKE ALL ON TABLE sequencetable FROM artadmin;
GRANT ALL ON TABLE sequencetable TO artadmin;
GRANT SELECT,INSERT,UPDATE ON TABLE sequencetable TO "artUsers";


--
-- Name: sessions; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE sessions FROM PUBLIC;
REVOKE ALL ON TABLE sessions FROM artadmin;
GRANT ALL ON TABLE sessions TO artadmin;
GRANT SELECT,INSERT,UPDATE ON TABLE sessions TO "artUsers";


--
-- Name: sessions_session_id_seq; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON SEQUENCE sessions_session_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE sessions_session_id_seq FROM artadmin;
GRANT ALL ON SEQUENCE sessions_session_id_seq TO artadmin;
GRANT SELECT,UPDATE ON SEQUENCE sessions_session_id_seq TO "artUsers";


--
-- Name: stacktracebeancontainers; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE stacktracebeancontainers FROM PUBLIC;
REVOKE ALL ON TABLE stacktracebeancontainers FROM artadmin;
GRANT ALL ON TABLE stacktracebeancontainers TO artadmin;
GRANT SELECT,INSERT,UPDATE ON TABLE stacktracebeancontainers TO "artUsers";


--
-- Name: stacktracedetails; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE stacktracedetails FROM PUBLIC;
REVOKE ALL ON TABLE stacktracedetails FROM artadmin;
GRANT ALL ON TABLE stacktracedetails TO artadmin;
GRANT SELECT,INSERT,UPDATE ON TABLE stacktracedetails TO "artUsers";


--
-- Name: stacktracerows; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE stacktracerows FROM PUBLIC;
REVOKE ALL ON TABLE stacktracerows FROM artadmin;
GRANT ALL ON TABLE stacktracerows TO artadmin;
GRANT SELECT,INSERT,UPDATE ON TABLE stacktracerows TO "artUsers";


--
-- Name: stacktracerows_row_id_seq; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON SEQUENCE stacktracerows_row_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE stacktracerows_row_id_seq FROM artadmin;
GRANT ALL ON SEQUENCE stacktracerows_row_id_seq TO artadmin;
GRANT SELECT,UPDATE ON SEQUENCE stacktracerows_row_id_seq TO "artUsers";


--
-- Name: stacktraces; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE stacktraces FROM PUBLIC;
REVOKE ALL ON TABLE stacktraces FROM artadmin;
GRANT ALL ON TABLE stacktraces TO artadmin;
GRANT SELECT,INSERT,UPDATE ON TABLE stacktraces TO "artUsers";


--
-- Name: users; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON TABLE users FROM PUBLIC;
REVOKE ALL ON TABLE users FROM artadmin;
GRANT ALL ON TABLE users TO artadmin;
GRANT SELECT,INSERT,UPDATE ON TABLE users TO "artUsers";


--
-- Name: users_user_id_seq; Type: ACL; Schema: public; Owner: artadmin
--

REVOKE ALL ON SEQUENCE users_user_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE users_user_id_seq FROM artadmin;
GRANT ALL ON SEQUENCE users_user_id_seq TO artadmin;
GRANT SELECT,UPDATE ON SEQUENCE users_user_id_seq TO "artUsers";


--
-- PostgreSQL database dump complete
--

