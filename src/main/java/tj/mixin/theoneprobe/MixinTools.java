package tj.mixin.theoneprobe;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import mcjty.theoneprobe.Tools;
import net.minecraft.client.resources.I18n;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = Tools.class, remap = false)
public abstract class MixinTools {

    @WrapOperation(method = "stylifyString",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/text/translation/I18n;func_74838_a(Ljava/lang/String;)Ljava/lang/String;", remap = true))
    private static String wrapOperationStylingText(String s, Operation<String> original) {
        if (s.contains("[*") && s.contains("*]")) {
            final int startFormat = s.indexOf("[*");
            final int endFormat = s.indexOf("*]");
            final String middle = s.substring(0, startFormat);
            final String[] format = s.substring(startFormat + 2, endFormat).split(";");
            for (int i = 0; i < format.length; i++) {
                if (format[i].contains("{*") && format[i].contains("*}")) {
                    format[i] = I18n.format(format[i].substring(2, format[i].length() - 2));
                }
            }
            return I18n.format(middle, format);
        } else return original.call(s);
    }
}
