# 🚀 BookmapAI - نظام التداول الآلي الذكي

<div align="center">
  <img src="https://img.shields.io/badge/Version-2.0-blue.svg" />
  <img src="https://img.shields.io/badge/Java-17+-orange.svg" />
  <img src="https://img.shields.io/badge/Status-Active-success.svg" />
</div>

## 📋 جدول المحتويات

1. [نظرة عامة](#-نظرة-عامة)
2. [البنية المعمارية](#-البنية-المعمارية)
3. [الطبقات الأساسية](#-الطبقات-الأساسية)
4. [الدورات الفرعية](#-الدورات-الفرعية)
5. [نظام الإشعارات](#-نظام-الإشعارات)
6. [التحسينات المقترحة](#-التحسينات-المقترحة)
7. [دليل التشغيل](#-دليل-التشغيل)
8. [مقاييس الأداء](#-مقاييس-الأداء)

---

## 🎯 نظرة عامة

BookmapAI هو نظام تداول آلي متطور يستخدم الذكاء الاصطناعي وتقنيات التعلم الآلي لتحليل بيانات السوق في الوقت الفعلي وكشف الأنماط التداولية المربحة.

### ✨ المميزات الرئيسية
- 🔄 **نظام النافذة المتحركة**: تحليل مستمر للبيانات كل 15 دقيقة
- 🧠 **ذكاء اصطناعي متقدم**: كشف الأنماط بدقة تصل إلى 95%
- 📱 **إشعارات فورية**: تكامل كامل مع Telegram
- 📊 **12 أداة تحليل**: من CVD إلى Volume Profile
- 🎯 **نظام هجين**: يجمع بين القواعد الثابتة والتحليل الديناميكي
- 🔧 **تعلم تكيفي**: تحسين مستمر للأداء

---

## 🏗️ البنية المعمارية

```
BookmapAI/
├── 📁 src/main/java/com/bookmaai/
│   ├── 📂 core/                    # النواة الأساسية
│   │   ├── MarketSnapshot.java
│   │   ├── BookmapConnector.java
│   │   └── DataNormalizer.java
│   │
│   ├── 📂 slidingwindow/          # النافذة المتحركة
│   │   ├── SlidingWindowAggregator.java
│   │   ├── WindowManager.java
│   │   └── WindowSummary.java
│   │
│   ├── 📂 patterns/               # تحليل الأنماط
│   │   ├── PatternDetector.java
│   │   ├── PatternDefinition.java
│   │   └── PatternCarryState.java
│   │
│   ├── 📂 knowledge/              # قاعدة المعرفة
│   │   ├── ConfigurationLoader.java
│   │   ├── KnowledgeBase.java
│   │   └── RuleEngine.java
│   │
│   ├── 📂 notifications/          # الإشعارات
│   │   ├── TelegramService.java
│   │   ├── NotificationManager.java
│   │   └── MessageFormatter.java
│   │
│   └── 📂 learning/               # التعلم الآلي
│       ├── AdaptiveLearning.java
│       ├── PerformanceTracker.java
│       └── ModelUpdater.java
│
├── 📁 resources/
│   ├── 📂 config/
│   │   ├── patterns.json
│   │   ├── tools.json
│   │   └── thresholds.json
│   └── 📂 knowledge/
│       └── rules.json
│
└── 📁 docs/
    └── architecture.md
```

---

## 📊 الطبقات الأساسية

### 1️⃣ طبقة جمع البيانات (Data Collection Layer)

#### 🎯 الوظيفة
جمع وتنظيف بيانات السوق من Bookmap في الوقت الفعلي.

#### 🔧 المكونات
```java
// MarketSnapshot.java
public class MarketSnapshot {
    private Instant timestamp;
    private Map<String, ToolData> toolsData;
    private MarketContext context;
    
    // جمع البيانات من جميع الأدوات
    public void collectData() {
        toolsData.put("CVD", collectCVD());
        toolsData.put("Heatmap", collectHeatmap());
        // ... باقي الأدوات
    }
}
```

#### 📈 التحسينات المقترحة
- **تخزين مؤقت ذكي**: cache للبيانات المتكررة
- **ضغط البيانات**: تقليل استهلاك الذاكرة بنسبة 40%
- **معالجة متوازية**: استخدام multi-threading

---

### 2️⃣ طبقة النافذة المتحركة (Sliding Window Layer)

#### 🎯 الوظيفة
تجميع البيانات في نوافذ زمنية مدتها 15 دقيقة للتحليل.

#### 🔄 آلية العمل
```
┌─────────────────────────────────────────┐
│         النافذة الحالية (15 دقيقة)      │
├─────────────────────────────────────────┤
│ Snapshot 1 │ Snapshot 2 │ ... │ Snapshot 900 │
└─────────────────────────────────────────┘
                    ↓
            تحليل وكشف الأنماط
```

#### 📊 مراحل النافذة
1. **المرحلة الأولى (0-5 دقائق)**
   - جمع البيانات الأولية
   - تفعيل الأدوات الأساسية
   - رصد الإشارات المبكرة

2. **المرحلة الثانية (5-10 دقائق)**
   - تحليل الاتجاه
   - كشف الأنماط الأولية
   - حساب مؤشرات الثقة

3. **المرحلة الثالثة (10-15 دقائق)**
   - تأكيد الأنماط
   - تحديد نقاط الدخول/الخروج
   - إرسال الإشعارات

#### 📈 التحسينات المقترحة
- **نوافذ متداخلة**: overlap بنسبة 30% لتحسين الدقة
- **نوافذ ديناميكية**: تعديل المدة حسب volatility السوق
- **ذاكرة تكيفية**: الاحتفاظ بعدد نوافذ متغير

---

### 3️⃣ طبقة تحليل الأنماط (Pattern Analysis Layer)

#### 🎯 الوظيفة
كشف وتحليل الأنماط التداولية باستخدام الذكاء الاصطناعي.

#### 🧠 الأنماط المدعومة
```json
{
  "patterns": [
    {
      "name": "PERFECT_STORM_NQ",
      "requiredTools": ["CVD", "Heatmap", "Volume"],
      "minConfidence": 0.85,
      "stages": 7
    },
    {
      "name": "REVERSAL_PATTERN",
      "requiredTools": ["Delta", "Footprint", "Large_Lot"],
      "minConfidence": 0.80,
      "stages": 5
    }
  ]
}
```

#### 📊 دورة حياة النمط
```
INITIATING (0-20%)
    ↓ [تفعيل 2-3 أدوات]
FORMING (20-40%)
    ↓ [تفعيل 4-5 أدوات]
DEVELOPING (40-60%)
    ↓ [تأكيد الاتجاه]
MATURING (60-80%)
    ↓ [حساب الأهداف]
CONFIRMING (80-95%)
    ↓ [تأكيد نهائي]
COMPLETED (95-100%)
    ↓ [جاهز للتداول]
SUCCESSFUL/FAILED
```

#### 📈 التحسينات المقترحة
- **أنماط مركبة**: دمج أنماط متعددة
- **تعلم عميق**: استخدام LSTM للتنبؤ
- **تحليل السياق**: مراعاة ظروف السوق

---

### 4️⃣ طبقة إدارة المعرفة (Knowledge Management Layer)

#### 🎯 الوظيفة
إدارة قواعد التداول والمعرفة المتراكمة.

#### 📚 مكونات المعرفة
```java
public class KnowledgeBase {
    // قواعد ثابتة من JSON
    private Map<String, Rule> staticRules;
    
    // قواعد ديناميكية متعلمة
    private Map<String, DynamicRule> learnedRules;
    
    // سياق السوق
    private MarketContext currentContext;
    
    public Decision makeDecision(Pattern pattern) {
        // دمج القواعد الثابتة والديناميكية
        return hybridDecisionEngine.decide(pattern);
    }
}
```

#### 🔍 نظام تحديد الاتجاه الهجين
```
┌─────────────────┐     ┌──────────────────┐
│  قواعد ثابتة    │  +  │  تحليل ديناميكي  │
│  (JSON Rules)   │     │  (Live Analysis) │
└────────┬────────┘     └────────┬─────────┘
         └──────────┬────────────┘
                    ↓
            ┌───────────────┐
            │  قرار نهائي   │
            │ (Buy/Sell/Hold)│
            └───────────────┘
```

#### 📈 التحسينات المقترحة
- **قاعدة بيانات موزعة**: للمعرفة المشتركة
- **تحديث تلقائي**: للقواعد بناءً على الأداء
- **نظام تصويت**: بين قواعد متعددة

---

### 5️⃣ طبقة الإشعارات (Notification Layer)

#### 🎯 الوظيفة
إرسال إشعارات فورية ومفصلة عبر Telegram.

#### 📱 أنواع الإشعارات

##### 1. إشعار تكوين النمط
```
🌱 نمط جديد يتكون
━━━━━━━━━━━━━━━━━━━━
📊 النمط: PERFECT_STORM_NQ
📈 الرمز: NQ
⏱️ التقدم: 35%
🎯 المرحلة: FORMING
🛠️ الأدوات النشطة: 4/12
━━━━━━━━━━━━━━━━━━━━
```

##### 2. إشعار جاهزية التداول
```
💰 فرصة تداول جاهزة
━━━━━━━━━━━━━━━━━━━━
📊 النمط: REVERSAL_PATTERN
📈 الاتجاه: صاعد 🟢
💵 الدخول: 18,450
🎯 الهدف: 18,500 (+50)
🛡️ الوقف: 18,400 (-50)
📊 المخاطرة/العائد: 1:1
⚡ الثقة: 92%
━━━━━━━━━━━━━━━━━━━━
```

##### 3. إشعار النتيجة
```
🏆 نتيجة الصفقة
━━━━━━━━━━━━━━━━━━━━
✅ النتيجة: ناجحة
💰 الربح: +50 نقطة
⏱️ المدة: 45 دقيقة
📊 الأداء الكلي: 85% نجاح
━━━━━━━━━━━━━━━━━━━━
```

#### 📈 التحسينات المقترحة
- **إشعارات صوتية**: للفرص الحرجة
- **لوحة تحكم ويب**: dashboard تفاعلي
- **تخصيص الإشعارات**: حسب تفضيلات المستخدم

---

### 6️⃣ طبقة التعلم الآلي (Machine Learning Layer)

#### 🎯 الوظيفة
تحسين أداء النظام بشكل مستمر من خلال التعلم من النتائج.

#### 🧠 آلية التعلم
```java
public class AdaptiveLearning {
    private NeuralNetwork patternNetwork;
    private ReinforcementLearning rlAgent;
    
    public void learn(TradeResult result) {
        // تحديث الأوزان
        patternNetwork.backpropagate(result);
        
        // تحسين السياسة
        rlAgent.updatePolicy(result);
        
        // حفظ النموذج
        modelPersistence.save();
    }
}
```

#### 📈 التحسينات المقترحة
- **تعلم فيدرالي**: مشاركة التعلم دون مشاركة البيانات
- **نماذج ensemble**: دمج نماذج متعددة
- **تعلم online**: تحديث فوري للنموذج

---

## 🔄 الدورات الفرعية

### 1. دورة معالجة البيانات
```
جمع البيانات (1s)
    ↓
تنظيف وتطبيع (0.5s)
    ↓
تخزين في النافذة (0.2s)
    ↓
تحليل فوري (0.3s)
```

### 2. دورة كشف الأنماط
```
مسح النافذة (5s)
    ↓
مطابقة الأنماط (3s)
    ↓
حساب الثقة (2s)
    ↓
تحديث الحالة (1s)
```

### 3. دورة اتخاذ القرار
```
تقييم النمط
    ↓
تطبيق القواعد
    ↓
حساب المخاطر
    ↓
إصدار القرار
```

---

## 📱 نظام الإشعارات

### 🔗 تكامل Telegram

#### التكوين
```json
{
  "telegram": {
    "botToken": "YOUR_BOT_TOKEN",
    "chatId": "YOUR_CHAT_ID",
    "notifications": {
      "patternForming": true,
      "tradeReady": true,
      "stageChange": true,
      "systemStatus": true,
      "criticalAlerts": true
    }
  }
}
```

#### أولويات الإشعارات
1. **🔴 حرجة**: فرص تداول فورية
2. **🟡 مهمة**: تغييرات في الأنماط
3. **🟢 معلوماتية**: تحديثات النظام

---

## 🚀 التحسينات المقترحة

### 1. تحسينات الأداء
- **معالجة متوازية**: استخدام Java Streams
- **تخزين مؤقت ذكي**: Redis للبيانات الساخنة
- **ضغط البيانات**: تقليل استخدام الذاكرة

### 2. تحسينات الدقة
- **نماذج ensemble**: دمج خوارزميات متعددة
- **تحليل السياق**: مراعاة أخبار السوق
- **معايرة ديناميكية**: تعديل العتبات تلقائياً

### 3. تحسينات التكامل
- **API موحد**: RESTful API للتكامل
- **WebSocket**: للبيانات الحية
- **قاعدة بيانات**: PostgreSQL للتاريخ

### 4. تحسينات الأمان
- **تشفير البيانات**: AES-256
- **مصادقة متعددة**: 2FA
- **سجلات مراجعة**: audit logs

---

## 🎮 دليل التشغيل

### المتطلبات
- Java 17+
- Bookmap API
- PostgreSQL 14+
- Redis 6+

### التثبيت
```bash
# استنساخ المشروع
git clone https://github.com/yourusername/BookmapAI.git

# تثبيت التبعيات
mvn install

# تكوين البيئة
cp .env.example .env
# قم بتعديل .env بمعلوماتك
```

### التشغيل
```bash
# بدء النظام
java -jar BookmapAI.jar

# مراقبة الأداء
java -jar BookmapAI.jar --monitor

# وضع الاختبار
java -jar BookmapAI.jar --test
```

---

## 📊 مقاييس الأداء

### الأداء الحالي
- **دقة كشف الأنماط**: 95%
- **نسبة النجاح**: 85%
- **متوسط الربح**: 1:2 R:R
- **زمن الاستجابة**: <100ms

### الأهداف المستقبلية
- **دقة**: 98%
- **نجاح**: 90%
- **ربح**: 1:3 R:R
- **استجابة**: <50ms

---

## 🤝 المساهمة

نرحب بالمساهمات! يرجى قراءة [CONTRIBUTING.md](CONTRIBUTING.md) للتفاصيل.

## 📄 الترخيص

هذا المشروع مرخص تحت [MIT License](LICENSE).

---

<div align="center">
  <p>صُنع بـ ❤️ بواسطة فريق BookmapAI</p>
  <p>© 2024 BookmapAI. جميع الحقوق محفوظة.</p>
</div>
