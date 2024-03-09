package com.example.domasnazadaca1

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TableLayout
import android.widget.TableRow
import androidx.compose.runtime.Composable
import java.util.*

class FavoriteTwitterSearches : Activity() {
    private var savedSearches: SharedPreferences? = null
    private var queryTableLayout: TableLayout? = null
    private var queryEditText: EditText? = null
    private var tagEditText: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tablelayout) // Corrected layout file name
        savedSearches = getSharedPreferences("searches", Context.MODE_PRIVATE)
        queryTableLayout = findViewById(R.id.TableLayout) // Corrected TableLayout ID
        queryEditText = findViewById(R.id.queryEditText)
        tagEditText = findViewById(R.id.tagEditText)

        // Add the onFocusChangeListener to queryEditText
        queryEditText?.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                queryEditText?.setBackgroundResource(R.color.light_orange)
            } else {
                queryEditText?.setBackgroundResource(android.R.color.white)
            }
        }

        val saveButton = findViewById<Button>(R.id.saveButton)
        saveButton.setOnClickListener(saveButtonListener)
        val clearTagsButton = findViewById<Button>(R.id.clearTagsButton)
        clearTagsButton.setOnClickListener(clearTagsButtonListener)
        refreshButtons(null)
    }

    private val saveButtonListener = View.OnClickListener {
        if (queryEditText?.text?.isNotEmpty() == true && tagEditText?.text?.isNotEmpty() == true) {
            makeTag(queryEditText?.text.toString(), tagEditText?.text.toString())
            queryEditText?.setText("")
            tagEditText?.setText("")
            (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(tagEditText?.windowToken, 0)
        } else {
            val builder = AlertDialog.Builder(this@FavoriteTwitterSearches)
            builder.setTitle(R.string.missingTitle)
                .setMessage(R.string.missingMessage)
                .setPositiveButton(R.string.OK, null)
            val errorDialog: AlertDialog = builder.create()
            errorDialog.show()
        }
    }

    private val clearTagsButtonListener = View.OnClickListener {
        val builder = AlertDialog.Builder(this@FavoriteTwitterSearches)
        builder.setTitle(R.string.confirmTitle)
            .setPositiveButton(R.string.erase) { dialog, _ ->
                clearButtons()
                val preferencesEditor = savedSearches?.edit()
                preferencesEditor?.clear()
                preferencesEditor?.apply()
            }
            .setNegativeButton(R.string.cancel, null)
            .setMessage(R.string.confirmMessage)
        val confirmDialog: AlertDialog = builder.create()
        confirmDialog.show()
    }

    private val queryButtonListener = View.OnClickListener { v ->
        val buttonText = (v as Button).text.toString()
        val query = savedSearches?.getString(buttonText, null)
        val urlString = getString(R.string.searchURL) + query
        val getURL = Intent(Intent.ACTION_VIEW, Uri.parse(urlString))
        startActivity(getURL)
    }

    private val editButtonListener = View.OnClickListener { v ->
        val buttonTableRow = v.parent as TableRow
        val searchButton = buttonTableRow.findViewById<Button>(R.id.newTagButton)
        val editButton = buttonTableRow.findViewById<Button>(R.id.editTagButton)
        editButton.text = getString(R.string.editButtonText)
        val tag = searchButton.text.toString()
        tagEditText?.setText(tag)
        queryEditText?.setText(savedSearches?.getString(tag, null))
    }

    private fun refreshButtons(newTag: String?) {
        val tags = savedSearches?.all?.keys?.toTypedArray() ?: arrayOfNulls(0)
        tags.sortWith(compareBy { it?.lowercase() })

        if (newTag != null) {
            makeTagGUI(newTag, Arrays.binarySearch(tags, newTag))
        } else {
            for (index in tags.indices) {
                makeTagGUI(tags[index]!!, index)
            }
        }
    }

    private fun makeTag(query: String?, tag: String?) {
        val originalQuery = savedSearches?.getString(tag, null)
        val preferencesEditor = savedSearches?.edit()
        preferencesEditor?.putString(tag, query)
        preferencesEditor?.apply()
        if (originalQuery == null) {
            refreshButtons(tag)
        }
    }

    private fun makeTagGUI(tag: String?, index: Int) {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val newTagView = inflater.inflate(R.layout.new_tag_view, queryTableLayout, false)

        val newTagButton = newTagView.findViewById<Button>(R.id.newTagButton)
        newTagButton.text = tag
        newTagButton.setOnClickListener(queryButtonListener)

        val newEditButton = newTagView.findViewById<Button>(R.id.editTagButton) // Corrected button ID
        newEditButton.setOnClickListener(editButtonListener)

        queryTableLayout?.addView(newTagView, index)
    }

    private fun clearButtons() {
        queryTableLayout?.removeAllViews()
    }
}
