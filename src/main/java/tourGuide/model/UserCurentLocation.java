package tourGuide.model;

import gpsUtil.location.Location;

import java.util.UUID;

public class UserCurentLocation {


    private UUID userId;
    private Location location;

    public UserCurentLocation() {
    }

    public UserCurentLocation(UUID userId, Location location) {
        this.userId = userId;
        this.location = location;
    }


    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }




    @Override
    public String toString() {
        return "UserCurentLocation{" +
                "userId=" + userId +
                ", lastVisitedLocation=" + location +
                '}';
    }


}
