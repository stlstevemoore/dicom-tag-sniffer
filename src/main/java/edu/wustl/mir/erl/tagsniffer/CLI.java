package edu.wustl.mir.erl.tagsniffer;

public class CLI {
    public static void main(String[] args) {
        int command = Integer.parseInt(args[0]);
        switch (command) {
            case 0:
                case_0(args);
                break;
            case 1:
                case_1(args);
                break;
            default:
                usage_bad_command(args);
                break;
        }

        System.out.println("Complete run");

    }
    public static void usage(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: <input folder> <output folder> [scan_rules.xml]");
            System.exit(1);
        }
    }

    public static void usage_bad_command(String args[]) {
        System.err.println(
                        "Did not recognize the command at first argument: " + args[0] + "\n" +
                        "Expected: \n" +
                        "     0  Collect unique values, no counts\n" +
                        "     1  Collect unique values with counts");
        System.exit(1);
    }

    public static void case_0(String[] args) {
        try {
            TagExtractor folderScanner = new TagExtractor(args[1], args[2], args[3]);
            TagReporter reporter = new TagReporter(folderScanner, args[2]);
            folderScanner.scan();
            reporter.report();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void case_1(String[] args) {
        try {
            TagExtractorDBModel folderScanner = new TagExtractorDBModel(args[1], args[2], args[3]);
            folderScanner.scan();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}

