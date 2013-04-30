require 'java'
require 'rubygems'
require '../jars/ojdbc5.11.2.0.1.jar'
gem "activerecord", "=3.2.12"
require 'active_record'

ActiveRecord::Base.establish_connection(
    :adapter=> "jdbc",
    :driver=> "oracle.jdbc.OracleDriver",
    :url=> "jdbc:oracle:thin:@qa-ioeq-db1:1521:ioeq",
    :host => "qa-ioeq-db1",
    :database=> "ioeq",
    :username => "i97_user",
    :password => "horton"
)


class Authentication < ActiveRecord::Base    
    #set_table_name "authentication"
    #self.table_name "authentication"
    self.table_name = "authentication"
    def initialize(hash)         
        @login_id = hash['login_id']
    end
    def init(hash)         
        @login_id = hash['login_id']
    end
    def init_withttt(coder)
        @attributes = self.class.initialize_attributes(coder['attributes'])
        @relation = nil

        @attributes_cache, @previously_changed, @changed_attributes = {}, {}, {}
        @association_cache = {}
        @aggregation_cache = {}
        @readonly = @destroyed = @marked_for_destruction = false
        @new_record = false
    end
end

q = <<EOS
select auth.login_id, auth.login_state, i.first_name, i.last_name, i.email, g.account_group, g.account_group_name, g.account_group_state,
ag.group_name as user_acct_grp_name 
from authentication auth, info i, account_group g, user_acct_group ag
where auth.user_key=i.user_key and 
auth.account_group=g.account_group and 
auth.account_group='029393'
and auth.user_acct_grp_id=ag.user_acct_grp_id
and auth.login_type='S'
union
select auth.login_id, auth.login_state, i.first_name, i.last_name, i.email, g.account_group, g.account_group_name, g.account_group_state,
ag.group_name as user_acct_grp_name 
from authentication auth, info i, account_group g, user_acct_group ag
where auth.user_key=i.user_key and 
auth.account_group=g.account_group and 
auth.account_group='029393'
and auth.user_acct_grp_id=ag.user_acct_grp_id
and i.email is not null and
auth.user_key=ag.originator and
auth.login_type!='S'
EOS



#r = Authentication.find_by_sql(q)
#puts r
r = ActiveRecord::Base.connection.execute(q)
#puts r.inspect
puts r.class.name
puts r[0].class.name


require 'ostruct'

class MyBean < OpenStruct
    def initialize(hash)
        super(hash)
    end
end

mb = MyBean.new(r)
puts r

#puts r.to_xml
#a = Authentication.new(r[0])
#puts Authentication.public_methods(false)
#a = Authentication.allocate
#puts r[0].to_xml
#puts r[0].to_json
#puts r[0].to_yaml
##Authentication.after_save.clear
##a.init_with('attributes' => r[0])
#a = Authentication.new(r[0])
##puta a.inspect
#puts a.login_id
##puts a.login_id
##puts a.inspect
#puts a.public_methods
#r.each do |record|    
#    puts record['login_id']
#end

#result = ActiveRecord::Base.connection.execute("select * from accessrecords order by recordpk desc limit 10000")
#result.each do |r| 
#    f << r.to_
#end

#puts r
