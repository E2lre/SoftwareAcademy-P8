package tourGuide;

import static org.junit.Assert.assertTrue;

import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

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
	//@Ignore
	@Test
	public void highVolumeTrackLocation() {
		Locale.setDefault(Locale.US);
		logger.debug("Start highVolumeTrackLocation");
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		// Users should be incremented up to 100,000, and test finishes within 15 minutes
		InternalTestHelper.setInternalUserNumber(100);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

		List<User> allUsers = new ArrayList<>();
		allUsers = tourGuideService.getAllUsers();
		
	    StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		/*for(User user : allUsers) {
			tourGuideService.trackUserLocation(user);
		}*/
		allUsers.parallelStream().forEach(u -> tourGuideService.trackUserLocation(u));

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
		InternalTestHelper.setInternalUserNumber(100);
//position historique des stopWatch ci dessous
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);


	    Attraction attraction = gpsUtil.getAttractions().get(0);
		List<User> allUsers = new ArrayList<>();
		allUsers = tourGuideService.getAllUsers();


		allUsers.forEach(u -> u.addToVisitedLocations(new VisitedLocation(u.getUserId(), attraction, new Date())));
		//allUsers.parallelStream().forEach(u -> u.addToVisitedLocations(new VisitedLocation(u.getUserId(), attraction, new Date())));


		//Déplacement stopwatch
		/*StopWatch stopWatch = new StopWatch();
		stopWatch.start();*/
/*		try {
			semaphore.acquire();*/

			//allUsers.forEach(u -> rewardsService.calculateRewards(u));
			allUsers.parallelStream().forEach(u -> rewardsService.calculateRewards(u));
/*		} catch (InterruptedException e) {
		e.printStackTrace();
		} finally {
			//verrou.unlock();
			semaphore.release();
		}*/


		for(User user : allUsers) {
			assertTrue(user.getUserRewards().size() > 0);
		}
		stopWatch.stop();
		tourGuideService.tracker.stopTracking();

		System.out.println("highVolumeGetRewards: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds."); 
		assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
		logger.debug("End of highVolumeGetRewards");
	}
	
}
