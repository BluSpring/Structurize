package com.ldtteam.structurize.client.model;

import io.github.fabricators_of_create.porting_lib.models.geometry.IUnbakedGeometry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

/**
 * Simple wrapper to create {@link OverlaidBakedModel}.
 */
public class OverlaidGeometry implements IUnbakedGeometry<OverlaidGeometry>
{
    private ResourceLocation overlayModelId;

    public OverlaidGeometry(final ResourceLocation overlayModelId)
    {
        this.overlayModelId = overlayModelId;
    }

    @Override
    public BakedModel bake(BlockModel context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, ResourceLocation modelLocation, boolean isGui3d) {
        UnbakedModel unbaked = baker.getModel(overlayModelId);
        BakedModel baked = unbaked.bake(baker, spriteGetter, modelState, overlayModelId);

        if (baked == null)
        {
            baked = Minecraft.getInstance().getModelManager().getMissingModel();
        }

        return new OverlaidBakedModel(baked);
    }
}
