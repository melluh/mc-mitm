package com.melluh.mcmitm.util;

import dev.dewy.nbt.tags.collection.CompoundTag;

public record ItemStack(int itemId, byte itemCount, CompoundTag tag) {

    public boolean isAir() {
        return itemId == 0;
    }

}
