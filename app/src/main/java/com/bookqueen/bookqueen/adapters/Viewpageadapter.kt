@file:Suppress("DEPRECATION")

package com.bookqueen.bookqueen.adapters


import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter



class viewpageadapter(supportfragmentmanger: FragmentManager) : FragmentPagerAdapter(
    supportfragmentmanger,
    BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
) {
    private val fragmentlist = ArrayList<Fragment>()
    private val fragmenttitlelist = ArrayList<String>()

    override fun getItem(position: Int): Fragment {
        return fragmentlist[position]
    }

    override fun getCount(): Int {
        return fragmentlist.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return fragmenttitlelist[position]
    }

    fun addfragment(fragment: Fragment, title: String) {
        fragmentlist.add(fragment)
        fragmenttitlelist.add(title)
    }

}