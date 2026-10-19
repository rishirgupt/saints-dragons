package com.leon.saintsdragons.client.renderer.otheranimals;

import com.leon.saintsdragons.client.model.otheranimals.MoopModel;
import com.leon.saintsdragons.common.SaintsDragonsCommon;
import com.leon.saintsdragons.server.entity.otheranimals.Moop;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import com.leon.saintsdragons.client.render26.LivingGeoRenderer;
import com.leon.saintsdragons.client.render26.ObjectGeoRenderer;

public class MoopRenderer extends LivingGeoRenderer<Moop> {
    public MoopRenderer(EntityRendererProvider.Context context) {
        super(context, new MoopModel());
        this.shadowRadius = 0.25F;
    }

    @Override
    public @NotNull Identifier getTextureLocation(@NotNull Moop entity) {
        return SaintsDragonsCommon.rl("textures/entity/moop/moop.png");
    }
}
