package tj.mixin.gregtech.client;

import gregicadditions.GAValues;
import gregtech.api.render.scene.WorldSceneRenderer;
import gregtech.api.util.BlockInfo;
import gregtech.api.util.ItemStackKey;
import gregtech.integration.jei.multiblock.MultiblockInfoPage;
import gregtech.integration.jei.multiblock.MultiblockInfoRecipeWrapper;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.gui.recipes.RecipeLayout;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import tj.TJConfig;
import tj.TJValues;
import tj.integration.jei.MBPattern;
import tj.integration.jei.PartInfo;
import tj.integration.jei.TJMultiblockShapeInfo;
import tj.integration.jei.multi.parallel.IParallelMultiblockInfoPage;

import java.util.*;


@Mixin(value = MultiblockInfoRecipeWrapper.class, remap = false)
public abstract class MultiblockInfoRecipeWrapperMixin {

    @Shadow
    @Final
    private int ICON_SIZE;

    @Shadow
    @Final
    private int RIGHT_PADDING;

    @Shadow
    private Map<GuiButton, Runnable> buttons;


    @Shadow
    protected abstract void switchChannel(int amount);

    @Shadow
    private int currentChannelIndex;
    @Unique
    private GuiButton buttonVoltage;

    @Unique
    private boolean multiLayer;

    @Inject(method = "<init>", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void injectMultiblockInfoRecipeWrapper_Init(MultiblockInfoPage infoPage, CallbackInfo ci, HashSet<ItemStackKey> drops) {
        if (infoPage instanceof IParallelMultiblockInfoPage) {
            this.multiLayer = true;
        }
    }

    @Inject(method = "setRecipeLayout", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void injectSetRecipeLayout(RecipeLayout layout, IGuiHelper guiHelper, CallbackInfo ci, IDrawable border, boolean isPagesDisabled) {
        if (this.multiLayer) {
            this.buttonVoltage = new GuiButton(0, border.getWidth() - ((2 * ICON_SIZE) + RIGHT_PADDING + 1), 110, ICON_SIZE + 21, ICON_SIZE, TJValues.VCC[this.currentChannelIndex] + GAValues.VN[this.currentChannelIndex]);
            this.buttons.put(this.buttonVoltage, () -> this.switchChannel(Mouse.isButtonDown(0) ? 1 : Mouse.isButtonDown(1) ? -1 : 0));
        }
    }

}
