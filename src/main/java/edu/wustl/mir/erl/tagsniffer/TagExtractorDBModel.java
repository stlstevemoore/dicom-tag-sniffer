package edu.wustl.mir.erl.tagsniffer;

import org.dcm4che3.data.*;
import org.dcm4che3.io.DicomInputHandler;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.util.TagUtils;

import java.io.*;
import java.util.*;

public class TagExtractorDBModel extends FolderScanner {
    private int fileCount = 0;          // Number of files we have visited


    TagExtractorDBModel(String folder, String reportPath, String scanDefinitionsPath) throws Exception {
        super(folder);
    }

    public void processFile(File f) throws Exception {
        System.out.println("TagExtractor::processFile: " + "Count: " + (++fileCount) + " " + new Date().toString() + " " + f.getAbsolutePath());
        if (!f.getName().endsWith(".dcm")) {
            System.out.println("Skipping file: " + f.getAbsoluteFile().toString());
            return;
        }
        try {
            DicomInputStream dis = new DicomInputStream(f);
            Attributes attrs = dis.readDataset(-1, -1);
            int[] tags = attrs.tags();
            for (int tag : tags) {
                String tagHex = TagUtils.toHexString(tag);
                VR vr = attrs.getVR(tag);
                String s = "...";
                if (vr != VR.SQ && vr != VR.OW && vr != VR.OB) {
                    s = attrs.getString(tag);
                }
                System.out.println(tagHex + " " + vr + " " + s);
            }
        } catch (EOFException e) {
            System.out.println("Aborted Parse / java.io.EOFException: " + f.getAbsolutePath().toString());
            System.out.println("DICOM parser thinks there are more bytes in the file. File is either truncated or improperly encoded");
        } catch (Exception e) {
            System.out.println("Aborted Parse / unknown exception: " + f.getAbsolutePath().toString());
            e.printStackTrace();
        }
    }
}