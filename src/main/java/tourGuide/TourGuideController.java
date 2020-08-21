package tourGuide;

import java.util.List;

import gpsUtil.location.Attraction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.jsoniter.output.JsonStream;

import gpsUtil.location.VisitedLocation;
import tourGuide.exceptions.UserNameNotFoundException;
import tourGuide.exceptions.UserPreferenceEmptyException;
import tourGuide.model.NearestAttractionsForUser;
import tourGuide.model.UserCurentLocation;
import tourGuide.model.UserPreferenceDTO;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import tourGuide.user.UserPreferences;
import tripPricer.Provider;

@RestController
public class TourGuideController {

    private Logger logger = LoggerFactory.getLogger(TourGuideController.class);

    @Autowired
	TourGuideService tourGuideService;
	
    @RequestMapping("/")
    public String index() {
        return "Greetings from TourGuide!";
    }
    
    @RequestMapping("/getLocation") 
    public String getLocation(@RequestParam String userName) {
    	VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(userName));
		return JsonStream.serialize(visitedLocation.location);
    }
    
    //  TODO: Change this method to no longer return a List of Attractions.==> Done
 	//  Instead: Get the closest five tourist attractions to the user - no matter how far away they are.
 	//  Return a new JSON object that contains:
    	// Name of Tourist attraction, 
        // Tourist attractions lat/long, 
        // The user's location lat/long, 
        // The distance in miles between the user's location and each of the attractions.
        // The reward points for visiting each Attraction.
        //    Note: Attraction reward points can be gathered from RewardsCentral
 /* Retreive By EDE */
/*    @RequestMapping("/getNearbyAttractions")
    public String getNearbyAttractions(@RequestParam String userName) {
    	VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(userName));
    	return JsonStream.serialize(tourGuideService.getNearByAttractions(visitedLocation));
    }*/
    @RequestMapping("/getNearbyAttractions")
    public NearestAttractionsForUser getNearbyAttractionsV2(@RequestParam String userName) {
        NearestAttractionsForUser nearestAttractionsForUserResult;

        User user = getUser(userName);
        VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(userName));
        nearestAttractionsForUserResult = tourGuideService.getNearByAttractionsForUSer(user, visitedLocation);

        return nearestAttractionsForUserResult;

    }
    
    @RequestMapping("/getRewards") 
    public String getRewards(@RequestParam String userName) {
    	return JsonStream.serialize(tourGuideService.getUserRewards(getUser(userName)));
    }
    
    @RequestMapping("/getAllCurrentLocations")
    //public String getAllCurrentLocations() {
    public List<UserCurentLocation> getAllCurrentLocations() {
    	// TODO: Get a list of every user's most recent location as JSON ==> Done
    	//- Note: does not use gpsUtil to query for their current location, 
    	//        but rather gathers the user's current location from their stored location history.
    	//
    	// Return object should be the just a JSON mapping of userId to Locations similar to:
    	//     {
    	//        "019b04a9-067a-4c76-8817-ee75088c3822": {"longitude":-48.188821,"latitude":74.84371} 
    	//        ...
    	//     }
    	
    	return tourGuideService.getAllCurrentLocations();
    }
    
    @RequestMapping("/getTripDeals")
 //   public String getTripDeals(@RequestParam String userName) {
    public List<Provider> getTripDeals(@RequestParam String userName) {
        List<Provider> providers = tourGuideService.getTripDeals(getUser(userName));
    	//return JsonStream.serialize(providers);
        return providers ;
    }

    @RequestMapping("/getUserPreference")
    @ResponseStatus(HttpStatus.OK)
    public UserPreferenceDTO getUserPreference(@RequestParam String userName) throws UserNameNotFoundException {

        if (tourGuideService.getUser(userName) == null ) {
            String message = " this username does not exist : "+ userName;
            logger.error(message);
            throw new UserNameNotFoundException(message);
        }
        return tourGuideService.getUserPreference (userName);

    }

    @PostMapping("/setUserPreference")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public UserPreferenceDTO setUserPreference(@RequestParam String userName, @RequestBody UserPreferenceDTO userPreference) throws UserNameNotFoundException, UserPreferenceEmptyException {
        if (tourGuideService.getUser(userName) == null ) {
            String message = " this username does not exist : "+ userName;
            logger.error(message);
            throw new UserNameNotFoundException(message);
        }
        if (userPreference == null ) {
            String message = " userPreference is empty  ";
            logger.error(message);
            throw new UserPreferenceEmptyException(message);
        }

        return tourGuideService.setUserPreference(userName,userPreference);
    }

    private User getUser(String userName) {
    	return tourGuideService.getUser(userName);
    }
   

}