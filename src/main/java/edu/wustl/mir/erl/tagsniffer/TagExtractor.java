package edu.wustl.mir.erl.tagsniffer;

import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.ArrayList;
import java.security.MessageDigest;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomInputHandler;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.Fragments;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.util.TagUtils;

public class TagExtractor extends FolderScanner implements DicomInputHandler {
    private String reportPath;
    private String currentFilePath;
    private int fileCount = 0;
    private TreeMap<Integer, TreeSet<String>> publicElementValues  = new TreeMap<Integer, TreeSet<String>>();
    private TreeMap<String,  TreeSet<String>> privateElementValues = new TreeMap<String, TreeSet<String>>();
    private TreeMap<String,  TreeSet<String>> publicSequenceValues = new TreeMap<String, TreeSet<String>>();
    private TreeMap<String,  TreeSet<String>> privateSequenceValues = new TreeMap<String, TreeSet<String>>();
    private TreeMap<Integer, TreeSet<String>> dateValues           = new TreeMap<Integer, TreeSet<String>>();
    private TreeMap<Integer, TreeSet<String>> timeValues           = new TreeMap<Integer, TreeSet<String>>();
    private TreeSet<String> privateCreatorIds                      = new TreeSet<String>();
    private TreeMap<String, String> privateCreatorIndexValues      = new TreeMap<String, String>();

    private TreeSet<String> filePathSOPClass                       = new TreeSet<String>();

    // These next two HashMaps are created on a per file basis.
    private HashMap<Integer, String> tagPrivateCreatorMap;
    private HashMap<Integer, Integer> maskTagMap;

    private TreeSet<String>  privateElementSet;
    //private TreeSet<Integer> privateElementTags = null;
    private TreeSet<String>  sopClassSet;
    private TreeSet<String>  studyUIDSet;

    private HashMap<Integer, String> truncatedStandardElements;

    private TreeMap<String, Integer> privateElements_1;
    private TreeMap<String, Integer> privateElements_2;
    private TreeMap<String, Integer> privateElements_3;
    private TreeMap<String, Integer> filesInStudies;

    // Key is the hash of the large element value, value is the number of times we see it
    private TreeMap<String, Integer> largePrivateElements = new TreeMap<String, Integer>();

    private int[] sequenceElementTags;
//    private String lastPrivateCreator;
    //private TreeMap<Integer, String> privateCreatorIDMap;

    private ArrayList<ElementInContext> elementsInContext;

    //private int counter = 0;
    private String instanceUID;
    private String seriesUID;
    private String studyUID;
    private String accessionNumber;
    private String studyId ;
    private String classUID;
    private int privateElementSize_1 = 0;
    private int privateElementSize_2 = 0;
    private int privateElementSize_3 = 0;

    private ScanDefinitions scanDefinitions = null;

