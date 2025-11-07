/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.linguisticbits.workflow.normalizer;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.exmaralda.orthonormal.lexicon.AbstractSimpleWordlist;
import org.exmaralda.orthonormal.lexicon.CocaWordlist;
import org.exmaralda.orthonormal.lexicon.DerewoWordlist;
import org.exmaralda.orthonormal.lexicon.FormLanguagePair;
import org.exmaralda.orthonormal.lexicon.LexiconException;
import org.exmaralda.orthonormal.lexicon.XMLLexicon;

/**
 *
 * @author bernd
 */
public class Normalizer {
    
    XMLLexicon TGDP_Lexicon;
    XMLLexicon FOLK_Lexicon;
    AbstractSimpleWordlist derewoWordList;
    AbstractSimpleWordlist cocaWordList;
    
    Map<String,Integer> oovWords;
    
    
    // this is the full TGDP normalisation lexicon
    // it has all the normalisations from TGSC plus a (long) list of OOV forms manually edited by Margo
    //static final String TGDP_LEXICON_PATH = "C:\\linguisticbits_nb\\src\\de\\linguisticbits\\austin\\tgdp\\march_2023\\normalizer\\TGDP_Normalisation_Lexicon.xml";
    static final String INTERNAL_TGDP_LEXICON_PATH = "/de/linguisticbits/workflow/normalizer/TGDP_Normalisation_Lexicon.xml";
    
    
    public Normalizer(Map<String,Integer> oovWords){
        try {
            this.oovWords = oovWords;
            TGDP_Lexicon = new XMLLexicon();
            //TGDP_Lexicon.read(new File(TGDP_LEXICON_PATH));            
            TGDP_Lexicon.read(INTERNAL_TGDP_LEXICON_PATH);            
            
            
            FOLK_Lexicon = new XMLLexicon();
            FOLK_Lexicon.read(null);
            
            derewoWordList = new DerewoWordlist();
            cocaWordList = new CocaWordlist();
        } catch (IOException ex) {
            Logger.getLogger(Normalizer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void main(String[] args){
        try {
            Normalizer n = new Normalizer(new HashMap<>());
            System.out.println(String.join(" ", n.lookupForm("dogs")));
        } catch (LexiconException ex) {
            Logger.getLogger(Normalizer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public String[] lookupForm(String form) throws LexiconException{
        String[] result = new String[2];
        
            String token = form;
            // if the form starts with two or more capitals
            // HOUSE or GArden
            if (form.matches("^[A-ZÄÖÜ]{2}.*")){
                // the same form with just an initial capital
                // House or Garden
                String capitalWord = form.substring(0,1) + form.substring(1).toLowerCase();
                if (FOLK_Lexicon.isCapitalOnly(capitalWord)){
                    // if it is in the list of German capital only words 
                    // lookup the token with initial capital
                    token = capitalWord;
                } else {
                    // else lookup the token in all lower case
                    token = form.toLowerCase();
                }                
            }
            
            //***********************************
            // Now we now which form to lookup in the different lexicons and wordlists
            
            // start with the TGDP lexicon
            List<FormLanguagePair> tgdpCandidateForms = TGDP_Lexicon.getCandidateFormsWithLanguages(token);
            if (!tgdpCandidateForms.isEmpty()){
                FormLanguagePair bestForm = tgdpCandidateForms.get(0);
                //normEvent.setDescription(bestForm);
                result[0] = bestForm.form.trim();
                result[1] = bestForm.language;
            } else {
                List<String> folkCandidateForms = FOLK_Lexicon.getCandidateForms(token, false);
                if (!folkCandidateForms.isEmpty()){
                    String bestForm = folkCandidateForms.get(0);
                    result[0] = bestForm.trim();
                    result[1] = "deu";
                    //System.out.println("Found it in FOLK");
                } else {
                    String capitalWord = token.substring(0,1).toUpperCase() + token.substring(1);
                    if (FOLK_Lexicon.isCapitalOnly(capitalWord)){
                        result[0] = capitalWord.trim();
                        result[1] = "deu";
                    } else {
                        if (derewoWordList.wordExists(token)){
                            result[0] = token.trim();
                            result[1] = "deu";
                        } else {
                            if (cocaWordList.wordExists(token)){
                                result[0] = token.trim();
                                result[1] = "eng";                                
                            } else if (cocaWordList.wordExists(token.toLowerCase())){
                                result[0] = token.trim().toLowerCase();
                                result[1] = "eng";                                                                
                            } else {
                                // This is the case where we did not find the token 
                                // in any of the available resources
                                // set the language to deu in this case
                                // because it is by far the most likely option
                                result[0] = token.trim();
                                result[1] = "deu";
                                
                                if (!oovWords.containsKey(token)){
                                    oovWords.put(token, 0);
                                }
                                oovWords.put(token, oovWords.get(token)+1);
                                //System.out.println("YIP");

                            }                                   
                        }
                    }                                                        
                }
            }
        
        return result;
    }
    
    
}
