package com.ldtteam.structurize.fabric;

import net.fabricmc.fabric.api.renderer.v1.model.WrapperBakedModel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class BakedModelWrapper<T extends BakedModel> implements BakedModel, WrapperBakedModel {
    protected final T originalModel;

    public BakedModelWrapper(T originalModel)
    {
        this.originalModel = originalModel;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand)
    {
        return originalModel.getQuads(state, side, rand);
    }

    @Override
    public boolean useAmbientOcclusion()
    {
        return originalModel.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d()
    {
        return originalModel.isGui3d();
    }

    @Override
    public boolean usesBlockLight()
    {
        return originalModel.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer()
    {
        return originalModel.isCustomRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleIcon()
    {
        return originalModel.getParticleIcon();
    }

    @Override
    public ItemTransforms getTransforms()
    {
        return originalModel.getTransforms();
    }

    @Override
    public ItemOverrides getOverrides()
    {
        return originalModel.getOverrides();
    }

    @Override
    public boolean isVanillaAdapter() {
        return originalModel.isVanillaAdapter();
    }

    @Override
    public @Nullable BakedModel getWrappedModel() {
        return originalModel;
    }
}
