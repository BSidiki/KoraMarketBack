package com.koramarket.common.utils;

public class StringUtils {

    // Vérifie si une string est vide ou null
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    // Vérifie si une string n’est PAS vide
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    // Met la première lettre en majuscule
    public static String capitalize(String str) {
        if (isEmpty(str)) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    // Tronque une chaîne à une longueur max, ajoute "..." si besoin
    public static String truncate(String str, int maxLength) {
        if (isEmpty(str) || str.length() <= maxLength) return str;
        return str.substring(0, maxLength) + "...";
    }

    // Nettoie les espaces
    public static String clean(String str) {
        return isEmpty(str) ? "" : str.trim().replaceAll("\\s+", " ");
    }
}
