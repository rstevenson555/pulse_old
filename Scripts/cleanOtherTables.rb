#!/usr/bin/env jruby
#
require 'java'
require 'rubygems'
require '../jars/postgresql-9.2-1002.jdbc4.jar'
require '../jars/joda-time-2.1.jar'
require 'active_record'
module Joda
java_import 'org.joda.time.DateMidnight'
java_import 'org.joda.time.DateTime'
java_import 'org.joda.time.Interval'
end

ActiveRecord::Base.establish_connection(
    :adapter=> "jdbc",
    :driver=> "org.postgresql.Driver",
    :url=> "jdbc:postgresql://prod-art-db1:5432/artdb",
    :host => "prod-art-db1",
    :database=> "artdb",
    :username => "artadmin",
    :password => "abc123"
)

class Sessions < ActiveRecord::Base
end

class Accessrecords < ActiveRecord::Base   
    self.table_name = "accessrecords"
end

two_weeks_back = Joda::DateTime.new
#two_weeks_back = two_weeks_back
two_weeks_back = two_weeks_back.minusWeeks(2)

s = Sessions.find(:first)
puts s.session_id

#r1 = Accessrecords.find_by_sql("select a.recordpk,a.*,q.* from accessrecords a,queryparamrecords q where a.recordpk = q.recordpk order by a.recordpk limit 10")
#r1.each do |r| 
#    puts r.inspect
#    puts r.queryparameter_id
#end

r1 = Accessrecords.find_by_sql("select cont.contextName,  page.pageName,  avg(ar.loadTime), count(ar.loadTime), 
                max(ar.loadTime)  
                from AccessRecords ar, Contexts cont, Pages page where ar.Time> '2013-03-11' and ar.Time< '2013-03-13'
                and cont.Context_ID=ar.Context_ID and page.Page_ID=ar.Page_ID 
                and page.pageName like '%%' and cont.contextName like '%shop%' 
                group by cont.contextName, page.pageName limit 5")
#puts "blah"
#puts r1.inspect
#puts r1
r1.each do |r|
    puts r.inspect
    puts r.to_xml
    puts r.to_json
    puts r.count
end

#r1 = Accessrecords.find_each(:batch_size=>50).where('recordpk=1563193962') do |r|
##r1.each do |r| 
#    puts r.inspect
#    puts r.queryparameter_id
#end

r2 = Accessrecords.where('recordpk=1563193962')
#puts r2.inserttime
puts r2.inspect
puts r2[0].to_xml
puts r2[0].page_id
puts r2[0].inserttime

some_model_objects = []
#result = ActiveRecord::Base.connection.execute("select * from sessions limit 1")
result = Sessions.find_by_sql("select * from sessions limit 10")
#s = Sessions.new(result)
#result.each_hash{ |res| some_model_objects << Sessions.new(res) }
#puts some_model_objects
puts result.length
#result = ActiveRecord::Base.connection.execute("delete from sessions where inserttime < '#{two_weeks_back}'")
#puts result

#"delete from sessions where inserttime < two_week_back"

require 'ostruct'

class MyBean < OpenStruct
    def initialize(hash)
        super(hash)
    end
end

mb = MyBean({'name'=>'joe'})
puts mb
