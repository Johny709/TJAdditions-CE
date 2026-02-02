package tj.integration.appeng.items.materials;

import appeng.core.AppEng;
import appeng.core.features.AEFeature;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import tj.integration.appeng.items.TJAE2MaterialStackSrc;

import java.util.EnumSet;
import java.util.Set;

public enum TJAE2MaterialType {
    CELL_65M_PART(0, "material_cell65m_part", EnumSet.of(AEFeature.STORAGE_CELLS)),
    CELL_262M_PART(1, "material_cell262m_part", EnumSet.of(AEFeature.STORAGE_CELLS)),
    CELL_1048M_PART(2, "material_cell1048m_part", EnumSet.of(AEFeature.STORAGE_CELLS)),
    ITEM_CELL_DIGITAL_SINGULARITY(3, "material_item_digital_singularity_part", EnumSet.of(AEFeature.STORAGE_CELLS)),
    FLUID_CELL_65M_PART(4, "material_fluid_cell65m_part", EnumSet.of(AEFeature.STORAGE_CELLS)),
    FLUID_CELL_262M_PART(5, "material_fluid_cell262m_part", EnumSet.of(AEFeature.STORAGE_CELLS)),
    FLUID_CELL_1048M_PART(6, "material_fluid_cell1048m_part", EnumSet.of(AEFeature.STORAGE_CELLS)),
    FLUID_CELL_DIGITAL_SINGULARITY(7, "material_fluid_digital_singularity_part", EnumSet.of(AEFeature.STORAGE_CELLS)),
    INVALID_TYPE(-100, "material_invalid_type");

    private final Set<AEFeature> features;
    private final ModelResourceLocation model;
    private Item itemInstance;
    private int damageValue;
    // stack!
    private TJAE2MaterialStackSrc stackSrc;
    private String oreName;
    private Class<? extends Entity> droppedEntity;
    private boolean isRegistered = false;

    TJAE2MaterialType(final int metaValue, String modelName) {
        this(metaValue, modelName, EnumSet.of(AEFeature.CORE));
    }

    TJAE2MaterialType(final int metaValue, String modelName, final Set<AEFeature> features) {
        this.setDamageValue(metaValue);
        this.features = features;
        this.model = new ModelResourceLocation(new ResourceLocation(AppEng.MOD_ID, modelName), "inventory");
    }

    TJAE2MaterialType(final int metaValue, String modelName, final Set<AEFeature> features, final Class<? extends Entity> c) {
        this(metaValue, modelName, features);
        this.droppedEntity = c;
    }

    TJAE2MaterialType(final int metaValue, String modelName, final Set<AEFeature> features, final String oreDictionary, final Class<? extends Entity> c) {
        this(metaValue, modelName, features);
        this.oreName = oreDictionary;
        this.droppedEntity = c;
    }

    TJAE2MaterialType(final int metaValue, String modelName, final Set<AEFeature> features, final String oreDictionary) {
        this(metaValue, modelName, features);
        this.oreName = oreDictionary;
    }

    public ItemStack stack(final int size) {
        return new ItemStack(this.getItemInstance(), size, this.getDamageValue());
    }

    Set<AEFeature> getFeature() {
        return this.features;
    }

    public String getOreName() {
        return this.oreName;
    }

    boolean hasCustomEntity() {
        return this.droppedEntity != null;
    }

    Class<? extends Entity> getCustomEntityClass() {
        return this.droppedEntity;
    }

    public boolean isRegistered() {
        return this.isRegistered;
    }

    void markReady() {
        this.isRegistered = true;
    }

    public int getDamageValue() {
        return this.damageValue;
    }

    void setDamageValue(final int damageValue) {
        this.damageValue = damageValue;
    }

    public Item getItemInstance() {
        return this.itemInstance;
    }

    void setItemInstance(final Item itemInstance) {
        this.itemInstance = itemInstance;
    }

    TJAE2MaterialStackSrc getStackSrc() {
        return this.stackSrc;
    }

    void setStackSrc(final TJAE2MaterialStackSrc stackSrc) {
        this.stackSrc = stackSrc;
    }

    public ModelResourceLocation getModel() {
        return this.model;
    }
}
