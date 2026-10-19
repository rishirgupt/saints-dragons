package com.leon.saintsdragons.client.model.otheranimals;

import com.leon.saintsdragons.common.SaintsDragonsCommon;
import com.leon.saintsdragons.server.entity.otheranimals.Moop;
import com.leon.saintsdragons.client.render26.PosedGeoModel;

public class MoopModel extends PosedGeoModel<Moop> {
    public MoopModel() {
        super(SaintsDragonsCommon.rl("moop/moop"));
    }
}
