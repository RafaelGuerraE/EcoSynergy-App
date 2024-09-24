package br.ecosynergy_app.teams

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import br.ecosynergy_app.R
import br.ecosynergy_app.RetrofitClient
import br.ecosynergy_app.room.AppDatabase
import br.ecosynergy_app.room.MembersRepository
import br.ecosynergy_app.room.TeamsRepository
import br.ecosynergy_app.room.UserRepository
import br.ecosynergy_app.user.UserViewModel
import br.ecosynergy_app.user.UserViewModelFactory
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class TeamOverviewActivity : AppCompatActivity() {

    private lateinit var btnClose: ImageButton
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    private lateinit var userViewModel: UserViewModel
    private lateinit var teamsViewModel: TeamsViewModel

    private var userId: Int = 0
    private var accessToken: String = ""

    val userDao = AppDatabase.getDatabase(this).userDao()
    private val userRepository = UserRepository(userDao)

    val teamsDao = AppDatabase.getDatabase(this).teamsDao()
    private val teamsRepository = TeamsRepository(teamsDao)

    val membersDao = AppDatabase.getDatabase(this).membersDao()
    private val membersRepository = MembersRepository(membersDao)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teamoverview)

        teamsViewModel = ViewModelProvider(
            this,
            TeamsViewModelFactory(RetrofitClient.teamsService, teamsRepository, membersRepository)
        )[TeamsViewModel::class.java]
        userViewModel = ViewModelProvider(
            this,
            UserViewModelFactory(RetrofitClient.userService, userRepository)
        )[UserViewModel::class.java]

        val teamId = intent.getIntExtra("TEAM_ID", 0)
        val teamHandle = intent.getStringExtra("TEAM_HANDLE").toString()

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


        observeUserData {
            val adapter = OverviewAdapter(this, teamId, teamHandle, userId, accessToken)
            viewPager.adapter = adapter

            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                when (position) {
                    0 -> tab.text = "Visão Geral"
                    1 -> tab.text = "Membros"
                }
            }.attach()
        }
    }

    private fun observeUserData(onComplete: () -> Unit) {
        userViewModel.getUserInfoFromDB{}
        userViewModel.userInfo.observe(this) { userInfo ->
            userId = userInfo.id
            accessToken = userInfo.accessToken
            onComplete()
        }
    }
}