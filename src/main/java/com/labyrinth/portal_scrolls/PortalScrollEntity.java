package com.labyrinth.portal_scrolls;

import com.hypixel.hytale.math.vector.Location;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Table(name = "scrolls_portals")
public class PortalScrollEntity {

    public static final String HYTALE_ID = "Portal_Scroll";
    public static final String PORTAL_HYTALE_ID = "Portal_Return";
    public static final String METADATA_ID = "scroll_id";

    @Id
    @GeneratedValue
    private long id;

    private Double x;
    private Double y;
    private Double z;

    private String world;

    public Location toLocation() {
        return new Location(world, x, y, z);
    }

    public boolean hasLocation() {
        return world != null && x != null && y != null && z != null;
    }

    public void fromLocation(Location loc) {
        this.x = loc.getPosition().getX();
        this.y = loc.getPosition().getY();
        this.z = loc.getPosition().getZ();
        this.world = loc.getWorld();
    }
}
