/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.linguisticbits.workflow.data;

import de.linguisticbits.workflow.normalizer.ISOTEINormalizer;
import de.linguisticbits.workflow.phonetic.G2PLookup;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.exmaralda.common.jdomutilities.IOUtilities;
import org.exmaralda.exakt.utilities.FileIO;
import org.exmaralda.orthonormal.lexicon.LexiconException;
import org.exmaralda.partitureditor.jexmaralda.convert.StylesheetFactory;
import org.exmaralda.tagging.SextantISOTEIIntegrator;
import org.exmaralda.tagging.TreeTaggableISOTEITranscription;
import org.exmaralda.tagging.TreeTagger;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.xpath.XPath;
import org.xml.sax.SAXException;
import org.zumult.backend.Configuration;

/**
 *
 * @author bernd
 */
public class EAFAnnotator {


    File originalEAFFile;
    Document isoTeiDoc;
    
    static Map<String, String> englishMap; 
    static Map<String, String> germanMap;
    static Map<String, String> otherMap;
    
    static {
        try {
            System.out.println("Static initialisation of lexicon maps for phonetic annotation");
            File germanFile = new File(Configuration.getConfigurationVariable("phonetic-lexicon-german"));
            File englishFile = new File(Configuration.getConfigurationVariable("phonetic-lexicon-english"));
            File otherFile = new File(Configuration.getConfigurationVariable("phonetic-lexicon-other"));
            
            Document englishLexiconDoc = FileIO.readDocumentFromLocalFile(englishFile);
            Document germanLexiconDoc = FileIO.readDocumentFromLocalFile(germanFile);
            Document otherLexiconDoc = FileIO.readDocumentFromLocalFile(otherFile);
                        
            englishMap = readMap(englishLexiconDoc);
            germanMap = readMap(germanLexiconDoc);
            otherMap = readMap(otherLexiconDoc);
            System.out.println("Lexicon maps read");
        } catch (JDOMException | IOException ex) {
            Logger.getLogger(EAFAnnotator.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    private static Map<String, String> readMap(Document lexiconDoc) throws JDOMException {
        Map<String, String> map = new HashMap<>();
        List l = XPath.selectNodes(lexiconDoc, "//entry");
        for (Object o : l){
            Element w = (Element)o;
            String orth = w.getAttributeValue("orth");
            String pho = w.getAttributeValue("pho-us");
            map.put(orth, pho);
        }
        return map;
    }
    
    
    public EAFAnnotator(File originalEAFFile) {
        this.originalEAFFile = originalEAFFile;
    }
    
    
    public String getIsoTeiXML(){
        return IOUtilities.documentToString(isoTeiDoc);
    }
    
    public void convertAndAnnotate(){
        try {
            convert();
            new ISOTEINormalizer(isoTeiDoc).normalize();
            lemmatizePosDe();
            lemmatizePosEn();
            mergeLemmas();
            annotatePhonetic();
            annotateSpeechRate();
        } catch (SAXException | ParserConfigurationException | IOException | TransformerException | JDOMException | LexiconException ex) {
            Logger.getLogger(EAFAnnotator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    /**************************************************************/
    /**************************************************************/
    /**************************************************************/
    
    // first XSL stylesheet for EAF to ISO/TEI conversion (part of this package)
    String XSL1 = "/de/linguisticbits/workflow/xsl/EAF2TEI_tgdp.xsl";
    // second XSL stylesheet for EAF to ISO/TEI conversion (part of this package)
    String XSL2 = "/de/linguisticbits/workflow/xsl/EAF2TEI_tgdp2.xsl";
    // third XSL stylesheet for EAF to ISO/TEI conversion (part of this package)
    // 2024-02-20, for issue #1 (make incidents out of "(laugh)" etc.
    String XSL3 = "/de/linguisticbits/workflow/xsl/EAF2TEI_tgdp3.xsl";
    // XSL stylesheet for normalizing ISO/TEI files (part of EXMARaLDA package)
    // N.B.: This is about normalizing things in the format, NOT adding a normalization annotation layer
    String XSL4 = "/org/exmaralda/tei/xml/normalize.xsl";
    // XSL stylesheet for turning span annotations into attribute annotations (part of EXMARaLDA package)
    String XSL5 = "/org/exmaralda/tei/xml/spans2attributes.xsl";
    
    private void convert() throws SAXException, ParserConfigurationException, IOException, TransformerException, JDOMException{
        StylesheetFactory ssf = new StylesheetFactory(true);
        
        // 17-06-2024, changed for issue#34
        //String[][] params = {
        //    {"TRANSCRIPT-ID", originalEAFFile.getName().replaceAll("\\.eaf", "")}
        //};
        String[][] params = {
            {"TRANSCRIPT-ID", "TRS_" + originalEAFFile.getName().replaceAll("\\.eaf", "")}
        };
        String s1 = ssf.applyInternalStylesheetToExternalXMLFile(XSL1, originalEAFFile.getAbsolutePath(), params);
        String s2 = ssf.applyInternalStylesheetToString(XSL2, s1);
        String s3 = ssf.applyInternalStylesheetToString(XSL3, s2);
        String s4 = ssf.applyInternalStylesheetToString(XSL4, s3);
        String s5 = ssf.applyInternalStylesheetToString(XSL5, s4);

        isoTeiDoc = IOUtilities.readDocumentFromString(s5);        
    }
    
    /**************************************************************/
    /**************************************************************/
    /**************************************************************/

    private void lemmatizePosDe() throws IOException, JDOMException{
        List l = XPath.selectNodes(isoTeiDoc, "//@lemma|//@pos|//@p-pos");
        for (Object o : l){
            Attribute a = (Attribute)o;
            a.detach();
        }
        File intermediate = File.createTempFile("ISO_TEI","TMP");
        intermediate.deleteOnExit();
        FileIO.writeDocumentToLocalFile(intermediate, isoTeiDoc);

        String TTC = Configuration.getConfigurationVariable("tree-tagger-directory"); // "C:\\Users\\bernd\\Dropbox\\TreeTagger";
        String PF = Configuration.getConfigurationVariable("tree-tagger-parameter-file-german"); // "C:\\linguisticbits_nb\\src\\de\\linguisticbits\\austin\\tgdp\\march_2023\\tagger\\2021-04-16_ParameterFile_ORIGINAL_ALL_FINAL.par";
        String ENC = "UTF-8";
        String[] OPT = {"-token","-lemma","-sgml","-no-unknown"};
        String xpathToTokens = TreeTaggableISOTEITranscription.XPATH_NO_XY;

        TreeTagger tt = new TreeTagger(TTC, PF, ENC, OPT);
        tt.verbose = false;

        TreeTaggableISOTEITranscription ttont = new TreeTaggableISOTEITranscription(intermediate, true);
        ttont.setXPathToTokens(xpathToTokens);

        File output = File.createTempFile("ISO_TEI","TMP");
        System.out.println(output.getAbsolutePath() + " created.");
        output.deleteOnExit();
        tt.tag(ttont, output);
        System.out.println("Tagging done");
        SextantISOTEIIntegrator soi = new SextantISOTEIIntegrator(intermediate.getAbsolutePath());
        soi.integrate(output.getAbsolutePath());
        
        isoTeiDoc = soi.getDocument();
        
        intermediate.delete();
        
    }
    
    /**************************************************************/
    /**************************************************************/
    /**************************************************************/

    private void lemmatizePosEn() throws IOException, JDOMException{
        File intermediate = File.createTempFile("ISO_TEI","TMP");
        intermediate.deleteOnExit();
        FileIO.writeDocumentToLocalFile(intermediate, isoTeiDoc);

        String TTC = Configuration.getConfigurationVariable("tree-tagger-directory"); // "C:\\Users\\bernd\\Dropbox\\TreeTagger";
        String PF = Configuration.getConfigurationVariable("tree-tagger-parameter-file-english"); 
        String ENC = "UTF-8";
        String[] OPT = {"-token","-lemma","-sgml","-no-unknown"};
        String xpathToTokens = TreeTaggableISOTEITranscription.XPATH_NO_XY;

        TreeTagger tt = new TreeTagger(TTC, PF, ENC, OPT);
        tt.verbose = false;

        TreeTaggableISOTEITranscription ttont = new TreeTaggableISOTEITranscription(intermediate, true);
        ttont.setXPathToTokens(xpathToTokens);

        File output = File.createTempFile("ISO_TEI","TMP");
        System.out.println(output.getAbsolutePath() + " created.");
        output.deleteOnExit();
        tt.tag(ttont, output);
        System.out.println("Tagging done");
        SextantISOTEIIntegrator soi = new SextantISOTEIIntegrator(intermediate.getAbsolutePath());
        soi.addAnnotationCategoryMapping("pos", "pos-en");
        soi.addAnnotationCategoryMapping("lemma", "lemma-en");        
        soi.integrate(output.getAbsolutePath());
        
        isoTeiDoc = soi.getDocument();
        
        intermediate.delete();
        
    }

    /**************************************************************/
    /**************************************************************/
    /**************************************************************/

    private void mergeLemmas() throws JDOMException {
        XPath xp = XPath.newInstance("//tei:w");
        Namespace teiNs = Namespace.getNamespace("tei", "http://www.tei-c.org/ns/1.0");
        xp.addNamespace(teiNs);
        List l = xp.selectNodes(isoTeiDoc);

        for (Object o : l){
            Element w = (Element)o;

            // 2024-01-23, issue #1, Post processing rules
            // For TGSC, there were basically 2 post-processing rules:
            // apply tag "UI" for all tokens transcribed as "???" (9753 hits in the corpus, currently mostly tagged as NE which is plain wrong)
            // apply tag "NGHES" for all tokens which are in a dedicated list of hesitation markers. Currently, "uh" is indeed tagged as NGIRR, and there are no fewer than 36356 instances in the corpus. Conversely, nothing is tagged as NGHES
            // assuming that only "uh" is the hes marker we're interested in
            String transcribedText = w.getText();
            if (transcribedText.contains("???")){
                w.setAttribute("pos", "UI");
                w.setAttribute("lang", "xxx", Namespace.XML_NAMESPACE);
                w.setAttribute("lemma", "xxx");
                w.setAttribute("norm", "xxx");
            }

            if (transcribedText.equals("uh")){
                w.setAttribute("pos", "NGHES");
                w.setAttribute("lang", "xxx", Namespace.XML_NAMESPACE);
                w.setAttribute("lemma", "uh");
                w.setAttribute("norm", "uh");
            }



            String lang = w.getAttributeValue("lang", Namespace.XML_NAMESPACE);
            if ("eng".equals(lang)){
                String enLemma = w.getAttributeValue("lemma-en");
                if (enLemma==null){
                    w.setAttribute("lemma", w.getText());
                    System.out.println(IOUtilities.elementToString(w));
                    w.setAttribute("pos", "FM");
                } else {
                    w.setAttribute("lemma", enLemma);
                    w.setAttribute("pos", "FM");
                }
            }
        }
        
        // new 22-02-2024: post-process <pc> elements
        XPath xp2 = XPath.newInstance("//tei:pc");
        xp2.addNamespace(teiNs);
        List l2 = xp2.selectNodes(isoTeiDoc);
        for (Object o : l2){
            Element pc = (Element)o;
            pc.setAttribute("pos", "$");
            pc.setAttribute("lemma", pc.getText());
        }

        // get rid of all existing attributes for pos and lemma
        List l3 = XPath.selectNodes(isoTeiDoc, "//@lemma-en|//@pos-en|//@p-pos");
        for (Object o : l3){
            Attribute a = (Attribute)o;
            a.detach();
        }
    }
    
    /**************************************************************/
    /**************************************************************/
    /**************************************************************/
    
    private void annotatePhonetic() throws JDOMException, IOException {
        G2PLookup g2pLookup = new G2PLookup();
        XPath xp = XPath.newInstance("//tei:w");
        Namespace teiNs = Namespace.getNamespace("tei", "http://www.tei-c.org/ns/1.0");
        xp.addNamespace(teiNs);
        List l = xp.selectNodes(isoTeiDoc);

        for (Object o : l){
            Element w = (Element)o;
            String transcribedForm = w.getText();
            String lang = w.getAttributeValue("lang", Namespace.XML_NAMESPACE);
            String pho;
            if (null == lang){
                pho = otherMap.get(transcribedForm);
            } else switch (lang) {
                case "deu":
                    pho = germanMap.get(transcribedForm);
                    break;
                case "eng":
                    pho = englishMap.get(transcribedForm);
                    break;
                default:
                    pho = otherMap.get(transcribedForm);
                    break;
            }
            if (pho!=null){
                w.setAttribute("phon", pho);                                                            
            } else {
                String lookupLang = "eng-US";
                if ("deu".endsWith(lang)){
                    lookupLang = "deu";
                }                    
                // 07-11-2024: changed this because web service call fails a lot for UT Austin                
                // pho = g2pLookup.lookupOrth(transcribedForm, lookupLang);
                pho = "xxx";
                if (pho!=null){
                    w.setAttribute("phon", pho);                                                            

                    if ("deu".equals(lang)){
                        germanMap.put(transcribedForm, pho);
                    } else if ("eng".equals(lang)){
                        englishMap.put(transcribedForm, pho);
                    } else {
                        otherMap.put(transcribedForm, pho);
                    }
                    //System.out.println("==> Added " + transcribedForm + " / " + pho + " to lexicon.");
                } else {
                    w.setAttribute("phon", "xxx");                                                            
                    //System.out.println("==> Could not annotate " + transcribedForm);                        
                }
            }
        }
        
    }

    /**************************************************************/
    /**************************************************************/
    /**************************************************************/

    private void annotateSpeechRate() throws JDOMException {
        XPath xpath = XPath.newInstance("//tei:annotationBlock");
        Namespace teiNs = Namespace.getNamespace("tei", "http://www.tei-c.org/ns/1.0");
        xpath.addNamespace(teiNs);
        XPath xpath2 = XPath.newInstance("descendant::tei:w");
        xpath2.addNamespace(teiNs);
        
        List annotationBlocks = xpath.selectNodes(isoTeiDoc);

        for (Object o : annotationBlocks){
            Element ab = (Element)o;
            final DecimalFormat df = new DecimalFormat("0.00");

            String startID = ab.getAttributeValue("start");
            XPath xpath3 = XPath.newInstance("//tei:when[@xml:id='" + startID + "']");
            xpath3.addNamespace(teiNs);
            Element startWhen = (Element) xpath3.selectSingleNode(isoTeiDoc);
            double startTime = Double.parseDouble(startWhen.getAttributeValue("interval"));

            String endID = ab.getAttributeValue("end");
            XPath xpath4 = XPath.newInstance("//tei:when[@xml:id='" + endID + "']");
            xpath4.addNamespace(teiNs);
            Element endWhen = (Element) xpath4.selectSingleNode(isoTeiDoc);
            double endTime = Double.parseDouble(endWhen.getAttributeValue("interval"));

            double duration = endTime - startTime;

            List l2 = xpath2.selectNodes(ab);
            int syllableCount = 0;
            for (Object o2 : l2){
                Element wElement = (Element)o2;
                String pho = wElement.getAttributeValue("phon");
                int thisSyllableCount = pho.length() - pho.replaceAll("\\.", "").length() + 1;
                syllableCount+=thisSyllableCount;
            }

            double speechrate = syllableCount / duration;

            Element spanGrpElement = new Element("spanGrp", teiNs);
            spanGrpElement.setAttribute("type", "speech-rate");
            spanGrpElement.setAttribute("subtype", "time-based");
            Element spanElement = new Element("span", teiNs);
            spanElement.setAttribute("from", startID);
            spanElement.setAttribute("to", endID);
            spanElement.setText(df.format(speechrate));
            ab.addContent(spanGrpElement);
            spanGrpElement.addContent(spanElement);


        }
        
        
        
    }


    
}
