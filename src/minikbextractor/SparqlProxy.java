/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package minikbextractor;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author murloc
 */
public class SparqlProxy implements Serializable
{
    
    //static part
    
    private static HashMap<String, SparqlProxy> insts;
    
    private static ArrayList<String> labelsUri;
    private static ArrayList<String> subUri;
    private static ArrayList<String> excludeUri;
    
    private static boolean initiated = false;
    
    public static SparqlProxy getSparqlProxy(String url)
    {
        if(!initiated)
        {
            insts = new HashMap<>();
            
            labelsUri = new ArrayList<>();
            labelsUri.add("http://www.w3.org/2004/02/skos/core#prefLabel");
            labelsUri.add("http://www.w3.org/2004/02/skos/core#altLabel");


            subUri = new ArrayList<>();
            subUri.add("http://www.w3.org/2004/02/skos/core#broader");

            excludeUri = new ArrayList<>();
            excludeUri.add("http://www.w3.org/2004/02/skos/core#prefLabel");
            excludeUri.add("http://www.w3.org/2004/02/skos/core#altLabel");
            excludeUri.add("http://www.w3.org/2004/02/skos/core#broader");
            excludeUri.add("http://www.w3.org/2008/05/skos-xl#prefLabel");
            excludeUri.add("http://www.w3.org/2008/05/skos-xl#altLabel");
            excludeUri.add("http://www.w3.org/2004/02/skos/core#narrower");
            excludeUri.add("http://www.w3.org/2004/02/skos/core#broader");
        
            initiated = true;
        }
        
        
        SparqlProxy inst = insts.get(url);
        
        if(inst == null)
        {
            inst = new SparqlProxy(url);
            insts.put(url, inst);
        }
        
        return inst;
    }
    
    
    public static  String  makeQuery(String q)
        {
             String query ="PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
                                            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
                                            "PREFIX owl:    <http://www.w3.org/2002/07/owl#>"+
                                            "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"+
                                            "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>"+
                                            //"PREFIX foaf: <http://xmlns.com/foaf/0.1/>"+
                                            //"PREFIX dc: <http://purl.org/dc/elements/1.1/>"+
                                            //"PREFIX pf: <http://jena.hpl.hp.com/ARQ/property#>"+
                                            "PREFIX agrovoc: <http://aims.fao.org/aos/agrontology#>"+q;
             return query;
        }
      
      public static  StringBuilder  makeQuery(StringBuilder q)
        {
             StringBuilder query = new StringBuilder("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
                                            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
                                            "PREFIX owl:    <http://www.w3.org/2002/07/owl#>"+
                                            "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"+
                                            "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>"+
                                            //"PREFIX foaf: <http://xmlns.com/foaf/0.1/>"+
                                            //"PREFIX dc: <http://purl.org/dc/elements/1.1/>"+
                                            //"PREFIX pf: <http://jena.hpl.hp.com/ARQ/property#>"+
                                            "PREFIX agrovoc: <http://aims.fao.org/aos/agrontology#>");
             query.append(q);
             
             
             return query;
        }
    
      public static String cleanString(String s)
        {
            return s.replaceAll("\r", "").replaceAll("\n", "").replaceAll("\"", "");
        }
      
       public static boolean isSubRel(String relUri)
        {
            return subUri.contains(relUri);
        }
        
        public static boolean isLabelRel(String relUri)
        {
            return labelsUri.contains(relUri);
        }
        
        public static boolean isExcludeRel(String relUri)
        {
            return excludeUri.contains(relUri);
        }
      
      
        public static void saveQueryOnFile(String fileName, String query)
        {
            try 
            {
                FileUtils.writeStringToFile(new File("out/backupQueries/"+fileName),query);
            } 
            catch (IOException ex) 
            {
                System.err.println("FAILED TO BACKUP the query into file "+fileName);
            }
        }
        
        
    // non static part
    private String urlServer;
    
    private SparqlProxy(String urlServer)
    {
        this.urlServer = urlServer;
    }
    
