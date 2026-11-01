package tj.event;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.function.Consumer;

public class RenderEventHandler {

    private static final Object2ObjectMap<String, Consumer<RenderWorldLastEvent>> RENDER_LAST_EVENT_CALLBACKS = new Object2ObjectOpenHashMap<>();

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        for (Consumer<RenderWorldLastEvent> entry : RENDER_LAST_EVENT_CALLBACKS.values())
            entry.accept(event);
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        RENDER_LAST_EVENT_CALLBACKS.clear();
    }

    public static void addToRenderWorldLastEvent(String key, Consumer<RenderWorldLastEvent> event) {
        RENDER_LAST_EVENT_CALLBACKS.put(key, event);
    }

    public static void removeFromRenderWorldLastEvent(String key) {
        RENDER_LAST_EVENT_CALLBACKS.remove(key);
    }
}
