package de.keybird.build.dbupdate;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;

public class DBHelferTest extends TestCase {
    private static final Logger LOG = Logger.getLogger(DBHelferTest.class.getName());

    private String dbName = "DbUpdateTest";
    private Properties props = new Properties();

    private List<File> tmpDateien;
    private DBHelfer sqlHelfer;
    private Connection con;
    private Statement stmt;

    @Override
    public void setUp() throws Exception {

        props.put(DBHelfer.PROP_DB_HOST, "127.0.0.1");
        props.put(DBHelfer.PROP_DB_NAME, dbName);
        props.put(DBHelfer.PROP_USER, "DbUpdateTest");
        props.put(DBHelfer.PROP_PASSWORD, "DbUpdateTest");
        sqlHelfer = new DBHelfer(props);

        setUpDatabaseConnection();
        stmt = con.createStatement();
        stmt.execute("DROP TABLE IF EXISTS `dbversion`");
        stmt.execute("DROP TABLE IF EXISTS `test`");
        stmt.execute("CREATE TABLE `test` (`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY ,`text` VARCHAR( 255 ) NOT NULL);");
        stmt.close();
        tmpDateien = new ArrayList<File>();
    }

    private void setUpDatabaseConnection() throws Exception {
        Class.forName(DBHelfer.DRIVER).newInstance();
        con = DriverManager.getConnection(sqlHelfer.getConnectionString(), props);
    }

    private void closeDatabaseConnection() {
        try {
            stmt.close();
            con.close();
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Kann DB-Verbindung nicht schliessen", e);
        }
    }

    public void testInsertDBSkript() throws Exception {
        try {

            // SQL with an error in the second statement
            StringBuilder b = new StringBuilder();
            b.append("INSERT INTO `test` (`id` ,`text`) VALUES (NULL , 'xxx');\n");
            b.append("broken SQL;\n");
            DBSkript skript = sqlVorbereiten(b.toString(), 10);
            try {
                sqlHelfer.update(skript);
                fail("Expected UpdateNotPossibleException was not thrown");
            } catch (UpdateNotPossibleException e) {
                LOG.log(Level.INFO, "Caught expected UpdateNotPossibleException");
            }

            // correct script
            b = new StringBuilder();
            b.append("--- Kommentar\n");
            b.append("INSERT INTO `test` (`id` ,`text`) VALUES (NULL , 'xxx');\n");
            skript = sqlVorbereiten(b.toString(), 10);
            sqlHelfer.update(skript);

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "error", e);
            fail();
        }
    }

    public void testRollback() throws Exception {
        try {

            // SQL with an error in the second statement
            StringBuilder b = new StringBuilder();
            b.append("INSERT INTO `test` (`id` ,`text`) VALUES (NULL , 'xxx');\n");
            b.append("broken SQL;\n");
            DBSkript skript = sqlVorbereiten(b.toString(), 10);
            try {
                sqlHelfer.update(skript);
                fail("Expected UpdateNotPossibleException was not thrown");
            } catch (UpdateNotPossibleException e) {
                LOG.log(Level.INFO, "Caught expected UpdateNotPossibleException");
            }

            // Check if rollback worked
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM test");
            if (rs.next()) {
                fail("Rollback has not worked");
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "error", e);
            fail();
        }
    }

    private DBSkript sqlVorbereiten(String sql, int version) throws Exception {
        File tmpDir = SystemUtils.getJavaIoTmpDir();
        File f = new File(tmpDir, "dbupdate_v123_test.sql");
        tmpDateien.add(f);
        FileUtils.writeStringToFile(f, sql);
        DBSkript skript = new DBSkript(f);
        skript.lesen();
        return skript;
    }

    public void testVersionenAuslesenTest() throws Exception {
        // Erstmal Versionstabelle erstellen
        sqlHelfer.createVersionsTable();
        con = DriverManager.getConnection(sqlHelfer.getConnectionString(), props);
        stmt = con.createStatement();
        stmt.execute("INSERT INTO `dbversion` (`version` ,`success` ,`description`) VALUES ('10', '0', 'xxx') ");
        stmt.execute("INSERT INTO `dbversion` (`version` ,`success` ,`description`) VALUES ('10', '1', 'xxx') ");
        stmt.execute("INSERT INTO `dbversion` (`version` ,`success` ,`description`) VALUES ('11', '1', 'xxx') ");
        stmt.execute("INSERT INTO `dbversion` (`version` ,`success` ,`description`) VALUES ('12', '0', 'xxx') ");
        int version = sqlHelfer.getAktuelleVersion();
        assertEquals(11, version);
    }

    public void testVersionstabelleErstellen() throws Exception {
        try {
            sqlHelfer.createVersionsTable();
            stmt.execute("INSERT INTO `dbversion` (`version` ,`success` ,`description`) VALUES ('123', '1', 'xxx') ");
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Insert hat nicht geklappt.", e);
            fail();
        }
    }

    public void testPrepareSql() throws Exception {
        String sql = "xxx;\n--  nnn\naaa;\n";
        List<String> erg = sqlHelfer.prepareSql(sql);
        assertEquals(2, erg.size());
        assertEquals(erg.get(0), "xxx");
        assertEquals(erg.get(1), "aaa");
    }

    public void testPrepareSql2() throws Exception {
        String file = FileUtils.readFileToString(new File("src/test/resources/de/keybird/build/dbupdate/DBHelferTest.sql"));
        List<String> erg = sqlHelfer.prepareSql(file);
        for (String sql : erg) {
            System.out.println(sql);
        }
    }

    @Override
    public void tearDown() {
        for (File f : tmpDateien) {
            if (!f.delete()) {
                LOG.log(Level.WARNING, "Kann Datei nicht loeschen: " + f.getAbsolutePath());
            }
        }
        closeDatabaseConnection();
    }
}
