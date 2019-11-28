package com.github.fgoncalves.identicon

import android.Manifest
import android.content.Intent
import androidx.databinding.DataBindingUtil
import androidx.databinding.Observable
import android.os.Build
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.core.view.MenuItemCompat
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.ShareActionProvider
import android.view.Menu
import com.github.fgoncalves.androididenticon.identicon.IdenticonImpl
import com.github.fgoncalves.identicon.databinding.ActivityMainBinding
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.android.schedulers.AndroidSchedulers
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.darkslategray)
        }

        binding.viewModel = viewModel

        viewModel.shareIntent.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(observable: Observable?, propertyId: Int) {
                setShareIntent(viewModel.shareIntent.get())
            }
        })

        setSupportActionBar(toolbar)

        RxPermissions(this)
                .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            if (!it) {
                                AlertDialog.Builder(this@MainActivity)
                                        .setTitle(R.string.oops)
                                        .setMessage(R.string.no_permissions)
                                        .setPositiveButton(R.string.ok) { _, _ ->
                                            finish()
                                        }
                                        .show()
                            }
                        },
                        {})
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
