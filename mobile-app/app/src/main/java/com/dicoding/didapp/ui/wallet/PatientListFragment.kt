package com.dicoding.didapp.ui.wallet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.didapp.R
import com.dicoding.didapp.data.model.PatientData

/**
 * Fragment untuk menampilkan daftar pasien
 */
class PatientListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PatientDataAdapter
    private var patients: List<PatientData> = emptyList()
    private var onPatientClick: (PatientData) -> Unit = {}

    companion object {
        fun newInstance(
            patients: List<PatientData>,
            onPatientClick: (PatientData) -> Unit = {}
        ): PatientListFragment {
            val fragment = PatientListFragment()
            fragment.patients = patients
            fragment.onPatientClick = onPatientClick
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_patient_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        android.util.Log.d("PatientListFragment", "onViewCreated with ${patients.size} patients")
        
        recyclerView = view.findViewById(R.id.recycler_view)
        adapter = PatientDataAdapter(onPatientClick)
        
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        
        // Update data if we have patients
        if (patients.isNotEmpty()) {
            adapter.updateData(patients)
        }
        
        // Update empty state visibility
        updateEmptyState()
        
        // Force layout update
        recyclerView.post {
            android.util.Log.d("PatientListFragment", "Post onViewCreated: forcing layout update")
            recyclerView.requestLayout()
            recyclerView.invalidate()
        }
    }

    fun updateData(newPatients: List<PatientData>) {
        android.util.Log.d("PatientListFragment", "updateData called with ${newPatients.size} patients")
        patients = newPatients
        if (::adapter.isInitialized) {
            adapter.updateData(newPatients)
            android.util.Log.d("PatientListFragment", "Adapter updated with ${newPatients.size} patients")
        } else {
            android.util.Log.w("PatientListFragment", "Adapter not initialized yet")
        }
        updateEmptyState()
    }
    
    private fun updateEmptyState() {
        android.util.Log.d("PatientListFragment", "updateEmptyState called, patients.size: ${patients.size}")
        if (::recyclerView.isInitialized) {
            val emptyState = view?.findViewById<View>(R.id.empty_state)
            android.util.Log.d("PatientListFragment", "Empty state view found: ${emptyState != null}")
            android.util.Log.d("PatientListFragment", "RecyclerView visibility: ${recyclerView.visibility}")
            
            if (patients.isEmpty()) {
                android.util.Log.d("PatientListFragment", "Showing empty state")
                emptyState?.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                android.util.Log.d("PatientListFragment", "Showing RecyclerView with ${patients.size} items")
                emptyState?.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                
                // Force layout update
                recyclerView.requestLayout()
                recyclerView.invalidate()
            }
        } else {
            android.util.Log.w("PatientListFragment", "RecyclerView not initialized yet")
        }
    }
}