    TagExtractor(String folder, String reportPath, String scanDefinitionsPath) throws Exception {
        super(folder);
        this.reportPath = reportPath;

        privateElementSet = new TreeSet<String>();
        sopClassSet = new TreeSet<String>();
        studyUIDSet = new TreeSet<String>();
        privateElements_1 = new TreeMap<String, Integer>();
        privateElements_2 = new TreeMap<String, Integer>();
        privateElements_3 = new TreeMap<String, Integer>();
        filesInStudies = new TreeMap<String, Integer>();

        privateCreatorIndexValues.put("00130000 CTP", "0013Ra");
        privateCreatorIndexValues.put("00190000 SIEMENS CT VA0  COAD", "0019Sa");
        privateCreatorIndexValues.put("00210000 SIEMENS MED", "0021Sb");
        privateCreatorIndexValues.put("00E10000 ELSCINT1", "00E1Ea");

        truncatedStandardElements = new HashMap<Integer, String>();
        truncatedStandardElements.put(Tag.InstanceNumber, "Set of Instance Numbers truncated");
        truncatedStandardElements.put(Tag.PatientPosition, "Set of Patient Position elements truncated");
        truncatedStandardElements.put(Tag.ImagePosition, "Set of Image Position elements truncated");
        truncatedStandardElements.put(Tag.ImagePositionPatient, "Set of Image Position Patient elements truncated");
        truncatedStandardElements.put(Tag.ImageLocation, "Set of Image Location elements truncated");
        truncatedStandardElements.put(Tag.ImageOrientation, "Set of Image Orientation elements truncated");
        truncatedStandardElements.put(Tag.ImageOrientationPatient, "Set of Image Orientation Patient elements truncated");
        truncatedStandardElements.put(Tag.Location, "Set of Location elements truncated");
        truncatedStandardElements.put(Tag.SliceLocation, "Set of Slice Location elements truncated");
        truncatedStandardElements.put(Tag.WindowCenter, "Set of Window Center elements truncated");
        truncatedStandardElements.put(Tag.WindowWidth, "Set of Window Width elements truncated");

        // Pixel data related
        truncatedStandardElements.put(Tag.PixelData, "Set of Pixel Data elements truncated");
        truncatedStandardElements.put(Tag.SmallestImagePixelValue, "Set of Smallest Image Pixel Value elements truncated");
        truncatedStandardElements.put(Tag.LargestImagePixelValue, "Set of Largest Image Pixel Value elements truncated");
        truncatedStandardElements.put(Tag.RedPaletteColorLookupTableDescriptor, "Set of Red Palette Color LUT Descriptor elements truncated");
        truncatedStandardElements.put(Tag.RedPaletteColorLookupTableData, "Set of Red Palette Color LUT Data elements truncated");
        truncatedStandardElements.put(Tag.GreenPaletteColorLookupTableDescriptor, "Set of Green Palette Color LUT Descriptor elements truncated");
        truncatedStandardElements.put(Tag.GreenPaletteColorLookupTableData, "Set of Green Palette Color LUT Data elements truncated");
        truncatedStandardElements.put(Tag.BluePaletteColorLookupTableDescriptor, "Set of Blue Palette Color LUT Descriptor elements truncated");
        truncatedStandardElements.put(Tag.BluePaletteColorLookupTableData, "Set of Blue Palette Color LUT Data elements truncated");

        // Truncate time elements but not date
        truncatedStandardElements.put(Tag.SeriesTime,           "Set of Series Time elements truncated");
        truncatedStandardElements.put(Tag.StudyTime,            "Set of Study Tie elements truncated");
        truncatedStandardElements.put(Tag.StudyArrivalTime,     "Set of Study Arrival Time elements truncated");
        truncatedStandardElements.put(Tag.StudyCompletionTime,  "Set of Study Completion Time elements truncated");
        truncatedStandardElements.put(Tag.InstanceCreationTime, "Set of Instance Creation Time elements truncated");
        truncatedStandardElements.put(0x00700083,               "Set of Presentation Creation Time elements truncated");
        truncatedStandardElements.put(Tag.AcquisitionTime,      "Set of Acquisition Time elements truncated");
        truncatedStandardElements.put(Tag.ContentTime,          "Set of Content Time elements truncated");
        truncatedStandardElements.put(Tag.TriggerTime,          "Set of Trigger Time elements truncated");

        truncatedStandardElements.put(Tag.TimeOfLastCalibration,         "Set of Time of Last Calibration elements truncated");
        truncatedStandardElements.put(Tag.TimeOfLastDetectorCalibration, "Set of Time of Last Detector Calibration elements truncated");

        sequenceElementTags = new int[20];
        for (int i = 0; i < 20; i++) {
            sequenceElementTags[i] = 0;
        }

        scanDefinitions = ScanDefinitionsFactory.readScanDefinitions(scanDefinitionsPath);
        //String versionDate = scanDefinitions.getVersion().getDate();
        //String versionRelease = scanDefinitions.getVersion().getRelease();
        //String x = versionRelease + " " + versionDate;
    }

    public void processFile(File f) throws Exception {
        System.out.println("TagExtractor::processFile: " + "Count: " + (++fileCount) + " " + new Date().toString() + " " + f.getAbsolutePath());
        if (!f.getName().endsWith(".dcm")) {
            System.out.println("Skipping file: " + f.getAbsoluteFile().toString());
            return;
        }
        try {
            DicomInputStream dis = new DicomInputStream(f);
            preFileProcessing(f);
            this.parse(dis);
            postFileProcessing(f.getAbsolutePath(), dis);
        } catch (java.io.EOFException e) {
            System.out.println("Aborted Parse / java.io.EOFException: " + f.getAbsolutePath().toString());
            System.out.println("DICOM parser thinks there are more bytes in the file. File is either truncated or improperly encoded");
        } catch (Exception e) {
            System.out.println("Aborted Parse / unknown exception: " + f.getAbsolutePath().toString());
            e.printStackTrace();
        }
    }

