package br.ecosynergy_app.home.homefragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import br.ecosynergy_app.R
import br.ecosynergy_app.home.UserViewModel
import com.facebook.shimmer.ShimmerFrameLayout

class Home : Fragment() {

    private lateinit var lblFirstname: TextView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var shimmerViewContainer: ShimmerFrameLayout
    private val userViewModel: UserViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        lblFirstname = view.findViewById(R.id.lblFirstname)
        shimmerViewContainer = view.findViewById(R.id.shimmerViewContainer)

        userViewModel.user.observe(viewLifecycleOwner) { result ->
            shimmerViewContainer.animate().alpha(0f).setDuration(300).withEndAction {
                shimmerViewContainer.stopShimmer()
                shimmerViewContainer.visibility = View.GONE
                lblFirstname.visibility = View.VISIBLE
                lblFirstname.animate().alpha(1f).setDuration(300)
            }

            result.onSuccess { user ->
                val firstName = user.fullName.split(" ").firstOrNull()
                lblFirstname.text = "$firstName!"
            }.onFailure { throwable ->
                Log.e("HomeFragment", "Error fetching user data", throwable)
                lblFirstname.text = ""
            }
        }
        fetchUserData()
        refreshApp()
        return view
    }

    private fun fetchUserData() {
        lblFirstname.visibility = View.GONE
        lblFirstname.alpha = 0f
        shimmerViewContainer.visibility = View.VISIBLE
        shimmerViewContainer.startShimmer()
        shimmerViewContainer.alpha = 1f

        val sharedPreferences: SharedPreferences = requireActivity().getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", null)
        val token = sharedPreferences.getString("accessToken", null)

        if (username != null && token != null) {
            userViewModel.fetchUserData(username, token)
        } else {
            shimmerViewContainer.animate().alpha(0f).setDuration(300).withEndAction {
                shimmerViewContainer.stopShimmer()
                shimmerViewContainer.visibility = View.GONE
                lblFirstname.visibility = View.VISIBLE
                lblFirstname.animate().alpha(1f).setDuration(300)
            }
            lblFirstname.text = ""
        }
    }

    private fun refreshApp() {
        swipeRefresh.setOnRefreshListener {
            fetchUserData()
            swipeRefresh.isRefreshing = false
        }
    }
}