use File::Find;
use File::Basename;
@DIRS = $ARGV[0];
$pattern = $ARGV[1];
sub processFile{
    $baseN="";
	$dirN="";
	$extN="";
	$linesProcessed = 0;
    $currentFile = $File::Find::name;
	($baseN,$dirN,$extN) = fileparse($currentFile,'\..*');
    if($dirN =~ /\/CVS\//){
        return;
    }elsif ($extN =~ /.*\,v/){
    	print "\nProcessing: $currentFile\n\n";
	}else{
        #print "\nNot FOUND : $currentFile\n\n";
        return;
    }
	$currentBaseName = $_;
	open(FILE,"+< $currentBaseName");
    $out='';
	while($line =<FILE>){
        $linesProcessed++;
		if($line =~ /$pattern/){
			chomp($line);
			print "Removing line:$linesProcessed $line\n";
		}else{
            $out .= $line;

        }
	}
    seek(FILE, 0, 0) or die "can't seek to start of $currentBaseName $!" ;
    print FILE $out  or die "can't print to $currentBaseName $!";
    truncate(FILE, tell(FILE)) or die "can't truncate $currentBaseName $!";
    close(FILE) or die "can't close $currentBaseName $!";
	close FILE;
}

find (\&processFile, @DIRS);
