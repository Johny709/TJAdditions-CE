package tj.util;

import net.minecraft.util.IStringSerializable;
import tj.TJValues;

import javax.annotation.Nonnull;

public enum VoltageOverclock implements IStringSerializable {
    X, ULV, LV, MV, HV, EV, IV, LUV, ZPM, UV, UHV, UEV, UIV, UMV, UXV, MAX;

    @Nonnull
    @Override
    public String getName() {
        return this.ordinal() == 0 ? Color.WHITE + this.name() : TJValues.VCC[this.ordinal() - 1] + this.name();
    }
}
