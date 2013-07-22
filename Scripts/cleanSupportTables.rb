#!/usr/bin/env jruby
#
require 'java'
require 'rubygems'
require '../jars/postgresql-9.2-1002.jdbc4.jar'
require 'active_record'
require 'benchmark'

ActiveRecord::Base.establish_connection(
    :adapter=> "jdbc",
    :driver=> "org.postgresql.Driver",
    :url=> "jdbc:postgresql://prod-art-db1:5432/artdb",
    :host => "prod-art-db1",
    :database=> "artdb",
    :username => "artadmin",
    :password => "abc123"
)

Benchmark.bm(25) do |x|
    x.report("delete from sessions") { result = ActiveRecord::Base.connection.execute("delete from sessions where inserttime < (now() - interval '4 weeks')") ; puts "deleted #{result} records" }
    x.report("delete from users") { result = ActiveRecord::Base.connection.execute("delete from users where lastmodtime < (now() - interval '12 weeks')"); puts "deleted #{result} records" }
    x.report("delete from htmlpageresponse") { result = ActiveRecord::Base.connection.execute("delete from htmlpageresponse where time < (now() - interval '3 days')"); puts "deleted #{result} records"  }
    x.report("delete from queryparameter") { result = ActiveRecord::Base.connection.execute("delete from queryparameters where lastmodtime < (now() - interval '3 days')"); puts "deleted #{result} records"  }
end