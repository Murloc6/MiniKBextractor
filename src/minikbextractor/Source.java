/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package minikbextractor;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author fabien.amarger
 */
public class Source
{
    private SparqlProxy spIn;
    
    private String name;
    
    private HashMap<String, String> limitSpOut;
    
    private String adomFile;
    
    public Source(SparqlProxy spIn, String name, HashMap<String, String> limitSpOut, String adomFile)
    {
        this.spIn = spIn;
        this.name = name;
        this.limitSpOut = limitSpOut;
        this.adomFile = adomFile;
    }
    
    
    public String getStatUnderLimit(String limitUri)
    {
        String ret = "Stats of "+this.name+" with the limitUri : "+limitUri+"\n";
         ArrayList<JsonNode> response = spIn.getResponse("SELECT ?uri WHERE { ?uri <http://ontology.irstea.fr/AgronomicTaxon#hasHigherRank>+ <"+limitUri+">}");
        //ret += ("LIMIT Taxon : "+spIn.getDescribe(this.limitUri)+"\n");
        ret += ("-------NB : "+response.size()+"---- \n");
        int nbTotalUnderTaxon = response.size();
        for(JsonNode jn : response)
        {
            String uriTaxonUnderLimit =  jn.get("uri").get("value").textValue();
            String retCurrent  = uriTaxonUnderLimit+" ----> ";
            ArrayList<JsonNode> repLabels = spIn.getResponse("SELECT ?l WHERE { <"+uriTaxonUnderLimit+"> <http://ontology.irstea.fr/AgronomicTaxon#hasScientificName> ?l. FILTER (langMatches( lang(?l), 'FR') || langMatches( lang(?l), 'EN') || langMatches( lang(?l), '')). }");
            int i = 0;
            while(i < repLabels.size() && i<5)
            {
                String label = repLabels.get(i).get("l").get("value").textValue();
                retCurrent += label+", ";
                i++;
            }
            ArrayList<JsonNode> repNbUnderTaxon = spIn.getResponse("SELECT DISTINCT ?uri WHERE { ?uri <http://ontology.irstea.fr/AgronomicTaxon#hasHigherRank>+ <"+uriTaxonUnderLimit+">}");
            int nbUnderTaxon = 0;
            if(repNbUnderTaxon != null)
            {
                nbUnderTaxon = repNbUnderTaxon.size();
            }
            nbTotalUnderTaxon += nbUnderTaxon;
            retCurrent += "\n\t --->"+nbUnderTaxon+"\n";
            
            if(nbUnderTaxon >= 9) // limit min nb under taxon
                ret += retCurrent;
            
        }
        
        
        ret += " ----- TOTAL : "+nbTotalUnderTaxon+"------";
        return ret;
    }
    
    private String getADOMTtl()
    {
         String ret = "prefix : <http://www.w3.org/2002/07/owl#> \n" +
                                "prefix owl: <http://www.w3.org/2002/07/owl#> \n" +
                                "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
                                "prefix xml: <http://www.w3.org/XML/1998/namespace> \n" +
                                "prefix xsd: <http://www.w3.org/2001/XMLSchema#> \n" +
                                "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
                                "prefix skos: <http://www.w3.org/2004/02/skos/core#> \n" +
                                "prefix swrl: <http://www.w3.org/2003/11/swrl#> \n" +
                                "prefix swrlb: <http://www.w3.org/2003/11/swrlb#> \n" +
                                "prefix terms: <http://purl.org/dc/terms/> \n" +
                                "prefix AgronomicTaxon: <http://ontology.irstea.fr/AgronomicTaxon#> \n" +
                                "base <http://ontology.irstea.fr/AgronomicTaxon> \n";
        
        ret += "INSERT DATA {";
        
        try 
        {
            ret +=  IOUtils.toString( new FileInputStream(new File(this.adomFile)));
            //ret = ret.replaceAll("^@.+\\.$", "");   // remove Prefix (wrong syntax for SPARQL insert query)
        } 
        catch (IOException ex) 
        {
            System.err.println("Can't read adom file!");
            System.exit(0);
        }
        
        
        ret += "}";
        return ret;
    }
    
