package com.dicoding.didapp.ui.wallet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.didapp.R
import com.dicoding.didapp.data.model.SavedVIC

/**
 * Fragment untuk menampilkan daftar VIC yang tersimpan
 */
class VICListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: VICAdapter
    private var vics: List<SavedVIC> = emptyList()
    private var onVICClick: (SavedVIC) -> Unit = {}

    companion object {
        fun newInstance(
            vics: List<SavedVIC>,
            onVICClick: (SavedVIC) -> Unit = {}
        ): VICListFragment {
            val fragment = VICListFragment()
            fragment.vics = vics
            fragment.onVICClick = onVICClick
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        android.util.Log.d("VICListFragment", "onCreateView called with ${vics.size} VICs")
        return inflater.inflate(R.layout.fragment_vic_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        android.util.Log.d("VICListFragment", "onViewCreated with ${vics.size} VICs")
        
        recyclerView = view.findViewById(R.id.recycler_view)
        val emptyState = view.findViewById<View>(R.id.empty_state)
        
        adapter = VICAdapter(requireContext(), vics, onVICClick)
        
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        
        // Update data if we have VICs
        if (vics.isNotEmpty()) {
            adapter.updateData(vics)
        }
        
        // Update empty state visibility
        updateEmptyState()
        
        // Force layout update
        recyclerView.post {
            android.util.Log.d("VICListFragment", "Post onViewCreated: forcing layout update")
            recyclerView.requestLayout()
            recyclerView.invalidate()
        }
    }

    fun updateData(newVics: List<SavedVIC>) {
        android.util.Log.d("VICListFragment", "updateData called with ${newVics.size} VICs")
        vics = newVics
        if (::adapter.isInitialized) {
            adapter.updateData(newVics)
            android.util.Log.d("VICListFragment", "Adapter updated with ${newVics.size} VICs")
        } else {
            android.util.Log.w("VICListFragment", "Adapter not initialized yet")
        }
        updateEmptyState()
    }
    
    private fun updateEmptyState() {
        android.util.Log.d("VICListFragment", "updateEmptyState called, vics.size: ${vics.size}")
        if (::recyclerView.isInitialized) {
            val emptyState = view?.findViewById<View>(R.id.empty_state)
            android.util.Log.d("VICListFragment", "Empty state view found: ${emptyState != null}")
            android.util.Log.d("VICListFragment", "RecyclerView visibility: ${recyclerView.visibility}")
            
            if (vics.isEmpty()) {
                android.util.Log.d("VICListFragment", "Showing empty state")
                emptyState?.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                android.util.Log.d("VICListFragment", "Showing RecyclerView with ${vics.size} items")
                emptyState?.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                
                // Force layout update
                recyclerView.requestLayout()
                recyclerView.invalidate()
            }
        } else {
            android.util.Log.w("VICListFragment", "RecyclerView not initialized yet")
        }
    }
}