    /*private JSONArray getResponse(String query)
        {
            HttpURLConnection connection = null;  
            JSONArray arr = null;
            String jsonRet = "";
            try 
            {
                 URL url = new URL(this.urlServer+"query?output=json&query="+URLEncoder.encode(query, "UTF-8"));
              //Create connection
              connection = (HttpURLConnection)url.openConnection();
              connection.setRequestMethod("GET");
              connection.setRequestProperty("Content-Type", 
                   "application/x-www-form-urlencoded");


              //Get Response	
              InputStream is = connection.getInputStream();
              BufferedReader rd = new BufferedReader(new InputStreamReader(is));
              String line;
              StringBuilder response = new StringBuilder(); 
              while((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
              }
              rd.close();
              jsonRet = response.toString();
            
              JSONObject json = (JSONObject) JSONSerializer.toJSON(jsonRet);
              arr =  json.getJSONObject("results").getJSONArray("bindings");
            }
            catch(Exception e)
            {
                System.err.println("ERROR during the response parsing... : "+e);
                System.err.println(query);
                SparqlProxy.saveQueryOnFile("SELECT_query", query);
                System.exit(0);
            }
             finally 
            {
              if(connection != null) 
              {
                connection.disconnect(); 
              }
            }
              return arr;
        }*/
    
