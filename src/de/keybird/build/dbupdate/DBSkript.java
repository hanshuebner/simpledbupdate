package de.keybird.build.dbupdate;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class DBSkript {

    private static final Logger LOG = Logger.getLogger(DBSkript.class.getName());

    private static final String PREFIX = "dbupdate_v";
    private static final String SUFFIX = ".sql";

    private File datei;
    private int version;
    private String description;
    private String inhalt;
    /** Wurde der Dateinhalt schon geparst? */
    private boolean gelesen = false;

    private int parseVersion(String name) {
        int start = PREFIX.length();
        int end = name.length() - SUFFIX.length();
        String versionS = name.substring(start, end);
        int result = Integer.parseInt(versionS);
        return result;
    }

    public DBSkript(File f) {
        datei = f;
        this.version = parseVersion(f.getName());
    }

    public String getInhalt() {
        if (!this.gelesen) {
            throw new IllegalStateException("lesen() wurde noch nicht aufgerufen");
        }
        return inhalt;
    }

    public int getVersion() {
        return version;
    }

    public File getDatei() {
        return datei;
    }

    public void lesen() throws IOException {
        this.gelesen = true;
        try {
            @SuppressWarnings("unchecked")
            List<String> zeilen = FileUtils.readLines(this.datei, "UTF-8");
            this.description = getFirstComment(zeilen);
            this.inhalt = FileUtils.readFileToString(this.datei);
        } catch (IOException e) {
            this.description = "Lesefehler " + e.getMessage();
            LOG.log(Level.WARNING, "Kann Dateiinhalt nicht lesen: " + this.datei.getAbsolutePath(), e);
            throw e;
        }
    }

    public String getBeschreibung() {

        if (!this.gelesen) {
            throw new IllegalStateException("lesen() wurde noch nicht aufgerufen");
        }
        return this.description;
    }

    private String getFirstComment(List<String> zeilen) {
        for (String zeile : zeilen) {
            zeile = zeile.trim();
            if (zeile.startsWith("--")) {
                return zeile.substring(2, zeile.length());
            }
        }

        return "[kein Kommentar vorhanden]";
    }

    public boolean equals() {
        EqualsBuilder b = new EqualsBuilder();
        b.append(version, version);
        b.append(datei, datei);
        return b.isEquals();
    }

    @Override
    public int hashCode() {
        HashCodeBuilder b = new HashCodeBuilder();
        b.append(version);
        b.append(datei);
        return b.hashCode();
    }

}
