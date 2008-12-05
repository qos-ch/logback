
if ($#ARGV < 1) {
  print "Usage: version.pl VER FILE {FILE, FILE}\n";
  exit;
}

$V= $ARGV[0];
print "VER:$V\r\n";
shift(@ARGV);

sub replace () {
  my $filename = $_[0];

  if(-s $filename) {
    print "Processing [" . $filename . "]\r\n";

    my $backup = "$filename.original";
    
    rename($filename, $backup);
    open(OUT, ">$filename");
    open(IN, "$backup");
    
    my $hitCount=0;
    while(<IN>) {
      if($hitCount == 0 && /<version>.*<\/version>/) {
        s/<version>.*<\/version>/<version>$V<\/version>/;
        $hitCount++;
      } 
      print OUT;
    }
    close(IN);
    close(OUT);
  } else {
    print "File [" . $filename . "] does not exist\r\n" 
  }
}

foreach $ARG (@ARGV) {
  do replace($ARG);
}



