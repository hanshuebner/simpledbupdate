package de.keybird.build;

import java.io.File;
import java.util.Properties;
import java.util.SortedSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.keybird.build.dbupdate.ConfigReader;
import de.keybird.build.dbupdate.DBHelfer;
import de.keybird.build.dbupdate.DBSkript;
import de.keybird.build.dbupdate.DBSkriptReader;
import de.keybird.build.dbupdate.UpdateNotPossibleException;

public class DBUpdate {

    private static final Logger LOG = Logger.getLogger(DBUpdate.class.getName());

    private final File skriptVerzeichnis;
    private ConfigReader configReader;

    public DBUpdate(File pathToUpdatescripts, ConfigReader configReader) throws UpdateNotPossibleException {
        LOG.log(Level.INFO, "DBUpdate from: " + pathToUpdatescripts.getAbsolutePath());
        skriptVerzeichnis = pathToUpdatescripts;
        this.configReader = configReader;
    }

    public void execute() throws UpdateNotPossibleException {

        Properties props = configReader.getProperties();
        DBHelfer db = new DBHelfer(props);
        DBSkriptReader skriptReader = new DBSkriptReader();
        SortedSet<DBSkript> alleSkripte = skriptReader.getAlleDBSkripte(skriptVerzeichnis);
        int skriptVersion = (alleSkripte.size() == 0) ? 0 : alleSkripte.last().getVersion();

        db.createVersionsTable();
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
            LOG.log(Level.INFO, "Datenbank aktuell. Aktuelle DB Version= " + dbVersion + ", aktuelle Skriptversion=" + skriptVersion);
        }
    }
}
