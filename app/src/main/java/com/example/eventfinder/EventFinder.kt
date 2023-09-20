package com.example.eventfinder

import android.Manifest
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val Context.dataStore by preferencesDataStore(name = FavoriteDataStoreSingleton.FAVORITE_DATASTORE_NAME)
    override fun onCreate(savedInstanceState: Bundle?) {
        VolleySingleton.init(this)
        FavoriteDataStoreSingleton.init(this.dataStore, lifecycleScope)

        setTheme(R.style.Theme_EventFinder)
        super.onCreate(savedInstanceState)
        Log.d("activity context", this.toString())

//        // try to track resource leak problem
//        StrictMode.setVmPolicy(
//            VmPolicy.Builder(StrictMode.getVmPolicy())
//                .detectLeakedClosableObjects()
//                .build()
//        )

        setContentView(R.layout.activity_main)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                0
            )
        }

        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        viewPager.adapter = ViewPagerAdapter(this)

        val tabLayOut = findViewById<TabLayout>(R.id.tabLayout)
        TabLayoutMediator(tabLayOut, viewPager) { tab, position ->
            when(position) {
                0 -> tab.text = "SEARCH"
                1 -> tab.text = "FAVORITES"
            }
        }.attach()
    }

    override fun onDestroy() {
        VolleySingleton.cancelAll(true)
        super.onDestroy()
    }

//    override fun onResume() {
//        Log.d("old activity resumed", "")
//            super.onResume()
//    }
}
