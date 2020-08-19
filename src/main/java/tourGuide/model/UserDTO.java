package tourGuide.model;

import gpsUtil.location.VisitedLocation;
import org.modelmapper.ModelMapper;
import tourGuide.user.User;

import java.util.Date;
import java.util.UUID;

public class UserDTO {



    private UUID userId;
    private String userName;
    private String phoneNumber;
    private String emailAddress;
    private Date latestLocationTimestamp;
    private VisitedLocation lastVisitedLocation;

    public UserDTO() {
    }

    public UserDTO(UUID userId, String userName, String phoneNumber, String emailAddress, Date latestLocationTimestamp, VisitedLocation lastVisitedLocation) {
        this.userId = userId;
        this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.emailAddress = emailAddress;
        this.latestLocationTimestamp = latestLocationTimestamp;
        this.lastVisitedLocation = lastVisitedLocation;
    }



    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public Date getLatestLocationTimestamp() {
        return latestLocationTimestamp;
    }

    public void setLatestLocationTimestamp(Date latestLocationTimestamp) {
        this.latestLocationTimestamp = latestLocationTimestamp;
    }

    public VisitedLocation getLastVisitedLocation() {
        return lastVisitedLocation;
    }

    public void setLastVisitedLocation(VisitedLocation lastVisitedLocation) {
        this.lastVisitedLocation = lastVisitedLocation;
    }

    @Override
    public String toString() {
        return "UserDTO{" +
                "userId=" + userId +
                ", userName='" + userName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", emailAddress='" + emailAddress + '\'' +
                ", latestLocationTimestamp=" + latestLocationTimestamp +
                ", lastVisitedLocation=" + lastVisitedLocation +
                '}';
    }

    public UserDTO convertToDto(User user) {
        ModelMapper modelMapper = new ModelMapper();
        UserDTO userDto = modelMapper.map(user, UserDTO.class);
        return userDto;
    }


}
