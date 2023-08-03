package blossom.project.ec.client.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class CollectionUtil {

    public static <T> T findFirst(List<T> list, Predicate<T> predicate){
        for(T p : list){
            if(predicate.test(p)){
                return p;
            }
        }
        return null;
    }

    public static <T> List<T> collect(List<T> list, Predicate<T> predicate){
        List<T> selected = new ArrayList<>();
        for(T p : list){
            if(predicate.test(p)){
                selected.add(p);
            }
        }
        return selected;
    }

    public static <T> Set<T> collectDistinct(List<T> list, Predicate<T> predicate){
        Set<T> selected = new HashSet<>();
        for(T p : list){
            if(predicate.test(p)){
                selected.add(p);
            }
        }
        return selected;
    }
}
