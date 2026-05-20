package com.notdefteri.app

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NoteAdapter(private val onNoteClick: (Note) -> Unit) :
    ListAdapter<Note, NoteAdapter.NoteViewHolder>(NoteDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = getItem(position)
        holder.bind(note)
        holder.itemView.setOnClickListener { onNoteClick(note) }
    }

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleText: TextView = itemView.findViewById(R.id.noteTitle)
        private val contentText: TextView = itemView.findViewById(R.id.noteContent)
        private val dateText: TextView = itemView.findViewById(R.id.noteDate)
        private val thumbImage: ImageView = itemView.findViewById(R.id.noteThumb)

        fun bind(note: Note) {
            titleText.text = note.title.ifEmpty { "Başlıksız Not" }
            val preview = note.content.take(120)
            contentText.text = preview.ifEmpty { "" }
            contentText.visibility = if (preview.isEmpty()) View.GONE else View.VISIBLE

            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("tr"))
            dateText.text = sdf.format(Date(note.createdAt))

            if (!note.drawingPath.isNullOrEmpty()) {
                val file = File(note.drawingPath)
                if (file.exists()) {
                    val bmp = BitmapFactory.decodeFile(note.drawingPath)
                    thumbImage.setImageBitmap(bmp)
                    thumbImage.visibility = View.VISIBLE
                } else {
                    thumbImage.visibility = View.GONE
                }
            } else {
                thumbImage.visibility = View.GONE
            }
        }
    }

    class NoteDiffCallback : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean =
            oldItem == newItem
    }
}
