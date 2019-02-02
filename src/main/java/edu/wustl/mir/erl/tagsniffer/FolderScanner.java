package edu.wustl.mir.erl.tagsniffer;
import java.io.File;


public class FolderScanner {
    private String folder = null;
    public FolderScanner(String folder) {
        this.folder=folder;
    }

    public void scan() throws Exception {
        this.enterFolder(new File(folder));

    }


    public void enterFolder(File f) throws Exception {
        System.out.println("FolderScanner::enterFolder: " + f.getAbsolutePath());
        if (! f.exists()) {
            throw new Exception("Path <" + f.getAbsolutePath() + "> does not point to a folder or a file that exists");
        }
        if (! f.isDirectory()) {
            throw new Exception ("Path <" + f.getAbsolutePath() + "> exists but does not point to a folder/directory");
        }

        String[] entries = f.list();
        for (int i = 0; i < entries.length; i++) {
            String pathEntry = f.getAbsolutePath() + "/" + entries[i];
            File fileEntry = new File(pathEntry);
            if (fileEntry.isFile()) {
                this.processFile(fileEntry);
            } else if (fileEntry.isDirectory()) {
                this.enterFolder(fileEntry);
            } else {
                throw new Exception("Path <" + pathEntry + "> does not resolve to a folder or a file. We are aborting.");
            }
        }

    }

    public void processFile(File f) throws Exception {
        System.out.println("FolderScanner::processFile: " + f.getAbsolutePath());
    }

}
