package de.keybird.build;

import java.io.File;
import java.util.Properties;
import java.util.SortedSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import de.keybird.build.dbupdate.ConfigReader;
import de.keybird.build.dbupdate.DBHelfer;
import de.keybird.build.dbupdate.DBSkript;
import de.keybird.build.dbupdate.DBSkriptReader;
import de.keybird.build.dbupdate.UpdateNotPossibleException;

public class DBUpdate {

    private static final Logger LOG = Logger.getLogger(DBUpdate.class.getName());

    public static final String SYSTEM_PARAM_PROJECT_ROOT = "PROJECT_ROOT";
    public static final String PFAD_ZU_UPDATESKRIPTEN = "output/customer/support/data/";
    public static final String PFAD_ZU_KONFIGSKRIPT = "output/config/config.inc.php";
    private final File skriptVerzeichnis;
    private final File configFile;

    public DBUpdate(String srmRoot) throws UpdateNotPossibleException {
        LOG.log(Level.INFO, "DBUpdate mit Projektverzeichnis: " + srmRoot);
        File srmRootFile = new File(srmRoot);
        File f = new File(srmRootFile, PFAD_ZU_UPDATESKRIPTEN);
        if (!f.isDirectory()) {
            LOG.log(Level.SEVERE, "DB-Skript Verzeichnis existiert nicht: " + f.getAbsolutePath());
            throw new UpdateNotPossibleException();
        }
        skriptVerzeichnis = f;

        f = new File(srmRootFile, PFAD_ZU_KONFIGSKRIPT);
        if (!f.isFile()) {
            LOG.log(Level.SEVERE, "config.inc.php existiert nicht: " + f.getAbsolutePath());
            throw new UpdateNotPossibleException();
        }
        configFile = f;
    }

    public void execute() throws UpdateNotPossibleException {

        ConfigReader config = new ConfigReader();
        Properties props = config.parse(configFile);
        DBHelfer db = new DBHelfer(props);
        DBSkriptReader skriptReader = new DBSkriptReader();
        SortedSet<DBSkript> alleSkripte = skriptReader.getAlleDBSkripte(skriptVerzeichnis);
        int skriptVersion = (alleSkripte.size() == 0) ? 0 : alleSkripte.last().getVersion();
        int dbVersion = db.getAktuelleVersion();
        DBSkript skript = skriptReader.getNextDBSkript(dbVersion, alleSkripte);

        // Alle Updates abarbeiten
        while (skript != null) {
            db.update(skript);
            dbVersion = db.getAktuelleVersion();
            skript = skriptReader.getNextDBSkript(dbVersion, alleSkripte);
        }
        db.finish();

        if (skript == null) {
            LOG.log(Level.INFO, "Datenbank aktuell. Aktuelle DB Version= " + dbVersion + ", aktuelle Skriptversion="
                    + skriptVersion);
        }
    }

    public static void main(String[] args) {
        String projektverzeichnis = System.getProperty(SYSTEM_PARAM_PROJECT_ROOT);
        if (StringUtils.isBlank(projektverzeichnis)) {
            LOG.log(Level.SEVERE, "Kein Verzeichnis fuer SRM-System angegeben! Breche ab.");
            System.exit(2);
        }
        try {
            DBUpdate dbupdate = new DBUpdate(projektverzeichnis);
            dbupdate.execute();
        } catch (UpdateNotPossibleException e) {
            LOG.log(Level.SEVERE, "Update nicht moeglich.", e);
            System.exit(3);
        }
    }
}
