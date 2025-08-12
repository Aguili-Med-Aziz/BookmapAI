# 📊 **Active Sessions System - Real-Time Session-Based Analysis**

## 🚀 **Overview**

The Active Sessions system provides **dedicated, individual panels** for each active Bookmap chart window. Instead of showing aggregated data, each trading session (instrument) gets its own space with real-time patterns, AI predictions, and market analytics.

---

## ✨ **Key Features**

### **🎯 Individual Session Panels**
- **Dedicated Space:** Each Bookmap chart gets its own panel
- **Real-Time Updates:** Live data updates every 2 seconds
- **Session Isolation:** Patterns and predictions are specific to each instrument
- **Dynamic Creation:** Sessions automatically appear when you open charts in Bookmap

### **🔍 Pattern Detection per Session**
- **Real-Time Patterns:** Live detection of Iceberg, Absorption, Market Imbalances
- **Confidence Levels:** Each pattern shows confidence percentage (70-95%+)
- **Pattern History:** Last 20 patterns per session stored
- **Color-Coded Display:** Visual indicators based on confidence levels

### **🤖 AI Predictions per Session**
- **Direction Predictions:** UP/DOWN/SIDEWAYS with confidence scores
- **Target Levels:** Specific price targets for each prediction
- **Timeframe Analysis:** Short-term (5-15 min) to medium-term predictions
- **Highest Confidence First:** Top 3 predictions displayed, sorted by confidence

### **📈 Session Analytics**
- **Order Flow Analysis:** Real-time buy/sell pressure analysis
- **Market Pressure:** Bullish/Bearish/Neutral pressure detection
- **Volatility Monitoring:** Normal/High/Extreme volatility levels
- **Session Score:** Overall session performance rating (0-100%)

---

## 📋 **Dashboard Interface**

### **Session Panel Structure**

Each active session displays:

```
┌─────────────────────────────────────────────────────────────┐
│ 📈 SYMBOL NAME    [MARKET_TYPE]              🟢 LIVE         │
│ Price: $XXXX.XX (+X.XX%)  |  Volume: XXXX  |  Last: HH:MM:SS│
├─────────────────────────────────────────────────────────────┤
│ 🔍 Detected Patterns    🔮 AI Predictions    📊 Analytics    │
│                                                             │
│ • Pattern Type (XX%)     📈 UP (XX%)         Order Flow: XX  │
│ • Pattern Type (XX%)     📉 DOWN (XX%)       Pressure: XX   │
│ • Pattern Type (XX%)     📊 SIDE (XX%)       Volatility: XX │
│                                              Score: XX%     │
│ Active: X | Conf: XX%    High Conf: X                       │
└─────────────────────────────────────────────────────────────┘
```

### **Visual Indicators**

- **🟢 Green:** High confidence (90%+) / Bullish signals
- **🟡 Yellow:** Good confidence (80-89%) / Neutral signals  
- **🟠 Orange:** Fair confidence (70-79%) / Warning signals
- **🔴 Red:** Low confidence (<70%) / Bearish signals

---

## 🔧 **API Endpoints**

### **Active Sessions API**
```http
GET /api/active-sessions
```

**Response:**
```json
{
  "status": "ACTIVE",
  "sessions": [
    {
      "session_id": "session_001",
      "symbol": "ESU5",
      "market_type": "FUTURES",
      "current_price": "4487.50",
      "price_change": "-0.12",
      "volume": "890",
      "volume_level": "Medium",
      "last_update": "14:23:45",
      "patterns": [
        {
          "type": "ICEBERG_DETECTED",
          "confidence": 87.5,
          "description": "Large hidden order detected",
          "timestamp": 1672934625000
        }
      ],
      "predictions": [
        {
          "direction": "UP",
          "confidence": 92.3,
          "target": "4495.00",
          "timeframe": "5-10 minutes",
          "timestamp": 1672934625000
        }
      ],
      "order_flow": "Bullish",
      "market_pressure": "Strong Buy",
      "volatility": "Normal",
      "session_score": 78.5
    }
  ],
  "count": 1,
  "timestamp": 1672934625000
}
```

### **Session Live Data API**
```http
GET /api/session/{sessionId}/live-data
```

**Response:**
```json
{
  "status": "ACTIVE",
  "session_id": "session_001",
  "last_update": "14:23:45",
  "patterns": [
    {
      "type": "ABSORPTION_PATTERN",
      "confidence": 84.2,
      "description": "Volume absorption at key level",
      "timestamp": 1672934625000
    }
  ],
  "predictions": [
    {
      "direction": "DOWN",
      "confidence": 78.9,
      "target": "4480.00",
      "timeframe": "3-8 minutes",
      "timestamp": 1672934625000
    }
  ],
  "analytics": {
    "order_flow": "Bearish",
    "market_pressure": "Moderate Sell",
    "volatility": "High",
    "session_score": 72.1
  }
}
```

---

## 🎮 **How to Use**

### **Step 1: Open Bookmap Charts**
```
1. Launch Bookmap
2. Open charts for instruments you want to analyze:
   - NQU5 (Nasdaq Futures)
   - ESU5 (S&P 500 Futures)  
   - EURUSD (Forex)
   - BTCUSD (Crypto)
   - etc.
3. Enable the BookmapAI addon on each chart
```

### **Step 2: View Active Sessions**
```
1. Navigate to "Open Markets & Predictions" section
2. Individual session panels appear automatically
3. Each chart gets its own dedicated space
4. Real-time updates start immediately
```

### **Step 3: Monitor Session Data**
```
🔍 Patterns: Watch for high-confidence pattern detections
🤖 Predictions: Focus on 90%+ confidence predictions
📊 Analytics: Monitor order flow and market pressure changes
⏱️ Updates: Data refreshes every 2 seconds automatically
```

