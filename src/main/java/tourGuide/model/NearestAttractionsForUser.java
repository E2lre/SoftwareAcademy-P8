package tourGuide.model;


import tourGuide.user.User;

import java.util.List;

/**
 * Give list of nearest attraction for a user
 */
public class NearestAttractionsForUser {

    // Name of Tourist attraction, //TODO commentaire à supprimer
    // Tourist attractions lat/long,
    // The user's location lat/long,
    // The distance in miles between the user's location and each of the attractions.
    // The reward points for visiting each Attraction.
    //    Note: Attraction reward points can be gathered from RewardsCentral


    private UserDTO user;
    private List<NearestAttraction> nearestAttractions;


    public NearestAttractionsForUser(UserDTO user, List<NearestAttraction> nearestAttractions) {
        this.user = user;
        this.nearestAttractions = nearestAttractions;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }


    public List<NearestAttraction> getNearestAttractions() {
        return nearestAttractions;
    }

    public void setNearestAttractions(List<NearestAttraction> nearestAttractions) {
        this.nearestAttractions = nearestAttractions;
    }


    @Override
    public String toString() {
        return "NearestAttractionsForUser{" +
                "user=" + user +
                ", nearestAttractions=" + nearestAttractions +
                '}';
    }

}
