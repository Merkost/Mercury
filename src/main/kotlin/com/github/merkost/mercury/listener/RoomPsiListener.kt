package com.github.merkost.mercury.listener

import com.github.merkost.mercury.services.MercuryProjectService
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiTreeChangeAdapter
import com.intellij.psi.PsiTreeChangeEvent
import com.intellij.util.Alarm

class RoomPsiListener(
    private val project: Project,
    private val service: MercuryProjectService
) : PsiTreeChangeAdapter() {

    private val alarm = Alarm(Alarm.ThreadToUse.POOLED_THREAD, service)

    fun register(parentDisposable: Disposable) {
        PsiManager.getInstance(project).addPsiTreeChangeListener(this, parentDisposable)
    }

    override fun childrenChanged(event: PsiTreeChangeEvent) {
        scheduleIfRoomRelated(event)
    }

    override fun childAdded(event: PsiTreeChangeEvent) {
        scheduleIfRoomRelated(event)
    }

    override fun childRemoved(event: PsiTreeChangeEvent) {
        scheduleIfRoomRelated(event)
    }

    override fun childReplaced(event: PsiTreeChangeEvent) {
        scheduleIfRoomRelated(event)
    }

    private fun scheduleIfRoomRelated(event: PsiTreeChangeEvent) {
        val file = event.file ?: return
        val fileName = file.name
        if (!fileName.endsWith(".kt") && !fileName.endsWith(".java")) return

        if (!isRoomRelatedFile(file.text)) return

        alarm.cancelAllRequests()
        alarm.addRequest({ service.refreshSchema() }, DEBOUNCE_MS)
    }

    companion object {
        private const val DEBOUNCE_MS = 500

        fun isRoomRelatedFile(text: String): Boolean =
            text.contains("androidx.room") ||
                text.contains("@Database") ||
                text.contains("@Entity") ||
                text.contains("@Dao")
    }
}
