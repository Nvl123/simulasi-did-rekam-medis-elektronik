package com.dicoding.didapp.ui.wallet

import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.didapp.R
import com.dicoding.didapp.data.model.SavedVIC
import com.dicoding.didapp.utils.Config
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adapter untuk menampilkan VIC yang tersimpan di e-wallet
 */
class VICAdapter(
    private val context: Context,
    private var vics: List<SavedVIC> = emptyList(),
    private val onVICClick: (SavedVIC) -> Unit = {}
) : RecyclerView.Adapter<VICAdapter.VICViewHolder>() {

    class VICViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvPatientName: TextView = view.findViewById(R.id.tv_patient_name)
        val tvTransactionHash: TextView = view.findViewById(R.id.tv_transaction_hash)
        val tvVerificationDate: TextView = view.findViewById(R.id.tv_verification_date)
        val tvStatus: TextView = view.findViewById(R.id.tv_status)
        val btnViewDetails: com.google.android.material.button.MaterialButton = view.findViewById(R.id.btn_view_details)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VICViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_saved_vic, parent, false)
        return VICViewHolder(view)
    }

    override fun onBindViewHolder(holder: VICViewHolder, position: Int) {
        android.util.Log.d("VICAdapter", "onBindViewHolder position: $position, total items: ${vics.size}")
        val vic = vics[position]
        
        android.util.Log.d("VICAdapter", "Binding VIC: ${vic.patientName}, status: ${vic.verificationStatus}")
        
        holder.tvPatientName.text = vic.patientName
        holder.tvTransactionHash.text = "Hash: ${vic.transactionHash.take(16)}..."
        holder.tvVerificationDate.text = formatDate(vic.verificationDate)
        holder.tvStatus.text = vic.verificationStatus
        
        // Set status color
        when (vic.verificationStatus) {
            "VERIFIED" -> {
                holder.tvStatus.setTextColor(context.getColor(android.R.color.holo_green_dark))
            }
            "FAILED" -> {
                holder.tvStatus.setTextColor(context.getColor(android.R.color.holo_red_dark))
            }
            else -> {
                holder.tvStatus.setTextColor(context.getColor(android.R.color.darker_gray))
            }
        }
        
        // View details button
        holder.btnViewDetails.setOnClickListener {
            onVICClick(vic)
        }
        
        // Click on item
        holder.itemView.setOnClickListener {
            onVICClick(vic)
        }
    }

    override fun getItemCount(): Int {
        android.util.Log.d("VICAdapter", "getItemCount called: ${vics.size} VICs")
        return vics.size
    }

    fun updateData(newVics: List<SavedVIC>) {
        android.util.Log.d("VICAdapter", "updateData called with ${newVics.size} VICs")
        vics = newVics
        notifyDataSetChanged()
        android.util.Log.d("VICAdapter", "notifyDataSetChanged() called, adapter should now show ${vics.size} items")
    }


    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}