    private void postFileProcessing(String path, DicomInputStream dis) throws IOException {
        filePathSOPClass.add(path + "   @   " + classUID);
        Integer ix;

        ix = privateElements_1.get(studyUID);
        if (ix == null) {
            ix = new Integer(0);
        }
        ix += privateElementSize_1;
        privateElements_1.put(studyUID, ix);

        ix = privateElements_2.get(studyUID);
        if (ix == null) {
            ix = new Integer(0);
        }
        ix += privateElementSize_2;
        privateElements_2.put(studyUID, ix);

        ix = privateElements_3.get(studyUID);
        if (ix == null) {
            ix = new Integer(0);
        }
        ix += privateElementSize_3;
        privateElements_3.put(studyUID, ix);

        ix = filesInStudies.get(studyUID);
        if (ix == null) {
            ix = new Integer(0);
        }
        ix++;
        filesInStudies.put(studyUID, ix);

/*        for (ElementInContext e: elementsInContext) {
            System.out.println("" + TagUtils.toHexString(e.getElementTag()) +
                    String.format("%10d", e.getTagPosition()) + " " +
                    e.getElementName());
            if (e.getElementTag() != 0x7fe00010) {
                dis.setPosition(e.getTagPosition());
                dis.
                int width = 80;
                StringBuilder line = new StringBuilder(width);
//                dis.vr().prompt(b, dis.bigEndian(),
//                        dis.readItem().getSpecificCharacterSet(),
//                        width - line.length() - 1, line);
//                System.out.println("  " + line);
            }

        }*/
        String z = null;
    }

    private void preFileProcessing (File f) {
        String instanceUID = "";
        String seriesUID = "";
        String studyUID  = "";
        String accessionNumber = "";
        String studyId = "";
        String classUID = "";
        privateElementSize_1 = 0;
        privateElementSize_2 = 0;
        privateElementSize_3 = 0;
        currentFilePath = f.getAbsolutePath();

        elementsInContext = new ArrayList<ElementInContext>();
        tagPrivateCreatorMap = new HashMap<Integer, String>();
        maskTagMap = new HashMap<Integer, Integer>();
    }

    private void parse(DicomInputStream dis) throws IOException {
        dis.setDicomInputHandler(this);
        dis.readDataset(-1, -1);
    }

    //@Override
    public void startDataset(DicomInputStream dis) throws IOException {
        //System.out.println("startDataset");
        //      promptPreamble(dis.getPreamble());
    }

    //@Override
    public void endDataset(DicomInputStream dis) throws IOException {
        //System.out.println("endDataset");
    }

    //@Override
    public void readValue(DicomInputStream dis, Attributes attrs)
            throws IOException {
        if (TagUtils.isPrivateGroup(dis.tag())) {
            this.readPrivateElement(dis, attrs);
        } else {
            this.readStandardElement(dis, attrs);
        }
    }

    //@Override
    public void readValue(DicomInputStream dis, Sequence seq)
            throws IOException {
        //boolean undeflen = dis.length() == -1;

        dis.readValue(dis, seq);
        //TODO
/*        if (undeflen) {
        }*/
    }

    //@Override
    public void readValue(DicomInputStream dis, Fragments frags)
            throws IOException {
        //System.out.println("readValue/Fragments");
        dis.readValue();
    }

