package br.ecosynergy_app.teams

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import br.ecosynergy_app.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class TeamOverviewActivity : AppCompatActivity() {

    private lateinit var btnClose: ImageButton
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teamoverview)

        val teamHandle = intent.getStringExtra("TEAM_HANDLE")
        val teamId = intent.getStringExtra("TEAM_ID")

        btnClose = findViewById(R.id.btnClose)

        btnClose.setOnClickListener {
            finish()
        }

        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)


        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                val selectedPageView =
                    (viewPager[0] as RecyclerView).findViewHolderForAdapterPosition(position)?.itemView

                selectedPageView?.post {
                    val widthSpec = View.MeasureSpec.makeMeasureSpec(
                        selectedPageView.width,
                        View.MeasureSpec.EXACTLY
                    )
                    val heightSpec =
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                    selectedPageView.measure(widthSpec, heightSpec)

                    val newHeight = selectedPageView.measuredHeight
                    if (viewPager.layoutParams.height != newHeight) {
                        viewPager.layoutParams =
                            (viewPager.layoutParams as LinearLayout.LayoutParams).also { lp ->
                                lp.height = newHeight
                            }
                    }
                }
            }
        })
        val adapter = OverviewAdapter(this, teamHandle,teamId)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Visão Geral"
                1 -> tab.text = "Membros"
            }
        }.attach()
    }
}