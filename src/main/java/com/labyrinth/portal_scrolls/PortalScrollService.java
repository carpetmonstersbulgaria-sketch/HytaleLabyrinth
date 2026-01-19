package com.labyrinth.portal_scrolls;

import com.hypixel.hytale.math.vector.Location;
import com.labyrinth.core.database.BaseService;

import java.util.List;

public class PortalScrollService extends BaseService<PortalScrollEntity, Long> {

    private static PortalScrollService INSTANCE;

    private PortalScrollService() {
        super(PortalScrollEntity.class);
    }

    public static PortalScrollService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PortalScrollService();
        }
        return INSTANCE;
    }

    public PortalScrollEntity create(Location location) {
        PortalScrollEntity entity = new PortalScrollEntity();
        entity.fromLocation(location);
        save(entity);
        return entity;
    }

    public List<PortalScrollEntity> findByWorld(String world) {
        return query("SELECT p FROM PortalScrollEntity p WHERE p.world = ?1", world);
    }
}