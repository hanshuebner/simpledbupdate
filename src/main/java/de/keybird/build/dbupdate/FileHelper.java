package de.keybird.build.dbupdate;

import java.io.File;
import java.util.logging.Level;

import lombok.extern.java.Log;

@Log
public class FileHelper {
    public static File checkFile(File parent, String child) throws UpdateNotPossibleException {
        File f = new File(parent, child);
        if (!f.isFile()) {
            log.log(Level.SEVERE, "File does not exist: " + f.getAbsolutePath());
            throw new UpdateNotPossibleException();
        }
        return f;
    }

    public static File checkFolder(File parent, String child) throws UpdateNotPossibleException {
        File f = new File(parent, child);
        checkFolder(f);
        return f;
    }

    public static File checkFolder(String path) throws UpdateNotPossibleException {
        File f = new File(path);
        checkFolder(f);
        return f;
    }

    public static File checkFile(String path) throws UpdateNotPossibleException {
        File f = new File(path);
        checkFile(f);
        return f;
    }

    public static File checkFile(File f) throws UpdateNotPossibleException {
        if (!f.isFile()) {
            log.log(Level.SEVERE, "File does not exist: " + f.getAbsolutePath());
            throw new UpdateNotPossibleException();
        }
        return f;
    }

    public static File checkFolder(File f) throws UpdateNotPossibleException {
        if (!f.isDirectory()) {
            log.log(Level.SEVERE, "Folder does not exist: " + f.getAbsolutePath());
            throw new UpdateNotPossibleException();
        }
        return f;
    }
}
