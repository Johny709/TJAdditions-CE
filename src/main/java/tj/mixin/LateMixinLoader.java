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
        return ImmutableList.of("mixins.tj.json");
    }
}