    private void readStandardElement(DicomInputStream dis, Attributes attrs)
            throws IOException {
        VR vr = dis.vr();
        int vallen = dis.length();
        int tag = dis.tag();
        String xy = dis.getAttributePath();
        System.out.println(xy);
        boolean undeflen = vallen == -1;
        if (vr == VR.SQ) {
            sequenceElementTags[dis.level()] = dis.tag();
        }

        ElementInContext e = new ElementInContext(dis.getTagPosition(), tag, ElementDictionary.keywordOf(tag, null));
        elementsInContext.add(e);


        if (vr == VR.SQ || undeflen) {
            dis.readValue(dis, attrs);
/*            if (undeflen) {
                ;
            }*/
            return;
        }

        byte[] b = dis.readValue();
        int width = 160;
        StringBuilder line = new StringBuilder(width);
        vr.prompt(b, dis.bigEndian(),
                attrs.getSpecificCharacterSet(),
                width - line.length() - 1, line);
        if (tag == Tag.FileMetaInformationGroupLength)
            dis.setFileMetaInformationGroupLength(b);
        else if (tag == Tag.TransferSyntaxUID
                || tag == Tag.SpecificCharacterSet
                || TagUtils.isPrivateCreator(tag))
            attrs.setBytes(tag, vr, b);

        String elementValue = line.toString();
        elementValue = applyScanningRules(elementValue, tag, vr);
        addStandardElementString(dis, elementValue);
        addStandardDateOrTimeElement(tag, elementValue);
    }

    // ToDO need a more efficient mechanism
    private String applyScanningRules(String inputString, int tag, VR vr) {
        if (scanDefinitions != null) {
            boolean ruleTriggered = false;
            String tagString = TagUtils.toHexString(tag);
            StandardElements standardElements = scanDefinitions.getRules().getStandardElements();

            // Search rules for anything that matches a specific element tag. Apply those rules first.

            for (Rule rule : standardElements.getRule()) {
                String ruleTag = rule.getTag();
                if (tagString.equals(ruleTag)) {
                    String regex = rule.getPattern();
                    if (inputString.matches(rule.getPattern())) {
                        inputString = rule.getReplace();
                        ruleTriggered = true;
                        break;
                    }
                }
            }
            if (!ruleTriggered) {
                String xy = null;
                // Search rules by VR
                for (Rule rule: standardElements.getRule()) {
                    String vrCode = rule.getVr();
                    VR x = translateVRCode(vrCode);
                    if (vr == x) {
                        if (inputString.matches(rule.getPattern())) {
                            inputString = rule.getReplace();
                            ruleTriggered = true;
                            break;
                        }
                    }
                }
            }
        }
        return inputString;
    }

    private VR translateVRCode(String code) {
        VR rtn = null;
        if (code == null) {
            ; // nothing to do
        } else if (code.equals("DA")) {
            rtn = VR.DA;
        } else if (code.equals("DT")) {
            rtn = VR.DT;
        } else if (code.equals("SH")) {
            rtn = VR.SH;
        }
        return rtn;
    }

