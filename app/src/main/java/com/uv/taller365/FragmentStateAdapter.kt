package com.uv.taller365

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.uv.taller365.clientFiles.ClientsFragment
import com.uv.taller365.repairFiles.RepairFragment
import com.uv.taller365.vehicleFiles.VehiclesFragment
import com.uv.taller365.workshopFiles.ModifyWorkshop

class FragmentStateAdapter(
    fragmentActivity: FragmentActivity,
    private val workshopId: String
) : FragmentStateAdapter(fragmentActivity) {

    private var itemCount = 4
    private var userRole: String = "admin"

    fun setRole(role: String) {
        userRole = role
    }

    fun setItemCount(count: Int) {
        itemCount = count
    }

    override fun getItemCount(): Int = itemCount

    override fun createFragment(position: Int): Fragment {
        val bundle = Bundle().apply {
            putString("WORKSHOP_ID", workshopId)
        }

        return when (position) {
            0 -> VehiclesFragment().apply { arguments = bundle }
            1 -> RepairFragment().apply { arguments = bundle }
            2 -> ClientsFragment().apply { arguments = bundle }
            3 -> ModifyWorkshop().apply { arguments = bundle }
            else -> throw IllegalStateException("Fragment desconocido para posición $position")
        }
    }
}

