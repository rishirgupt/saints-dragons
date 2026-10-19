package com.leon.saintsdragons.fabric.client.accessor;

import com.leon.saintsdragons.client.renderer.vfx.BloodTempestAfterimageTrail;

import java.util.List;

/** Implemented on entity render states: the Blood Tempest afterimages to draw behind a player this frame. */
public interface AfterimageHolder {
    List<BloodTempestAfterimageTrail.RenderedSnapshot> saintsdragons$getAfterimages();

    void saintsdragons$setAfterimages(List<BloodTempestAfterimageTrail.RenderedSnapshot> afterimages);
}
