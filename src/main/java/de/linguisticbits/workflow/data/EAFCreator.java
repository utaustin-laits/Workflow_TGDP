/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.linguisticbits.workflow.data;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.exmaralda.common.jdomutilities.IOUtilities;
import org.exmaralda.partitureditor.jexmaralda.convert.StylesheetFactory;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.xml.sax.SAXException;

/**
 *
 * @author bernd
 */
public class EAFCreator {
    
    String[] speakerNumbers;
    String[] interviewerNumbers;
    String audioPath;
    File file;

    public EAFCreator(String[] speakerNumbers, String[] interviewerNumbers, String audioPath, File file) {
        this.speakerNumbers = speakerNumbers;
        this.interviewerNumbers = interviewerNumbers;
        this.audioPath = audioPath;
        this.file = file;
    }
    

    public EAFCreator(String[] speakerNumbers, String[] interviewerNumbers, String sectionNumber) {
        this.speakerNumbers = speakerNumbers;
        this.interviewerNumbers = interviewerNumbers;
        String name = interviewerNumbers[0] + "-" + speakerNumbers[0] + "-1-" + sectionNumber + "-a";
        this.audioPath = name + ".wav";
    }

    public Document createEAF() throws JDOMException, IOException, SAXException, ParserConfigurationException, TransformerException{
        /*
            <xsl:param name="SPEAKER_NUMBERS">1,2</xsl:param>
            <xsl:param name="INTERVIEWER_NUMBERS">3,4</xsl:param>
            <xsl:param name="AUDIO_PATH">file:///D:/tgdp_20230210/prod/sound_files/1-1-1/1-1-1-3-a.wav</xsl:param>
            <xsl:param name="RELATIVE_AUDIO_PATH">../AUDIO/1-1-1-3-a.wav</xsl:param>        
        */
        
        File audioFile = new File(audioPath);
        String audioURL = audioFile.toURI().toString();        
        String relativeAudioURL = audioURL;
        if (file!=null){
            relativeAudioURL = file.toPath().relativize(audioFile.toPath()).toString();
        }
        
        
        String[][] parameters = {
            {"SPEAKER_NUMBERS", String.join(",", speakerNumbers)},
            {"INTERVIEWER_NUMBERS", String.join(",", interviewerNumbers)},
            {"AUDIO_PATH", audioURL},
            {"RELATIVE_AUDIO_PATH", relativeAudioURL},
        };

        Document skeleton = new IOUtilities().readDocumentFromResource("/de/linguisticbits/workflow/data/EAFSkeleton.eaf");        
        StylesheetFactory sf = new StylesheetFactory(true);
        String docString = sf.applyInternalStylesheetToString("/de/linguisticbits/workflow/data/Skeleton2EAF.xsl", IOUtilities.documentToString(skeleton), parameters);
        Document document = IOUtilities.readDocumentFromString(docString);
        return document;
    }
    
    
    
}
