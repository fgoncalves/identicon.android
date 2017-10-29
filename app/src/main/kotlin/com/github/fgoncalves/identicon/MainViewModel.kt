package com.github.fgoncalves.identicon

import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.graphics.Bitmap
import android.util.Log
import android.view.View
import com.github.fgoncalves.identicon.lib.Identicon
import io.reactivex.android.schedulers.AndroidSchedulers


interface MainViewModel {
    val progressVisibility: ObservableInt

    val imageVisibility: ObservableInt

    val imageBitmap: ObservableField<Bitmap>

    val text: ObservableField<String>

    val generateButtonEnabled: ObservableBoolean

    fun onGenerateButtonClicked(view: View)
}

class MainViewModelImpl(
        val identicon: Identicon) : MainViewModel {
    override val progressVisibility = ObservableInt(View.GONE)
    override val imageVisibility = ObservableInt(View.VISIBLE)
    override val imageBitmap = ObservableField<Bitmap>(null)
    override val text = ObservableField<String>("")
    override val generateButtonEnabled = ObservableBoolean(true)

    override fun onGenerateButtonClicked(view: View) {
        generateState()
        identicon.generate(text.get())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            normalState()
                            imageBitmap.set(it)
                        },
                        {
                            normalState()
                            imageBitmap.set(null)
                            Log.e("MainViewModelImpl", "Failed to generate bitmap", it)
                        })
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
