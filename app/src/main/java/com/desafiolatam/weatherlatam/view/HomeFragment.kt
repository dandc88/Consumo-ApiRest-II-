package com.desafiolatam.weatherlatam.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.desafiolatam.weatherlatam.R
import com.desafiolatam.weatherlatam.WeatherApplication
import com.desafiolatam.weatherlatam.data.CELSIUS
import com.desafiolatam.weatherlatam.data.ITEM_ID
import com.desafiolatam.weatherlatam.databinding.FragmentHomeBinding
import com.desafiolatam.weatherlatam.extension.hide
import com.desafiolatam.weatherlatam.extension.show
import com.desafiolatam.weatherlatam.model.WeatherDto
import com.desafiolatam.weatherlatam.view.adapter.WeatherAdapter
import com.desafiolatam.weatherlatam.view.viewmodel.UIState
import com.desafiolatam.weatherlatam.view.viewmodel.WeatherViewModel
import com.desafiolatam.weatherlatam.view.viewmodel.WeatherViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var adapter: WeatherAdapter
    private lateinit var tempUnit: String

    private val viewModel: WeatherViewModel by viewModels {
        WeatherViewModelFactory((activity?.application as WeatherApplication).repository)
    }

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // Esta variable ve si los datos ya fueron cargados en la base de datos
    private var isDataLoaded = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        sharedPref.getString(getString(R.string.settings_temperature_unit), CELSIUS)?.let {
            tempUnit = it
        }

        adapter = WeatherAdapter(
            weatherList = emptyList(), inCelsius = tempUnit == CELSIUS
        )

        // Solo llama a la función si los datos no se han cargado
        if (!isDataLoaded) {
            getRemoteWeatherData()
            isDataLoaded = true
        }


        getWeatherData()
        initRecyclerView()
        navigateToDetails()
        navigateToSettings()

        //Llama a la funcion que maneja el Pull To Refresh
        initSwipeRefreshLayout()
    }

    private fun initSwipeRefreshLayout() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            // Llamar a la función en el ViewModel para actualizar los datos
            lifecycleScope.launch {
                viewModel.getRemoteWeatherData().collectLatest { state ->
                    when (state) {
                        is UIState.Loading -> {
                            // Mostrar el indicador de refresco mientras se cargan los datos
                            binding.swipeRefreshLayout.isRefreshing = true
                        }
                        is UIState.Success -> {
                            // Datos cargados con éxito, ocultar el indicador de refresco
                            binding.swipeRefreshLayout.isRefreshing = false
                        }
                        is UIState.Error -> {
                            // Manejar errores si es necesario y ocultar el indicador de refresco
                            binding.swipeRefreshLayout.isRefreshing = false
                        }
                    }
                }
            }
        }
    }

    private fun getRemoteWeatherData() {
        lifecycleScope.launchWhenCreated {
            viewModel.getRemoteWeatherData().collectLatest { state ->

                when (state) {
                    is UIState.Loading -> binding.rvWeather.show()
                    is UIState.Success -> {
                        binding.rvWeather.show()
                        val list = listOf(state.data as WeatherDto)
                        populateRecyclerView(list)
                    }

                    is UIState.Error -> {
                        binding.rvWeather.hide()
                        binding.cityName.hide()
                    }
                }
            }
        }
    }

    private fun getWeatherData() {
        lifecycleScope.launchWhenCreated {
            viewModel.getWeather().collectLatest { list ->
                list?.let {
                    populateRecyclerView(it)
                }
            }
        }
    }

    private fun initRecyclerView() {
        binding.rvWeather.layoutManager = LinearLayoutManager(context)
        binding.rvWeather.adapter = adapter
    }

    private fun populateRecyclerView(list: List<WeatherDto>) {
        if (list.isNotEmpty()) {
            binding.cityName.text = list.last().cityName
            adapter.weatherList = list
            adapter.inCelsius = tempUnit == CELSIUS
        }
        adapter.notifyDataSetChanged()
    }

    private fun navigateToDetails() {
        adapter.onClick = {
            val bundle = bundleOf(ITEM_ID to it)
            findNavController().navigate(R.id.action_homeFragment_to_detailsFragment, bundle)
        }
    }

    private fun navigateToSettings() {
        binding.btnSettings.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_settingsFragment)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}