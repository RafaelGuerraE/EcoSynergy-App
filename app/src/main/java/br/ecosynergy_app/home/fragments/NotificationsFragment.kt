package br.ecosynergy_app.home.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import br.ecosynergy_app.R
import br.ecosynergy_app.room.notifications.Notifications
import br.ecosynergy_app.user.NotificationActivity
import br.ecosynergy_app.user.NotificationAdapter

class NotificationsFragment : Fragment() {

    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var recyclerViewNotifications: RecyclerView
    private lateinit var linearAlert: LinearLayout
    private lateinit var notificationAdapter: NotificationAdapter

    private var notificationsList = listOf<Notifications>() // Replace with actual data fetching


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notifications, container, false)


        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        recyclerViewNotifications = view.findViewById(R.id.recyclerViewNotifications)
        linearAlert = view.findViewById(R.id.linearAlert)


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSwipeRefresh()

        // Initially load notifications and update the UI
        loadNotifications()
    }

    private fun setupRecyclerView() {
        // Initialize the adapter with an empty list and a click listener
        notificationAdapter = NotificationAdapter(notificationsList)

        recyclerViewNotifications.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = notificationAdapter
        }
    }

    private fun setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener {
            loadNotifications()
            swipeRefresh.isRefreshing = false
        }
    }

    private fun loadNotifications() {
        // Fetch notifications from the database or other data source
        // Replace notificationsList with actual data loading logic
        notificationsList = fetchNotifications()

        // Update adapter data and notify changes
        notificationAdapter.updateData(notificationsList)

        // Show or hide linearAlert based on data availability
        if (notificationsList.isEmpty()) {
            linearAlert.visibility = View.VISIBLE
            recyclerViewNotifications.visibility = View.GONE
        } else {
            linearAlert.visibility = View.GONE
            recyclerViewNotifications.visibility = View.VISIBLE
        }
    }

    private fun fetchNotifications(): List<Notifications> {
        // Placeholder for fetching notifications from Room or another data source
        return listOf(Notifications(1, "greeting", "Seja bem vindo à plataforma Ecosynergy!", "Aqui você pode explorar todas as métricas de sua indústria", "2024-10-26T19:40:00"))
        // Empty list simulates no notifications
    }
}
