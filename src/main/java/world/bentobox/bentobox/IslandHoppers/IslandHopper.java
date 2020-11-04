package world.bentobox.bentobox.IslandHoppers;

import com.google.common.collect.Lists;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.List;
import java.util.UUID;

public class IslandHopper {
    private UUID id;
    private Location location;
    private List<Material> filter = Lists.newArrayList();
    private boolean enabled = false;

    public IslandHopper(UUID id, Location location){
        this.id = id;
        this.location = location;
    }
    public IslandHopper(Location location){
        this.id = UUID.randomUUID();
        this.location = location;
    }

    public UUID getID(){
        return this.id;
    }

    public List<Material> getFilter(){
        return this.filter;
    }

    public Location getLocation(){
        return this.location;
    }

    public boolean isEnabled(){
        return this.enabled;
    }

    public void setFilter(List<Material> filter){
        this.filter = filter;
    }

}
