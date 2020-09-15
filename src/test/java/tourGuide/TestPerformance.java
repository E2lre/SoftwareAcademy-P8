package tourGuide;

import static org.junit.Assert.assertTrue;

import java.util.*;
import java.util.concurrent.*;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.Ignore;
import org.junit.Test;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rewardCentral.RewardCentral;
import tourGuide.helper.InternalTestHelper;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import tourGuide.user.UserReward;

public class TestPerformance {

	/*
	 * A note on performance improvements:
	 *     
	 *     The number of users generated for the high volume tests can be easily adjusted via this method:
	 *     
	 *     		InternalTestHelper.setInternalUserNumber(100000);
	 *     
	 *     
	 *     These tests can be modified to suit new solutions, just as long as the performance metrics
	 *     at the end of the tests remains consistent. 
	 * 
	 *     These are performance metrics that we are trying to hit:
	 *     
	 *     highVolumeTrackLocation: 100,000 users within 15 minutes:
	 *     		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
     *
     *     highVolumeGetRewards: 100,000 users within 20 minutes:
	 *          assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	 */
	private Logger logger = LoggerFactory.getLogger(TestPerformance.class);
	static Semaphore semaphore = new Semaphore(1);
	private int cpt;
	//@Ignore
	@Test
	public void highVolumeTrackLocation() {
		Locale.setDefault(Locale.US);
		logger.debug("Start highVolumeTrackLocation");
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		// Users should be incremented up to 100,000, and test finishes within 15 minutes
		InternalTestHelper.setInternalUserNumber(100000);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

		List<User> allUsers = new ArrayList<>();
		allUsers = tourGuideService.getAllUsers();
		
	    StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		/*for(User user : allUsers) {
			tourGuideService.trackUserLocation(user);
		}*/
		//allUsers.parallelStream().forEach(u -> tourGuideService.trackUserLocation(u));

		/****** Mise en place de Executor Services ****************/
		logger.debug("start exec");
		ExecutorService executor = Executors.newFixedThreadPool(1000);

		cpt =0;

		for(User user : allUsers) {
			//logger.debug("debut boucle" + cpt+ " "+ user.getUserName());
			Runnable runnableTask = () -> {
				tourGuideService.trackUserLocation(user);
				//logger.debug("run--------------------------"+cpt+ " "+ user.getUserName());
			};
			//logger.debug("exec ");
			executor.execute(runnableTask);
			//logger.debug("fin boucle");
			cpt ++;
		}
		logger.debug("shutdown");
		executor.shutdown();

		try {
			//if (!executor.awaitTermination(800, TimeUnit.MILLISECONDS)) {
			if (!executor.awaitTermination(15, TimeUnit.MINUTES)) { //15 minutes est notre objectif
				logger.debug("************* end now ********************");
				executor.shutdownNow();
				assertTrue(false);
			}
		} catch (InterruptedException e) {
			logger.debug("************* end now catch *************");
			executor.shutdownNow();
			assertTrue(false);
		}
		logger.debug("end");
		/****** fin de Mise en place de Executor Services ****************/
		stopWatch.stop();
		tourGuideService.tracker.stopTracking();

		System.out.println("highVolumeTrackLocation: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds."); 
		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
		logger.debug("End of highVolumeTrackLocation");
	}
	
	//@Ignore
	@Test
	public void highVolumeGetRewards() {
		//Locale.setDefault(Locale.US);
		logger.debug("Start highVolumeGetRewards");
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());

		// Users should be incremented up to 100,000, and test finishes within 20 minutes
		InternalTestHelper.setInternalUserNumber(100000);
//position historique des stopWatch ci dessous
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);


	    Attraction attraction = gpsUtil.getAttractions().get(0);
		List<User> allUsers = new ArrayList<>();
		allUsers = tourGuideService.getAllUsers();


//		allUsers.forEach(u -> u.addToVisitedLocations(new VisitedLocation(u.getUserId(), attraction, new Date()))); // mis dans la boucle
		//allUsers.parallelStream().forEach(u -> u.addToVisitedLocations(new VisitedLocation(u.getUserId(), attraction, new Date())));


			//allUsers.forEach(u -> rewardsService.calculateRewards(u));
			//allUsers.parallelStream().forEach(u -> rewardsService.calculateRewards(u));

		/****** Mise en place de Executor Services ****************/
		logger.debug("start exec");
		ExecutorService executor = Executors.newFixedThreadPool(1000);

		for(User user : allUsers) {
		//allUsers.parallelStream().forEach(user -> {
					//logger.debug("debut boucle" + cpt+ " "+ user.getUserName());
					Runnable runnableTask = () -> {
						user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date()));// AJouté du déébut
						rewardsService.calculateRewards(user);
						assertTrue(user.getUserRewards().size() > 0); // AJouté de la fin
						//logger.debug("run--------------------------"+cpt+ " "+ user.getUserName());
					};
					//logger.debug("exec ");
					executor.execute(runnableTask);
					//logger.debug("fin boucle");

		//		});
		}
		logger.debug("shutdown");
		executor.shutdown();

		try {
			if (!executor.awaitTermination(20, TimeUnit.MINUTES)) { //15 minutes est notre objectif
				logger.debug("************* end now ********************");
				executor.shutdownNow();
				assertTrue(false);
			}
		} catch (InterruptedException e) {
			logger.debug("************* end now catch *************");
			executor.shutdownNow();
			assertTrue(false);
		}
		logger.debug("end");
		/****** fin de Mise en place de Executor Services ****************/


//		for(User user : allUsers) {
//			assertTrue(user.getUserRewards().size() > 0);// mis dans la boucle
//		}
		stopWatch.stop();
		tourGuideService.tracker.stopTracking();

		System.out.println("highVolumeGetRewards: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds."); 
		assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
		logger.debug("End of highVolumeGetRewards");
	}

//	@Test
	public void highVolumeGetRewards_OLD() {
		//Locale.setDefault(Locale.US);
		logger.debug("Start highVolumeGetRewards");
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());

		// Users should be incremented up to 100,000, and test finishes within 20 minutes
		InternalTestHelper.setInternalUserNumber(100);
//position historique des stopWatch ci dessous
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);


		Attraction attraction = gpsUtil.getAttractions().get(0);
		List<User> allUsers = new ArrayList<>();
		allUsers = tourGuideService.getAllUsers();


//		allUsers.forEach(u -> u.addToVisitedLocations(new VisitedLocation(u.getUserId(), attraction, new Date()))); // mis dans la boucle
		//allUsers.parallelStream().forEach(u -> u.addToVisitedLocations(new VisitedLocation(u.getUserId(), attraction, new Date())));


		//allUsers.forEach(u -> rewardsService.calculateRewards(u));
		//allUsers.parallelStream().forEach(u -> rewardsService.calculateRewards(u));


		for(User user : allUsers) {

				user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date()));// AJouté du déébut
				rewardsService.calculateRewards_New(user);
				assertTrue(user.getUserRewards().size() > 0); // AJouté de la fin


		}



//		for(User user : allUsers) {
//			assertTrue(user.getUserRewards().size() > 0);// mis dans la boucle
//		}
		stopWatch.stop();
		tourGuideService.tracker.stopTracking();

		System.out.println("highVolumeGetRewards: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
		assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
		logger.debug("End of highVolumeGetRewards");
	}
}
