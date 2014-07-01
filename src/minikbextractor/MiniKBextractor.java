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

/**
 *
 * @author murloc
 */
public class MiniKBextractor {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {
        
        
        ArrayList<Source> sources = new ArrayList();
        
       /* SparqlProxy spInAgrovoc = SparqlProxy.getSparqlProxy("http://amarger.murloc.fr:8080/Agrovoc2KB_OUT/");
        HashMap<String, String> limitSpOutAgrovoc = new HashMap<>();
        limitSpOutAgrovoc.put("http://aims.fao.org/aos/agrovoc/c_5608", "http://amarger.murloc.fr:8080/Agrovoc_mini_Paspalum/");
        limitSpOutAgrovoc.put("http://aims.fao.org/aos/agrovoc/c_148", "http://amarger.murloc.fr:8080/Agrovoc_mini_Aegilops/");
        limitSpOutAgrovoc.put("http://aims.fao.org/aos/agrovoc/c_5435", "http://amarger.murloc.fr:8080/Agrovoc_mini_Oryza/");
        limitSpOutAgrovoc.put("http://aims.fao.org/aos/agrovoc/c_7950", "http://amarger.murloc.fr:8080/Agrovoc_mini_Triticum/");

        String nameAgrovoc = "Agrovoc";
        
        sources.add(new Source(spInAgrovoc, nameAgrovoc, limitSpOutAgrovoc));
*/
        /*SparqlProxy spInTaxRef = SparqlProxy.getSparqlProxy("http://amarger.murloc.fr:8080/TaxRef2RKB_out_TEST/");
        HashMap<String, String> limitSpOutTaxRef = new HashMap<>();
        limitSpOutTaxRef.put("http://inpn.mnhn.fr/espece/cd_nom/195870", "http://amarger.murloc.fr:8080/TaxRef_mini_Paspalum/");
        limitSpOutTaxRef.put("http://inpn.mnhn.fr/espece/cd_nom/188834", "http://amarger.murloc.fr:8080/TaxRef_mini_Aegilops/");
        limitSpOutTaxRef.put("http://inpn.mnhn.fr/espece/cd_nom/195564", "http://amarger.murloc.fr:8080/TaxRef_mini_Oryza/");
        limitSpOutTaxRef.put("http://inpn.mnhn.fr/espece/cd_nom/198676", "http://amarger.murloc.fr:8080/TaxRef_mini_Triticum/");
        //String limitUriTaxRef = "http://inpn.mnhn.fr/espece/cd_nom/187444"; //Poaceae
        String nameTaxRef = "TaxRef   ";
        
        sources.add(new Source(spInTaxRef,nameTaxRef, limitSpOutTaxRef));*/
        
        SparqlProxy spInNCBI = SparqlProxy.getSparqlProxy("http://amarger.murloc.fr:8080/Ncbi2RKB_out/");
        HashMap<String, String> limitSpOutNCBI = new HashMap<>();
        //limitSpOutNCBI.put("http://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?id=147271", "http://amarger.murloc.fr:8080/NCBI_mini_Paspalum/");
        limitSpOutNCBI.put("http://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?id=4480", "http://amarger.murloc.fr:8080/NCBI_mini_Aegilops/");
        limitSpOutNCBI.put("http://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?id=4527", "http://amarger.murloc.fr:8080/NCBI_mini_Oryza/");
        //limitSpOutNCBI.put("http://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?id=4564", "http://amarger.murloc.fr:8080/NCBI_mini_Triticum/");
        String nameNCBI = "NCBI";
        
        sources.add(new Source(spInNCBI,nameNCBI, limitSpOutNCBI));  
        
        
       for(Source s : sources)
        {
            //System.out.println(s.getStatUnderLimit());
            s.exportAllSubRKB();
            System.out.println("------------------------------------");
        }
        System.exit(0);
        
    }
    
}
