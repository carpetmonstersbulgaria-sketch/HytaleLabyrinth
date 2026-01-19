package com.labyrinth.portal_scrolls;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.math.vector.Location;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.labyrinth.core.configs.ConfigMessages;

import javax.annotation.Nonnull;
import java.util.logging.Level;

import static com.hypixel.hytale.builtin.hytalegenerator.LoggerUtil.getLogger;

public class ScrollPlaceBlockSystem extends EntityEventSystem<EntityStore, PlaceBlockEvent> {

    public ScrollPlaceBlockSystem() {
        super(PlaceBlockEvent.class);
    }

    @Override
    public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> chunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer, @Nonnull PlaceBlockEvent event) {
        Ref<EntityStore> ref = chunk.getReferenceTo(index);
        UUIDComponent uuidComponent = chunk.getComponent(index, UUIDComponent.getComponentType());
        if (uuidComponent == null) {
            return;
        }

        World world = store.getExternalData().getWorld();
        Entity entity = world.getEntity(uuidComponent.getUuid());

        if (!(entity instanceof Player player)) {
            return;
        }

        ItemStack item = player.getInventory().getHotbar().getItemStack(player.getInventory().getActiveHotbarSlot());

        if (item == null || !item.getItemId().equals(PortalScrollEntity.HYTALE_ID)) {
            return;
        }

        event.setCancelled(true);

        var location = new Location(event.getTargetBlock());
        location.setWorld(world.getName());
        handlePortalScroll(player, item, location);
    }

    private void handlePortalScroll(Player player, ItemStack item, Location location) {
        var metadata = item.getMetadata();

        if (metadata == null) {
            return;
        }

        var bsonValue = metadata.get(PortalScrollEntity.METADATA_ID);
        long databaseId = bsonValue.isInt64() ? bsonValue.asInt64().getValue() : bsonValue.asInt32().getValue();
        var entity = PortalScrollService.getInstance().findById(databaseId);

        if (entity.isEmpty()) {
            getLogger().log(Level.SEVERE, "Orphan portal scroll without database id: " + databaseId);
            return;
        }

        if (entity.get().hasLocation()) {
            PortalScrollService.getInstance().delete(entity.get());
            ScrollUtils.spawn(entity.get(), location);
            player.getInventory().getHotbar().removeItemStackFromSlot(player.getInventory().getActiveHotbarSlot(), 1);
            player.sendMessage(Message.raw(ConfigMessages.SCROLLS.PORTALS_CREATED));
        } else {
            entity.get().fromLocation(location);
            PortalScrollService.getInstance().merge(entity.get());
            player.sendMessage(Message.raw(ConfigMessages.SCROLLS.PORTALS_LOCATION_SET));
        }
    }

    @Override
    public Archetype<EntityStore> getQuery() {
        return Archetype.empty();
    }
}