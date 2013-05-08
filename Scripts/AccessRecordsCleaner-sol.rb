require 'java'
require '../jars/postgresql-9.2-1002.jdbc4.jar'
require 'rubygems'
require 'omx_jdbc_manager'
java_import 'java.sql.SQLException'
require 'benchmark'
module JavaLang
    java_import 'java.lang.System'
end

include OMX_Common_DB

class AccessRecords < OMX_Common_DB::ValueObject
end

class AccessRecordsCleaner
    attr_accessor :artdb,:current_record_pk,:max_record_pk,:min_record_pk,:startTime, :initial_increment_amount
    
    DEFAULT_INCREMENT_AMOUNT = 2
    MAX_INCREMENT = 500000
    
    def restart
        @startTime = JavaLang::System.currentTimeMillis()
        @initial_increment_amount = DEFAULT_INCREMENT_AMOUNT
        
        @artdb = DatabaseManager.new({
            :user=>'artadmin',
            :passwd=>'abc123',
            :url=>'jdbc:postgresql://prod-art-db1:5432/artdb',
            :driver=>"org.postgresql.Driver"
        })

        # returns an array of 1
        record = @artdb.execute_prepared_query("select a.maxRPK as max_rpk , b.minRPK  as min_rpk, a.maxRPK - b.minRPK as diff from 
                    (select max(recordpk) as maxRPK from AccessRecords where time < now() - interval '12 days' - interval '12 hours' and time > now() - interval '16 days' - interval '12 hours' ) a, 
                    (select min(recordpk) as minRPK from AccessRecords ) b limit 1",AccessRecords,{:all=>true})
        
        puts record
        @min_record_pk = record[0].min_rpk
        @max_record_pk = record[0].max_rpk
        
        @current_record_pk = @min_record_pk
        @startTime = JavaLang::System.currentTimeMillis()        
    end
    
    def has_next
        return @current_record_pk < @max_record_pk
    end

    def next
        transactionStartTime = JavaLang::System.currentTimeMillis();
        
        maxRemoveRecordPK = (@current_record_pk + @initial_increment_amount < @max_record_pk) ? @current_record_pk + @initial_increment_amount : @max_record_pk;

        rowsRemoved = @artdb.execute_prepared_query("delete from accessrecords where recordpk>=? and recordpk<=?",nil,{:each=>true,:bind1=>@current_record_pk,:bind2=>maxRemoveRecordPK})
        qprowsRemoved = @artdb.execute_prepared_query("delete from QueryParamRecords where RecordPK>=? and RecordPK<=?",nil,{:each=>true,:bind1=>@current_record_pk,:bind2=>maxRemoveRecordPK})
        
        puts "AccessRecords Rows Removed: #{rowsRemoved}"
        puts "QueryParamRecords Removed : #{qprowsRemoved}"
        puts "delete from AccessRecords where RecordPK>=#{@current_record_pk} and RecordPK<= #{maxRemoveRecordPK}"
        
        @current_record_pk += @initial_increment_amount
        currentSystemTime = JavaLang::System.currentTimeMillis
        elapsedTime = currentSystemTime - transactionStartTime
        if ( elapsedTime < 4000)
            if (@initial_increment_amount < MAX_INCREMENT)
                @initial_increment_amount *= 2
            else
                @initial_increment_amount = MAX_INCREMENT
            end
        elsif (elapsedTime > 8000)
                if (@initial_increment_amount >4) 
                    @initial_increment_amount /= 2
                else
                    @initial_increment_amount = 4
                end
        end
        total_removals = @max_record_pk - @min_record_pk
        removed_removals = @current_record_pk - @min_record_pk
        percentCompleted = 1.0
        
        puts "total_removals #{total_removals}"
        puts "removed_removals #{removed_removals}" 
        
        if(total_removals >0)
            percentCompleted = removed_removals.to_f / total_removals.to_f
        end
        totalElapsedTime = currentSystemTime - @startTime
        puts "totalElapsedTime #{totalElapsedTime}"
        puts "percentCompleted #{percentCompleted}" 
        
        estimatedFinishTime = @startTime + (totalElapsedTime / percentCompleted)
        progressBarPercentage = (100.0 * percentCompleted).to_f
        
        return progressBarPercentage.to_i
    end
end

arc = AccessRecordsCleaner.new
arc.restart
while(arc.has_next)
    o = arc.next
    puts o
end

