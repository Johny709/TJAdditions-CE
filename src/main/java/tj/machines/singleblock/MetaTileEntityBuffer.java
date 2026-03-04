package tj.machines.singleblock;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregicadditions.machines.overrides.GATieredMetaTileEntity;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.gui.widgets.TankWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.util.Position;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import tj.gui.TJGuiTextures;
import tj.gui.widgets.TJLabelWidget;
import tj.gui.widgets.impl.SlotScrollableWidgetGroup;
import tj.items.handlers.LargeItemStackHandler;
import tj.textures.TJTextures;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MetaTileEntityBuffer extends GATieredMetaTileEntity {

    public MetaTileEntityBuffer(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
        this.initializeInventory();
        this.itemInventory = this.importItems;
        this.fluidInventory = this.importFluids;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityBuffer(this.metaTileEntityId, this.getTier());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("tj.machine.gt_buffer.description"));
        tooltip.add(I18n.format("machine.universal.stack", 64 << Math.max(0, this.getTier() - 8)));
        tooltip.add(I18n.format("machine.universal.slots", this.getTierSlots(this.getTier())));
        tooltip.add(I18n.format("gtadditions.machine.multi_fluid_hatch_universal.tooltip.1", 64000 << Math.max(0, this.getTier() - 8)));
        tooltip.add(I18n.format("gtadditions.machine.multi_fluid_hatch_universal.tooltip.2", Math.max(1, this.getTier() + 1)));
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new LargeItemStackHandler(this.getTierSlots(this.getTier()), 64 << Math.max(0, this.getTier() - 8));
    }

    @Override
    protected FluidTankList createImportFluidHandler() {
        return new FluidTankList(false, IntStream.range(0, Math.max(1, this.getTier() + 1))
                .mapToObj(i -> new FluidTank(64000 << Math.max(0, this.getTier() - 8)))
                .collect(Collectors.toList()));
    }

    @Override
    protected ModularUI createUI(EntityPlayer player) {
        int tier = Math.min(10, this.getTier() + 2);
        int startX = Math.max(7, 61 - (9 * (this.getTier() - 1)));
        WidgetGroup slotGroup = new WidgetGroup(new Position(0, 7));
        SlotScrollableWidgetGroup slotScrollGroup = new SlotScrollableWidgetGroup(0, 7, 193, 180, 10)
                .setScrollWidth(5);
        for (int i = 0, itemI = 0, fluidI = 0; i < this.importItems.getSlots() + this.importFluids.getTanks(); i++) {
            Widget slotWidget = i % tier == 0 || itemI >= this.importItems.getSlots()
                    ? new TankWidget(this.importFluids.getTankAt(fluidI++), startX + (18 * (i % tier)), 18 * (i / tier), 18, 18).setBackgroundTexture(GuiTextures.FLUID_SLOT)
                    : new SlotWidget(this.importItems, itemI++, startX + (18 * (i % tier)), 18 * (i / tier)).setBackgroundTexture(GuiTextures.SLOT);
            if (this.getTier() > 9)
                slotScrollGroup.addWidget(slotWidget);
            else slotGroup.addWidget(slotWidget);
        }
        return ModularUI.builder(GuiTextures.BACKGROUND, 196, 137 + Math.min(144, 18 * (this.getTier() - 1)))
                .widget(new TJLabelWidget(7, -18, 178, 18, TJGuiTextures.MACHINE_LABEL)
                        .setItemLabel(this.getStackForm()).setLocale(this.getMetaFullName()))
                .widget(this.getTier() > 9 ? slotScrollGroup : slotGroup)
                .bindPlayerInventory(player.inventory, 55 + Math.min(144, 18 * (this.getTier() - 1)))
                .build(this.getHolder(), player);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        for (EnumFacing facing : EnumFacing.VALUES) {
            if (facing == EnumFacing.UP || facing == EnumFacing.DOWN) {
                TJTextures.BUFFER.renderSided(facing, renderState, translation, pipeline);
            } else TJTextures.SUPER_BUFFER.renderSided(facing, renderState, translation, pipeline);
        }
    }

    private int getTierSlots(int tier) {
        tier++;
        return tier * tier;
    }
}
