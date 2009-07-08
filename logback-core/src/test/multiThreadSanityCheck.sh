

end[0]=116723
end[1]=113160
end[2]=100117
end[3]=118897
end[4]=111249
end[5]=119030
end[6]=106358
end[7]=107372
end[8]=113765
end[9]=110767


for t in $(seq 0 1 9) 
do
  echo doing $t
  grep "$t " aggregated |cut -d ' ' -f 2 > $t-def
  for i in $(seq 1 1 ${end[$t]}); do echo $i; done > $t-wit
  echo "diffing thread $t"
  diff -q $t-def $t-wit
  res=$?
  if [ $res != "0" ]; then
    echo "FAILED for $t"
    exit 1;
  fi
done

echo SUCCESS