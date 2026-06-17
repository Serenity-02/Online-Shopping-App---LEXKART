package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.viewmodel.ShoppingViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
    assertEquals("LEXKART", appName)
  }

  @Test
  fun `viewmodel login and state flow verification`() = runTest {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = ShoppingViewModel(application)

    // Verify initial state is empty because there are no logged in users
    assertEquals(null, viewModel.currentUser.value)

    // Register a new user
    viewModel.register("Test User", "test@test.com", "mypass123")
    
    // Give flow a tiny tick to execute insert and update
    var current = viewModel.currentUser.value
    // Wait for the flow to receive the newly registered user (if it propagates)
    for (i in 1..20) {
      current = viewModel.currentUser.value
      if (current != null) break
      kotlinx.coroutines.delay(50)
    }

    assertNotNull("User registration did not log in the user", current)
    assertEquals("Test User", current?.username)
    assertEquals("test@test.com", current?.email)
  }
}
