package com.dicoding.didapp.ui.wallet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.didapp.R
import com.dicoding.didapp.data.model.PatientData

/**
 * Adapter untuk RecyclerView di Digital Wallet
 */
class PatientDataAdapter(
    private val onItemClick: (PatientData) -> Unit
) : RecyclerView.Adapter<PatientDataAdapter.PatientViewHolder>() {
    
    private var patients: List<PatientData> = emptyList()
    
    fun updateData(newPatients: List<PatientData>) {
        android.util.Log.d("PatientDataAdapter", "updateData called with ${newPatients.size} patients")
        patients = newPatients
        notifyDataSetChanged()
        android.util.Log.d("PatientDataAdapter", "notifyDataSetChanged() called, adapter should now show ${patients.size} items")
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_patient, parent, false)
        return PatientViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: PatientViewHolder, position: Int) {
        android.util.Log.d("PatientDataAdapter", "onBindViewHolder position: $position, total items: ${patients.size}")
        holder.bind(patients[position])
    }
    
    override fun getItemCount(): Int {
        android.util.Log.d("PatientDataAdapter", "getItemCount called: ${patients.size} patients")
        return patients.size
    }
    
    inner class PatientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tv_patient_name)
        private val tvAge: TextView = itemView.findViewById(R.id.tv_patient_age)
        private val tvGender: TextView = itemView.findViewById(R.id.tv_patient_gender)
        private val tvDid: TextView = itemView.findViewById(R.id.tv_patient_did)
        private val tvStatus: TextView = itemView.findViewById(R.id.tv_verification_status)
        private val btnViewPatient: com.google.android.material.button.MaterialButton = itemView.findViewById(R.id.btn_view_patient)
        private val btnSharePatient: com.google.android.material.button.MaterialButton = itemView.findViewById(R.id.btn_share_patient)
        
        fun bind(patient: PatientData) {
            try {
                tvName.text = "👤 ${patient.name}"
                tvAge.text = "🎂 Age: ${patient.age}"
                tvGender.text = "⚥ ${patient.gender}"
                tvDid.text = "🔗 DID: ${patient.did.take(8)}..."
                tvStatus.text = "✅ ${patient.verificationStatus}"
                
                // Set status color
                when (patient.verificationStatus) {
                    "VERIFIED" -> {
                        tvStatus.setTextColor(itemView.context.getColor(android.R.color.holo_green_dark))
                    }
                    "FAILED" -> {
                        tvStatus.setTextColor(itemView.context.getColor(android.R.color.holo_red_dark))
                    }
                    else -> {
                        tvStatus.setTextColor(itemView.context.getColor(android.R.color.darker_gray))
                    }
                }
                
                // View patient button
                btnViewPatient.setOnClickListener {
                    try {
                        android.util.Log.d("PatientAdapter", "View patient clicked: ${patient.name}")
                        onItemClick(patient)
                    } catch (e: Exception) {
                        android.util.Log.e("PatientAdapter", "Error clicking view patient: ${e.message}")
                    }
                }
                
                // Share patient button
                btnSharePatient.setOnClickListener {
                    try {
                        android.util.Log.d("PatientAdapter", "Share patient clicked: ${patient.name}")
                        // TODO: Implement share functionality
                        android.widget.Toast.makeText(itemView.context, "Share functionality coming soon!", android.widget.Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        android.util.Log.e("PatientAdapter", "Error clicking share patient: ${e.message}")
                    }
                }
                
                // Click on item
                itemView.setOnClickListener {
                    try {
                        android.util.Log.d("PatientAdapter", "Patient clicked: ${patient.name}")
                        onItemClick(patient)
                    } catch (e: Exception) {
                        android.util.Log.e("PatientAdapter", "Error clicking patient: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("PatientAdapter", "Error binding patient data: ${e.message}")
                tvName.text = "Error loading patient data"
                tvAge.text = ""
                tvGender.text = ""
                tvDid.text = ""
                tvStatus.text = "Error"
            }
        }
    }
}
