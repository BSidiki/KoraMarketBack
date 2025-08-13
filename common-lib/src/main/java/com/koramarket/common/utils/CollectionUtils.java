package com.koramarket.common.utils;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CollectionUtils {

    // Vérifie si une collection est vide ou null
    public static boolean isEmpty(Collection<?> coll) {
        return coll == null || coll.isEmpty();
    }

    // Vérifie si une collection n’est PAS vide
    public static boolean isNotEmpty(Collection<?> coll) {
        return !isEmpty(coll);
    }

    // Retire les éléments nuls d’une liste (immuable)
    public static <T> List<T> withoutNulls(List<T> list) {
        return isEmpty(list)
                ? List.of()
                : list.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    // Compte le nombre d’occurrences d’un élément dans une liste
    public static <T> long countOccurrences(List<T> list, T element) {
        if (isEmpty(list)) return 0;
        return list.stream().filter(e -> Objects.equals(e, element)).count();
    }
}
