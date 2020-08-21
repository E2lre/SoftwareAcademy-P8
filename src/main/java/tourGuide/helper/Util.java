package tourGuide.helper;

import org.javamoney.moneta.Money;
import org.modelmapper.ModelMapper;
import tourGuide.model.NearestAttraction;
import tourGuide.model.UserDTO;
import tourGuide.model.UserPreferenceDTO;
import tourGuide.user.User;
import tourGuide.user.UserPreferences;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import java.util.*;

public class Util {

    private int NUMBER_PROXY_ATTACTION = 5;
    /**
     * Convert user to userDTO
     * @param user user to convert
     * @return user converted to userDTO
     */
    public UserDTO convertToDto(User user) {
        ModelMapper modelMapper = new ModelMapper();
        UserDTO userDto = modelMapper.map(user, UserDTO.class);
        return userDto;
    }

    /**
     * Convert UserPreferences to UserPreferenceDTO
     * @param userPreference userPreference to convert
     * @return userPreferences converted to userPreferenceDTO
     */
    public UserPreferenceDTO convertUserPreferenceToDto(UserPreferences userPreference) {

        UserPreferenceDTO userPreferenceDTO = new UserPreferenceDTO (userPreference.getAttractionProximity(),
                userPreference.getCurrency().getCurrencyCode(),
                userPreference.getLowerPricePoint().getNumber().doubleValue(),
                userPreference.getHighPricePoint().getNumber().doubleValue(),
                userPreference.getTripDuration(),
                userPreference.getTicketQuantity(),
                userPreference.getNumberOfAdults(),
                userPreference.getNumberOfChildren());
        return userPreferenceDTO;
    }

    /**
     * Convert UserPreferenceDTO to UserPreferences
     * @param userPreferenceDto userPreferenceDto to convert
     * @return UserPreferenceDTO converted to UserPreferences
     */
    public UserPreferences convertDtoToUserPreference(UserPreferenceDTO userPreferenceDto) {
         CurrencyUnit currency = Monetary.getCurrency(userPreferenceDto.getCurrency());
         Money lowerPricePoint = Money.of(userPreferenceDto.getLowerPricePoint(), currency);
         Money highPricePoint = Money.of(userPreferenceDto.getHighPricePoint(), currency);
        UserPreferences userPreference  = new UserPreferences(userPreferenceDto.getAttractionProximity(),
                currency,
                lowerPricePoint,
                highPricePoint,
                userPreferenceDto.getTripDuration(),
                userPreferenceDto.getTicketQuantity(),
                userPreferenceDto.getNumberOfAdults(),
                userPreferenceDto.getNumberOfChildren());
        return userPreference;
    }
    /**
     * Identify the 5 proxy attractions
     * @param nearestAttractions list of attraction
     * @return list of  5 proxy attraction
     */
    public List<NearestAttraction> selectFiveProxyAttraction (List<NearestAttraction> nearestAttractions) {
/*        Map<NearestAttraction,Integer> table = new HashMap<NearestAttraction,Integer>();
        ValueComparator comparator = new ValueComparator();
        TreeMap<NearestAttraction,Integer> mapSort = new TreeMap<NearestAttraction,Integer>(comparator);*/
        List<NearestAttraction> nearestAttractionListResult = new ArrayList<>();

        Map<Double,NearestAttraction> table = new HashMap<Double,NearestAttraction>();
        ValueComparator comparator = new ValueComparator();
        TreeMap<Double,NearestAttraction> mapSort = new TreeMap<Double,NearestAttraction>(comparator);

        for (NearestAttraction nearestAttraction : nearestAttractions) {
            table.put(nearestAttraction.getDistance(),nearestAttraction);
        }
        mapSort.putAll(table);
        Set<Map.Entry<Double,NearestAttraction>> entires = mapSort.entrySet();
        int i =0 ;
        for(Map.Entry<Double,NearestAttraction> ent:entires){
            if (i<NUMBER_PROXY_ATTACTION) {
                nearestAttractionListResult.add(ent.getValue());
            } else {
                break;
            }
            i++;
        }
        return nearestAttractionListResult;
    }
}
