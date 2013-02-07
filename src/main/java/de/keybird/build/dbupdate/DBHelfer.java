package de.keybird.build.dbupdate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;

/**
 * Executes the database scripts.
 * 
 * @author Patrick
 */
public class DBHelfer {

    private static final Logger LOG = Logger.getLogger(DBHelfer.class.getName());

    private static final String DB_SKRIPT_CREATE_TABLE_VERSION = getTableCreateSql();
    private static final String DB_SKRIPT_SELECT_TABLE_VERSION = getVersionTableSelectSql();
    private static final String DB_SKRIPT_INSERT_VERSION = getVersionInsertSql();
    private static final String DB_SKRIPT_UPDATE_VERSION = getVersionUpdateSql();

    public static final String PROP_DRIVER = "driver";
    public static final String PROP_USER = "user";
    public static final String PROP_PASSWORD = "password";
    public static final String PROP_DB_NAME = "dbname";
    public static final String PROP_DB_HOST = "dbhost";
    public static final String DRIVER = "com.mysql.jdbc.Driver";

    private Connection con;
    private Properties props;

    public DBHelfer(Properties dbProperties) throws UpdateNotPossibleException {
        this.props = dbProperties;
        init();
    }

    public DBHelfer(DataSource ds) throws UpdateNotPossibleException {
        try {
            this.con = ds.getConnection();
            con.setAutoCommit(false);
            LOG.info("Init successful.");
        } catch (SQLException e) {
            throw new UpdateNotPossibleException("Cannot get connection from datasource", e);
        }
    }

