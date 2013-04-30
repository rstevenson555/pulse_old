#!/bin/bash
export base=general_query
export datadir=/var/lib/mysql
cd $datadir
mv $base.3 $base.4
mv $base.2 $base.3
mv $base.1 $base.2
mv $base.0 $base.1
mv $base.log $base.0
mysqladmin -u root --password=oak flush-logs

