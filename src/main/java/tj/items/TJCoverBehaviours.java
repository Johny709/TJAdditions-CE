package tj.items;

import gregicadditions.GAValues;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.ICoverable;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.util.GTLog;
import gregtech.common.items.behaviors.CoverPlaceBehavior;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import tj.TJ;
import tj.items.covers.*;

import java.util.function.BiFunction;

import static tj.items.TJMetaItems.*;

public class TJCoverBehaviours {

    public static void init() {
        GTLog.logger.info("Registering Covers from TJ...");

        registerBehavior(126, new ResourceLocation(TJ.MODID, "creative.fluid.cover"), TJMetaItems.CREATIVE_FLUID_COVER, CreativeFluidCover::new);
        registerBehavior(127, new ResourceLocation(TJ.MODID, "creative.item.cover"), TJMetaItems.CREATIVE_ITEM_COVER, CreativeItemCover::new);
        registerBehavior(128, new ResourceLocation(TJ.MODID, "creative.energy.cover"), TJMetaItems.CREATIVE_ENERGY_COVER, CreativeEnergyCover::new);

        int enderCoverID = 129; // occupies IDs 129 - 165
        for (int i = 0; i < ENDER_FLUID_COVERS.length; i++) {
            int finalI = i + 3;
            registerBehavior(enderCoverID++, new ResourceLocation(TJ.MODID, "ender_fluid_cover_" + GAValues.VN[i + 3].toLowerCase()), ENDER_FLUID_COVERS[i], (cover, face) -> new EnderFluidCover(cover, face, finalI));
            registerBehavior(enderCoverID++, new ResourceLocation(TJ.MODID, "ender_item_cover_" + GAValues.VN[i + 3].toLowerCase()), ENDER_ITEM_COVERS[i], (cover, face) -> new EnderItemCover(cover, face, finalI));
            registerBehavior(enderCoverID++, new ResourceLocation(TJ.MODID, "ender_energy_cover_" + GAValues.VN[i + 3].toLowerCase()), ENDER_ENERGY_COVERS[i], (cover, face) -> new EnderEnergyCover(cover, face, finalI));
        }
        registerBehavior(166, new ResourceLocation(TJ.MODID, "void_item_cover"), VOID_ITEM_COVER, VoidItemCover::new);
        registerBehavior(167, new ResourceLocation(TJ.MODID, "void_fluid_cover"), VOID_FLUID_COVER, VoidFluidCover::new);
        registerBehavior(168, new ResourceLocation(TJ.MODID,"void_energy_cover"), VOID_ENERGY_COVER, VoidEnergyCover::new);
        registerBehavior(169, new ResourceLocation(TJ.MODID, "void_advanced_item_cover"), VOID_ADVANCED_ITEM_COVER, VoidAdvancedItemCover::new);
        registerBehavior(170, new ResourceLocation(TJ.MODID, "void_advanced_fluid_cover"), VOID_ADVANCED_FLUID_COVER, VoidAdvancedFluidCover::new);
        registerBehavior(171, new ResourceLocation(TJ.MODID, "void_advanced_energy_cover"), VOID_ADVANCED_ENERGY_COVER, VoidAdvancedEnergyCover::new);
    }

    public static void registerBehavior(int coverNetworkId, ResourceLocation coverId, MetaItem<?>.MetaValueItem placerItem, BiFunction<ICoverable, EnumFacing, CoverBehavior> behaviorCreator) {
        CoverDefinition coverDefinition = new CoverDefinition(coverId, behaviorCreator, placerItem.getStackForm());
        CoverDefinition.registerCover(coverNetworkId, coverDefinition);
        placerItem.addComponents(new CoverPlaceBehavior(coverDefinition));
    }
}
