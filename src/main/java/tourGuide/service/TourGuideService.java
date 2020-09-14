package tourGuide.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import tourGuide.helper.InternalTestHelper;
import tourGuide.helper.Util;
import tourGuide.model.*;
import tourGuide.tracker.Tracker;
import tourGuide.user.User;
import tourGuide.user.UserPreferences;
import tourGuide.user.UserReward;
import tripPricer.Provider;
import tripPricer.TripPricer;

@Service
public class TourGuideService {
	private Logger logger = LoggerFactory.getLogger(TourGuideService.class);
	private final GpsUtil gpsUtil;
	private final RewardsService rewardsService;
	private final TripPricer tripPricer = new TripPricer();
	public final Tracker tracker;
	boolean testMode = true;
	
	public TourGuideService(GpsUtil gpsUtil, RewardsService rewardsService) {
		this.gpsUtil = gpsUtil;
		this.rewardsService = rewardsService;
		
		if(testMode) {
			logger.info("TestMode enabled");
			logger.debug("Initializing users");
			initializeInternalUsers();
			logger.debug("Finished initializing users");
		}
		tracker = new Tracker(this);
		addShutDownHook();
	}
	
	public List<UserReward> getUserRewards(User user) {
		return user.getUserRewards();
	}
	
	public VisitedLocation getUserLocation(User user) {
		logger.debug("getUserLocation Start");
		VisitedLocation visitedLocation = (user.getVisitedLocations().size() > 0) ?
			user.getLastVisitedLocation() :
			trackUserLocation(user);
		logger.debug("getUserLocation End");
		return visitedLocation;
	}

	/**
	 * get curent location for evryboby (EDE august 2020)
	 * @return list of postion for evrybody
	 */
	public List<UserCurentLocation> getAllCurrentLocations(){
		List<UserCurentLocation> userCurentLocations = new ArrayList<>();
		List<User> userList = getAllUsers();
		for (User user : userList) {

			VisitedLocation lastVisitedLocation = user.getLastVisitedLocation();
			Location location = new Location(lastVisitedLocation.location.latitude,lastVisitedLocation.location.longitude);
			UserCurentLocation userCurentLocation = new UserCurentLocation(user.getUserId(),location);
			userCurentLocations.add(userCurentLocation);
		}

		return userCurentLocations;
	}

	public User getUser(String userName) {
		return internalUserMap.get(userName);
	}
	
	public List<User> getAllUsers() {
		return internalUserMap.values().stream().collect(Collectors.toList());
	}
	
	public void addUser(User user) {
		if(!internalUserMap.containsKey(user.getUserName())) {
			internalUserMap.put(user.getUserName(), user);
		}
	}

	//EDE August 2020 : modification to check price with preference
	public List<Provider> getTripDeals(User user) {
		int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();
		List<Provider> providers = tripPricer.getPrice(tripPricerApiKey, user.getUserId(), user.getUserPreferences().getNumberOfAdults(), 
				user.getUserPreferences().getNumberOfChildren(), user.getUserPreferences().getTripDuration(), cumulatativeRewardPoints);

		List<Provider> providersResult = new ArrayList<>();
		for (Provider provider : providers) {
			if (provider.price <= user.getUserPreferences().getHighPricePoint().getNumber().doubleValue()) {
				providersResult.add(provider);
			}
		}

		//user.setTripDeals(providers);
		//return providers;
		user.setTripDeals(providersResult);
		return providersResult;

	}
	
	public VisitedLocation trackUserLocation(User user) {
		VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());
		user.addToVisitedLocations(visitedLocation);
		rewardsService.calculateRewards(user);
		return visitedLocation;
	}
/* retreive by EDE*/
/*	public List<Attraction> getNearByAttractions(VisitedLocation visitedLocation) {
		List<Attraction> nearbyAttractions = new ArrayList<>();
		for(Attraction attraction : gpsUtil.getAttractions()) {
			if(rewardsService.isWithinAttractionProximity(attraction, visitedLocation.location)) {
				nearbyAttractions.add(attraction);
			}
		}

		return nearbyAttractions;
	}*/
