package tj.builder.multicontrollers;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import gregicadditions.capabilities.IMultiRecipe;
import gregtech.api.recipes.RecipeMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

public abstract class TJMultiRecipeMapMultiblockController extends TJRecipeMapMultiblockController implements IMultiRecipe {

    protected final RecipeMap<?>[] recipeMaps;
    protected int recipeMapIndex;

    public TJMultiRecipeMapMultiblockController(ResourceLocation metaTileEntityId, RecipeMap<?>... recipeMaps) {
        this(metaTileEntityId, true, true, recipeMaps);
    }

    public TJMultiRecipeMapMultiblockController(ResourceLocation metaTileEntityId, boolean hasMaintenance, boolean hasDistinct, RecipeMap<?>... recipeMaps) {
        super(metaTileEntityId, recipeMaps[0], hasMaintenance, hasDistinct);
        this.recipeMaps = recipeMaps;
    }

    @Override
    protected void addDisplayText(GUIDisplayBuilder builder) {
        super.addDisplayText(builder);
        if (!this.isStructureFormed()) return;
        builder.addTextComponent(new TextComponentTranslation("gregtech.multiblock.recipe", new TextComponentTranslation("recipemap." + this.recipeMaps[this.getRecipeMapIndex()].getUnlocalizedName() + ".name")
                .setStyle(new Style().setColor(TextFormatting.AQUA))));
    }

    @Override
    public boolean onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        if (!this.getWorld().isRemote && !this.recipeLogic.isActive()) {
            if (++this.recipeMapIndex == this.recipeMaps.length)
                this.recipeMapIndex = 0;
            playerIn.sendMessage(new TextComponentTranslation("tj.multiblock.multi_recipemap.switched", new TextComponentTranslation("recipemap." + this.recipeMaps[this.getRecipeMapIndex()].getUnlocalizedName() + ".name")));
            this.writeCustomData(10, buffer -> buffer.writeInt(this.recipeMapIndex));
            this.markDirty();
        }
        return true;
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == 10) {
            this.recipeMapIndex = buf.readInt();
            this.scheduleRenderUpdate();
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeInt(this.recipeMapIndex);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.recipeMapIndex = buf.readInt();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("recipeMapIndex", this.recipeMapIndex);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.recipeMapIndex = data.getInteger("recipeMapIndex");
    }

    @Override
    public RecipeMap<?> getRecipeMap() {
        return this.recipeMaps[this.getRecipeMapIndex()];
    }

    @Override
    public RecipeMap<?>[] getRecipeMaps() {
        return this.recipeMaps;
    }

    @Override
    public int getRecipeMapIndex() {
        return this.recipeMapIndex;
    }

    @Override
    public String getRecipeMapNames() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < this.getRecipeMaps().length; i++) {
            builder.append(this.getRecipeMaps()[i].getLocalizedName());
            if (i < this.getRecipeMaps().length - 1)
                builder.append(", ");
        }
        return builder.toString();
    }

    @Override
    public void addRecipeMaps(RecipeMap<?>[] recipeMaps) {
        // don't plan on adding more recipemaps once initialized
    }
}
