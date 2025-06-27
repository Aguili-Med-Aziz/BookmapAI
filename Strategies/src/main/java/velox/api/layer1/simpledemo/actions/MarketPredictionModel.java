package velox.api.layer1.simpledemo.actions;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import velox.api.layer1.common.Log;

public class MarketPredictionModel {
    private final String sessionId;
    private final FileWriter predictionWriter;
    private final List<Double> priceHistory;
    private final List<Double> volumeHistory;
    private final List<Double> vwapHistory;
    private final List<Double> imbalanceHistory;
    private final AtomicInteger predictionCounter;
    private static final int HISTORY_WINDOW = 100;
    private static final double PREDICTION_THRESHOLD = 0.7;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    public MarketPredictionModel(String sessionId) throws IOException {
        this.sessionId = sessionId;
        this.priceHistory = new ArrayList<>();
        this.volumeHistory = new ArrayList<>();
        this.vwapHistory = new ArrayList<>();
        this.imbalanceHistory = new ArrayList<>();
        this.predictionCounter = new AtomicInteger(0);

        // Create predictions directory and file
        File predictionsDir = new File("AdvVwapAnalyzer_Output/Predictions");
        if (!predictionsDir.exists()) {
            predictionsDir.mkdirs();
        }
        File predictionFile = new File(predictionsDir, sessionId + "_predictions.txt");
        this.predictionWriter = new FileWriter(predictionFile, true);
        writeHeader();
    }

    private void writeHeader() throws IOException {
        predictionWriter.write("Timestamp,PredictionID,CurrentPrice,VWAP,Imbalance,Volume,PredictedDirection,Confidence,Action,Reason\n");
        predictionWriter.flush();
    }

    public void update(double price, double vwap, double imbalance, double volume) {
        // Update history
        priceHistory.add(price);
        volumeHistory.add(volume);
        vwapHistory.add(vwap);
        imbalanceHistory.add(imbalance);

        // Maintain window size
        if (priceHistory.size() > HISTORY_WINDOW) {
            priceHistory.remove(0);
            volumeHistory.remove(0);
            vwapHistory.remove(0);
            imbalanceHistory.remove(0);
        }

        // Make prediction if we have enough data
        if (priceHistory.size() >= HISTORY_WINDOW) {
            makePrediction(price, vwap, imbalance, volume);
        }
    }

    private void makePrediction(double currentPrice, double currentVwap, double currentImbalance, double currentVolume) {
        try {
            // Calculate features
            double priceMomentum = calculateMomentum(priceHistory);
            double volumeMomentum = calculateMomentum(volumeHistory);
            double vwapDistance = (currentPrice - currentVwap) / currentVwap;
            double imbalanceStrength = Math.abs(currentImbalance);

            // Simple prediction model
            double bullishScore = calculateBullishScore(priceMomentum, volumeMomentum, vwapDistance, currentImbalance);
            double bearishScore = calculateBearishScore(priceMomentum, volumeMomentum, vwapDistance, currentImbalance);

            String predictedDirection;
            String action;
            double confidence;
            String reason;

            if (bullishScore > bearishScore && bullishScore > PREDICTION_THRESHOLD) {
                predictedDirection = "UP";
                action = "BUY";
                confidence = bullishScore;
                reason = buildReasonString("bullish", priceMomentum, volumeMomentum, vwapDistance, currentImbalance);
            } else if (bearishScore > bullishScore && bearishScore > PREDICTION_THRESHOLD) {
                predictedDirection = "DOWN";
                action = "SELL";
                confidence = bearishScore;
                reason = buildReasonString("bearish", priceMomentum, volumeMomentum, vwapDistance, currentImbalance);
            } else {
                predictedDirection = "NEUTRAL";
                action = "HOLD";
                confidence = Math.max(bullishScore, bearishScore);
                reason = "Insufficient directional strength";
            }

            // Write prediction to file
            String predictionLine = String.format("%s,%d,%.4f,%.4f,%.3f,%.1f,%s,%.2f,%s,%s\n",
                    formatter.format(Instant.now()),
                    predictionCounter.incrementAndGet(),
                    currentPrice,
                    currentVwap,
                    currentImbalance,
                    currentVolume,
                    predictedDirection,
                    confidence,
                    action,
                    reason);

            synchronized (predictionWriter) {
                predictionWriter.write(predictionLine);
                predictionWriter.flush();
            }

            Log.info(String.format("Market Prediction: %s (Confidence: %.2f) - Action: %s", 
                    predictedDirection, confidence, action));

        } catch (IOException e) {
            Log.error("Error writing prediction: " + e.getMessage(), e);
        }
    }

    private double calculateMomentum(List<Double> history) {
        if (history.size() < 2) return 0;
        double recent = history.get(history.size() - 1);
        double older = history.get(history.size() - 2);
        return (recent - older) / older;
    }

    private double calculateBullishScore(double priceMomentum, double volumeMomentum, 
                                       double vwapDistance, double imbalance) {
        double score = 0;
        
        // Price momentum is positive
        if (priceMomentum > 0) score += 0.3;
        
        // Volume momentum is positive
        if (volumeMomentum > 0) score += 0.2;
        
        // Price is above VWAP
        if (vwapDistance > 0) score += 0.2;
        
        // Positive imbalance
        if (imbalance > 0) score += 0.3;
        
        return score;
    }

    private double calculateBearishScore(double priceMomentum, double volumeMomentum, 
                                       double vwapDistance, double imbalance) {
        double score = 0;
        
        // Price momentum is negative
        if (priceMomentum < 0) score += 0.3;
        
        // Volume momentum is negative
        if (volumeMomentum < 0) score += 0.2;
        
        // Price is below VWAP
        if (vwapDistance < 0) score += 0.2;
        
        // Negative imbalance
        if (imbalance < 0) score += 0.3;
        
        return score;
    }

    private String buildReasonString(String direction, double priceMomentum, double volumeMomentum, 
                                   double vwapDistance, double imbalance) {
        List<String> reasons = new ArrayList<>();
        
        if (Math.abs(priceMomentum) > 0.001) {
            reasons.add(String.format("Price momentum: %.2f%%", priceMomentum * 100));
        }
        if (Math.abs(volumeMomentum) > 0.001) {
            reasons.add(String.format("Volume momentum: %.2f%%", volumeMomentum * 100));
        }
        if (Math.abs(vwapDistance) > 0.001) {
            reasons.add(String.format("VWAP distance: %.2f%%", vwapDistance * 100));
        }
        if (Math.abs(imbalance) > 0.1) {
            reasons.add(String.format("Imbalance: %.2f", imbalance));
        }
        
        return String.join(" | ", reasons);
    }

    public void close() {
        try {
            if (predictionWriter != null) {
                predictionWriter.close();
            }
        } catch (IOException e) {
            Log.error("Error closing prediction writer: " + e.getMessage(), e);
        }
    }
} 