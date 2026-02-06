package tj.util;

import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;
import java.util.function.Consumer;

@SideOnly(Side.CLIENT)
public final class TooltipHelper {

    private TooltipHelper() {}

    public static void shiftText(List<String> tooltip, Consumer<List<String>> tip) {
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            tip.accept(tooltip);
            tooltip.add(TooltipHelper.blinkingText(Color.WHITE, 50, "tj.multiblock.universal.tooltip.more_jei"));
        } else tooltip.add(TooltipHelper.blinkingText(Color.WHITE, 50, "tj.multiblock.universal.tooltip.shift"));
    }

    public static String blinking(Color color, int ticks) {
        return FMLClientHandler.instance().getWorldClient().getTotalWorldTime() % ticks < ticks / 2 ? String.valueOf(color) : "";
    }

    public static String blinkingText(Color color, int ticks, String locale, Object... params) {
        if (FMLClientHandler.instance().getWorldClient() == null)
            return color + I18n.format(locale, params);
        return (FMLClientHandler.instance().getWorldClient().getTotalWorldTime() % ticks < ticks / 2 ? color : "") + I18n.format(locale, params);
    }

    public static String rainbow(int ticks) {
        int ordinal = (int) ((FMLClientHandler.instance().getWorldClient().getTotalWorldTime() % (Color.values().length * ticks)) / ticks);
        return String.valueOf(Color.values()[ordinal]);
    }

    public static String rainbowText(int ticks, String locale, Object... params) {
        int ordinal = (int) ((FMLClientHandler.instance().getWorldClient().getTotalWorldTime() % (Color.values().length * ticks)) / ticks);
        return Color.values()[ordinal] + I18n.format(locale, params);
    }
}