    public int getAktuelleVersion() throws UpdateNotPossibleException {
        int version = 0;
        try {
            Statement stmt = null;

            try {
                stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(DB_SKRIPT_SELECT_TABLE_VERSION);
                if (rs.next()) {
                    version = rs.getInt("version");
                }
            } finally {
                if (stmt != null) {
                    stmt.close();
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Kann Version aus Datenbank nicht auslesen.", e);
            throw new UpdateNotPossibleException();
        }

        return version;
    }

    private void init() throws UpdateNotPossibleException {
        String driver = this.props.getProperty(PROP_DRIVER);
        loadDriver(driver);
        try {

            String connectionString = getConnectionString();
            con = DriverManager.getConnection(connectionString, props);
            con.setAutoCommit(false);
            LOG.info("Init erfolgreich.");
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Problem beim Verbinden mit der DB: ", e);
            throw new UpdateNotPossibleException();
        }
    }

    String getConnectionString() {
        StringBuilder b = new StringBuilder();
        b.append("jdbc:mysql://");
        b.append(this.props.getProperty(PROP_DB_HOST));
        b.append("/");
        b.append(this.props.getProperty(PROP_DB_NAME));
        return b.toString();
    }

    public void finish() throws UpdateNotPossibleException {
        try {
            con.close();
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Problem beim Schliessen der Datenbankverbindung", e);
        }
    }

    private void commit() throws UpdateNotPossibleException {
        try {
            con.commit();
            LOG.info("Commit erfolgreich.");
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Problem beim Commit", e);
            throw new UpdateNotPossibleException();
        }
    }

    public void update(DBSkript skript) throws UpdateNotPossibleException {
        LOG.info("Starte Datenbank Update: " + skript.getVersion());
        createVersionsTable();
        boolean erfolgreich = executeDBSkript(skript);
        writeVersion(skript, erfolgreich);
        if (!erfolgreich) {
            throw new UpdateNotPossibleException();
        }
        commit();
        LOG.info("Datenbank Update: " + skript.getVersion() + " erfolgreich beendet.");
    }

    private void writeVersion(DBSkript skript, boolean erfolgreich) throws UpdateNotPossibleException {
        try {

            // zuerst ein update versuchen.
            PreparedStatement stmt = con.prepareStatement(DB_SKRIPT_UPDATE_VERSION);
            stmt.setBoolean(1, erfolgreich);
            stmt.setString(2, skript.getBeschreibung());
            stmt.setInt(3, skript.getVersion());
            stmt.executeUpdate();
            int update = stmt.getUpdateCount();
            stmt.close();

            // nicht geklappt? also insert.
            if (update == 0) {
                stmt = con.prepareStatement(DB_SKRIPT_INSERT_VERSION);
                stmt.setInt(1, skript.getVersion());
                stmt.setBoolean(2, erfolgreich);
                stmt.setString(3, skript.getBeschreibung());
                stmt.execute();
                stmt.close();
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Schreiben der Version fehlgeschlagen.", e);
        }
    }

    private boolean executeDBSkript(DBSkript skript) throws UpdateNotPossibleException {
        String sql;

        // 1. read script
        try {
            skript.lesen();
            sql = skript.getInhalt();
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Kann DBSkript nicht lesen: " + skript.getDatei().getAbsolutePath(), e);
            return false;
        }

        // 2. execute SQL
        try {
            sqlBatch(sql);
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Kann DBSkript nicht ausführen: " + skript.getDatei().getAbsolutePath(), e);
            return false;
        }
        return true;
    }

    /** Erzeugt die Versionstabelle falls sie noch nicht existiert. */
    public void createVersionsTable() throws UpdateNotPossibleException {
        if (this.con == null) {
            init();
        }
        try {
            int result = sqlUpdate(DB_SKRIPT_CREATE_TABLE_VERSION);
            if (result == 0) {
                LOG.info("Tabelle dbversion existiert bereits: Ok");
            } else {
                LOG.info("Tabelle dbversion erfolgreich angelegt.");
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Problem beim Verbinden mit der DB: ", e);
            throw new UpdateNotPossibleException();
        }
    }

    private int sqlBatch(String sql) throws SQLException, UpdateNotPossibleException {
        Statement stmt = null;
        List<String> sqlList = prepareSql(sql);
        try {
            int result;
            con.setAutoCommit(false);
            stmt = con.createStatement();
            for (String line : sqlList) {
                stmt.addBatch(line);
            }
            stmt.executeBatch();
            result = stmt.getUpdateCount();
            con.commit();
            return result;
        } catch (SQLException e) {
            con.rollback();
            throw e;
        } finally {
            if (stmt != null) {
                stmt.close();
            }
        }
    }

    List<String> prepareSql(String sql) throws UpdateNotPossibleException {
        BufferedReader reader = new BufferedReader(new StringReader(sql));
        String line;
        StringBuilder b = new StringBuilder();
        try {
            while ((line = reader.readLine()) != null) {
                if (StringUtils.isNotBlank(line) && !line.startsWith("--")) {
                    b.append(line + " ");
                }
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Kann SQL nicht parsen", e);
            throw new UpdateNotPossibleException();
        }
        String[] sqls = b.toString().split(";");
        List<String> result = new ArrayList<String>();
        for (String s : sqls) {
            s = s.trim();
            if (s.length() > 0) {
                result.add(s);
            }
        }
        return result;
    }

    private int sqlUpdate(String sql) throws SQLException {
        Statement stmt = null;

        try {
            int result;
            stmt = con.createStatement();
            stmt.execute(sql);
            result = stmt.getUpdateCount();
            return result;
        } finally {
            if (stmt != null) {
                stmt.close();
            }
        }
    }

    static String getVersionInsertSql() {
        StringBuilder b = new StringBuilder();
        b.append("INSERT INTO `dbversion` (`version`, `success`, `description`)");
        b.append(" VALUES (?, ?, ?)");
        return b.toString();
    }

    static String getVersionUpdateSql() {
        StringBuilder b = new StringBuilder();
        b.append("UPDATE `dbversion` SET success=?, description=? WHERE version=?");
        return b.toString();
    }

    static String getTableCreateSql() {
        StringBuilder b = new StringBuilder();
        b.append("CREATE TABLE IF NOT EXISTS `dbversion` (");
        b.append("`id` INT NOT NULL AUTO_INCREMENT,");
        b.append("`version` INT NOT NULL COMMENT 'Versionsnummer',");
        b.append("`success` BOOL NOT NULL COMMENT 'Aktualisierung erfolgreich? 1=ja, 0=nein',");
        b.append("`description` VARCHAR(255) NOT NULL COMMENT 'Beschreibung der Aktualisierung',");
        b.append("`date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Aktuelles Datum',");
        b.append("PRIMARY KEY ( `id` )");
        b.append(") COMMENT = 'Knows the version number of the data base.';");
        return b.toString();
    }

    static String getVersionTableSelectSql() {
        StringBuilder b = new StringBuilder();
        b.append("SELECT version FROM `dbversion` WHERE");
        b.append(" `success`=1");
        b.append(" ORDER BY version DESC LIMIT 1");
        return b.toString();
    }

    /**
     * Loads the appropriate JDBC driver for this environment/framework. For
     * example, if we are in an embedded environment, we load Derby's embedded
     * Driver, <code>org.apache.derby.jdbc.EmbeddedDriver</code>.
     */
    private void loadDriver(String treiber) throws UpdateNotPossibleException {
        /*
         * The JDBC driver is loaded by loading its class. If you are using JDBC
         * 4.0 (Java SE 6) or newer, JDBC drivers may be automatically loaded,
         * making this code optional.
         * 
         * In an embedded environment, this will also start up the Derby engine
         * (though not any databases), since it is not already running. In a
         * client environment, the Derby engine is being run by the network
         * server framework.
         * 
         * In an embedded environment, any static Derby system properties must
         * be set before loading the driver to take effect.
         */
        try {
            Class.forName(DRIVER).newInstance();
            return;
        } catch (ClassNotFoundException e) {
            LOG.log(Level.SEVERE, "Unable to load the JDBC driver, Klassenpfad ueberpruefen! " + treiber, e);
        } catch (InstantiationException e) {
            LOG.log(Level.SEVERE, "Unable to instantiate the JDBC driver " + treiber, e);
        } catch (IllegalAccessException e) {
            LOG.log(Level.SEVERE, "Not allowed to access the JDBC driver " + treiber, e);
        }
        throw new UpdateNotPossibleException();
    }
}
