package tj.util;

import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@SideOnly(Side.CLIENT)
public final class TooltipHelper {

    private static final TooltipHandler INSTANCE = new TooltipHandler();

    private TooltipHelper() {}

    public static void pageText(List<String> tooltip, int maxPages, BiConsumer<List<String>, TooltipHandler> tipHandler) {
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            INSTANCE.setEnd(maxPages);
            tooltip.add(Color.WHITE + I18n.format("tj.multiblock.universal.tooltip.page", INSTANCE.getIndex(), INSTANCE.getEnd()));
            if (Keyboard.isKeyDown(Keyboard.KEY_TAB) && !INSTANCE.isPressed()) {
                INSTANCE.setPressed(true);
                INSTANCE.setIndex(INSTANCE.getIndex() + 1);
            } else INSTANCE.setPressed(false);
            tipHandler.accept(tooltip, INSTANCE);
            tooltip.add(I18n.format("tj.multiblock.universal.tooltip.page_info"));
        } else INSTANCE.reset();
    }

    public static void shiftText(List<String> tooltip, Consumer<List<String>> tip) {
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            tip.accept(tooltip);
            tooltip.add(TooltipHelper.blinkingText(Color.WHITE, 50, "tj.multiblock.universal.tooltip.shifted"));
        } else tooltip.add(TooltipHelper.blinkingText(Color.WHITE, 50, "tj.multiblock.universal.tooltip.shift"));
    }

    public static void shiftTextJEI(List<String> tooltip, Consumer<List<String>> tip) {
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

    public static class TooltipHandler {

        private boolean isPressed;
        private boolean isSet;
        private int index;
        private int end;

        public void reset() {
            this.isPressed = false;
            this.isSet = false;
            this.index = 0;
            this.end = 0;
        }

        public void setPressed(boolean pressed) {
            this.isPressed = pressed;
        }

        public boolean isPressed() {
            return this.isPressed;
        }

        public void setSet(boolean set) {
            this.isSet = set;
        }

        public boolean isSet() {
            return this.isSet;
        }

        public void setIndex(int index) {
            if (index > this.getEnd())
                index = 0;
            this.index = Math.max(0, index);
        }

        public int getIndex() {
            return this.index;
        }

        public void setEnd(int end) {
            if (!this.isSet) {
                this.isSet = true;
                this.end = end;
            }
        }

        public int getEnd() {
            return this.end;
        }
    }
}
