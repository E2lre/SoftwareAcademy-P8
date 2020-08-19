package tourGuide.helper;

import org.modelmapper.ModelMapper;
import tourGuide.model.NearestAttraction;
import tourGuide.model.UserDTO;
import tourGuide.user.User;

import java.util.*;

public class Util {

    /**
     * Convert user to userDTO
     * @param user user to convert
     * @return user conted to userDTO
     */
    public UserDTO convertToDto(User user) {
        ModelMapper modelMapper = new ModelMapper();
        UserDTO userDto = modelMapper.map(user, UserDTO.class);
        return userDto;
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
            if (i<5) {
                nearestAttractionListResult.add(ent.getValue());
            }
            i++; //TODO A modifier
        }
        return nearestAttractionListResult;
    }
}
