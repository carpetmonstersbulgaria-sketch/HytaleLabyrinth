package com.labyrinth.portal_scrolls;

import com.hypixel.hytale.math.vector.Location;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Table(name = "scrolls_portals")
public class PortalScrollEntity {

    @Id
    @GeneratedValue
    private Long id;

    private double x;
    private double y;
    private double z;

    @Column(nullable = false)
    private String world;

    public Location toLocation() {
        return new Location(world, x, y, z);
    }

    public void fromLocation(Location loc) {
        this.x = loc.getPosition().getX();
        this.y = loc.getPosition().getY();
        this.z = loc.getPosition().getZ();
        this.world = loc.getWorld();
    }
}
