package com.bookqueen.bookqueen.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.bookqueen.bookqueen.R
import com.bookqueen.bookqueen.adapters.viewpageadapter
import com.google.android.material.tabs.TabLayout


class Home : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val rootview = inflater.inflate(R.layout.fragment_home, container, false)
        val viewPager: ViewPager = rootview.findViewById(R.id.viewpager)
        //setupviewpager(viewPager )
        val tabLayout: TabLayout = rootview.findViewById(R.id.tablayout)
        tabLayout.setupWithViewPager(viewPager)
        val adapter = viewpageadapter(childFragmentManager)
        adapter.addfragment(Books(), "Books")
        adapter.addfragment(BookSets(), "BookSets")
        adapter.addfragment(Tools(), "Tools")
        viewPager.adapter = adapter


            return rootview

    }
}