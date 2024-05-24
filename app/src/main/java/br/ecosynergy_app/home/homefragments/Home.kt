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
import br.ecosynergy_app.R
import br.ecosynergy_app.RetrofitClient
import br.ecosynergy_app.home.UserResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class Home : Fragment() {

    private lateinit var lblFirstname: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        lblFirstname = view.findViewById(R.id.lblFirstname)
        progressBar = view.findViewById(R.id.progressBar)

        fetchUserData()

        return view
    }

    private fun fetchUserData() {
        progressBar.visibility = View.VISIBLE
        lblFirstname.visibility = View.GONE

        val sharedPreferences: SharedPreferences = requireActivity().getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", null)
        val token = sharedPreferences.getString("accessToken", null)

        if (username != null && token != null) {
            RetrofitClient.userService.getUser(username, "Bearer $token").enqueue(object : Callback<UserResponse> {
                override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                    progressBar.visibility = View.GONE
                    lblFirstname.visibility = View.VISIBLE

                    if (response.isSuccessful) {
                        val user = response.body()
                        if (user != null) {
                            val firstName = user.fullName.split(" ").firstOrNull()
                            lblFirstname.text = firstName + "!"
                        }
                    } else {
                        Log.e("HomeFragment", "Error fetching user data: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    lblFirstname.visibility = View.VISIBLE

                    Log.e("HomeFragment", "Network error fetching user data", t)
                }
            })
        } else {
            progressBar.visibility = View.GONE
            lblFirstname.visibility = View.VISIBLE

            lblFirstname.text = "Invalid username or token"
        }
    }



    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Home().apply {
                arguments = Bundle().apply {
                    putString("param1", param1)
                    putString("param2", param2)
                }
            }
    }
}
