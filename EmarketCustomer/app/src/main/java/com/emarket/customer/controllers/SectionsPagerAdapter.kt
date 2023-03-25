package com.emarket.customer.controllers

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.emarket.customer.activities.ProfileFragment
import com.emarket.customer.R
import com.emarket.customer.activities.TransactionsFragment

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class SectionsPagerAdapter(private val context: Context, fm: FragmentManager) :
    FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            1 -> TransactionsFragment()
            else -> {
                ProfileFragment()
            }
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> context.resources.getString(R.string.tab_profile)
            1 -> context.resources.getString(R.string.tab_transactions)
            else -> {""}
        }
    }

    override fun getCount(): Int {
        return 2
    }

    companion object {
        @JvmStatic
        fun getTabsCount(): Int {
            return 2
        }
    }
}