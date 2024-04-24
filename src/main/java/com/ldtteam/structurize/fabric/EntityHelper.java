package com.ldtteam.structurize.fabric;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.phys.HitResult;

public class EntityHelper {
    public static ItemStack getPickedResult(Entity entity, HitResult target)
    {
        ItemStack result = entity.getPickResult();
        if (result == null) {
            SpawnEggItem egg = SpawnEggItem.byId(entity.getType());
            if (egg != null)
                result = new ItemStack(egg);
            else
                result = ItemStack.EMPTY;
        }
        return result;
    }
}
