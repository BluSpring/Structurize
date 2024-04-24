package com.ldtteam.structurize.client.model;

import com.ldtteam.structurize.fabric.BakedModelWrapper;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.fabricators_of_create.porting_lib.models.TransformTypeDependentItemBakedModel;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import org.jetbrains.annotations.NotNull;

/**
 * This exists because it seems to be the only way to override {@link #isCustomRenderer}...
 */
public class OverlaidBakedModel extends BakedModelWrapper<BakedModel> implements TransformTypeDependentItemBakedModel
{
    public OverlaidBakedModel(@NotNull final BakedModel overlay)
    {
        super(overlay);
    }

    @Override
    public boolean isCustomRenderer()
    {
        return true;
    }

    @Override
    public BakedModel applyTransform(ItemDisplayContext context, PoseStack poseStack, boolean leftHand, DefaultTransform defaultTransform) {
        var transformed = TransformTypeDependentItemBakedModel.maybeApplyTransform(originalModel, context, poseStack, leftHand, defaultTransform);

        if (transformed == null)
            return this;

        return new OverlaidBakedModel(transformed);
    }
}
