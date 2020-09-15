package tourGuide.service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import rewardCentral.RewardCentral;
import tourGuide.user.User;
import tourGuide.user.UserReward;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


@Service
public class RewardsService  {
//	public class RewardsService extends Thread {
    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

	static Semaphore semaphore = new Semaphore(1);

	//ADD EDE
	private ExecutorService executor
			= Executors.newSingleThreadExecutor();

	private Logger logger = LoggerFactory.getLogger(RewardsService.class);
	// proximity in miles
    private int defaultProximityBuffer = 10;
	private int proximityBuffer = defaultProximityBuffer;
	private int attractionProximityRange = 20000; //TODO EDE a enlever
	private final GpsUtil gpsUtil;
	private final RewardCentral rewardsCentral;
	
	public RewardsService(GpsUtil gpsUtil, RewardCentral rewardCentral) {
		this.gpsUtil = gpsUtil;
		this.rewardsCentral = rewardCentral;
	}
	
	public void setProximityBuffer(int proximityBuffer) {
		this.proximityBuffer = proximityBuffer;
	}
	
	public void setDefaultProximityBuffer() {
		proximityBuffer = defaultProximityBuffer;
	}
	

		public  void calculateRewards(User user) {
//E2LRE August 2020 :Correction to avoid ConcurrentModificationException in TestPerform
/*		 List<VisitedLocation> userLocations = user.getVisitedLocations();
		 List<Attraction> attractions = gpsUtil.getAttractions();
*/
			CopyOnWriteArrayList<VisitedLocation> userLocations = new CopyOnWriteArrayList<>();
			userLocations.addAll(user.getVisitedLocations());

			CopyOnWriteArrayList<Attraction> attractions = new CopyOnWriteArrayList<>();
			attractions.addAll(gpsUtil.getAttractions());

			/****** Mise en place de Executor Services ****************/
			//ExecutorService executor = Executors.newFixedThreadPool(1000);

				//userLocations.parallelStream().forEach(visitedLocation -> rewardsService.calculateRewards(u));
				//for (VisitedLocation visitedLocation : userLocations) {
				//userLocations.parallelStream().forEach(visitedLocation -> {
				userLocations.stream().forEach(visitedLocation -> {
				//for (Attraction attraction : attractions) {
						//attractions.parallelStream().forEach(attraction -> {
						attractions.stream().forEach(attraction -> {
						if (user.getUserRewards().stream().filter(r -> r.attraction.attractionName.equals(attraction.attractionName)).count() == 0) { //TODO voir efficacité du parallel ==>0
						//if (user.getUserRewards().parallelStream().filter(r -> r.attraction.attractionName.equals(attraction.attractionName)).count() == 0) {
							if (nearAttraction(visitedLocation, attraction)) {
							//Ajout de l'async

								user.addUserReward(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
//								Runnable runnableTask = () -> {
//									user.addUserReward(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
//									//logger.debug("run--------------------------"+cpt+ " "+ user.getUserName());
//								};
								//logger.debug("exec ");
//								executor.execute(runnableTask);

							}
						}
					});
				});

//			logger.debug("shutdown");
//			executor.shutdown();
//
//			try {
//				//if (!executor.awaitTermination(800, TimeUnit.MILLISECONDS)) {
//				if (!executor.awaitTermination(1, TimeUnit.MINUTES)) { //15 minutes est notre objectif
//					logger.debug("************* end now REward********************");
//					executor.shutdownNow();
//				}
//			} catch (InterruptedException e) {
//				logger.debug("************* end now catch REward *************");
//				executor.shutdownNow();
//			}
//			logger.debug("end");
			/****** fin de Mise en place de Executor Services ****************/
		//logger.debug("End of calculateRewards");
	}

	public  void calculateRewards_New(User user) {
//E2LRE August 2020 :Correction to avoid ConcurrentModificationException in TestPerform
/*		 List<VisitedLocation> userLocations = user.getVisitedLocations();
		 List<Attraction> attractions = gpsUtil.getAttractions();
*/
		CopyOnWriteArrayList<VisitedLocation> userLocations = new CopyOnWriteArrayList<>();
		userLocations.addAll(user.getVisitedLocations());

		CopyOnWriteArrayList<Attraction> attractions = new CopyOnWriteArrayList<>();
		attractions.addAll(gpsUtil.getAttractions());

		/****** Mise en place de Executor Services ****************/
		ExecutorService executor = Executors.newFixedThreadPool(1000);

		//userLocations.parallelStream().forEach(visitedLocation -> rewardsService.calculateRewards(u));
		//for (VisitedLocation visitedLocation : userLocations) {
		//userLocations.parallelStream().forEach(visitedLocation -> {
		userLocations.stream().forEach(visitedLocation -> {
			//for (Attraction attraction : attractions) {
			//attractions.parallelStream().forEach(attraction -> {
			attractions.stream().forEach(attraction -> {
				if (user.getUserRewards().stream().filter(r -> r.attraction.attractionName.equals(attraction.attractionName)).count() == 0) { //TODO voir efficacité du parallel ==>0
					//if (user.getUserRewards().parallelStream().filter(r -> r.attraction.attractionName.equals(attraction.attractionName)).count() == 0) {
					if (nearAttraction(visitedLocation, attraction)) {
						//Ajout de l'async

//						user.addUserReward(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
								Runnable runnableTask = () -> {
									user.addUserReward(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
									//logger.debug("run--------------------------"+cpt+ " "+ user.getUserName());
								};
						//logger.debug("exec ");
								executor.execute(runnableTask);

					}
				}
			});
		});

			logger.debug("shutdown");
			executor.shutdown();

			try {
				//if (!executor.awaitTermination(800, TimeUnit.MILLISECONDS)) {
				if (!executor.awaitTermination(1, TimeUnit.MINUTES)) { //15 minutes est notre objectif
					logger.debug("************* end now REward********************");
					executor.shutdownNow();
				}
			} catch (InterruptedException e) {
				logger.debug("************* end now catch REward *************");
				executor.shutdownNow();
			}
			logger.debug("end");
		/****** fin de Mise en place de Executor Services ****************/
		//logger.debug("End of calculateRewards");
	}
	public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
		return getDistance(attraction, location) > attractionProximityRange ? false : true;
	}
	
	private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
		return getDistance(attraction, visitedLocation.location) > proximityBuffer ? false : true;
	}

	public int getRewardPoints(Attraction attraction, User user) {
		return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
/*		Future<Integer> points =  executor.submit(() -> {
				 return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
				});
		int result = points.;
		return points;*/
	}

	//add by EDE
	private Future<Integer> calculateRewardPoints(Attraction attraction, User user) {
		return executor.submit(() -> {
			return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
		});
	}
	
	public double getDistance(Location loc1, Location loc2) {
        double lat1 = Math.toRadians(loc1.latitude);
        double lon1 = Math.toRadians(loc1.longitude);
        double lat2 = Math.toRadians(loc2.latitude);
        double lon2 = Math.toRadians(loc2.longitude);

        double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
                               + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

        double nauticalMiles = 60 * Math.toDegrees(angle);
        double statuteMiles = STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
        return statuteMiles;
	}

}
