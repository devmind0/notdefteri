package com.notdefteri.app

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class AddEditNoteActivity : AppCompatActivity() {

    private lateinit var viewModel: NoteViewModel
    private var noteId: Long = -1L
    private var existingDrawingPath: String? = null
    private var drawingModified = false

    private lateinit var titleEditText: EditText
    private lateinit var contentEditText: EditText
    private lateinit var drawingView: DrawingView
    private lateinit var colorPickerView: ColorPickerView
    private lateinit var seekBar: SeekBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_note)

        viewModel = NoteViewModel(application)

        val toolbar: MaterialToolbar = findViewById(R.id.toolbarAddEdit)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        titleEditText = findViewById(R.id.editTitle)
        contentEditText = findViewById(R.id.editContent)
        drawingView = findViewById(R.id.drawingView)
        colorPickerView = findViewById(R.id.colorPicker)
        seekBar = findViewById(R.id.strokeSeekBar)
        val undoBtn: Button = findViewById(R.id.btnUndo)
        val clearBtn: Button = findViewById(R.id.btnClear)
        val saveBtn: Button = findViewById(R.id.btnSave)

        colorPickerView.onColorSelected = { color ->
            drawingView.strokeColor = color
            drawingModified = true
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val width = (progress + 2).toFloat()
                drawingView.strokeWidth = width
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        undoBtn.setOnClickListener {
            drawingView.undo()
            drawingModified = true
        }

        clearBtn.setOnClickListener {
            drawingView.clearCanvas()
            drawingModified = true
        }

        saveBtn.setOnClickListener { saveNote() }

        noteId = intent.getLongExtra("NOTE_ID", -1L)
        if (noteId != -1L) {
            supportActionBar?.title = "Notu Düzenle"
            loadNote(noteId)
        } else {
            supportActionBar?.title = "Yeni Not"
        }
    }

    private fun loadNote(id: Long) {
        lifecycleScope.launch {
            val note = viewModel.getNoteByIdSync(id)
            if (note != null) {
                titleEditText.setText(note.title)
                contentEditText.setText(note.content)
                existingDrawingPath = note.drawingPath
                if (!note.drawingPath.isNullOrEmpty()) {
                    val file = File(note.drawingPath)
                    if (file.exists()) {
                        val bmp = BitmapFactory.decodeFile(note.drawingPath)
                        bmp?.let { drawingView.loadFromBitmap(it) }
                    }
                }
            }
        }
    }

    private fun saveNote() {
        val title = titleEditText.text.toString().trim()
        val content = contentEditText.text.toString().trim()

        if (title.isEmpty() && content.isEmpty() && drawingView.isEmpty()) {
            Toast.makeText(this, "Başlık, içerik veya çizim girin", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val drawingPath = saveDrawingToFile()

            if (noteId == -1L) {
                val note = Note(
                    title = title,
                    content = content,
                    drawingPath = drawingPath,
                    createdAt = System.currentTimeMillis()
                )
                viewModel.insertNote(note) {}
                Toast.makeText(this@AddEditNoteActivity, "Not kaydedildi", Toast.LENGTH_SHORT).show()
            } else {
                val note = Note(
                    id = noteId,
                    title = title,
                    content = content,
                    drawingPath = drawingPath,
                    createdAt = System.currentTimeMillis()
                )
                viewModel.updateNote(note)
                Toast.makeText(this@AddEditNoteActivity, "Not güncellendi", Toast.LENGTH_SHORT).show()
            }
            finish()
        }
    }

    private suspend fun saveDrawingToFile(): String? {
        return withContext(Dispatchers.IO) {
            if (drawingView.isEmpty()) {
                existingDrawingPath?.let {
                    File(it).let { f -> if (f.exists()) f.delete() }
                }
                return@withContext null
            }

            if (!drawingModified && existingDrawingPath != null) {
                return@withContext existingDrawingPath
            }

            try {
                existingDrawingPath?.let {
                    val oldFile = File(it)
                    if (oldFile.exists()) oldFile.delete()
                }

                val bitmap = drawingView.saveToBitmap()
                val dir = File(filesDir, "drawings")
                if (!dir.exists()) dir.mkdirs()
                val file = File(dir, "drawing_${System.currentTimeMillis()}.png")
                FileOutputStream(file).use { fos ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                    fos.flush()
                }
                file.absolutePath
            } catch (e: Exception) {
                null
            }
        }
    }
}
