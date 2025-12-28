package tj.capability;

import net.minecraft.entity.Entity;

public interface LinkEntity extends LinkPos {

    default Entity getEntity(int index) {
        return null;
    }
}
