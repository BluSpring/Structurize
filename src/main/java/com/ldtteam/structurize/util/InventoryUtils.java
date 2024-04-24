package com.ldtteam.structurize.util;

import com.ldtteam.structurize.api.util.ItemStackUtils;
import io.github.fabricators_of_create.porting_lib.transfer.item.SlottedStackStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Structurize specific inventory utilities.
 */
public class InventoryUtils
{
    /**
     * Check if an inventory has all the required stacks.
     * @param inventory the inventory to check.
     * @param requiredItems the list of items.
     * @return true if so, else false.
     */
    public static boolean hasRequiredItems(final SlottedStackStorage inventory, final List<ItemStack> requiredItems)
    {
        final List<ItemStack> listToDiscount = new ArrayList<>();
        for (final ItemStack stack : requiredItems)
        {
            listToDiscount.add(stack.copy());
        }

        for (int slot = 0; slot < inventory.getSlotCount(); slot++)
        {
            final ItemStack content = inventory.getStackInSlot(slot);
            if (content.isEmpty())
            {
                continue;
            }
            int contentCount = content.getCount();

            for (final ItemStack stack : listToDiscount)
            {
                if (!stack.isEmpty() && ItemStackUtils.compareItemStacksIgnoreStackSize(stack, content))
                {
                    if (stack.getCount() < content.getCount())
                    {
                        contentCount = contentCount - stack.getCount();
                        stack.setCount(0);
                    }
                    else
                    {
                        stack.setCount(stack.getCount() - contentCount);
                        break;
                    }
                }
            }
        }

        for (final ItemStack stack : listToDiscount)
        {
            if (!stack.isEmpty())
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Method to transfers a stack to the next best slot in the target inventory.
     *
     * @param targetHandler The {@link SlottedStackStorage} that works as Target.
     */
    public static void transferIntoNextBestSlot(final ItemStack stack, final SlottedStackStorage targetHandler)
    {
        if(stack.isEmpty())
        {
            return;
        }

        ItemStack sourceStack = stack.copy();
        for (int i = 0; i < targetHandler.getSlotCount(); i++)
        {
            var transaction = Transaction.openOuter();
            var total = targetHandler.insertSlot(i, ItemVariant.of(sourceStack), sourceStack.getCount(), transaction);
            sourceStack = sourceStack.copyWithCount(sourceStack.getMaxStackSize() - (int) total);
            transaction.commit();
            transaction.close();
            if (sourceStack.isEmpty())
            {
                return;
            }
        }
    }

    /**
     * Consume an ItemStack from an itemhandler.
     * @param tempStack the stack.
     * @param handler the handler.
     */
    public static void consumeStack(final ItemStack tempStack, final SlottedStackStorage handler)
    {
        int count = tempStack.getCount();
        final ItemStack container = tempStack.getRecipeRemainder();

        for (int i = 0; i < handler.getSlotCount(); i++)
        {
            if (ItemStackUtils.compareItemStacksIgnoreStackSize(handler.getStackInSlot(i), tempStack))
            {
                var stackInSlot = handler.getStackInSlot(i);
                var transaction = Transaction.openOuter();
                var total = handler.extractSlot(i, ItemVariant.of(stackInSlot), count, transaction);
                transaction.commit();
                transaction.close();

                final ItemStack result = stackInSlot.copyWithCount((int) total);
                if (result.getCount() == count)
                {
                    if (!container.isEmpty())
                    {
                        for (int j = 0; j < tempStack.getCount(); j++)
                        {
                            transferIntoNextBestSlot(container, handler);
                        }
                    }
                    return;
                }
                count -= result.getCount();
            }
        }
    }
}
