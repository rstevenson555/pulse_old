cp ../jsperror.txt* .
rm ../jsperror.txt*
wc -l jsperror.txt* | sed /total/d > filelengths.dat
cd ..
rm -f  jsperror.tar.Z
rm -f somefile
rm -f jsperror.DL
ftp -n 10.7.209.201 < psswd.txt
rm -f jsperror.tar
compress -d jsperror.tar.Z
tar -xvf jsperror.tar
wc -l jsperror.txt* | sed /total/d > filelengths.dat
java -classpath .:~artadmin/jars/batchTools.jar logParser.Tools.mkTailBat > tailBat.sh
chmod +x tailBat.sh
tailBat.sh
java -classpath .:~artadmin/jars/batchTools.jar logParser.Tools.mkDLFiles