---

## 📊 **Data Flow Architecture**

```
Bookmap Charts → BookmapAI Addon → RealTimeMarketDataStore → Session APIs → Dashboard UI
     ↓                ↓                      ↓                    ↓            ↓
Open ESU5.CME → Extract Trade Data → Store Session Data → API Response → Session Panel
```

### **Session Data Pipeline:**

1. **Chart Detection:** System detects new Bookmap chart opened
2. **Session Creation:** Creates `TradingSession` object with unique ID  
3. **Pattern Analysis:** Real-time pattern detection for that session
4. **AI Predictions:** Generate predictions specific to that instrument
5. **Analytics Calculation:** Calculate session-specific metrics
6. **UI Updates:** Update individual session panel every 2 seconds

---

## 🔧 **Backend Implementation**

### **Core Data Structures**

```java
// Trading session representation
public class TradingSession {
    private final String sessionId;
    private final String symbol;
    private final String marketType;
    private final double currentPrice;
    private final String priceChange;
    private final String volume;
    private final String volumeLevel;
    private final long lastUpdate;
}

// Pattern detection for sessions
public class PatternDetection {
    private final String type;           // ICEBERG, ABSORPTION, IMBALANCE
    private final double confidence;     // 70-95%
    private final String description;    // Human-readable description
    private final long timestamp;        // When detected
}

// AI predictions for sessions  
public class AIPrediction {
    private final String direction;      // UP, DOWN, SIDEWAYS
    private final double confidence;     // 70-95%
    private final String target;         // Price target
    private final String timeframe;      // "5-10 minutes"
    private final long timestamp;        // When generated
}

// Session analytics
public class SessionAnalytics {
    private final String orderFlow;      // Bullish/Bearish/Neutral
    private final String marketPressure; // Strong Buy/Moderate Sell/etc
    private final String volatility;     // Normal/High/Extreme
    private final double sessionScore;   // 0-100%
}
```

### **Key Methods**

```java
// Create/update session
dataStore.updateTradingSession(sessionId, symbol, marketType, price, change, volume, volumeLevel);

// Add pattern detection
dataStore.addSessionPattern(sessionId, "ICEBERG_DETECTED", 87.5, "Large hidden order detected");

// Add AI prediction  
dataStore.addSessionPrediction(sessionId, "UP", 92.3, "4495.00", "5-10 minutes");

// Update analytics
dataStore.updateSessionAnalytics(sessionId, "Bullish", "Strong Buy", "Normal", 78.5);
```

---

## 🎯 **Best Practices**

### **For Traders:**
1. **Focus on High-Confidence:** Look for 90%+ patterns and predictions
2. **Monitor Multiple Sessions:** Each instrument behaves differently
3. **Watch Session Scores:** Higher scores indicate better setups
4. **Use Timeframes:** Align predictions with your trading timeframe

### **For Developers:**
1. **Session Isolation:** Keep data separate per session
2. **Real-Time Updates:** Ensure 2-second update intervals
3. **Memory Management:** Limit patterns (20) and predictions (10) per session
4. **Error Handling:** Gracefully handle session disconnections

---

## 🔍 **Troubleshooting**

### **No Sessions Visible:**
```
✅ Check: Bookmap is running
✅ Check: Charts are open in Bookmap  
✅ Check: BookmapAI addon is enabled
✅ Check: Dashboard shows "Real Data Only Mode"
```

### **Sessions Not Updating:**
```
✅ Check: Session panels show "LIVE" status
✅ Check: Timestamps are recent (< 10 seconds old)
✅ Check: Browser console for API errors
✅ Check: Backend logs for data flow
```

### **Missing Patterns/Predictions:**
```
✅ Check: Pattern detection engines are enabled
✅ Check: Sufficient market activity for analysis
✅ Check: Session has been active for > 30 seconds
✅ Check: Confidence thresholds (patterns show 70%+)
```

---

## 📈 **Session States**

### **🟢 Active Session**
- **Status:** LIVE with green indicator
- **Data Flow:** Real-time updates every 2 seconds
- **Features:** All patterns, predictions, and analytics available

### **🟡 Initializing Session**  
- **Status:** New session, gathering data
- **Data Flow:** Basic price/volume data available
- **Features:** Patterns and predictions loading

### **🔴 Disconnected Session**
- **Status:** Bookmap chart closed or addon disabled
- **Data Flow:** No new updates
- **Features:** Last known data displayed, marked as stale

---

## 🎉 **Success Metrics**

### **System Performance:**
- **Session Creation:** < 2 seconds after chart opens
- **Real-Time Updates:** Every 2 seconds consistently  
- **Pattern Detection:** 70%+ accuracy on detected patterns
- **Prediction Accuracy:** 80%+ for high-confidence predictions

### **User Experience:**
- **Individual Focus:** Each instrument gets dedicated attention
- **Real-Time Clarity:** Always know which data is live vs stale
- **Actionable Insights:** High-confidence signals ready for trading
- **Scalable Interface:** Handles 1-10+ sessions simultaneously

---

## 🚀 **Future Enhancements**

- **Session Grouping:** Group related instruments (NQ/ES, EUR pairs)
- **Session Comparison:** Side-by-side analysis of multiple sessions
- **Session Alerts:** Notifications for high-confidence patterns/predictions
- **Session History:** Historical analysis and performance tracking
- **Session Templates:** Pre-configured setups for different trading styles

---

**🎯 The Active Sessions system transforms BookmapAI from a single-view dashboard into a powerful multi-instrument analysis platform where each trading session gets the individual attention it deserves!**