package br.ecosynergy_app.teams

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class OverviewAdapter(activity: AppCompatActivity, private val teamId: Int, private val teamHandle: String, private val userId: Int, private val accessToken: String) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> TeamOverviewFragment().apply {
                arguments = Bundle().apply {
                    putInt("TEAM_ID", teamId)
                    putString("TEAM_HANDLE", teamHandle)
                    putInt("USER_ID", userId)
                    putString("ACCESS_TOKEN", accessToken)
                }
            }
            1 -> TeamMembersFragment().apply {
                arguments = Bundle().apply {
                    putInt("TEAM_ID", teamId)
                    putString("TEAM_HANDLE", teamHandle)
                    putInt("USER_ID", userId)
                    putString("ACCESS_TOKEN", accessToken)
                }
            }
            else -> Fragment()
        }
    }
}
