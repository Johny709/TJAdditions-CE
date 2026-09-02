package tj.integration.theoneprobe.impl;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import mcjty.theoneprobe.api.IElement;
import mcjty.theoneprobe.network.NetworkTools;
import net.minecraft.item.ItemStack;
import tj.integration.theoneprobe.TheOneProbeCompatibility;
import tj.mui.TJGuiUtils;
import tj.util.map.Strategies;
import tj.util.wrappers.GTItemStackWrapper;

import java.util.Collection;

public class ElementItemList implements IElement {

    private final Object2ObjectMap<ItemStack, GTItemStackWrapper> itemMap = new Object2ObjectLinkedOpenCustomHashMap<>(Strategies.ITEMSTACK_STRATEGY);
    private Collection<ItemStack> itemStacks;

    public ElementItemList(Collection<ItemStack> itemStacks) {
        this.itemStacks = itemStacks;
    }

    public ElementItemList(ByteBuf byteBuf) {
        final int size = byteBuf.readInt();
        for (int i = 0; i < size; i++) {
            final ItemStack itemStack = NetworkTools.readItemStack(byteBuf);
            this.itemMap.computeIfAbsent(itemStack, key -> new GTItemStackWrapper(key, 0))
                    .increment(itemStack.getCount());
        }
    }

    @Override
    public void render(int x, int y) {
        int offsetX = 0;
        for (Object2ObjectMap.Entry<ItemStack, GTItemStackWrapper> entry : this.itemMap.object2ObjectEntrySet())
            TJGuiUtils.drawItemStack(x + (18 * offsetX++), y, entry.getKey(), entry.getValue().getCountLong());
    }

    @Override
    public int getWidth() {
        return 18 * this.itemMap.size();
    }

    @Override
    public int getHeight() {
        return 18;
    }

    @Override
    public void toBytes(ByteBuf byteBuf) {
        byteBuf.writeInt(this.itemStacks.size());
        for (ItemStack itemStack : this.itemStacks)
            NetworkTools.writeItemStack(byteBuf, itemStack);
    }

    @Override
    public int getID() {
        return TheOneProbeCompatibility.ELEMENT_ITEMLIST;
    }
}
