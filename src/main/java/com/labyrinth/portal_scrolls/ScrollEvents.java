package com.labyrinth.portal_scrolls;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.entity.LivingEntityInventoryChangeEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import org.bson.BsonDocument;
import org.bson.BsonInt64;

public class ScrollEvents {

    /**
     * Handle items metadata setup when giving items, to mark scrolls
     * @param event interaction event
     */
    public static void on(LivingEntityInventoryChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        ItemContainer container = event.getItemContainer();
        var transaction = event.getTransaction();

        for (short slot = 0; slot < container.getCapacity(); slot++) {
            if (!transaction.wasSlotModified(slot)) {
                continue;
            }

            ItemStack item = container.getItemStack(slot);

            if (item == null || !item.getItemId().equals(PortalScrollEntity.HYTALE_ID)) {
                continue;
            }

            BsonDocument metadata = item.getMetadata();

            if (metadata != null && metadata.containsKey(PortalScrollEntity.METADATA_ID)) {
                continue;
            }

            PortalScrollEntity entity = new PortalScrollEntity();
            PortalScrollService.getInstance().save(entity);

            BsonDocument newMetadata = new BsonDocument();
            newMetadata.append(PortalScrollEntity.METADATA_ID, new BsonInt64(entity.getId()));

            ItemStack newScroll = new ItemStack(item.getItemId(), 1, newMetadata);
            container.setItemStackForSlot(slot, newScroll);
        }
    }
}
