package com.uv.taller365

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class FragmentStateAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> VehiclesFragment()
            1 -> RepairFragment()
            2 -> ClientsFragment()
            else -> throw IllegalStateException("Fragment desconocido para posición $position")

        }
    }
}