package com.example.webviewtest

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.parser.PdfTextExtractor

class MainActivity : AppCompatActivity() {

    // on below line we are creating
    // variable for our button and text view.
    lateinit var extractedTV: TextView
    lateinit var extractBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // on below line we are initializing our
        // text view and button with its id.
        extractedTV = findViewById(R.id.idTVPDF)
        extractBtn = findViewById(R.id.idBtnExtract)

        // on below line we are adding on
        // click listener for our button.
        extractBtn.setOnClickListener {
            // on below line we are calling extract data method
            // to extract data from our pdf file and
            // display it in text view.
            extractData()
        }
    }

    // on below line we are creating an
    // extract data method to extract our data.
    private fun extractData() {
        // on below line we are running a try and catch block
        // to handle extract data operation.
        try {
            // on below line we are creating a
            // variable for storing our extracted text
            var extractedText = ""

            // on below line we are creating a
            // variable for our pdf extracter.
            val pdfReader: PdfReader = PdfReader("res/raw/test.pdf")

            // on below line we are creating
            // a variable for pages of our pdf.
            val n = pdfReader.numberOfPages

            // on below line we are running a for loop.
            for (i in 0 until n) {

                // on below line we are appending
                // our data to extracted
                // text from our pdf file using pdf reader.
                extractedText =
                    """ 
                 $extractedText${
                        PdfTextExtractor.getTextFromPage(pdfReader, i + 1).trim { it <= ' ' }
                    } 
                  
                 """.trimIndent()
                // to extract the PDF content from the different pages
            }

            // on below line we are setting
            // extracted text to our text view.
            extractedTV.setText(extractedText)

            // on below line we are
            // closing our pdf reader.
            pdfReader.close()

        }
        // on below line we are handling our
        // exception using catch block
        catch (e: Exception) {
            e.printStackTrace()
        }
    }
}