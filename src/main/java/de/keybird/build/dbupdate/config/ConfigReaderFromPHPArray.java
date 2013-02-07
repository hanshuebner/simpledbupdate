package de.keybird.build.dbupdate.config;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import de.keybird.build.dbupdate.DBHelfer;
import de.keybird.build.dbupdate.UpdateNotPossibleException;

public class ConfigReaderFromPHPArray {

    private static final Logger LOG = Logger.getLogger(ConfigReaderFromPHPArray.class.getName());

    private static final String PREFIX = "$APP_CONF['";
    private static final String PREFIX2 = "] = '";
    private Map<String, String> orgValues = new HashMap<String, String>();
    private static final Map<String, String> MAPPING = new HashMap<String, String>();
    private File configFile;
    static {
        MAPPING.put(DBHelfer.PROP_DB_HOST, "db_host");
        MAPPING.put(DBHelfer.PROP_DB_NAME, "db_database");
        MAPPING.put(DBHelfer.PROP_USER, "db_user");
        MAPPING.put(DBHelfer.PROP_PASSWORD, "db_passwd");
    }

    public ConfigReaderFromPHPArray(File configFile) {
        this.configFile = configFile;
    }

    public Properties getProperties() throws UpdateNotPossibleException {
        Properties props;
        try {
            List<String> lines = FileUtils.readLines(configFile);
            props = parse(lines);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Cannot read config file: " + configFile.getAbsolutePath(), e);
            throw new UpdateNotPossibleException();
        }
        return props;
    }

    public Properties parse(List<String> lines) {
        for (String line : lines) {
            if (line.startsWith(PREFIX)) {
                parse(line);
            }
        }
        return toProperties();
    }

    private Properties toProperties() {
        Properties prop = new Properties();
        fillProperty(prop, DBHelfer.PROP_DB_HOST);
        fillProperty(prop, DBHelfer.PROP_DB_NAME);
        fillProperty(prop, DBHelfer.PROP_USER);
        fillProperty(prop, DBHelfer.PROP_PASSWORD);
        return prop;
    }

    private void fillProperty(Properties prop, String key) {
        prop.put(key, orgValues.get(MAPPING.get(key)));
    }

    private void parse(String line) {
        try {
            line = line.substring(PREFIX.length(), line.length());
            String schluessel = line.substring(0, line.indexOf(PREFIX2) - 1);
            line = line.substring(line.indexOf(PREFIX2) + PREFIX2.length(), line.length());
            String wert = line.substring(0, line.indexOf('\''));
            orgValues.put(schluessel, wert);
        } catch (Exception e) {
            LOG.log(Level.FINE, "Zeile ignoriert " + line);
        }
    }
}
