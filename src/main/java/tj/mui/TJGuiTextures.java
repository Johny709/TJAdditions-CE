package tj.mui;

import gregtech.api.GTValues;
import gregtech.api.gui.resources.TextureArea;

public final class TJGuiTextures {

    public static final TextureArea DARKENED_SLOT = TJTextureArea.fullImage("textures/gui/base/darkened_slot");
    public static final TextureArea MULTIBLOCK_DISPLAY_BASE = TJTextureArea.adaptableImage("textures/gui/base/multiblock_display_base", 18, 18, 2, 2);
    public static final TextureArea TJ_LOGO_ANIMATED = TJTextureArea.fullImage("textures/tj_logo_animated");
    public static final TextureArea BLANK_SLOT = TJTextureArea.adaptableImage("textures/gui/widget/blank_slot", 18, 18, 1, 1);
    public static final TextureArea EMPTY_BUTTON = TJTextureArea.fullImage("textures/gui/base/empty_button");

    public static final TextureArea MACHINE_LABEL = TJTextureArea.adaptableImage("textures/gui/base/machine_label", 18, 18, 2, 2);
    public static final TextureArea MACHINE_LABEL_2 = TJTextureArea.adaptableImage("textures/gui/base/machine_label_2", 18, 18, 3, 3);
    public static final TextureArea MACHINE_LABEL_BRONZE = TJTextureArea.adaptableImage("textures/gui/base/machine_label_bronze", 20, 20, 2, 2);
    public static final TextureArea MACHINE_LABEL_STEEL = TJTextureArea.adaptableImage("textures/gui/base/machine_label_steel", 20, 20, 2, 2);

    public static final TextureArea SLOT_DOWN = TJTextureArea.adaptableImage("textures/gui/widget/slot_down", 18, 18, 1, 1);

    public static final TextureArea TOOLTIP_BOX = TJTextureArea.adaptableImage("textures/gui/base/tooltip_box", 18, 18, 2, 2);
    public static final TextureArea FLUID_BAR = TJTextureArea.adaptableImage("textures/gui/bar/fluid_bar", 10, 10, 1, 1);
    public static final TextureArea SELECTION_BOX = TJTextureArea.adaptableImage("textures/gui/base/select_box", 18, 18, 1, 1);
    public static final TextureArea SELECTION_BOX_2 = TJTextureArea.adaptableImage("textures/gui/base/select_box2", 18, 18, 1, 1);

    public static final TextureArea AUTOSCROLL = TJTextureArea.fullImage("textures/gui/base/auto_scroll");
    public static final TextureArea AUTOSCROLL_2 = TJTextureArea.fullImage("textures/gui/base/auto_scroll_2");
    public static final TextureArea AUTOSCROLL_UP = TJTextureArea.fullImage("textures/gui/base/auto_scroll_up");
    public static final TextureArea AUTOSCROLL_DOWN = TJTextureArea.fullImage("textures/gui/base/auto_scroll_down");
    public static final TextureArea AUTOSCROLL_LEFT = TJTextureArea.fullImage("textures/gui/base/auto_scroll_left");
    public static final TextureArea AUTOSCROLL_RIGHT = TJTextureArea.fullImage("textures/gui/base/auto_scroll_right");

    public static final TextureArea BAR_BLUE = TJTextureArea.fullImage("textures/gui/bar/bar_blue.");
    public static final TextureArea BAR_RED = TJTextureArea.fullImage("textures/gui/bar/bar_red");
    public static final TextureArea BAR_YELLOW = TJTextureArea.fullImage("textures/gui/bar/bar_yellow");
    public static final TextureArea BAR_GREEN = TJTextureArea.fullImage("textures/gui/bar/bar_green");
    public static final TextureArea BAR_ORANGE = TJTextureArea.fullImage("textures/gui/bar/bar_orange");

