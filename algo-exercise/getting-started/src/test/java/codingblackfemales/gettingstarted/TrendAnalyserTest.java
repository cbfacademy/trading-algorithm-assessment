package codingblackfemales.gettingstarted;

import codingblackfemales.gettingstarted.TrendAnalyser.TrendType;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class TrendAnalyserTest {

    @Test
    public void testInsufficientDataForTrendDetection() {
        // Test when there is insufficient data to detect a trend
        assertEquals("Should detect insufficient data with an empty list", 
                    TrendType.INSUFFICIENT_DATA_FOR_TREND_DETECTION, 
                    TrendAnalyser.detectTrend(List.of()));
        
        assertEquals("Should detect insufficient data with only one element", 
                    TrendType.INSUFFICIENT_DATA_FOR_TREND_DETECTION, 
                    TrendAnalyser.detectTrend(List.of(100L)));
    }

    @Test
    public void testIncreasingTrend() {
        // Test for an increasing trend
        List<Long> increasingData = List.of(100L, 105L, 110L, 115L, 120L);
        assertEquals("Should detect an increasing trend", 
                    TrendType.INCREASING, 
                    TrendAnalyser.detectTrend(increasingData));
    }

    @Test
    public void testDecreasingTrend() {
        // Test for a decreasing trend
        List<Long> decreasingData = List.of(120L, 115L, 110L, 105L, 100L);
        assertEquals("Should detect a decreasing trend", 
                    TrendType.DECREASING, 
                    TrendAnalyser.detectTrend(decreasingData));
    }

    @Test
    public void testInconsistentTrend() {
        // Test for an inconsistent trend
        List<Long> inconsistentData = List.of(100L, 102L, 98L, 101L, 99L);
        assertEquals("Should detect an inconsistent trend", 
                    TrendType.INCONSISTENT, 
                    TrendAnalyser.detectTrend(inconsistentData));
    }

    @Test
    public void testStableTrend() {
        // Test for a stable trend
        List<Long> stableData = List.of(100L, 100L, 101L, 100L, 100L, 101L);
        assertEquals("Should detect a stable trend", 
                    TrendType.STABLE, 
                    TrendAnalyser.detectTrend(stableData));
    }

    @Test
    public void testVolatileTrend() {
        // Test for a volatile trend
        List<Long> volatileData = List.of(100L, 120L, 80L, 110L, 90L);
        assertEquals("Should detect a volatile trend", 
                    TrendType.VOLATILE, 
                    TrendAnalyser.detectTrend(volatileData));
    }

    @Test
    public void testSharpSpike() {
        // Test for a sharp spike
        List<Long> sharpSpikeData = List.of(100L, 150L, 300L, 350L);
        assertEquals("Should detect a sharp spike", 
                    TrendType.SHARP_SPIKE, 
                    TrendAnalyser.detectTrend(sharpSpikeData));
    }

    @Test
    public void testSharpDrop() {
        // Test for a sharp drop
        List<Long> sharpDropData = List.of(350L, 300L, 150L, 100L);
        assertEquals("Should detect a sharp drop", 
                    TrendType.SHARP_DROP, 
                    TrendAnalyser.detectTrend(sharpDropData));
    }

    @Test
    public void testMixedDataWithNoClearTrend() {
        // Test for no clear trend
        List<Long> mixedData = List.of(105L, 110L, 95L, 115L, 105L, 98L);
        assertEquals("Should detect volatile trend with no clear direction", 
                    TrendType.VOLATILE, 
                    TrendAnalyser.detectTrend(mixedData));
    }

    @Test
    public void testFlatDataForStableDetection() {
        // Test for data that is almost completely flat
        List<Long> flatData = List.of(100L, 100L, 100L, 100L, 100L);
        assertEquals("Should detect a stable trend for flat data", 
                    TrendType.STABLE, 
                    TrendAnalyser.detectTrend(flatData));
    }
}
