/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.linguisticbits.workflow.phonetic;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import org.exmaralda.webservices.G2PConnector;
import org.jdom.JDOMException;

/**
 *
 * @author bernd
 */
public class G2PLookup {


    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     * @throws org.jdom.JDOMException
     */

    G2PConnector mc = new G2PConnector();

    // returns [pho-uk, pho-us]
    public String lookupOrth(String orth, String language) throws IOException, JDOMException {

        File inFile = File.createTempFile("TEMP", ".txt");
        inFile.deleteOnExit();
        Writer out = new BufferedWriter(new OutputStreamWriter(
            new FileOutputStream(inFile), "UTF-8"));
            out.write(orth);
        out.close();

        String[][] parameters = {
            {"lng",language},
            {"iform","list"},
            {"oform","tab"},
            {"outsym","x-sampa"},
            {"syl","yes"},
            {"stress","yes"}
        };
        HashMap<String, Object> otherParameters = new HashMap<>();
        for (String[] s : parameters){
            otherParameters.put(s[0], s[1]);
        }        
        String callG2P = mc.callG2P(inFile, otherParameters);
        if (callG2P.split(";").length <2){            
            return null;
        }
        String pho = callG2P.split(";")[1].replaceAll("\\u000A", "");

        return pho;
    }
    
}
