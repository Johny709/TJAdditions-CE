package tj.mixin;

import com.google.common.collect.ImmutableList;
import tj.TJ;
import gregtech.api.GTValues;
import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LateMixinLoader implements ILateMixinLoader {

    @Override
    public List<String> getMixinConfigs() {
        final ImmutableList.Builder<String> configs = ImmutableList.builder();
        configs.add("mixins.tj.gregtech.json", "mixins.tj.gregicality.json", "mixins.tj.jei.json");
        if (GTValues.isModLoaded(GTValues.MODID_AE2))
            configs.add("mixins.tj.ae2.json");
        if (GTValues.isModLoaded("gregicprobe"))
            configs.add("mixins.tj.gregic_probe.json");
        return configs.build();
    }
}
