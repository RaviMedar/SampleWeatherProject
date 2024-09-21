package com.testing.sampleweatherproject

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.testing.sampleweatherproject.data.model.Main
import com.testing.sampleweatherproject.data.model.Weather
import com.testing.sampleweatherproject.data.model.WeatherData
import com.testing.sampleweatherproject.ui.home.HomeScreen
import com.testing.sampleweatherproject.ui.home.HomeViewModel
import com.testing.sampleweatherproject.ui.home.WeatherState
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        hiltRule.inject()

        // Mock the ViewModel's behavior
        viewModel = mockk(relaxed = true)

        // Mock default values for StateFlows
        coEvery { viewModel.state } returns MutableStateFlow(WeatherState(isLoading = false))
        coEvery { viewModel.lastCity } returns MutableStateFlow("New York")
        coEvery { viewModel.locationPermissionGranted } returns MutableStateFlow(true)

        // Set up the composable under test
        composeTestRule.setContent {
            HomeScreen(viewModel = viewModel)
        }
    }

    @Test
    fun testInitialUIState() {
        // Verify that the TextField is populated with the lastCity
        composeTestRule.onNodeWithText("New York").assertIsDisplayed()

        // Verify that the "Get Weather" button is displayed
        composeTestRule.onNodeWithText("Get Weather").assertIsDisplayed()
    }

    @Test
    fun testLoadingState() {
        // Mock a loading state in the ViewModel
        coEvery { viewModel.state } returns MutableStateFlow(WeatherState(isLoading = true))

        // Trigger recomposition
        composeTestRule.setContent {
            HomeScreen(viewModel = viewModel)
        }

        // Check if the CircularProgressIndicator is displayed
        composeTestRule.onNode(isRoot()).onChild().assertExists(hasTestTag("CircularProgressIndicator").toString())
    }

    @Test
    fun testErrorState() {
        // Mock an error state in the ViewModel
        val errorMessage = "An unexpected error occurred"
        coEvery { viewModel.state } returns MutableStateFlow(WeatherState(error = errorMessage))

        // Trigger recomposition
        composeTestRule.setContent {
            HomeScreen(viewModel = viewModel)
        }

        // Verify that the error message is displayed
        composeTestRule.onNodeWithText(errorMessage).assertIsDisplayed()
    }

    @Test
    fun testWeatherDataDisplayed() {
        // Mock weather data in the ViewModel
        val mockWeatherData = WeatherData("New York",
            Main(25.0, 25.0, 20.0, 30.0, 1000, 50),
            listOf(Weather("Clear", "icon")))
        coEvery { viewModel.state } returns MutableStateFlow(WeatherState(weatherData = mockWeatherData))

        // Trigger recomposition
        composeTestRule.setContent {
            HomeScreen(viewModel = viewModel)
        }

        // Check if the city, temperature, and description are displayed
        composeTestRule.onNodeWithText("City: New York").assertIsDisplayed()
        composeTestRule.onNodeWithText("Temperature: 20.0°C").assertIsDisplayed()
        composeTestRule.onNodeWithText("Description: Clear sky").assertIsDisplayed()
    }

    @Test
    fun testGetWeatherButtonFunctionality() {
        // Enter a city name
        composeTestRule.onNodeWithText("Enter US city name").performTextInput("Los Angeles")

        // Click the "Get Weather" button
        composeTestRule.onNodeWithText("Get Weather").performClick()

        // Verify that the ViewModel's getWeather function is called
        coVerify { viewModel.getWeather("Los Angeles") }
    }
}