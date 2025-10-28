package com.dicoding.didapp.ui.wallet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.didapp.R
import com.dicoding.didapp.data.model.MedicalRecord

/**
 * Adapter untuk menampilkan medical records di PatientDetailsActivity
 */
class MedicalRecordAdapter(
    private val medicalRecords: List<MedicalRecord>
) : RecyclerView.Adapter<MedicalRecordAdapter.MedicalRecordViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicalRecordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_medical_record, parent, false)
        return MedicalRecordViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: MedicalRecordViewHolder, position: Int) {
        holder.bind(medicalRecords[position])
    }
    
    override fun getItemCount(): Int = medicalRecords.size
    
    inner class MedicalRecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDiagnosis: TextView = itemView.findViewById(R.id.tv_diagnosis)
        private val tvTreatment: TextView = itemView.findViewById(R.id.tv_treatment)
        private val tvMedications: TextView = itemView.findViewById(R.id.tv_medications)
        private val tvNotes: TextView = itemView.findViewById(R.id.tv_notes)
        private val tvCheckupDate: TextView = itemView.findViewById(R.id.tv_checkup_date)
        private val tvDoctorName: TextView = itemView.findViewById(R.id.tv_doctor_name)
        private val tvHospitalName: TextView = itemView.findViewById(R.id.tv_hospital_name)
        
        fun bind(medicalRecord: MedicalRecord) {
            tvDiagnosis.text = "Diagnosis: ${medicalRecord.diagnosis}"
            tvTreatment.text = "Treatment: ${medicalRecord.treatment}"
            tvMedications.text = "Medications: ${medicalRecord.medications ?: "None"}"
            tvNotes.text = "Notes: ${medicalRecord.notes ?: "None"}"
            tvCheckupDate.text = "Checkup Date: ${medicalRecord.checkupDate}"
            tvDoctorName.text = "Doctor: ${medicalRecord.doctorName}"
            tvHospitalName.text = "Hospital: ${medicalRecord.hospitalName}"
        }
    }
}
