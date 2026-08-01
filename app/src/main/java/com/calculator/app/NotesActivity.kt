package com.calculator.app

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.calculator.app.data.Note
import com.calculator.app.data.NotesStore
import com.calculator.app.databinding.ActivityNotesBinding
import com.calculator.app.databinding.ItemNoteBinding
import java.text.DateFormat

class NotesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotesBinding
    private val notes = mutableListOf<Note>()
    private val adapter = NoteAdapter { note -> openEditor(note.id) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeUtils.apply(this)
        binding = ActivityNotesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.rvNotes.layoutManager = LinearLayoutManager(this)
        binding.rvNotes.adapter = adapter
        binding.fabAddNote.setOnClickListener { openEditor(null) }
    }

    override fun onResume() {
        super.onResume()
        notes.clear()
        notes.addAll(NotesStore.load(this))
        adapter.submit(notes)
        binding.tvNotesEmpty.visibility = if (notes.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun openEditor(noteId: String?) {
        val intent = Intent(this, NoteEditorActivity::class.java)
        if (noteId != null) intent.putExtra(NoteEditorActivity.EXTRA_NOTE_ID, noteId)
        startActivity(intent)
    }

    private class NoteAdapter(
        private val onItemClick: (Note) -> Unit
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
                b.tvNoteTitle.text = note.title.ifBlank {
                    b.root.context.getString(R.string.notes_untitled)
                }
                val snippet = note.content.replace('\n', ' ').trim()
                if (snippet.isEmpty()) {
                    b.tvNoteSnippet.visibility = View.GONE
                } else {
                    b.tvNoteSnippet.visibility = View.VISIBLE
                    b.tvNoteSnippet.text = snippet
                }
                b.tvNoteDate.text = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                    .format(note.updatedAt)
                b.root.setOnClickListener { onItemClick(note) }
            }
        }
    }
}
