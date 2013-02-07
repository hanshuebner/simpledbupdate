package de.keybird.build.dbupdate.config;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import de.keybird.build.dbupdate.ConfigReader;
import de.keybird.build.dbupdate.DBHelfer;
import de.keybird.build.dbupdate.UpdateNotPossibleException;
import de.keybird.build.util.StringTrimmer;

public class ConfigReaderFromContextXml implements ConfigReader {

    private static final Logger LOG = Logger.getLogger(ConfigReaderFromContextXml.class.getName());
    private static final Map<String, String> MAPPING = new HashMap<String, String>();
    static {
        MAPPING.put(DBHelfer.PROP_USER, "username");
        MAPPING.put(DBHelfer.PROP_PASSWORD, "password");
    }

    private File configFile;

    public ConfigReaderFromContextXml(File configFile) {
        this.configFile = configFile;
    }

    @Override
    public Properties getProperties() throws UpdateNotPossibleException {
        Properties props;
        try {
            String config = FileUtils.readFileToString(configFile);
            props = parse(config);
            addURLParams(props, config);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Kann Konfigurationsdatei nicht lesen: " + configFile.getAbsolutePath(), e);
            throw new UpdateNotPossibleException();
        }
        return props;
    }

    public Properties parse(String config) {
        Properties props = new Properties();
        for (Entry<String, String> entry : MAPPING.entrySet()) {
            String attributeName = entry.getValue();
            String value = parse(config, attributeName);
            props.setProperty(entry.getKey(), value);
        }
        return props;
    }

    private String parse(String config, String attributeName) {
        StringTrimmer t = new StringTrimmer(config);
        String value = t.getAfterNext(attributeName).getAfterNext("\"").getBeforeNext("\"").toString();
        return value;
    }

    private void addURLParams(Properties properties, String config) {
        // jdbc:mysql://localhost:3306/dbname?rewriteBatchedStatements=true
        StringTrimmer t = new StringTrimmer(config).getAfterNext("jdbc:mysql://").getBeforeNext(":");
        String host = t.toString();
        t = new StringTrimmer(config);
        String dbName = t.getAfterNext("jdbc:mysql://").getAfterNext("/").getBeforeNext("?").getBeforeNext("\"").toString();
        properties.setProperty(DBHelfer.PROP_DB_HOST, host);
        properties.setProperty(DBHelfer.PROP_DB_NAME, dbName);
    }

}
