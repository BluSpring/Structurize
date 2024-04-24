package com.ldtteam.structurize.event;

import com.ldtteam.blockui.BOScreen;
import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.api.util.BlockPosUtil;
import com.ldtteam.structurize.api.util.IScrollableItem;
import com.ldtteam.structurize.api.util.ISpecialBlockPickItem;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blockentities.interfaces.IBlueprintDataProviderBE;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.client.BlueprintHandler;
import com.ldtteam.structurize.client.ModKeyMappings;
import com.ldtteam.structurize.client.gui.WindowExtendedBuildTool;
import com.ldtteam.structurize.items.ItemScanTool;
import com.ldtteam.structurize.items.ItemTagTool;
import com.ldtteam.structurize.items.ModItems;
import com.ldtteam.structurize.network.messages.ItemMiddleMouseMessage;
import com.ldtteam.structurize.network.messages.ScanToolTeleportMessage;
import com.ldtteam.structurize.storage.rendering.RenderingCache;
import com.ldtteam.structurize.storage.rendering.types.BlueprintPreviewData;
import com.ldtteam.structurize.storage.rendering.types.BoxPreviewData;
import com.ldtteam.structurize.util.WorldRenderMacros;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.fabricators_of_create.porting_lib.event.client.MouseInputEvents;
import io.github.fabricators_of_create.porting_lib.event.client.OverlayRenderCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;

