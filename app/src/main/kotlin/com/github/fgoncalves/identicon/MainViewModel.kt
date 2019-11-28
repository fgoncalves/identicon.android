package com.github.fgoncalves.identicon

import android.content.Context
import android.content.Intent
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import com.github.fgoncalves.androididenticon.identicon.Identicon
import io.reactivex.android.schedulers.AndroidSchedulers


interface MainViewModel {
    val progressVisibility: ObservableInt

    val imageVisibility: ObservableInt

    val imageBitmap: ObservableField<Bitmap?>

    val text: ObservableField<String>

    val generateButtonEnabled: ObservableBoolean

    val shareIntent: ObservableField<Intent?>

    val editorActionListener: TextView.OnEditorActionListener

    fun onGenerateButtonClicked(view: View)
}

class MainViewModelImpl(
        val context: Context,
        val identicon: Identicon) : MainViewModel {
    override val progressVisibility = ObservableInt(View.GONE)
    override val imageVisibility = ObservableInt(View.VISIBLE)
    override val imageBitmap = ObservableField<Bitmap?>()
    override val text = ObservableField("")
    override val generateButtonEnabled = ObservableBoolean(true)
    override val shareIntent = ObservableField<Intent?>()
    override val editorActionListener = TextView.OnEditorActionListener { textView, actionId, _ ->
        when (actionId) {
            EditorInfo.IME_ACTION_DONE -> {
                KeyboardUtils.hideKeyboard(textView)
                generateIdenticon()
                true
            }
            else -> false
        }
    }

    override fun onGenerateButtonClicked(view: View) {
        generateIdenticon()
    }

    private fun generateIdenticon() {
        generateState()
        identicon.generate(text.get() ?: "")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            normalState()
                            imageBitmap.set(it)
                            setShareIntent(it)
                        },
                        {
                            normalState()
                            imageBitmap.set(null)
                            Log.e("MainViewModelImpl", "Failed to generate bitmap", it)
                        })
    }

    private fun setShareIntent(bitmap: Bitmap) {
        val bitmapPath = PhotoUtils.insertImage(context.contentResolver, bitmap, "${text.get()}.png", "Identicon for ${text.get()}")
        val bitmapUri = Uri.parse(bitmapPath)
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "image/png"
        intent.putExtra(Intent.EXTRA_STREAM, bitmapUri)
        shareIntent.set(intent)
    }

    private fun generateState() {
        imageVisibility.set(View.GONE)
        progressVisibility.set(View.VISIBLE)
        generateButtonEnabled.set(false)
    }

    private fun normalState() {
        imageVisibility.set(View.VISIBLE)
        progressVisibility.set(View.GONE)
        generateButtonEnabled.set(true)
    }
}
