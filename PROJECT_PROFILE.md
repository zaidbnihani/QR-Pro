# ملف المشروع | Project profile

## العربية

### الغرض
وفقًا لـ `README.md` و`PRD.MD`، يعرّف المستودع تطبيق Android باسم `zaid&QR` لإنشاء رموز QR بواجهة عربية. ويؤكد المصدر في `app/src/main/java/com/example/MainActivity.kt` أنواع الإدخال السبعة، توليد QR بـ ZXing، تضمين شعار اختياري، وحفظ الصور ومشاركتها. كما يتضمن التطبيق فحصًا للتحديثات من إصدارات GitHub (انظر `app/src/main/java/com/example/updater/UpdateChecker.kt`). هذه خلاصة لوثائق المستودع والمصدر، وليست تحققًا تشغيليًا من كل ميزة معلنة.

### المكونات والتدفقات الأساسية
- شاشة Compose الرئيسية والحالة وإدخال بيانات الرمز بحسب النوع في `app/src/main/java/com/example/MainActivity.kt`؛ الأنواع في `QrType` هي الرابط والنص وWhatsApp وWi-Fi والموقع والبريد والهاتف.
- يبني المصدر محتوى كل نوع، ثم يستخدم `QRCodeWriter` مع تصحيح خطأ H أو M لتوليد الصورة؛ ويحفظ الصور عبر `MediaStore` ويشاركها عبر `FileProvider` (الملف نفسه).
- يطلب موقعًا دقيقًا أو تقريبيًا عند اختيار نوع الموقع؛ الصلاحيات معلنة في `app/src/main/AndroidManifest.xml`.
- إعداد المظهر والألوان والخطوط في `app/src/main/java/com/example/ui/theme/`، والأصول والنصوص والموارد في `app/src/main/res/`.
- فحص تحديثات الإصدارات وواجهة التنبيه والتنزيل/التثبيت في `app/src/main/java/com/example/updater/`؛ الواجهة تستخدم GitHub Releases لـ `zaidbnihani/QR-Pro` عبر Retrofit في `UpdateChecker.kt`.
- خط CI في `.github/workflows/build-apk.yml` يُشغّل بناء APK عند الدفع إلى `main` أو `master` أو عند الوسم `v*`، أو يدويًا، ثم يرفع artifact وينشئ/يحدّث إصدار GitHub.

### خريطة مختصرة
```text
.
├── README.md                         # نظرة عامة وتعليمات المشروع المنشورة
├── PRD.MD                            # وصف/متطلبات المنتج
├── build.gradle.kts                  # إعداد إضافات Gradle الجذرية
├── settings.gradle.kts               # المستودعات ووحدة :app
├── gradle/libs.versions.toml         # إصدارات الإضافات والتبعيات
├── .env.example                      # نموذج إعداد؛ لا يتضمنه هذا الملف قيمًا
├── .github/workflows/build-apk.yml   # بناء APK وإصدار GitHub آليًا
└── app/
    ├── build.gradle.kts              # إعداد Android والتبعيات والاختبارات
    ├── src/main/AndroidManifest.xml  # التطبيق والصلاحيات والمكوّنات
    ├── src/main/java/com/example/
    │   ├── MainActivity.kt           # واجهة QR والتوليد والحفظ والمشاركة
    │   ├── ui/theme/                 # ألوان وثيم وخطوط Compose
    │   └── updater/                  # فحص الإصدار والتنزيل والتثبيت
    ├── src/main/res/                 # موارد Android وأصول الصور
    ├── src/test/                     # اختبارات JVM/Robolectric ولقطة UI
    └── src/androidTest/              # اختبار Android instrumented
```