public class ClientEventSubscriber
{
    public static void init() {
        OverlayRenderCallback.EVENT.register((guiGraphics, partialTicks, window, type) -> {
            if (type == OverlayRenderCallback.Types.PLAYER_HEALTH && Minecraft.getInstance().screen instanceof BOScreen &&
                ((BOScreen) Minecraft.getInstance().screen).getWindow() instanceof WindowExtendedBuildTool)
            {
                return true;
            }

            return false;
        });

        /*WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> {
            var shouldRender = !(Structurize.getConfig().getClient().rendererTransparency.get() > TransparencyHack.THRESHOLD);

            if (shouldRender)
                renderWorldLastEvent(context);
        });

        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            var shouldRender = Structurize.getConfig().getClient().rendererTransparency.get() > TransparencyHack.THRESHOLD;

            if (shouldRender)
                renderWorldLastEvent(context);
        });*/

        WorldRenderEvents.BEFORE_DEBUG_RENDER.register(ClientEventSubscriber::renderWorldLastEvent);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            onClientTickEvent();
        });

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            onPreClientTickEvent();
        });

        MouseInputEvents.BEFORE_SCROLL.register((deltaX, deltaY) -> {
            return onMouseWheel(deltaY);
        });
    }


    /**
     * Used to catch the renderWorldLastEvent in order to draw the debug nodes for pathfinding.
     */
    public static void renderWorldLastEvent(WorldRenderContext context)
    {
        final PoseStack matrixStack = context.matrixStack();
        final MultiBufferSource.BufferSource bufferSource = WorldRenderMacros.getBufferSource();

        final Minecraft mc = Minecraft.getInstance();
        final Vec3 viewPosition = mc.gameRenderer.getMainCamera().getPosition();
        matrixStack.pushPose();
        matrixStack.translate(-viewPosition.x(), -viewPosition.y(), -viewPosition.z());

        for (final BlueprintPreviewData previewData : RenderingCache.getBlueprintsToRender())
        {
            final Blueprint blueprint = previewData.getBlueprint();

            if (blueprint != null)
            {
                mc.getProfiler().push("struct_render");

                final BlockPos pos = previewData.getPos();
                final BlockPos posMinusOffset = pos.subtract(blueprint.getPrimaryBlockOffset());

                BlueprintHandler.getInstance().draw(previewData, pos, context);
                WorldRenderMacros.renderWhiteLineBox(bufferSource,
                  matrixStack,
                  posMinusOffset,
                  posMinusOffset.offset(blueprint.getSizeX() - 1, blueprint.getSizeY() - 1, blueprint.getSizeZ() - 1),
                  0.02f);
                WorldRenderMacros.renderRedGlintLineBox(bufferSource, matrixStack, pos, pos, 0.02f);

                mc.getProfiler().pop();
            }
        }

        for (final BoxPreviewData previewData : RenderingCache.getBoxesToRender())
        {
            mc.getProfiler().push("struct_box");

            // Used to render a red box around a scan's Primary offset (primary block)
            WorldRenderMacros.renderWhiteLineBox(bufferSource, matrixStack, previewData.getPos1(), previewData.getPos2(), 0.02f);
            previewData.getAnchor().ifPresent(pos -> WorldRenderMacros.renderRedGlintLineBox(bufferSource, matrixStack, pos, pos, 0.02f));

            mc.getProfiler().pop();
        }


        final Player player = mc.player;
        final ItemStack itemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (itemStack.getItem() == ModItems.tagTool.get() && itemStack.getOrCreateTag().contains(ItemTagTool.TAG_ANCHOR_POS))
        {
            mc.getProfiler().push("struct_tags");

            final BlockPos tagAnchor = BlockPosUtil.readFromNBT(itemStack.getTag(), ItemTagTool.TAG_ANCHOR_POS);
            final BlockEntity te = player.level().getBlockEntity(tagAnchor);

            if (te instanceof IBlueprintDataProviderBE)
            {
                final Map<BlockPos, List<String>> tagPosList = ((IBlueprintDataProviderBE) te).getWorldTagPosMap();

                for (final Map.Entry<BlockPos, List<String>> entry : tagPosList.entrySet())
                {
                    WorldRenderMacros.renderWhiteLineBox(bufferSource, matrixStack, entry.getKey(), entry.getKey(), 0.02f);
                    WorldRenderMacros.renderDebugText(entry.getKey(), entry.getValue(), matrixStack, true, 3, bufferSource);
                }
            }
            WorldRenderMacros.renderRedGlintLineBox(bufferSource, matrixStack, tagAnchor, tagAnchor, 0.02f);

            mc.getProfiler().pop();
        }

        bufferSource.endBatch();
        matrixStack.popPose();
    }

    /**
     * Used to catch the clientTickEvent.
     * Call renderer cache cleaning every 5 secs (100 ticks).
     *
     */
    public static void onClientTickEvent()
    {
        final Minecraft mc = Minecraft.getInstance();
        mc.getProfiler().push("structurize");

        if (mc.level != null && mc.level.getGameTime() % (Constants.TICKS_SECOND * BlueprintHandler.CACHE_EXPIRE_CHECK_SECONDS) == 0)
        {
            mc.getProfiler().push("blueprint_manager_tick");
            BlueprintHandler.getInstance().cleanCache();
            mc.getProfiler().pop();
        }

        if (ModKeyMappings.TELEPORT.get().consumeClick() && mc.level != null && mc.player != null &&
            mc.player.getMainHandItem().getItem() instanceof ItemScanTool tool)
        {
            if (tool.onTeleport(mc.player, mc.player.getMainHandItem()))
            {
                Network.getNetwork().sendToServer(new ScanToolTeleportMessage());
            }
        }

        mc.getProfiler().pop();
    }

    public static void onPreClientTickEvent()
    {
        final Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null || mc.level == null) return;

        if (mc.options.keyPickItem.consumeClick())
        {
            BlockPos pos = mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.BLOCK ? ((BlockHitResult)mc.hitResult).getBlockPos() : null;
            if (pos != null && mc.level.getBlockState(pos).isAir())
            {
                pos = null;
            }

            final ItemStack current = mc.player.getInventory().getSelected();
            if (current.getItem() instanceof ISpecialBlockPickItem clickableItem)
            {
                final boolean ctrlKey = Screen.hasControlDown();
                switch (clickableItem.onBlockPick(mc.player, current, pos, ctrlKey))
                {
                    case PASS:
                        ++mc.options.keyPickItem.clickCount;
                        break;
                    case FAIL:
                        break;
                    default:
                        Network.getNetwork().sendToServer(new ItemMiddleMouseMessage(pos, ctrlKey));
                        break;
                }
            }
            else
            {
                ++mc.options.keyPickItem.clickCount;
            }
        }
    }

    public static boolean onMouseWheel(double scrollDelta)
    {
        final Minecraft mc = Minecraft.getInstance();
        if ( mc.player == null || mc.screen != null || mc.level == null) return true;
        if (!mc.player.isShiftKeyDown()) return false;

        final ItemStack current = mc.player.getInventory().getSelected();
        if (current.getItem() instanceof IScrollableItem scrollableItem)
        {
            final boolean ctrlKey = Screen.hasControlDown();
            switch (scrollableItem.onMouseScroll(mc.player, current, scrollDelta, ctrlKey))
            {
                case PASS:
                    break;
                case FAIL:
                    return true;
                default:
                    Network.getNetwork().sendToServer(new ItemMiddleMouseMessage(scrollDelta, ctrlKey));
                    return true;
            }
        }

        return false;
    }
}
