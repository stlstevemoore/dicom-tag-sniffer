package edu.wustl.mir.erl.tagsniffer;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

public class ScanDefinitionsFactory {
    public static ScanDefinitions readScanDefinitions(String path) throws Exception {
        ScanDefinitions scanDefinitions = null;
        if (path != null) {
            JAXBContext jc = JAXBContext.newInstance("edu.wustl.mir.erl.tagsniffer");
            Unmarshaller u = jc.createUnmarshaller();
            scanDefinitions = (ScanDefinitions) u.unmarshal(new File(path));
        }

        return scanDefinitions;
    }
}
