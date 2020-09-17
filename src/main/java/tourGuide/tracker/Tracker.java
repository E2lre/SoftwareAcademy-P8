package tourGuide.tracker;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tourGuide.service.TourGuideService;
import tourGuide.user.User;

public class Tracker extends Thread {
	private Logger logger = LoggerFactory.getLogger(Tracker.class);
	private static final long trackingPollingInterval = TimeUnit.MINUTES.toSeconds(5);
	private final ExecutorService executorService = Executors.newSingleThreadExecutor();
	private final TourGuideService tourGuideService;
	private boolean stop = false;

	public Tracker(TourGuideService tourGuideService) {
		this.tourGuideService = tourGuideService;
		executorService.submit(this);

/*
		CompletableFuture<String> completableFuture
				= new CompletableFuture<>();
		executorService.submit(completableFuture.supplyAsync(this));
*/
		logger.debug("tracker submit");
		//TODO EDE : ajouter un log avec
		/*Future<String> future = executorService.submit(callableTask);
String result = null;
try {
    result = future.get();
} catch (InterruptedException | ExecutionException e) {
    e.printStackTrace();
}*/
	}
	
	/**
	 * Assures to shut down the Tracker thread
	 */
	public void stopTracking() {
		stop = true;
		executorService.shutdownNow();
	}
	
	@Override
	public void run() {
		StopWatch stopWatch = new StopWatch();
		while(true) {
			if(Thread.currentThread().isInterrupted() || stop) {
				logger.debug("Tracker stopping");
				break;
			}
			
			List<User> users = tourGuideService.getAllUsers();
			logger.debug("Begin Tracker. Tracking " + users.size() + " users.");
			stopWatch.start();
			users.forEach(u -> tourGuideService.trackUserLocation(u));
			stopWatch.stop();
			logger.debug("Tracker Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds."); 
			stopWatch.reset();
			try {
				logger.debug("Tracker sleeping");
				TimeUnit.SECONDS.sleep(trackingPollingInterval);
				logger.debug("Tracker END of sleeping"); //TODO EDE a retirer
			} catch (InterruptedException e) {
				break;
			}
		}
		
	}
	/*@Override
	public void run() {
		StopWatch stopWatch = new StopWatch();
		while(true) {
			if(Thread.currentThread().isInterrupted() || stop) {
				logger.debug("Tracker stopping");
				break;
			}

			List<User> users = tourGuideService.getAllUsers();
			logger.debug("Begin Tracker. Tracking " + users.size() + " users.");
			stopWatch.start();
			logger.debug("start exec");
			ExecutorService executor = Executors.newFixedThreadPool(1000);
			users.forEach(u -> {
//				logger.debug("loop");
				Runnable runnableTask = () -> {
					tourGuideService.trackUserLocation(u);
//					logger.debug("run--------------------------"+ u.getUserName());
			};
				executor.execute(runnableTask);
			});
			logger.debug("shutdown");
			executor.shutdown();

			try {
				//if (!executor.awaitTermination(800, TimeUnit.MILLISECONDS)) {
				if (!executor.awaitTermination(15, TimeUnit.MINUTES)) { //15 minutes est notre objectif
					logger.debug("************* end now ********************");
					executor.shutdownNow();
				}
			} catch (InterruptedException e) {
				logger.debug("************* end now catch *************");
				executor.shutdownNow();
			}
			logger.debug("end");

			stopWatch.stop();
			logger.debug("Tracker Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
			stopWatch.reset();
			try {
				logger.debug("Tracker sleeping");
				TimeUnit.SECONDS.sleep(trackingPollingInterval);
				logger.debug("Tracker END of sleeping"); //TODO EDE a retirer
			} catch (InterruptedException e) {
				break;
			}
		}

	}*/
}
