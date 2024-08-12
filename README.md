## Este proyecto se basa en apoyo desafio evaluado - Consumo api rest (II)

**Se elimino de Contants.kt OPEN_WEATHER_KEY para publicarlo**

## 1. Actualizar la clase `RepositoryImp` para trabajar con el endpoint y guardar los datos en Room

**Descripción**: La clase `WeatherRepositoryImp` ha sido diseñada para interactuar tanto con el endpoint de la API de OpenWeather como con la base de datos local de Room.

**Implementación**:
- Se utiliza Retrofit para hacer la solicitud de datos al endpoint.
- Los datos obtenidos se transforman utilizando mappers antes de ser almacenados en Room.
- Se utilizan métodos `suspend` para las operaciones de inserción y obtención de datos desde Room.

**Ejemplo de código**:

```kotlin
override suspend fun getRemoteWeatherData(): Flow<ServiceResponse<WeatherDto?>> {
    // ... Lógica para hacer la llamada al endpoint
    if (response.isSuccessful) {
        response.body()?.let { weatherWrapper ->
            val weatherDto = weatherWrapper.toWeatherDto()
            weatherDao.insertData(weatherDto.toEntity()) // Guardado en Room
            data.value = ServiceResponse.Success(weatherDto)
        }
    }
    // ... Manejo de errores y retorno de flujo
}
```
## 2. Implementar la funcionalidad Pull To Refresh

**Funcionalidad**: El usuario puede refrescar los datos meteorológicos en la pantalla principal mediante un gesto de "Pull To Refresh".

**Implementación**:
- Se utiliza `SwipeRefreshLayout` en el `HomeFragment` para manejar el gesto de refresco.
- Al activarse, se llama a una función en el ViewModel que hace una nueva solicitud al endpoint, guarda los datos en Room, y actualiza la UI automáticamente.

**Ejemplo de código**:

```kotlin
private fun initSwipeRefreshLayout() {
    binding.swipeRefreshLayout.setOnRefreshListener {
        lifecycleScope.launch {
            viewModel.refreshWeatherData()
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }
}
```
## 3. Mostrar los datos usando StateFlow y usar el método collect() correctamente

**Funcionalidad**: Los cambios en la base de datos de Room se reflejan automáticamente en la UI.

**Implementación**:
- `StateFlow` se utiliza para emitir los estados de la aplicación, asegurando que cualquier cambio en la base de datos local se muestre automáticamente en la interfaz de usuario.
- `collectLatest` se usa en los fragments para escuchar los cambios y actualizar la UI.

**Ejemplo de código en HomeFragment.kt**:

```kotlin
lifecycleScope.launchWhenCreated {
    viewModel.getWeather().collectLatest { list ->
        list?.let { populateRecyclerView(it) }
    }
}
lifecycleScope.launchWhenResumed {
    viewModel.getWeatherDataById(id).collectLatest { weather ->
        weather?.let {
            binding.currentTemp.text = viewModel.formatTemperature(it.currentTemp, unit)
            binding.maximumTemp.text = getString(R.string.max_temp, viewModel.formatTemperature(it.maxTemp, unit))
            binding.minimumTemp.text = getString(R.string.min_temp, viewModel.formatTemperature(it.minTemp, unit))
            // Otros campos...
        }
    }
}
```
## 4. Permitir al usuario cambiar entre Métrico e Imperial

**Funcionalidad**: El usuario puede cambiar entre unidades de medida métricas e imperiales a través de la configuración, y la UI se actualizará en consecuencia.

**Implementación**:
- La preferencia del usuario se guarda en `SharedPreferences` y se aplica en toda la aplicación.
- Se implementa una función en el ViewModel para formatear las temperaturas según la unidad seleccionada, que es llamada desde los fragments correspondientes (`HomeFragment` y `DetailsFragment`).

**Ejemplo de código**:

```kotlin
fun formatTemperature(temperature: Double, unit: String?): String {
    return when (unit) {
        CELSIUS -> "$temperature°C"
        FAHRENHEIT -> "${temperature.toFahrenheit()}°F"
        else -> "$temperature°C"
    }
}
```


