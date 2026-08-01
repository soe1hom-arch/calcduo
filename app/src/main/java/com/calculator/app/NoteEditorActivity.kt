package com.calculator.app

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.calculator.app.data.Note
import com.calculator.app.data.NotesStore
import com.calculator.app.databinding.ActivityNoteEditorBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

class NoteEditorActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_NOTE_ID = "extra_note_id"
        private const val DEBOUNCE_MS = 300L
    }

    private lateinit var binding: ActivityNoteEditorBinding
    private var note: Note? = null
    private var saveJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeUtils.apply(this)
        binding = ActivityNoteEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_delete_note) {
                confirmDelete()
                true
            } else {
                false
            }
        }

        val noteId = intent.getStringExtra(EXTRA_NOTE_ID)
        val stored = NotesStore.load(this)
        note = stored.firstOrNull { it.id == noteId } ?: Note(
            id = noteId ?: UUID.randomUUID().toString(),
            title = "",
            content = "",
            updatedAt = System.currentTimeMillis()
        )

        binding.etNoteTitle.setText(note?.title.orEmpty())
        binding.etNoteContent.setText(note?.content.orEmpty())
        updateCount(note?.content?.length ?: 0)

        binding.etNoteTitle.addTextChangedListener(onFieldChanged { title ->
            note = note?.copy(title = title, updatedAt = System.currentTimeMillis())
            scheduleSave()
        })
        binding.etNoteContent.addTextChangedListener(onFieldChanged { content ->
            note = note?.copy(content = content, updatedAt = System.currentTimeMillis())
            updateCount(content.length)
            scheduleSave()
        })
    }

    override fun onPause() {
        super.onPause()
        saveJob?.cancel()
        persist()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun onFieldChanged(onChange: (String) -> Unit) =
        object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                onChange(s?.toString().orEmpty())
            }
        }

    private fun scheduleSave() {
        saveJob?.cancel()
        saveJob = lifecycleScope.launch {
            delay(DEBOUNCE_MS)
            persist()
        }
    }

    private fun persist() {
        val current = note ?: return
        val notes = NotesStore.load(this)
        val isEmpty = current.title.isBlank() && current.content.isBlank()
        val idx = notes.indexOfFirst { it.id == current.id }
        if (isEmpty) {
            if (idx >= 0) {
                notes.removeAt(idx)
                NotesStore.save(this, notes)
            }
            return
        }
        val saved = current.copy(updatedAt = System.currentTimeMillis())
        if (idx >= 0) notes[idx] = saved else notes.add(saved)
        NotesStore.save(this, notes)
    }

    private fun updateCount(count: Int) {
        binding.tvNoteCount.text = resources.getQuantityString(R.plurals.notes_chars, count, count)
    }

    private fun confirmDelete() {
        AlertDialog.Builder(this)
            .setTitle(R.string.notes_delete)
            .setMessage(R.string.notes_delete_message)
            .setPositiveButton(R.string.notes_delete) { _, _ ->
                val notes = NotesStore.load(this).filterNot { it.id == note?.id }
                NotesStore.save(this, notes)
                finish()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
