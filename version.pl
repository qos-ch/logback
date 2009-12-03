
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

    my $origFile = "$filename.original";
    
    rename($filename, $origFile);
    open(OUT, ">$filename");
    open(IN, "$origFile");
    
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
    unlink($origFile);
  } else {
    print "File [" . $filename . "] does not exist\r\n" 
  }
}

foreach $ARG (@ARGV) {
  do replace($ARG);
}