//EDE Add for getNearbyAttractions
	//TODO amélioer la perf sur Reward

	/**
	 * Give the 5 nearest attraction for a user (EDE august 2020)
	 * @param user user to check attraction
	 * @param visitedLocation last user's position
	 * @return list of 5 nearest attractions
	 */
	public NearestAttractionsForUser getNearByAttractionsForUSer(User user, VisitedLocation visitedLocation) {
		List<NearestAttraction> nearestAttractions = new ArrayList();
		List<NearestAttraction> nearestAttractionList = new ArrayList();

		Util util = new Util();
		UserDTO userDto = util.convertToDto(user);

		//userDto = util.convertToDto(user);
		logger.debug("start test");



//TODO : Transformer en stream et lambda
					List<Attraction> attractionListLambda = gpsUtil.getAttractions().parallelStream().collect(Collectors.toList());
					logger.debug("fin lambda");
					//users.forEach(u -> tourGuideService.trackUserLocation(u));
		//Forçage à 8 stream au lieu des 4 par défaut liés auc 4 coeur de la machine
//		ForkJoinPool myPool = new ForkJoinPool(8);
//		myPool.submit(() -> {
					attractionListLambda.parallelStream().forEach(attractionLb -> {
						logger.debug("	start loop");
						Location locationAttraction = new Location(attractionLb.latitude, attractionLb.longitude);
						logger.debug("		fin Location");
						NearestAttraction nearestAttraction = new NearestAttraction(attractionLb, rewardsService.getDistance(locationAttraction, userDto.getLastVisitedLocation().location), rewardsService.getRewardPoints(attractionLb, user));
						logger.debug("		fin nearestAttraction");
						//GetREward had bad performance. th reward will be call only for the 5 destination, not for all
						//NearestAttraction nearestAttraction = new NearestAttraction(attraction,rewardsService.getDistance(locationAttraction, userDto.getLastVisitedLocation().location),0);
						nearestAttractions.add(nearestAttraction);
						logger.debug("	End loop");
					});


//				}).get();
		/*
		for(Attraction attraction : attractionListLambda) {
		//for(Attraction attraction : gpsUtil.getAttractions()) {
			logger.debug("	start loop");
			Location locationAttraction = new Location(attraction.latitude,attraction.longitude);
			logger.debug("		fin Location");
			NearestAttraction nearestAttraction = new NearestAttraction(attraction,rewardsService.getDistance(locationAttraction, userDto.getLastVisitedLocation().location),rewardsService.getRewardPoints(attraction,user));
			logger.debug("		fin nearestAttraction");
			//GetREward had bad performance. th reward will be call only for the 5 destination, not for all
			//NearestAttraction nearestAttraction = new NearestAttraction(attraction,rewardsService.getDistance(locationAttraction, userDto.getLastVisitedLocation().location),0);
			nearestAttractions.add(nearestAttraction);
			logger.debug("	End loop");
		} */
		logger.debug("end count");
		nearestAttractionList = util.selectFiveProxyAttraction(nearestAttractions);

		logger.debug("end 5");
		//NearestAttractionsForUser nearestAttractionsForUserResult = new NearestAttractionsForUser(userDto.convertToDto(user),nearestAttractionList);
		NearestAttractionsForUser nearestAttractionsForUserResult = new NearestAttractionsForUser(userDto,nearestAttractionList);
		logger.debug("end test");
		return nearestAttractionsForUserResult;
	}

	/**
	 * get the user preference for a user (EDE august 2020)
	 * @param userName userName of the user
	 * @return curent preference for the user
	 */
	public UserPreferenceDTO getUserPreference(String userName){

		User curentUser = getUser(userName);
		UserPreferences userPreferenceResult = curentUser.getUserPreferences();
		Util util = new Util();
		return util.convertUserPreferenceToDto(userPreferenceResult);
	}
	/**
	 * set the user preference for a user (EDE august 2020)
	 * @param userName userName of the user
	 * @param userPreference new preference for the user
	 * @return new preference
	 */
	public UserPreferenceDTO setUserPreference(String userName, UserPreferenceDTO userPreference){
		UserPreferences userPreferenceResult = null;
		User curentUser = getUser(userName);

		UserPreferences curentUserPreference = curentUser.getUserPreferences();
		Util util = new Util();
		curentUser.setUserPreferences(util.convertDtoToUserPreference(userPreference));
		return util.convertUserPreferenceToDto(curentUser.getUserPreferences());
	}

	private void addShutDownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() { 
		      public void run() {
		        tracker.stopTracking();
		      } 
		    }); 
	}
	
	/**********************************************************************************
	 * 
	 * Methods Below: For Internal Testing
	 * 
	 **********************************************************************************/
	private static final String tripPricerApiKey = "test-server-api-key";
	// Database connection will be used for external users, but for testing purposes internal users are provided and stored in memory
	private final Map<String, User> internalUserMap = new HashMap<>();
	private void initializeInternalUsers() {
		IntStream.range(0, InternalTestHelper.getInternalUserNumber()).forEach(i -> {
			String userName = "internalUser" + i;
			String phone = "000";
			String email = userName + "@tourGuide.com";
			User user = new User(UUID.randomUUID(), userName, phone, email);
			generateUserLocationHistory(user);
			
			internalUserMap.put(userName, user);
		});
		logger.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
	}
	
	private void generateUserLocationHistory(User user) {
		IntStream.range(0, 3).forEach(i-> {
			user.addToVisitedLocations(new VisitedLocation(user.getUserId(), new Location(generateRandomLatitude(), generateRandomLongitude()), getRandomTime()));
		});
	}
	
	private double generateRandomLongitude() {
		double leftLimit = -180;
	    double rightLimit = 180;
	    return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}
	
	private double generateRandomLatitude() {
		double leftLimit = -85.05112878;
	    double rightLimit = 85.05112878;
	    return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}
	
	private Date getRandomTime() {
		LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
	    return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
	}

}
