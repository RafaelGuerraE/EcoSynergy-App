package br.ecosynergy_app.teams

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import br.ecosynergy_app.R
import br.ecosynergy_app.RetrofitClient
import br.ecosynergy_app.room.AppDatabase
import br.ecosynergy_app.room.invites.InvitesRepository
import br.ecosynergy_app.room.teams.MembersRepository
import br.ecosynergy_app.room.teams.TeamsRepository
import br.ecosynergy_app.room.user.UserRepository
import br.ecosynergy_app.teams.viewmodel.TeamsViewModel
import br.ecosynergy_app.teams.viewmodel.TeamsViewModelFactory
import br.ecosynergy_app.user.viewmodel.UserViewModel
import br.ecosynergy_app.user.viewmodel.UserViewModelFactory
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText

class AddMembersBottomSheet : BottomSheetDialogFragment() {

    private lateinit var btnClose: ImageButton
    private lateinit var txtMember: TextInputEditText
    private lateinit var btnSearch: ImageButton

    private lateinit var teamsViewModel: TeamsViewModel
    private lateinit var userViewModel: UserViewModel

    private lateinit var recycleUsers: RecyclerView
    lateinit var usersAdapter: UsersAdapter
    private lateinit var shimmerUsers: ShimmerFrameLayout

    private lateinit var swipeRefresh: SwipeRefreshLayout

    private var accessToken: String = ""
    private var teamId: Int = 0
    private var teamHandle: String = ""
    private var memberIds: ArrayList<String> = arrayListOf()

    private var userId: Int = 0
    private var username: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            teamHandle = it.getString("TEAM_HANDLE").toString()
            teamId = it.getInt("TEAM_ID")
            val memberIdsString = it.getString("MEMBER_IDS") ?: ""
            memberIds = ArrayList(memberIdsString.split(","))
            accessToken = it.getString("ACCESS_TOKEN", "")
        }
        Log.d("AddMembers", "MemberIDS in onCreate: $memberIds, $teamHandle, $teamId")
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_addmembers_bottom_sheet, container, false)

        val userRepository = UserRepository(AppDatabase.getDatabase(requireContext()).userDao())
        val teamsRepository = TeamsRepository(AppDatabase.getDatabase(requireContext()).teamsDao())
        val membersRepository =
            MembersRepository(AppDatabase.getDatabase(requireContext()).membersDao())
        val invitesRepository = InvitesRepository(AppDatabase.getDatabase(requireContext()).invitesDao())

        teamsViewModel = ViewModelProvider(
            requireActivity(),
            TeamsViewModelFactory(RetrofitClient.teamsService, teamsRepository, RetrofitClient.invitesService, membersRepository, invitesRepository)
        )[TeamsViewModel::class.java]
        userViewModel = ViewModelProvider(
            requireActivity(),
            UserViewModelFactory(RetrofitClient.userService, userRepository)
        )[UserViewModel::class.java]

        btnClose = view.findViewById(R.id.btnClose)

        txtMember = view.findViewById(R.id.txtMember)
        btnSearch = view.findViewById(R.id.btnSearch)

        shimmerUsers = view.findViewById(R.id.shimmerUsers)
        recycleUsers = view.findViewById(R.id.recycleUsers)

        shimmerUsers.visibility = View.GONE
        recycleUsers.visibility = View.GONE

        recycleUsers.layoutManager = LinearLayoutManager(requireContext())
        usersAdapter = UsersAdapter(
            mutableListOf(),
            teamId,
            teamHandle,
            teamsViewModel,
            memberIds,
            accessToken,
            userId,
            username,
            requireActivity()
        )
        recycleUsers.adapter = usersAdapter

        Log.d("AddMembers", "MemberIDS in onCreateView: $memberIds, $teamHandle, $teamId")

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomSheet =
            dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)
            behavior.peekHeight = 2000
        }

        btnClose.setOnClickListener {
            parentFragmentManager.setFragmentResult("requestKey", Bundle())
            dismiss()
        }

        btnSearch.setOnClickListener {
            searchUser(txtMember.text.toString(), accessToken)
        }
    }

    private fun searchUser(username: String, accessToken: String) {
        userViewModel.searchUser(username, accessToken)
        userViewModel.users.observe(viewLifecycleOwner) { result ->
            result.onSuccess { usersList ->
                shimmerUsers.visibility = View.VISIBLE
                recycleUsers.visibility = View.GONE

                usersAdapter = UsersAdapter(
                    usersList,
                    teamId,
                    teamHandle,
                    teamsViewModel,
                    memberIds,
                    accessToken,
                    userId,
                    username,
                    requireActivity()
                )
                recycleUsers.adapter = usersAdapter

                shimmerUsers.animate().alpha(0f).setDuration(300).withEndAction {
                    shimmerUsers.stopShimmer()
                    shimmerUsers.animate().alpha(1f).setDuration(300)
                    shimmerUsers.visibility = View.GONE
                    recycleUsers.visibility = View.VISIBLE
                }
            }.onFailure { error ->
                error.printStackTrace()
                Log.d("TeamOverviewFragment", "User Result Failed: ${error.message}")
                shimmerUsers.visibility = View.GONE
                recycleUsers.visibility = View.GONE
                showSnackBar("ERRO: Carregar usuários", "FECHAR", R.color.red)
            }
        }
    }

    private fun showSnackBar(message: String, action: String, bgTint: Int) {
        val rootView = requireActivity().findViewById<View>(android.R.id.content)
        val snackBar = Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT)
            .setAction(action) {}
        snackBar.setBackgroundTint(ContextCompat.getColor(requireContext(), bgTint))
        snackBar.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        snackBar.setActionTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        snackBar.show()
    }
}