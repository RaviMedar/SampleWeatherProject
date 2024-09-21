package com.testing.sampleweatherproject

import android.location.LocationManager
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.testing.sampleweatherproject.data.DataStore
import com.testing.sampleweatherproject.data.model.Main
import com.testing.sampleweatherproject.data.model.Weather
import com.testing.sampleweatherproject.data.model.WeatherData
import com.testing.sampleweatherproject.domain.GetWeatherUseCase
import com.testing.sampleweatherproject.ui.home.HomeViewModel
import com.testing.sampleweatherproject.util.GeocoderManager
import com.testing.sampleweatherproject.util.Resource
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    // Use this rule to run architecture components synchronously
    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    // Test coroutine dispatcher
    private val testDispatcher = UnconfinedTestDispatcher()

    // Mocked dependencies
    private lateinit var getWeatherUseCase: GetWeatherUseCase
    private lateinit var dataStore: DataStore
    private lateinit var locationManager: LocationManager
    private lateinit var geocoderManager: GeocoderManager

    // ViewModel under test
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        // Initialize MockK mocks
        getWeatherUseCase = mockk()
        dataStore = mockk()
        locationManager = mockk()
        geocoderManager = mockk()

        // Inject mocks into ViewModel
        viewModel = HomeViewModel(getWeatherUseCase, dataStore, locationManager, geocoderManager)

        // Set up coroutine dispatcher
        Dispatchers.setMain(testDispatcher)
    }

    @Test
    fun `loadLastCity should load city and call getWeather if city is not empty`() = runTest {
        // Mocking the lastCity in the DataStore to emit a non-empty city name
        val cityName = "New York"
        coEvery { dataStore.lastCity } returns flowOf(cityName)

        // Mock the getWeatherUseCase behavior
        coEvery { getWeatherUseCase(any()) } returns flowOf(Resource.Success(WeatherData("New York",
            Main(25.0, 25.0, 20.0, 30.0, 1000, 50),
            listOf(Weather("Clear", "icon")))))

        // Call init block (which triggers loadLastCity)
        viewModel.loadLastCity()

        // Check that lastCity is updated
        assertEquals(viewModel.lastCity.value, cityName)

        // Verify getWeather is called with the city name
        coVerify { getWeatherUseCase(cityName) }
    }

    @Test
    fun `getWeather should update state with success data when use case succeeds`() = runTest {
        // Mock successful response from getWeatherUseCase
        val mockWeatherData = WeatherData(
            name = "Los Angeles",
            main = Main(22.0, 20.0, 18.0, 26.0, 1012, 40),
            weather = listOf(Weather("Sunny", "sunny_icon"))
        )
        coEvery { getWeatherUseCase(any()) } returns flowOf(Resource.Success(mockWeatherData))

        // Call getWeather on the ViewModel
        viewModel.getWeather("Los Angeles")

        // Verify the state is updated correctly
        assertEquals(viewModel.state.value.weatherData, mockWeatherData)
    }

    @Test
    fun `getWeather should update state with error message when use case fails`() = runTest {
        // Mock error response from getWeatherUseCase
        val errorMessage = "Failed to fetch weather data"
        coEvery { getWeatherUseCase(any()) } returns flowOf(Resource.Error(errorMessage))

        // Call getWeather on the ViewModel
        viewModel.getWeather("Los Angeles")

        // Verify the state is updated with the error message
        assertEquals(viewModel.state.value.error, errorMessage)
    }

    @Test
    fun `getWeather should update state with loading state`() = runTest {
        // Mock loading response from getWeatherUseCase
        coEvery { getWeatherUseCase(any()) } returns flowOf(Resource.Loading())

        // Call getWeather on the ViewModel
        viewModel.getWeather("Los Angeles")

        // Verify the state is set to loading
        assertEquals(viewModel.state.value.isLoading, true)
    }

    @Test
    fun `saveLastCity should call saveLastCity in DataStore`() = runTest {
        // Mock saveLastCity in the DataStore
        coEvery { dataStore.saveLastCity(any()) } just Runs

        // Call saveLastCity in the ViewModel
        val cityName = "Los Angeles"
        viewModel.saveLastCity(cityName)

        // Verify that saveLastCity is called in the DataStore with the correct city name
        coVerify { dataStore.saveLastCity(cityName) }
    }
}
