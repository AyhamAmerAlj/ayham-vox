# AYHAM VOX

**GAMING WITHOUT LANGUAGE BARRIERS.** — BY AYHAM

AI Gaming Translator: تطبيق هاتف + تطبيق تلفزيون Android يعملان معاً لعرض ترجمة الألعاب على الشاشة.

## الحالة: V0.1 (تجريبي)

- ✅ واجهة تطبيق الهاتف (Jetpack Compose)
- ✅ واجهة تطبيق Android TV
- ✅ اتصال تجريبي عبر الشبكة المحلية (WebSocket) برمز اقتران
- ✅ Mock Translation Engine (فصحى + عراقي) بدون أي API
- ⏳ Speech-to-Text وترجمة AI حقيقية → V0.2 (بعد موافقتك)

## الهيكل

| المجلد | الوظيفة |
|---|---|
| `shared-core` | النماذج، بروتوكول الرسائل، محرك Mock، خادم/عميل WebSocket |
| `android-phone` | تطبيق الهاتف |
| `android-tv` | تطبيق التلفزيون (يشغّل الخادم المحلي) |
| `backend` | محجوز لـ V0.2 (مزودو STT/AI، المفاتيح تبقى هنا وليس في التطبيقات) |
| `documentation` | طريقة التثبيت والاستخدام |

## البناء

يتم بالكامل على GitHub Actions (لا حاجة لكمبيوتر). افتح تبويب **Actions** ← آخر تشغيل ← **Artifacts** وحمّل:

- `ayham-vox-phone-debug`
- `ayham-vox-tv-debug`
