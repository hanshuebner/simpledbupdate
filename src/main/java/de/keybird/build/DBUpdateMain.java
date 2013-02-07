package de.keybird.build;

import java.io.File;
import java.util.logging.Level;

import lombok.extern.java.Log;
import de.keybird.build.dbupdate.ConfigReader;
import de.keybird.build.dbupdate.FileHelper;
import de.keybird.build.dbupdate.UpdateNotPossibleException;
import de.keybird.build.dbupdate.config.ConfigReaderFromContextXml;

/**
 * 
 * This class is an example of how a DBUpdate can be configured
 * 
 * @author patrick
 */
@Log
public class DBUpdateMain {

    public static final String SYSTEM_PARAM_CONFIG_FILE = "CONFIG_FILE";
    public static final String SYSTEM_PARAM_UPDATE_SCRIPT_DIR = "UPDATE_SCRIPT_DIR";

    public static void main(String[] args) {
        try {
            File configFile = FileHelper.checkFile(System.getProperty(SYSTEM_PARAM_CONFIG_FILE), SYSTEM_PARAM_CONFIG_FILE);
            ConfigReader configReader = new ConfigReaderFromContextXml(configFile);
            File pathToUpdatescripts = FileHelper.checkFolder(System.getProperty(SYSTEM_PARAM_UPDATE_SCRIPT_DIR), SYSTEM_PARAM_UPDATE_SCRIPT_DIR);
            DBUpdate dbupdate = new DBUpdate(pathToUpdatescripts, configReader);
            dbupdate.execute();
        } catch (UpdateNotPossibleException e) {
            log.log(Level.SEVERE, "Update not possible.", e);
            System.exit(3);
        }
    }
}
