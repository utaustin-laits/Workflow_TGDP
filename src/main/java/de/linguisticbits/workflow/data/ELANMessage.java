/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.linguisticbits.workflow.data;

import java.io.File;

/**
 *
 * @author bernd
 */
public class ELANMessage {
    
    public enum ELANMessageType {MESSAGE, WARNING, ERROR, FORMAT, OK}
    
    public ELANMessageType type;
    public String description;
    public File file;

    public ELANMessage(ELANMessageType type, String description, File file) {
        this.type = type;
        this.description = description;
        this.file = file;
    }

    public ELANMessage(ELANMessageType type, String description) {
        this.type = type;
        this.description = description;
        file = null;
    }
    
    public ELANMessage(String description) {
        this.type = ELANMessageType.MESSAGE;
        this.description = description;
        file = null;
    }
    
    public String toXML(){
        String filename = "---";
        if (file!=null && file.getName()!=null){
            filename = file.getName();
        }
        String result = "<elan-message filename=\"" + filename + "\" type=\"" + type.toString() + "\">";
        result+=description;
        result += "</elan-message>";
        return result;
    }
    
    
    
}
