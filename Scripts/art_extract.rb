require 'java'
require '../jars/postgresql-9.2-1002.jdbc4.jar'
require '../jars/ojdbc5.11.2.0.1.jar'
require 'rubygems'
require 'omx_jdbc_manager'
java_import 'java.sql.SQLException'
require 'benchmark'

include OMX_Common_DB


class QueryParamRecords < OMX_Common_DB::ValueObject
end
class QueryParameters < OMX_Common_DB::ValueObject
end
class Sessions < OMX_Common_DB::ValueObject
end
class AccessRecords < OMX_Common_DB::ValueObject
end
class Pages < OMX_Common_DB::ValueObject
end
class Users < OMX_Common_DB::ValueObject
end

class Authentication < OMX_Common_DB::ValueObject
end
class AuthenticationKey < OMX_Common_DB::ValueObject
end
artdb = DatabaseManager.new({
    :user=>'artadmin',
    :passwd=>'abc123',
    :url=>'jdbc:postgresql://prod-art-db1:5432/artdb',
    :driver=>"org.postgresql.Driver"
})

ioeq = DatabaseManager.new(
    {:user=>'i97_user',
    :passwd=>'horton',
    :url=>'jdbc:oracle:thin:@qa-ioeq-db1:1521:ioeq',
    :driver=>"oracle.jdbc.OracleDriver"}

)


Benchmark.bm(25) do |x|
    # dump art tables
    #
#        artdb.execute_prepared_query("drop table temp_accessrecords")

 #   artdb.execute_prepared_query("select * into temp_accessrecords from accessrecords")
    x.report("accessrecords") {
        begin
            records = 0
            #CSV.open('g:\\bi\\accessrecords.csv', "w",{:headers=>true}) do |csv|
            starttime = Time.new
            CSV.open('c:\\art_extract\\accessrecords.csv', "w",{:headers=>true}) do |csv|
                artdb.execute_prepared_query("SELECT * FROM temp_accessrecords ",AccessRecords,{:concurrent=>true,:each=>true,:autocommit=>false,:fetchsize=>20000,:queuesize=>60000}) do |r|
                    csv << r.to_csv_headings if ( csv.header_row?)
                    csv << r.to_csv
                    records += 1
                    if (records % 10000 == 0 ) 
                        puts "10000 records processed in #{Time.new - starttime}"
                        starttime = Time.new
                    end
                end
            end
            rescue SQLException=>e
                puts e
        end
    }
    artdb.execute_prepared_query("drop table temp_accessrecords")

    
    artdb.execute_prepared_query("select * into table temp_queryparamrecords from queryparamrecords")
    x.report("queryparamrecords") {
        begin
            CSV.open('c:\\art_extract\\queryparamrecords.csv', "w",{:headers=>true}) do |csv|
                artdb.execute_prepared_query("SELECT * FROM temp_queryparamrecords",QueryParamRecords,{:concurrent=>true,:each=>true,:autocommit=>false,:fetchsize=>500,:queuesize=>1000}) do |r|
                    csv << r.to_csv_headings if ( csv.header_row?)
                    csv << r.to_csv
                end
            end
            rescue SQLException=>e
                puts e
        end        
    }
    artdb.execute_prepared_query("drop table temp_queryparamrecords")

    #artdb.execute_prepared_query("drop table temp_queryparamrecords")
    artdb.execute_prepared_query("select * into temp_queryparameters from queryparameters")

    x.report("queryparameters") {
        begin
            CSV.open('c:\\art_extract\\queryparameters.csv', "w",{:headers=>true}) do |csv|
                artdb.execute_prepared_query("SELECT * FROM temp_queryparameters",QueryParameters,{:concurrent=>true,:each=>true,:autocommit=>false,:fetchsize=>500,:queuesize=>1000}) do |r|
                    csv << r.to_csv_headings if ( csv.header_row?)
                    csv << r.to_csv
                end
            end
            rescue SQLException=>e
                puts e
        end
    }
    artdb.execute_prepared_query("drop table temp_queryparameters")
    
    artdb.execute_prepared_query("select * into temp_sessions from sessions")
    x.report("sessions") {
        begin
            CSV.open('c:\\art_extract\\sessions.csv', "w",{:headers=>true}) do |csv|
                artdb.execute_prepared_query("SELECT * FROM temp_sessions",Sessions,{:concurrent=>true,:each=>true,:autocommit=>false,:fetchsize=>500,:queuesize=>1000}) do |r|
                    csv << r.to_csv_headings if ( csv.header_row?)
                    csv << r.to_csv
                end
            end
            rescue SQLException=>e
                puts e
        end
    }
    artdb.execute_prepared_query("drop table temp_sessions")

    artdb.execute_prepared_query("select * into temp_accessrecords from accessrecords")
    x.report("accessrecords") {
        begin
            CSV.open('c:\\art_extract\\accessrecords.csv', "w",{:headers=>true}) do |csv|
                artdb.execute_prepared_query("SELECT * FROM temp_accessrecords",AccessRecords,{:concurrent=>true,:each=>true,:autocommit=>false,:fetchsize=>500,:queuesize=>1000}) do |r|
                    csv << r.to_csv_headings if ( csv.header_row?)
                    csv << r.to_csv
                end
            end
            rescue SQLException=>e
                puts e
        end
    }
    artdb.execute_prepared_query("drop table temp_accessrecords")

    #
    
    artdb.execute_prepared_query("select * into temp_pages from pages")
    x.report("pages") {
        begin
            CSV.open('c:\\art_extract\\pages.csv', "w",{:headers=>true}) do |csv|
                artdb.execute_prepared_query("SELECT * FROM temp_pages",Pages,{:concurrent=>true,:each=>true,:autocommit=>false,:fetchsize=>500,:queuesize=>1000}) do |r|
                    csv << r.to_csv_headings if ( csv.header_row?)
                    csv << r.to_csv
                end
            end
            rescue SQLException=>e
                puts e
        end
    }
    artdb.execute_prepared_query("drop table temp_pages")

    usermap = {}
    x.report("user map") {    
        begin
            ioeq.execute_prepared_query("select a.user_key,c.company_name from account_group ag,authentication a,company_list c where ag.account_group = a.account_group and ag.company_id = c.company_id",AuthenticationKey,{:autocommit=>false,:concurrent=>true,:each=>true,:fetchsize=>1000,:queuesize=>1000}) do |accnt|
                usermap[accnt.user_key] = accnt.company_name
            end
        rescue SQLException=>e
                puts e
        end
    }

    artdb.execute_prepared_query("select * into temp_users from users")

    x.report("users") {    
        begin
            CSV.open('c:\\art_extract\\users.csv', "w",{:headers=>true}) do |csv|
            #CSV.open('users.csv', "w") do |csv|
                artdb.execute_prepared_query("SELECT * FROM temp_users",Users,{:concurrent=>true,:each=>true,:autocommit=>false,:fetchsize=>500,:queuesize=>1000}) do |users|
                    company_name = usermap[users.username]
                    users.companyname = company_name

                    csv << users.to_csv_headings if ( csv.header_row?)
                    csv << users.to_csv

                end
            end
            rescue SQLException=>e
                puts e
        end
    }
    artdb.execute_prepared_query("drop table temp_users")

end