    public static final TextureArea OUTPUT_BUTTON = TJTextureArea.fullImage("textures/gui/widget/button_output");
    public static final TextureArea TOGGLE_POWER_BUTTON = TJTextureArea.fullImage("textures/gui/widget/power_button_toggle");
    public static final TextureArea TOGGLE_CAUTION_BUTTON = TJTextureArea.fullImage("textures/gui/widget/caution_button_toggle");
    public static final TextureArea TOGGLE_DISTINCT_BUTTON = TJTextureArea.fullImage("textures/gui/widget/distinct_button_toggle");
    public static final TextureArea ITEM_FILTER = TextureArea.fullImage("textures/items/metaitems/item_filter");
    public static final TextureArea FLUID_FILTER = TextureArea.fullImage("textures/items/metaitems/fluid_filter");
    public static final TextureArea ORE_DICTIONARY_FILTER = TJTextureArea.fullImage("textures/items/metaitems/ore_dictionary_filter");
    public static final TextureArea TOGGLE_RESET_BUTTON = TJTextureArea.fullImage("textures/gui/widget/reset_button_toggle");
    public static final TextureArea TOGGLE_UP_BUTTON = TJTextureArea.fullImage("textures/gui/widget/up_button_toggle");
    public static final TextureArea TOGGLE_DOWN_BUTTON = TJTextureArea.fullImage("textures/gui/widget/down_button_toggle");
    public static final TextureArea TOGGLE_ITEM_VOID_BUTTON = TJTextureArea.fullImage("textures/gui/widget/item_void_button_toggle");
    public static final TextureArea TOGGLE_FLUID_VOID_BUTTON = TJTextureArea.fullImage("textures/gui/widget/fluid_void_button_toggle");
    public static final TextureArea TOGGLE_CLEAR_GRID_BUTTON = TJTextureArea.fullImage("textures/gui/widget/button_clear_grid_toggle");
    public static final TextureArea TOGGLE_BOX = TJTextureArea.fullImage("textures/gui/widget/toggle_box");
    public static final TextureArea UNLOCK_LOCK = TJTextureArea.fullImage("textures/gui/widget/unlock_lock");
    public static final TextureArea TOGGLE_DISPLAY = TJTextureArea.fullImage("textures/gui/widget/toggle_button_display");
    public static final TextureArea TOGGLE_BLOCKING_MODE = TJTextureArea.fullImage("textures/gui/widget/block_mode_toggle");
    public static final TextureArea TOGGLE_INTERFACE_TERMINAL = TJTextureArea.fullImage("textures/gui/widget/interface_terminal_toggle");
    public static final TextureArea TOGGLE_SEND_FLUID = TJTextureArea.fullImage("textures/gui/widget/send_fluid_toggle");
    public static final TextureArea TOGGLE_SPLITTING_ITEMS_FLUIDS = TJTextureArea.fullImage("textures/gui/widget/splitting_items_fluids_toggle");
    public static final TextureArea CYCLE_BLOCKING_MODE_EX = TJTextureArea.fullImage("textures/gui/widget/blocking_mode_ex_cycle");
    public static final TextureArea TOGGLE_AUTO_PULL = TJTextureArea.fullImage("textures/gui/widget/auto_pull_toggle");
    public static final TextureArea TOGGLE_SHOW_INTERFACES = TJTextureArea.fullImage("textures/gui/widget/show_interfaces_toggle");
    public static final TextureArea TOGGLE_CRAFTING_INTERFACES = TJTextureArea.fullImage("textures/gui/widget/crafting_interfaces_toggle");
    public static final TextureArea CYCLE_LOCK_CRAFTING = TJTextureArea.fullImage("textures/gui/widget/lock_crafting_cycle");
    public static final TextureArea AE2_MULTIPLY2_BUTTON = TJTextureArea.fullImage("textures/gui/widget/ae2_multiply2_button");
    public static final TextureArea AE2_DIVIDE2_BUTTON = TJTextureArea.fullImage("textures/gui/widget/ae2_divide2_button");
    public static final TextureArea AE2_MULTIPLY3_BUTTON = TJTextureArea.fullImage("textures/gui/widget/ae2_multiply3_button");
    public static final TextureArea AE2_DIVIDE3_BUTTON = TJTextureArea.fullImage("textures/gui/widget/ae2_divide3_button");

    public static final TextureArea ENERGY_VOID = TJTextureArea.fullImage("textures/gui/widget/button_energy_void");

