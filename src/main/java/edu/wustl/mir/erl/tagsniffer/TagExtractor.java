package edu.wustl.mir.erl.tagsniffer;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;

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
    private int fileCount = 0;
    private TreeMap<Integer, TreeSet<String>> publicElementValues = null;
    private TreeMap<Integer, TreeSet<String>> privateElementValues = null;
    private TreeMap<String,  TreeSet<String>> publicSequenceValues = null;
    private TreeMap<String,  TreeSet<String>> privateSequenceValues = null;
    private TreeMap<Integer, TreeSet<String>> dateValues = null;
    private TreeMap<Integer, TreeSet<String>> timeValues = null;
    private TreeSet<String> privateCreatorIds = null;

    // These next two HashMaps are created on a per file basis.
    private HashMap<Integer, String> tagPrivateCreatorMap = null;
    private HashMap<Integer, Integer> maskTagMap = null;

    private TreeSet<String>  privateElementSet = null;
    //private TreeSet<Integer> privateElementTags = null;
    private TreeSet<String>  sopClassSet = null;

    private HashMap<Integer, String> truncatedStandardElements = null;

    private int[] sequenceElementTags = null;
    //private String lastPrivateCreator = null;

    //private int counter = 0;

    TagExtractor(String folder) {
        super(folder);

        publicElementValues = new TreeMap<Integer, TreeSet<String>>();
        privateElementValues = new TreeMap<Integer, TreeSet<String>>();
        publicSequenceValues = new TreeMap<String, TreeSet<String>>();
        privateSequenceValues = new TreeMap<String, TreeSet<String>>();
        dateValues = new TreeMap<Integer, TreeSet<String>>();
        timeValues = new TreeMap<Integer, TreeSet<String>>();
        privateCreatorIds = new TreeSet<String>();
        privateElementSet = new TreeSet<String>();
        sopClassSet = new TreeSet<String>();

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
    }

    public void processFile(File f) throws Exception {
        System.out.println("TagExtractor::processFile: " + "Count: " + (++fileCount) + " " + new Date().toString() + " " + f.getAbsolutePath());
        if (!f.getName().endsWith(".dcm")) {
            System.out.println("Skipping file: " + f.getAbsoluteFile().toString());
            return;
        }
        DicomInputStream dis = new DicomInputStream(f);
        tagPrivateCreatorMap = new HashMap<Integer, String>();
        maskTagMap = new HashMap<Integer, Integer>();
        this.parse(dis);

//        Integer three = new Integer(3);
//        String name = f.getName();
//        addPublicString(three, name);
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
        boolean undeflen = vallen == -1;
        if (vr == VR.SQ) {
            sequenceElementTags[dis.level()] = dis.tag();
        }

        if (vr == VR.SQ || undeflen) {
            dis.readValue(dis, attrs);
/*            if (undeflen) {
                ;
            }*/
            return;
        }

        byte[] b = dis.readValue();
        int width = 80;
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
        addStandardElementString(dis, line.toString());
        addStandardDateOrTimeElement(tag, line);
        if (tag == Tag.SOPClassUID) {
            sopClassSet.add(line.toString());
        }
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
        byte[] b = dis.readValue();
        int width = 80;
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

        if (TagUtils.isPrivateCreator(tag)) {
            privateCreatorIds.add(tagString + "\t" + elementValue);
            tagPrivateCreatorMap.put(tag, elementValue);
            int mask = this.constructPrivateCreatorMask(tag);
            maskTagMap.put(mask, tag);
            privateElementSet.add(tagString + "\t" + elementValue);
            //lastPrivateCreator = elementValue;
        } else {
            int maskedTag = tag & 0xffffff00;
            Integer privateGroupTag = maskTagMap.get(maskedTag);
            String privateCreatorId = tagPrivateCreatorMap.get(privateGroupTag);
            privateElementSet.add(tagString + "\t" + privateCreatorId);
        }

        addPrivateElementString(dis, vr + " " + line.toString());
    }

    private void addStandardDateOrTimeElement(int tag, StringBuilder line) {
        if (VR.DT == ElementDictionary.vrOf(tag, null)) {
            addToTreeMap(dateValues, tag, line.toString());
        } else if (VR.DA == ElementDictionary.vrOf(tag, null)) {
            addToTreeMap(dateValues, tag, line.toString());
        } else if (VR.TM == ElementDictionary.vrOf(tag, null)) {
            addToTreeMap(timeValues, tag, line.toString(), 5);
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

    TreeMap<Integer, TreeSet<String>> getPrivateElementValues() {
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
                addStandardElementString(dis.tag(), value);
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


/*    private void addPrivateElementString(int key, String value) {
        try {
            addPrivateElementString(new Integer(key), value);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }*/

/*    private void addPrivateElementString(Integer key, String value) throws Exception {
        TreeSet<String> set = null;
        if (!privateElementValues.containsKey(key)) {
            privateElementValues.put(key, new TreeSet<String>());
        }
        set = privateElementValues.get(key);
        set.add(value);
        privateElementValues.put(key, set);
    }*/

    private void addPrivateElementString(DicomInputStream dis, String value) {
        try {
            if (dis.level() == 0) {
                addToTreeMap(privateElementValues, dis.tag(), value);
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
        addToTreeMap(publicElementValues, key, value, 5);
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
