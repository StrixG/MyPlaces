package com.obrekht.maps.ui.myplaces

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.obrekht.maps.R
import com.obrekht.maps.databinding.FragmentMyPlacesBinding
import com.obrekht.maps.model.Place
import com.obrekht.maps.ui.navigateToMap
import com.obrekht.maps.utils.viewBinding
import kotlinx.coroutines.launch

class MyPlacesFragment : Fragment(R.layout.fragment_my_places) {

    private val binding by viewBinding(FragmentMyPlacesBinding::bind)
    private val viewModel: MyPlacesViewModel by viewModels { MyPlacesViewModel.Factory }

    private var adapter: MyPlacesAdapter? = null

    private val interactionListener: PlaceInteractionListener = object : PlaceInteractionListener {
        override fun onClick(place: Place, view: View) {
            navigateToMap(place.id)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        adapter = MyPlacesAdapter(interactionListener)
        placesList.adapter = adapter

        buttonCreateFirst.setOnClickListener {
            navigateToMap()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.flowWithLifecycle(viewLifecycleOwner.lifecycle)
                .collect {
                    adapter?.submitList(it.placeList)

                    emptyView.isVisible = it.placeList.isEmpty()
                }
        }

        Unit
    }

    override fun onDestroyView() {
        adapter = null
        super.onDestroyView()
    }
}
