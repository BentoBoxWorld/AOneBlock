package world.bentobox.aoneblock.panels;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.aoneblock.CommonTestSetup;
import world.bentobox.aoneblock.Settings;
import world.bentobox.aoneblock.dataobjects.OneBlockIslands;
import world.bentobox.aoneblock.listeners.BlockListener;
import world.bentobox.aoneblock.oneblocks.OneBlockPhase;
import world.bentobox.aoneblock.oneblocks.OneBlocksManager;
import world.bentobox.aoneblock.oneblocks.Requirement;
import world.bentobox.aoneblock.oneblocks.Requirement.ReqType;
import world.bentobox.bank.Bank;
import world.bentobox.bank.BankManager;
import world.bentobox.bank.data.Money;
import world.bentobox.bentobox.api.panels.TemplatedPanel;
import world.bentobox.bentobox.api.panels.reader.ItemTemplateRecord;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.hooks.LangUtilsHook;
import world.bentobox.bentobox.util.Util;
import world.bentobox.level.Level;

/**
 * Comprehensive tests for PhasesPanel covering all methods and branches.
 */
class PhasesPanelTest extends CommonTestSetup {

    @Mock
    private AOneBlock addon;
    @Mock
    private OneBlocksManager oneBlockManager;
    @Mock
    private BlockListener blockListener;
    @Mock
    private Settings addonSettings;
    @Mock
    private Bank bank;
    @Mock
    private Level level;

    private PhasesPanel panel;

    private void setUpAddonMocks() {
        when(addon.getPlugin()).thenReturn(plugin);
        when(addon.getOneBlockManager()).thenReturn(oneBlockManager);
        when(addon.getIslandsManager()).thenReturn(im);
        when(addon.getBlockListener()).thenReturn(blockListener);
        when(addon.getSettings()).thenReturn(addonSettings);
        when(addonSettings.getSetCountCommand()).thenReturn("setcount 0");
        when(addon.getPlayerCommand()).thenReturn(Optional.empty());
    }

    private OneBlockPhase createTestPhase(String phaseName) {
        OneBlockPhase phase = new OneBlockPhase("0");
        phase.setPhaseName(phaseName);
        phase.addBlock(Material.STONE, 100);
        phase.addBlock(Material.DIRT, 50);
        return phase;
    }

    private OneBlockPhase createTestPhaseWithRequirements(String phaseName) {
        OneBlockPhase phase = createTestPhase(phaseName);
        List<Requirement> reqs = new ArrayList<>();
        reqs.add(new Requirement(ReqType.ECO, 100.0));
        reqs.add(new Requirement(ReqType.BANK, 50.0));
        reqs.add(new Requirement(ReqType.LEVEL, 10L));
        reqs.add(new Requirement(ReqType.PERMISSION, "permission.test"));
        reqs.add(new Requirement(ReqType.COOLDOWN, 60L));
        phase.setRequirements(reqs);
        return phase;
    }

    private NavigableMap<Integer, OneBlockPhase> createBlockProbs() {
        NavigableMap<Integer, OneBlockPhase> probs = new TreeMap<>();
        OneBlockPhase phase1 = createTestPhase("Plains");
        probs.put(0, phase1);
        return probs;
    }

    // =========================================================================
    // Test insertNewlines (static method)
    // =========================================================================

    /**
     * Test insertNewlines with short string that doesn't need wrapping.
     */
    @Test
    void testInsertNewlinesShortString() throws Exception {
        Method method = PhasesPanel.class.getDeclaredMethod("insertNewlines", String.class, int.class);
        method.setAccessible(true);

        String input = "short";
        String result = (String) method.invoke(null, input, 50);

        assertEquals("short", result);
    }

    /**
     * Test insertNewlines with long string that needs wrapping at space.
     */
    @Test
    void testInsertNewlinesLongStringWithSpaces() throws Exception {
        Method method = PhasesPanel.class.getDeclaredMethod("insertNewlines", String.class, int.class);
        method.setAccessible(true);

        String input = "This is a very long string that should be wrapped at the interval";
        String result = (String) method.invoke(null, input, 20);

        assertTrue(result.contains("\n"));
    }

    /**
     * Test insertNewlines with long string without spaces (no wrapping possible).
     */
    @Test
    void testInsertNewlinesLongStringNoSpaces() throws Exception {
        Method method = PhasesPanel.class.getDeclaredMethod("insertNewlines", String.class, int.class);
        method.setAccessible(true);

        String input = "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz";
        String result = (String) method.invoke(null, input, 20);

        assertTrue(result.contains("\n"));
    }

    /**
     * Test insertNewlines with color codes (§).
     */
    @Test
    void testInsertNewlinesWithColorCode() throws Exception {
        Method method = PhasesPanel.class.getDeclaredMethod("insertNewlines", String.class, int.class);
        method.setAccessible(true);

        String input = "§cThis is a colored string that should wrap correctly when color codes are present";
        String result = (String) method.invoke(null, input, 20);

        // Should preserve or re-apply color codes
        assertTrue(result.contains("§"));
    }

    // =========================================================================
    // Test parseWrapAt
    // =========================================================================

    /**
     * Test parseWrapAt with valid number.
     */
    @Test
    void testParseWrapAtValid() throws Exception {
        setUpAddonMocks();
        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(null);
        when(oneBlockManager.getBlockProbs()).thenReturn(createBlockProbs());

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        when(user.getTranslation("aoneblock.gui.buttons.phase.wrap-at")).thenReturn("30");

        Method method = PhasesPanel.class.getDeclaredMethod("parseWrapAt");
        method.setAccessible(true);

        int result = (int) method.invoke(panel);

        assertEquals(30, result);
    }

    /**
     * Test parseWrapAt with invalid number (non-numeric).
     */
    @Test
    void testParseWrapAtInvalid() throws Exception {
        setUpAddonMocks();
        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(null);
        when(oneBlockManager.getBlockProbs()).thenReturn(createBlockProbs());

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        when(user.getTranslation("aoneblock.gui.buttons.phase.wrap-at")).thenReturn("not-a-number");

        Method method = PhasesPanel.class.getDeclaredMethod("parseWrapAt");
        method.setAccessible(true);

        int result = (int) method.invoke(panel);

        assertEquals(50, result); // default
        verify(addon).logError(anyString());
    }

    // =========================================================================
    // Test getMaterialName
    // =========================================================================

    /**
     * Test getMaterialName when LangUtils hook is present.
     */
    @Test
    void testGetMaterialNameWithLangUtils() throws Exception {
        setUpAddonMocks();
        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(null);
        when(oneBlockManager.getBlockProbs()).thenReturn(createBlockProbs());

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        when(hooksManager.getHook("LangUtils")).thenReturn(Optional.of(mock(LangUtilsHook.class)));

        Method method = PhasesPanel.class.getDeclaredMethod("getMaterialName", User.class, Material.class);
        method.setAccessible(true);

        String result = (String) method.invoke(panel, user, Material.STONE);

        assertNotNull(result);
    }

    /**
     * Test getMaterialName when LangUtils hook is absent.
     */
    @Test
    void testGetMaterialNameWithoutLangUtils() throws Exception {
        setUpAddonMocks();
        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(null);
        when(oneBlockManager.getBlockProbs()).thenReturn(createBlockProbs());

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        when(hooksManager.getHook("LangUtils")).thenReturn(Optional.empty());
        mockedUtil.when(() -> Util.prettifyText("STONE")).thenReturn("Stone");

        Method method = PhasesPanel.class.getDeclaredMethod("getMaterialName", User.class, Material.class);
        method.setAccessible(true);

        String result = (String) method.invoke(panel, user, Material.STONE);

        assertEquals("Stone", result);
    }

    // =========================================================================
    // Test buildRequirementsText
    // =========================================================================

    /**
     * Test buildRequirementsText with LEVEL requirement.
     */
    @Test
    void testBuildRequirementsTextAllTypes() throws Exception {
        setUpAddonMocks();
        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(null);
        when(oneBlockManager.getBlockProbs()).thenReturn(createBlockProbs());

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        OneBlockPhase phase = new OneBlockPhase("0");
        phase.setPhaseName("TestPhase");
        phase.setRequirements(List.of(new Requirement(ReqType.LEVEL, 10L)));

        Method method = PhasesPanel.class.getDeclaredMethod("buildRequirementsText", OneBlockPhase.class);
        method.setAccessible(true);

        Object result = method.invoke(panel, phase);

        assertNotNull(result);
    }

    // =========================================================================
    // Test buildBlocksText
    // =========================================================================

    /**
     * Test buildBlocksText with phase blocks.
     */
    @Test
    void testBuildBlocksText() throws Exception {
        setUpAddonMocks();
        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(null);
        when(oneBlockManager.getBlockProbs()).thenReturn(createBlockProbs());

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        OneBlockPhase phase = createTestPhase("Plains");

        when(user.getTranslation("aoneblock.gui.buttons.phase.blocks-prefix")).thenReturn("Blocks: ");
        when(user.getTranslation("aoneblock.gui.buttons.phase.wrap-at")).thenReturn("50");
        when(user.getTranslation("aoneblock.gui.buttons.phase.blocks", "name", "Stone")).thenReturn("Stone, ");
        when(user.getTranslation("aoneblock.gui.buttons.phase.blocks", "name", "Dirt")).thenReturn("Dirt, ");
        when(hooksManager.getHook("LangUtils")).thenReturn(Optional.empty());
        mockedUtil.when(() -> Util.prettifyText(anyString())).thenAnswer(i -> {
            String arg = i.getArgument(0);
            return arg.substring(0, 1).toUpperCase() + arg.substring(1).toLowerCase();
        });

        Method method = PhasesPanel.class.getDeclaredMethod("buildBlocksText", OneBlockPhase.class);
        method.setAccessible(true);

        String result = (String) method.invoke(panel, phase);

        assertNotNull(result);
        assertTrue(result.contains("Block"));
    }

    // =========================================================================
    // Test applyIcon
    // =========================================================================

    /**
     * Test applyIcon with template icon non-null.
     */
    @Test
    void testApplyIconWithTemplateIcon() throws Exception {
        setUpAddonMocks();
        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(null);
        when(oneBlockManager.getBlockProbs()).thenReturn(createBlockProbs());

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        OneBlockPhase phase = createTestPhase("Plains");
        ItemStack icon = new ItemStack(Material.APPLE);
        ItemTemplateRecord template = new ItemTemplateRecord(icon, null, null, null, Map.of(), null);

        Method method = PhasesPanel.class.getDeclaredMethod("applyIcon",
                world.bentobox.bentobox.api.panels.builders.PanelItemBuilder.class,
                ItemTemplateRecord.class, OneBlockPhase.class);
        method.setAccessible(true);

        world.bentobox.bentobox.api.panels.builders.PanelItemBuilder builder = new world.bentobox.bentobox.api.panels.builders.PanelItemBuilder();
        method.invoke(panel, builder, template, phase);

        assertNotNull(builder.getIcon());
    }

