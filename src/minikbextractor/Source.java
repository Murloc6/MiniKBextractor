/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package minikbextractor;

import com.fasterxml.jackson.databind.JsonNode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 *
 * @author fabien.amarger
 */
public class Source
{
    private SparqlProxy spIn;
    
    private String name;
    
    private HashMap<String, String> limitSpOut;
    
    public Source(SparqlProxy spIn, String name, HashMap<String, String> limitSpOut)
    {
        this.spIn = spIn;
        this.name = name;
        this.limitSpOut = limitSpOut;
    }
    
    
    public String getStatUnderLimit(String limitUri)
    {
        String ret = "Stats of "+this.name+" with the limitUri : "+limitUri+"\n";
         ArrayList<JsonNode> response = spIn.getResponse("SELECT ?uri WHERE { ?uri <http://ontology.irstea.fr/AgronomicTaxon#hasHigherRank> <"+limitUri+">}");
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
    
    private void exportSubRKB(String limitUri, SparqlProxy spOut, String extractName)
    {
        System.out.println("Erasing previous SPout");
        spOut.clearSp();
        
        spOut.storeData(new StringBuilder("INSERT DATA { "+spIn.getDescribe(limitUri)+"}"));
        spOut.storeData(new StringBuilder("DELETE WHERE{<"+limitUri+">  <http://ontology.irstea.fr/AgronomicTaxon#hasHigherRank> ?c}"));
        
        
        ArrayList<JsonNode> response = this.spIn.getResponse("SELECT ?uri WHERE { ?uri <http://ontology.irstea.fr/AgronomicTaxon#hasHigherRank>+ <"+limitUri+">}");
       // System.out.println("LIMIT Taxon : "+this.spIn.getDescribe(limitUri));
        System.out.println("-------");
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
