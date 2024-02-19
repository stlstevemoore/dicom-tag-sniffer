package edu.wustl.mir.erl.tagsniffer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import java.io.File;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.io.FileNotFoundException;

import org.dcm4che3.util.TagUtils;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.VR;

public class TagReporter {
    private TagExtractor tagExtractor = null;
    private String outputFolder = null;
    TagReporter(TagExtractor tagExtractor, String outputFolder) throws Exception {
        this.tagExtractor = tagExtractor;
        this.outputFolder = outputFolder;

        File f = new File(outputFolder);
        if (f.isFile()) {
            throw new Exception("String for output folder points to an existing file. It should point to an existing folder or a path that could be a folder: " + outputFolder);
        }
    }

    void report() throws Exception {
        createOutputFolder();
        reportSOPClasses();
        reportFilePaths();
        reportStudies();
        reportPrivateElementCounts();

        reportStandard();
        reportStandardSequences();
        reportDatesAndTimes();

        reportPrivate();
        reportPrivateSequences();
        reportPrivateCreators();
//        reportPrivateElementSet();
    }

    private void createOutputFolder() throws Exception {
        File f = new File(outputFolder);

        if (! f.isDirectory()) {
            if (!f.mkdirs()) {
                throw new Exception("Unable to create folder: " + outputFolder);
            }
        }

        f = new File(outputFolder + "/private");
        if (! f.isDirectory()) {
            if (!f.mkdirs()) {
                throw new Exception("Unable to create folder: " + outputFolder + "/private");
            }
        }


        f = new File(outputFolder + "/standard");
        if (! f.isDirectory()) {
            if (!f.mkdirs()) {
                throw new Exception("Unable to create folder: " + outputFolder + "/standard");
            }
        }
    }

