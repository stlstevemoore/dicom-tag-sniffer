package edu.wustl.mir.erl.tagsniffer;

public class TagsnifferCommandLine {
    public static void main(String[] args) {
        try {
            TagExtractor folderScanner = new TagExtractor(args[0], args[1], args[2]);
            TagReporter reporter = new TagReporter(folderScanner, args[1]);
            folderScanner.scan();
            reporter.report();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println("Complete run");

    }
    public static void usage(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: <input folder> <output folder> [scan_rules.xml]");
            System.exit(1);
        }
    }
}

