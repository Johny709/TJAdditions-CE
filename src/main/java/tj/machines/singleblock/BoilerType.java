package tj.machines.singleblock;

import gregtech.api.gui.resources.TextureArea;
import gregtech.api.render.SimpleSidedCubeRenderer;

import static tj.gui.TJGuiTextures.*;
import static gregtech.api.gui.GuiTextures.*;
import static gregtech.api.render.Textures.*;

public enum BoilerType {
    BRONZE(25, 150, 0.75F, BAR_STEAM, SUN_BRONZE, MOON_BRONZE, BRONZE_BACKGROUND, BRONZE_SLOT, BRONZE_IN, BRONZE_OUT, CANISTER_OVERLAY, BRONZE_FUEL_EMPTY, BRONZE_FUEL_FULL, STEAM_BRICKED_CASING_BRONZE),
    STEEL(10, 150, 0.75F, BAR_STEEL, SUN_STEEL, MOON_STEEL, STEEL_BACKGROUND, STEEL_SLOT, STEEL_IN, STEEL_OUT, DARK_CANISTER_OVERLAY, STEEL_FUEL_EMPTY, STEEL_FUEL_FULL, STEAM_BRICKED_CASING_STEEL),
    LV(10, 320, 0.50F, BAR_STEEL, SUN_STEEL, MOON_STEEL, BACKGROUND, SLOT, STEEL_IN, STEEL_OUT, DARK_CANISTER_OVERLAY, STEEL_FUEL_EMPTY, STEEL_FUEL_FULL, VOLTAGE_CASINGS[1]);

    BoilerType(int ticks, int steamProduction, float cooldown, TextureArea progressBar, TextureArea sun, TextureArea moon, TextureArea background, TextureArea slot, TextureArea in, TextureArea out, TextureArea canister, TextureArea fuelEmpty, TextureArea fuelFull, SimpleSidedCubeRenderer casing) {
        this.ticks = ticks;
        this.steamProduction = steamProduction;
        this.cooldown = cooldown;
        this.progressBar = progressBar;
        this.sun = sun;
        this.moon = moon;
        this.background = background;
        this.slot = slot;
        this.in = in;
        this.out = out;
        this.canister = canister;
        this.fuelEmpty = fuelEmpty;
        this.fuelFull = fuelFull;
        this.casing = casing;
    }
    private final int ticks;
    private final int steamProduction;
    private final float cooldown;
    private final TextureArea progressBar;
    private final TextureArea sun;
    private final TextureArea moon;
    private final TextureArea background;
    private final TextureArea slot;
    private final TextureArea in;
    private final TextureArea out;
    private final TextureArea canister;
    private final TextureArea fuelEmpty;
    private final TextureArea fuelFull;
    private final SimpleSidedCubeRenderer casing;

    public int getTicks() {
        return ticks;
    }

    public int getSteamProduction() {
        return steamProduction;
    }

    public float getCooldown() {
        return cooldown;
    }

    public TextureArea getProgressBar() {
        return progressBar;
    }

    public TextureArea getSun() {
        return sun;
    }

    public TextureArea getMoon() {
        return moon;
    }

    public TextureArea getBackground() {
        return background;
    }

    public TextureArea getSlot() {
        return slot;
    }

    public TextureArea getIn() {
        return in;
    }

    public TextureArea getOut() {
        return out;
    }

    public TextureArea getCanister() {
        return canister;
    }

    public TextureArea getFuelEmpty() {
        return fuelEmpty;
    }

    public TextureArea getFuelFull() {
        return fuelFull;
    }

    public SimpleSidedCubeRenderer getCasing() {
        return casing;
    }
}
