export ORACLE_SID=ARTP
export ORACLE_HOME=/opt/oracle/product/817
$ORACLE_HOME/bin/sqlplus / << EOF
execute LogParser.DELETE_ACCESSRECORDS('$1')
exit
EOF

