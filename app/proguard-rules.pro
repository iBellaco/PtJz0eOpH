# ===================================================================
# OFUSCACIÓN AVANZADA Y REGLAS DE SEGURIDAD R8 / PROGUARD
# ===================================================================

# Optimización y reducción de metadatos de depuración
-optimizationpasses 5
-allowaccessmodification
-repackageclasses 'com.example.wrdftx.o'
-renamesourcefileattribute SourceFile
-keepattributes *Annotation*, InnerClasses, EnclosingMethod, Signature, Exceptions

# Eliminar logs de depuración en compilaciones ofuscadas de release
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# Mantener Componentes Clave del Sistema Android (Declarados en AndroidManifest)
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# Keep data models used for serialization (Kotlinx Serialization, Firebase, Room, Json)
-keep class com.example.model.** { *; }
-keep class com.example.data.remote.model.** { *; }
-keep class com.example.data.local.entity.** { *; }

# Mantener serializadores generados por kotlinx.serialization
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}
-keepclassmembers class * implements kotlinx.serialization.KSerializer {
    public static *** INSTANCE;
}
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}

# Firebase & Google Play Services (Mapeos y Reflection)
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Room (Mantener DAOs y Entidades)
-keep class com.example.data.local.dao.** { *; }
-keepclassmembers class * {
    @androidx.room.Query *;
    @androidx.room.Insert *;
    @androidx.room.Update *;
    @androidx.room.Delete *;
    @androidx.room.Transaction *;
}

# Jetpack Compose (Reglas de estabilidad para recomposiciones)
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# AndroidJUnitRunner uses this shared dependency from the tested APK. It may
# otherwise be removed because production code does not call its public API.
-keep class androidx.tracing.Trace { *; }

# The installed test APK shares these libraries with the release app. Their
# public APIs must remain callable by the runner and the real native OCR test.
# Coach's own implementation classes remain eligible for R8 obfuscation.
-keep class kotlin.** { *; }
-keep class com.google.mlkit.** { *; }

# Coroutines & Kotlin Reflection
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**

# Jsoup & Networking
-keep class org.jsoup.** { *; }
-dontwarn org.jsoup.**
-dontwarn java.lang.management.**
-dontwarn io.ktor.**

# Módulos de Seguridad y Anti-Tampering (Evitar que sean removidos por dead-code elimination)
-keep class com.Coach { *; }
-keep class com.example.WildRiftApp { *; }
-keep class com.example.util.AppSecurityManager { *; }
-keep class com.example.util.DeviceAndSessionManager { *; }
-keep class com.example.util.SubscriptionManager { *; }

# App Notices, Analytics and Media
-keep class com.example.data.AppNotice** { *; }
-keep class com.example.data.NoticeMetrics { *; }
-keep class com.example.data.AppNoticeAnalyticsManager** { *; }
-keep class com.example.ui.components.NoticeMediaUtils** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**

# AndroidX WorkManager (Keep Workers, InputMergers, and reflection constructors)
-keep class androidx.work.** { *; }
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class * extends androidx.work.InputMerger {
    public <init>();
}
-keep class androidx.work.OverwritingInputMerger {
    public <init>();
}
-keep class androidx.work.ArrayCreatingInputMerger {
    public <init>();
}
-keep class com.example.service.** { *; }
-dontwarn androidx.work.**

# Exercise the installed history UI in a real service-style window, including its theme.
-keep class com.example.ui.screens.DraftHistoryScreenKt { public static void DraftHistoryScreen(...); }
-keep class com.example.ui.theme.ThemeKt { public static void MyApplicationTheme(...); }
-keep class com.example.util.AppLanguage { *; }

# The release instrumentation hosts an actual service-style Compose window.
# Keep only the lifecycle/saved-state API it calls; Coach's own code stays obfuscated.
-keep class androidx.savedstate.SavedStateRegistryController** { *; }
-keep class androidx.savedstate.SavedStateRegistryOwner { *; }
-keep class androidx.savedstate.SavedStateRegistry { *; }
-keep class androidx.savedstate.ViewTreeSavedStateRegistryOwner { *; }
-keep class androidx.lifecycle.Lifecycle { *; }
-keep class androidx.lifecycle.Lifecycle$Event { *; }
-keep class androidx.lifecycle.Lifecycle$State { *; }
-keep class androidx.lifecycle.LifecycleOwner { *; }
-keep class androidx.lifecycle.LifecycleRegistry { *; }
-keep class androidx.lifecycle.ViewModelStore { *; }
-keep class androidx.lifecycle.ViewModelStoreOwner { *; }
-keep class androidx.lifecycle.ViewTreeLifecycleOwner { *; }
-keep class androidx.lifecycle.ViewTreeViewModelStoreOwner { *; }

# The release Compose test runner shares the coroutine runtime with the APK.
# Retain callable APIs and subclass extensibility; Coach remains eligible for obfuscation.
-keep class kotlinx.coroutines.** { *; }
# Public entry points hosted by the installed deletion interaction tests.
-keep class com.example.ui.components.AccountDeletionCardKt { public static void AccountDeletionCard(...); }
-keep class com.example.util.DynamicTranslations { public *; }
-keep class androidx.activity.compose.ComponentActivityKt { public static void setContent*(...); }
