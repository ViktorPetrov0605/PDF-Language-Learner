package com.example.webviewtest

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.parser.PdfTextExtractor
import java.io.InputStream

class MainActivity : AppCompatActivity() {

    lateinit var extractedTV: TextView
    lateinit var extractBtn: Button
    private var selectedPdfUri: Uri? = null

    // Register a launcher to handle the PDF file selection
    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            selectedPdfUri = it
            extractData() // Extract data once the user selects a file
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        extractedTV = findViewById(R.id.idTVPDF)
        extractBtn = findViewById(R.id.idBtnExtract)

        extractBtn.setOnClickListener {
            openFilePicker() // Open file picker when button is clicked
        }
    }

    private fun openFilePicker() {
        // Open the file picker for PDFs
        filePickerLauncher.launch(arrayOf("application/pdf"))
    }

    private fun extractData() {
        // Ensure a file was selected
        val pdfUri = selectedPdfUri ?: return

        try {
            var extractedText = ""

            // Open an InputStream to read the PDF from the selected URI
            contentResolver.openInputStream(pdfUri)?.use { inputStream ->
                val pdfReader = PdfReader(inputStream)

                val pageCount = pdfReader.numberOfPages
                for (i in 0 until pageCount) {
                    val pageText = PdfTextExtractor.getTextFromPage(pdfReader, i + 1).trim()

                    // Filter lines that contain " - " pattern
                    val filteredLines = pageText.lines().filter { line -> " - " in line }

                    // Append only filtered lines to extractedText
                    extractedText += filteredLines.joinToString("\n") + "\n"
                }

                pdfReader.close()
            }

            extractedTV.text = extractedText // Display the filtered text
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
