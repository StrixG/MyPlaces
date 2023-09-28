package com.obrekht.maps

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.obrekht.maps.databinding.ActivityMainBinding
import com.obrekht.maps.utils.viewBinding
import com.yandex.mapkit.MapKitFactory


class MainActivity : AppCompatActivity() {

    private val binding by viewBinding(ActivityMainBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.initialize(this)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val navController = binding.navHostFragment.getFragment<NavHostFragment>().navController
        NavigationUI.setupWithNavController(binding.bottomNavigation, navController)
    }
}
