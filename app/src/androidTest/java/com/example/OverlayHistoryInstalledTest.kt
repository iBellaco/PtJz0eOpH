package com.example

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.os.SystemClock
import android.view.WindowManager
import android.view.accessibility.AccessibilityNodeInfo
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.ui.screens.DraftHistoryScreen
import com.example.ui.theme.MyApplicationTheme
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/** A real Android overlay window with service owners and no activity dispatcher. */
@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
@RunWith(AndroidJUnit4::class)
class OverlayHistoryInstalledTest {
    private class Owners : LifecycleOwner, SavedStateRegistryOwner, ViewModelStoreOwner {
        val registry = LifecycleRegistry(this)
        private val state = SavedStateRegistryController.create(this)
        override val lifecycle: Lifecycle get() = registry
        override val savedStateRegistry get() = state.savedStateRegistry
        override val viewModelStore = ViewModelStore()
        fun start() {
            state.performRestore(null)
            registry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
            registry.handleLifecycleEvent(Lifecycle.Event.ON_START)
            registry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        }
    }

    @Test fun historyOpensAndReturnsFromTheInstalledObfuscatedOverlay() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext.applicationContext
        val automation = instrumentation.uiAutomation
        android.os.ParcelFileDescriptor.AutoCloseInputStream(automation.executeShellCommand("appops set ${context.packageName} SYSTEM_ALERT_WINDOW allow")).use { it.readBytes() }
        automation.serviceInfo = automation.serviceInfo.apply {
            flags = flags or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        }
        val manager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val closed = AtomicBoolean(false)
        lateinit var view: ComposeView
        lateinit var owners: Owners
        instrumentation.runOnMainSync {
            com.example.util.AppLanguage.select(context, "es")
            owners = Owners().apply { start() }
            view = ComposeView(context).apply {
                setViewTreeLifecycleOwner(owners)
                setViewTreeSavedStateRegistryOwner(owners)
                setViewTreeViewModelStoreOwner(owners)
                setContent {
                    MyApplicationTheme {
                        Box(Modifier.semantics { testTagsAsResourceId = true }) {
                            DraftHistoryScreen(isOverlay = true, onNavigateBack = { closed.set(true) },
                                onLoadDraft = { _, _, _, _ -> })
                        }
                    }
                }
            }
            val metrics = context.resources.displayMetrics
            manager.addView(view, WindowManager.LayoutParams(
                minOf((330 * metrics.density).toInt(), metrics.widthPixels - 16),
                minOf((520 * metrics.density).toInt(), metrics.heightPixels - 60),
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT))
        }
        try {
            fun find(node: AccessibilityNodeInfo?, tag: String): AccessibilityNodeInfo? {
                if (node == null) return null
                if (node.viewIdResourceName?.endsWith(tag) == true) return node
                for (index in 0 until node.childCount) find(node.getChild(index), tag)?.let { return it }
                return null
            }
            fun waitFor(tag: String): AccessibilityNodeInfo {
                val deadline = SystemClock.elapsedRealtime() + 15000
                while (SystemClock.elapsedRealtime() < deadline) {
                    automation.windows.forEach { find(it.root, tag)?.let { node -> return node } }
                    SystemClock.sleep(200)
                }
                throw AssertionError("Overlay control did not render: $tag")
            }
            waitFor("draft_history_title")
            val folder = File(context.getExternalFilesDir(null), "coach-device-audit").apply { mkdirs() }
            automation.takeScreenshot()?.let { picture ->
                File(folder, "overlay-history-es.png").outputStream().use { picture.compress(Bitmap.CompressFormat.PNG, 100, it) }
                picture.recycle()
            }
            assertTrue(waitFor("history_back_button").performAction(AccessibilityNodeInfo.ACTION_CLICK))
            val deadline = SystemClock.elapsedRealtime() + 5000
            while (!closed.get() && SystemClock.elapsedRealtime() < deadline) SystemClock.sleep(100)
            assertTrue("History did not return", closed.get())
        } finally {
            instrumentation.runOnMainSync {
                manager.removeViewImmediate(view)
                owners.registry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
                owners.registry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
                owners.registry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            }
        }
    }
}