    private void reportStandard() throws Exception {
        PrintWriter out = null;
        try {
            out = new PrintWriter(new OutputStreamWriter(
                    new BufferedOutputStream(new FileOutputStream(outputFolder + "/standard_elements.txt")), "UTF-8"));
            out.println("Standard Elements");
            writeElementTags(out, tagExtractor.getPublicElementValues());
            writeTreeMap(out,     tagExtractor.getPublicElementValues());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if(out != null) {
                out.flush();
                out.close();
            }
        }

        // Now write separate files for each of the standard elements
        TreeMap<Integer, TreeSet<String>> standardElementMap = tagExtractor.getPublicElementValues();
        for (Integer i : standardElementMap.keySet()) {
            try {
                out = new PrintWriter(new OutputStreamWriter(
                        new BufferedOutputStream(new FileOutputStream(outputFolder + "/standard/" + TagUtils.toHexString(i) + ".txt")), "UTF-8"));
                out.println("Standard Element: " + TagUtils.toHexString(i));
                writeTreeMap(out, standardElementMap, i);
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
    }

    private void reportStandardSequences() throws Exception {
        PrintWriter out = null;
        try {
            out = new PrintWriter(new OutputStreamWriter(
                    new BufferedOutputStream(new FileOutputStream(outputFolder + "/standard_sequences.txt")), "UTF-8"));
            out.println("Standard Sequence Elements");
            writeSequenceMap(out, tagExtractor.getPublicSequenceValues());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if(out != null) {
                out.flush();
                out.close();
            }
        }
    }

    private void reportPrivate() throws Exception {
        PrintWriter out = null;
        try {
            out = new PrintWriter(new OutputStreamWriter(
                    new BufferedOutputStream(new FileOutputStream(outputFolder + "/private_elements.txt")), "UTF-8"));
            out.println("Private Elements");
            writeElementKeys(out, tagExtractor.getPrivateElementValues());
            writeTreeMapStringKeys(out, tagExtractor.getPrivateElementValues());
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

        // Now write separate files for each of the private elements
        TreeMap<String, TreeSet<String>> privateElementMap = tagExtractor.getPrivateElementValues();
        for (String key : privateElementMap.keySet()) {
            try {
                String fileBase = key.replaceAll(" ", "_");
                out = new PrintWriter(new OutputStreamWriter(
                        new BufferedOutputStream(new FileOutputStream(outputFolder + "/private/" + fileBase + ".txt")), "UTF-8"));
                out.println("Private Element: " + key);
                writeTreeMap(out, privateElementMap, key);
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
    }

    private void reportPrivateSequences() throws Exception {
        PrintWriter out = null;
        try {
            out = new PrintWriter(new OutputStreamWriter(
                    new BufferedOutputStream(new FileOutputStream(outputFolder + "/private_sequences.txt")), "UTF-8"));
            out.println("Private Sequence Elements");
            writeSequenceMap(out, tagExtractor.getPrivateSequenceValues());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if(out != null) {
                out.flush();
                out.close();
            }
        }
    }


    private void reportSOPClasses() throws Exception {
        PrintWriter out = null;
        try {
            out = new PrintWriter(new OutputStreamWriter(
                    new BufferedOutputStream(new FileOutputStream(outputFolder + "/sop_classes.txt")), "UTF-8"));
            out.println("SOP Classes");
            writeSet(out, tagExtractor.getSopClassSet());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if(out != null) {
                out.flush();
                out.close();
            }
        }
    }

    private void reportFilePaths() throws Exception {
        PrintWriter out = null;
        try {
            out = new PrintWriter(new OutputStreamWriter(
                    new BufferedOutputStream(new FileOutputStream(outputFolder + "/paths.txt")), "UTF-8"));
            out.println("File paths with SOP Classes");
            writeSet(out, tagExtractor.getFilePathSOPClass());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if(out != null) {
                out.flush();
                out.close();
            }
        }
    }


    private void reportStudies() throws Exception {
        PrintWriter out = null;
        try {
            out = new PrintWriter(new OutputStreamWriter(
                    new BufferedOutputStream(new FileOutputStream(outputFolder + "/dicom_studies.txt")), "UTF-8"));
            out.println("DICOM Studies");
            writeSet(out, tagExtractor.getStudyUIDSet());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if(out != null) {
                out.flush();
                out.close();
            }
        }
    }


    private void reportPrivateElementCounts() throws Exception {
        PrintWriter out = null;
        try {
            out = new PrintWriter(new OutputStreamWriter(
                    new BufferedOutputStream(new FileOutputStream(outputFolder + "/counts.txt")), "UTF-8"));

            TreeMap<String, Integer> filesInStudies = tagExtractor.getFilesInStudies();
            TreeMap<String, Integer> e1 = tagExtractor.getPrivateElements_1();
            TreeMap<String, Integer> e2 = tagExtractor.getPrivateElements_2();
            TreeMap<String, Integer> e3 = tagExtractor.getPrivateElements_3();

            out.println("Private Element Counts");
            Set<String> studyUIDs = tagExtractor.getStudyUIDSet();
            for (String uid : studyUIDs) {
                Integer fileCount = filesInStudies.get(uid);
                Integer count1 = e1.get(uid);
                Integer count2 = e2.get(uid);
                Integer count3 = e3.get(uid);
                out.println(uid + " Files/1000/20000/50000 " +fileCount + " " + count1 + " " + count2 + " " + count3);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if(out != null) {
                out.flush();
                out.close();
            }
        }

        try {
            out = new PrintWriter(new OutputStreamWriter(
                    new BufferedOutputStream(new FileOutputStream(outputFolder + "/large_private_elements.txt")), "UTF-8"));

            TreeMap<String, Integer> largePrivateElements = tagExtractor.getLargePrivateElements();

            out.println("Large Private Element Counts");
            for (String hash : largePrivateElements.keySet()) {
                Integer hashCount = largePrivateElements.get(hash);
                out.println("Hash: " + hash + ", count: " + hashCount);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if(out != null) {
                out.flush();
                out.close();
            }
        }

    }


    private void reportDatesAndTimes() throws Exception {
        PrintWriter out = null;
        try {
            out = new PrintWriter(new OutputStreamWriter(
                    new BufferedOutputStream(new FileOutputStream(outputFolder + "/date_time_elements.txt")), "UTF-8"));
            out.println("Date/Time Elements");
            writeTreeMap(out, tagExtractor.getDateValues());
            writeTreeMap(out, tagExtractor.getTimeValues());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if(out != null) {
                out.flush();
                out.close();
            }
        }
    }


    private void writeElementTags(PrintWriter out, TreeMap<Integer, TreeSet<String>> hashMap) throws Exception {
        Iterator<Integer> it = hashMap.keySet().iterator();
        out.println("List of Standard Elements");
        while (it.hasNext()) {
            Integer i = it.next();
            out.println(TagUtils.toString(i) + " " +
                    ElementDictionary.vrOf(i, null).name() + " " +       // vr.name
                    ElementDictionary.keywordOf(i, null));               // element name
        }
    }

    private void writeElementKeys(PrintWriter out, TreeMap<String, TreeSet<String>> hashMap) throws Exception {
        Iterator<String> it = hashMap.keySet().iterator();
        out.println("List of Element Keys");
        while (it.hasNext()) {
            String key = it.next();
            out.println(key);

        }
    }

    private void writeTreeMap(PrintWriter out, TreeMap<Integer, TreeSet<String>> hashMap) throws Exception {
        for (Integer i : hashMap.keySet()) {
            //        String elementName = ElementDictionary.keywordOf(i.intValue(), null);
            //        VR vr = ElementDictionary.vrOf(i.intValue(), null);
            out.println("\n" + TagUtils.toString(i) + " " +
                    ElementDictionary.vrOf(i, null).name() + " " +       // vr.name
                    ElementDictionary.keywordOf(i, null));               // element name
            TreeSet valueSet = hashMap.get(i);
            for (String aValueSet : (Iterable<String>) valueSet) {
                out.println("  " + aValueSet);
            }
        }
    }

    private void writeTreeMapStringKeys(PrintWriter out, TreeMap<String, TreeSet<String>> hashMap) throws Exception {
        for (String key : hashMap.keySet()) {
            out.println("\n" + key);               // element name
            TreeSet valueSet = hashMap.get(key);
            for (String aValueSet : (Iterable<String>) valueSet) {
                out.println("  " + aValueSet);
            }
        }
    }


    private void writeTreeMap(PrintWriter out, TreeMap<Integer, TreeSet<String>> hashMap, Integer elementTag) throws Exception {

        //        String elementName = ElementDictionary.keywordOf(i.intValue(), null);
        //        VR vr = ElementDictionary.vrOf(i.intValue(), null);
        out.println("\n" + TagUtils.toString(elementTag) + " " +
                ElementDictionary.vrOf(elementTag, null).name() + " " +       // vr.name
                ElementDictionary.keywordOf(elementTag, null));               // element name
        TreeSet valueSet = hashMap.get(elementTag);
        for (String aValueSet : (Iterable<String>) valueSet) {
            out.println("  " + aValueSet);
        }
    }

    private void writeTreeMap(PrintWriter out, TreeMap<String, TreeSet<String>> hashMap, String key) throws Exception {
        out.println("\n" + key);               // element name
        TreeSet valueSet = hashMap.get(key);
        for (String aValueSet : (Iterable<String>) valueSet) {
            out.println("  " + aValueSet);
        }
    }

    void writeTreeStringMap(PrintWriter out, TreeMap<String, TreeSet<String>> hashMap) throws Exception {
        for (String i : hashMap.keySet()) {
            //String elementName = ElementDictionary.keywordOf(i.intValue(), null);
            //VR vr = ElementDictionary.vrOf(i.intValue(), null);
            out.println("\n" + i);
            TreeSet valueSet = hashMap.get(i);
            for (String aValueSet : (Iterable<String>) valueSet) {
                out.println("  " + aValueSet);
            }
        }
    }

    /**
     * writeSequenceMap assumes that the keys in the TreeMap input are formatted strings of hex encoded DICOM tags.
     * @param out
     * @param hashMap
     * @throws Exception
     */
    private void writeSequenceMap(PrintWriter out, TreeMap<String, TreeSet<String>> hashMap) throws Exception {
        for (String key : hashMap.keySet()) {
            //String elementName = ElementDictionary.keywordOf(i.intValue(), null);
            //VR vr = ElementDictionary.vrOf(i.intValue(), null);
            out.print("\n");
            String[] tagStrings = key.split(" ");
            writeFormattedTagValues(out, tagStrings);
            for (String tagString : tagStrings) {
                out.print(" (" + ElementDictionary.keywordOf(TagUtils.intFromHexString(tagString), null) + ")");
            }
            out.println();

            TreeSet valueSet = hashMap.get(key);
            for (String aValueSet : (Iterable<String>) valueSet) {
                out.println("  " + aValueSet);
            }
        }
    }

    private void writeFormattedTagValues(PrintWriter out, String[] tagStrings) throws Exception {
        for (int i = 0; i < tagStrings.length; i++) {
            out.print(TagUtils.toString(TagUtils.intFromHexString(tagStrings[i])) + " ");
        }
    }

    private void writeSet(PrintWriter out, Set<String> set) throws Exception {
        for (String aSet : set) {
            out.println(aSet);
        }
    }

    public void reportPrivateCreators() throws Exception {
        PrintWriter out = null;
        try {
            out = new PrintWriter(new OutputStreamWriter(
                    new BufferedOutputStream(new FileOutputStream(outputFolder + "/private_creators.txt")), "UTF-8"));
            out.println("\n\nPrivate Creators");

            TreeSet<String> privateCreatorIds = tagExtractor.getPrivateCreatorIds();
            for (String privateCreatorId : privateCreatorIds) {
                out.println(privateCreatorId);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if(out != null) {
                out.flush();
                out.close();
            }
        }
    }

/*
    public void reportPrivateElementSet() throws Exception {
        PrintWriter out = null;
        try {
            out = new PrintWriter(new OutputStreamWriter(
                    new BufferedOutputStream(new FileOutputStream(outputFolder + "/private_element_set.txt")), "UTF-8"));
            out.println("\n\nPrivate Element Set");

            TreeSet<String> privateElementSet = tagExtractor.getPrivateElementSet();
            Iterator<String> it = privateElementSet.iterator();
            while (it.hasNext()) {
                out.println(it.next());
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if(out != null) {
                out.flush();
                out.close();
            }
        }
    }*/
}
