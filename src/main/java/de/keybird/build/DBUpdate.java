package de.keybird.build;

import java.io.File;
import java.util.Properties;
import java.util.SortedSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import de.keybird.build.dbupdate.ConfigReader;
import de.keybird.build.dbupdate.DBHelfer;
import de.keybird.build.dbupdate.DBSkript;
import de.keybird.build.dbupdate.DBSkriptReader;
import de.keybird.build.dbupdate.UpdateNotPossibleException;

public class DBUpdate {

    private static final Logger LOG = Logger.getLogger(DBUpdate.class.getName());

    private final File skriptVerzeichnis;
    private final DBHelfer db;

    public DBUpdate(File pathToUpdatescripts, ConfigReader dbConfigReader) throws UpdateNotPossibleException {
        LOG.log(Level.INFO, "DBUpdate from: " + pathToUpdatescripts.getAbsolutePath());
        skriptVerzeichnis = pathToUpdatescripts;
        Properties props = dbConfigReader.getProperties();
        db = new DBHelfer(props);
    }

    public DBUpdate(File pathToUpdatescripts, DataSource ds) throws UpdateNotPossibleException {
        LOG.log(Level.INFO, "DBUpdate from: " + pathToUpdatescripts.getAbsolutePath());
        skriptVerzeichnis = pathToUpdatescripts;
        db = new DBHelfer(ds);
    }

    public void execute() throws UpdateNotPossibleException {

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
