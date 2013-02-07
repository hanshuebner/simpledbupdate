package de.keybird.build.dbupdate;

import java.io.File;
import java.io.IOException;
import java.util.SortedSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;

import de.keybird.build.dbupdate.DBSkriptReader.DBFileFilter;

public class DBSkriptReaderTest extends TestCase {

    private File tmpDir;

    private static final Logger LOG = Logger.getLogger(DBSkriptReaderTest.class.getName());

    @Override
    public void setUp() throws IOException {
        File tmpFile = File.createTempFile("~" + this.getClass().getSimpleName(), "tmp");
        tmpDir = new File(tmpFile.getParentFile(), this.getClass().getSimpleName());
        assertTrue(tmpDir.mkdir());
        assertTrue(tmpFile.delete());
    }

    public void testDBFileFilter() throws IOException {
        DBFileFilter filter = new DBFileFilter();

        // Nicht existierende Dateien
        assertFalse(filter.accept(new File(tmpDir, "dbupdate_v1234.sql")));
        File dir = new File(tmpDir, "dbupdate_v11111.sql");
        assertTrue(dir.mkdir());
        assertFalse(filter.accept(dir));

        // Regex
        assertTrue(filter.accept(getFile("dbupdate_v12345_abc.sql")));
        assertTrue(filter.accept(getFile("dbupdate_v0_.sql")));
        assertTrue(filter.accept(getFile("dbupdate_v0003_xxx.sql")));
        assertFalse(filter.accept(getFile("dbupdate_v.sql")));
        assertFalse(filter.accept(getFile("dbupdate_v12a345_ff.sql")));
        assertFalse(filter.accept(getFile("dbupdatev12345_dd.sql")));
        assertFalse(filter.accept(getFile(".sql")));
    }

    public void testGetAlleDBSkripte() throws IOException {
        int max = 5;
        createSetOfFiles(max);
        getFile("falscherName.sql");
        getFile("f99999Name.sql");
        DBSkriptReader db = new DBSkriptReader();
        SortedSet<DBSkript> skripte = db.getAlleDBSkripte(tmpDir);
        assertEquals(max, skripte.size());
        int i = 0;
        for (DBSkript skript : skripte) {
            assertEquals(i, skript.getVersion());
            i++;
        }
    }

    public void testGetAlleDBSkripteDoppelteVersionsNr() throws IOException {
        int max = 5;
        createSetOfFiles(max);
        getFile("dbupdate_v0033_aa.sql");
        getFile("dbupdate_v33_bb.sql");
        DBSkriptReader db = new DBSkriptReader();
        try {
            db.getAlleDBSkripte(tmpDir);
            fail("Erwartet: IllegalArgumentException wegen doppelter Versionsnummer");
        } catch (IllegalArgumentException e) {
            LOG.log(Level.INFO, "Erwartete Ausnahme IllegalArgumentException gefangen.");
        }

    }

    public void testGetNextSkript() throws IOException {

        createSetOfFiles(5);
        getFile("dbupdate_v0033_bb.sql");
        getFile("f99999Name.sql");
        DBSkriptReader db = new DBSkriptReader();
        SortedSet<DBSkript> skripte = db.getAlleDBSkripte(tmpDir);
        assertEquals(1, db.getNextDBSkript(0, skripte).getVersion());
        assertEquals(2, db.getNextDBSkript(1, skripte).getVersion());
        assertEquals(3, db.getNextDBSkript(2, skripte).getVersion());
        assertEquals(4, db.getNextDBSkript(3, skripte).getVersion());
        assertEquals(4, db.getNextDBSkript(3, skripte).getVersion());
        assertEquals(33, db.getNextDBSkript(32, skripte).getVersion());
        assertNull(db.getNextDBSkript(33, skripte));
    }

    private void createSetOfFiles(int max) throws IOException {
        for (int i = 0; i < max; i++) {
            getFile("dbupdate_v" + i + "_xx" + i + ".sql");
        }
    }

    private File getFile(String name) throws IOException {
        File f = new File(tmpDir, name);
        assertTrue(f.createNewFile());
        return f;
    }

    @Override
    public void tearDown() throws IOException {
        FileUtils.deleteDirectory(tmpDir);
    }
}
