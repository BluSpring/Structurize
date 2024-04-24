package com.ldtteam.structurize.fabric;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface CustomHighlightTooltipItem {
    default Component getHighlightTip(@NotNull final ItemStack stack, @NotNull final Component displayName) {
        return displayName;
    }
}
