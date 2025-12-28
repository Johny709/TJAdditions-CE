package tj.integration.jei.multi.steam;

import com.google.common.collect.Lists;
import gregicadditions.machines.GATileEntities;
import gregtech.api.GTValues;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.recipes.RecipeMaps;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.common.metatileentities.multi.MetaTileEntityLargeBoiler;
import gregtech.integration.jei.multiblock.MultiblockInfoPage;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import tj.machines.multi.steam.MetaTileEntityMegaBoiler;
import tj.util.Color;

import java.util.List;

public class MegaBoilerInfo extends MultiblockInfoPage {

    private final MetaTileEntityLargeBoiler.BoilerType boilerType;
    private final MetaTileEntityMegaBoiler megaBoiler;

    public MegaBoilerInfo(MetaTileEntityLargeBoiler.BoilerType boilerType, MetaTileEntityMegaBoiler megaBoiler) {
        this.boilerType = boilerType;
        this.megaBoiler = megaBoiler;
    }

    @Override
    public MultiblockControllerBase getController() {
        return this.megaBoiler;
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        MultiblockShapeInfo shapeInfo = MultiblockShapeInfo.builder()
                .aisle("XXXXXXXXXXXXXXX", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC")
                .aisle("XXXXXXXXXXXXXXX", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CCCCCCCCCCCCCCC")
                .aisle("XXXXXXXXXXXXXXX", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CCCCCCCCCCCCCCC")
                .aisle("XXXXXXXXXXXXXXX", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CCCCCCCCCCCCCCC")
                .aisle("XXXXXXXXXXXXXXX", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CCCCCCCCCCCCCCC")
                .aisle("XXXXXXXXXXXXXXX", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CCCCCCCCCCCCCCC")
                .aisle("HXXXXXXXXXXXXXX", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CCCCCCCCCCCCCCC")
                .aisle("MXXXXXXXXXXXXXm", "OPPPPPPPPPPPPPC", "SPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CCCCCCCCCCCCCCC")
                .aisle("IXXXXXXXXXXXXXX", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CCCCCCCCCCCCCCC")
                .aisle("XXXXXXXXXXXXXXX", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CCCCCCCCCCCCCCC")
                .aisle("XXXXXXXXXXXXXXX", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CCCCCCCCCCCCCCC")
                .aisle("XXXXXXXXXXXXXXX", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CCCCCCCCCCCCCCC")
                .aisle("XXXXXXXXXXXXXXX", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CCCCCCCCCCCCCCC")
                .aisle("XXXXXXXXXXXXXXX", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CCCCCCCCCCCCCCC")
                .aisle("XXXXXXXXXXXXXXX", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC")
                .where('S', this.megaBoiler, EnumFacing.WEST)
                .where('C', this.boilerType.casingState)
                .where('X', this.boilerType.fireboxState)
                .where('P', this.boilerType.pipeState)
                .where('I', MetaTileEntities.ITEM_IMPORT_BUS[GTValues.MAX], EnumFacing.WEST)
                .where('O', MetaTileEntities.FLUID_EXPORT_HATCH[GTValues.MAX], EnumFacing.WEST)
                .where('H', MetaTileEntities.FLUID_IMPORT_HATCH[GTValues.MAX], EnumFacing.WEST)
                .where('M', GATileEntities.MAINTENANCE_HATCH[0], EnumFacing.WEST)
                .where('m', GATileEntities.MUFFLER_HATCH[0], EnumFacing.EAST)
                .build();
        return Lists.newArrayList(shapeInfo);
    }

    @Override
    protected void generateBlockTooltips() {
        super.generateBlockTooltips();
        Block casing = this.boilerType.casingState.getBlock();
        Block firebox = this.boilerType.fireboxState.getBlock();
        this.addBlockTooltip(new ItemStack(casing, 1, casing.getMetaFromState(this.boilerType.casingState)), new TextComponentTranslation("gregtech.multiblock.preview.limit", 200)
                .setStyle(new Style().setColor(TextFormatting.RED)));
        this.addBlockTooltip(new ItemStack(firebox, 1, firebox.getMetaFromState(this.boilerType.fireboxState)), new TextComponentTranslation("gregtech.multiblock.preview.limit", 200)
                .setStyle(new Style().setColor(TextFormatting.RED)));
    }


    @Override
    public String[] getDescription() {
        return new String[] {
                I18n.format(this.boilerType == MetaTileEntityLargeBoiler.BoilerType.BRONZE ? "tj.multiblock.mega_bronze_boiler.description"
                        : this.boilerType == MetaTileEntityLargeBoiler.BoilerType.STEEL ? "tj.multiblock.mega_steel_boiler.description"
                        : this.boilerType == MetaTileEntityLargeBoiler.BoilerType.TITANIUM ? "tj.multiblock.mega_titanium_boiler.description"
                        : "tj.multiblock.mega_tungstensteel_boiler.description"),
                I18n.format("tj.multiblock.mega_boiler.parallel.description", this.megaBoiler.getParallel()),
                Color.YELLOW + I18n.format("tj.multiblock.mega_boiler.warning"),
                I18n.format("tj.multiblock.universal.tooltip.1", RecipeMaps.DIESEL_GENERATOR_FUELS.getLocalizedName() + ", " + RecipeMaps.SEMI_FLUID_GENERATOR_FUELS.getLocalizedName()),
                I18n.format("tj.multiblock.universal.tooltip.2", this.megaBoiler.getParallel())};
    }

    @Override
    public float getDefaultZoom() {
        return 0.3f;
    }
}
