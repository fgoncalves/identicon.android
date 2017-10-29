package com.github.fgoncalves.identicon

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.github.fgoncalves.identicon.databinding.ActivityMainBinding
import com.github.fgoncalves.identicon.lib.IdenticonImpl

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val viewModel = MainViewModelImpl(IdenticonImpl())

        binding.viewModel = viewModel
    }
}