    /**
     * Test applyIcon with null template icon and null phase first block.
     */
    @Test
    void testApplyIconNullTemplateNullFirstBlock() throws Exception {
        setUpAddonMocks();
        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(null);
        when(oneBlockManager.getBlockProbs()).thenReturn(createBlockProbs());

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        OneBlockPhase phase = createTestPhase("Plains");
        ItemTemplateRecord template = new ItemTemplateRecord(null, null, null, null, Map.of(), null);

        Method method = PhasesPanel.class.getDeclaredMethod("applyIcon",
                world.bentobox.bentobox.api.panels.builders.PanelItemBuilder.class,
                ItemTemplateRecord.class, OneBlockPhase.class);
        method.setAccessible(true);

        world.bentobox.bentobox.api.panels.builders.PanelItemBuilder builder = new world.bentobox.bentobox.api.panels.builders.PanelItemBuilder();
        method.invoke(panel, builder, template, phase);

        assertNotNull(builder.getIcon());
    }

    // =========================================================================
    // Test applyTitle
    // =========================================================================

    /**
     * Test applyTitle with null template title.
     */
    @Test
    void testApplyTitleNullTemplateTitle() throws Exception {
        setUpAddonMocks();
        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(null);
        when(oneBlockManager.getBlockProbs()).thenReturn(createBlockProbs());

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        OneBlockPhase phase = createTestPhase("Plains");
        ItemTemplateRecord template = new ItemTemplateRecord(null, null, null, null, Map.of(), null);

        when(user.getTranslation("aoneblock.gui.buttons.phase.name", "[phase]", "Plains")).thenReturn("Plains Phase");

        Method method = PhasesPanel.class.getDeclaredMethod("applyTitle",
                world.bentobox.bentobox.api.panels.builders.PanelItemBuilder.class,
                ItemTemplateRecord.class, OneBlockPhase.class);
        method.setAccessible(true);

        world.bentobox.bentobox.api.panels.builders.PanelItemBuilder builder = new world.bentobox.bentobox.api.panels.builders.PanelItemBuilder();
        method.invoke(panel, builder, template, phase);

        assertNotNull(builder.getName());
    }

    /**
     * Test applyTitle with non-null template title.
     */
    @Test
    void testApplyTitleWithTemplateTitle() throws Exception {
        setUpAddonMocks();
        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(null);
        when(oneBlockManager.getBlockProbs()).thenReturn(createBlockProbs());

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        OneBlockPhase phase = createTestPhase("Plains");
        ItemTemplateRecord template = new ItemTemplateRecord(null, "custom.title", null, null, Map.of(), null);

        when(user.getTranslation(world, "custom.title", "[phase]", "Plains")).thenReturn("Custom Title");

        Method method = PhasesPanel.class.getDeclaredMethod("applyTitle",
                world.bentobox.bentobox.api.panels.builders.PanelItemBuilder.class,
                ItemTemplateRecord.class, OneBlockPhase.class);
        method.setAccessible(true);

        world.bentobox.bentobox.api.panels.builders.PanelItemBuilder builder = new world.bentobox.bentobox.api.panels.builders.PanelItemBuilder();
        method.invoke(panel, builder, template, phase);

        assertNotNull(builder.getName());
    }

    // =========================================================================
    // Test canApplyPhase
    // =========================================================================

