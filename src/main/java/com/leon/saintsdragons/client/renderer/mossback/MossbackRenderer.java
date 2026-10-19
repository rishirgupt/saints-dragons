package com.leon.saintsdragons.client.renderer.mossback;

import com.leon.saintsdragons.client.model.mossback.MossbackModel;
import com.leon.saintsdragons.server.entity.dragons.Mossback;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import com.leon.saintsdragons.client.render26.LivingGeoRenderer;
import com.leon.saintsdragons.client.render26.ObjectGeoRenderer;

public class MossbackRenderer extends LivingGeoRenderer<Mossback> {
    public MossbackRenderer(EntityRendererProvider.Context context) {
        super(context, new MossbackModel());
        this.shadowRadius = 0.3F;
    }

    @Override
    public float getMotionAnimThreshold(Mossback animatable) {
        return 0.000001F;
    }

}
