package com.koramarket.common.utils;

public class NumberUtils {

    // Vérifie si un string est un nombre entier valide
    public static boolean isInteger(String str) {
        if (str == null) return false;
        try {
            Integer.parseInt(str.trim());
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    // Vérifie si un string est un nombre décimal valide
    public static boolean isDouble(String str) {
        if (str == null) return false;
        try {
            Double.parseDouble(str.trim());
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    // Limite une valeur dans un intervalle donné
    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    // Convertit un string en int (défaut si erreur)
    public static int toInt(String str, int defaultValue) {
        try {
            return Integer.parseInt(str.trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    // Idem pour double
    public static double toDouble(String str, double defaultValue) {
        try {
            return Double.parseDouble(str.trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
