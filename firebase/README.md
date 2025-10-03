# Notes

TODO: Delete this once things are implemented.

## Documentation links
This document mentions how to return chunks of data that will be useful for when calling gen AI tasks: https://firebase.google.com/docs/functions/callable?authuser=0#receive_streaming_results

```kotlin
// Client app call
// Get the callable by passing an initialized functions SDK.
val getForecast = functions.getHttpsCallable("getForecast");

// Call the function with the `.stream()` method and convert it to a flow
getForecast.stream(
  mapOf("locations" to favoriteLocations)
).asFlow().collect { response ->
  when (response) {
    is StreamResponse.Message -> {
      // The flow will emit a [StreamResponse.Message] value every time the
      // callable function calls `sendChunk()`.
      val forecastDataChunk = response.message.data as Map<String, Any>
      // Update the UI every time a new chunk is received
      // from the callable function
      updateUI(
        forecastDataChunk["latitude"] as Double,
        forecastDataChunk["longitude"] as Double,
        forecastDataChunk["forecast"] as Double,
      )
    }
    is StreamResponse.Result -> {
      // The flow will emit a [StreamResponse.Result] value when the
      // callable function completes.
      val allWeatherForecasts = response.result.data as List<Map<String, Any>>
      finalizeUI(allWeatherForecasts)
    }
  }
}
```
