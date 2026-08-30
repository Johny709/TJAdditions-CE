package tj.integration.theoneprobe.impl;

import io.netty.buffer.ByteBuf;
import mcjty.theoneprobe.api.IElement;
import mcjty.theoneprobe.network.NetworkTools;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import tj.integration.theoneprobe.TheOneProbeCompatibility;

public class ElementTJText implements IElement {

    private final String text;

    public ElementTJText(String text) {
        this.text = text;
    }

    public ElementTJText(ByteBuf byteBuf) {
        this.text = formatLocaleText(NetworkTools.readString(byteBuf));
    }

    @Override
    public void render(int x, int y) {
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(this.text, x, y, 0xFFFFFF);
    }

    @Override
    public int getWidth() {
        return this.text.length() * 6;
    }

    @Override
    public int getHeight() {
        return 12;
    }

    @Override
    public void toBytes(ByteBuf byteBuf) {
        NetworkTools.writeString(byteBuf, this.text);
    }

    @Override
    public int getID() {
        return TheOneProbeCompatibility.ELEMENT_TJ_TEXT;
    }

    private static String formatLocaleText(String text) {
        while (text.contains("{*") && text.contains("*}")) {
            final int start = text.indexOf("{*");
            final int end = text.lastIndexOf("*}");
            final String formatStr = I18n.format(text.substring(start + 2, end));
            if (formatStr.contains("[*") && formatStr.contains("*]")) {
                final int startFormat = formatStr.indexOf("[*");
                final int endFormat = formatStr.indexOf("*]");
                final String middle = formatStr.substring(0, startFormat);
                final String[] format = formatStr.substring(startFormat + 2, endFormat).split(";");
                for (int i = 0; i < format.length; i++) {
                    if (format[i].contains("{*") && format[i].contains("*}")) {
                        format[i] = I18n.format(format[i].substring(2, format[i].length() - 2));
                    } else format[i] = I18n.format(format[i]);
                }
                text = I18n.format(middle, format) + text.substring(end + 2);
            } else text = formatStr + text.substring(end + 2);
        }
        return text;
    }
}
