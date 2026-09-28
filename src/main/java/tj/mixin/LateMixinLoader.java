package tj.mixin;

import com.google.common.collect.ImmutableList;
import gregtech.api.GTValues;
import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.List;

public class LateMixinLoader implements ILateMixinLoader {

    @Override
    public List<String> getMixinConfigs() {
        final ImmutableList.Builder<String> configs = ImmutableList.builder();
        configs.add("mixins.tj.gregtech.json", "mixins.tj.gregicality.json");
        if (GTValues.isModLoaded(GTValues.MODID_AE2))
            configs.add("mixins.tj.ae2.json");
        if (GTValues.isModLoaded("gregicprobe"))
            configs.add("mixins.tj.gregic_probe.json");
        return configs.build();
    }
}
