package br.ecosynergy_app.teams

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class OverviewAdapter(activity: AppCompatActivity, private val teamHandle: String?, private val teamId: String?) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> TeamOverviewFragment().apply {
                arguments = Bundle().apply {
                    putString("TEAM_HANDLE", teamHandle)
                    putString("TEAM_ID", teamId)
                }
            }
            1 -> TeamMembersFragment().apply {
                arguments = Bundle().apply {
                    putString("TEAM_HANDLE", teamHandle)
                    putString("TEAM_ID", teamId)
                }
            }
            else -> Fragment()
        }
    }
}
