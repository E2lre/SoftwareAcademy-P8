package tourGuide.model;

import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Give information about 1 nearest attraction
 */
public class NearestAttraction {
// Name of Tourist attraction, //TODO commentaire à supprimer
    // Tourist attractions lat/long, ==>Yes
    // The user's location lat/long,==>No
    // The distance in miles between the user's location and each of the attractions.==>Yes
    // The reward points for visiting each Attraction.==>Yes
    //    Note: Attraction reward points can be gathered from RewardsCentral

    //private  User attraction;


    private Attraction attraction;
    private Double distance;
    private int rewardPoints;

    public NearestAttraction(Attraction attraction, Double distance, int rewardPoints) {
        this.attraction = attraction;
        this.distance = distance;
        this.rewardPoints = rewardPoints;
    }

    public Attraction getAttraction() {
        return attraction;
    }

    public void setAttraction(Attraction attraction) {
        this.attraction = attraction;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public int getRewardPoints() {
        return rewardPoints;
    }

    public void setRewardPoints(int rewardPoints) {
        this.rewardPoints = rewardPoints;
    }

    @Override
    public String toString() {
        return "NearestAttraction{" +
                "attraction=" + attraction +
                ", distance=" + distance +
                ", rewardPoints=" + rewardPoints +
                '}';
    }

}
