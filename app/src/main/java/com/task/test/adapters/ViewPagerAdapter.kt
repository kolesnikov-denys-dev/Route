package com.task.test.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(activity: FragmentActivity, private val list: List<Fragment>) :
    FragmentStateAdapter(activity) {

    override fun createFragment(position: Int): Fragment = list[position]
    override fun getItemCount(): Int = list.size

}