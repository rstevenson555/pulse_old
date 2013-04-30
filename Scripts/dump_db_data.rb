#!/usr/bin/env jruby
#
require 'java'
require 'rubygems'
require '../jars/postgresql-9.2-1002.jdbc4.jar'
require 'active_record'
require 'csv'

ActiveRecord::Base.establish_connection(
    :adapter=> "jdbc",
    :driver=> "org.postgresql.Driver",
    :url=> "jdbc:postgresql://prod-art-db1:5432/artdb",
    :host => "prod-art-db1",
    :database=> "artdb",
    :username => "artadmin",
    :password => "abc123"
)


#result = ActiveRecord::Base.connection.execute("select * from accessrecords order by recordpk desc limit 10000")
#result.each do |r| 
#    f << r.to_
#end


module Exporter
    DEFAULT_EXPORT_TABLES = [ ]
    DESTINATION_FOLDER = "c:\\apps\\"    
 
    def self.included(klass)
        klass.extend ClassLevelMethods
    end
 
    def self.export_tables_to_csv(tables = DEFAULT_EXPORT_TABLES,order = "",where = "")        
        ClassLevelMethods::set_order order
        ClassLevelMethods::set_where where
        tables.each &:export_table_to_csv
    end
 
    def data
        self.class.column_names.map { |column| send(column) }
    end
 
    module ClassLevelMethods
        @@order = ""
        @@where = ""
        
        def self.set_where w
            @@where = w
        end
        def self.set_order o
            @@order = o
        end
        
        def export_table_to_csv
            CSV.open(filename_for_class, "w") do |output_file|
                output_file << column_names
                data.each{ |row| output_file << row }
            end
        end
 
        def filename_for_class
            [DESTINATION_FOLDER, to_s.pluralize.underscore, '.csv'].join
        end
 
        def data
            #all.map(&:data)
            #last(100000).reverse.map(&:data)
            #limit(100000).order('oid desc').reverse.map(&:data)
            puts @@order
            puts @@where
            order(@@order).where(@@where).map(&:data)
        end
    end
end
 
class ActiveRecord::Base
    include Exporter
end

class Accessrecords < ActiveRecord::Base
end
class Sessions < ActiveRecord::Base
end
class Users < ActiveRecord::Base
end
class Pages < ActiveRecord::Base
end
class Queryparameters < ActiveRecord::Base
    #@@criteria = ""
end
class Queryparamrecords < ActiveRecord::Base
    #@@criteria = ""
end
#class Browsers < ActiveRecord::Base
#end

#puts Browsers.last(10).explain
#puts Browsers.last(10).to_sql

#ccessrecords::export_tables_to_csv
Exporter::export_tables_to_csv([Accessrecords],'recordpk desc','date(inserttime) = date(now())')
Exporter::export_tables_to_csv([Sessions],'session_id desc','date(inserttime) = date(now())')
Exporter::export_tables_to_csv([Users],'user_id desc','date(lastmodtime) = date(now())')
Exporter::export_tables_to_csv([Pages],'page_id desc','date(lastmodtime) = date(now())')
Exporter::export_tables_to_csv([Queryparameters],'queryparameter_id desc','date(lastmodtime) = date(now())')
Exporter::export_tables_to_csv([Queryparamrecords],'recordpk desc','oid > 0')

#puts a.public_methods
#puts a