### ملفات محورية
- `app/build.gradle.kts`: `namespace` هو `com.example`، و`applicationId` هو `com.aistudio.qrgenerator.yhqmzo`، الحد الأدنى API 24، و`compileSdk` 36 (minor API 1)، و`targetSdk` 36؛ الإصدار المحدد `1.1.4` ورمز البناء 6.
- `gradle/libs.versions.toml`: AGP `9.1.1`، Kotlin `2.2.10`، Compose BOM `2024.09.00`، ZXing `3.5.3`، وإصدارات AndroidX والتبعيات الأخرى. المكتبات المعرّفة ليست كلها مستخدمة؛ راجع كتل `dependencies` الفعلية في ملف التطبيق.
- `app/src/main/AndroidManifest.xml`: أذونات الإنترنت والتثبيت والموقع والتخزين القديم، والنشاط الرئيسي ومزوّد الملفات ومستقبل التثبيت.
- `app/src/main/java/com/example/updater/`: `UpdateChecker.kt`, `GitHubRelease.kt`, `AppUpdateDialog.kt`, `AppInstaller.kt`, و`InstallStatusReceiver.kt`.
- `app/src/test/java/com/example/`: اختبارات `ExampleUnitTest.kt`, `ExampleRobolectricTest.kt`, و`GreetingScreenshotTest.kt`؛ كما يوجد `ExampleInstrumentedTest.kt` في `src/androidTest`.
- ملفات إعداد التوقيع في `app/build.gradle.kts` تقرأ أسماء متغيرات البيئة `KEYSTORE_PATH`, `STORE_PASSWORD`, و`KEY_PASSWORD`. ويذكر `.env.example` المتغير `GEMINI_API_KEY`؛ لا يعرض هذا الملف أي قيم ولا يحتاج معظم التطوير الظاهر إلى مفاتيح API حسب المصدر المفحوص.

### الإعداد والتشغيل والاختبار والبناء
- README يقترح فتح المشروع ومزامنة Gradle في Android Studio، ويذكر `./gradlew assembleDebug`، لكن لا يظهر في شجرة المستودع Gradle Wrapper (`gradlew`/`gradlew.bat` وملفات wrapper). لذلك لا تعتمد على أمر wrapper قبل توفيره.
- الأمر الموثق فعليًا في CI لبناء التطبيق هو `gradle :app:assembleDebug --stacktrace --no-daemon` بعد إعداد JDK 21 وGradle 9.3.1 في `.github/workflows/build-apk.yml`. هذا هو أمر البناء المرصود في CI، وليس تقريرًا عن نجاحه في هذه المراجعة.
- الاختبارات موجودة تحت `app/src/test` و`app/src/androidTest`، لكن سير العمل المفحوص لا يشغّل مهام اختبار. لم أستنتج أمر اختبار منفذًا من الملفات.
- إعداد التوقيع يقرأ متغيرات البيئة المذكورة أعلاه، ويستخدم `KEYSTORE_PATH` افتراضيًا مسارًا محليًا داخل جذر المشروع؛ لا تضع أسرارًا في المستودع. لا يحتوي هذا الملف على قيم إعداد.

### ملاحظات عملية ونقاط غير محسومة
- ابدأ بمقارنة `README.md` و`PRD.MD` مع المصدر الفعلي؛ بعض تفاصيل المستودع لا تتطابق تمامًا بين الوصف والتنفيذ. مثلًا يصف README خلفية فيديو في `res/raw`، بينما لا يظهر مجلد `raw` في الشجرة الحالية التي فُحصت، ويستخدم `MainActivity.kt` مشغل Media3. لذلك لا تفترض توفر المورد المشار إليه.
- توجد فجوة إصدارات بين التطبيق (`versionName` 1.1.4) وخط الإصدار في workflow الذي يسمّي APK وGitHub Release بالإصدار 1.1.3؛ راجع ذلك قبل نشر إصدار.
- تتضمن ملفات الاختبار أمثلة اختبار بسيطة ولقطة واجهة؛ مدى تغطية سلوك التوليد الفعلي غير واضح من هذه الملفات وحدها.
- لم يُفحص محتوى ملفات الأسرار أو ملفات الاعتماد. تم تجنب فتح `.env` وأي ملفات مفاتيح؛ أسماء متغيرات الإعداد فقط موثقة هنا.

## English

### Purpose
According to `README.md` and `PRD.MD`, this repository describes an Android app named `zaid&QR` for generating QR codes with an Arabic interface. The source in `app/src/main/java/com/example/MainActivity.kt` confirms seven input types, ZXing QR generation, an optional center logo, and image saving and sharing. The app also checks GitHub Releases for updates (see `app/src/main/java/com/example/updater/UpdateChecker.kt`). This summary reflects repository documentation and source; it is not an operational verification of every advertised feature.

### Main components and flows
- The main Compose screen, state, and type-specific input are in `app/src/main/java/com/example/MainActivity.kt`; `QrType` defines link, text, WhatsApp, Wi-Fi, location, email, and phone.
- Source constructs each type's content, then uses `QRCodeWriter` with error correction H or M to generate an image; it saves images through `MediaStore` and shares them through `FileProvider` (same file).
- It requests fine or coarse location when the location type is selected; permissions are declared in `app/src/main/AndroidManifest.xml`.
- Theme, colors, and typography are under `app/src/main/java/com/example/ui/theme/`; Android assets, strings, and other resources are under `app/src/main/res/`.
- Release-update checking and the update/download/install UI are in `app/src/main/java/com/example/updater/`; the UI checks GitHub Releases for `zaidbnihani/QR-Pro` through Retrofit in `UpdateChecker.kt`.
- CI in `.github/workflows/build-apk.yml` builds an APK on pushes to `main` or `master`, `v*` tags, or manual dispatch, then uploads an artifact and creates/updates a GitHub Release.

