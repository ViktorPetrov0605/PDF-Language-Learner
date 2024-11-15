package com.example.pdflearner

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.parser.PdfTextExtractor
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    lateinit var extractedTV: TextView
    lateinit var extractBtn: Button
    lateinit var cardView: CardView
    lateinit var cardTextView: TextView
    lateinit var newWordBtn: Button
    lateinit var userInput: EditText
    lateinit var submitAnswerBtn: Button
    lateinit var feedbackTV: TextView
    private var selectedPdfUri: Uri? = null

    private val wordTranslationPairs = mutableListOf<Pair<String, String>>()
    private var showingTranslation = false

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
        userInput = findViewById(R.id.idETUserInput)
        submitAnswerBtn = findViewById(R.id.idBtnSubmitAnswer)
        feedbackTV = findViewById(R.id.idTVFeedback)

        // Initially hide all buttons and inputs except "Extract Text from PDF"
        newWordBtn.visibility = View.GONE
        cardView.visibility = View.GONE
        userInput.visibility = View.GONE
        submitAnswerBtn.visibility = View.GONE
        feedbackTV.visibility = View.GONE

        extractBtn.setOnClickListener {
            openFilePicker()
        }

        cardView.setOnClickListener {
            toggleTranslation()
        }

        newWordBtn.setOnClickListener {
            showRandomWord()
        }

        submitAnswerBtn.setOnClickListener {
            checkAnswer()
        }
    }

    private fun openFilePicker() {
        filePickerLauncher.launch(arrayOf("application/pdf"))
    }

    private fun extractData() {
        selectedPdfUri?.let { pdfUri ->
            try {
                wordTranslationPairs.clear()
                contentResolver.openInputStream(pdfUri)?.use { inputStream ->
                    val pdfReader = PdfReader(inputStream)
                    val pageCount = pdfReader.numberOfPages

                    for (i in 0 until pageCount) {
                        val pageText = PdfTextExtractor.getTextFromPage(pdfReader, i + 1).trim()

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

                if (wordTranslationPairs.isNotEmpty()) {
                    // Show all relevant UI elements when data is available
                    newWordBtn.visibility = View.VISIBLE
                    cardView.visibility = View.VISIBLE
                    userInput.visibility = View.VISIBLE
                    submitAnswerBtn.visibility = View.VISIBLE
                    feedbackTV.visibility = View.GONE
                    showRandomWord()
                } else {
                    cardTextView.text = "No valid entries found."
                    feedbackTV.text = ""
                }
            } catch (e: Exception) {
                e.printStackTrace()
                cardTextView.text = "Failed to extract text. Try a different file."
            }
        }
    }

    private fun showRandomWord() {
        if (wordTranslationPairs.isNotEmpty()) {
            val randomPair = wordTranslationPairs[Random.nextInt(wordTranslationPairs.size)]
            cardTextView.text = randomPair.first
            showingTranslation = false
            cardTextView.tag = randomPair

            // Reset user input and feedback
            userInput.text.clear()
            feedbackTV.visibility = View.GONE
        } else {
            cardTextView.text = "No words available. Please load a PDF with entries."
        }
    }

    private fun toggleTranslation() {
        val currentPair = cardTextView.tag as? Pair<String, String> ?: return

        cardTextView.animate().alpha(0f).setDuration(200).withEndAction {
            cardTextView.text = if (showingTranslation) currentPair.first else currentPair.second
            showingTranslation = !showingTranslation
            cardTextView.animate().alpha(1f).setDuration(200).start()
        }.start()
    }

    private fun checkAnswer() {
        val currentPair = cardTextView.tag as? Pair<String, String> ?: return
        val userAnswer = userInput.text.toString().trim()

        if (userAnswer.equals(currentPair.second, ignoreCase = true)) {
            feedbackTV.text = "Right!"
            feedbackTV.setTextColor(getColor(R.color.green))
        } else {
            feedbackTV.text = "Wrong!"
            feedbackTV.setTextColor(getColor(R.color.red))
        }
        feedbackTV.visibility = View.VISIBLE

        // Show a new word after a short delay
        cardView.postDelayed({
            showRandomWord()
        }, 1500)
    }
}
