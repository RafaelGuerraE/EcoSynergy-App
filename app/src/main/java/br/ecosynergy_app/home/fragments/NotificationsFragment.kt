package br.ecosynergy_app.home.fragments

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import br.ecosynergy_app.R
import br.ecosynergy_app.room.AppDatabase
import br.ecosynergy_app.room.notifications.Notifications
import br.ecosynergy_app.room.notifications.NotificationsRepository
import br.ecosynergy_app.user.viewmodel.UserViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotificationsFragment : Fragment() {

    private val userViewModel: UserViewModel  by activityViewModels()

    private var userId: Int = 0
    private var accessToken: String =""

    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var recyclerViewNotifications: RecyclerView
    private lateinit var linearAlert: LinearLayout
    private lateinit var notificationAdapter: NotificationAdapter

    private lateinit var loadingProgressBar: ProgressBar

    private lateinit var notificationsRepository: NotificationsRepository

    private var notificationsList = mutableListOf<Notifications>()

    private var deleteIcon: Drawable? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notifications, container, false)
        deleteIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete)

        notificationsRepository =
            NotificationsRepository(AppDatabase.getDatabase(requireContext()).notificationsDao())

        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        recyclerViewNotifications = view.findViewById(R.id.recyclerViewNotifications)
        linearAlert = view.findViewById(R.id.linearAlert)

        loadingProgressBar = view.findViewById(R.id.loadingProgressBar)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingProgressBar.visibility = View.VISIBLE

        userViewModel.getUserInfoFromDB {
            val userInfo = userViewModel.userInfo.value
            accessToken = userInfo!!.accessToken
            userId = userInfo.id
            setupRecyclerView()
            setupSwipeRefresh()
            loadNotifications()
        }
    }

    private fun setupRecyclerView() {
        notificationAdapter = NotificationAdapter(notificationsList, accessToken, userId) { notificationId ->
            lifecycleScope.launch {
                notificationsRepository.markAsRead(notificationId)
                val updatedNotifications = notificationsRepository.getAllNotifications()
                notificationAdapter.updateData(updatedNotifications)
            }
        }

        recyclerViewNotifications.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = notificationAdapter
        }

        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerViewNotifications)
    }

    private val itemTouchHelperCallback =
        object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val notification = notificationAdapter.notificationsList[position]

                lifecycleScope.launch {
                    notificationsRepository.deleteNotification(notification)
                    val updatedNotifications = notificationsRepository.getAllNotifications()
                    notificationAdapter.updateData(updatedNotifications)
                }

                showToast("Notificação excluída")
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {

                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    val itemView = viewHolder.itemView

                    val background = RectF(
                        itemView.right + dX,
                        itemView.top.toFloat(),
                        itemView.right.toFloat(),
                        itemView.bottom.toFloat()
                    )
                    val paint = Paint()
                    paint.color = Color.RED
                    c.drawRect(background, paint)

                    if (dX < -100) {
                        val iconMargin = (itemView.height - (deleteIcon?.intrinsicHeight ?: 0)) / 2
                        val iconTop = itemView.top + iconMargin
                        val iconLeft = itemView.right - iconMargin - (deleteIcon?.intrinsicWidth ?: 0)
                        val iconRight = itemView.right - iconMargin
                        val iconBottom = iconTop + (deleteIcon?.intrinsicHeight ?: 0)

                        deleteIcon?.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                        deleteIcon?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
                        deleteIcon?.draw(c)
                    }
                }else {
                }
                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
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
            notificationsList = fetchNotifications().toMutableList()

            notificationAdapter.updateData(notificationsList)

            if (notificationsList.isEmpty()) {
                linearAlert.visibility = View.VISIBLE
                recyclerViewNotifications.visibility = View.GONE
            } else {
                linearAlert.visibility = View.GONE
                recyclerViewNotifications.visibility = View.VISIBLE
            }

            loadingProgressBar.visibility = View.GONE
        }
    }

    private suspend fun fetchNotifications(): List<Notifications> {
        return withContext(Dispatchers.IO) {
            notificationsRepository.getAllNotifications()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

}