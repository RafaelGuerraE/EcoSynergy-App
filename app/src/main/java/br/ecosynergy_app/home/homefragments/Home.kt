package br.ecosynergy_app.home.homefragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import br.ecosynergy_app.R
import br.ecosynergy_app.home.UserViewModel

class Home : Fragment() {

    private lateinit var lblFirstname: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private val userViewModel: UserViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        lblFirstname = view.findViewById(R.id.lblFirstname)
        progressBar = view.findViewById(R.id.progressBar)

        userViewModel.user.observe(viewLifecycleOwner) { result ->
            progressBar.visibility = View.GONE
            lblFirstname.visibility = View.VISIBLE
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
        progressBar.visibility = View.VISIBLE
        lblFirstname.visibility = View.GONE

        val sharedPreferences: SharedPreferences = requireActivity().getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", null)
        val token = sharedPreferences.getString("accessToken", null)

        if (username != null && token != null) {
            userViewModel.fetchUserData(username, token)
        } else {
            progressBar.visibility = View.GONE
            lblFirstname.visibility = View.VISIBLE
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