#!/usr/bin/env jruby
#
require 'java'
require 'rubygems'
require '../jars/postgresql-9.2-1002.jdbc4.jar'
require 'active_record'

ActiveRecord::Base.establish_connection(
    :adapter=> "jdbc",
    :driver=> "org.postgresql.Driver",
    :url=> "jdbc:postgresql://prod-art-db1:5432/artdb",
    :host => "prod-art-db1",
    :database=> "artdb",
    :username => "artadmin",
    :password => "abc123"
)

result = ActiveRecord::Base.connection.execute("delete from sessions where inserttime < (now() - interval '2 weeks')")
result = ActiveRecord::Base.connection.execute("delete from users where lastmodtime < (now() - interval '12 weeks')")
