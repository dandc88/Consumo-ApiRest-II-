package com.desafiolatam.weatherlatam.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow


@Dao
interface WeatherDao {

    @Query("SELECT * FROM weather")
    fun getWeatherData(): Flow<List<WeatherEntity>?>

    @Query("SELECT * FROM weather WHERE id =:id")
    fun getWeatherDataById(id: Int): Flow<WeatherEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertData(weatherEntity: WeatherEntity)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun updateCityName(weatherEntity: WeatherEntity)

    @Query("DELETE FROM weather")
    suspend fun clearAll()
}