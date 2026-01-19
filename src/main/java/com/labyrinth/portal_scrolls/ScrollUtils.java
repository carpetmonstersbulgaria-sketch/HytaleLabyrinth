package com.labyrinth.portal_scrolls;

import com.hypixel.hytale.builtin.portals.PortalsPlugin;
import com.hypixel.hytale.builtin.portals.integrations.PortalGameplayConfig;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Location;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.packets.interface_.PortalDef;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.asset.type.portalworld.PortalSpawn;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.logging.Level;

import static com.hypixel.hytale.builtin.hytalegenerator.LoggerUtil.getLogger;

public abstract class ScrollUtils {

    public static void spawn(PortalScrollEntity entity, Location location) {
        World world = Universe.get().getWorld(location.getWorld());
        World savedWorld = Universe.get().getWorld(entity.getWorld());

        final String model = "Portal_Return";
        Item asset = Item.getAssetMap().getAsset(model);

        if (asset == null) {
            getLogger().log(Level.SEVERE, "Could not find asset model: " + model);
            return;
        }

        world.setBlock(
                (int) location.getPosition().getX(),
                (int) location.getPosition().getY(),
                (int) location.getPosition().getZ(),
                asset.getBlockId()
        );

        savedWorld.setBlock(
                entity.getX().intValue(),
                entity.getY().intValue(),
                entity.getZ().intValue(),
                asset.getBlockId()
        );
    }
}