        public ArrayList<JsonNode> getResponse(String query)
        {
            HttpURLConnection connection = null;  
            ArrayList<JsonNode> arr = null;
            String jsonRet = "";
            query = SparqlProxy.makeQuery(query);
            try 
            {
                 URL url = new URL(this.urlServer+"query?output=json&query="+URLEncoder.encode(query, "UTF-8"));
              //Create connection
              connection = (HttpURLConnection)url.openConnection();
              connection.setRequestMethod("GET");
              connection.setRequestProperty("Content-Type", 
                   "application/x-www-form-urlencoded");


              //Get Response	
              InputStream is = connection.getInputStream();
              BufferedReader rd = new BufferedReader(new InputStreamReader(is));
              String line;
              StringBuilder response = new StringBuilder(); 
              while((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
              }
              rd.close();
             // System.out.println("Fin tout!");
              jsonRet = response.toString();
            
              /*System.out.println("string");
              JSONObject json = (JSONObject) JSONSerializer.toJSON(jsonRet);
              System.out.println("json o");
              arr =  json.getJSONObject("results").getJSONArray("bindings");
              System.out.println("results");*/
              
                   ObjectMapper mapper = new ObjectMapper();
                //JsonFactory factory = mapper.getFactory(); // since 2.1 use mapper.getFactory() instead
                //JsonParser jp = factory.createJsonParser(jsonRet);
                // System.out.println("Parsing result (Jackson) ...");
                JsonNode root = mapper.readTree(jsonRet);
                  //System.out.println("Results parsed!");
                  Iterator<JsonNode> i = root.get("results").get("bindings").iterator();
                  while(i.hasNext())
                  {
                      if(arr == null)
                      {
                          arr = new ArrayList<>();
                      }
                      arr.add(i.next());
                  }
            }
            catch(Exception e)
            {
                System.err.println("ERROR during the response parsing... : "+e);
                System.err.println(query);
                SparqlProxy.saveQueryOnFile("SELECT_query", query);
                System.exit(0);
            }
             finally 
            {
              if(connection != null) 
              {
                connection.disconnect(); 
              }
            }
              return arr;
        }
    
         public String getDescribe(String uri)
        {
            HttpURLConnection connection = null;  
            ArrayList<JsonNode> arr = null;
            String ret = "";
            //query = SparqlProxy.makeQuery(query);
            String query = "DESCRIBE <"+uri+">";
            try 
            {
                 URL url = new URL(this.urlServer+"query?output=text&query="+URLEncoder.encode(query, "UTF-8"));
              //Create connection
              connection = (HttpURLConnection)url.openConnection();
              connection.setRequestMethod("GET");
              connection.setRequestProperty("Content-Type", 
                   "application/x-www-form-urlencoded");


              //Get Response	
              InputStream is = connection.getInputStream();
              BufferedReader rd = new BufferedReader(new InputStreamReader(is));
              String line;
              StringBuilder response = new StringBuilder(); 
              while((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
              }
              rd.close();
             // System.out.println("Fin tout!");
              ret = response.toString();
            }
            catch(IOException e)
            {
                System.err.println("ERROR during the response parsing... : "+e);
                System.err.println(query);
                SparqlProxy.saveQueryOnFile("SELECT_query", query);
                System.exit(0);
            }
             finally 
            {
              if(connection != null) 
              {
                connection.disconnect(); 
              }
            }
              return ret;
        }
        
    
        /*public ArrayList<JSONObject> sendQuery(String query)
        {
            ArrayList<JSONObject> ret = new ArrayList<>();
            
            query = this.makeQuery(query);
            
            
            JSONArray resp = this.getResponse(query);
            
            int i = 0;
            if(resp != null && !resp.isEmpty())
            {
                for(Object o : resp)
                {
                    i++;
                    JSONObject jo = (JSONObject) o;
                    ret.add(jo);
                }
            }
            
            return ret;
        }*/
    
         public boolean storeData(StringBuilder query)
         {
             return this.storeData(query, true);
         }
         
        public boolean storeData(StringBuilder query, boolean makeQuery)
    {
        boolean ret = true;
        if(makeQuery)
            query = SparqlProxy.makeQuery(query);
         HttpURLConnection connection = null;  
            try 
            {
                String urlParameters = "update="+URLEncoder.encode(query.toString(), "UTF-8");
                 URL url = new URL(this.urlServer+"update");
                //Create connection
                connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("POST");

               connection.setDoOutput(true);

              OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());

              writer.write(urlParameters);
              writer.flush();

              String line;
              BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
              String rep = "";
              while ((line = reader.readLine()) != null) {
                  rep += line;
              }
              writer.close();
              reader.close();  
            }
            catch(Exception e)
            {
                System.err.println("ERROR UPDATE : "+e);
                SparqlProxy.saveQueryOnFile("Query.sparql", query.toString());
                ret = false;
            }
             finally 
            {
              if(connection != null) 
              {
                connection.disconnect(); 
              }
            }
            return ret;
    }
        
        
        public void writeKBFile(String fileName)
    {
        
         HttpURLConnection connection = null;  
            try 
            {
                 URL url = new URL(this.urlServer+"data?default");
              //Create connection
              connection = (HttpURLConnection)url.openConnection();
              connection.setRequestMethod("GET");

            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            
            FileWriter f = new FileWriter("out/"+fileName+".owl");
            

            while ((line = reader.readLine()) != null) {
                f.write(line+"\n");
            }
            reader.close();    
            f.close();
            }
            catch(Exception e)
            {System.err.println("ERROR writing file : "+e);}
             finally 
            {
              if(connection != null) 
              {
                connection.disconnect(); 
              }
            }
    }
        
        public boolean sendAskQuery(String query)
        {
            boolean ret = false;
            
            HttpURLConnection connection = null;  
            JsonNode arr = null;
            query = SparqlProxy.makeQuery(query);
            try 
            {
                 URL url = new URL(this.urlServer+"query?output=json&query="+URLEncoder.encode(query, "UTF-8"));
              //Create connection
              connection = (HttpURLConnection)url.openConnection();
              connection.setRequestMethod("GET");
              connection.setRequestProperty("Content-Type", 
                   "application/x-www-form-urlencoded");


              //Get Response	
              InputStream is = connection.getInputStream();
              BufferedReader rd = new BufferedReader(new InputStreamReader(is));
              String line;
              StringBuilder response = new StringBuilder(); 
              while((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
              }
              rd.close();
             String  jsonRet = response.toString();
             ObjectMapper mapper = new ObjectMapper();
             JsonNode root = mapper.readTree(jsonRet);
              //JSONObject json = (JSONObject) JSONSerializer.toJSON(jsonRet);
              ret = root.get("boolean").asBoolean();
            }
            catch(Exception e)
            {System.err.println("ERROR during the response parsing...");}
             finally 
            {
              if(connection != null) 
              {
                connection.disconnect(); 
              }
            }
              return ret;
        }
        
        /*public boolean isSubClassOfStar(String s1, String s2)
        {
            String query = SparqlProxy.makeQuery("ASK {<"+s1+"> rdfs:subClassOf* <"+s2+">}");
            
            return this.sendAskQuery(query);
            
        }*/
        
        public void clearSp()
        {
            this.storeData(new StringBuilder("DELETE WHERE{?a ?b ?c}"));
        }
        
        public String getUrlServer()
        {
            return this.urlServer;
        }
    
}
