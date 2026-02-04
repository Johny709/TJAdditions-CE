package appeng.items.parts;

import appeng.api.parts.IPart;
import appeng.core.AEConfig;
import appeng.core.AppEng;
import appeng.core.features.AEFeature;
import appeng.core.localization.GuiText;
import appeng.integration.IntegrationRegistry;
import appeng.integration.IntegrationType;
import appeng.util.Platform;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import appeng.tile.misc.TJPartFluidInterface;
import appeng.tile.misc.PartSuperInterface;

import java.lang.reflect.Constructor;
import java.util.*;

public enum TJPartType {
    INVALID_TYPE(-1, "invalid", EnumSet.of(AEFeature.CORE), EnumSet.noneOf(IntegrationType.class), null),
    SUPER_INTERFACE(0, "super_interface", EnumSet.of(AEFeature.INTERFACE), EnumSet.noneOf(IntegrationType.class), PartSuperInterface.class),
    SUPER_FLUID_INTERFACE(1, "super_fluid_interface", EnumSet.of(AEFeature.FLUID_INTERFACE), EnumSet.noneOf(IntegrationType.class), TJPartFluidInterface.class);

    private final int baseDamage;
    private final Set<AEFeature> features;
    private final Set<IntegrationType> integrations;
    private final Class<? extends IPart> myPart;
    private final GuiText extraName;
    @SideOnly(Side.CLIENT)
    private List<ModelResourceLocation> itemModels;
    private final Set<ResourceLocation> models;
    private final boolean enabled;
    private Constructor<? extends IPart> constructor;
    private final String oreName;

    TJPartType(final int baseMetaValue, final String itemModel, final Set<AEFeature> features, final Set<IntegrationType> integrations, final Class<? extends IPart> c) {
        this(baseMetaValue, itemModel, features, integrations, c, null, null);
    }

    TJPartType(final int baseMetaValue, final String itemModel, final Set<AEFeature> features, final Set<IntegrationType> integrations, final Class<? extends IPart> c, final String oreDict) {
        this(baseMetaValue, itemModel, features, integrations, c, null, oreDict);
    }

    TJPartType(final int baseMetaValue, final String itemModel, final Set<AEFeature> features, final Set<IntegrationType> integrations, final Class<? extends IPart> c, final GuiText en) {
        this(baseMetaValue, itemModel, features, integrations, c, en, null);
    }

    TJPartType(final int baseMetaValue, final String itemModel, final Set<AEFeature> features, final Set<IntegrationType> integrations, final Class<? extends IPart> c, final GuiText en, final String oreDict) {
        this.baseDamage = baseMetaValue;
        this.features = Collections.unmodifiableSet(features);
        this.integrations = Collections.unmodifiableSet(integrations);
        this.myPart = c;
        this.extraName = en;
        this.oreName = oreDict;

        // The part is enabled if all features + integrations it needs are enabled
        this.enabled = features.stream().allMatch(AEConfig.instance()::isFeatureEnabled) && integrations.stream().allMatch(IntegrationRegistry.INSTANCE::isEnabled);

        if (this.enabled) {
            // Only load models if the part is enabled, otherwise we also run into class-loading issues while
            // scanning for annotations
            if (Platform.isClientInstall()) {
                this.itemModels = this.createItemModels(itemModel);
            }
            if (c != null) {
                this.models = new HashSet<>(TJPartModelsHelper.createModels(c));
            } else {
                this.models = Collections.emptySet();
            }
        } else {
            if (Platform.isClientInstall()) {
                this.itemModels = Collections.emptyList();
            }
            this.models = Collections.emptySet();
        }
    }

    @SideOnly(Side.CLIENT)
    private List<ModelResourceLocation> createItemModels(String baseName) {
        return ImmutableList.of(modelFromBaseName(baseName));
    }

    @SideOnly(Side.CLIENT)
    private static ModelResourceLocation modelFromBaseName(String baseName) {
        return new ModelResourceLocation(new ResourceLocation(AppEng.MOD_ID, "part/" + baseName), "inventory");
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    int getBaseDamage() {
        return this.baseDamage;
    }

    public boolean isCable() {
        return false;
    }

    Set<AEFeature> getFeature() {
        return this.features;
    }

    Set<IntegrationType> getIntegrations() {
        return this.integrations;
    }

    Class<? extends IPart> getPart() {
        return this.myPart;
    }

    String getUnlocalizedName() {
        return this.name().toLowerCase();
    }

    GuiText getExtraName() {
        return this.extraName;
    }

    Constructor<? extends IPart> getConstructor() {
        return this.constructor;
    }

    void setConstructor(final Constructor<? extends IPart> constructor) {
        this.constructor = constructor;
    }

    public String getOreName() {
        return this.oreName;
    }

    @SideOnly(Side.CLIENT)
    public List<ModelResourceLocation> getItemModels() {
        return this.itemModels;
    }

    public Set<ResourceLocation> getModels() {
        return this.models;
    }
}
