package rnd;

import java.util.Collection;
import java.util.function.Predicate;

public class Utils {

    public static <T> boolean exists(Collection<T> collection, Predicate<T> predicate) {
        for(T element : collection) {
            if(predicate.test(element)) {
                return true;
            }
        }

        return false;
    }
}