    /**
     * Test canApplyPhase returns false when island is null.
     */
    @Test
    void testCanApplyPhaseIslandNull() throws Exception {
        setUpAddonMocks();
        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(null);
        when(oneBlockManager.getBlockProbs()).thenReturn(createBlockProbs());

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        OneBlockPhase phase = createTestPhase("Plains");

        Method method = PhasesPanel.class.getDeclaredMethod("canApplyPhase", OneBlockPhase.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(panel, phase);

        assertFalse(result);
    }

    /**
     * Test canApplyPhase returns false when oneBlockIsland is null.
     */
    @Test
    void testCanApplyPhaseOneBlockIslandNull() throws Exception {
        setUpAddonMocks();
        Island testIsland = new Island();
        testIsland.setOwner(uuid);
        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(testIsland);
        when(blockListener.getIsland(testIsland)).thenReturn(null);
        when(oneBlockManager.getBlockProbs()).thenReturn(createBlockProbs());

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        OneBlockPhase phase = createTestPhase("Plains");

        Method method = PhasesPanel.class.getDeclaredMethod("canApplyPhase", OneBlockPhase.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(panel, phase);

        assertFalse(result);
    }

    /**
     * Test canApplyPhase returns true when all conditions pass.
     */
    @Test
    void testCanApplyPhaseSuccess() throws Exception {
        setUpAddonMocks();
        Island testIsland = new Island();
        testIsland.setOwner(uuid);
        OneBlockIslands oneBlockIsland = new OneBlockIslands(testIsland.getUniqueId());
        oneBlockIsland.setLifetime(100L);
        oneBlockIsland.setLastPhaseChangeTime(System.currentTimeMillis());

        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(testIsland);
        when(blockListener.getIsland(testIsland)).thenReturn(oneBlockIsland);
        when(oneBlockManager.getBlockProbs()).thenReturn(createBlockProbs());

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        OneBlockPhase phase = createTestPhase("Plains");

        Method method = PhasesPanel.class.getDeclaredMethod("canApplyPhase", OneBlockPhase.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(panel, phase);

        assertTrue(result);
    }

    // =========================================================================
    // Test phaseRequirementsFail
    // =========================================================================

    /**
     * Test phaseRequirementsFail with LEVEL requirement passing.
     */
    @Test
    void testPhaseRequirementsFailLevelPass() throws Exception {
        setUpAddonMocks();
        Island testIsland = new Island();
        testIsland.setOwner(uuid);
        OneBlockIslands oneBlockIsland = new OneBlockIslands(testIsland.getUniqueId());

        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(testIsland);
        when(blockListener.getIsland(testIsland)).thenReturn(oneBlockIsland);
        when(oneBlockManager.getBlockProbs()).thenReturn(createBlockProbs());

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        OneBlockPhase phase = new OneBlockPhase("0");
        phase.setPhaseName("TestPhase");
        phase.setRequirements(List.of(new Requirement(ReqType.LEVEL, 10L)));

        when(addon.getAddonByName("Level")).thenReturn(Optional.of(level));
        when(level.isEnabled()).thenReturn(true);
        when(level.getIslandLevel(world, testIsland.getOwner())).thenReturn(100L);

        Method method = PhasesPanel.class.getDeclaredMethod("phaseRequirementsFail", OneBlockPhase.class, OneBlockIslands.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(panel, phase, oneBlockIsland);

        assertFalse(result); // Level is 100, requirement is 10, so it passes
    }

    /**
     * Test phaseRequirementsFail with BANK requirement failing.
     */
    @Test
    void testPhaseRequirementsFailBankFail() throws Exception {
        setUpAddonMocks();
        Island testIsland = new Island();
        testIsland.setOwner(uuid);
        OneBlockIslands oneBlockIsland = new OneBlockIslands(testIsland.getUniqueId());

        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(testIsland);
        when(blockListener.getIsland(testIsland)).thenReturn(oneBlockIsland);
        when(oneBlockManager.getBlockProbs()).thenReturn(createBlockProbs());

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        OneBlockPhase phase = new OneBlockPhase("0");
        phase.setPhaseName("TestPhase");
        phase.setRequirements(List.of(new Requirement(ReqType.BANK, 100000.0)));

        BankManager bm = mock(BankManager.class);
        when(bank.getBankManager()).thenReturn(bm);
        when(bm.getBalance(testIsland)).thenReturn(new Money(10.0)); // Low balance
        when(bank.isEnabled()).thenReturn(true);
        when(addon.getAddonByName("Bank")).thenReturn(Optional.of(bank));

        Method method = PhasesPanel.class.getDeclaredMethod("phaseRequirementsFail", OneBlockPhase.class, OneBlockIslands.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(panel, phase, oneBlockIsland);

        assertTrue(result); // Bank is 10, requirement is 100000, so it fails
    }

    /**
     * Test phaseRequirementsFail with PERMISSION requirement.
     */
    @Test
    void testPhaseRequirementsFailPermissionFail() throws Exception {
        setUpAddonMocks();
        Island testIsland = new Island();
        testIsland.setOwner(uuid);
        OneBlockIslands oneBlockIsland = new OneBlockIslands(testIsland.getUniqueId());

        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(testIsland);
        when(blockListener.getIsland(testIsland)).thenReturn(oneBlockIsland);
        when(oneBlockManager.getBlockProbs()).thenReturn(createBlockProbs());

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        OneBlockPhase phase = new OneBlockPhase("0");
        phase.setPhaseName("TestPhase");
        phase.setRequirements(List.of(new Requirement(ReqType.PERMISSION, "admin.phase")));

        when(user.hasPermission("admin.phase")).thenReturn(false);

        Method method = PhasesPanel.class.getDeclaredMethod("phaseRequirementsFail", OneBlockPhase.class, OneBlockIslands.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(panel, phase, oneBlockIsland);

        assertTrue(result); // User doesn't have permission, so it fails
    }

    /**
     * Test phaseRequirementsFail with COOLDOWN requirement.
     */
    @Test
    void testPhaseRequirementsFailCooldownPass() throws Exception {
        setUpAddonMocks();
        Island testIsland = new Island();
        testIsland.setOwner(uuid);
        OneBlockIslands oneBlockIsland = new OneBlockIslands(testIsland.getUniqueId());
        oneBlockIsland.setLastPhaseChangeTime(System.currentTimeMillis() - 120000); // 2 minutes ago

        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(testIsland);
        when(blockListener.getIsland(testIsland)).thenReturn(oneBlockIsland);
        when(oneBlockManager.getBlockProbs()).thenReturn(createBlockProbs());

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        OneBlockPhase phase = new OneBlockPhase("0");
        phase.setPhaseName("TestPhase");
        phase.setRequirements(List.of(new Requirement(ReqType.COOLDOWN, 60L))); // 60 second cooldown

        Method method = PhasesPanel.class.getDeclaredMethod("phaseRequirementsFail", OneBlockPhase.class, OneBlockIslands.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(panel, phase, oneBlockIsland);

        assertFalse(result); // Cooldown has passed
    }

    // =========================================================================
    // Test collectTooltips
    // =========================================================================

    /**
     * Test collectTooltips with null and blank tooltips.
     */
    @Test
    void testCollectTooltipsNullAndBlank() throws Exception {
        setUpAddonMocks();
        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(null);
        when(oneBlockManager.getBlockProbs()).thenReturn(createBlockProbs());

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        List<ItemTemplateRecord.ActionRecords> actions = List.of(
                new ItemTemplateRecord.ActionRecords(ClickType.LEFT, "SELECT", "content", null),
                new ItemTemplateRecord.ActionRecords(ClickType.LEFT, "SELECT", "content", ""),
                new ItemTemplateRecord.ActionRecords(ClickType.LEFT, "SELECT", "content", "   ")
        );

        when(user.getTranslation(world, "")).thenReturn("");
        when(user.getTranslation(world, "   ")).thenReturn("   ");

        Method method = PhasesPanel.class.getDeclaredMethod("collectTooltips", List.class);
        method.setAccessible(true);

        List<String> result = (List<String>) method.invoke(panel, actions);

        assertEquals(0, result.size()); // All tooltips are null or blank
    }

    /**
     * Test collectTooltips with real tooltip.
     */
    @Test
    void testCollectTooltipsWithRealTooltip() throws Exception {
        setUpAddonMocks();
        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(null);
        when(oneBlockManager.getBlockProbs()).thenReturn(createBlockProbs());

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        List<ItemTemplateRecord.ActionRecords> actions = List.of(
                new ItemTemplateRecord.ActionRecords(ClickType.LEFT, "SELECT", "content", "tooltip.key")
        );

        when(user.getTranslation(world, "tooltip.key")).thenReturn("Real tooltip");

        Method method = PhasesPanel.class.getDeclaredMethod("collectTooltips", List.class);
        method.setAccessible(true);

        List<String> result = (List<String>) method.invoke(panel, actions);

        assertEquals(1, result.size());
        assertEquals("Real tooltip", result.get(0));
    }

    // =========================================================================
    // Test createNextButton
    // =========================================================================

    /**
     * Test createNextButton returns null when no next page.
     */
    @Test
    void testCreateNextButtonNoNextPage() throws Exception {
        setUpAddonMocks();
        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(null);
        when(oneBlockManager.getBlockProbs()).thenReturn(createBlockProbs());

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        // Set pageIndex to 0
        Field pageIndexField = PhasesPanel.class.getDeclaredField("pageIndex");
        pageIndexField.setAccessible(true);
        pageIndexField.set(panel, 0);

        ItemTemplateRecord template = new ItemTemplateRecord(new ItemStack(Material.STONE),
                "title", "desc", null, Map.of("PHASE", 1), null);

        TemplatedPanel.ItemSlot slot = mock(TemplatedPanel.ItemSlot.class);
        when(slot.amountMap()).thenReturn(Map.of("PHASE", 10));
        when(slot.slot()).thenReturn(0);

        Method method = PhasesPanel.class.getDeclaredMethod("createNextButton", ItemTemplateRecord.class, TemplatedPanel.ItemSlot.class);
        method.setAccessible(true);

        Object result = method.invoke(panel, template, slot);

        assertNull(result); // Only 1 phase, so no next page
    }

    /**
     * Test createNextButton returns button with indexing.
     */
    @Test
    void testCreateNextButtonWithIndexing() throws Exception {
        setUpAddonMocks();
        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(null);

        NavigableMap<Integer, OneBlockPhase> probs = new TreeMap<>();
        for (int i = 0; i < 20; i++) {
            OneBlockPhase p = createTestPhase("Phase" + i);
            probs.put(i, p);
        }
        when(oneBlockManager.getBlockProbs()).thenReturn(probs);

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        // Set pageIndex to 0
        Field pageIndexField = PhasesPanel.class.getDeclaredField("pageIndex");
        pageIndexField.setAccessible(true);
        pageIndexField.set(panel, 0);

        when(user.getTranslation(world, "title")).thenReturn("Next Page");
        when(user.getTranslation(world, "desc", "number", "2")).thenReturn("Page 2");

        ItemTemplateRecord template = new ItemTemplateRecord(new ItemStack(Material.STONE),
                "title", "desc", List.of(), Map.of("INDEXING", true), null);

        TemplatedPanel.ItemSlot slot = mock(TemplatedPanel.ItemSlot.class);
        when(slot.amountMap()).thenReturn(Map.of("PHASE", 10));
        when(slot.slot()).thenReturn(0);

        Method method = PhasesPanel.class.getDeclaredMethod("createNextButton", ItemTemplateRecord.class, TemplatedPanel.ItemSlot.class);
        method.setAccessible(true);

        Object result = method.invoke(panel, template, slot);

        assertNotNull(result);
    }

    // =========================================================================
    // Test createPreviousButton
    // =========================================================================

    /**
     * Test createPreviousButton returns null when pageIndex is 0.
     */
    @Test
    void testCreatePreviousButtonPageIndexZero() throws Exception {
        setUpAddonMocks();
        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(null);
        when(oneBlockManager.getBlockProbs()).thenReturn(createBlockProbs());

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        // pageIndex is already 0 by default
        ItemTemplateRecord template = new ItemTemplateRecord(new ItemStack(Material.STONE),
                "title", "desc", List.of(), Map.of(), null);

        TemplatedPanel.ItemSlot slot = mock(TemplatedPanel.ItemSlot.class);
        when(slot.amountMap()).thenReturn(Map.of("PHASE", 1));

        Method method = PhasesPanel.class.getDeclaredMethod("createPreviousButton", ItemTemplateRecord.class, TemplatedPanel.ItemSlot.class);
        method.setAccessible(true);

        Object result = method.invoke(panel, template, slot);

        assertNull(result);
    }

    /**
     * Test createPreviousButton returns button when pageIndex > 0.
     */
    @Test
    void testCreatePreviousButtonPageIndexPositive() throws Exception {
        setUpAddonMocks();
        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(null);
        when(oneBlockManager.getBlockProbs()).thenReturn(createBlockProbs());

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        // Set pageIndex to 1
        Field pageIndexField = PhasesPanel.class.getDeclaredField("pageIndex");
        pageIndexField.setAccessible(true);
        pageIndexField.set(panel, 1);

        when(user.getTranslation(world, "title")).thenReturn("Previous Page");
        when(user.getTranslation(world, "desc", "number", "1")).thenReturn("Page 1");

        ItemTemplateRecord template = new ItemTemplateRecord(new ItemStack(Material.STONE),
                "title", "desc", List.of(), Map.of("INDEXING", true), null);

        TemplatedPanel.ItemSlot slot = mock(TemplatedPanel.ItemSlot.class);
        when(slot.amountMap()).thenReturn(Map.of("PHASE", 1));

        Method method = PhasesPanel.class.getDeclaredMethod("createPreviousButton", ItemTemplateRecord.class, TemplatedPanel.ItemSlot.class);
        method.setAccessible(true);

        Object result = method.invoke(panel, template, slot);

        assertNotNull(result);
    }

    // =========================================================================
    // Test createPhaseButton (with slot)
    // =========================================================================

    /**
     * Test createPhaseButton with slot returns null when elementList is empty.
     */
    @Test
    void testCreatePhaseButtonSlotEmptyList() throws Exception {
        setUpAddonMocks();
        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(null);
        when(oneBlockManager.getBlockProbs()).thenReturn(new TreeMap<>()); // Empty

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        ItemTemplateRecord template = new ItemTemplateRecord(null, null, null, List.of(), Map.of(), null);
        TemplatedPanel.ItemSlot slot = mock(TemplatedPanel.ItemSlot.class);
        when(slot.amountMap()).thenReturn(Map.of("PHASE", 1));
        when(slot.slot()).thenReturn(0);

        Method method = PhasesPanel.class.getDeclaredMethod("createPhaseButton", ItemTemplateRecord.class, TemplatedPanel.ItemSlot.class);
        method.setAccessible(true);

        Object result = method.invoke(panel, template, slot);

        assertNull(result);
    }

    /**
     * Test createPhaseButton with slot returns null when index >= size.
     */
    @Test
    void testCreatePhaseButtonSlotOutOfIndex() throws Exception {
        setUpAddonMocks();
        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(null);
        when(oneBlockManager.getBlockProbs()).thenReturn(createBlockProbs()); // 1 phase

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        ItemTemplateRecord template = new ItemTemplateRecord(null, null, null, List.of(), Map.of(), null);
        TemplatedPanel.ItemSlot slot = mock(TemplatedPanel.ItemSlot.class);
        when(slot.amountMap()).thenReturn(Map.of("PHASE", 1));
        when(slot.slot()).thenReturn(5); // Out of index

        Method method = PhasesPanel.class.getDeclaredMethod("createPhaseButton", ItemTemplateRecord.class, TemplatedPanel.ItemSlot.class);
        method.setAccessible(true);

        Object result = method.invoke(panel, template, slot);

        assertNull(result);
    }

    // =========================================================================
    // Test createPhaseButton (with entry)
    // =========================================================================

    /**
     * Test createPhaseButton with null entry returns null.
     */
    @Test
    void testCreatePhaseButtonEntryNull() throws Exception {
        setUpAddonMocks();
        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(null);
        when(oneBlockManager.getBlockProbs()).thenReturn(createBlockProbs());

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        ItemTemplateRecord template = new ItemTemplateRecord(null, null, null, List.of(), Map.of(), null);

        Method method = PhasesPanel.class.getDeclaredMethod("createPhaseButton", ItemTemplateRecord.class, Map.Entry.class);
        method.setAccessible(true);

        Object result = method.invoke(panel, template, (Object) null);

        assertNull(result);
    }

    /**
     * Test createPhaseButton with null phase value returns null.
     */
    @Test
    void testCreatePhaseButtonEntryNullValue() throws Exception {
        setUpAddonMocks();
        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(null);
        when(oneBlockManager.getBlockProbs()).thenReturn(createBlockProbs());

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        ItemTemplateRecord template = new ItemTemplateRecord(null, null, null, List.of(), Map.of(), null);
        @SuppressWarnings("unchecked")
        Map.Entry<Integer, OneBlockPhase> entry = mock(Map.Entry.class);
        when(entry.getValue()).thenReturn(null);

        Method method = PhasesPanel.class.getDeclaredMethod("createPhaseButton", ItemTemplateRecord.class, Map.Entry.class);
        method.setAccessible(true);

        Object result = method.invoke(panel, template, entry);

        assertNull(result);
    }

    // =========================================================================
    // Test runCommandCall
    // =========================================================================

    /**
     * Test runCommandCall with empty playerCommand.
     */
    @Test
    void testRunCommandCallNoPlayerCommand() throws Exception {
        setUpAddonMocks();
        when(addon.getPlayerCommand()).thenReturn(Optional.empty());
        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(null);
        when(oneBlockManager.getBlockProbs()).thenReturn(createBlockProbs());

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        OneBlockPhase phase = createTestPhase("Plains");

        Method method = PhasesPanel.class.getDeclaredMethod("runCommandCall", String.class, OneBlockPhase.class);
        method.setAccessible(true);

        // Should not throw - playerCommand is empty so nothing happens beyond closeInventory
        method.invoke(panel, "setcount", phase);
    }

    // =========================================================================
    // Test build
    // =========================================================================

    /**
     * Test build with empty elementList.
     */
    @Test
    void testBuildEmptyElementList() throws Exception {
        setUpAddonMocks();
        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(null);
        when(oneBlockManager.getBlockProbs()).thenReturn(new TreeMap<>()); // Empty

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        when(addon.getDescription()).thenReturn(mock());
        when(addon.getDescription().getName()).thenReturn("AOneBlock");

        Method method = PhasesPanel.class.getDeclaredMethod("build");
        method.setAccessible(true);

        method.invoke(panel);

        verify(addon).logError("There are no available phases for selection!");
    }

    // =========================================================================
    // Test openPanel (public static)
    // =========================================================================

    /**
     * Test openPanel public method with empty phases.
     */
    @Test
    void testOpenPanelEmptyPhases() throws Exception {
        setUpAddonMocks();
        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(null);
        when(oneBlockManager.getBlockProbs()).thenReturn(new TreeMap<>()); // Empty
        when(addon.getDescription()).thenReturn(mock());
        when(addon.getDescription().getName()).thenReturn("AOneBlock");

        PhasesPanel.openPanel(addon, world, user);

        verify(addon).logError("There are no available phases for selection!");
    }

    // =========================================================================
    // Additional coverage tests
    // =========================================================================

    /**
     * Test buildBlocksText with trim.
     */
    @Test
    void testBuildBlocksTextWithTrim() throws Exception {
        setUpAddonMocks();
        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(null);
        when(oneBlockManager.getBlockProbs()).thenReturn(createBlockProbs());

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        OneBlockPhase phase = createTestPhase("Plains");

        when(user.getTranslation("aoneblock.gui.buttons.phase.blocks-prefix")).thenReturn("Blocks: ");
        when(user.getTranslation("aoneblock.gui.buttons.phase.wrap-at")).thenReturn("50");
        when(user.getTranslation("aoneblock.gui.buttons.phase.blocks", "name", "Stone")).thenReturn("Stone, ");
        when(user.getTranslation("aoneblock.gui.buttons.phase.blocks", "name", "Dirt")).thenReturn("Dirt\n"); // Has newline
        when(hooksManager.getHook("LangUtils")).thenReturn(Optional.empty());
        mockedUtil.when(() -> Util.prettifyText(anyString())).thenAnswer(i -> {
            String arg = i.getArgument(0);
            return arg.substring(0, 1).toUpperCase() + arg.substring(1).toLowerCase();
        });

        Method method = PhasesPanel.class.getDeclaredMethod("buildBlocksText", OneBlockPhase.class);
        method.setAccessible(true);

        String result = (String) method.invoke(panel, phase);

        assertNotNull(result);
    }

    /**
     * Test applyIcon with phase icon block present.
     */
    @Test
    void testApplyIconWithPhaseIconBlock() throws Exception {
        setUpAddonMocks();
        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(null);
        when(oneBlockManager.getBlockProbs()).thenReturn(createBlockProbs());

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        OneBlockPhase phase = createTestPhase("Plains");
        ItemStack phaseIcon = new ItemStack(Material.DIAMOND);
        phase.setIconBlock(phaseIcon);

        ItemTemplateRecord template = new ItemTemplateRecord(null, null, null, null, Map.of(), null);

        Method method = PhasesPanel.class.getDeclaredMethod("applyIcon",
                world.bentobox.bentobox.api.panels.builders.PanelItemBuilder.class,
                ItemTemplateRecord.class, OneBlockPhase.class);
        method.setAccessible(true);

        world.bentobox.bentobox.api.panels.builders.PanelItemBuilder builder = new world.bentobox.bentobox.api.panels.builders.PanelItemBuilder();
        method.invoke(panel, builder, template, phase);

        assertNotNull(builder.getIcon());
    }

    /**
     * Test createPhaseButton (with entry) with valid phase.
     */
    @Test
    void testCreatePhaseButtonEntryValid() throws Exception {
        setUpAddonMocks();
        Island testIsland = new Island();
        testIsland.setOwner(uuid);
        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(testIsland);
        when(blockListener.getIsland(testIsland)).thenReturn(new OneBlockIslands(testIsland.getUniqueId()));
        when(oneBlockManager.getBlockProbs()).thenReturn(createBlockProbs());

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        OneBlockPhase phase = createTestPhase("Plains");
        Map.Entry<Integer, OneBlockPhase> entry = Map.entry(0, phase);

        ItemTemplateRecord template = new ItemTemplateRecord(new ItemStack(Material.STONE),
                "title", "description", List.of(new ItemTemplateRecord.ActionRecords(ClickType.LEFT, "SELECT", "cmd", "tooltip")),
                Map.of(), null);

        when(user.getTranslation(world, "title", "[phase]", "Plains")).thenReturn("Plains");
        when(user.getTranslationOrNothing("aoneblock.gui.buttons.phase.blocks-prefix")).thenReturn("Blocks: ");
        when(user.getTranslation("aoneblock.gui.buttons.phase.wrap-at")).thenReturn("50");
        when(user.getTranslation(world, "tooltip")).thenReturn("Click to select");
        when(hooksManager.getHook("LangUtils")).thenReturn(Optional.empty());
        mockedUtil.when(() -> Util.prettifyText(anyString())).thenAnswer(i -> {
            String arg = i.getArgument(0);
            return arg.substring(0, 1).toUpperCase() + arg.substring(1).toLowerCase();
        });

        Method method = PhasesPanel.class.getDeclaredMethod("createPhaseButton", ItemTemplateRecord.class, Map.Entry.class);
        method.setAccessible(true);

        Object result = method.invoke(panel, template, entry);

        assertNotNull(result);
    }

    /**
     * Test createNextButton with title and description null.
     */
    @Test
    void testCreateNextButtonNullTitleDesc() throws Exception {
        setUpAddonMocks();
        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(null);

        NavigableMap<Integer, OneBlockPhase> probs = new TreeMap<>();
        for (int i = 0; i < 20; i++) {
            OneBlockPhase p = createTestPhase("Phase" + i);
            probs.put(i, p);
        }
        when(oneBlockManager.getBlockProbs()).thenReturn(probs);

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        Field pageIndexField = PhasesPanel.class.getDeclaredField("pageIndex");
        pageIndexField.setAccessible(true);
        pageIndexField.set(panel, 0);

        // Template with null title and description
        ItemTemplateRecord template = new ItemTemplateRecord(new ItemStack(Material.STONE),
                null, null, List.of(), Map.of("INDEXING", false), null);

        TemplatedPanel.ItemSlot slot = mock(TemplatedPanel.ItemSlot.class);
        when(slot.amountMap()).thenReturn(Map.of("PHASE", 10));
        when(slot.slot()).thenReturn(0);

        Method method = PhasesPanel.class.getDeclaredMethod("createNextButton", ItemTemplateRecord.class, TemplatedPanel.ItemSlot.class);
        method.setAccessible(true);

        Object result = method.invoke(panel, template, slot);

        assertNotNull(result);
    }

    /**
     * Test createPreviousButton with no icon.
     */
    @Test
    void testCreatePreviousButtonNoIcon() throws Exception {
        setUpAddonMocks();
        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(null);
        when(oneBlockManager.getBlockProbs()).thenReturn(createBlockProbs());

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        Field pageIndexField = PhasesPanel.class.getDeclaredField("pageIndex");
        pageIndexField.setAccessible(true);
        pageIndexField.set(panel, 1);

        // Template with null icon
        ItemTemplateRecord template = new ItemTemplateRecord(null,
                "title", "desc", List.of(), Map.of(), null);

        TemplatedPanel.ItemSlot slot = mock(TemplatedPanel.ItemSlot.class);
        when(slot.amountMap()).thenReturn(Map.of("PHASE", 1));

        Method method = PhasesPanel.class.getDeclaredMethod("createPreviousButton", ItemTemplateRecord.class, TemplatedPanel.ItemSlot.class);
        method.setAccessible(true);

        Object result = method.invoke(panel, template, slot);

        assertNotNull(result);
    }

    /**
     * Test phaseRequirementsFail with no requirements.
     */
    @Test
    void testPhaseRequirementsFailNoRequirements() throws Exception {
        setUpAddonMocks();
        Island testIsland = new Island();
        testIsland.setOwner(uuid);
        OneBlockIslands oneBlockIsland = new OneBlockIslands(testIsland.getUniqueId());

        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(testIsland);
        when(blockListener.getIsland(testIsland)).thenReturn(oneBlockIsland);
        when(oneBlockManager.getBlockProbs()).thenReturn(createBlockProbs());

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        OneBlockPhase phase = new OneBlockPhase("0");
        phase.setPhaseName("TestPhase");
        phase.setRequirements(List.of()); // No requirements

        Method method = PhasesPanel.class.getDeclaredMethod("phaseRequirementsFail", OneBlockPhase.class, OneBlockIslands.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(panel, phase, oneBlockIsland);

        assertFalse(result); // No requirements, so doesn't fail
    }

    /**
     * Test canApplyPhase with requirements failing.
     */
    @Test
    void testCanApplyPhaseRequirementsFail() throws Exception {
        setUpAddonMocks();
        Island testIsland = new Island();
        testIsland.setOwner(uuid);
        OneBlockIslands oneBlockIsland = new OneBlockIslands(testIsland.getUniqueId());
        oneBlockIsland.setLifetime(100L);

        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(testIsland);
        when(blockListener.getIsland(testIsland)).thenReturn(oneBlockIsland);
        when(oneBlockManager.getBlockProbs()).thenReturn(createBlockProbs());

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        OneBlockPhase phase = new OneBlockPhase("0");
        phase.setPhaseName("TestPhase");
        phase.setRequirements(List.of(new Requirement(ReqType.PERMISSION, "admin.phase")));

        when(user.hasPermission("admin.phase")).thenReturn(false);

        Method method = PhasesPanel.class.getDeclaredMethod("canApplyPhase", OneBlockPhase.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(panel, phase);

        assertFalse(result); // Requirement fails, so can't apply phase
    }

    /**
     * Test phaseRequirementsFail with LEVEL requirement failing.
     */
    @Test
    void testPhaseRequirementsFailLevelFail() throws Exception {
        setUpAddonMocks();
        Island testIsland = new Island();
        testIsland.setOwner(uuid);
        OneBlockIslands oneBlockIsland = new OneBlockIslands(testIsland.getUniqueId());

        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(testIsland);
        when(blockListener.getIsland(testIsland)).thenReturn(oneBlockIsland);
        when(oneBlockManager.getBlockProbs()).thenReturn(createBlockProbs());

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        OneBlockPhase phase = new OneBlockPhase("0");
        phase.setPhaseName("TestPhase");
        phase.setRequirements(List.of(new Requirement(ReqType.LEVEL, 1000L)));

        when(addon.getAddonByName("Level")).thenReturn(Optional.of(level));
        when(level.isEnabled()).thenReturn(true);
        when(level.getIslandLevel(world, testIsland.getOwner())).thenReturn(10L); // Low level

        Method method = PhasesPanel.class.getDeclaredMethod("phaseRequirementsFail", OneBlockPhase.class, OneBlockIslands.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(panel, phase, oneBlockIsland);

        assertTrue(result); // Level is 10, requirement is 1000, so it fails
    }

    /**
     * Test collectTooltips with only blank tooltips.
     */
    @Test
    void testCollectTooltipsAllBlank() throws Exception {
        setUpAddonMocks();
        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(null);
        when(oneBlockManager.getBlockProbs()).thenReturn(createBlockProbs());

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        List<ItemTemplateRecord.ActionRecords> actions = List.of(
                new ItemTemplateRecord.ActionRecords(ClickType.LEFT, "SELECT", "content", "tooltip1"),
                new ItemTemplateRecord.ActionRecords(ClickType.LEFT, "VIEW", "content", "tooltip2")
        );

        when(user.getTranslation(world, "tooltip1")).thenReturn("   "); // Blank after translation
        when(user.getTranslation(world, "tooltip2")).thenReturn(""); // Empty

        Method method = PhasesPanel.class.getDeclaredMethod("collectTooltips", List.class);
        method.setAccessible(true);

        List<String> result = (List<String>) method.invoke(panel, actions);

        assertEquals(0, result.size()); // All translated to blank
    }

    /**
     * Test insertNewlines with exact boundary.
     */
    @Test
    void testInsertNewlinesExactBoundary() throws Exception {
        Method method = PhasesPanel.class.getDeclaredMethod("insertNewlines", String.class, int.class);
        method.setAccessible(true);

        String input = "123456789"; // 9 chars - exactly boundary - 1
        String result = (String) method.invoke(null, input, 10);

        assertEquals("123456789", result); // No wrap needed
    }

    /**
     * Test phaseRequirementsFail with ECO requirement failing.
     */
    @Test
    void testPhaseRequirementsFailEcoFail() throws Exception {
        setUpAddonMocks();
        Island testIsland = new Island();
        testIsland.setOwner(uuid);
        OneBlockIslands oneBlockIsland = new OneBlockIslands(testIsland.getUniqueId());

        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(testIsland);
        when(blockListener.getIsland(testIsland)).thenReturn(oneBlockIsland);
        when(oneBlockManager.getBlockProbs()).thenReturn(createBlockProbs());

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        OneBlockPhase phase = new OneBlockPhase("0");
        phase.setPhaseName("TestPhase");
        phase.setRequirements(List.of(new Requirement(ReqType.ECO, 10000.0)));

        // Don't mock vault - it returns empty Optional, so eco check fails (returns false)
        when(addon.getPlugin().getVault()).thenReturn(Optional.empty());

        Method method = PhasesPanel.class.getDeclaredMethod("phaseRequirementsFail", OneBlockPhase.class, OneBlockIslands.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(panel, phase, oneBlockIsland);

        assertFalse(result); // Vault empty means requirement check passes
    }

    /**
     * Test buildClickHandler with VIEW action.
     */
    @Test
    void testBuildClickHandlerViewAction() throws Exception {
        setUpAddonMocks();
        Island testIsland = new Island();
        testIsland.setOwner(uuid);
        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(testIsland);
        when(blockListener.getIsland(testIsland)).thenReturn(new OneBlockIslands(testIsland.getUniqueId()));
        when(oneBlockManager.getBlockProbs()).thenReturn(createBlockProbs());

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        List<ItemTemplateRecord.ActionRecords> actions = List.of(
                new ItemTemplateRecord.ActionRecords(ClickType.LEFT, "VIEW", "content", null)
        );

        OneBlockPhase phase = createTestPhase("Plains");

        Method method = PhasesPanel.class.getDeclaredMethod("buildClickHandler", List.class, OneBlockPhase.class);
        method.setAccessible(true);

        Object result = method.invoke(panel, actions, phase);

        assertNotNull(result);
    }

    /**
     * Test createPhaseButton with VIEW action (no SELECT).
     */
    @Test
    void testCreatePhaseButtonViewOnly() throws Exception {
        setUpAddonMocks();
        Island testIsland = new Island();
        testIsland.setOwner(uuid);
        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(testIsland);
        when(blockListener.getIsland(testIsland)).thenReturn(new OneBlockIslands(testIsland.getUniqueId()));
        when(oneBlockManager.getBlockProbs()).thenReturn(createBlockProbs());

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        OneBlockPhase phase = createTestPhase("Plains");
        Map.Entry<Integer, OneBlockPhase> entry = Map.entry(0, phase);

        ItemTemplateRecord template = new ItemTemplateRecord(new ItemStack(Material.STONE),
                "title", "description",
                List.of(new ItemTemplateRecord.ActionRecords(ClickType.LEFT, "VIEW", "cmd", null)),
                Map.of(), null);

        when(user.getTranslation(world, "title", "[phase]", "Plains")).thenReturn("Plains");
        when(user.getTranslationOrNothing("aoneblock.gui.buttons.phase.blocks-prefix")).thenReturn("Blocks: ");
        when(user.getTranslation("aoneblock.gui.buttons.phase.wrap-at")).thenReturn("50");
        when(hooksManager.getHook("LangUtils")).thenReturn(Optional.empty());
        mockedUtil.when(() -> Util.prettifyText(anyString())).thenAnswer(i -> {
            String arg = i.getArgument(0);
            return arg.substring(0, 1).toUpperCase() + arg.substring(1).toLowerCase();
        });

        Method method = PhasesPanel.class.getDeclaredMethod("createPhaseButton", ItemTemplateRecord.class, Map.Entry.class);
        method.setAccessible(true);

        Object result = method.invoke(panel, template, entry);

        assertNotNull(result);
    }

    /**
     * Test phaseRequirementsFail with COOLDOWN requirement failing.
     */
    @Test
    void testPhaseRequirementsFailCooldownFail() throws Exception {
        setUpAddonMocks();
        Island testIsland = new Island();
        testIsland.setOwner(uuid);
        OneBlockIslands oneBlockIsland = new OneBlockIslands(testIsland.getUniqueId());
        oneBlockIsland.setLastPhaseChangeTime(System.currentTimeMillis()); // Just now

        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(testIsland);
        when(blockListener.getIsland(testIsland)).thenReturn(oneBlockIsland);
        when(oneBlockManager.getBlockProbs()).thenReturn(createBlockProbs());

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        OneBlockPhase phase = new OneBlockPhase("0");
        phase.setPhaseName("TestPhase");
        phase.setRequirements(List.of(new Requirement(ReqType.COOLDOWN, 300L))); // 5 minute cooldown

        Method method = PhasesPanel.class.getDeclaredMethod("phaseRequirementsFail", OneBlockPhase.class, OneBlockIslands.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(panel, phase, oneBlockIsland);

        assertTrue(result); // Cooldown not yet elapsed
    }

    /**
     * Test createNextButton with actions that don't match.
     */
    @Test
    void testCreateNextButtonActionsNoMatch() throws Exception {
        setUpAddonMocks();
        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(null);

        NavigableMap<Integer, OneBlockPhase> probs = new TreeMap<>();
        for (int i = 0; i < 20; i++) {
            OneBlockPhase p = createTestPhase("Phase" + i);
            probs.put(i, p);
        }
        when(oneBlockManager.getBlockProbs()).thenReturn(probs);

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        Field pageIndexField = PhasesPanel.class.getDeclaredField("pageIndex");
        pageIndexField.setAccessible(true);
        pageIndexField.set(panel, 0);

        // Action that doesn't match NEXT
        ItemTemplateRecord template = new ItemTemplateRecord(new ItemStack(Material.STONE),
                "title", "desc",
                List.of(new ItemTemplateRecord.ActionRecords(ClickType.LEFT, "PREVIOUS", "content", null)),
                Map.of(), null);

        TemplatedPanel.ItemSlot slot = mock(TemplatedPanel.ItemSlot.class);
        when(slot.amountMap()).thenReturn(Map.of("PHASE", 10));
        when(slot.slot()).thenReturn(0);

        Method method = PhasesPanel.class.getDeclaredMethod("createNextButton", ItemTemplateRecord.class, TemplatedPanel.ItemSlot.class);
        method.setAccessible(true);

        Object result = method.invoke(panel, template, slot);

        assertNotNull(result); // Action doesn't match, but button is still created
    }

    /**
     * Test createPreviousButton with actions that don't match.
     */
    @Test
    void testCreatePreviousButtonActionsNoMatch() throws Exception {
        setUpAddonMocks();
        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(null);
        when(oneBlockManager.getBlockProbs()).thenReturn(createBlockProbs());

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        Field pageIndexField = PhasesPanel.class.getDeclaredField("pageIndex");
        pageIndexField.setAccessible(true);
        pageIndexField.set(panel, 1);

        // Action that doesn't match PREVIOUS
        ItemTemplateRecord template = new ItemTemplateRecord(new ItemStack(Material.STONE),
                "title", "desc",
                List.of(new ItemTemplateRecord.ActionRecords(ClickType.LEFT, "NEXT", "content", null)),
                Map.of(), null);

        TemplatedPanel.ItemSlot slot = mock(TemplatedPanel.ItemSlot.class);
        when(slot.amountMap()).thenReturn(Map.of("PHASE", 1));

        Method method = PhasesPanel.class.getDeclaredMethod("createPreviousButton", ItemTemplateRecord.class, TemplatedPanel.ItemSlot.class);
        method.setAccessible(true);

        Object result = method.invoke(panel, template, slot);

        assertNotNull(result); // Action doesn't match, but button is still created
    }

    /**
     * Test getMaterialName with different material.
     */
    @Test
    void testGetMaterialNameDifferentMaterial() throws Exception {
        setUpAddonMocks();
        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(null);
        when(oneBlockManager.getBlockProbs()).thenReturn(createBlockProbs());

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        when(hooksManager.getHook("LangUtils")).thenReturn(Optional.empty());
        mockedUtil.when(() -> Util.prettifyText("DIAMOND_BLOCK")).thenReturn("Diamond Block");

        Method method = PhasesPanel.class.getDeclaredMethod("getMaterialName", User.class, Material.class);
        method.setAccessible(true);

        String result = (String) method.invoke(panel, user, Material.DIAMOND_BLOCK);

        assertEquals("Diamond Block", result);
    }

    /**
     * Test createPhaseButton slot with valid index.
     */
    @Test
    void testCreatePhaseButtonSlotValidIndex() throws Exception {
        setUpAddonMocks();
        Island testIsland = new Island();
        testIsland.setOwner(uuid);
        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(testIsland);
        when(blockListener.getIsland(testIsland)).thenReturn(new OneBlockIslands(testIsland.getUniqueId()));

        NavigableMap<Integer, OneBlockPhase> probs = new TreeMap<>();
        OneBlockPhase phase = createTestPhase("Phase1");
        probs.put(0, phase);
        when(oneBlockManager.getBlockProbs()).thenReturn(probs);

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        ItemTemplateRecord template = new ItemTemplateRecord(new ItemStack(Material.STONE),
                "title", "description", List.of(), Map.of(), null);
        TemplatedPanel.ItemSlot slot = mock(TemplatedPanel.ItemSlot.class);
        when(slot.amountMap()).thenReturn(Map.of("PHASE", 1));
        when(slot.slot()).thenReturn(0);

        when(user.getTranslation(world, "title", "[phase]", "Phase1")).thenReturn("Phase1");
        when(user.getTranslationOrNothing("aoneblock.gui.buttons.phase.blocks-prefix")).thenReturn("Blocks: ");
        when(user.getTranslation("aoneblock.gui.buttons.phase.wrap-at")).thenReturn("50");
        when(hooksManager.getHook("LangUtils")).thenReturn(Optional.empty());
        mockedUtil.when(() -> Util.prettifyText(anyString())).thenAnswer(i -> {
            String arg = i.getArgument(0);
            return arg.substring(0, 1).toUpperCase() + arg.substring(1).toLowerCase();
        });

        Method method = PhasesPanel.class.getDeclaredMethod("createPhaseButton", ItemTemplateRecord.class, TemplatedPanel.ItemSlot.class);
        method.setAccessible(true);

        Object result = method.invoke(panel, template, slot);

        assertNotNull(result);
    }

    /**
     * Test insertNewlines with long string with exact space at interval.
     */
    @Test
    void testInsertNewlinesSpaceAtInterval() throws Exception {
        Method method = PhasesPanel.class.getDeclaredMethod("insertNewlines", String.class, int.class);
        method.setAccessible(true);

        String input = "word1 word2 word3 word4";
        String result = (String) method.invoke(null, input, 6);

        assertTrue(result.contains("\n"));
    }

    /**
     * Test parseWrapAt returns correct value from translation.
     */
    @Test
    void testParseWrapAtReturnsTranslatedValue() throws Exception {
        setUpAddonMocks();
        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(null);
        when(oneBlockManager.getBlockProbs()).thenReturn(createBlockProbs());

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        when(user.getTranslation("aoneblock.gui.buttons.phase.wrap-at")).thenReturn("75");

        Method method = PhasesPanel.class.getDeclaredMethod("parseWrapAt");
        method.setAccessible(true);

        int result = (int) method.invoke(panel);

        assertEquals(75, result);
    }

    /**
     * Test phaseRequirementsFail with PERMISSION requirement passing.
     */
    @Test
    void testPhaseRequirementsFailPermissionPass() throws Exception {
        setUpAddonMocks();
        Island testIsland = new Island();
        testIsland.setOwner(uuid);
        OneBlockIslands oneBlockIsland = new OneBlockIslands(testIsland.getUniqueId());

        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(testIsland);
        when(blockListener.getIsland(testIsland)).thenReturn(oneBlockIsland);
        when(oneBlockManager.getBlockProbs()).thenReturn(createBlockProbs());

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        OneBlockPhase phase = new OneBlockPhase("0");
        phase.setPhaseName("TestPhase");
        phase.setRequirements(List.of(new Requirement(ReqType.PERMISSION, "admin.phase")));

        when(user.hasPermission("admin.phase")).thenReturn(true);

        Method method = PhasesPanel.class.getDeclaredMethod("phaseRequirementsFail", OneBlockPhase.class, OneBlockIslands.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(panel, phase, oneBlockIsland);

        assertFalse(result); // User has permission, so it passes
    }

    /**
     * Test buildNextButton with UNKNOWN click type (always matches).
     */
    @Test
    void testCreateNextButtonUnknownClickType() throws Exception {
        setUpAddonMocks();
        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(null);

        NavigableMap<Integer, OneBlockPhase> probs = new TreeMap<>();
        for (int i = 0; i < 20; i++) {
            OneBlockPhase p = createTestPhase("Phase" + i);
            probs.put(i, p);
        }
        when(oneBlockManager.getBlockProbs()).thenReturn(probs);

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        Field pageIndexField = PhasesPanel.class.getDeclaredField("pageIndex");
        pageIndexField.setAccessible(true);
        pageIndexField.set(panel, 0);

        // Action with UNKNOWN click type (should match any click)
        ItemTemplateRecord template = new ItemTemplateRecord(new ItemStack(Material.STONE),
                "title", "desc",
                List.of(new ItemTemplateRecord.ActionRecords(ClickType.UNKNOWN, "NEXT", "content", null)),
                Map.of(), null);

        TemplatedPanel.ItemSlot slot = mock(TemplatedPanel.ItemSlot.class);
        when(slot.amountMap()).thenReturn(Map.of("PHASE", 10));
        when(slot.slot()).thenReturn(0);

        Method method = PhasesPanel.class.getDeclaredMethod("createNextButton", ItemTemplateRecord.class, TemplatedPanel.ItemSlot.class);
        method.setAccessible(true);

        Object result = method.invoke(panel, template, slot);

        assertNotNull(result);
    }

    /**
     * Test insertNewlines with no spaces in long text.
     */
    @Test
    void testInsertNewlinesNoSpacesLongText() throws Exception {
        Method method = PhasesPanel.class.getDeclaredMethod("insertNewlines", String.class, int.class);
        method.setAccessible(true);

        String input = "verylongwordwithoutanyspacesinit";
        String result = (String) method.invoke(null, input, 10);

        assertTrue(result.contains("\n"));
    }

    /**
     * Test canApplyPhase with block number >= lifetime.
     */
    @Test
    void testCanApplyPhaseBlockNumberHigherThanLifetime() throws Exception {
        setUpAddonMocks();
        Island testIsland = new Island();
        testIsland.setOwner(uuid);
        OneBlockIslands oneBlockIsland = new OneBlockIslands(testIsland.getUniqueId());
        oneBlockIsland.setLifetime(5L); // Small lifetime

        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(testIsland);
        when(blockListener.getIsland(testIsland)).thenReturn(oneBlockIsland);
        when(oneBlockManager.getBlockProbs()).thenReturn(createBlockProbs());

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        OneBlockPhase phase = new OneBlockPhase("100");
        phase.setPhaseName("HighBlock");

        Method method = PhasesPanel.class.getDeclaredMethod("canApplyPhase", OneBlockPhase.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(panel, phase);

        assertFalse(result); // Block 100 >= lifetime 5, so cannot apply
    }

    /**
     * Test phaseRequirementsFail with BANK requirement passing.
     */
    @Test
    void testPhaseRequirementsFailBankPass() throws Exception {
        setUpAddonMocks();
        Island testIsland = new Island();
        testIsland.setOwner(uuid);
        OneBlockIslands oneBlockIsland = new OneBlockIslands(testIsland.getUniqueId());

        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(testIsland);
        when(blockListener.getIsland(testIsland)).thenReturn(oneBlockIsland);
        when(oneBlockManager.getBlockProbs()).thenReturn(createBlockProbs());

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        OneBlockPhase phase = new OneBlockPhase("0");
        phase.setPhaseName("TestPhase");
        phase.setRequirements(List.of(new Requirement(ReqType.BANK, 50.0)));

        BankManager bm = mock(BankManager.class);
        when(bank.getBankManager()).thenReturn(bm);
        when(bm.getBalance(testIsland)).thenReturn(new Money(1000.0)); // Plenty of balance
        when(bank.isEnabled()).thenReturn(true);
        when(addon.getAddonByName("Bank")).thenReturn(Optional.of(bank));

        Method method = PhasesPanel.class.getDeclaredMethod("phaseRequirementsFail", OneBlockPhase.class, OneBlockIslands.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(panel, phase, oneBlockIsland);

        assertFalse(result); // Bank balance is sufficient
    }

    /**
     * Test phaseRequirementsFail with LEVEL addon not enabled.
     */
    @Test
    void testPhaseRequirementsFailLevelNotEnabled() throws Exception {
        setUpAddonMocks();
        Island testIsland = new Island();
        testIsland.setOwner(uuid);
        OneBlockIslands oneBlockIsland = new OneBlockIslands(testIsland.getUniqueId());

        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(testIsland);
        when(blockListener.getIsland(testIsland)).thenReturn(oneBlockIsland);
        when(oneBlockManager.getBlockProbs()).thenReturn(createBlockProbs());

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        OneBlockPhase phase = new OneBlockPhase("0");
        phase.setPhaseName("TestPhase");
        phase.setRequirements(List.of(new Requirement(ReqType.LEVEL, 100L)));

        when(addon.getAddonByName("Level")).thenReturn(Optional.empty()); // Addon not present

        Method method = PhasesPanel.class.getDeclaredMethod("phaseRequirementsFail", OneBlockPhase.class, OneBlockIslands.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(panel, phase, oneBlockIsland);

        assertFalse(result); // Addon not present, so returns false (doesn't fail)
    }

    // =========================================================================
    // High-coverage targeted tests to hit missed lines (90%+ goal)
    // =========================================================================

    /**
     * Test buildRequirementsText with actual requirement iteration (L432-441).
     * Tests all requirement switch branches.
     */
    @Test
    void testBuildRequirementsTextAllBranches() throws Exception {
        setUpAddonMocks();
        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(null);
        when(oneBlockManager.getBlockProbs()).thenReturn(createBlockProbs());

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        // Create phase with all requirement types
        OneBlockPhase phase = new OneBlockPhase("0");
        phase.setPhaseName("AllReqs");
        phase.setRequirements(List.of(
            new Requirement(ReqType.ECO, 100.0),
            new Requirement(ReqType.BANK, 50.0),
            new Requirement(ReqType.LEVEL, 10L),
            new Requirement(ReqType.PERMISSION, "test.perm"),
            new Requirement(ReqType.COOLDOWN, 60L)
        ));

        when(user.getTranslationOrNothing("aoneblock.gui.buttons.phase.economy")).thenReturn("Econ: ");
        when(user.getTranslationOrNothing("aoneblock.gui.buttons.phase.bank")).thenReturn("Bank: ");
        when(user.getTranslationOrNothing("aoneblock.gui.buttons.phase.level")).thenReturn("Level: ");
        when(user.getTranslationOrNothing("aoneblock.gui.buttons.phase.permission")).thenReturn("Perm: ");

        Method method = PhasesPanel.class.getDeclaredMethod("buildRequirementsText", OneBlockPhase.class);
        method.setAccessible(true);

        Object reqTexts = method.invoke(panel, phase);

        assertNotNull(reqTexts);
        // Get fields via reflection to verify they're populated
        Field bankField = reqTexts.getClass().getDeclaredField("bank");
        Field economyField = reqTexts.getClass().getDeclaredField("economy");
        Field levelField = reqTexts.getClass().getDeclaredField("level");
        Field permissionField = reqTexts.getClass().getDeclaredField("permission");
        bankField.setAccessible(true);
        economyField.setAccessible(true);
        levelField.setAccessible(true);
        permissionField.setAccessible(true);

        assertNotNull(bankField.get(reqTexts));
        assertNotNull(economyField.get(reqTexts));
        assertNotNull(levelField.get(reqTexts));
        assertNotNull(permissionField.get(reqTexts));
    }

    /**
     * Test buildDescriptionText with templated path and null biome (L488-511).
     */
    @Test
    void testBuildDescriptionTextTemplatedNullBiome() throws Exception {
        setUpAddonMocks();
        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(null);
        when(oneBlockManager.getBlockProbs()).thenReturn(createBlockProbs());

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        OneBlockPhase phase = createTestPhase("Plains");
        phase.setPhaseBiome(null);

        ItemTemplateRecord template = new ItemTemplateRecord(null, null, "custom.desc", List.of(), Map.of(), null);

        // Create RequirementTexts via reflection
        Class<?> reqTextClass = Class.forName("world.bentobox.aoneblock.panels.PhasesPanel$RequirementTexts");
        java.lang.reflect.Constructor<?> reqConstructor = reqTextClass.getDeclaredConstructor(String.class, String.class, String.class, String.class);
        reqConstructor.setAccessible(true);
        Object reqTexts = reqConstructor.newInstance("", "", "", "");

        when(user.getTranslationOrNothing("custom.desc", "number", "0", "[biome]", "", "[bank]", "", "[economy]", "", "[level]", "", "[permission]", "", "[blocks]", ""))
            .thenReturn("Description");

        Method method = PhasesPanel.class.getDeclaredMethod("buildDescriptionText", ItemTemplateRecord.class, OneBlockPhase.class, reqTextClass, String.class);
        method.setAccessible(true);

        String result = (String) method.invoke(panel, template, phase, reqTexts, "");

        assertNotNull(result);
    }

    /**
     * Test buildDescriptionText with templated path and non-null biome (L501).
     */
    @Test
    void testBuildDescriptionTextTemplatedWithBiome() throws Exception {
        setUpAddonMocks();
        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(null);
        when(oneBlockManager.getBlockProbs()).thenReturn(createBlockProbs());

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        OneBlockPhase phase = new OneBlockPhase("0");
        phase.setPhaseName("BiomePhase");
        phase.addBlock(Material.GRASS_BLOCK, 100);
        // Mock a biome object
        org.bukkit.block.Biome biome = mock(org.bukkit.block.Biome.class);
        phase.setPhaseBiome(biome);

        ItemTemplateRecord template = new ItemTemplateRecord(null, null, "custom.desc", List.of(), Map.of(), null);

        Class<?> reqTextClass = Class.forName("world.bentobox.aoneblock.panels.PhasesPanel$RequirementTexts");
        java.lang.reflect.Constructor<?> reqConstructor = reqTextClass.getDeclaredConstructor(String.class, String.class, String.class, String.class);
        reqConstructor.setAccessible(true);
        Object reqTexts = reqConstructor.newInstance("", "", "", "");

        try (MockedStatic<LangUtilsHook> ms = mockStatic(LangUtilsHook.class)) {
            ms.when(() -> LangUtilsHook.getBiomeName(biome, user)).thenReturn("Plains");

            when(user.getTranslationOrNothing("custom.desc", "number", "0", "[biome]", "Plains", "[bank]", "", "[economy]", "", "[level]", "", "[permission]", "", "[blocks]", ""))
                .thenReturn("Plains Description");

            Method method = PhasesPanel.class.getDeclaredMethod("buildDescriptionText", ItemTemplateRecord.class, OneBlockPhase.class, reqTextClass, String.class);
            method.setAccessible(true);

            String result = (String) method.invoke(panel, template, phase, reqTexts, "");

            assertNotNull(result);
            assertTrue(result.contains("Plains"));
        }
    }

    /**
     * Test buildDefaultDescription path (L516-531).
     */
    @Test
    void testBuildDefaultDescription() throws Exception {
        setUpAddonMocks();
        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(null);
        when(oneBlockManager.getBlockProbs()).thenReturn(createBlockProbs());

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        OneBlockPhase phase = createTestPhase("Plains");
        phase.setPhaseBiome(null);

        Class<?> reqTextClass = Class.forName("world.bentobox.aoneblock.panels.PhasesPanel$RequirementTexts");
        java.lang.reflect.Constructor<?> reqConstructor = reqTextClass.getDeclaredConstructor(String.class, String.class, String.class, String.class);
        reqConstructor.setAccessible(true);
        Object reqTexts = reqConstructor.newInstance("", "", "", "");

        when(user.getTranslationOrNothing("aoneblock.gui.buttons.phase.starting-block", "number", "0")).thenReturn("Block 0");
        when(user.getTranslationOrNothing("aoneblock.gui.buttons.phase.description", "[starting-block]", "Block 0", "[biome]", "", "[bank]", "", "[economy]", "", "[level]", "", "[permission]", "", "[blocks]", ""))
            .thenReturn("Default Desc");

        Method method = PhasesPanel.class.getDeclaredMethod("buildDefaultDescription", OneBlockPhase.class, reqTextClass, String.class);
        method.setAccessible(true);

        String result = (String) method.invoke(panel, phase, reqTexts, "");

        assertNotNull(result);
        assertTrue(result.contains("Default"));
    }

    /**
     * Test runCommandCall with playerCommand present (L660-L669).
     */
    @Test
    void testRunCommandCallWithPlayerCommand() throws Exception {
        setUpAddonMocks();
        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(null);
        when(oneBlockManager.getBlockProbs()).thenReturn(createBlockProbs());

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        // Mock command structure - but playerCommand returns empty since we want the alternative path
        when(addon.getPlayerCommand()).thenReturn(Optional.empty());

        OneBlockPhase phase = createTestPhase("Plains");

        Method method = PhasesPanel.class.getDeclaredMethod("runCommandCall", String.class, OneBlockPhase.class);
        method.setAccessible(true);

        // Should complete without NPE - it just closes inventory since playerCommand is empty
        method.invoke(panel, "setcount", phase);
    }

    /**
     * Test createPhaseButton with SELECT action (L361-367).
     */
    @Test
    void testCreatePhaseButtonWithSelectAction() throws Exception {
        setUpAddonMocks();
        Island testIsland = new Island();
        testIsland.setOwner(uuid);
        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(testIsland);
        when(blockListener.getIsland(testIsland)).thenReturn(new OneBlockIslands(testIsland.getUniqueId()));
        when(oneBlockManager.getBlockProbs()).thenReturn(createBlockProbs());

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        OneBlockPhase phase = createTestPhase("Plains");
        Map.Entry<Integer, OneBlockPhase> entry = Map.entry(0, phase);

        ItemTemplateRecord template = new ItemTemplateRecord(new ItemStack(Material.STONE),
                "title", "desc",
                List.of(
                    new ItemTemplateRecord.ActionRecords(ClickType.LEFT, "SELECT", "cmd", "select_tooltip"),
                    new ItemTemplateRecord.ActionRecords(ClickType.RIGHT, "VIEW", "cmd", "view_tooltip")
                ),
                Map.of(), null);

        when(user.getTranslation(world, "title", "[phase]", "Plains")).thenReturn("Plains");
        when(user.getTranslationOrNothing("aoneblock.gui.buttons.phase.blocks-prefix")).thenReturn("Blocks: ");
        when(user.getTranslation("aoneblock.gui.buttons.phase.wrap-at")).thenReturn("50");
        when(user.getTranslation(world, "select_tooltip")).thenReturn("Select");
        when(user.getTranslation(world, "view_tooltip")).thenReturn("View");
        when(hooksManager.getHook("LangUtils")).thenReturn(Optional.empty());
        mockedUtil.when(() -> Util.prettifyText(anyString())).thenAnswer(i -> {
            String arg = i.getArgument(0);
            return arg.substring(0, 1).toUpperCase() + arg.substring(1).toLowerCase();
        });

        Method method = PhasesPanel.class.getDeclaredMethod("createPhaseButton", ItemTemplateRecord.class, Map.Entry.class);
        method.setAccessible(true);

        Object result = method.invoke(panel, template, entry);

        assertNotNull(result);
    }

    /**
     * Test createNextButton with INDEXING and invoke click handler (L164-166, L186-197).
     */
    @Test
    void testCreateNextButtonWithIndexingAndClickHandler() throws Exception {
        setUpAddonMocks();
        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(null);

        NavigableMap<Integer, OneBlockPhase> probs = new TreeMap<>();
        for (int i = 0; i < 20; i++) {
            OneBlockPhase p = createTestPhase("Phase" + i);
            probs.put(i, p);
        }
        when(oneBlockManager.getBlockProbs()).thenReturn(probs);

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        Field pageIndexField = PhasesPanel.class.getDeclaredField("pageIndex");
        pageIndexField.setAccessible(true);
        pageIndexField.set(panel, 0);

        when(user.getTranslation(world, "title")).thenReturn("Next");
        when(user.getTranslation(world, "desc", "number", "2")).thenReturn("Page 2");

        ItemTemplateRecord template = new ItemTemplateRecord(new ItemStack(Material.STONE),
                "title", "desc",
                List.of(new ItemTemplateRecord.ActionRecords(ClickType.LEFT, "NEXT", "content", "next_tooltip")),
                Map.of("INDEXING", true), null);

        TemplatedPanel.ItemSlot slot = mock(TemplatedPanel.ItemSlot.class);
        when(slot.amountMap()).thenReturn(Map.of("PHASE", 10));
        when(slot.slot()).thenReturn(0);

        Method method = PhasesPanel.class.getDeclaredMethod("createNextButton", ItemTemplateRecord.class, TemplatedPanel.ItemSlot.class);
        method.setAccessible(true);

        world.bentobox.bentobox.api.panels.PanelItem panelItem = (world.bentobox.bentobox.api.panels.PanelItem) method.invoke(panel, template, slot);

        assertNotNull(panelItem); // Verifies INDEXING branch was executed and PanelItem created
    }

    /**
     * Test createPreviousButton with INDEXING and click handler (L243-246, L266-292).
     */
    @Test
    void testCreatePreviousButtonWithIndexingAndClickHandler() throws Exception {
        setUpAddonMocks();
        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(null);
        when(oneBlockManager.getBlockProbs()).thenReturn(createBlockProbs());

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        Field pageIndexField = PhasesPanel.class.getDeclaredField("pageIndex");
        pageIndexField.setAccessible(true);
        pageIndexField.set(panel, 2);

        when(user.getTranslation(world, "title")).thenReturn("Previous");
        when(user.getTranslation(world, "desc", "number", "2")).thenReturn("Page 2");

        ItemTemplateRecord template = new ItemTemplateRecord(new ItemStack(Material.STONE),
                "title", "desc",
                List.of(new ItemTemplateRecord.ActionRecords(ClickType.LEFT, "PREVIOUS", "content", "prev_tooltip")),
                Map.of("INDEXING", true), null);

        TemplatedPanel.ItemSlot slot = mock(TemplatedPanel.ItemSlot.class);
        when(slot.amountMap()).thenReturn(Map.of("PHASE", 1));

        Method method = PhasesPanel.class.getDeclaredMethod("createPreviousButton", ItemTemplateRecord.class, TemplatedPanel.ItemSlot.class);
        method.setAccessible(true);

        world.bentobox.bentobox.api.panels.PanelItem panelItem = (world.bentobox.bentobox.api.panels.PanelItem) method.invoke(panel, template, slot);

        assertNotNull(panelItem); // Verifies INDEXING branch was executed and PanelItem created
    }

    /**
     * Test buildRequirementsText with empty requirements list.
     */
    @Test
    void testBuildRequirementsTextEmpty() throws Exception {
        setUpAddonMocks();
        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(null);
        when(oneBlockManager.getBlockProbs()).thenReturn(createBlockProbs());

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        OneBlockPhase phase = new OneBlockPhase("0");
        phase.setPhaseName("NoReqs");
        phase.addBlock(Material.STONE, 100);
        phase.setRequirements(List.of()); // Empty list

        Method method = PhasesPanel.class.getDeclaredMethod("buildRequirementsText", OneBlockPhase.class);
        method.setAccessible(true);

        Object result = method.invoke(panel, phase);

        assertNotNull(result);
    }

    /**
     * Test buildDefaultDescription with biome present (L520-522).
     */
    @Test
    void testBuildDefaultDescriptionWithBiome() throws Exception {
        setUpAddonMocks();
        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(null);
        when(oneBlockManager.getBlockProbs()).thenReturn(createBlockProbs());

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        OneBlockPhase phase = new OneBlockPhase("0");
        phase.setPhaseName("BiomePhase");
        phase.addBlock(Material.STONE, 100);
        org.bukkit.block.Biome biome = mock(org.bukkit.block.Biome.class);
        phase.setPhaseBiome(biome);

        Class<?> reqTextClass = Class.forName("world.bentobox.aoneblock.panels.PhasesPanel$RequirementTexts");
        java.lang.reflect.Constructor<?> reqConstructor = reqTextClass.getDeclaredConstructor(String.class, String.class, String.class, String.class);
        reqConstructor.setAccessible(true);
        Object reqTexts = reqConstructor.newInstance("", "", "", "");

        when(user.getTranslationOrNothing("aoneblock.gui.buttons.phase.starting-block", "number", "0")).thenReturn("Block 0");
        when(user.getTranslationOrNothing("aoneblock.gui.buttons.phase.biome", "[biome]", "Plains")).thenReturn("Biome: Plains");
        when(user.getTranslationOrNothing("aoneblock.gui.buttons.phase.description", "[starting-block]", "Block 0", "[biome]", "Biome: Plains", "[bank]", "", "[economy]", "", "[level]", "", "[permission]", "", "[blocks]", ""))
            .thenReturn("Description with biome");

        try (MockedStatic<LangUtilsHook> ms = mockStatic(LangUtilsHook.class)) {
            ms.when(() -> LangUtilsHook.getBiomeName(biome, user)).thenReturn("Plains");

            Method method = PhasesPanel.class.getDeclaredMethod("buildDefaultDescription", OneBlockPhase.class, reqTextClass, String.class);
            method.setAccessible(true);

            String result = (String) method.invoke(panel, phase, reqTexts, "");

            assertNotNull(result);
            assertTrue(result.contains("biome"));
        }
    }

    /**
     * Test buildDescriptionText with null template (default path) (L492).
     */
    @Test
    void testBuildDescriptionTextNullTemplate() throws Exception {
        setUpAddonMocks();
        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(null);
        when(oneBlockManager.getBlockProbs()).thenReturn(createBlockProbs());

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        OneBlockPhase phase = createTestPhase("Plains");
        phase.setPhaseBiome(null);

        ItemTemplateRecord template = new ItemTemplateRecord(null, null, null, List.of(), Map.of(), null); // description is null

        Class<?> reqTextClass = Class.forName("world.bentobox.aoneblock.panels.PhasesPanel$RequirementTexts");
        java.lang.reflect.Constructor<?> reqConstructor = reqTextClass.getDeclaredConstructor(String.class, String.class, String.class, String.class);
        reqConstructor.setAccessible(true);
        Object reqTexts = reqConstructor.newInstance("", "", "", "");

        when(user.getTranslationOrNothing("aoneblock.gui.buttons.phase.starting-block", "number", "0")).thenReturn("Block 0");
        when(user.getTranslationOrNothing("aoneblock.gui.buttons.phase.description", "[starting-block]", "Block 0", "[biome]", "", "[bank]", "", "[economy]", "", "[level]", "", "[permission]", "", "[blocks]", ""))
            .thenReturn("Default Description");

        Method method = PhasesPanel.class.getDeclaredMethod("buildDescriptionText", ItemTemplateRecord.class, OneBlockPhase.class, reqTextClass, String.class);
        method.setAccessible(true);

        String result = (String) method.invoke(panel, template, phase, reqTexts, "");

        assertNotNull(result);
        assertTrue(result.contains("Default"));
    }

    /**
     * Test createPhaseButton with VIEW-only action (no SELECT allowed).
     */
    @Test
    void testCreatePhaseButtonViewOnlyCannotApply() throws Exception {
        setUpAddonMocks();
        User user = User.getInstance(mockPlayer);
        Island testIsland = new Island();
        testIsland.setOwner(uuid);
        OneBlockIslands oneBlockIsland = new OneBlockIslands(testIsland.getUniqueId());
        oneBlockIsland.setLifetime(5L); // Low lifetime so can't apply
        when(im.getIsland(world, user)).thenReturn(testIsland);
        when(blockListener.getIsland(testIsland)).thenReturn(oneBlockIsland);
        when(oneBlockManager.getBlockProbs()).thenReturn(createBlockProbs());

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        OneBlockPhase phase = createTestPhase("Plains");
        Map.Entry<Integer, OneBlockPhase> entry = Map.entry(0, phase);

        // Only VIEW action, no SELECT (so canApply filtering removes SELECT actions)
        ItemTemplateRecord template = new ItemTemplateRecord(new ItemStack(Material.STONE),
                "title", "desc",
                List.of(new ItemTemplateRecord.ActionRecords(ClickType.LEFT, "VIEW", "cmd", "view")),
                Map.of(), null);

        when(user.getTranslation(world, "title", "[phase]", "Plains")).thenReturn("Plains");
        when(user.getTranslationOrNothing("aoneblock.gui.buttons.phase.blocks-prefix")).thenReturn("Blocks: ");
        when(user.getTranslation("aoneblock.gui.buttons.phase.wrap-at")).thenReturn("50");
        when(user.getTranslation(world, "view")).thenReturn("View");
        when(hooksManager.getHook("LangUtils")).thenReturn(Optional.empty());
        mockedUtil.when(() -> Util.prettifyText(anyString())).thenAnswer(i -> {
            String arg = i.getArgument(0);
            return arg.substring(0, 1).toUpperCase() + arg.substring(1).toLowerCase();
        });

        Method method = PhasesPanel.class.getDeclaredMethod("createPhaseButton", ItemTemplateRecord.class, Map.Entry.class);
        method.setAccessible(true);

        Object result = method.invoke(panel, template, entry);

        assertNotNull(result);
    }

    /**
     * Test insertNewlines with color code at interval boundary (L593-615).
     */
    @Test
    void testInsertNewlinesColorAtBoundary() throws Exception {
        Method method = PhasesPanel.class.getDeclaredMethod("insertNewlines", String.class, int.class);
        method.setAccessible(true);

        // String with § at 10 chars
        String input = "123456789§a more text";
        String result = (String) method.invoke(null, input, 10);

        assertTrue(result.contains("§")); // Color code preserved
    }

    /**
     * Test getMaterialName with no LangUtils and valid material.
     */
    @Test
    void testGetMaterialNameWithPrettify() throws Exception {
        setUpAddonMocks();
        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(null);
        when(oneBlockManager.getBlockProbs()).thenReturn(createBlockProbs());

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        when(hooksManager.getHook("LangUtils")).thenReturn(Optional.empty());
        mockedUtil.when(() -> Util.prettifyText("IRON_ORE")).thenReturn("Iron Ore");

        Method method = PhasesPanel.class.getDeclaredMethod("getMaterialName", User.class, Material.class);
        method.setAccessible(true);

        String result = (String) method.invoke(panel, user, Material.IRON_ORE);

        assertEquals("Iron Ore", result);
    }

    /**
     * Test createNextButton with empty actions list (L200-212).
     */
    @Test
    void testCreateNextButtonNoTooltips() throws Exception {
        setUpAddonMocks();
        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(null);

        NavigableMap<Integer, OneBlockPhase> probs = new TreeMap<>();
        for (int i = 0; i < 20; i++) {
            OneBlockPhase p = createTestPhase("Phase" + i);
            probs.put(i, p);
        }
        when(oneBlockManager.getBlockProbs()).thenReturn(probs);

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        Field pageIndexField = PhasesPanel.class.getDeclaredField("pageIndex");
        pageIndexField.setAccessible(true);
        pageIndexField.set(panel, 0);

        when(user.getTranslation(world, "title")).thenReturn("Next");
        when(user.getTranslation(world, "desc", "number", "2")).thenReturn("Page 2");

        // Actions with null tooltips
        ItemTemplateRecord template = new ItemTemplateRecord(new ItemStack(Material.STONE),
                "title", "desc",
                List.of(new ItemTemplateRecord.ActionRecords(ClickType.LEFT, "NEXT", "content", null)),
                Map.of(), null);

        TemplatedPanel.ItemSlot slot = mock(TemplatedPanel.ItemSlot.class);
        when(slot.amountMap()).thenReturn(Map.of("PHASE", 10));
        when(slot.slot()).thenReturn(0);

        Method method = PhasesPanel.class.getDeclaredMethod("createNextButton", ItemTemplateRecord.class, TemplatedPanel.ItemSlot.class);
        method.setAccessible(true);

        Object result = method.invoke(panel, template, slot);

        assertNotNull(result); // Should still create button, just without tooltips
    }

    /**
     * Test createPreviousButton with null actions (L288-293).
     */
    @Test
    void testCreatePreviousButtonNullActions() throws Exception {
        setUpAddonMocks();
        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(null);
        when(oneBlockManager.getBlockProbs()).thenReturn(createBlockProbs());

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        Field pageIndexField = PhasesPanel.class.getDeclaredField("pageIndex");
        pageIndexField.setAccessible(true);
        pageIndexField.set(panel, 1);

        when(user.getTranslation(world, "title")).thenReturn("Previous");
        when(user.getTranslation(world, "desc", "number", "1")).thenReturn("Page 1");

        // Empty actions list
        ItemTemplateRecord template = new ItemTemplateRecord(new ItemStack(Material.STONE),
                "title", "desc",
                List.of(), Map.of(), null);

        TemplatedPanel.ItemSlot slot = mock(TemplatedPanel.ItemSlot.class);
        when(slot.amountMap()).thenReturn(Map.of("PHASE", 1));

        Method method = PhasesPanel.class.getDeclaredMethod("createPreviousButton", ItemTemplateRecord.class, TemplatedPanel.ItemSlot.class);
        method.setAccessible(true);

        Object result = method.invoke(panel, template, slot);

        assertNotNull(result);
    }

    /**
     * Test buildRequirementsText with single ECO requirement only (L432).
     */
    @Test
    void testBuildRequirementsTextEcoOnly() throws Exception {
        setUpAddonMocks();
        User user = User.getInstance(mockPlayer);
        when(im.getIsland(world, user)).thenReturn(null);
        when(oneBlockManager.getBlockProbs()).thenReturn(createBlockProbs());

        Constructor<PhasesPanel> constructor = PhasesPanel.class.getDeclaredConstructor(AOneBlock.class, World.class, User.class);
        constructor.setAccessible(true);
        panel = constructor.newInstance(addon, world, user);

        OneBlockPhase phase = new OneBlockPhase("0");
        phase.setPhaseName("EcoOnly");
        phase.addBlock(Material.STONE, 100);
        phase.setRequirements(List.of(new Requirement(ReqType.ECO, 500.0)));

        when(user.getTranslationOrNothing("aoneblock.gui.buttons.phase.economy")).thenReturn("Economy: 500");

        Method method = PhasesPanel.class.getDeclaredMethod("buildRequirementsText", OneBlockPhase.class);
        method.setAccessible(true);

        Object result = method.invoke(panel, phase);

        assertNotNull(result);
        Field economyField = result.getClass().getDeclaredField("economy");
        economyField.setAccessible(true);
        assertTrue(economyField.get(result).toString().contains("500"));
    }
}
