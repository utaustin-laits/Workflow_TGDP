/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.linguisticbits.workflow.normalizer;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.exmaralda.orthonormal.lexicon.LexiconException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.xpath.XPath;
import org.xml.sax.SAXException;

/**
 *
 * @author bernd
 */
public class ISOTEINormalizer {
    
    Document inDoc;
    
    Namespace teiNs = Namespace.getNamespace("tei", "http://www.tei-c.org/ns/1.0");

    Normalizer normalizer = null;
    
    public ISOTEINormalizer(Document inDoc) {
        this.inDoc = inDoc;
        normalizer = new Normalizer(new HashMap<>() );         
    }    
    
    
    public void normalize() throws JDOMException, IOException, SAXException, ParserConfigurationException, TransformerException, LexiconException{
        XPath xpSeg = XPath.newInstance("//tei:u/tei:seg");
        xpSeg.addNamespace(teiNs);
        List segs =  xpSeg.selectNodes(inDoc);
        for (Object o : segs){
            Element seg = (Element)o;
            
            XPath xpW = XPath.newInstance("descendant::tei:w");
            xpW.addNamespace(teiNs);
            List ws = xpW.selectNodes(seg);
            for (Object o2 : ws){
                Element w = (Element)o2;
                String transcribedForm = w.getText();
                
                String[] normalizedForms = normalizer.lookupForm(transcribedForm);                
                String norm = normalizedForms[0];
                if (norm.length()==0) {
                    norm = "&";
                }
                String lang = normalizedForms[1];
                
                w.setAttribute("norm", norm);
                w.setAttribute("lang", lang, Namespace.XML_NAMESPACE);
                
            }
            
            
            // ADDITION: Change eng - deu - eng to eng - eng - eng
            for (int i=1; i< ws.size()-1; i++){
                String previousLang = ((Element)ws.get(i-1)).getAttributeValue("lang", Namespace.XML_NAMESPACE);
                Element thisWord = (Element)ws.get(i);
                String followingLang = ((Element)ws.get(i+1)).getAttributeValue("lang", Namespace.XML_NAMESPACE);;
                
                if ("eng".equals(previousLang) && "eng".equals(followingLang)){
                    thisWord.setAttribute("lang", "eng", Namespace.XML_NAMESPACE);
                }
            }
        }
        
        
    }
    
    
}
