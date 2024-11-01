package br.ecosynergy_app.home.fragments

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
import br.ecosynergy_app.room.AppDatabase
import br.ecosynergy_app.room.notifications.Notifications
import br.ecosynergy_app.room.notifications.NotificationsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotificationsFragment : Fragment() {

    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var recyclerViewNotifications: RecyclerView
    private lateinit var linearAlert: LinearLayout
    private lateinit var notificationAdapter: NotificationAdapter

    private lateinit var notificationsRepository: NotificationsRepository

    private var notificationsList = listOf<Notifications>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notifications, container, false)

        notificationsRepository = NotificationsRepository(AppDatabase.getDatabase(requireContext()).notificationsDao())

        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        recyclerViewNotifications = view.findViewById(R.id.recyclerViewNotifications)
        linearAlert = view.findViewById(R.id.linearAlert)


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSwipeRefresh()

        loadNotifications()
    }

    private fun setupRecyclerView() {
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
        CoroutineScope(Dispatchers.Main).launch {
            notificationsList = fetchNotifications()

            notificationAdapter.updateData(notificationsList)

            if (notificationsList.isEmpty()) {
                linearAlert.visibility = View.VISIBLE
                recyclerViewNotifications.visibility = View.GONE
            } else {
                linearAlert.visibility = View.GONE
                recyclerViewNotifications.visibility = View.VISIBLE
            }
        }
    }

    private suspend fun fetchNotifications(): List<Notifications> {
        return withContext(Dispatchers.IO) {
            notificationsRepository.getAllNotifications()
        }
    }

}