    private void readPrivateElement(DicomInputStream dis, Attributes attrs)
            throws IOException {
        VR vr = dis.vr();
        int vallen = dis.length();
        int tag = dis.tag();

        if (vr == VR.SQ) {
            sequenceElementTags[dis.level()] = dis.tag();
            System.out.println("REGEX: Private element that is a sequence: " + TagUtils.toHexString(tag));
        }
        boolean undeflen = vallen == -1;
        if (vr == VR.SQ || undeflen) {
            dis.readValue(dis, attrs);
/*            if (undeflen) {
                ;
            }*/
            return;
        }

        String tagString = TagUtils.toHexString(tag);
        byte[] b = null; //dis.readValue();
        int width = 80;
        StringBuilder line = null;
        boolean reportPrivateElementInSeparateFile = false;
        if ((vr == VR.UN || vr==VR.OB) && vallen > 100) {
            reportPrivateElementInSeparateFile = true;
            if (vallen > 50000) {
                privateElementSize_3++;
            } else if (vallen > 20000) {
                privateElementSize_2++;
            } else if (vallen > 1000) {
                privateElementSize_1++;
            }
        } else if (vr == VR.FD) {
            b = dis.readValue();
            line = new StringBuilder(40);
            line.append("" + b.length + " bytes, FD values not reported");
        } else if (vr == VR.DS) {
            b = dis.readValue();
            line = new StringBuilder(40);
            line.append("" + b.length + " bytes, DS values not reported");
        } else if (vr == VR.UN) {
            line = new StringBuilder(width*2 + 20);
            b=dis.readValue();
            vr.prompt(b, dis.bigEndian(),
                    attrs.getSpecificCharacterSet(),
                    width - line.length() - 1, line);
            line.append(" --ASCII-- " ).append(makeBytesPrintable(b));
        } else {
            line = new StringBuilder(width);
            b=dis.readValue();
            vr.prompt(b, dis.bigEndian(),
                    attrs.getSpecificCharacterSet(),
                    width - line.length() - 1, line);
        }

        String privateCreatorId;
        String elementKeyword = "xxxxxxxx";
        if (TagUtils.isPrivateCreator(tag)) {
            privateCreatorId = line.toString().replaceAll("/", "_");
            String tmp = TagUtils.toHexString(tag);
            elementKeyword = tmp.substring(0, 4) + "__" + tmp.substring(6);
            privateCreatorIds.add(tagString + "\t" + privateCreatorId);

            int mask = this.constructPrivateCreatorMask(tag);   // This is GGGG0000 plus lower byte shifted left one byte
                                                                // If original was GGGGxxXX, result is GGGGXX00
            tmp = TagUtils.toHexString(mask);

            if (tagPrivateCreatorMap.containsKey(mask)) {
                System.out.println("ERROR: Tried to add a private element key to our map when one already exists: " + privateCreatorId + " " + tmp);
                System.out.println(" File path: " + currentFilePath);
            }
            tagPrivateCreatorMap.put(mask, privateCreatorId);
            maskTagMap.put(mask, tag);
            privateElementSet.add(tagString + "\t" + privateCreatorId);
        } else {
            int maskedTag = tag & 0xffffff00;
            privateCreatorId = tagPrivateCreatorMap.get(maskedTag);
            if (privateCreatorId == null) {
                System.out.println("ERROR: Failed to find a private creator ID for: " + TagUtils.toHexString(tag));
                System.out.println(" File: " + currentFilePath);
                privateCreatorId = "No Private Creator ID";
            }
            String tmp = TagUtils.toHexString(tag);
            elementKeyword = tmp.substring(0, 4) + "xx" + tmp.substring(6);
//            String privateCreatorKey = TagUtils.toHexString(tag & 0xffff00ff) + " " + privateCreatorId;
//            String privateCreatorVal = privateCreatorIndexValues.get(privateCreatorKey);
//            System.out.println("XXX: " + TagUtils.toHexString(tag) + " " + privateCreatorKey + " " + privateCreatorVal);
            //Integer privateGroupTag = maskTagMap.get(maskedTag);
            //String privateCreatorId = tagPrivateCreatorMap.get(privateGroupTag);
            privateElementSet.add(tagString + "\t" + privateCreatorId);
        }

        if (reportPrivateElementInSeparateFile) {
            elementKeyword = elementKeyword.substring(0,4) + "XL" + elementKeyword.substring(6);
            b = dis.readValue();
            dumpLargePrivateElement(dis, b, privateCreatorId, elementKeyword);
            addPrivateElementString(dis, vr + " XL Element logged to file", privateCreatorId, elementKeyword);
        } else {
            addPrivateElementString(dis, vr + " " + line.toString(), privateCreatorId, elementKeyword);
        }
    }

