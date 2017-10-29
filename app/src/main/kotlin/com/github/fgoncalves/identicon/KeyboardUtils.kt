package com.github.fgoncalves.identicon

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager


object KeyboardUtils {
    fun hideKeyboard(view: View) {
        val context = view.context
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
