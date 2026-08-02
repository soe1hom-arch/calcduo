/*
 * Copyright 2026 soe1hom-arch
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.soe1hom.calcduo

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.soe1hom.calcduo.data.Note
import com.soe1hom.calcduo.data.NotesStore
import com.soe1hom.calcduo.databinding.ActivityNotesBinding
import com.soe1hom.calcduo.databinding.ItemNoteBinding
import com.google.android.material.card.MaterialCardView
import java.text.DateFormat

class NotesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotesBinding
    private val notes = mutableListOf<Note>()
    private var selectionMode = false
    private val selectedIds = mutableSetOf<String>()

    private val adapter = NoteAdapter(
        onItemClick = { note -> onNoteClick(note) },
        onItemLongClick = { note -> onNoteLongClick(note) },
        selectionMode = { selectionMode },
        selectedIds = selectedIds
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeUtils.apply(this)
        binding = ActivityNotesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ThemeUtils.applySystemBarInsets(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.rvNotes.layoutManager = LinearLayoutManager(this)
        binding.rvNotes.adapter = adapter
        binding.fabAddNote.setOnClickListener { openEditor(null) }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (selectionMode) exitSelectionMode() else finish()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        exitSelectionMode()
        refreshList()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.notes_menu, menu)
        updateSelectionUi()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_select_notes -> { enterSelectionMode(); true }
            R.id.action_done_select -> { exitSelectionMode(); true }
            R.id.action_delete_selected -> { confirmDeleteSelected(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun refreshList() {
        notes.clear()
        notes.addAll(NotesStore.load(this))
        adapter.submit(notes)
        binding.tvNotesEmpty.visibility = if (notes.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun openEditor(noteId: String?) {
        val intent = Intent(this, NoteEditorActivity::class.java)
        if (noteId != null) intent.putExtra(NoteEditorActivity.EXTRA_NOTE_ID, noteId)
        startActivity(intent)
    }

    private fun onNoteClick(note: Note) {
        if (selectionMode) toggleSelection(note) else openEditor(note.id)
    }

    private fun onNoteLongClick(note: Note) {
        if (!selectionMode) enterSelectionMode()
        toggleSelection(note)
    }

    private fun enterSelectionMode() {
        selectionMode = true
        updateSelectionUi()
    }

    private fun exitSelectionMode() {
        selectionMode = false
        selectedIds.clear()
        updateSelectionUi()
        adapter.notifyDataSetChanged()
    }

    private fun toggleSelection(note: Note) {
        if (selectedIds.contains(note.id)) selectedIds.remove(note.id) else selectedIds.add(note.id)
        adapter.notifyDataSetChanged()
        updateSelectionUi()
    }

    private fun updateSelectionUi() {
        val title = if (selectionMode) {
            resources.getQuantityString(R.plurals.notes_selected_count, selectedIds.size, selectedIds.size)
        } else {
            getString(R.string.notes_title)
        }
        supportActionBar?.title = title
        supportActionBar?.setDisplayHomeAsUpEnabled(!selectionMode)
        binding.toolbar.menu.findItem(R.id.action_select_notes)?.isVisible = !selectionMode
        binding.toolbar.menu.findItem(R.id.action_done_select)?.isVisible = selectionMode
        binding.toolbar.menu.findItem(R.id.action_delete_selected)?.isVisible = selectionMode
        binding.toolbar.menu.findItem(R.id.action_delete_selected)?.isEnabled = selectedIds.isNotEmpty()
    }

    private fun confirmDeleteSelected() {
        if (selectedIds.isEmpty()) return
        AlertDialog.Builder(this)
            .setTitle(R.string.notes_delete)
            .setMessage(R.string.notes_delete_selected_message)
            .setPositiveButton(R.string.notes_delete) { _, _ ->
                val remaining = NotesStore.load(this).filterNot { it.id in selectedIds }
                NotesStore.save(this, remaining)
                exitSelectionMode()
                refreshList()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private class NoteAdapter(
        private val onItemClick: (Note) -> Unit,
        private val onItemLongClick: (Note) -> Unit,
        private val selectionMode: () -> Boolean,
        private val selectedIds: Set<String>
    ) : RecyclerView.Adapter<NoteAdapter.VH>() {

        private val items = mutableListOf<Note>()

        fun submit(list: List<Note>) {
            items.clear()
            items.addAll(list)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val b = ItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return VH(b)
        }

        override fun getItemCount(): Int = items.size

        override fun onBindViewHolder(holder: VH, position: Int) {
            holder.bind(items[position])
        }

        inner class VH(private val b: ItemNoteBinding) : RecyclerView.ViewHolder(b.root) {
            fun bind(note: Note) {
                val ctx = b.root.context
                b.tvNoteTitle.text = note.title.ifBlank { ctx.getString(R.string.notes_untitled) }
                val snippet = note.content.replace('\n', ' ').trim()
                if (snippet.isEmpty()) {
                    b.tvNoteSnippet.visibility = View.GONE
                } else {
                    b.tvNoteSnippet.visibility = View.VISIBLE
                    b.tvNoteSnippet.text = snippet
                }
                b.tvNoteDate.text = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                    .format(note.updatedAt)

                val selected = selectedIds.contains(note.id)
                b.tvNoteCheck.visibility = if (selected) View.VISIBLE else View.GONE
                val card = b.root as MaterialCardView
                if (selected) {
                    val density = ctx.resources.displayMetrics.density
                    card.strokeWidth = (2 * density).toInt()
                } else {
                    card.strokeWidth = 0
                }

                b.root.setOnClickListener { onItemClick(note) }
                b.root.setOnLongClickListener { onItemLongClick(note); true }
            }
        }
    }
}
