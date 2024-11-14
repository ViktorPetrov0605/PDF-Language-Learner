package com.example.pdflearner

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.parser.PdfTextExtractor
import kotlin.random.Random

import java.util.logging.Logger;

class MainActivity : AppCompatActivity() {

    lateinit var extractedTV: TextView
    lateinit var extractBtn: Button
    lateinit var cardView: CardView
    lateinit var cardTextView: TextView
    lateinit var newWordBtn: Button
    private var selectedPdfUri: Uri? = null

    // List to hold word-translation pairs
    private val wordTranslationPairs = mutableListOf<Pair<String, String>>()
    private var showingTranslation = false // Track if translation is shown on the card

    // Register file picker launcher
    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            selectedPdfUri = it
            extractData()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        extractedTV = findViewById(R.id.idTVPDF)
        extractBtn = findViewById(R.id.idBtnExtract)
        cardView = findViewById(R.id.idCardView)
        cardTextView = findViewById(R.id.idTVCardWord)
        newWordBtn = findViewById(R.id.idBtnNewWord)

        extractBtn.setOnClickListener {
            openFilePicker()
        }

        cardView.setOnClickListener {
            toggleTranslation()
        }
        val Log = Logger.getLogger(MainActivity::class.java.name)
        newWordBtn.setOnClickListener {
            Log.warning("New Word button clicked")
            showRandomWord() // Show a new random word when this button is clicked
        }
    }

    private fun openFilePicker() {
        filePickerLauncher.launch(arrayOf("application/pdf"))
    }

    private fun extractData() {
        selectedPdfUri?.let { pdfUri ->
            try {
                wordTranslationPairs.clear() // Clear previous entries
                contentResolver.openInputStream(pdfUri)?.use { inputStream ->
                    val pdfReader = PdfReader(inputStream)
                    val pageCount = pdfReader.numberOfPages

                    for (i in 0 until pageCount) {
                        val pageText = PdfTextExtractor.getTextFromPage(pdfReader, i + 1).trim()

                        // Filter lines with " - " and split them
                        pageText.lines().forEach { line ->
                            if (" - " in line) {
                                val parts = line.split(" - ", limit = 2)
                                if (parts.size == 2) {
                                    wordTranslationPairs.add(Pair(parts[0].trim(), parts[1].trim()))
                                }
                            }
                        }
                    }

                    pdfReader.close()
                }

                // Show an initial random word on the card if there are pairs available
                if (wordTranslationPairs.isNotEmpty()) {
                    showRandomWord()
                } else {
                    cardTextView.text = "No valid entries found."
                }
            } catch (e: Exception) {
                e.printStackTrace()
                cardTextView.text = "Failed to extract text. Try a different file."
            }
        }
    }

    private fun showRandomWord() {
        if (wordTranslationPairs.isNotEmpty()) {
            // Pick a random word-translation pair
            val randomPair = wordTranslationPairs[Random.nextInt(wordTranslationPairs.size)]
            cardTextView.text = randomPair.first
            showingTranslation = false
            cardTextView.tag = randomPair // Store the pair in the tag for easy retrieval
        } else {
            cardTextView.text = "No words available. Please load a PDF with entries."
        }
    }

    private fun toggleTranslation() {
        val currentPair = cardTextView.tag as? Pair<String, String> ?: return

        // Toggle between word and translation with a fade effect
        cardTextView.animate().alpha(0f).setDuration(200).withEndAction {
            cardTextView.text = if (showingTranslation) currentPair.first else currentPair.second
            showingTranslation = !showingTranslation
            cardTextView.animate().alpha(1f).setDuration(200).start()
        }.start()
    }
}
