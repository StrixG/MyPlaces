package com.obrekht.maps.ui

import androidx.fragment.app.Fragment
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.obrekht.maps.R
import com.obrekht.maps.ui.map.MapFragmentArgs

fun Fragment.navigateToMap(placeId: Long = 0) {
    val navController = findNavController()
    val startDestinationId = navController.graph.findStartDestination().id
    val args = MapFragmentArgs.Builder()
        .setPlaceId(placeId)
        .build()

    navController.clearBackStack(R.id.map_fragment)

    navController.navigate(R.id.map_fragment, args.toBundle(), navOptions {
        restoreState = true
        launchSingleTop = true
        popUpTo(startDestinationId) {
            saveState = true
        }
    })
}
