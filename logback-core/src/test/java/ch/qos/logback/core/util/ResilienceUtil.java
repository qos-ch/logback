package ch.qos.logback.core.util;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResilienceUtil {

  
  static public void verify(String logfile, String regexp, long totalSteps, double successRatioLowerBound) throws NumberFormatException, IOException {
    FileReader fr = new FileReader(logfile);
    BufferedReader br = new BufferedReader(fr);
    Pattern p = Pattern.compile(regexp);
    String line;
    
    int totalLines = 0;
    int oldNum = -1;
    int gaps = 0;
    while ((line = br.readLine()) != null) {
      Matcher m = p.matcher(line);
      if (m.matches()) {
        totalLines++;
        String g = m.group(1);
        int num = Integer.parseInt(g);
        if(num != oldNum+1) {
          gaps++;
        }
        oldNum = num;
      }
    }
    fr.close();
    br.close();

    int lowerLimit = (int) (totalSteps*successRatioLowerBound);
    assertTrue("totalLines="+totalLines+" less than "+lowerLimit, totalLines > lowerLimit);
    
    // we want some gaps which indicate recuperation
    assertTrue("gaps="+gaps+" less than 3", gaps >= 3);
    
  }
}
