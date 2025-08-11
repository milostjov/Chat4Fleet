package com.jkpmediana.fleetnotes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.awaitResponse

class KontoViewModel : ViewModel() {

    private val _konta = MutableStateFlow<List<Konto>>(emptyList())
    val konta: StateFlow<List<Konto>> = _konta

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var hasLoadedOnce = false

    /** Učitaj ako već nismo (keš u memoriji ostaje aktivan dok app proces živi) */
    fun ensureLoaded() {
        if (!hasLoadedOnce) loadKonta(forceRefresh = true)
    }

    fun loadKonta(forceRefresh: Boolean = false) {
        if (_isLoading.value) return
        if (hasLoadedOnce && !forceRefresh) return

        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val resp = ApiClient.api.getKonta().awaitResponse()
                if (resp.isSuccessful) {
                    _konta.value = resp.body().orEmpty()
                    hasLoadedOnce = true
                } else {
                    _error.value = "Greška ${resp.code()}"
                }
            } catch (t: Throwable) {
                _error.value = t.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** Dodaj komentar i refrešuj listu (osvežavanje zadržava keš). */
    fun addKonto(brojKonta: String, komentar: String, userName: String, onDone: () -> Unit) {
        if (brojKonta.isBlank() || komentar.isBlank()) return

        viewModelScope.launch {
            try {
                val resp = ApiClient.api.addComment(NewComment(brojKonta, komentar, userName)).awaitResponse()
                if (resp.isSuccessful) {
                    // Odmah osveži listu (force) da UI vidi promenu
                    loadKonta(forceRefresh = true)
                    onDone()
                } else {
                    _error.value = "Greška ${resp.code()}"
                    onDone()
                }
            } catch (t: Throwable) {
                _error.value = t.message
                onDone()
            }
        }
    }
}
