/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package de.linguisticbits.workflow.phonetic;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom.JDOMException;

/**
 *
 * @author bernd
 */
public class TestG2PLookup {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new TestG2PLookup().doit();
    }

    private void doit() {
        try {
            G2PLookup g2pLookup = new G2PLookup();
            String pho = g2pLookup.lookupOrth("awesome", "eng");
            System.out.println("awesome --> " + pho);
        } catch (IOException | JDOMException ex) {
            Logger.getLogger(TestG2PLookup.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
