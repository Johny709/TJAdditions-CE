package tj.util;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public final class TextUtils {

    private TextUtils() {}

    public static String translate(String locale, Object... format) {
        return I18n.translateToLocalFormatted(locale, format);
    }

    public static TextComponentString addTranslationText(String locale, Object... format) {
        return addTranslationText(null, locale, format);
    }

    public static TextComponentString addTranslationText(Consumer<TextComponentString> textBuilder, String locale, Object... format) {
        final TextComponentString textComponent = new TextComponentString(I18n.translateToLocalFormatted(locale, format));
        if (textBuilder != null)
            textBuilder.accept(textComponent);
        return textComponent;
    }

    public static List<ITextComponent> splitText(ITextComponent textComponent, int maxTextLength, FontRenderer fontRendererIn) {
        boolean flag = false;
        StringBuilder colorCode = new StringBuilder();
        final List<ITextComponent> textComponentList = new ArrayList<>();
        final List<ITextComponent> siblings = new ArrayList<>(Collections.singleton(textComponent));
        siblings.addAll(textComponent.getSiblings());
        for (int i = 0; i < siblings.size(); i++) {
            final ITextComponent sibling = siblings.get(i);
            String text = sibling.getUnformattedComponentText();
            int textLeft = fontRendererIn.getStringWidth(text);
            if (textLeft >= maxTextLength) {
                for (; textLeft >= maxTextLength; textLeft -= maxTextLength) {
                    final String text1 = fontRendererIn.trimStringToWidth(text, Math.min(maxTextLength, textLeft));
                    for (int j = 0; j < text1.length(); j++) {
                        final char letter = text1.charAt(j);
                        if (i < text1.length() - 1 && letter == '§')
                            colorCode.append(text1, j, j + 2);
                    }
                    text = text.replace(text1, "");
                    textComponent.appendSibling(new TextComponentString(text1)
                            .setStyle(sibling.getStyle().createShallowCopy()));
                    textComponentList.add(textComponent);
                    textComponent = new TextComponentString("");
                    textComponent.appendSibling(new TextComponentString(colorCode + text)
                            .setStyle(sibling.getStyle().createShallowCopy()));
                    textComponentList.add(textComponent);
                    textComponent = new TextComponentString("");
                    colorCode = new StringBuilder();
                }
                if (i == 0) {
                    flag = true;
                    break;
                }
            }
        }
        if (!flag)
            textComponentList.add(textComponent);
        return textComponentList;
    }

    public static String stringFromArgs(int start, int end, String... args) {
        return stringFromArgs(start, end, " ", args);
    }

    public static String stringFromArgs(int start, int end, String append, String... args) {
        if (args.length < 1)
            return "";
        final StringBuilder tipStr = new StringBuilder();
        int i = start;
        while (true) {
            tipStr.append(args[i]);
            if (!(++i < end))
                break;
            tipStr.append(append);
        }
        return tipStr.toString();
    }
}
