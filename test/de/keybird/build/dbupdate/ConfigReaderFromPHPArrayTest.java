package de.keybird.build.dbupdate;

import java.io.File;
import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;

import de.keybird.build.dbupdate.config.ConfigReaderFromPHPArray;

public class ConfigReaderFromPHPArrayTest extends TestCase {

    public void testParse() throws Exception {

        // Pr�fen ob original Konfigurationsdatei eingelesen werden kann.
        File datei = FileUtils.toFile(this.getClass().getResource("config.inc.php"));
        ConfigReaderFromPHPArray config = new ConfigReaderFromPHPArray(datei);
        List<String> lines = FileUtils.readLines(datei);
        Properties props = config.parse(lines);
        assertEquals("root", props.getProperty(DBHelfer.PROP_USER));
        assertEquals("entwickler", props.getProperty(DBHelfer.PROP_PASSWORD));
        assertEquals("trunk-wild", props.getProperty(DBHelfer.PROP_DB_NAME));

    }
}
