<div align="center">

# 📱 zaid&QR

### مولّد رموز QR عربي بواجهة أنيقة (Glassmorphism) — أندرويد

[![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-UI-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)](#license)

[![Min SDK](https://img.shields.io/badge/minSdk-24-orange?style=flat-square)](.)
[![Target SDK](https://img.shields.io/badge/targetSdk-36-blue?style=flat-square)](.)
[![Version](https://img.shields.io/badge/version-1.0-success?style=flat-square)](.)

</div>

---

## ✨ نظرة عامة

**zaid&QR** تطبيق أندرويد أصلي (Native) لإنشاء رموز QR بواجهة عربية بالكامل (RTL)، مبني بـ **Jetpack Compose** ومكتبة **ZXing**. يدعم 7 أنواع بيانات مختلفة بحقول مخصصة لكل نوع، مع إمكانية تضمين شعار في مركز الرمز، وحفظ/مشاركة فورية.

<div align="center">
<img src="https://img.shields.io/badge/-Glassmorphism%20UI-8A2BE2?style=flat-square" />
<img src="https://img.shields.io/badge/-RTL%20Support-00E5FF?style=flat-square" />
<img src="https://img.shields.io/badge/-Offline%20First-success?style=flat-square" />
</div>

---

## 🚀 الميزات

| الميزة | الوصف |
|---|---|
| 🔗 **رابط ويب** | توليد QR لأي عنوان URL |
| 📝 **نص عام** | ملاحظات أو معلومات حرة |
| 💬 **واتساب** | رقم + رسالة جاهزة تفتح المحادثة مباشرة |
| 📶 **واي فاي** | بطاقة اتصال تلقائي (SSID + كلمة مرور + نوع التشفير) |
| 📍 **الموقع الجغرافي** | تعبئة تلقائية من GPS الجهاز أو إدخال يدوي |
| 📧 **بريد إلكتروني** | مستلم + موضوع + نص جاهز |
| ☎️ **رقم هاتف** | اتصال مباشر بضغطة واحدة |
| 🖼️ **شعار مركزي** | 7 أيقونات جاهزة أو رفع صورة خاصة، مع تصحيح خطأ تلقائي (Error Correction Level H) |
| 💾 **حفظ ومشاركة** | حفظ تلقائي في المعرض عبر MediaStore + مشاركة مباشرة |

---

## 🛠️ التقنيات المستخدمة

```
Kotlin 2.2.10
Jetpack Compose (BOM 2024.09.00)
ZXing — توليد QR
FileProvider — مشاركة الصور
MediaStore — الحفظ في المعرض
```

---

## 📦 التثبيت والتشغيل

### المتطلبات
- [Android Studio](https://developer.android.com/studio) (أحدث إصدار)
- JDK 17+

### الخطوات

```bash
# 1. استنسخ المشروع
git clone <repo-url>
cd zaid-qr

# 2. افتحه في Android Studio
#    File > Open > اختر مجلد المشروع

# 3. دع Android Studio يزامن Gradle تلقائيًا

# 4. شغّل التطبيق على محاكي أو جهاز حقيقي
```

### البناء من سطر الأوامر

```bash
./gradlew assembleDebug
```

سيكون ملف الـ APK الناتج في:

```
app/build/outputs/apk/debug/app-debug.apk
```

> ⚠️ **قبل إصدار نسخة Release:** أزل `signingConfig = signingConfigs.getByName("debugConfig")` من `app/build.gradle.kts` واستبدله بتوقيع إنتاجي حقيقي.

---

## 📁 هيكل المشروع

```
zaid-qr/
├── app/
│   └── src/main/
│       ├── java/com/example/
│       │   ├── MainActivity.kt      # الشاشة الرئيسية + منطق توليد QR بالكامل
│       │   └── ui/theme/            # الألوان والخطوط والثيم
│       ├── res/
│       │   ├── raw/bg_video.mp4     # خلفية الفيديو المتحركة
│       │   └── xml/                 # قواعد النسخ الاحتياطي والـ FileProvider
│       └── AndroidManifest.xml
├── build.gradle.kts
└── README.md
```

---

## 🔐 الصلاحيات المطلوبة

| الصلاحية | السبب |
|---|---|
| `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` | تعبئة الإحداثيات تلقائيًا عند اختيار نوع "الموقع الجغرافي" |
| `WRITE_EXTERNAL_STORAGE` (≤ API 28) | حفظ الصورة على الأجهزة الأقدم |
| `READ_EXTERNAL_STORAGE` (≤ API 32) | التوافق مع النسخ الأقدم من أندرويد |

---

## 🗺️ خارطة الطريق (مقترحة)

- [ ] سجل/أرشيف للرموز المولّدة سابقًا
- [ ] دعم مسح (قراءة) رموز QR من الكاميرا
- [ ] تخصيص ألوان رمز QR
- [ ] فصل منطق العمل عن الواجهة (ViewModel / Repository)

---

## 📄 الترخيص

هذا المشروع خاص بـ **Zaid**. جميع الحقوق محفوظة ما لم يُذكر خلاف ذلك.

<div align="center">

صُنع بـ ❤️ باستخدام Kotlin & Jetpack Compose

</div>
