package org.cordell.com.anizottiradiation.objects;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

@Setter
@Getter
public class Area {
    public Area(Location firstBound, Location secondBound)  {
        firstLocation = firstBound;
        secondLocation = secondBound;
    }

    private Location firstLocation;
    private Location secondLocation;

    public Location getCenter() {
        double centerX = (firstLocation.getX() + secondLocation.getX()) / 2;
        double centerY = (firstLocation.getY() + secondLocation.getY()) / 2;
        double centerZ = (firstLocation.getZ() + secondLocation.getZ()) / 2;
        return new Location(firstLocation.getWorld(), centerX, centerY, centerZ);
    }
}
