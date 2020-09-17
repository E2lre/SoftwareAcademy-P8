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
import java.util.function.Supplier;


@Service
public class RewardsService  {
//	public class RewardsService extends Thread {
    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;
	public CompletableFuture<Void> completableFuture;
	ExecutorService executorService = Executors.newFixedThreadPool(1000);

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

	public  void calculateRewards_new(User user) {
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
						logger.debug("calculateRewards add start");
						int rewardPoints = getRewardPoints_Async(attraction, user);
						logger.debug("calculateRewards after getRewardPoints");
						user.addUserReward(new UserReward(visitedLocation, attraction, rewardPoints));
						//user.addUserReward(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
						//user.addUserReward_Async(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
						logger.debug("calculateRewards add End");
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
		logger.debug("END calculateRewards");
	}
		public  void calculateRewards_exec(User user) {
//E2LRE August 2020 :Correction to avoid ConcurrentModificationException in TestPerform
/*		 List<VisitedLocation> userLocations = user.getVisitedLocations();
		 List<Attraction> attractions = gpsUtil.getAttractions();
*/
			CopyOnWriteArrayList<VisitedLocation> userLocations = new CopyOnWriteArrayList<>();
			userLocations.addAll(user.getVisitedLocations());

			CopyOnWriteArrayList<Attraction> attractions = new CopyOnWriteArrayList<>();
			attractions.addAll(gpsUtil.getAttractions());
			logger.debug("exec start");
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
			logger.debug("exec start");
	}
	//public  Future<String> calculateRewards_Async(User user) {
	public void calculateRewards_Async(User user) {
		logger.debug("start Ascyn");
		CompletableFuture<Void> completableFuture = new CompletableFuture<>();
		completableFuture.runAsync(() -> {
			logger.debug("call exec");
			calculateRewards_exec(user);
		});

		try {
			logger.debug("get start");
			completableFuture.get();
			logger.debug("get end");
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

	/*
		Executors.newCachedThreadPool().submit(() -> {
			logger.debug("start calculateRewards");
			calculateRewards( user) ;
			logger.debug("End calculateRewards");
			return null;
		});
*/
	}
	public void calculateRewards_toto(User user){
		calculateRewards_Async(user);
	}


	public  void calculateRewards_future(User user) {
//E2LRE August 2020 :Correction to avoid ConcurrentModificationException in TestPerform
/*		 List<VisitedLocation> userLocations = user.getVisitedLocations();
		 List<Attraction> attractions = gpsUtil.getAttractions();
*/
		CopyOnWriteArrayList<VisitedLocation> userLocations = new CopyOnWriteArrayList<>();
		userLocations.addAll(user.getVisitedLocations());

		CopyOnWriteArrayList<Attraction> attractions = new CopyOnWriteArrayList<>();
		attractions.addAll(gpsUtil.getAttractions());

		/****** Mise en place de Executor Services ****************/


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
						completableFuture = CompletableFuture.runAsync(() -> {
							logger.debug("run-------------------------- deb");
							user.addUserReward(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
							logger.debug("run-------------------------- fin");
						});
											}
				}
			});
		});

		try {
			logger.debug("getRewardPoints get start");
			completableFuture.get();
			logger.debug("getRewardPoints get end");
		} catch (InterruptedException e) {
			logger.debug("************* end now catch REward *************" + e.getMessage());
			//ExecutionException, InterruptedException
		} catch (ExecutionException e) {
			logger.debug("************* end now catch REward *************" + e.getMessage());
			e.printStackTrace();
		}
		logger.debug("end");
		/****** fin de Mise en place de Executor Services ****************/
		//logger.debug("End of calculateRewards");
	}
	public  void calculateRewards(User user) {
		calculateRewards_standard(user);
		//calculateRewards_async(user);
		/*logger.debug("calculateRewards start");
		Runnable runnableTask = () -> {
			logger.debug("run-------------------------- Start");
			calculateRewards_standard(user);
			logger.debug("run-------------------------- End");
		};
		logger.debug("execute");
		executor.execute(runnableTask);
		logger.debug("shutdown");
		executor.shutdown();
		logger.debug("calculateRewards End");*/

	}

	public  void calculateRewards_standard(User user) {
//E2LRE August 2020 :Correction to avoid ConcurrentModificationException in TestPerform
/*		 List<VisitedLocation> userLocations = user.getVisitedLocations();
		 List<Attraction> attractions = gpsUtil.getAttractions();
*/
		logger.debug("calculateRewards_standard start");
		CopyOnWriteArrayList<VisitedLocation> userLocations = new CopyOnWriteArrayList<>();
		userLocations.addAll(user.getVisitedLocations());

		CopyOnWriteArrayList<Attraction> attractions = new CopyOnWriteArrayList<>();
		attractions.addAll(gpsUtil.getAttractions());

		/****** Mise en place de Executor Services ****************/
		ExecutorService executor = Executors.newFixedThreadPool(1000);


		userLocations.stream().forEach(visitedLocation -> {
				attractions.stream().forEach(attraction -> {
				if (user.getUserRewards().stream().filter(r -> r.attraction.attractionName.equals(attraction.attractionName)).count() == 0) { //TODO voir efficacité du parallel ==>0
					if (nearAttraction(visitedLocation, attraction)) {
							user.addUserReward(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
					}
				}
			});
		});
		//logger.debug("End of calculateRewards");
		logger.debug("calculateRewards_standard End");
	}
	public  void calculateRewards_async(User user) {
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
									logger.debug("run- calculateRewards_async------------------------- start"+ user.getUserName());
									user.addUserReward(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
									logger.debug("run- calculateRewards_async------------------------- end"+ user.getUserName());
								};
						//logger.debug("exec ");
								executor.execute(runnableTask);

					}
				}
			});
		});

			logger.debug("calculateRewards_async shutdown");
			executor.shutdown();

			try {
				//if (!executor.awaitTermination(800, TimeUnit.MILLISECONDS)) {
				if (!executor.awaitTermination(1, TimeUnit.MINUTES)) { //15 minutes est notre objectif
					logger.debug("************* calculateRewards_async end now REward********************");
					executor.shutdownNow();
				}
			} catch (InterruptedException e) {
				logger.debug("************* calculateRewards_async end now catch REward *************");
				executor.shutdownNow();
			}
			logger.debug(" calculateRewards_async end");
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
	//	return getRewardPoints_Async(attraction, user);

	}
	public int getRewardPoints_exec(Attraction attraction, User user) {
		return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
	}

	public int getRewardPoints_Async(Attraction attraction, User user) {
		logger.debug("getRewardPoints_Async start Ascyn");
		String result ="";
		int res = 0;
/*		CompletableFuture<Void> completableFuture = new CompletableFuture<>();
		completableFuture.supplyAsync(() -> {
			logger.debug("call exec");
			getRewardPoints_exec( attraction, user);
		});*/
		//CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(new Supplier<String>() {
/*
		 completableFuture = CompletableFuture.supplyAsync(new Supplier<String>() {
			@Override
			public String get() {
				String result ="0";
				int res = 0;
				res = getRewardPoints_exec( attraction, user);
				result.valueOf(res);
				return result;
			}
		});

		try {
			logger.debug("getRewardPoints_Async get start");
			result = completableFuture.get();
			logger.debug("getRewardPoints_Async get end");
		} catch (InterruptedException e) {
			logger.debug("************* end now catch REward *************" + e.getMessage());
			//ExecutionException, InterruptedException
		} catch (ExecutionException e) {
			logger.debug("************* end now catch REward *************" + e.getMessage());
			e.printStackTrace();
		}
		res = Integer.parseInt(result);
		logger.debug("getRewardPoints_Async get Finish");
*/

		return res;
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
