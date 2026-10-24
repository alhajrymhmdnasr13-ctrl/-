package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.camera.PhotoPoseProcessor
import com.example.data.ClientEntity
import com.example.data.ClientRepository
import com.example.data.TailorDatabase
import com.example.model.BodyMeasurements
import com.example.model.PoseMeasurementCalculator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class TailorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ClientRepository
    init {
        val database = TailorDatabase.getDatabase(application)
        repository = ClientRepository(database.clientDao())
    }

    // Live measurements from camera
    private val _liveMeasurements = MutableStateFlow(BodyMeasurements())
    val liveMeasurements: StateFlow<BodyMeasurements> = _liveMeasurements.asStateFlow()

    // Captured / Frozen measurements
    private val _isFrozen = MutableStateFlow(false)
    val isFrozen: StateFlow<Boolean> = _isFrozen.asStateFlow()

    private val _frozenMeasurements = MutableStateFlow<BodyMeasurements?>(null)
    val frozenMeasurements: StateFlow<BodyMeasurements?> = _frozenMeasurements.asStateFlow()

    // Calibration: pixel-to-cm ratio (default 0.15 matching python script)
    private val _pixelToCmRatio = MutableStateFlow(0.15)
    val pixelToCmRatio: StateFlow<Double> = _pixelToCmRatio.asStateFlow()

    private var lastDetectedHeightPx: Float = 0f

    // Unit: cm vs in
    private val _unit = MutableStateFlow("cm")
    val unit: StateFlow<String> = _unit.asStateFlow()

    // Dialogs
    private val _showSaveDialog = MutableStateFlow(false)
    val showSaveDialog: StateFlow<Boolean> = _showSaveDialog.asStateFlow()

    private val _showCalibrateDialog = MutableStateFlow(false)
    val showCalibrateDialog: StateFlow<Boolean> = _showCalibrateDialog.asStateFlow()

    // Photo scan state
    private val _photoUri = MutableStateFlow<Uri?>(null)
    val photoUri: StateFlow<Uri?> = _photoUri.asStateFlow()

    private val _photoResult = MutableStateFlow<PoseMeasurementCalculator.CalculationResult?>(null)
    val photoResult: StateFlow<PoseMeasurementCalculator.CalculationResult?> = _photoResult.asStateFlow()

    private val _isPhotoProcessing = MutableStateFlow(false)
    val isPhotoProcessing: StateFlow<Boolean> = _isPhotoProcessing.asStateFlow()

    // Search and Clients
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _garmentFilter = MutableStateFlow("All")
    val garmentFilter: StateFlow<String> = _garmentFilter.asStateFlow()

    val clientsList: StateFlow<List<ClientEntity>> = combine(_searchQuery, _garmentFilter) { query, garment ->
        Pair(query, garment)
    }.flatMapLatest { (query, garment) ->
        val flow = if (query.isBlank()) {
            repository.getAllClients()
        } else {
            repository.searchClients(query.trim())
        }
        flow.map { list ->
            if (garment == "All") list else list.filter { it.garmentType.contains(garment, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedClient = MutableStateFlow<ClientEntity?>(null)
    val selectedClient: StateFlow<ClientEntity?> = _selectedClient.asStateFlow()

    val selectedClientHistory: StateFlow<List<ClientEntity>> = _selectedClient
        .flatMapLatest { client ->
            if (client == null) {
                flowOf(emptyList())
            } else {
                repository.getClientHistory(client.name, client.phone)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateLiveMeasurements(measurements: BodyMeasurements, heightPx: Float) {
        if (!_isFrozen.value) {
            _liveMeasurements.value = measurements
            if (heightPx > 0f) {
                lastDetectedHeightPx = heightPx
            }
        }
    }

    fun toggleFreeze() {
        if (_isFrozen.value) {
            _isFrozen.value = false
            _frozenMeasurements.value = null
        } else {
            _isFrozen.value = true
            _frozenMeasurements.value = _liveMeasurements.value
        }
    }

    fun adjustRatio(delta: Double) {
        val newRatio = ((_pixelToCmRatio.value + delta) * 1000.0) / 1000.0
        _pixelToCmRatio.value = newRatio.coerceIn(0.01, 1.0)
    }

    fun setRatio(newRatio: Double) {
        _pixelToCmRatio.value = (Math.round(newRatio * 1000.0) / 1000.0).coerceIn(0.01, 1.0)
    }

    fun calibrateWithHeight(knownHeightCm: Double) {
        val targetHeightPx = if (_photoResult.value != null && _photoResult.value!!.totalHeightPx > 0f) {
            _photoResult.value!!.totalHeightPx
        } else {
            lastDetectedHeightPx
        }

        if (targetHeightPx > 0f) {
            val calculatedRatio = PoseMeasurementCalculator.calibrateByHeight(knownHeightCm, targetHeightPx)
            _pixelToCmRatio.value = calculatedRatio
        }
        _showCalibrateDialog.value = false
    }

    fun toggleUnit() {
        _unit.value = if (_unit.value == "cm") "in" else "cm"
    }

    fun setShowSaveDialog(show: Boolean) {
        _showSaveDialog.value = show
    }

    fun setShowCalibrateDialog(show: Boolean) {
        _showCalibrateDialog.value = show
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setGarmentFilter(filter: String) {
        _garmentFilter.value = filter
    }

    fun selectClient(client: ClientEntity?) {
        _selectedClient.value = client
    }

    fun saveClient(
        name: String,
        phone: String,
        garmentType: String = "Thobe / Suit",
        notes: String = "",
        customMeasurements: BodyMeasurements? = null
    ) {
        val targetMeasurements = customMeasurements
            ?: _frozenMeasurements.value
            ?: _photoResult.value?.measurements
            ?: _liveMeasurements.value

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val timestamp = sdf.format(Date())

        val client = ClientEntity(
            name = name.ifBlank { "Client #" + System.currentTimeMillis().toString().takeLast(4) },
            phone = phone,
            timestamp = timestamp,
            shoulder = targetMeasurements.shoulderCm,
            torso = targetMeasurements.torsoCm,
            sleeve = targetMeasurements.sleeveCm,
            leg = targetMeasurements.legCm,
            height = targetMeasurements.heightCm,
            chest = targetMeasurements.chestCm,
            waist = targetMeasurements.waistCm,
            garmentType = garmentType,
            notes = notes,
            unit = _unit.value
        )

        viewModelScope.launch {
            repository.insertClient(client)
            _showSaveDialog.value = false
        }
    }

    fun updateClient(client: ClientEntity) {
        viewModelScope.launch {
            repository.updateClient(client)
            if (_selectedClient.value?.id == client.id) {
                _selectedClient.value = client
            }
        }
    }

    fun deleteClient(client: ClientEntity) {
        viewModelScope.launch {
            repository.deleteClient(client)
            if (_selectedClient.value?.id == client.id) {
                _selectedClient.value = null
            }
        }
    }

    fun setPhotoUri(context: Context, uri: Uri?) {
        _photoUri.value = uri
        if (uri != null) {
            _isPhotoProcessing.value = true
            viewModelScope.launch {
                val result = PhotoPoseProcessor.processImageUri(context, uri, _pixelToCmRatio.value)
                _photoResult.value = result
                if (result != null && result.totalHeightPx > 0f) {
                    lastDetectedHeightPx = result.totalHeightPx
                }
                _isPhotoProcessing.value = false
            }
        } else {
            _photoResult.value = null
        }
    }

    fun reprocessPhoto(context: Context) {
        val uri = _photoUri.value ?: return
        _isPhotoProcessing.value = true
        viewModelScope.launch {
            val result = PhotoPoseProcessor.processImageUri(context, uri, _pixelToCmRatio.value)
            _photoResult.value = result
            _isPhotoProcessing.value = false
        }
    }
}
