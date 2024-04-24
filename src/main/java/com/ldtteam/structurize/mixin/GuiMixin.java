package com.ldtteam.structurize.mixin;

import com.ldtteam.structurize.fabric.CustomHighlightTooltipItem;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.gui.Gui;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Gui.class)
public class GuiMixin {
    @Shadow private ItemStack lastToolHighlight;

    @ModifyArg(method = "renderSelectedItemName", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;width(Lnet/minecraft/network/chat/FormattedText;)I"))
    private FormattedText structurize$useHighlightTipForWidth(FormattedText text, @Share("highlightTip") LocalRef<Component> highlightTip) {
        if (this.lastToolHighlight.getItem() instanceof CustomHighlightTooltipItem highlightTooltipItem) {
            highlightTip.set(highlightTooltipItem.getHighlightTip(this.lastToolHighlight, (Component) text));
        }

        return text;
    }

    @ModifyArg(method = "renderSelectedItemName", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)I"), index = 1)
    private Component structurize$useHighlightTip(Component text, @Share("highlightTip") Component highlightTip) {
        if (highlightTip == null)
            return text;

        return highlightTip;
    }
}
