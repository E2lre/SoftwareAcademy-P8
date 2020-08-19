package tourGuide.helper;

import java.util.Comparator;

/**
 * Use sort the list of distance of attraction
 *
 */

public class ValueComparator implements Comparator<Double> {


    @Override
    public int compare(Double a, Double b) {
        if (a >= b) {
            return 1;
        }
        else {
            return -1;
        }
    }
}