### Short map
```text
.
├── README.md                         # Published overview and instructions
├── PRD.MD                            # Product description/requirements
├── build.gradle.kts                  # Root Gradle plugin configuration
├── settings.gradle.kts               # Repositories and :app module
├── gradle/libs.versions.toml         # Plugin and dependency versions
├── .env.example                      # Configuration template; values omitted here
├── .github/workflows/build-apk.yml   # APK build and GitHub release automation
└── app/
    ├── build.gradle.kts              # Android, dependencies, and test configuration
    ├── src/main/AndroidManifest.xml  # App, permissions, and components
    ├── src/main/java/com/example/
    │   ├── MainActivity.kt           # QR UI, generation, saving, and sharing
    │   ├── ui/theme/                 # Compose colors, theme, and typography
    │   └── updater/                  # Release checking, download, and install
    ├── src/main/res/                 # Android resources and image assets
    ├── src/test/                     # JVM/Robolectric tests and UI screenshot
    └── src/androidTest/              # Instrumented Android test
```

### Key files
- `app/build.gradle.kts`: `namespace` is `com.example`, `applicationId` is `com.aistudio.qrgenerator.yhqmzo`, minimum API is 24, `compileSdk` is 36 (minor API 1), and `targetSdk` is 36; configured app version is `1.1.4`, version code 6.
- `gradle/libs.versions.toml`: AGP `9.1.1`, Kotlin `2.2.10`, Compose BOM `2024.09.00`, ZXing `3.5.3`, plus AndroidX and other dependency versions. Not every catalog entry is used; check the app module's actual `dependencies` block.
- `app/src/main/AndroidManifest.xml`: internet, install, location, and legacy storage permissions, plus the launcher activity, file provider, and install receiver.
- `app/src/main/java/com/example/updater/`: `UpdateChecker.kt`, `GitHubRelease.kt`, `AppUpdateDialog.kt`, `AppInstaller.kt`, and `InstallStatusReceiver.kt`.
- `app/src/test/java/com/example/`: `ExampleUnitTest.kt`, `ExampleRobolectricTest.kt`, and `GreetingScreenshotTest.kt`; `ExampleInstrumentedTest.kt` is under `src/androidTest`.
- Signing configuration in `app/build.gradle.kts` reads environment variable names `KEYSTORE_PATH`, `STORE_PASSWORD`, and `KEY_PASSWORD`. `.env.example` mentions `GEMINI_API_KEY`; this profile includes no values, and the inspected source does not show that normal development needs an API key.

### Setup, run, test, and build
- The README suggests opening the project and syncing Gradle in Android Studio and lists `./gradlew assembleDebug`, but the repository tree has no Gradle Wrapper (`gradlew`/`gradlew.bat` and wrapper files). Do not rely on that wrapper command unless a wrapper is supplied.
- The build command actually used by CI is `gradle :app:assembleDebug --stacktrace --no-daemon`, after setting up JDK 21 and Gradle 9.3.1 in `.github/workflows/build-apk.yml`. This is the observed CI command, not a claim that it succeeded during this review.
- Tests exist under `app/src/test` and `app/src/androidTest`, but the inspected workflow does not run test tasks. No test command is inferred as executed from the files.
- Signing reads the environment variable names above, with `KEYSTORE_PATH` defaulting to a local path under the project root; do not commit secrets. This profile includes no configuration values.

### Practical notes and open questions
- Compare `README.md` and `PRD.MD` with the actual source before relying on their details; repository descriptions do not always match implementation. For example, README describes a video background under `res/raw`, while the inspected tree has no `raw` directory and `MainActivity.kt` uses a Media3 player. Do not assume the referenced resource is present.
- There is a version discrepancy: the app is `versionName` 1.1.4, while the workflow names the APK and GitHub Release as 1.1.3; review this before publishing.
- Test files include simple example tests and a UI screenshot; the extent of coverage for actual QR-generation behavior is unclear from these files alone.
- Secret contents and credential files were not inspected. `.env` and key files were intentionally not opened; only configuration variable names are documented here.