    private void dumpLargePrivateElement(DicomInputStream dis, byte[] b, String privateCreatorId, String elementKeyword) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            md.update(b);
            byte[] digest = md.digest();
            String hexDigest = TagUtils.toHexString(digest);
            System.out.println("DIGEST: " + hexDigest);
            if (largePrivateElements.containsKey(hexDigest)) {
                Integer count = largePrivateElements.get(hexDigest) + 1;
                largePrivateElements.put(hexDigest, count);
                return;
            } else {
                largePrivateElements.put(hexDigest, 1);
            }
        } catch (java.security.NoSuchAlgorithmException e) {
            // If this happens, we just continue on
        }
        VR vr = dis.vr();
        int vallen = dis.length();

        String header = String.format("%s_%s_%d: ", privateCreatorId, elementKeyword, vallen).replaceAll("\\s+", "_");
        System.out.println(header);
        StringBuilder sb = makeBytesPrintable(b);

        String fileName = privateCreatorId.replaceAll("\\s+", "_") + "_" + elementKeyword + ".txt";

        PrintWriter out = null;
        try {
            File folder = new File(reportPath + "/private_xl");
            if (!folder.exists()) {
                folder.mkdirs();
            }
            boolean appendFlag = false;
            File f = new File(reportPath + "/private_xl/" + fileName);
            if (f.exists()) {
                appendFlag = true;
            }
            out = new PrintWriter(new OutputStreamWriter(
                    new BufferedOutputStream(new FileOutputStream(f, true)), "UTF-8"));
            out.print  (header);
            out.println(sb);
            sb = null;
            System.gc();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                out.flush();
                out.close();
            }
        }
    }

    private void byteToStringBuilder(byte[] inputBytes, StringBuilder sb) {
        StringBuilder tmp = new StringBuilder(100);
        char lastChar = '.';
        int len = inputBytes.length;
        int index = 0;
        char c = '.';
        int  s = 0;
        for (byte b: inputBytes) {
            s = ((int)b) & 0xff;
            if ((s >= 32) && (s< 127)) {
                c = (char) b;
                if (lastChar == '.') {
                    sb.append(tmp);
                    tmp = new StringBuilder(100);
                }
            } else {
                c = '.';
                if (lastChar != '.') {
                    sb.append(tmp);
                    tmp = new StringBuilder(100);
                }
            }

            lastChar = c;
            tmp.append(c);
            if (index++ > 20000) {
                sb.append(tmp);
                tmp = new StringBuilder(1);
                sb.append(" Element dump truncated at 20,000 bytes");
                break;
            }
        }
        sb.append(tmp);
    }

    private StringBuilder makeBytesPrintable(byte[] inputBytes) {
        StringBuilder builder = new StringBuilder(inputBytes.length + 1);
        byte b = 0;
        int  s = 0;
        for (int ix = 0; ix < inputBytes.length; ix++) {
            s = ((int)inputBytes[ix]) & 0xff;
            if (s < 32 || s > 127) {
                builder.append(',');
             } else {
                builder.append((char)s);
            }
        }
        return builder;
    }

    private void addStandardDateOrTimeElement(int tag, String string) {
        if (VR.DT == ElementDictionary.vrOf(tag, null)) {
            addToTreeMap(dateValues, tag, string);
        } else if (VR.DA == ElementDictionary.vrOf(tag, null)) {
            addToTreeMap(dateValues, tag, string);
        } else if (VR.TM == ElementDictionary.vrOf(tag, null)) {
            addToTreeMap(timeValues, tag, string, 5);
        }
    }

    private int constructPrivateCreatorMask(int tag) {
        int group = tag & 0xffff0000;
        int lowerByte = tag & 0xff;
        return (group | (lowerByte << 8));
    }


    TreeMap<Integer, TreeSet<String>> getPublicElementValues() {
        return publicElementValues;
    }

    TreeMap<String, TreeSet<String>>  getPublicSequenceValues() {
        return publicSequenceValues;
    }

    TreeMap<String, TreeSet<String>> getPrivateElementValues() {
        return privateElementValues;
    }

    TreeMap<String, TreeSet<String>>  getPrivateSequenceValues() {
        return privateSequenceValues;
    }

    TreeMap<Integer, TreeSet<String>> getDateValues() {
        return dateValues;
    }

    TreeMap<Integer, TreeSet<String>> getTimeValues() {
        return timeValues;
    }

    TreeSet<String> getPrivateCreatorIds() {
        return privateCreatorIds;
    }

    TreeSet<String> getPrivateElementSet() {
        return privateElementSet;
    }

    TreeSet<String> getSopClassSet() {
        return sopClassSet;
    }

    TreeSet<String> getFilePathSOPClass() { return filePathSOPClass; }

    TreeSet<String> getStudyUIDSet() { return studyUIDSet; }

    TreeMap<String, Integer> getPrivateElements_1() { return privateElements_1; }
    TreeMap<String, Integer> getPrivateElements_2() { return privateElements_2; }
    TreeMap<String, Integer> getPrivateElements_3() { return privateElements_3; }
    TreeMap<String, Integer> getFilesInStudies() { return filesInStudies; }
    TreeMap<String, Integer> getLargePrivateElements() { return largePrivateElements; }

