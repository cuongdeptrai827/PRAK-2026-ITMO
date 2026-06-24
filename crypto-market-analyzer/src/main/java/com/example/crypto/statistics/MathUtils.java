package com.example.crypto.statistics;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MathUtils {

    private MathUtils() {
    }

    public static BigDecimal min(List<BigDecimal> values) {
        return removeNulls(values).stream()
                .min(Comparator.naturalOrder())
                .orElse(null);
    }

    public static BigDecimal max(List<BigDecimal> values) {
        return removeNulls(values).stream()
                .max(Comparator.naturalOrder())
                .orElse(null);
    }

    public static BigDecimal average(List<BigDecimal> values) {
        List<BigDecimal> cleanValues = removeNulls(values);

        if (cleanValues.isEmpty()) {
            return null;
        }

        BigDecimal sum = BigDecimal.ZERO;

        for (BigDecimal value : cleanValues) {
            sum = sum.add(value);
        }

        return sum.divide(BigDecimal.valueOf(cleanValues.size()), 8, RoundingMode.HALF_UP);
    }

    public static BigDecimal median(List<BigDecimal> values) {
        List<BigDecimal> cleanValues = removeNulls(values);

        if (cleanValues.isEmpty()) {
            return null;
        }

        cleanValues.sort(Comparator.naturalOrder());

        int size = cleanValues.size();

        if (size % 2 == 1) {
            return cleanValues.get(size / 2).setScale(8, RoundingMode.HALF_UP);
        }

        BigDecimal left = cleanValues.get(size / 2 - 1);
        BigDecimal right = cleanValues.get(size / 2);

        return left.add(right).divide(BigDecimal.valueOf(2), 8, RoundingMode.HALF_UP);
    }

    public static BigDecimal standardDeviation(List<BigDecimal> values) {
        List<BigDecimal> cleanValues = removeNulls(values);

        if (cleanValues.isEmpty()) {
            return null;
        }

        double average = cleanValues.stream()
                .mapToDouble(BigDecimal::doubleValue)
                .average()
                .orElse(0.0);

        double variance = 0.0;

        for (BigDecimal value : cleanValues) {
            double difference = value.doubleValue() - average;
            variance += difference * difference;
        }

        variance = variance / cleanValues.size();

        return BigDecimal.valueOf(Math.sqrt(variance)).setScale(8, RoundingMode.HALF_UP);
    }

    public static BigDecimal difference(BigDecimal last, BigDecimal first) {
        if (last == null || first == null) {
            return null;
        }

        return last.subtract(first).setScale(8, RoundingMode.HALF_UP);
    }

    public static BigDecimal percentChange(BigDecimal last, BigDecimal first) {
        if (last == null || first == null || first.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        return last.subtract(first)
                .divide(first, 8, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(4, RoundingMode.HALF_UP);
    }

    public static BigDecimal rmsd(List<BigDecimal> values) {
        List<BigDecimal> cleanValues = removeNulls(values);

        if (cleanValues.isEmpty()) {
            return null;
        }

        double sumSquares = 0.0;

        for (BigDecimal value : cleanValues) {
            double current = value.doubleValue();
            sumSquares += current * current;
        }

        double result = Math.sqrt(sumSquares / cleanValues.size());

        return BigDecimal.valueOf(result).setScale(8, RoundingMode.HALF_UP);
    }

    public static List<BigDecimal> removeNulls(List<BigDecimal> values) {
        List<BigDecimal> result = new ArrayList<>();

        for (BigDecimal value : values) {
            if (value != null) {
                result.add(value);
            }
        }

        return result;
    }
}