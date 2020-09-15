package tourGuide.service;

import java.util.UUID;

public interface RewardCentralService {
    public int getAttractionRewardPoints(UUID attractionId, UUID userId);
}
