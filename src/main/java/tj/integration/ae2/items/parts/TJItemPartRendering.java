package tj.integration.ae2.items.parts;

import appeng.api.util.AEColor;
import appeng.bootstrap.IItemRendering;
import appeng.bootstrap.ItemRenderingCustomizer;
import appeng.client.render.StaticItemColor;
import appeng.core.AppEng;
import appeng.core.features.registries.PartModels;
import appeng.items.parts.PartType;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TJItemPartRendering extends ItemRenderingCustomizer {

    private final PartModels partModels;

    private final TJItemPart item;

    public TJItemPartRendering(PartModels partModels, TJItemPart item) {
        this.partModels = partModels;
        this.item = item;
    }

    @Override
    public void customize(IItemRendering rendering) {
        rendering.meshDefinition(this::getItemMeshDefinition);

        rendering.color(new StaticItemColor(AEColor.TRANSPARENT));

        // Register all item models as variants so they get loaded
        rendering.variants(Arrays.stream(PartType.values())
                .filter(f -> f != PartType.INVALID_TYPE)
                .flatMap(part -> part.getItemModels().stream())
                .collect(Collectors.toList()));

        List<String> modelNames = new ArrayList<>();

        List<ResourceLocation> partResourceLocs = modelNames.stream()
                .map(name -> new ResourceLocation(AppEng.MOD_ID, name))
                .collect(Collectors.toList());
        this.partModels.registerModels(partResourceLocs);
    }

    private ModelResourceLocation getItemMeshDefinition(ItemStack is) {
        TJPartType partType = this.item.getTypeByStack(is);
        int variant = this.item.variantOf(is.getItemDamage());
        return partType.getItemModels().get(variant);
    }
}