    public static final TextureArea SUN_BRONZE = TJTextureArea.fullImage(GTValues.MODID,"textures/gui/steam/bronze/boiler_sun_active");
    public static final TextureArea SUN_STEEL = TJTextureArea.fullImage(GTValues.MODID,"textures/gui/steam/steel/boiler_sun_active");
    public static final TextureArea MOON_BRONZE = TJTextureArea.fullImage(GTValues.MODID,"textures/gui/steam/bronze/boiler_sun");
    public static final TextureArea MOON_STEEL = TJTextureArea.fullImage(GTValues.MODID,"textures/gui/steam/steel/boiler_sun");
    public static final TextureArea BAR_STEAM = TJTextureArea.fullImage(GTValues.MODID,"textures/gui/steam/bronze/bar_bronze_empty");
    public static final TextureArea BAR_STEEL = TJTextureArea.fullImage(GTValues.MODID,"textures/gui/steam/steel/bar_steam");
    public static final TextureArea BAR_HEAT = TJTextureArea.fullImage(GTValues.MODID,"textures/gui/steam/steel/bar_heat");
    public static final TextureArea BRONZE_IN = TJTextureArea.fullImage(GTValues.MODID,"textures/gui/steam/bronze/overlay_bronze_in");
    public static final TextureArea BRONZE_OUT = TJTextureArea.fullImage(GTValues.MODID,"textures/gui/steam/bronze/overlay_bronze_out");
    public static final TextureArea STEEL_IN = TJTextureArea.fullImage(GTValues.MODID,"textures/gui/steam/steel/overlay_steel_in");
    public static final TextureArea STEEL_OUT = TJTextureArea.fullImage(GTValues.MODID,"textures/gui/steam/steel/overlay_steel_out");
    public static final TextureArea STEEL_BACKGROUND = TJTextureArea.fullImage(GTValues.MODID,"textures/gui/steam/steel/steel_gui");
    public static final TextureArea STEEL_SLOT = TJTextureArea.fullImage(GTValues.MODID,"textures/gui/steam/steel/slot_steel");
    public static final TextureArea BRONZE_FUEL_EMPTY = TJTextureArea.fullImage(GTValues.MODID,"textures/gui/steam/bronze/boiler_bronze_fuel");
    public static final TextureArea BRONZE_FUEL_FULL = TJTextureArea.fullImage(GTValues.MODID,"textures/gui/steam/bronze/boiler_bronze_fuel_full");
    public static final TextureArea STEEL_FUEL_EMPTY = TJTextureArea.fullImage(GTValues.MODID,"textures/gui/steam/steel/boiler_steel_fuel");
    public static final TextureArea STEEL_FUEL_FULL = TJTextureArea.fullImage(GTValues.MODID,"textures/gui/steam/steel/boiler_steel_fuel_full");

    public static final TextureArea BUTTON_BATCH_ONE = TJTextureArea.fullImage("textures/gui/button/batch_1");
    public static final TextureArea BUTTON_BATCH_FOUR = TJTextureArea.fullImage("textures/gui/button/batch_4");
    public static final TextureArea BUTTON_BATCH_SIXTEEN = TJTextureArea.fullImage("textures/gui/button/batch_16");
    public static final TextureArea BUTTON_BATCH_SIXTY_FOUR = TJTextureArea.fullImage("textures/gui/button/batch_64");
    public static final TextureArea BUTTON_BATCH_TWO_HUNDRED_FIFTY_SIX = TJTextureArea.fullImage("textures/gui/button/batch_256");

    public static final TextureArea AXE_OVERLAY = TJTextureArea.fullImage("textures/items/metaitems/overlay/axe_overlay");
    public static final TextureArea HOE_OVERLAY = TJTextureArea.fullImage("textures/items/metaitems/overlay/hoe_overlay");
    public static final TextureArea SHEARS_OVERLAY = TJTextureArea.fullImage("textures/items/metaitems/overlay/shears_overlay");
    public static final TextureArea SEEDS_OVERLAY = TJTextureArea.fullImage("textures/items/metaitems/overlay/seeds_overlay");
    public static final TextureArea BONE_MEAL_OVERLAY = TJTextureArea.fullImage("textures/items/metaitems/overlay/bone_meal_overlay");
    public static final TextureArea LIST_OVERLAY = TJTextureArea.fullImage("textures/gui/widget/list_overlay");

    public static final TextureArea SUPER_INTERFACE = TJTextureArea.fullImage("textures/gui/super_interface");
    public static final TextureArea INTERFACE_SETTINGS_EDGE_RIGHT = TJTextureArea.fullImage("textures/gui/widget/interface_settings_base_edge_right");
    public static final TextureArea INTERFACE_SETTINGS_LEFT = TJTextureArea.fullImage("textures/gui/widget/interface_settings_base_left");
    public static final TextureArea UPGRADE_OVERLAY = TJTextureArea.fullImage("textures/gui/widget/me.upgrade_overlay");
    public static final TextureArea PATTERN_OVERLAY = TJTextureArea.fullImage("textures/gui/widget/me.pattern_overlay");

    private TJGuiTextures() {}

    public static TextureArea getBarByColor(String color) {
        switch (color) {
            case "§b": return TJGuiTextures.BAR_BLUE;
            case "§e": return TJGuiTextures.BAR_YELLOW;
            case "§a": return TJGuiTextures.BAR_GREEN;
            case "§6": return TJGuiTextures.BAR_ORANGE;
            default: return TJGuiTextures.BAR_RED;
        }
    }
}