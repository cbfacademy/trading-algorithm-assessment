package codingblackfemales.gettingstarted;

import java.util.List;

public class TrendAnalyser {

    public enum TrendType {
        INCREASING,
        DECREASING,
        INCONSISTENT,
        STABLE,
        VOLATILE,
        SHARP_SPIKE,
        SHARP_DROP,
        INSUFFICIENT_DATA_FOR_TREND_DETECTION
    }

    public static TrendType detectTrend(List<? extends Number> listOfNums) {
        if (listOfNums == null || listOfNums.size() < 2) {
            return TrendType.INSUFFICIENT_DATA_FOR_TREND_DETECTION;
        }

        List<Double> listOfNumsAsDoubles = listOfNums.stream()
                .map(Number::doubleValue)
                .toList();

        int midPoint = listOfNumsAsDoubles.size() / 2;
        List<Double> firstHalf = listOfNumsAsDoubles.subList(0, midPoint);
        List<Double> secondHalf = listOfNumsAsDoubles.subList(midPoint, listOfNumsAsDoubles.size());

        double avgFirstHalf = firstHalf.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double avgSecondHalf = secondHalf.stream().mapToDouble(Double::doubleValue).average().orElse(0);

        double percentageChange = (avgFirstHalf != 0) ? (avgSecondHalf - avgFirstHalf) / Math.abs(avgFirstHalf) : Double.POSITIVE_INFINITY;

        double mean = listOfNumsAsDoubles.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double variance = listOfNumsAsDoubles.stream().mapToDouble(v -> Math.pow(v - mean, 2)).average().orElse(0);
        double stdDev = Math.sqrt(variance);

        // Count increases and decreases
        int increases = 0, decreases = 0;
        for (int i = 1; i < listOfNumsAsDoubles.size(); i++) {
            if (listOfNumsAsDoubles.get(i) > listOfNumsAsDoubles.get(i - 1)) {
                increases++;
            } else if (listOfNumsAsDoubles.get(i) < listOfNumsAsDoubles.get(i - 1)) {
                decreases++;
            }
        }

        double totalChanges = increases + decreases;
        double changePercentage = totalChanges > 0 ? (Math.min(increases, decreases) / totalChanges) : 0;

        // Thresholds for detecting trends
        double significantChangeThreshold = 0.05; 
        double sharpChangeFactorThreshold = 2.0;
        double stabilityThreshold = 0.1; // 10% threshold for stability

        // Check for sharp changes first
        if (avgSecondHalf >= sharpChangeFactorThreshold * avgFirstHalf) {
            return TrendType.SHARP_SPIKE;
        } else if (avgFirstHalf >= sharpChangeFactorThreshold * avgSecondHalf) {
            return TrendType.SHARP_DROP;
        }

        // Check for regular increase or decrease
        if (percentageChange >= significantChangeThreshold) {
            return TrendType.INCREASING;
        } else if (percentageChange <= -significantChangeThreshold) {
            return TrendType.DECREASING;
        }

        // Check for volatility or stability
        if (stdDev > significantChangeThreshold) {
            return TrendType.VOLATILE; // High fluctuation indicates volatility
        } else if (changePercentage < stabilityThreshold) {
            return TrendType.STABLE; // Low percentage of changes indicates stability
        }

        return TrendType.INCONSISTENT; // If no clear trend can be determined
    }

}
