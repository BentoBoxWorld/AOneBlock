package world.bentobox.aoneblock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.block.Biome;
import org.bukkit.entity.EntityType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import world.bentobox.aoneblock.listeners.BlockListener;

/**
 * @author tastybento
 *
 */
@RunWith(PowerMockRunner.class)
public class SettingsTest {

    private Settings s;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        s = new Settings();
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getFriendlyName()}.
     */
    @Test
    public void testGetFriendlyName() {
        assertEquals("OneBlock", s.getFriendlyName());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getWorldName()}.
     */
    @Test
    public void testGetWorldName() {
        assertEquals("oneblock_world", s.getWorldName());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getDifficulty()}.
     */
    @Test
    public void testGetDifficulty() {
        assertEquals(Difficulty.NORMAL, s.getDifficulty());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getIslandDistance()}.
     */
    @Test
    public void testGetIslandDistance() {
        assertEquals(400, s.getIslandDistance());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getIslandProtectionRange()}.
     */
    @Test
    public void testGetIslandProtectionRange() {
        assertEquals(50, s.getIslandProtectionRange());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getIslandStartX()}.
     */
    @Test
    public void testGetIslandStartX() {
        assertEquals(0, s.getIslandStartX());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getIslandStartZ()}.
     */
    @Test
    public void testGetIslandStartZ() {
        assertEquals(0, s.getIslandStartZ());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getIslandXOffset()}.
     */
    @Test
    public void testGetIslandXOffset() {
        assertEquals(0, s.getIslandXOffset());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getIslandZOffset()}.
     */
    @Test
    public void testGetIslandZOffset() {
        assertEquals(0, s.getIslandZOffset());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getIslandHeight()}.
     */
    @Test
    public void testGetIslandHeight() {
        assertEquals(120, s.getIslandHeight());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isUseOwnGenerator()}.
     */
    @Test
    public void testIsUseOwnGenerator() {
        assertFalse(s.isUseOwnGenerator());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getSeaHeight()}.
     */
    @Test
    public void testGetSeaHeight() {
        assertEquals(0, s.getSeaHeight());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getMaxIslands()}.
     */
    @Test
    public void testGetMaxIslands() {
        assertEquals(-1, s.getMaxIslands());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getDefaultGameMode()}.
     */
    @Test
    public void testGetDefaultGameMode() {
        assertEquals(GameMode.SURVIVAL, s.getDefaultGameMode());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isNetherGenerate()}.
     */
    @Test
    public void testIsNetherGenerate() {
        assertTrue(s.isNetherGenerate());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isNetherIslands()}.
     */
    @Test
    public void testIsNetherIslands() {
        assertFalse(s.isNetherIslands());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isNetherRoof()}.
     */
    @Test
    public void testIsNetherRoof() {
        assertFalse(s.isNetherRoof());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getNetherSpawnRadius()}.
     */
    @Test
    public void testGetNetherSpawnRadius() {
        assertEquals(32, s.getNetherSpawnRadius());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isEndGenerate()}.
     */
    @Test
    public void testIsEndGenerate() {
        assertFalse(s.isEndGenerate());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isEndIslands()}.
     */
    @Test
    public void testIsEndIslands() {
        assertFalse(s.isEndIslands());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isDragonSpawn()}.
     */
    @Test
    public void testIsDragonSpawn() {
        assertFalse(s.isDragonSpawn());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getRemoveMobsWhitelist()}.
     */
    @Test
    public void testGetRemoveMobsWhitelist() {
        assertTrue(s.getRemoveMobsWhitelist().isEmpty());
     
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getWorldFlags()}.
     */
    @Test
    public void testGetWorldFlags() {
        assertTrue(s.getWorldFlags().isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getDefaultIslandFlagNames()}.
     */
    @Test
    public void testGetDefaultIslandFlagNames() {
        assertTrue(s.getDefaultIslandFlagNames().isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getDefaultIslandSettingNames()}.
     */
    @Test
    public void testGetDefaultIslandSettingNames() {
        assertTrue(s.getDefaultIslandSettingNames().isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getDefaultIslandFlags()}.
     */
    @SuppressWarnings("deprecation")
    @Test
    public void testGetDefaultIslandFlags() {
        assertTrue(s.getDefaultIslandFlags().isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getDefaultIslandSettings()}.
     */
    @SuppressWarnings("deprecation")
    @Test
    public void testGetDefaultIslandSettings() {
        assertTrue(s.getDefaultIslandSettings().isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getHiddenFlags()}.
     */
    @Test
    public void testGetHiddenFlags() {
        assertTrue(s.getHiddenFlags().isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getVisitorBannedCommands()}.
     */
    @Test
    public void testGetVisitorBannedCommands() {
        assertTrue(s.getVisitorBannedCommands().isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getFallingBannedCommands()}.
     */
    @Test
    public void testGetFallingBannedCommands() {
        assertTrue(s.getFallingBannedCommands().isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getMaxTeamSize()}.
     */
    @Test
    public void testGetMaxTeamSize() {
        assertEquals(4, s.getMaxTeamSize());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getMaxHomes()}.
     */
    @Test
    public void testGetMaxHomes() {
        assertEquals(5, s.getMaxHomes());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getResetLimit()}.
     */
    @Test
    public void testGetResetLimit() {
        assertEquals(-1, s.getResetLimit());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isLeaversLoseReset()}.
     */
    @Test
    public void testIsLeaversLoseReset() {
        assertFalse(s.isLeaversLoseReset());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isKickedKeepInventory()}.
     */
    @Test
    public void testIsKickedKeepInventory() {
        assertFalse(s.isKickedKeepInventory());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isCreateIslandOnFirstLoginEnabled()}.
     */
    @Test
    public void testIsCreateIslandOnFirstLoginEnabled() {
        assertFalse(s.isCreateIslandOnFirstLoginEnabled());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getCreateIslandOnFirstLoginDelay()}.
     */
    @Test
    public void testGetCreateIslandOnFirstLoginDelay() {
        assertEquals(5, s.getCreateIslandOnFirstLoginDelay());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isCreateIslandOnFirstLoginAbortOnLogout()}.
     */
    @Test
    public void testIsCreateIslandOnFirstLoginAbortOnLogout() {
        assertTrue(s.isCreateIslandOnFirstLoginAbortOnLogout());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isOnJoinResetMoney()}.
     */
    @Test
    public void testIsOnJoinResetMoney() {
        assertFalse(s.isOnJoinResetMoney());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isOnJoinResetInventory()}.
     */
    @Test
    public void testIsOnJoinResetInventory() {
        assertTrue(s.isOnJoinResetInventory());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isOnJoinResetEnderChest()}.
     */
    @Test
    public void testIsOnJoinResetEnderChest() {
        assertFalse(s.isOnJoinResetEnderChest());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isOnLeaveResetMoney()}.
     */
    @Test
    public void testIsOnLeaveResetMoney() {
        assertFalse(s.isOnLeaveResetMoney());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isOnLeaveResetInventory()}.
     */
    @Test
    public void testIsOnLeaveResetInventory() {
        assertFalse(s.isOnLeaveResetInventory());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isOnLeaveResetEnderChest()}.
     */
    @Test
    public void testIsOnLeaveResetEnderChest() {
        assertFalse(s.isOnLeaveResetEnderChest());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isDeathsCounted()}.
     */
    @Test
    public void testIsDeathsCounted() {
        assertTrue(s.isDeathsCounted());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isAllowSetHomeInNether()}.
     */
    @Test
    public void testIsAllowSetHomeInNether() {
        assertTrue(s.isAllowSetHomeInNether());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isAllowSetHomeInTheEnd()}.
     */
    @Test
    public void testIsAllowSetHomeInTheEnd() {
        assertTrue(s.isAllowSetHomeInTheEnd());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isRequireConfirmationToSetHomeInNether()}.
     */
    @Test
    public void testIsRequireConfirmationToSetHomeInNether() {
        assertTrue(s.isRequireConfirmationToSetHomeInNether());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isRequireConfirmationToSetHomeInTheEnd()}.
     */
    @Test
    public void testIsRequireConfirmationToSetHomeInTheEnd() {
        assertTrue(s.isRequireConfirmationToSetHomeInTheEnd());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getDeathsMax()}.
     */
    @Test
    public void testGetDeathsMax() {
        assertEquals(10, s.getDeathsMax());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isTeamJoinDeathReset()}.
     */
    @Test
    public void testIsTeamJoinDeathReset() {
        assertTrue(s.isTeamJoinDeathReset());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getGeoLimitSettings()}.
     */
    @Test
    public void testGetGeoLimitSettings() {
        assertTrue(s.getGeoLimitSettings().isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getIvSettings()}.
     */
    @Test
    public void testGetIvSettings() {
        assertTrue(s.getIvSettings().isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getResetEpoch()}.
     */
    @Test
    public void testGetResetEpoch() {
        assertEquals(0L, s.getResetEpoch());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setFriendlyName(java.lang.String)}.
     */
    @Test
    public void testSetFriendlyName() {
        s.setFriendlyName("test");
        assertEquals("test", s.getFriendlyName());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setWorldName(java.lang.String)}.
     */
    @Test
    public void testSetWorldName() {
        s.setWorldName("test");
        assertEquals("test", s.getWorldName());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setDifficulty(org.bukkit.Difficulty)}.
     */
    @Test
    public void testSetDifficulty() {
        s.setDifficulty(Difficulty.HARD);
        assertEquals(Difficulty.HARD, s.getDifficulty());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setIslandDistance(int)}.
     */
    @Test
    public void testSetIslandDistance() {
        s.setIslandDistance(12345);
        assertEquals(12345, s.getIslandDistance());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setIslandProtectionRange(int)}.
     */
    @Test
    public void testSetIslandProtectionRange() {
        s.setIslandProtectionRange(12345);
        assertEquals(12345, s.getIslandProtectionRange());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setIslandStartX(int)}.
     */
    @Test
    public void testSetIslandStartX() {
        s.setIslandStartX(12345);
        assertEquals(12345, s.getIslandStartX());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setIslandStartZ(int)}.
     */
    @Test
    public void testSetIslandStartZ() {
        s.setIslandStartZ(12345);
        assertEquals(12345, s.getIslandStartZ());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setIslandXOffset(int)}.
     */
    @Test
    public void testSetIslandXOffset() {
        s.setIslandXOffset(12345);
        assertEquals(12345, s.getIslandXOffset());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setIslandZOffset(int)}.
     */
    @Test
    public void testSetIslandZOffset() {
        s.setIslandZOffset(12345);
        assertEquals(12345, s.getIslandZOffset());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setIslandHeight(int)}.
     */
    @Test
    public void testSetIslandHeight() {
        s.setIslandHeight(12345);
        assertEquals(12345, s.getIslandHeight());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setUseOwnGenerator(boolean)}.
     */
    @Test
    public void testSetUseOwnGenerator() {
        s.setUseOwnGenerator(true);
        assertTrue(s.isUseOwnGenerator());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setSeaHeight(int)}.
     */
    @Test
    public void testSetSeaHeight() {
        s.setSeaHeight(12345);
        assertEquals(12345, s.getSeaHeight());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setMaxIslands(int)}.
     */
    @Test
    public void testSetMaxIslands() {
        s.setMaxIslands(12345);
        assertEquals(12345, s.getMaxIslands());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setDefaultGameMode(org.bukkit.GameMode)}.
     */
    @Test
    public void testSetDefaultGameMode() {
        s.setDefaultGameMode(GameMode.SPECTATOR);
        assertEquals(GameMode.SPECTATOR, s.getDefaultGameMode());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setNetherGenerate(boolean)}.
     */
    @Test
    public void testSetNetherGenerate() {
        s.setNetherGenerate(false);
        assertFalse(s.isNetherGenerate());
        s.setNetherGenerate(true);
        assertTrue(s.isNetherGenerate());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setNetherIslands(boolean)}.
     */
    @Test
    public void testSetNetherIslands() {
        s.setNetherIslands(false);
        assertFalse(s.isNetherIslands());
        s.setNetherIslands(true);
        assertTrue(s.isNetherIslands());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setNetherRoof(boolean)}.
     */
    @Test
    public void testSetNetherRoof() {
        s.setNetherRoof(false);
        assertFalse(s.isNetherRoof());
        s.setNetherRoof(true);
        assertTrue(s.isNetherRoof());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setNetherSpawnRadius(int)}.
     */
    @Test
    public void testSetNetherSpawnRadius() {
        s.setNetherSpawnRadius(12345);
        assertEquals(12345, s.getNetherSpawnRadius());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setEndGenerate(boolean)}.
     */
    @Test
    public void testSetEndGenerate() {
        s.setEndGenerate(false);
        assertFalse(s.isEndGenerate());
        s.setEndGenerate(true);
        assertTrue(s.isEndGenerate());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setEndIslands(boolean)}.
     */
    @Test
    public void testSetEndIslands() {
        s.setEndIslands(false);
        assertFalse(s.isEndIslands());
        s.setEndIslands(true);
        assertTrue(s.isEndIslands());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setRemoveMobsWhitelist(java.util.Set)}.
     */
    @Test
    public void testSetRemoveMobsWhitelist() {
        s.setRemoveMobsWhitelist(Collections.singleton(EntityType.AXOLOTL));
        assertTrue(s.getRemoveMobsWhitelist().contains(EntityType.AXOLOTL));
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setWorldFlags(java.util.Map)}.
     */
    @Test
    public void testSetWorldFlags() {
        s.setWorldFlags(Map.of("trueFlag", true, "falseFlag", false));
        assertTrue(s.getWorldFlags().get("trueFlag"));
        assertFalse(s.getWorldFlags().get("falseFlag"));
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setDefaultIslandFlagNames(java.util.Map)}.
     */
    @Test
    public void testSetDefaultIslandFlagNames() {
        s.setDefaultIslandFlagNames(Map.of("TEST", 500));
        assertTrue(s.getDefaultIslandFlagNames().get("TEST") == 500);
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setDefaultIslandSettingNames(java.util.Map)}.
     */
    @Test
    public void testSetDefaultIslandSettingNames() {
        s.setDefaultIslandSettingNames(Map.of("SETTING", 456));
        assertTrue(s.getDefaultIslandSettingNames().get("SETTING") == 456);
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setHiddenFlags(java.util.List)}.
     */
    @Test
    public void testSetHiddenFlags() {
        s.setHiddenFlags(List.of("FLAG1", "FLAG2"));
        assertTrue(s.getHiddenFlags().contains("FLAG2"));
        assertFalse(s.getHiddenFlags().contains("FLAG3"));
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setVisitorBannedCommands(java.util.List)}.
     */
    @Test
    public void testSetVisitorBannedCommands() {
        s.setVisitorBannedCommands(List.of("banned"));
        assertTrue(s.getVisitorBannedCommands().contains("banned"));
        assertFalse(s.getVisitorBannedCommands().contains("not-banned"));
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setFallingBannedCommands(java.util.List)}.
     */
    @Test
    public void testSetFallingBannedCommands() {
        s.setFallingBannedCommands(List.of("banned"));
        assertTrue(s.getFallingBannedCommands().contains("banned"));
        assertFalse(s.getFallingBannedCommands().contains("not-banned"));
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setMaxTeamSize(int)}.
     */
    @Test
    public void testSetMaxTeamSize() {
        s.setMaxTeamSize(12345);
        assertEquals(12345, s.getMaxTeamSize());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setMaxHomes(int)}.
     */
    @Test
    public void testSetMaxHomes() {
        s.setMaxHomes(12345);
        assertEquals(12345, s.getMaxHomes());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setResetLimit(int)}.
     */
    @Test
    public void testSetResetLimit() {
        s.setResetLimit(12345);
        assertEquals(12345, s.getResetLimit());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setLeaversLoseReset(boolean)}.
     */
    @Test
    public void testSetLeaversLoseReset() {
        s.setLeaversLoseReset(false);
        assertFalse(s.isLeaversLoseReset());
        s.setLeaversLoseReset(true);
        assertTrue(s.isLeaversLoseReset());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setKickedKeepInventory(boolean)}.
     */
    @Test
    public void testSetKickedKeepInventory() {
        s.setKickedKeepInventory(false);
        assertFalse(s.isKickedKeepInventory());
        s.setKickedKeepInventory(true);
        assertTrue(s.isKickedKeepInventory());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setOnJoinResetMoney(boolean)}.
     */
    @Test
    public void testSetOnJoinResetMoney() {
        s.setOnJoinResetMoney(false);
        assertFalse(s.isOnJoinResetMoney());
        s.setOnJoinResetMoney(true);
        assertTrue(s.isOnJoinResetMoney());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setOnJoinResetInventory(boolean)}.
     */
    @Test
    public void testSetOnJoinResetInventory() {
        s.setOnJoinResetInventory(false);
        assertFalse(s.isOnJoinResetInventory());
        s.setOnJoinResetInventory(true);
        assertTrue(s.isOnJoinResetInventory());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setOnJoinResetEnderChest(boolean)}.
     */
    @Test
    public void testSetOnJoinResetEnderChest() {
        s.setOnJoinResetEnderChest(false);
        assertFalse(s.isOnJoinResetEnderChest());
        s.setOnJoinResetEnderChest(true);
        assertTrue(s.isOnJoinResetEnderChest());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setOnLeaveResetMoney(boolean)}.
     */
    @Test
    public void testSetOnLeaveResetMoney() {
        s.setOnLeaveResetMoney(false);
        assertFalse(s.isOnLeaveResetMoney());
        s.setOnLeaveResetMoney(true);
        assertTrue(s.isOnLeaveResetMoney());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setOnLeaveResetInventory(boolean)}.
     */
    @Test
    public void testSetOnLeaveResetInventory() {
        s.setOnLeaveResetInventory(false);
        assertFalse(s.isOnLeaveResetInventory());
        s.setOnLeaveResetInventory(true);
        assertTrue(s.isOnLeaveResetInventory());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setOnLeaveResetEnderChest(boolean)}.
     */
    @Test
    public void testSetOnLeaveResetEnderChest() {
        s.setOnLeaveResetEnderChest(false);
        assertFalse(s.isOnLeaveResetEnderChest());
        s.setOnLeaveResetEnderChest(true);
        assertTrue(s.isOnLeaveResetEnderChest());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setCreateIslandOnFirstLoginEnabled(boolean)}.
     */
    @Test
    public void testSetCreateIslandOnFirstLoginEnabled() {
        s.setCreateIslandOnFirstLoginEnabled(false);
        assertFalse(s.isCreateIslandOnFirstLoginEnabled());
        s.setCreateIslandOnFirstLoginEnabled(true);
        assertTrue(s.isCreateIslandOnFirstLoginEnabled());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setCreateIslandOnFirstLoginDelay(int)}.
     */
    @Test
    public void testSetCreateIslandOnFirstLoginDelay() {
        s.setCreateIslandOnFirstLoginDelay(12345);
        assertEquals(12345, s.getCreateIslandOnFirstLoginDelay());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setCreateIslandOnFirstLoginAbortOnLogout(boolean)}.
     */
    @Test
    public void testSetCreateIslandOnFirstLoginAbortOnLogout() {
        s.setCreateIslandOnFirstLoginAbortOnLogout(false);
        assertFalse(s.isCreateIslandOnFirstLoginAbortOnLogout());
        s.setCreateIslandOnFirstLoginAbortOnLogout(true);
        assertTrue(s.isCreateIslandOnFirstLoginAbortOnLogout());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setDeathsCounted(boolean)}.
     */
    @Test
    public void testSetDeathsCounted() {
        s.setDeathsCounted(false);
        assertFalse(s.isDeathsCounted());
        s.setDeathsCounted(true);
        assertTrue(s.isDeathsCounted());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setDeathsMax(int)}.
     */
    @Test
    public void testSetDeathsMax() {
        s.setDeathsMax(12345);
        assertEquals(12345, s.getDeathsMax());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setTeamJoinDeathReset(boolean)}.
     */
    @Test
    public void testSetTeamJoinDeathReset() {
        s.setTeamJoinDeathReset(false);
        assertFalse(s.isTeamJoinDeathReset());
        s.setTeamJoinDeathReset(true);
        assertTrue(s.isTeamJoinDeathReset());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setGeoLimitSettings(java.util.List)}.
     */
    @Test
    public void testSetGeoLimitSettings() {
        s.setGeoLimitSettings(List.of("test"));
        assertTrue(s.getGeoLimitSettings().contains("test"));
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setIvSettings(java.util.List)}.
     */
    @Test
    public void testSetIvSettings() {
        s.setIvSettings(List.of("test"));
        assertTrue(s.getIvSettings().contains("test"));
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setAllowSetHomeInNether(boolean)}.
     */
    @Test
    public void testSetAllowSetHomeInNether() {
        s.setAllowSetHomeInNether(false);
        assertFalse(s.isAllowSetHomeInNether());
        s.setAllowSetHomeInNether(true);
        assertTrue(s.isAllowSetHomeInNether());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setAllowSetHomeInTheEnd(boolean)}.
     */
    @Test
    public void testSetAllowSetHomeInTheEnd() {
        s.setAllowSetHomeInTheEnd(false);
        assertFalse(s.isAllowSetHomeInTheEnd());
        s.setAllowSetHomeInTheEnd(true);
        assertTrue(s.isAllowSetHomeInTheEnd());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setRequireConfirmationToSetHomeInNether(boolean)}.
     */
    @Test
    public void testSetRequireConfirmationToSetHomeInNether() {
        s.setRequireConfirmationToSetHomeInNether(false);
        assertFalse(s.isRequireConfirmationToSetHomeInNether());
        s.setRequireConfirmationToSetHomeInNether(true);
        assertTrue(s.isRequireConfirmationToSetHomeInNether());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setRequireConfirmationToSetHomeInTheEnd(boolean)}.
     */
    @Test
    public void testSetRequireConfirmationToSetHomeInTheEnd() {
        s.setRequireConfirmationToSetHomeInTheEnd(false);
        assertFalse(s.isRequireConfirmationToSetHomeInTheEnd());
        s.setRequireConfirmationToSetHomeInTheEnd(true);
        assertTrue(s.isRequireConfirmationToSetHomeInTheEnd());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setResetEpoch(long)}.
     */
    @Test
    public void testSetResetEpoch() {
        s.setResetEpoch(12345);
        assertEquals(12345, s.getResetEpoch());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getPermissionPrefix()}.
     */
    @Test
    public void testGetPermissionPrefix() {
        assertEquals("aoneblock", s.getPermissionPrefix());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isWaterUnsafe()}.
     */
    @Test
    public void testIsWaterUnsafe() {
        assertFalse(s.isWaterUnsafe());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getDefaultBiome()}.
     */
    @Test
    public void testGetDefaultBiome() {
        assertEquals(Biome.PLAINS, s.getDefaultBiome());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setDefaultBiome(org.bukkit.block.Biome)}.
     */
    @Test
    public void testSetDefaultBiome() {
        assertEquals(Biome.PLAINS, s.getDefaultBiome());
        s.setDefaultBiome(Biome.BAMBOO_JUNGLE);
        assertEquals(Biome.BAMBOO_JUNGLE, s.getDefaultBiome());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getBanLimit()}.
     */
    @Test
    public void testGetBanLimit() {
        assertEquals(-1, s.getBanLimit());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setBanLimit(int)}.
     */
    @Test
    public void testSetBanLimit() {
        assertEquals(-1, s.getBanLimit());
        s.setBanLimit(12345);
        assertEquals(12345, s.getBanLimit());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getPlayerCommandAliases()}.
     */
    @Test
    public void testGetPlayerCommandAliases() {
        assertEquals("ob oneblock",s.getPlayerCommandAliases());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setPlayerCommandAliases(java.lang.String)}.
     */
    @Test
    public void testSetPlayerCommandAliases() {
        assertEquals("ob oneblock",s.getPlayerCommandAliases());
        s.setPlayerCommandAliases("aliases");
        assertEquals("aliases",s.getPlayerCommandAliases());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getAdminCommandAliases()}.
     */
    @Test
    public void testGetAdminCommandAliases() {
        assertEquals("oba obadmin",s.getAdminCommandAliases());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setAdminCommandAliases(java.lang.String)}.
     */
    @Test
    public void testSetAdminCommandAliases() {
        assertEquals("oba obadmin",s.getAdminCommandAliases());
        s.setAdminCommandAliases("aliases");
        assertEquals("aliases",s.getAdminCommandAliases());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isDeathsResetOnNewIsland()}.
     */
    @Test
    public void testIsDeathsResetOnNewIsland() {
        assertTrue(s.isDeathsResetOnNewIsland());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setDeathsResetOnNewIsland(boolean)}.
     */
    @Test
    public void testSetDeathsResetOnNewIsland() {
        s.setDeathsResetOnNewIsland(false);
        assertFalse(s.isDeathsResetOnNewIsland());
        s.setDeathsResetOnNewIsland(true);
        assertTrue(s.isDeathsResetOnNewIsland());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getOnJoinCommands()}.
     */
    @Test
    public void testGetOnJoinCommands() {
        assertTrue(s.getOnJoinCommands().isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setOnJoinCommands(java.util.List)}.
     */
    @Test
    public void testSetOnJoinCommands() {
        s.setOnJoinCommands(List.of("command", "do this"));
        assertEquals("do this", s.getOnJoinCommands().get(1));
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getOnLeaveCommands()}.
     */
    @Test
    public void testGetOnLeaveCommands() {
        assertTrue(s.getOnLeaveCommands().isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setOnLeaveCommands(java.util.List)}.
     */
    @Test
    public void testSetOnLeaveCommands() {
        s.setOnLeaveCommands(List.of("command", "do this"));
        assertEquals("do this", s.getOnLeaveCommands().get(1));
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getOnRespawnCommands()}.
     */
    @Test
    public void testGetOnRespawnCommands() {
        assertTrue(s.getOnRespawnCommands().isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setOnRespawnCommands(java.util.List)}.
     */
    @Test
    public void testSetOnRespawnCommands() {
        s.setOnRespawnCommands(List.of("command", "do this"));
        assertEquals("do this", s.getOnRespawnCommands().get(1));
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isOnJoinResetHealth()}.
     */
    @Test
    public void testIsOnJoinResetHealth() {
        assertTrue(s.isOnJoinResetHealth());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setOnJoinResetHealth(boolean)}.
     */
    @Test
    public void testSetOnJoinResetHealth() {
        s.setOnJoinResetHealth(false);
        assertFalse(s.isOnJoinResetHealth());
        s.setOnJoinResetHealth(true);
        assertTrue(s.isOnJoinResetHealth());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isOnJoinResetHunger()}.
     */
    @Test
    public void testIsOnJoinResetHunger() {
        assertTrue(s.isOnJoinResetHunger());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setOnJoinResetHunger(boolean)}.
     */
    @Test
    public void testSetOnJoinResetHunger() {
        s.setOnJoinResetHunger(false);
        assertFalse(s.isOnJoinResetHunger());
        s.setOnJoinResetHunger(true);
        assertTrue(s.isOnJoinResetHunger());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isOnJoinResetXP()}.
     */
    @Test
    public void testIsOnJoinResetXP() {
        assertTrue(s.isOnJoinResetXP());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setOnJoinResetXP(boolean)}.
     */
    @Test
    public void testSetOnJoinResetXP() {
        s.setOnJoinResetXP(false);
        assertFalse(s.isOnJoinResetXP());
        s.setOnJoinResetXP(true);
        assertTrue(s.isOnJoinResetXP());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isOnLeaveResetHealth()}.
     */
    @Test
    public void testIsOnLeaveResetHealth() {
        assertFalse(s.isOnLeaveResetHealth());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setOnLeaveResetHealth(boolean)}.
     */
    @Test
    public void testSetOnLeaveResetHealth() {
        s.setOnLeaveResetHealth(false);
        assertFalse(s.isOnLeaveResetHealth());
        s.setOnLeaveResetHealth(true);
        assertTrue(s.isOnLeaveResetHealth());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isOnLeaveResetHunger()}.
     */
    @Test
    public void testIsOnLeaveResetHunger() {
        assertFalse(s.isOnLeaveResetHunger());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setOnLeaveResetHunger(boolean)}.
     */
    @Test
    public void testSetOnLeaveResetHunger() {
        s.setOnLeaveResetHunger(false);
        assertFalse(s.isOnLeaveResetHunger());
        s.setOnLeaveResetHunger(true);
        assertTrue(s.isOnLeaveResetHunger());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isOnLeaveResetXP()}.
     */
    @Test
    public void testIsOnLeaveResetXP() {
        assertFalse(s.isOnLeaveResetXP());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setOnLeaveResetXP(boolean)}.
     */
    @Test
    public void testSetOnLeaveResetXP() {
        assertFalse(s.isOnLeaveResetXP());
        s.setOnLeaveResetXP(true);
        assertTrue(s.isOnLeaveResetXP());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isPasteMissingIslands()}.
     */
    @Test
    public void testIsPasteMissingIslands() {
        assertFalse(s.isPasteMissingIslands());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setPasteMissingIslands(boolean)}.
     */
    @Test
    public void testSetPasteMissingIslands() {
        assertFalse(s.isPasteMissingIslands());
        s.setPasteMissingIslands(true);
        assertTrue(s.isPasteMissingIslands());
        
        
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isTeleportPlayerToIslandUponIslandCreation()}.
     */
    @Test
    public void testIsTeleportPlayerToIslandUponIslandCreation() {
        assertTrue(s.isTeleportPlayerToIslandUponIslandCreation());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setTeleportPlayerToIslandUponIslandCreation(boolean)}.
     */
    @Test
    public void testSetTeleportPlayerToIslandUponIslandCreation() {
        assertTrue(s.isTeleportPlayerToIslandUponIslandCreation());
        s.setTeleportPlayerToIslandUponIslandCreation(false);
        assertFalse(s.isTeleportPlayerToIslandUponIslandCreation());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getSpawnLimitMonsters()}.
     */
    @Test
    public void testGetSpawnLimitMonsters() {
        assertEquals(-1, s.getSpawnLimitMonsters());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setSpawnLimitMonsters(int)}.
     */
    @Test
    public void testSetSpawnLimitMonsters() {
        assertEquals(-1, s.getSpawnLimitMonsters());
        s.setSpawnLimitMonsters(12345);
        assertEquals(12345, s.getSpawnLimitMonsters());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getSpawnLimitAnimals()}.
     */
    @Test
    public void testGetSpawnLimitAnimals() {
        assertEquals(-1, s.getSpawnLimitAnimals());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setSpawnLimitAnimals(int)}.
     */
    @Test
    public void testSetSpawnLimitAnimals() {
        assertEquals(-1, s.getSpawnLimitAnimals());
        s.setSpawnLimitAnimals(12345);
        assertEquals(12345, s.getSpawnLimitAnimals());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getSpawnLimitWaterAnimals()}.
     */
    @Test
    public void testGetSpawnLimitWaterAnimals() {
        assertEquals(-1, s.getSpawnLimitWaterAnimals());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setSpawnLimitWaterAnimals(int)}.
     */
    @Test
    public void testSetSpawnLimitWaterAnimals() {
        assertEquals(-1, s.getSpawnLimitWaterAnimals());
        s.setSpawnLimitWaterAnimals(12345);
        assertEquals(12345, s.getSpawnLimitWaterAnimals());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getSpawnLimitAmbient()}.
     */
    @Test
    public void testGetSpawnLimitAmbient() {
        assertEquals(-1, s.getSpawnLimitAmbient());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setSpawnLimitAmbient(int)}.
     */
    @Test
    public void testSetSpawnLimitAmbient() {
        assertEquals(-1, s.getSpawnLimitAmbient());
        s.setSpawnLimitAmbient(12345);
        assertEquals(12345, s.getSpawnLimitAmbient());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getTicksPerAnimalSpawns()}.
     */
    @Test
    public void testGetTicksPerAnimalSpawns() {
        assertEquals(-1, s.getTicksPerAnimalSpawns());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setTicksPerAnimalSpawns(int)}.
     */
    @Test
    public void testSetTicksPerAnimalSpawns() {
        assertEquals(-1, s.getTicksPerAnimalSpawns());
        s.setTicksPerAnimalSpawns(12345);
        assertEquals(12345, s.getTicksPerAnimalSpawns());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getTicksPerMonsterSpawns()}.
     */
    @Test
    public void testGetTicksPerMonsterSpawns() {
        assertEquals(-1, s.getTicksPerMonsterSpawns());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setTicksPerMonsterSpawns(int)}.
     */
    @Test
    public void testSetTicksPerMonsterSpawns() {
        assertEquals(-1, s.getTicksPerMonsterSpawns());
        s.setTicksPerMonsterSpawns(12345);
        assertEquals(12345, s.getTicksPerMonsterSpawns());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getMaxCoopSize()}.
     */
    @Test
    public void testGetMaxCoopSize() {
        assertEquals(4, s.getMaxCoopSize());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setMaxCoopSize(int)}.
     */
    @Test
    public void testSetMaxCoopSize() {
        s.setMaxCoopSize(12345);
        assertEquals(12345, s.getMaxCoopSize());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getMaxTrustSize()}.
     */
    @Test
    public void testGetMaxTrustSize() {
        assertEquals(4, s.getMaxTrustSize());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setMaxTrustSize(int)}.
     */
    @Test
    public void testSetMaxTrustSize() {
        s.setMaxTrustSize(12345);
        assertEquals(12345, s.getMaxTrustSize());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getMobWarning()}.
     */
    @Test
    public void testGetMobWarning() {
        assertEquals(5, s.getMobWarning());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setMobWarning(int)}.
     */
    @Test
    public void testSetMobWarning() {
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
    public void testIsWaterMobProtection() {
        assertTrue(s.isWaterMobProtection());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setWaterMobProtection(boolean)}.
     */
    @Test
    public void testSetWaterMobProtection() {
        s.setWaterMobProtection(false);
        assertFalse(s.isWaterMobProtection());
        s.setWaterMobProtection(true);
        assertTrue(s.isWaterMobProtection());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getDefaultNewPlayerAction()}.
     */
    @Test
    public void testGetDefaultNewPlayerAction() {
        assertEquals("create", s.getDefaultNewPlayerAction());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setDefaultNewPlayerAction(java.lang.String)}.
     */
    @Test
    public void testSetDefaultNewPlayerAction() {
        s.setDefaultNewPlayerAction("test");
        assertEquals("test", s.getDefaultNewPlayerAction());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getDefaultPlayerAction()}.
     */
    @Test
    public void testGetDefaultPlayerAction() {
        assertEquals("go", s.getDefaultPlayerAction());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setDefaultPlayerAction(java.lang.String)}.
     */
    @Test
    public void testSetDefaultPlayerAction() {
        s.setDefaultPlayerAction("test");
        assertEquals("test", s.getDefaultPlayerAction());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getMobLimitSettings()}.
     */
    @Test
    public void testGetMobLimitSettings() {
        assertTrue(s.getMobLimitSettings().isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setMobLimitSettings(java.util.List)}.
     */
    @Test
    public void testSetMobLimitSettings() {
        s.setMobLimitSettings(List.of("test"));
        assertEquals("test", s.getMobLimitSettings().get(0));
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isDropOnTop()}.
     */
    @Test
    public void testIsDropOnTop() {
        assertTrue(s.isDropOnTop());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setDropOnTop(boolean)}.
     */
    @Test
    public void testSetDropOnTop() {
        s.setDropOnTop(false);
        assertFalse(s.isDropOnTop());
        s.setDropOnTop(true);
        assertTrue(s.isDropOnTop());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getDefaultNetherBiome()}.
     */
    @Test
    public void testGetDefaultNetherBiome() {
        assertEquals(Biome.NETHER_WASTES, s.getDefaultNetherBiome());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setDefaultNetherBiome(org.bukkit.block.Biome)}.
     */
    @Test
    public void testSetDefaultNetherBiome() {
        assertEquals(Biome.NETHER_WASTES, s.getDefaultNetherBiome());
        s.setDefaultNetherBiome(Biome.BADLANDS);
        assertEquals(Biome.BADLANDS, s.getDefaultNetherBiome());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getDefaultEndBiome()}.
     */
    @Test
    public void testGetDefaultEndBiome() {
        assertEquals(Biome.THE_END, s.getDefaultEndBiome());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setDefaultEndBiome(org.bukkit.block.Biome)}.
     */
    @Test
    public void testSetDefaultEndBiome() {
        assertEquals(Biome.THE_END, s.getDefaultEndBiome());
        s.setDefaultEndBiome(Biome.BADLANDS);
        assertEquals(Biome.BADLANDS, s.getDefaultEndBiome());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isMakeNetherPortals()}.
     */
    @Test
    public void testIsMakeNetherPortals() {
        assertFalse(s.isMakeNetherPortals());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#isMakeEndPortals()}.
     */
    @Test
    public void testIsMakeEndPortals() {
        assertFalse(s.isMakeEndPortals());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setMakeNetherPortals(boolean)}.
     */
    @Test
    public void testSetMakeNetherPortals() {
        s.setMakeNetherPortals(false);
        assertFalse(s.isMakeNetherPortals());
        s.setMakeNetherPortals(true);
        assertTrue(s.isMakeNetherPortals());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setMakeEndPortals(boolean)}.
     */
    @Test
    public void testSetMakeEndPortals() {
        s.setMakeEndPortals(false);
        assertFalse(s.isMakeEndPortals());
        s.setMakeEndPortals(true);
        assertTrue(s.isMakeEndPortals());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getPercentCompleteSymbol()}.
     */
    @Test
    public void testGetPercentCompleteSymbol() {
        assertEquals("■", s.getPercentCompleteSymbol());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setPercentCompleteSymbol(java.lang.String)}.
     */
    @Test
    public void testSetPercentCompleteSymbol() {
        assertEquals("■", s.getPercentCompleteSymbol());
        s.setPercentCompleteSymbol("#");
        assertEquals("#", s.getPercentCompleteSymbol());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getCountCommand()}.
     */
    @Test
    public void testGetCountCommand() {
        assertEquals("count", s.getCountCommand());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setCountCommand(java.lang.String)}.
     */
    @Test
    public void testSetCountCommand() {
        s.setCountCommand("count123");
        assertEquals("count123", s.getCountCommand());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getPhasesCommand()}.
     */
    @Test
    public void testGetPhasesCommand() {
        assertEquals("phases", s.getPhasesCommand());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setPhasesCommand(java.lang.String)}.
     */
    @Test
    public void testSetPhasesCommand() {
        s.setPhasesCommand("count123");
        assertEquals("count123", s.getPhasesCommand());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getSetCountCommand()}.
     */
    @Test
    public void testGetSetCountCommand() {
        assertEquals("setCount", s.getSetCountCommand());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setSetCountCommand(java.lang.String)}.
     */
    @Test
    public void testSetSetCountCommand() {
        s.setSetCountCommand("count123");
        assertEquals("count123", s.getSetCountCommand());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#getRespawnBlockCommand()}.
     */
    @Test
    public void testGetRespawnBlockCommand() {
        assertEquals("respawnBlock check", s.getRespawnBlockCommand());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.Settings#setRespawnBlockCommand(java.lang.String)}.
     */
    @Test
    public void testSetRespawnBlockCommand() {
        s.setRespawnBlockCommand("respawn");
        assertEquals("respawn", s.getRespawnBlockCommand());
    }

}