package de.keybird.build.dbupdate;

import java.util.Properties;

public interface ConfigReader {
    public Properties getProperties() throws UpdateNotPossibleException;
}
