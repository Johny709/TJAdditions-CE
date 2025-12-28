package tj.machines.multi;

import net.minecraft.util.IStringSerializable;

public enum BatchMode implements IStringSerializable {
        ONE("batch_one", 1),
        FOUR("batch_four", 4),
        SIXTEEN("batch_sixteen", 16),
        SIXTY_FOUR("batch_sixty_four", 64),
        TWO_HUNDRED_FIFTY_SIX("batch_two_hundred_fifty_six", 256);

        BatchMode(String name, int amount) {
            this.name = name;
            this.amount = amount;
        }

        private final String name;
        private final int amount;

        @Override
        public String getName() {
            return name;
        }

        public int getAmount() {
            return amount;
        }
    }