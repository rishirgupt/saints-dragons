package com.leon.saintsdragons.client.renderer.vfx;

import com.mojang.blaze3d.vertex.VertexConsumer;

public final class BloodTempestAfterimageVertexConsumer implements VertexConsumer {
    private final VertexConsumer delegate;
    private final float red;
    private final float green;
    private final float blue;
    private final float alpha;

    private BloodTempestAfterimageVertexConsumer(VertexConsumer delegate) {
        this.delegate = delegate;
        this.red = BloodTempestAfterimageRenderContext.red();
        this.green = BloodTempestAfterimageRenderContext.green();
        this.blue = BloodTempestAfterimageRenderContext.blue();
        this.alpha = BloodTempestAfterimageRenderContext.alpha();
    }

    public static VertexConsumer wrap(VertexConsumer delegate) {
        return BloodTempestAfterimageRenderContext.isActive()
                ? new BloodTempestAfterimageVertexConsumer(delegate)
                : delegate;
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        delegate.addVertex(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer setColor(int red, int green, int blue, int alpha) {
        delegate.setColor(channel(this.red), channel(this.green), channel(this.blue), channel(this.alpha));
        return this;
    }

    @Override
    public VertexConsumer setColor(int color) {
        return setColor(0, 0, 0, 0);
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        delegate.setUv(u, v);
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

    private static int channel(float value) {
        return Math.round(value * 255.0F);
    }
}
