export ORACLE_SID=ARTP
export ORACLE_HOME=/opt/oracle/product/816
sqlplus / << EOF
execute LogParser.DELETE_ACCESSRECORDS('$1')
exit
EOF

