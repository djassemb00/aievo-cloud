# خطة بناء مشروع AI EVO المتكامل

الهدف هو تحويل المشروع من مجرد قالب أندرويد لـ "المخبوزات" إلى نظام ذكاء اصطناعي سيادي متكامل يربط بين واجهة المستخدم الأندرويد ونواة التطور الذاتي (Python Core).

## 1. تطوير واجهة المستخدم (Android UI)

سيتم استبدال شاشة المخبوزات بشاشة "لوحة التحكم السيادية" (Sovereign Dashboard).

### [Sovereign Dashboard Component]

#### [MainActivity.kt](file:///C:/Users/hp/AndroidStudioProjects/AIEVO/app/src/main/java/com/djb/aievo/MainActivity.kt)
- تحديث المحتوى لاستدعاء `AIEVODashboard()` بدلاً من `BakingScreen()`.

#### [NEW] [AIEVODashboard.kt](file:///C:/Users/hp/AndroidStudioProjects/AIEVO/app/src/main/java/com/djb/aievo/AIEVODashboard.kt)
- إنشاء واجهة جديدة تعرض:
    - **نظام المراقبة**: حالة المعالج والذاكرة.
    - **الخزينة**: الرصيد الحالي للـ AI.
    - **سجل الوكلاء**: عرض حوارات الوكلاء المتخصصين في الوقت الفعلي.
    - **الوعي الذاتي**: عرض خريطة الكود التي تم تحليلها.

#### [DELETE] [BakingScreen.kt](file:///C:/Users/hp/AndroidStudioProjects/AIEVO/app/src/main/java/com/djb/aievo/BakingScreen.kt)
#### [DELETE] [BakingViewModel.kt](file:///C:/Users/hp/AndroidStudioProjects/AIEVO/app/src/main/java/com/djb/aievo/BakingViewModel.kt)

---

## 2. تطوير جسر التواصل (Communication Bridge)

لتمكين أندرويد من التحدث مع نواة البايثون، سننشئ واجهة برمجية (API) بسيطة.

### [API Layer - Python]

#### [NEW] [api_server.py](file:///C:/Users/hp/AndroidStudioProjects/AIEVO/aievo/api/server.py)
- إنشاء خادم Flask/FastAPI صغير داخل مجلد `aievo/api`.
- توفير endpoints لجلب:
    - `GET /status`: حالة النظام، الرصيد، والموارد.
    - `GET /logs`: آخر حوارات الوكلاء.
    - `POST /evolve`: بدء دورة تطوير جديدة بناءً على طلب المستخدم.

---

## 3. تكامل النواة (Core Integration)

#### [main.py](file:///C:/Users/hp/AndroidStudioProjects/AIEVO/aievo/main.py)
- تحديث الملف الرئيسي ليعمل كخادم (Server Mode) عند الحاجة، أو دمجه مع `api_server.py`.

## خطة التحقق

### التحقق اليدوي
1. تشغيل خادم البايثون `api_server.py`.
2. تشغيل تطبيق الأندرويد والتأكد من ظهور البيانات الحقيقية من النواة (الرصيد، الموارد) بدلاً من صور الكعك.
3. إرسال طلب "تطوير" من الأندرويد ومراقبة النواة وهي تبدأ دورة التفكير.
