package com.nordisapps.nordisradiojournal.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.nordisapps.nordisradiojournal.data.TranslationRepository
import com.nordisapps.nordisradiojournal.data.model.Announcement
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.LocalDate

class AnnouncementsViewModel(
    application: Application,
    private val shared: SharedStateHolder,
    private val getCurrentLanguage: () -> String
) : AndroidViewModel(application) {

    private companion object {
        const val ENABLE_ANNOUNCEMENTS = true
    }

    private val firestore = FirebaseFirestore.getInstance()
    private val translationRepository = TranslationRepository(application)
    private var translationJob: Job? = null
    private var lastTranslatedLanguage: String? = null
    private var originalAnnouncements: List<Announcement> = emptyList()
    private var announcementsListener: ListenerRegistration? = null
    var allAnnouncements by mutableStateOf<List<Announcement>>(emptyList())
        private set
    var announcements by mutableStateOf<List<Announcement>>(emptyList())
        private set

    var isTranslating by mutableStateOf(false)
        private set

    init {
        if (ENABLE_ANNOUNCEMENTS) {
            loadAnnouncement()
        }
    }

    private fun loadAnnouncement() {
        announcementsListener?.remove()
        announcementsListener = firestore.collection("announcements")
            .whereEqualTo("enabled", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    shared.update { it.copy(isLoading = false) }
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener

                val today = LocalDate.now()

                val active = snapshot.documents
                    .mapNotNull { doc ->
                        doc.toObject(Announcement::class.java)?.copy(id = doc.id)
                    }
                    .filter { isInDateRange(it, today) }
                    .sortedWith(compareByDescending<Announcement> { it.priority }
                        .thenByDescending { it.startDate })

                originalAnnouncements = active
                announcements = active
                shared.update { it.copy(isLoading = false) }

                translateAnnouncements(getCurrentLanguage())
            }
    }

    fun loadAllAnnouncementsForAdmin() {
        firestore.collection("announcements")
            .get()
            .addOnSuccessListener { snapshot ->
                allAnnouncements = snapshot.documents
                    .mapNotNull { doc ->
                        doc.toObject(Announcement::class.java)?.copy(id = doc.id)
                    }
                    .sortedWith(
                        compareByDescending<Announcement> { it.priority }
                            .thenByDescending { it.startDate }
                    )
                shared.update { it.copy(announcements = allAnnouncements) }
            }
    }

    fun saveAnnouncement(
        announcement: Announcement,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val data = mapOf(
            "enabled" to announcement.enabled,
            "type" to announcement.type,
            "priority" to announcement.priority,
            "startDate" to announcement.startDate,
            "endDate" to announcement.endDate,
            "emoji" to announcement.emoji,
            "imageUrl" to announcement.imageUrl,
            "title" to announcement.title,
            "description" to announcement.description,
            "actionText" to announcement.actionText,
            "actionType" to announcement.actionType,
            "actionValue" to announcement.actionValue
        )

        val ref = if (announcement.id.isBlank()) {
            firestore.collection("announcements").document()
        } else {
            firestore.collection("announcements").document(announcement.id)
        }

        ref.set(data)
            .addOnSuccessListener {
                loadAllAnnouncementsForAdmin()
                onSuccess()
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun deleteAnnouncement(
        announcement: Announcement,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (announcement.id.isBlank()) return
        firestore.collection("announcements").document(announcement.id)
            .delete()
            .addOnSuccessListener {
                loadAllAnnouncementsForAdmin()
                onSuccess()
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun translateAnnouncements(targetLanguage: String) {
        if (targetLanguage == "en") {
            announcements = originalAnnouncements
            lastTranslatedLanguage = targetLanguage
            return
        }
        if (originalAnnouncements.isEmpty()) return
        if (targetLanguage == lastTranslatedLanguage) return

        translationJob?.cancel()
        translationJob = viewModelScope.launch {
            isTranslating = true
            val titles = originalAnnouncements.map { it.title }
            val descriptions = originalAnnouncements.map { it.description }

            val translatedTitles = translationRepository.translateBatch(titles, targetLanguage)
            val translatedDescriptions =
                translationRepository.translateBatch(descriptions, targetLanguage)

            val translated = originalAnnouncements.mapIndexed { index, announcement ->
                announcement.copy(
                    title = translatedTitles.getOrElse(index) { announcement.title },
                    description = translatedDescriptions.getOrElse(index) { announcement.description }
                )
            }
            announcements = translated
            lastTranslatedLanguage = targetLanguage
            isTranslating = false
        }
    }

    private fun isInDateRange(a: Announcement, today: LocalDate): Boolean {
        val start = runCatching { LocalDate.parse(a.startDate) }.getOrNull() ?: return false
        val end = runCatching { LocalDate.parse(a.endDate) }.getOrNull() ?: return false
        return !today.isBefore(start) && today.isBefore(end)
    }

    override fun onCleared() {
        super.onCleared()
        announcementsListener?.remove()
    }
}