    private void exportSubRKB(String limitUri, SparqlProxy spOut, String extractName)
    {
        
        System.out.println("New export "+this.name+" -> "+extractName);
        
        System.out.println("Erasing previous SPout");
        spOut.clearSp();
        
        /*System.out.println("Exporting ADOM ...");
        if(!spOut.storeData(new StringBuilder(this.getADOMTtl()), false))
        {
            System.out.println("ERROR during exporting ADOM");
            System.exit(0);
        }
        System.out.println("ADOM exported!");*/
        
        // Add on spOut new classes extracted (all of them)
        ArrayList<JsonNode> newClasses = this.spIn.getResponse("SELECT ?c WHERE {?c rdfs:subClassOf <http://ontology.irstea.fr/AgronomicTaxon#Taxon>.}");
        System.out.println("Exporting new classes ...");
        for (JsonNode jClass : newClasses)
        {
            String uriClass = jClass.get("c").get("value").asText();
            if(!uriClass.startsWith("http://ontology.irstea.fr/AgronomicTaxon")) //only the new classes
            {
                String descClass =spIn.getDescribe(uriClass);
                spOut.storeData(new StringBuilder("INSERT DATA{"+descClass+"}"));
                System.out.println("ADD NEW CLASS : "+uriClass);
            }
        }
        
        // Add on spOut the limitUri description (all triples)
        System.out.println("Exporting limit uri");
        spOut.storeData(new StringBuilder("INSERT DATA { "+spIn.getDescribe(limitUri)+"}"));
        //spOut.storeData(new StringBuilder("DELETE WHERE{<"+limitUri+">  <http://ontology.irstea.fr/AgronomicTaxon#hasHigherRank> ?c}"));
        
        
        // Add on spOut the higher taxon of the limitUri (vertical extraction until top)
        ArrayList<JsonNode> verticalTaxons = this.spIn.getResponse("SELECT ?uri WHERE { <"+limitUri+"> <http://ontology.irstea.fr/AgronomicTaxon#hasHigherRank>+ ?uri}");
        System.out.println("Exporting upper taxons");
        for (JsonNode jvertTaxon : verticalTaxons)
        {
            String uriTaxon = jvertTaxon.get("uri").get("value").asText();
            spOut.storeData(new StringBuilder("INSERT DATA{"+spIn.getDescribe(uriTaxon)+"}"));
        }
        
        ArrayList<JsonNode> response = this.spIn.getResponse("SELECT ?uri WHERE { ?uri <http://ontology.irstea.fr/AgronomicTaxon#hasHigherRank>+ <"+limitUri+">}");
       // System.out.println("LIMIT Taxon : "+this.spIn.getDescribe(limitUri));
        System.out.println("Exporting all under taxons...");
        if(response != null)
        {
            System.out.println("Exporting  "+response.size()+" Taxons ("+extractName+"). All is lower rank of "+limitUri);
            // System.exit(0);
            for(JsonNode jn : response)
             {
                 String uriTaxon = jn.get("uri").get("value").textValue();
                 String describe = spIn.getDescribe(uriTaxon);
                 boolean storeData = spOut.storeData(new StringBuilder(" INSERT DATA {"+describe+"}"));
                 if(!storeData)
                 {
                     System.err.println("ERROR DURING EXPORTING!");
                     System.exit(0);
                 }
             }
             String dateFileName = new SimpleDateFormat("dd-MM_HH-mm_").format(new Date());
            spOut.writeKBFile(extractName+"_-_"+dateFileName);
             System.out.println("All taxons are exported!");
        }
        else
        {
            System.out.println("No taxons to export!  -> "+spOut.getUrlServer());
        }
        
    }
    
    public void exportAllSubRKB()
    {
        for(Entry<String, String> e : this.limitSpOut.entrySet())
        {
            SparqlProxy spOut = SparqlProxy.getSparqlProxy(e.getValue());
            String s = e.getValue();
            String extractName = s.substring(s.lastIndexOf("0/")+2, s.lastIndexOf("/"));
            this.exportSubRKB(e.getKey(), spOut, extractName);
        }
    }
    
}
