package com.obrekht.maps.ui.myplaces

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.obrekht.maps.R
import com.obrekht.maps.databinding.FragmentMyPlacesBinding
import com.obrekht.maps.model.Place
import com.obrekht.maps.utils.viewBinding

class MyPlacesFragment : Fragment(R.layout.fragment_my_places) {

    private val binding by viewBinding(FragmentMyPlacesBinding::bind)
    private val viewModel: MyPlacesViewModel by viewModels()

    private var adapter: MyPlacesAdapter? = null

    private val interactionListener: PlaceInteractionListener = object : PlaceInteractionListener {
        override fun onClick(place: Place, view: View) {

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        adapter = MyPlacesAdapter(interactionListener)
        placesList.adapter = adapter

        adapter?.submitList(listOf(
            Place(1, "Test", "", 20.0, 50.0),
            Place(2, "Test 2", "", 20.0, 55.0),
            Place(3, "Test 3", "", 25.0, 50.0),
        ))

        Unit
    }

    override fun onDestroyView() {
        adapter = null
        super.onDestroyView()
    }
}