/*
    private void addStandardElementString(int key, String value) {
        try {
            addStandardElementString(new Integer(key), value);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }*/


    private void addStandardElementString(DicomInputStream dis, String value) {
        try {
            if (dis.level() == 0) {
                int tag = dis.tag();
                addStandardElementString(tag, value);
                switch (tag) {
                    case Tag.MediaStorageSOPClassUID:
                        classUID = value;
                        sopClassSet.add(value);
                        break;
                    case Tag.SOPInstanceUID:
                        instanceUID = value;
                        break;
                    case Tag.StudyInstanceUID:
                        studyUID = value;
                        studyUIDSet.add(value);
                        break;
                    case Tag.SeriesInstanceUID:
                        seriesUID = value;
                        break;
                    case Tag.AccessionNumber:
                        accessionNumber = value;
                        break;
                    case Tag.StudyID:
                        studyId = value;
                        break;
                    default:
                        break;
                }
            } else {
                addToTreeStringMap(publicSequenceValues, dis, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }



/*        if (dis.level() == 0) {
            try {
                addStandardElementString(new Integer(dis.tag()), value);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        } else {
            String sequenceTags = "";
            for (int i = 0; i < dis.level(); i++) {
                sequenceTags += TagUtils.toHexString(sequenceElementTags[i]) + " ";
            }
            sequenceTags += TagUtils.toHexString(dis.tag());
            try {
                addToTreeMap(publicSequenceValues, sequenceTags, value);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }*/
    }



    private void addPrivateElementString(DicomInputStream dis, String value, String privateCreatorId, String elementKeyword) {
        try {
            if (dis.level() == 0) {
                int maskedTag = dis.tag() & 0xffffff00;
                String key = privateCreatorId + " " + elementKeyword;
                addToTreeMap(privateElementValues, key, value);
            } else {
                addToTreeStringMap(privateSequenceValues, dis, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }


    /**
     * Stores one string value in a set of values that are associated the (DICOM Sequence) element.
     * This method is used when the data element is for a sequence. The key in the output TreeMap is a string
     * consisting of 8 character hexadecimal values for the tags in the various levels of the sequence.
     * The hexadecimal values are separated by single space characters (ggggeeee ggggeeee ggggeeee).
     * The second part of TreeMap is a TreeSet that is used to store an ordered set of values that
     * are associated with the current DICOM element.
     *
     * @param treeMap The structure used to store the output of method. See description for the usage.
     * @param dis     This is the DicomInputStream in the current state for the current element.
     * @param value   The value of the current element that will be stored in the treeMap
     */
    private void addToTreeStringMap(TreeMap<String, TreeSet<String>> treeMap, DicomInputStream dis, String value) throws Exception {
        String sequenceTags = "";
        for (int i = 0; i < dis.level(); i++) {
            sequenceTags = sequenceTags.concat(TagUtils.toHexString(sequenceElementTags[i]) + " ");
        }
        sequenceTags += TagUtils.toHexString(dis.tag());
        addToTreeMap(treeMap, sequenceTags, value);
    }

    private void addToTreeMap(TreeMap<Integer, TreeSet<String>> treeMap, Integer key, String value) {
        TreeSet<String> set;
        if (!treeMap.containsKey(key)) {
            treeMap.put(key, new TreeSet<String>());
        }
        set = treeMap.get(key);
        set.add(value);
        treeMap.put(key, set);
    }

    private void addToTreeMap(TreeMap<Integer, TreeSet<String>> treeMap, Integer key, String value, int limit) {
        TreeSet<String> set;
        if (!treeMap.containsKey(key)) {
            treeMap.put(key, new TreeSet<String>());
        }
        set = treeMap.get(key);
        if (set.size() >= limit) {
            value = truncateStandardValue(key, value);
        }

        set.add(value);
        treeMap.put(key, set);
    }


    private void addToTreeMap(TreeMap<String, TreeSet<String>> treeMap, String key, String value) {
        TreeSet<String> set;
        if (!treeMap.containsKey(key)) {
            treeMap.put(key, new TreeSet<String>());
        }
        set = treeMap.get(key);
        set.add(value);
        treeMap.put(key, set);
    }

    private void addStandardElementString(Integer key, String value) throws Exception {
        addToTreeMap(publicElementValues, key, value, 10);
    }

    private String truncateStandardValue(Integer key, String value) {
        if (ElementDictionary.vrOf(key, null) == VR.UI) {
            return "UID: Set of standard elements truncated";
        }
        if (truncatedStandardElements.containsKey(key)) {
            value = truncatedStandardElements.get(key);
        }
        return value;
    }


}
