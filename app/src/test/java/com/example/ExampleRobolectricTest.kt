package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Coach", appName)
  }

  @Test
  fun `verify all runes and drawables load without exception`() {
    val runes = com.example.data.WildRiftSpellsAndRunes.runes
    org.junit.Assert.assertTrue(runes.isNotEmpty())
    val context = ApplicationProvider.getApplicationContext<Context>()
    runes.forEach { rune ->
      val drawableRes = com.example.data.WildRiftSpellsAndRunes.getRuneDrawableRes(rune.name)
      if (drawableRes != null && drawableRes != 0) {
        val drawable = try { androidx.core.content.ContextCompat.getDrawable(context, drawableRes) } catch (_: Exception) { null }
        org.junit.Assert.assertNotNull("Drawable resource should load for ${rune.name}", drawable)
      }
    }
  }
}
