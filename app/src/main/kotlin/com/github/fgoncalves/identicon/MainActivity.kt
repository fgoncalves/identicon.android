package com.github.fgoncalves.identicon

import android.content.Intent
import android.databinding.DataBindingUtil
import android.databinding.Observable
import android.os.Bundle
import android.support.v4.view.MenuItemCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.ShareActionProvider
import android.view.Menu
import com.github.fgoncalves.identicon.databinding.ActivityMainBinding
import com.github.fgoncalves.identicon.lib.IdenticonImpl
import kotlinx.android.synthetic.main.activity_main.*


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class MainActivity : AppCompatActivity() {
    private lateinit var shareActionProvider: ShareActionProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val viewModel = MainViewModelImpl(this, IdenticonImpl())

        binding.viewModel = viewModel

        viewModel.shareIntent.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(observable: Observable?, propertyId: Int) {
                setShareIntent(viewModel.shareIntent.get())
            }
        })

        setSupportActionBar(toolbar)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)

        val item = menu.findItem(R.id.menu_item_share)
        shareActionProvider = MenuItemCompat.getActionProvider(item) as ShareActionProvider

        return true
    }

    private fun setShareIntent(shareIntent: Intent?) {
        shareActionProvider.setShareIntent(shareIntent)
    }
}
