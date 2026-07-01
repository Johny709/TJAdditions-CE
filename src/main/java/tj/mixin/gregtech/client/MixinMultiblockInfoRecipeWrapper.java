package tj.mixin.gregtech.client;

import gregicadditions.GAValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.render.scene.WorldSceneRenderer;
import gregtech.integration.jei.multiblock.MultiblockInfoPage;
import gregtech.integration.jei.multiblock.MultiblockInfoRecipeWrapper;
import gregtech.integration.jei.multiblock.channel.ChannelState;
import gregtech.integration.jei.multiblock.channel.StructureChannels;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.gui.recipes.RecipeLayout;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tj.TJValues;
import tj.capability.IJEIExtentSync;
import tj.integration.jei.multi.parallel.IParallelMultiblockInfoPage;

import java.util.*;


@Mixin(value = MultiblockInfoRecipeWrapper.class, remap = false)
public abstract class MixinMultiblockInfoRecipeWrapper {

    @Shadow
    @Final
    private int ICON_SIZE;

    @Shadow
    @Final
    private int RIGHT_PADDING;

    @Shadow
    private Map<GuiButton, Runnable> buttons;


    @Shadow
    private int currentChannelIndex;
    @Unique
    private GuiButton buttonVoltage;

    @Unique
    private boolean multiLayer;


    @Shadow
    private int currentExtent;
    @Shadow
    private GuiButton buttonPreviousPattern;
    @Shadow
    private GuiButton buttonNextPattern;
    @Shadow
    protected abstract void rebuildScene();

    @Shadow
    @Final
    private boolean hasVoltagePages;

    @Unique
    private int tj_voltageIndex = 0;

    @Shadow
    @Final
    private MultiblockInfoPage infoPage;

    @Shadow
    private ChannelState channelState;

    @Shadow
    private BlockPos controllerPos;


    @Shadow
    protected abstract void triggerStructureCheck(WorldSceneRenderer.TrackedDummyWorld world);

    @Shadow
    private WorldSceneRenderer renderer;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void injectMultiblockInfoRecipeWrapper_Init(MultiblockInfoPage infoPage, CallbackInfo ci) {
        if (infoPage instanceof IParallelMultiblockInfoPage) {
            this.multiLayer = true;
        }
        if (infoPage.getController().getMaxExtent() > 1) {
            this.multiLayer = true;
        }
    }

    @Inject(method = "setRecipeLayout", at = @At("TAIL"))
    private void injectSetRecipeLayout(RecipeLayout layout, IGuiHelper guiHelper, CallbackInfo ci) {
        if (!this.multiLayer) {
            return;
        }

        IDrawable border = layout.getRecipeCategory().getBackground();

        if (this.hasVoltagePages) {
            this.buttonVoltage = new GuiButton(0,
                    border.getWidth() - ((2 * ICON_SIZE) + RIGHT_PADDING + 1), 110,
                    ICON_SIZE + 21, ICON_SIZE,
                    TJValues.VCC[tj_voltageIndex] + GAValues.VN[tj_voltageIndex]);
            this.buttons.put(this.buttonVoltage, () -> this.switchVoltage(Mouse.isButtonDown(0) ? 1 : Mouse.isButtonDown(1) ? -1 : 0));
        }

        this.buttons.put(this.buttonPreviousPattern, () -> this.switchExtent(-1));
        this.buttons.put(this.buttonNextPattern, () -> this.switchExtent(1));

        this.buttonPreviousPattern.visible = true;
        this.buttonPreviousPattern.enabled = true;
        this.buttonNextPattern.visible = true;
        this.buttonNextPattern.enabled = true;
    }

    @Unique
    private void switchExtent(int amount) {
        MultiblockControllerBase controller = infoPage.getController();
        int minExtent = controller.getMinExtent();
        int maxExtent = controller.getMaxExtent();
        int newExtent = Math.max(minExtent, Math.min(maxExtent, this.currentExtent + amount));
        if (newExtent == this.currentExtent) return;
        this.currentExtent = newExtent;
        this.buttonPreviousPattern.enabled = newExtent > minExtent;
        this.buttonNextPattern.enabled = newExtent < maxExtent;
        this.rebuildScene();
        this.triggerStructureCheck(this.renderer.world);
    }

    @Unique
    private void switchVoltage(int amount) {
        int maxIndex = 14;
        int newIndex = Math.max(0, Math.min(maxIndex, this.tj_voltageIndex + amount));
        if (newIndex == this.tj_voltageIndex) return;
        this.tj_voltageIndex = newIndex;
        this.buttonVoltage.displayString = TJValues.VCC[newIndex] + GAValues.VN[newIndex];

        for (StructureChannels ch : StructureChannels.values()) {
            channelState.set(ch, newIndex);
        }
        this.rebuildScene();
        this.triggerStructureCheck(this.renderer.world);
    }


    @Inject(method = "triggerStructureCheck", at = @At("HEAD"))
    private void injectSyncExtentBeforeCheck(WorldSceneRenderer.TrackedDummyWorld world, CallbackInfo ci) {
        if (!this.multiLayer || this.controllerPos == null) return;
        TileEntity te = world.getTileEntity(this.controllerPos);
        if (te instanceof MetaTileEntityHolder) {
            MetaTileEntity mte = ((MetaTileEntityHolder) te).getMetaTileEntity();
            if (mte instanceof IJEIExtentSync) {
                ((IJEIExtentSync) mte).setJEIPreviewLayer(this.currentExtent);
            }
        }
    }

    @Inject(method = "switchChannel", at = @At("TAIL"))
    private void injectSwitchChannel(int amount, CallbackInfo ci) {
        if (this.multiLayer && this.buttonVoltage != null) {
            this.buttonVoltage.displayString = TJValues.VCC[this.currentChannelIndex] + GAValues.VN[this.currentChannelIndex];
        }
    }

}
