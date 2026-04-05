package world.bentobox.aoneblock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.block.Biome;
import org.bukkit.entity.EntityType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import world.bentobox.aoneblock.listeners.BlockListener;

/**
 * @author tastybento
 *
 */
public class SettingsTest extends CommonTestSetup {

    private Settings s;

    /**
     * @throws java.lang.Exception
     */
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        s = new Settings();
    }

    /**
     * @throws java.lang.Exception
     */
    @Override
    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getFriendlyName()}.
     */
    @Test
    void testGetFriendlyName() {
        assertEquals("OneBlock", s.getFriendlyName());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getWorldName()}.
     */
    @Test
    void testGetWorldName() {
        assertEquals("oneblock_world", s.getWorldName());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getDifficulty()}.
     */
    @Test
    void testGetDifficulty() {
        assertEquals(Difficulty.NORMAL, s.getDifficulty());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getIslandDistance()}.
     */
    @Test
    void testGetIslandDistance() {
        assertEquals(400, s.getIslandDistance());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getIslandProtectionRange()}.
     */
    @Test
    void testGetIslandProtectionRange() {
        assertEquals(50, s.getIslandProtectionRange());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getIslandStartX()}.
     */
    @Test
    void testGetIslandStartX() {
        assertEquals(0, s.getIslandStartX());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getIslandStartZ()}.
     */
    @Test
    void testGetIslandStartZ() {
        assertEquals(0, s.getIslandStartZ());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getIslandXOffset()}.
     */
    @Test
    void testGetIslandXOffset() {
        assertEquals(0, s.getIslandXOffset());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getIslandZOffset()}.
     */
    @Test
    void testGetIslandZOffset() {
        assertEquals(0, s.getIslandZOffset());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getIslandHeight()}.
     */
    @Test
    void testGetIslandHeight() {
        assertEquals(120, s.getIslandHeight());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isUseOwnGenerator()}.
     */
    @Test
    void testIsUseOwnGenerator() {
        assertFalse(s.isUseOwnGenerator());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getSeaHeight()}.
     */
    @Test
    void testGetSeaHeight() {
        assertEquals(0, s.getSeaHeight());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getMaxIslands()}.
     */
    @Test
    void testGetMaxIslands() {
        assertEquals(-1, s.getMaxIslands());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getDefaultGameMode()}.
     */
    @Test
    void testGetDefaultGameMode() {
        assertEquals(GameMode.SURVIVAL, s.getDefaultGameMode());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isNetherGenerate()}.
     */
    @Test
    void testIsNetherGenerate() {
        assertTrue(s.isNetherGenerate());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isNetherIslands()}.
     */
    @Test
    void testIsNetherIslands() {
        assertFalse(s.isNetherIslands());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isNetherRoof()}.
     */
    @Test
    void testIsNetherRoof() {
        assertFalse(s.isNetherRoof());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getNetherSpawnRadius()}.
     */
    @Test
    void testGetNetherSpawnRadius() {
        assertEquals(32, s.getNetherSpawnRadius());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isEndGenerate()}.
     */
    @Test
    void testIsEndGenerate() {
        assertFalse(s.isEndGenerate());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isEndIslands()}.
     */
    @Test
    void testIsEndIslands() {
        assertFalse(s.isEndIslands());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isDragonSpawn()}.
     */
    @Test
    void testIsDragonSpawn() {
        assertFalse(s.isDragonSpawn());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getRemoveMobsWhitelist()}.
     */
    @Test
    void testGetRemoveMobsWhitelist() {
        assertTrue(s.getRemoveMobsWhitelist().isEmpty());
     
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getWorldFlags()}.
     */
    @Test
    void testGetWorldFlags() {
        assertTrue(s.getWorldFlags().isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getDefaultIslandFlagNames()}.
     */
    @Test
    void testGetDefaultIslandFlagNames() {
        assertTrue(s.getDefaultIslandFlagNames().isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getDefaultIslandSettingNames()}.
     */
    @Test
    void testGetDefaultIslandSettingNames() {
        assertTrue(s.getDefaultIslandSettingNames().isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getDefaultIslandFlags()}.
     */
    @SuppressWarnings("deprecation")
    @Test
    void testGetDefaultIslandFlags() {
        assertTrue(s.getDefaultIslandFlags().isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getDefaultIslandSettings()}.
     */
    @SuppressWarnings("deprecation")
    @Test
    void testGetDefaultIslandSettings() {
        assertTrue(s.getDefaultIslandSettings().isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getHiddenFlags()}.
     */
    @Test
    void testGetHiddenFlags() {
        assertTrue(s.getHiddenFlags().isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getVisitorBannedCommands()}.
     */
    @Test
    void testGetVisitorBannedCommands() {
        assertTrue(s.getVisitorBannedCommands().isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getFallingBannedCommands()}.
     */
    @Test
    void testGetFallingBannedCommands() {
        assertTrue(s.getFallingBannedCommands().isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getMaxTeamSize()}.
     */
    @Test
    void testGetMaxTeamSize() {
        assertEquals(4, s.getMaxTeamSize());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getMaxHomes()}.
     */
    @Test
    void testGetMaxHomes() {
        assertEquals(5, s.getMaxHomes());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getResetLimit()}.
     */
    @Test
    void testGetResetLimit() {
        assertEquals(-1, s.getResetLimit());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isLeaversLoseReset()}.
     */
    @Test
    void testIsLeaversLoseReset() {
        assertFalse(s.isLeaversLoseReset());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isKickedKeepInventory()}.
     */
    @Test
    void testIsKickedKeepInventory() {
        assertFalse(s.isKickedKeepInventory());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isCreateIslandOnFirstLoginEnabled()}.
     */
    @Test
    void testIsCreateIslandOnFirstLoginEnabled() {
        assertFalse(s.isCreateIslandOnFirstLoginEnabled());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getCreateIslandOnFirstLoginDelay()}.
     */
    @Test
    void testGetCreateIslandOnFirstLoginDelay() {
        assertEquals(5, s.getCreateIslandOnFirstLoginDelay());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isCreateIslandOnFirstLoginAbortOnLogout()}.
     */
    @Test
    void testIsCreateIslandOnFirstLoginAbortOnLogout() {
        assertTrue(s.isCreateIslandOnFirstLoginAbortOnLogout());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isOnJoinResetMoney()}.
     */
    @Test
    void testIsOnJoinResetMoney() {
        assertFalse(s.isOnJoinResetMoney());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isOnJoinResetInventory()}.
     */
    @Test
    void testIsOnJoinResetInventory() {
        assertTrue(s.isOnJoinResetInventory());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isOnJoinResetEnderChest()}.
     */
    @Test
    void testIsOnJoinResetEnderChest() {
        assertFalse(s.isOnJoinResetEnderChest());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isOnLeaveResetMoney()}.
     */
    @Test
    void testIsOnLeaveResetMoney() {
        assertFalse(s.isOnLeaveResetMoney());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isOnLeaveResetInventory()}.
     */
    @Test
    void testIsOnLeaveResetInventory() {
        assertFalse(s.isOnLeaveResetInventory());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isOnLeaveResetEnderChest()}.
     */
    @Test
    void testIsOnLeaveResetEnderChest() {
        assertFalse(s.isOnLeaveResetEnderChest());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isDeathsCounted()}.
     */
    @Test
    void testIsDeathsCounted() {
        assertTrue(s.isDeathsCounted());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isAllowSetHomeInNether()}.
     */
    @Test
    void testIsAllowSetHomeInNether() {
        assertTrue(s.isAllowSetHomeInNether());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isAllowSetHomeInTheEnd()}.
     */
    @Test
    void testIsAllowSetHomeInTheEnd() {
        assertTrue(s.isAllowSetHomeInTheEnd());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isRequireConfirmationToSetHomeInNether()}.
     */
    @Test
    void testIsRequireConfirmationToSetHomeInNether() {
        assertTrue(s.isRequireConfirmationToSetHomeInNether());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isRequireConfirmationToSetHomeInTheEnd()}.
     */
    @Test
    void testIsRequireConfirmationToSetHomeInTheEnd() {
        assertTrue(s.isRequireConfirmationToSetHomeInTheEnd());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getDeathsMax()}.
     */
    @Test
    void testGetDeathsMax() {
        assertEquals(10, s.getDeathsMax());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isTeamJoinDeathReset()}.
     */
    @Test
    void testIsTeamJoinDeathReset() {
        assertTrue(s.isTeamJoinDeathReset());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getGeoLimitSettings()}.
     */
    @Test
    void testGetGeoLimitSettings() {
        assertTrue(s.getGeoLimitSettings().isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getIvSettings()}.
     */
    @Test
    void testGetIvSettings() {
        assertTrue(s.getIvSettings().isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getResetEpoch()}.
     */
    @Test
    void testGetResetEpoch() {
        assertEquals(0L, s.getResetEpoch());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setFriendlyName(java.lang.String)}.
     */
    @Test
    void testSetFriendlyName() {
        s.setFriendlyName("test");
        assertEquals("test", s.getFriendlyName());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setWorldName(java.lang.String)}.
     */
    @Test
    void testSetWorldName() {
        s.setWorldName("test");
        assertEquals("test", s.getWorldName());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setDifficulty(org.bukkit.Difficulty)}.
     */
    @Test
    void testSetDifficulty() {
        s.setDifficulty(Difficulty.HARD);
        assertEquals(Difficulty.HARD, s.getDifficulty());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setIslandDistance(int)}.
     */
    @Test
    void testSetIslandDistance() {
        s.setIslandDistance(12345);
        assertEquals(12345, s.getIslandDistance());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setIslandProtectionRange(int)}.
     */
    @Test
    void testSetIslandProtectionRange() {
        s.setIslandProtectionRange(12345);
        assertEquals(12345, s.getIslandProtectionRange());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setIslandStartX(int)}.
     */
    @Test
    void testSetIslandStartX() {
        s.setIslandStartX(12345);
        assertEquals(12345, s.getIslandStartX());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setIslandStartZ(int)}.
     */
    @Test
    void testSetIslandStartZ() {
        s.setIslandStartZ(12345);
        assertEquals(12345, s.getIslandStartZ());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setIslandXOffset(int)}.
     */
    @Test
    void testSetIslandXOffset() {
        s.setIslandXOffset(12345);
        assertEquals(12345, s.getIslandXOffset());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setIslandZOffset(int)}.
     */
    @Test
    void testSetIslandZOffset() {
        s.setIslandZOffset(12345);
        assertEquals(12345, s.getIslandZOffset());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setIslandHeight(int)}.
     */
    @Test
    void testSetIslandHeight() {
        s.setIslandHeight(12345);
        assertEquals(12345, s.getIslandHeight());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setUseOwnGenerator(boolean)}.
     */
    @Test
    void testSetUseOwnGenerator() {
        s.setUseOwnGenerator(true);
        assertTrue(s.isUseOwnGenerator());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setSeaHeight(int)}.
     */
    @Test
    void testSetSeaHeight() {
        s.setSeaHeight(12345);
        assertEquals(12345, s.getSeaHeight());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setMaxIslands(int)}.
     */
    @Test
    void testSetMaxIslands() {
        s.setMaxIslands(12345);
        assertEquals(12345, s.getMaxIslands());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setDefaultGameMode(org.bukkit.GameMode)}.
     */
    @Test
    void testSetDefaultGameMode() {
        s.setDefaultGameMode(GameMode.SPECTATOR);
        assertEquals(GameMode.SPECTATOR, s.getDefaultGameMode());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setNetherGenerate(boolean)}.
     */
    @Test
    void testSetNetherGenerate() {
        s.setNetherGenerate(false);
        assertFalse(s.isNetherGenerate());
        s.setNetherGenerate(true);
        assertTrue(s.isNetherGenerate());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setNetherIslands(boolean)}.
     */
    @Test
    void testSetNetherIslands() {
        s.setNetherIslands(false);
        assertFalse(s.isNetherIslands());
        s.setNetherIslands(true);
        assertTrue(s.isNetherIslands());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setNetherRoof(boolean)}.
     */
    @Test
    void testSetNetherRoof() {
        s.setNetherRoof(false);
        assertFalse(s.isNetherRoof());
        s.setNetherRoof(true);
        assertTrue(s.isNetherRoof());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setNetherSpawnRadius(int)}.
     */
    @Test
    void testSetNetherSpawnRadius() {
        s.setNetherSpawnRadius(12345);
        assertEquals(12345, s.getNetherSpawnRadius());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setEndGenerate(boolean)}.
     */
    @Test
    void testSetEndGenerate() {
        s.setEndGenerate(false);
        assertFalse(s.isEndGenerate());
        s.setEndGenerate(true);
        assertTrue(s.isEndGenerate());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setEndIslands(boolean)}.
     */
    @Test
    void testSetEndIslands() {
        s.setEndIslands(false);
        assertFalse(s.isEndIslands());
        s.setEndIslands(true);
        assertTrue(s.isEndIslands());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setRemoveMobsWhitelist(java.util.Set)}.
     */
    @Test
    void testSetRemoveMobsWhitelist() {
        s.setRemoveMobsWhitelist(Collections.singleton(EntityType.AXOLOTL));
        assertTrue(s.getRemoveMobsWhitelist().contains(EntityType.AXOLOTL));
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setWorldFlags(java.util.Map)}.
     */
    @Test
    void testSetWorldFlags() {
        s.setWorldFlags(Map.of("trueFlag", true, "falseFlag", false));
        assertTrue(s.getWorldFlags().get("trueFlag"));
        assertFalse(s.getWorldFlags().get("falseFlag"));
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setDefaultIslandFlagNames(java.util.Map)}.
     */
    @Test
    void testSetDefaultIslandFlagNames() {
        s.setDefaultIslandFlagNames(Map.of("TEST", 500));
        assertEquals(500, (int) s.getDefaultIslandFlagNames().get("TEST"));
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setDefaultIslandSettingNames(java.util.Map)}.
     */
    @Test
    void testSetDefaultIslandSettingNames() {
        s.setDefaultIslandSettingNames(Map.of("SETTING", 456));
        assertEquals(456, (int) s.getDefaultIslandSettingNames().get("SETTING"));
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setHiddenFlags(java.util.List)}.
     */
    @Test
    void testSetHiddenFlags() {
        s.setHiddenFlags(List.of("FLAG1", "FLAG2"));
        assertTrue(s.getHiddenFlags().contains("FLAG2"));
        assertFalse(s.getHiddenFlags().contains("FLAG3"));
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setVisitorBannedCommands(java.util.List)}.
     */
    @Test
    void testSetVisitorBannedCommands() {
        s.setVisitorBannedCommands(List.of("banned"));
        assertTrue(s.getVisitorBannedCommands().contains("banned"));
        assertFalse(s.getVisitorBannedCommands().contains("not-banned"));
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setFallingBannedCommands(java.util.List)}.
     */
    @Test
    void testSetFallingBannedCommands() {
        s.setFallingBannedCommands(List.of("banned"));
        assertTrue(s.getFallingBannedCommands().contains("banned"));
        assertFalse(s.getFallingBannedCommands().contains("not-banned"));
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setMaxTeamSize(int)}.
     */
    @Test
    void testSetMaxTeamSize() {
        s.setMaxTeamSize(12345);
        assertEquals(12345, s.getMaxTeamSize());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setMaxHomes(int)}.
     */
    @Test
    void testSetMaxHomes() {
        s.setMaxHomes(12345);
        assertEquals(12345, s.getMaxHomes());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setResetLimit(int)}.
     */
    @Test
    void testSetResetLimit() {
        s.setResetLimit(12345);
        assertEquals(12345, s.getResetLimit());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setLeaversLoseReset(boolean)}.
     */
    @Test
    void testSetLeaversLoseReset() {
        s.setLeaversLoseReset(false);
        assertFalse(s.isLeaversLoseReset());
        s.setLeaversLoseReset(true);
        assertTrue(s.isLeaversLoseReset());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setKickedKeepInventory(boolean)}.
     */
    @Test
    void testSetKickedKeepInventory() {
        s.setKickedKeepInventory(false);
        assertFalse(s.isKickedKeepInventory());
        s.setKickedKeepInventory(true);
        assertTrue(s.isKickedKeepInventory());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setOnJoinResetMoney(boolean)}.
     */
    @Test
    void testSetOnJoinResetMoney() {
        s.setOnJoinResetMoney(false);
        assertFalse(s.isOnJoinResetMoney());
        s.setOnJoinResetMoney(true);
        assertTrue(s.isOnJoinResetMoney());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setOnJoinResetInventory(boolean)}.
     */
    @Test
    void testSetOnJoinResetInventory() {
        s.setOnJoinResetInventory(false);
        assertFalse(s.isOnJoinResetInventory());
        s.setOnJoinResetInventory(true);
        assertTrue(s.isOnJoinResetInventory());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setOnJoinResetEnderChest(boolean)}.
     */
    @Test
    void testSetOnJoinResetEnderChest() {
        s.setOnJoinResetEnderChest(false);
        assertFalse(s.isOnJoinResetEnderChest());
        s.setOnJoinResetEnderChest(true);
        assertTrue(s.isOnJoinResetEnderChest());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setOnLeaveResetMoney(boolean)}.
     */
    @Test
    void testSetOnLeaveResetMoney() {
        s.setOnLeaveResetMoney(false);
        assertFalse(s.isOnLeaveResetMoney());
        s.setOnLeaveResetMoney(true);
        assertTrue(s.isOnLeaveResetMoney());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setOnLeaveResetInventory(boolean)}.
     */
    @Test
    void testSetOnLeaveResetInventory() {
        s.setOnLeaveResetInventory(false);
        assertFalse(s.isOnLeaveResetInventory());
        s.setOnLeaveResetInventory(true);
        assertTrue(s.isOnLeaveResetInventory());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setOnLeaveResetEnderChest(boolean)}.
     */
    @Test
    void testSetOnLeaveResetEnderChest() {
        s.setOnLeaveResetEnderChest(false);
        assertFalse(s.isOnLeaveResetEnderChest());
        s.setOnLeaveResetEnderChest(true);
        assertTrue(s.isOnLeaveResetEnderChest());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setCreateIslandOnFirstLoginEnabled(boolean)}.
     */
    @Test
    void testSetCreateIslandOnFirstLoginEnabled() {
        s.setCreateIslandOnFirstLoginEnabled(false);
        assertFalse(s.isCreateIslandOnFirstLoginEnabled());
        s.setCreateIslandOnFirstLoginEnabled(true);
        assertTrue(s.isCreateIslandOnFirstLoginEnabled());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setCreateIslandOnFirstLoginDelay(int)}.
     */
    @Test
    void testSetCreateIslandOnFirstLoginDelay() {
        s.setCreateIslandOnFirstLoginDelay(12345);
        assertEquals(12345, s.getCreateIslandOnFirstLoginDelay());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setCreateIslandOnFirstLoginAbortOnLogout(boolean)}.
     */
    @Test
    void testSetCreateIslandOnFirstLoginAbortOnLogout() {
        s.setCreateIslandOnFirstLoginAbortOnLogout(false);
        assertFalse(s.isCreateIslandOnFirstLoginAbortOnLogout());
        s.setCreateIslandOnFirstLoginAbortOnLogout(true);
        assertTrue(s.isCreateIslandOnFirstLoginAbortOnLogout());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setDeathsCounted(boolean)}.
     */
    @Test
    void testSetDeathsCounted() {
        s.setDeathsCounted(false);
        assertFalse(s.isDeathsCounted());
        s.setDeathsCounted(true);
        assertTrue(s.isDeathsCounted());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setDeathsMax(int)}.
     */
    @Test
    void testSetDeathsMax() {
        s.setDeathsMax(12345);
        assertEquals(12345, s.getDeathsMax());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setTeamJoinDeathReset(boolean)}.
     */
    @Test
    void testSetTeamJoinDeathReset() {
        s.setTeamJoinDeathReset(false);
        assertFalse(s.isTeamJoinDeathReset());
        s.setTeamJoinDeathReset(true);
        assertTrue(s.isTeamJoinDeathReset());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setGeoLimitSettings(java.util.List)}.
     */
    @Test
    void testSetGeoLimitSettings() {
        s.setGeoLimitSettings(List.of("test"));
        assertTrue(s.getGeoLimitSettings().contains("test"));
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setIvSettings(java.util.List)}.
     */
    @Test
    void testSetIvSettings() {
        s.setIvSettings(List.of("test"));
        assertTrue(s.getIvSettings().contains("test"));
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setAllowSetHomeInNether(boolean)}.
     */
    @Test
    void testSetAllowSetHomeInNether() {
        s.setAllowSetHomeInNether(false);
        assertFalse(s.isAllowSetHomeInNether());
        s.setAllowSetHomeInNether(true);
        assertTrue(s.isAllowSetHomeInNether());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setAllowSetHomeInTheEnd(boolean)}.
     */
    @Test
    void testSetAllowSetHomeInTheEnd() {
        s.setAllowSetHomeInTheEnd(false);
        assertFalse(s.isAllowSetHomeInTheEnd());
        s.setAllowSetHomeInTheEnd(true);
        assertTrue(s.isAllowSetHomeInTheEnd());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setRequireConfirmationToSetHomeInNether(boolean)}.
     */
    @Test
    void testSetRequireConfirmationToSetHomeInNether() {
        s.setRequireConfirmationToSetHomeInNether(false);
        assertFalse(s.isRequireConfirmationToSetHomeInNether());
        s.setRequireConfirmationToSetHomeInNether(true);
        assertTrue(s.isRequireConfirmationToSetHomeInNether());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setRequireConfirmationToSetHomeInTheEnd(boolean)}.
     */
    @Test
    void testSetRequireConfirmationToSetHomeInTheEnd() {
        s.setRequireConfirmationToSetHomeInTheEnd(false);
        assertFalse(s.isRequireConfirmationToSetHomeInTheEnd());
        s.setRequireConfirmationToSetHomeInTheEnd(true);
        assertTrue(s.isRequireConfirmationToSetHomeInTheEnd());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setResetEpoch(long)}.
     */
    @Test
    void testSetResetEpoch() {
        s.setResetEpoch(12345);
        assertEquals(12345, s.getResetEpoch());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getPermissionPrefix()}.
     */
    @Test
    void testGetPermissionPrefix() {
        assertEquals("aoneblock", s.getPermissionPrefix());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isWaterUnsafe()}.
     */
    @Test
    void testIsWaterUnsafe() {
        assertFalse(s.isWaterUnsafe());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getDefaultBiome()}.
     */
    @Test
    //@Ignore("Need to solve Biome enum issue")
    void testGetDefaultBiome() {
        assertEquals(Biome.PLAINS, s.getDefaultBiome());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setDefaultBiome(org.bukkit.block.Biome)}.
     */
    @Test
    //@Ignore("Need to solve Biome enum issue")
    void testSetDefaultBiome() {
        assertEquals(Biome.PLAINS, s.getDefaultBiome());
        s.setDefaultBiome(Biome.BAMBOO_JUNGLE);
        assertEquals(Biome.BAMBOO_JUNGLE, s.getDefaultBiome());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getBanLimit()}.
     */
    @Test
    void testGetBanLimit() {
        assertEquals(-1, s.getBanLimit());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setBanLimit(int)}.
     */
    @Test
    void testSetBanLimit() {
        assertEquals(-1, s.getBanLimit());
        s.setBanLimit(12345);
        assertEquals(12345, s.getBanLimit());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getPlayerCommandAliases()}.
     */
    @Test
    void testGetPlayerCommandAliases() {
        assertEquals("ob oneblock",s.getPlayerCommandAliases());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setPlayerCommandAliases(java.lang.String)}.
     */
    @Test
    void testSetPlayerCommandAliases() {
        assertEquals("ob oneblock",s.getPlayerCommandAliases());
        s.setPlayerCommandAliases("aliases");
        assertEquals("aliases",s.getPlayerCommandAliases());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getAdminCommandAliases()}.
     */
    @Test
    void testGetAdminCommandAliases() {
        assertEquals("oba obadmin",s.getAdminCommandAliases());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setAdminCommandAliases(java.lang.String)}.
     */
    @Test
    void testSetAdminCommandAliases() {
        assertEquals("oba obadmin",s.getAdminCommandAliases());
        s.setAdminCommandAliases("aliases");
        assertEquals("aliases",s.getAdminCommandAliases());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isDeathsResetOnNewIsland()}.
     */
    @Test
    void testIsDeathsResetOnNewIsland() {
        assertTrue(s.isDeathsResetOnNewIsland());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setDeathsResetOnNewIsland(boolean)}.
     */
    @Test
    void testSetDeathsResetOnNewIsland() {
        s.setDeathsResetOnNewIsland(false);
        assertFalse(s.isDeathsResetOnNewIsland());
        s.setDeathsResetOnNewIsland(true);
        assertTrue(s.isDeathsResetOnNewIsland());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getOnJoinCommands()}.
     */
    @Test
    void testGetOnJoinCommands() {
        assertTrue(s.getOnJoinCommands().isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setOnJoinCommands(java.util.List)}.
     */
    @Test
    void testSetOnJoinCommands() {
        s.setOnJoinCommands(List.of("command", "do this"));
        assertEquals("do this", s.getOnJoinCommands().get(1));
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getOnLeaveCommands()}.
     */
    @Test
    void testGetOnLeaveCommands() {
        assertTrue(s.getOnLeaveCommands().isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setOnLeaveCommands(java.util.List)}.
     */
    @Test
    void testSetOnLeaveCommands() {
        s.setOnLeaveCommands(List.of("command", "do this"));
        assertEquals("do this", s.getOnLeaveCommands().get(1));
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getOnRespawnCommands()}.
     */
    @Test
    void testGetOnRespawnCommands() {
        assertTrue(s.getOnRespawnCommands().isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setOnRespawnCommands(java.util.List)}.
     */
    @Test
    void testSetOnRespawnCommands() {
        s.setOnRespawnCommands(List.of("command", "do this"));
        assertEquals("do this", s.getOnRespawnCommands().get(1));
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isOnJoinResetHealth()}.
     */
    @Test
    void testIsOnJoinResetHealth() {
        assertTrue(s.isOnJoinResetHealth());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setOnJoinResetHealth(boolean)}.
     */
    @Test
    void testSetOnJoinResetHealth() {
        s.setOnJoinResetHealth(false);
        assertFalse(s.isOnJoinResetHealth());
        s.setOnJoinResetHealth(true);
        assertTrue(s.isOnJoinResetHealth());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isOnJoinResetHunger()}.
     */
    @Test
    void testIsOnJoinResetHunger() {
        assertTrue(s.isOnJoinResetHunger());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setOnJoinResetHunger(boolean)}.
     */
    @Test
    void testSetOnJoinResetHunger() {
        s.setOnJoinResetHunger(false);
        assertFalse(s.isOnJoinResetHunger());
        s.setOnJoinResetHunger(true);
        assertTrue(s.isOnJoinResetHunger());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isOnJoinResetXP()}.
     */
    @Test
    void testIsOnJoinResetXP() {
        assertTrue(s.isOnJoinResetXP());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setOnJoinResetXP(boolean)}.
     */
    @Test
    void testSetOnJoinResetXP() {
        s.setOnJoinResetXP(false);
        assertFalse(s.isOnJoinResetXP());
        s.setOnJoinResetXP(true);
        assertTrue(s.isOnJoinResetXP());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isOnLeaveResetHealth()}.
     */
    @Test
    void testIsOnLeaveResetHealth() {
        assertFalse(s.isOnLeaveResetHealth());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setOnLeaveResetHealth(boolean)}.
     */
    @Test
    void testSetOnLeaveResetHealth() {
        s.setOnLeaveResetHealth(false);
        assertFalse(s.isOnLeaveResetHealth());
        s.setOnLeaveResetHealth(true);
        assertTrue(s.isOnLeaveResetHealth());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isOnLeaveResetHunger()}.
     */
    @Test
    void testIsOnLeaveResetHunger() {
        assertFalse(s.isOnLeaveResetHunger());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setOnLeaveResetHunger(boolean)}.
     */
    @Test
    void testSetOnLeaveResetHunger() {
        s.setOnLeaveResetHunger(false);
        assertFalse(s.isOnLeaveResetHunger());
        s.setOnLeaveResetHunger(true);
        assertTrue(s.isOnLeaveResetHunger());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isOnLeaveResetXP()}.
     */
    @Test
    void testIsOnLeaveResetXP() {
        assertFalse(s.isOnLeaveResetXP());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setOnLeaveResetXP(boolean)}.
     */
    @Test
    void testSetOnLeaveResetXP() {
        assertFalse(s.isOnLeaveResetXP());
        s.setOnLeaveResetXP(true);
        assertTrue(s.isOnLeaveResetXP());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isPasteMissingIslands()}.
     */
    @Test
    void testIsPasteMissingIslands() {
        assertFalse(s.isPasteMissingIslands());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setPasteMissingIslands(boolean)}.
     */
    @Test
    void testSetPasteMissingIslands() {
        assertFalse(s.isPasteMissingIslands());
        s.setPasteMissingIslands(true);
        assertTrue(s.isPasteMissingIslands());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isTeleportPlayerToIslandUponIslandCreation()}.
     */
    @Test
    void testIsTeleportPlayerToIslandUponIslandCreation() {
        assertTrue(s.isTeleportPlayerToIslandUponIslandCreation());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setTeleportPlayerToIslandUponIslandCreation(boolean)}.
     */
    @Test
    void testSetTeleportPlayerToIslandUponIslandCreation() {
        assertTrue(s.isTeleportPlayerToIslandUponIslandCreation());
        s.setTeleportPlayerToIslandUponIslandCreation(false);
        assertFalse(s.isTeleportPlayerToIslandUponIslandCreation());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getSpawnLimitMonsters()}.
     */
    @Test
    void testGetSpawnLimitMonsters() {
        assertEquals(-1, s.getSpawnLimitMonsters());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setSpawnLimitMonsters(int)}.
     */
    @Test
    void testSetSpawnLimitMonsters() {
        assertEquals(-1, s.getSpawnLimitMonsters());
        s.setSpawnLimitMonsters(12345);
        assertEquals(12345, s.getSpawnLimitMonsters());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getSpawnLimitAnimals()}.
     */
    @Test
    void testGetSpawnLimitAnimals() {
        assertEquals(-1, s.getSpawnLimitAnimals());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setSpawnLimitAnimals(int)}.
     */
    @Test
    void testSetSpawnLimitAnimals() {
        assertEquals(-1, s.getSpawnLimitAnimals());
        s.setSpawnLimitAnimals(12345);
        assertEquals(12345, s.getSpawnLimitAnimals());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getSpawnLimitWaterAnimals()}.
     */
    @Test
    void testGetSpawnLimitWaterAnimals() {
        assertEquals(-1, s.getSpawnLimitWaterAnimals());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setSpawnLimitWaterAnimals(int)}.
     */
    @Test
    void testSetSpawnLimitWaterAnimals() {
        assertEquals(-1, s.getSpawnLimitWaterAnimals());
        s.setSpawnLimitWaterAnimals(12345);
        assertEquals(12345, s.getSpawnLimitWaterAnimals());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getSpawnLimitAmbient()}.
     */
    @Test
    void testGetSpawnLimitAmbient() {
        assertEquals(-1, s.getSpawnLimitAmbient());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setSpawnLimitAmbient(int)}.
     */
    @Test
    void testSetSpawnLimitAmbient() {
        assertEquals(-1, s.getSpawnLimitAmbient());
        s.setSpawnLimitAmbient(12345);
        assertEquals(12345, s.getSpawnLimitAmbient());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getTicksPerAnimalSpawns()}.
     */
    @Test
    void testGetTicksPerAnimalSpawns() {
        assertEquals(-1, s.getTicksPerAnimalSpawns());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setTicksPerAnimalSpawns(int)}.
     */
    @Test
    void testSetTicksPerAnimalSpawns() {
        assertEquals(-1, s.getTicksPerAnimalSpawns());
        s.setTicksPerAnimalSpawns(12345);
        assertEquals(12345, s.getTicksPerAnimalSpawns());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getTicksPerMonsterSpawns()}.
     */
    @Test
    void testGetTicksPerMonsterSpawns() {
        assertEquals(-1, s.getTicksPerMonsterSpawns());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setTicksPerMonsterSpawns(int)}.
     */
    @Test
    void testSetTicksPerMonsterSpawns() {
        assertEquals(-1, s.getTicksPerMonsterSpawns());
        s.setTicksPerMonsterSpawns(12345);
        assertEquals(12345, s.getTicksPerMonsterSpawns());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getMaxCoopSize()}.
     */
    @Test
    void testGetMaxCoopSize() {
        assertEquals(4, s.getMaxCoopSize());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setMaxCoopSize(int)}.
     */
    @Test
    void testSetMaxCoopSize() {
        s.setMaxCoopSize(12345);
        assertEquals(12345, s.getMaxCoopSize());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getMaxTrustSize()}.
     */
    @Test
    void testGetMaxTrustSize() {
        assertEquals(4, s.getMaxTrustSize());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setMaxTrustSize(int)}.
     */
    @Test
    void testSetMaxTrustSize() {
        s.setMaxTrustSize(12345);
        assertEquals(12345, s.getMaxTrustSize());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getMobWarning()}.
     */
    @Test
    void testGetMobWarning() {
        assertEquals(5, s.getMobWarning());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setMobWarning(int)}.
     */
    @Test
    void testSetMobWarning() {
        s.setMobWarning(12345);
        // Should be no more than 5
        assertEquals(BlockListener.MAX_LOOK_AHEAD, s.getMobWarning());
        s.setMobWarning(-12345);
        // Should be 0
        assertEquals(0, s.getMobWarning());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isWaterMobProtection()}.
     */
    @Test
    void testIsWaterMobProtection() {
        assertTrue(s.isWaterMobProtection());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setWaterMobProtection(boolean)}.
     */
    @Test
    void testSetWaterMobProtection() {
        s.setWaterMobProtection(false);
        assertFalse(s.isWaterMobProtection());
        s.setWaterMobProtection(true);
        assertTrue(s.isWaterMobProtection());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getDefaultNewPlayerAction()}.
     */
    @Test
    void testGetDefaultNewPlayerAction() {
        assertEquals("create", s.getDefaultNewPlayerAction());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setDefaultNewPlayerAction(java.lang.String)}.
     */
    @Test
    void testSetDefaultNewPlayerAction() {
        s.setDefaultNewPlayerAction("test");
        assertEquals("test", s.getDefaultNewPlayerAction());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getDefaultPlayerAction()}.
     */
    @Test
    void testGetDefaultPlayerAction() {
        assertEquals("go", s.getDefaultPlayerAction());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setDefaultPlayerAction(java.lang.String)}.
     */
    @Test
    void testSetDefaultPlayerAction() {
        s.setDefaultPlayerAction("test");
        assertEquals("test", s.getDefaultPlayerAction());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getMobLimitSettings()}.
     */
    @Test
    void testGetMobLimitSettings() {
        assertTrue(s.getMobLimitSettings().isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setMobLimitSettings(java.util.List)}.
     */
    @Test
    void testSetMobLimitSettings() {
        s.setMobLimitSettings(List.of("test"));
        assertEquals("test", s.getMobLimitSettings().getFirst());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isDropOnTop()}.
     */
    @Test
    void testIsDropOnTop() {
        assertTrue(s.isDropOnTop());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setDropOnTop(boolean)}.
     */
    @Test
    void testSetDropOnTop() {
        s.setDropOnTop(false);
        assertFalse(s.isDropOnTop());
        s.setDropOnTop(true);
        assertTrue(s.isDropOnTop());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getDefaultNetherBiome()}.
     */
    @Test
    void testGetDefaultNetherBiome() {
        assertEquals(Biome.NETHER_WASTES, s.getDefaultNetherBiome());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setDefaultNetherBiome(org.bukkit.block.Biome)}.
     */
    @Test
    void testSetDefaultNetherBiome() {
        assertEquals(Biome.NETHER_WASTES, s.getDefaultNetherBiome());
        s.setDefaultNetherBiome(Biome.BADLANDS);
        assertEquals(Biome.BADLANDS, s.getDefaultNetherBiome());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getDefaultEndBiome()}.
     */
    @Test
    void testGetDefaultEndBiome() {
        assertEquals(Biome.THE_END, s.getDefaultEndBiome());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setDefaultEndBiome(org.bukkit.block.Biome)}.
     */
    @Test
    void testSetDefaultEndBiome() {
        assertEquals(Biome.THE_END, s.getDefaultEndBiome());
        s.setDefaultEndBiome(Biome.BADLANDS);
        assertEquals(Biome.BADLANDS, s.getDefaultEndBiome());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isMakeNetherPortals()}.
     */
    @Test
    void testIsMakeNetherPortals() {
        assertFalse(s.isMakeNetherPortals());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isMakeEndPortals()}.
     */
    @Test
    void testIsMakeEndPortals() {
        assertFalse(s.isMakeEndPortals());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setMakeNetherPortals(boolean)}.
     */
    @Test
    void testSetMakeNetherPortals() {
        s.setMakeNetherPortals(false);
        assertFalse(s.isMakeNetherPortals());
        s.setMakeNetherPortals(true);
        assertTrue(s.isMakeNetherPortals());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setMakeEndPortals(boolean)}.
     */
    @Test
    void testSetMakeEndPortals() {
        s.setMakeEndPortals(false);
        assertFalse(s.isMakeEndPortals());
        s.setMakeEndPortals(true);
        assertTrue(s.isMakeEndPortals());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getPercentCompleteSymbol()}.
     */
    @Test
    void testGetPercentCompleteSymbol() {
        assertEquals("■", s.getPercentCompleteSymbol());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setPercentCompleteSymbol(java.lang.String)}.
     */
    @Test
    void testSetPercentCompleteSymbol() {
        assertEquals("■", s.getPercentCompleteSymbol());
        s.setPercentCompleteSymbol("#");
        assertEquals("#", s.getPercentCompleteSymbol());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getCountCommand()}.
     */
    @Test
    void testGetCountCommand() {
        assertEquals("count", s.getCountCommand());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setCountCommand(java.lang.String)}.
     */
    @Test
    void testSetCountCommand() {
        s.setCountCommand("count123");
        assertEquals("count123", s.getCountCommand());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getPhasesCommand()}.
     */
    @Test
    void testGetPhasesCommand() {
        assertEquals("phases", s.getPhasesCommand());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setPhasesCommand(java.lang.String)}.
     */
    @Test
    void testSetPhasesCommand() {
        s.setPhasesCommand("count123");
        assertEquals("count123", s.getPhasesCommand());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getSetCountCommand()}.
     */
    @Test
    void testGetSetCountCommand() {
        assertEquals("setCount", s.getSetCountCommand());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setSetCountCommand(java.lang.String)}.
     */
    @Test
    void testSetSetCountCommand() {
        s.setSetCountCommand("count123");
        assertEquals("count123", s.getSetCountCommand());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getRespawnBlockCommand()}.
     */
    @Test
    void testGetRespawnBlockCommand() {
        assertEquals("respawnBlock check", s.getRespawnBlockCommand());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setRespawnBlockCommand(java.lang.String)}.
     */
    @Test
    void testSetRespawnBlockCommand() {
        s.setRespawnBlockCommand("respawn");
        assertEquals("respawn", s.getRespawnBlockCommand());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isUseHolograms()}.
     */
    @Test
    void testIsUseHolograms() {
        assertTrue(s.isUseHolograms());
    }
    
    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setUseHolograms(boolean)}.
     */
    @Test
    void testSetUseHolograms() {
        assertTrue(s.isUseHolograms());
        s.setUseHolograms(false);
        assertFalse(s.isUseHolograms());
    }
    
    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setHologramDuration(int)}.
     */
    @Test
    void testSetHologramDuration() {
        s.setHologramDuration(2345);
        assertEquals(2345, s.getHologramDuration());
    }
    
    
    
}