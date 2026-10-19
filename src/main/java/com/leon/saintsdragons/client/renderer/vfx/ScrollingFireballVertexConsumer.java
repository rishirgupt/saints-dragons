package com.leon.saintsdragons.client.renderer.vfx;

import com.mojang.blaze3d.vertex.VertexConsumer;

public final class ScrollingFireballVertexConsumer implements VertexConsumer {
    private final VertexConsumer delegate;
    private final float offset;

    public ScrollingFireballVertexConsumer(VertexConsumer delegate, float offset) {
        this.delegate = delegate;
        this.offset = offset;
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        delegate.addVertex(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer setColor(int red, int green, int blue, int alpha) {
        delegate.setColor(red, green, blue, alpha);
        return this;
    }

    @Override
    public VertexConsumer setColor(int color) {
        delegate.setColor(color);
        return this;
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        delegate.setUv(u, v * 0.5F + offset);
        return this;
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        delegate.setUv1(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        delegate.setUv2(u, v);
        return this;
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        delegate.setNormal(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer setLineWidth(float width) {
        delegate.setLineWidth(width);
        return this;
    }
}
