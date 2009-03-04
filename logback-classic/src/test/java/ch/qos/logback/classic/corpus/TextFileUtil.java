package ch.qos.logback.classic.corpus;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TextFileUtil {

  //FileReader fr = new FileReader(filename);
  //BufferedReader br = new BufferedReader(fr);
  
  public static List<String> toWords(BufferedReader br) throws IOException {
   

    // (\\d+)$
    //String regExp = "^(\\d+) "+ msg +  " ([\\dabcdef-]+)$";
    //Pattern p = Pattern.compile(regExp);
    String line;
    
   List<String> wordList = new ArrayList<String>();
    
    while ((line = br.readLine()) != null) {
      //line = line.replaceAll("\\p{Punct}+", " ");
      String[] words = line.split("\\s");
      for(String word: words) {
        wordList.add(word);
      }
    }  
    br.close();
  
    return wordList;
  }
}
