package com.dicoding.didapp.ui.wallet

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dicoding.didapp.data.model.PatientData
import com.dicoding.didapp.data.model.SavedVIC

/**
 * ViewPager adapter untuk menampilkan Patients dan VICs di Digital Wallet
 */
class WalletPagerAdapter(
    fragmentActivity: FragmentActivity,
    private var patients: List<PatientData>,
    private var vics: List<SavedVIC>,
    private val onPatientClick: (PatientData) -> Unit = {},
    private val onVICClick: (SavedVIC) -> Unit = {}
) : FragmentStateAdapter(fragmentActivity) {

    private var patientFragment: PatientListFragment? = null
    private var vicFragment: VICListFragment? = null

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        android.util.Log.d("WalletPagerAdapter", "createFragment position: $position, patients: ${patients.size}, vics: ${vics.size}")
        return when (position) {
            0 -> {
                android.util.Log.d("WalletPagerAdapter", "Creating PatientListFragment with ${patients.size} patients")
                patientFragment = PatientListFragment.newInstance(patients, onPatientClick)
                patientFragment!!
            }
            1 -> {
                android.util.Log.d("WalletPagerAdapter", "Creating VICListFragment with ${vics.size} VICs")
                vicFragment = VICListFragment.newInstance(vics, onVICClick)
                vicFragment!!
            }
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }
    
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
    
    override fun containsItem(itemId: Long): Boolean {
        return itemId in 0 until itemCount
    }

    fun updateData(newPatients: List<PatientData>, newVics: List<SavedVIC>) {
        android.util.Log.d("WalletPagerAdapter", "updateData called with ${newPatients.size} patients and ${newVics.size} VICs")
        patients = newPatients
        vics = newVics
        
        // Update existing fragments if they exist
        patientFragment?.updateData(newPatients)
        vicFragment?.updateData(newVics)
        
        // Notify adapter that data has changed
        notifyDataSetChanged()
        
        android.util.Log.d("WalletPagerAdapter", "Data update completed - patients: ${newPatients.size}, vics: ${newVics.size}")
    }
    
    fun getPatientFragment(): PatientListFragment? = patientFragment
    fun getVICFragment(): VICListFragment? = vicFragment
}


