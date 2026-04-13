package tj.util;

import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;

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
}
