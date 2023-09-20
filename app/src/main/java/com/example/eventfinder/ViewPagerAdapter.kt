package com.example.eventfinder

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(fa: FragmentActivity):
    FragmentStateAdapter(fa) {
    override fun getItemCount(): Int = 2
    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> {
                NavHostFragment.create(R.navigation.form_result_nav_graph)
            }
            1 -> {
                FavoritesFragment()
            }
            else -> {
                throw IllegalAccessError("ViewPager index out of bound!")
            }
        }
    }
}