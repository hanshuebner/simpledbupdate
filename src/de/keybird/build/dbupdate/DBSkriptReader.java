package de.keybird.build.dbupdate;

import java.io.File;
import java.io.FileFilter;
import java.io.Serializable;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DBSkriptReader {

    private DBFileFilter filter = new DBFileFilter();
    private DBFileSorter sorter = new DBFileSorter();

    private static final Logger LOG = Logger.getLogger(DBSkriptReader.class.getName());

    public SortedSet<DBSkript> getAlleDBSkripte(File dir) {
        LOG.log(Level.INFO, "Durchsuche Verzeichnis nach DB-Skripten: " + dir.getAbsolutePath());
        File[] filesArray = dir.listFiles(filter);
        SortedSet<DBSkript> skripte = new TreeSet<DBSkript>(sorter);
        DBSkript skript;
        for (File file : filesArray) {
            skript = new DBSkript(file);
            skripte.add(skript);
        }
        LOG.log(Level.INFO, skripte.size() + " Skripte gefunden.");
        return skripte;
    }

    public DBSkript getNextDBSkript(int letztesSkript, SortedSet<DBSkript> skripte) {
        for (DBSkript skript : skripte) {
            if (skript.getVersion() > letztesSkript) {
                return skript;
            }
        }
        return null;
    }

    static class DBFileSorter implements Comparator<DBSkript>, Serializable {
        private static final long serialVersionUID = 1L;

        public int compare(DBSkript skript1, DBSkript skript2) {
            int comp = skript1.getVersion() - skript2.getVersion();
            if (comp == 0 && !skript1.equals(skript2)) {
                LOG.log(Level.WARNING, "Warning: 2 scripts have the same version number, please check: "
                        + skript1.getDatei().getAbsolutePath() + " " + skript2.getDatei().getAbsolutePath());
                throw new IllegalArgumentException("2 scripts have the same version number, please check.");
            }
            return comp;
        }
    }

    static class DBFileFilter implements FileFilter {
        private static final String REGEXP = "dbupdate_v[0-9]{1}[0-9]*_.*\\.sql";
        private Pattern pattern;

        public DBFileFilter() {
            this.pattern = Pattern.compile(REGEXP);
        }

        public boolean accept(File datei) {
            if (!datei.isFile()) {
                return false;
            }
            if (!datei.canRead()) {
                return false;
            }
            Matcher matcher = pattern.matcher(datei.getName());
            if (!matcher.matches()) {
                return false;
            }
            return true;
        }
    }
}
