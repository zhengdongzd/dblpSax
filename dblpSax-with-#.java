package dblpSax;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

// Deal with the dblp dataset, 48,000,000 
public class dblpSax{
    
   public static void main(String[] args){


       System.setProperty("jdk.xml.entityExpansionLimit", "0");

       try{
         File inputFile = new File("dblp.xml");
         SAXParserFactory factory = SAXParserFactory.newInstance();

         factory.setNamespaceAware(true);
         factory.setValidating(true);


         File writename1 = new File("E:\\CODING FILES\\JavaCode\\dblpSax\\output\\authorString2num.txt");
         writename1.createNewFile(); 
         BufferedWriter authorString2numOut = new BufferedWriter(new FileWriter(writename1));

         File writename2 = new File("E:\\CODING FILES\\JavaCode\\dblpSax\\output\\edges.txt");
         writename2.createNewFile(); 
         BufferedWriter edgesOut = new BufferedWriter(new FileWriter(writename2));

         File writename3 = new File("E:\\CODING FILES\\JavaCode\\dblpSax\\output\\edgesWeight.txt");
         writename3.createNewFile(); 
         BufferedWriter edgesWeightOut = new BufferedWriter(new FileWriter(writename3));

         
         Map<String, Integer> hmedgesWeight = new HashMap<String, Integer>();
         
         SAXParser saxParser = factory.newSAXParser();
         UserHandler userhandler = new UserHandler(authorString2numOut, edgesOut, edgesWeightOut, hmedgesWeight);

         saxParser.parse(inputFile, userhandler);

         
         
         for (Map.Entry<String, Integer> entry : hmedgesWeight.entrySet()) {
             String key = entry.getKey();
             int value = entry.getValue();
             String[] ss = key.split("\\s+");
             edgesWeightOut.write("#" + ss[0] + "#" + "    " + "#" + ss[1] + "#" + "   " + value + "\r\n");               
             edgesWeightOut.flush();
         }
         
         //System.out.println(x);
         System.out.println("Edges Number: " + hmedgesWeight.size());
         
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
}


class UserHandler extends DefaultHandler {


    Map<String, Integer> hmauthorString2int = new HashMap<String, Integer>();
    int authorGlobalNum = 0;
    ArrayList<Integer> authorsToGroup = new ArrayList<Integer>();
    int parseLines = 0;

   private final BufferedWriter authorString2numOut;
   private final BufferedWriter edgesOut;
   private final BufferedWriter edgesWeightOut;
   private final Map<String, Integer> hmedgesWeight;

   UserHandler(BufferedWriter authorString2numOut, BufferedWriter edgesOut, BufferedWriter edgesWeightOut, Map<String, Integer> hmedgesWeight) {
       this.authorString2numOut = authorString2numOut;
       this.edgesOut = edgesOut;
       this.edgesWeightOut = edgesWeightOut;
       this.hmedgesWeight = hmedgesWeight;
   }


   boolean bauthor = false;
   boolean inOneArticleFlag = false;
   int authorsCount = 0;

   public void startElement(String uri, 
      String localName, String qName, Attributes attributes)
         throws SAXException {
      if (qName.equalsIgnoreCase("article")) {
          inOneArticleFlag = true;
      } else if (qName.equalsIgnoreCase("author")) {
         bauthor = true;
      }

   }


   public void endElement(String uri, 
      String localName, String qName) throws SAXException {
      if (qName.equalsIgnoreCase("article")) {
          inOneArticleFlag = false;
      }
      else if (qName.equalsIgnoreCase("title")) {
          inOneArticleFlag = false;
      }
   }


   public void characters(char ch[], 
      int start, int length) throws SAXException {

      int authorValue = -1;

      if (bauthor) {
          
          String author = new String(ch, start, length);

          
          if(hmauthorString2int.containsKey(author)){
              authorValue = hmauthorString2int.get(author);
          }
          else{
              authorValue = authorGlobalNum++;
              hmauthorString2int.put(author, authorValue);
              try{
                  
                  authorString2numOut.write(author + "    " + "#" + authorValue + "#" + "\r\n");           
                  authorString2numOut.flush();

                  }catch(IOException e){
                      ;
                  }
          }

         bauthor = false;

      }
      
      
      if(inOneArticleFlag && (authorValue!= -1)){
          //System.out.println("===============================");
          authorsToGroup.add(authorValue);
          authorsCount++;
      }

      if(!inOneArticleFlag){
          //System.out.println("*******************************");
          if(authorsToGroup.size()>1){
              for(int i = 0; i < authorsToGroup.size();i++)
                  for(int j = i + 1; j < authorsToGroup.size();j++){

                      String create = Integer.toString(authorsToGroup.get(i)) + "   " + Integer.toString(authorsToGroup.get(j));

                      String createExchange = Integer.toString(authorsToGroup.get(j)) + "   " + Integer.toString(authorsToGroup.get(i));
                      
                      if(hmedgesWeight.containsKey(create)==true)
                          hmedgesWeight.put(create, hmedgesWeight.get(create) + 1);
                      else if(hmedgesWeight.containsKey(createExchange)==true){
                          hmedgesWeight.put(createExchange, hmedgesWeight.get(createExchange) + 1);
                      }
                      else{
                          hmedgesWeight.put(create,1);
                          try{
                              edgesOut.write("#" + authorsToGroup.get(i) + "#" + " " + "#" + authorsToGroup.get(j) + "#" + "\r\n");
                              edgesOut.flush();
                          } catch(Exception e){
                              System.out.println("edgesWeightOut.write(authorsToGroup.get(i) problem. ");
                          }
                      }
                  }
                      
          }
          authorsToGroup.clear();
          authorsCount = 0;
      }
      
      parseLines++;
      
      if(parseLines % 1000000 == 0)//48,000,000 in total; should appear 48 times
          System.out.println(parseLines);

   }



}