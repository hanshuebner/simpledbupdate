package de.keybird.build.dbupdate.config;

import java.io.File;
import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;

import de.keybird.build.dbupdate.DBHelfer;

public class ConfigReaderFromPHPArrayTest extends TestCase {

    public void testParse() throws Exception {

        // Check if we can read a php file
        File datei = FileUtils.toFile(this.getClass().getResource("config.inc.php"));
        ConfigReaderFromPHPArray config = new ConfigReaderFromPHPArray(datei);
        List<String> lines = FileUtils.readLines(datei);
        Properties props = config.parse(lines);
        assertEquals("user", props.getProperty(DBHelfer.PROP_USER));
        assertEquals("password", props.getProperty(DBHelfer.PROP_PASSWORD));
        assertEquals("database", props.getProperty(DBHelfer.PROP_DB_NAME));

    }
}
