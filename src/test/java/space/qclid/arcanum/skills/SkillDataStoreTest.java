package space.qclid.arcanum.skills;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class SkillDataStoreTest {

    private static final UUID STEVE = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID ALEX = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @TempDir
    File dir;

    private final SkillCurve curve = new SkillCurve(12.0, 1.3, 150);
    private File file;
    private SkillDataStore store;

    @BeforeEach
    void setUp() {
        file = new File(dir, "skill-data.yml");
        store = new SkillDataStore(file, Logger.getLogger("test"), curve);
    }

    private long countFiles(String contains) {
        String[] names = dir.list();
        long n = 0;
        if (names != null) for (String s : names) if (s.contains(contains)) n++;
        return n;
    }

    @Test
    void roundTripKeepsXpAndSkipsZeroRows() {
        PlayerSkills steve = new PlayerSkills();
        steve.setXp(SkillType.MINING, 1234);
        PlayerSkills alex = new PlayerSkills();
        alex.setXp(SkillType.RUNNING, 99);
        assertTrue(store.save(Map.of(STEVE, steve, ALEX, alex)));

        Map<UUID, PlayerSkills> loaded = store.load();
        assertEquals(1234, loaded.get(STEVE).getXp(SkillType.MINING));
        assertEquals(0, loaded.get(STEVE).getXp(SkillType.RUNNING));
        assertEquals(99, loaded.get(ALEX).getXp(SkillType.RUNNING));
        assertEquals(0, countFiles(".bak"));
    }

    @Test
    void missingFileLoadsEmpty() {
        assertTrue(store.load().isEmpty());
    }

    @Test
    void badRowsAreSkippedAndTheOriginalIsBackedUp() throws Exception {
        String yaml = "players:\n"
            + "  " + STEVE + ":\n"
            + "    mining: 500\n"
            + "    flying: 40\n"          // unknown skill id
            + "    running: \"12k\"\n"     // not a number
            + "  not-a-uuid:\n"
            + "    mining: 7\n";
        Files.writeString(file.toPath(), yaml);

        Map<UUID, PlayerSkills> loaded = store.load();

        assertEquals(1, loaded.size());
        assertEquals(500, loaded.get(STEVE).getXp(SkillType.MINING));
        assertEquals(0, loaded.get(STEVE).getXp(SkillType.RUNNING));
        assertEquals(1, countFiles(".bak"), "original must be backed up before bad rows are lost");
        File bak = new File(dir, java.util.Objects.requireNonNull(dir.list((d, n) -> n.contains(".bak")))[0]);
        assertEquals(yaml, Files.readString(bak.toPath()));
    }

    @Test
    void corruptYamlIsMovedAsideAndLoadReturnsEmpty() throws Exception {
        Files.writeString(file.toPath(), "players: [unclosed\n  - : :\n");
        assertTrue(store.load().isEmpty());
        assertFalse(file.exists());
        assertEquals(1, countFiles(".corrupt-"));
    }

    @Test
    void hugeAndNegativeXpAreClamped() throws Exception {
        Files.writeString(file.toPath(), "players:\n  " + STEVE + ":\n    mining: 999999999999999\n    running: -5\n");
        Map<UUID, PlayerSkills> loaded = store.load();
        assertEquals(curve.maxTotalXp(), loaded.get(STEVE).getXp(SkillType.MINING));
        assertEquals(0, loaded.get(STEVE).getXp(SkillType.RUNNING));
    }

    @Test
    void savedFileIsValidYamlWithNoTempLeftBehind() throws Exception {
        PlayerSkills steve = new PlayerSkills();
        steve.setXp(SkillType.MINING, 5);
        assertTrue(store.save(Map.of(STEVE, steve)));
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        assertEquals(5, yaml.getLong("players." + STEVE + ".mining"));
        assertEquals(0, countFiles(".tmp"));
    }
}
