// HomeScreen.kt
package com.example.eventticketapp.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.eventticketapp.data.model.Event
import com.example.eventticketapp.data.model.Resource
import com.example.eventticketapp.data.model.Ticket
import com.example.eventticketapp.ui.components.EventCard
import com.example.eventticketapp.ui.components.ShimmerEventCard
import com.example.eventticketapp.ui.components.TicketCard
import com.example.eventticketapp.ui.navigation.Screen
import com.example.eventticketapp.util.Constants
import com.example.eventticketapp.util.DateTimeUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel(),
    dateTimeUtils: DateTimeUtils = DateTimeUtils()
) {
    val scope = rememberCoroutineScope()

    val currentUser by viewModel.currentUser.collectAsState()
    val userEvents by viewModel.userEvents.collectAsState()
    val publicEvents by viewModel.publicEvents.collectAsState()
    val userTickets by viewModel.userTickets.collectAsState()

    // State
    var selectedTabIndex by remember { mutableStateOf(0) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    // Filter state
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    val categories = remember { Constants.EVENT_CATEGORIES }

    // Pull to refresh
    val pullRefreshState = rememberPullToRefreshState()
    if (pullRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            viewModel.refreshData()
            delay(1500) // Add a small delay to make the refresh animation visible
            pullRefreshState.endRefresh()
        }
    }

    // Animation state
    val itemVisibleState = remember {
        MutableTransitionState(false).apply {
            targetState = true
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.signOut()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("No")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Event Ticket Generator",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            if (selectedTabIndex == 1 && viewModel.isUserOrganizer()) {
                ExtendedFloatingActionButton(
                    onClick = { navController.navigate(Screen.CreateEvent.route) },
                    icon = { Icon(Icons.Default.Add, contentDescription = "Create Event") },
                    text = { Text("Create Event") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Explore, contentDescription = "Explore") },
                    label = { Text("Explore") },
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Outlined.EventNote, contentDescription = "My Events") },
                    label = { Text("My Events") },
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 }
                )
                NavigationBarItem(
                    icon = {
                        BadgedBox(
                            badge = {
                                if (userTickets is Resource.Success &&
                                    (userTickets.data?.size ?: 0) > 0) {
                                    Badge { Text("${userTickets.data?.size}") }
                                }
                            }
                        ) {
                            Icon(Icons.Default.ConfirmationNumber, contentDescription = "My Tickets")
                        }
                    },
                    label = { Text("My Tickets") },
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    selected = selectedTabIndex == 3,
                    onClick = { selectedTabIndex = 3 }
                )
            }
        },
        modifier = Modifier.nestedScroll(pullRefreshState.nestedScrollConnection)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTabIndex) {
                0 -> {
                    // Explore Tab
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Search Bar
                        SearchBar(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            onSearch = {
                                viewModel.searchEvents(it)
                                isSearchActive = false
                            },
                            active = isSearchActive,
                            onActiveChange = { isSearchActive = it },
                            placeholder = { Text("Search events...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                            trailingIcon = {
                                IconButton(onClick = { /* Show filters */ }) {
                                    Icon(Icons.Default.FilterList, contentDescription = "Filter")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // Search suggestions go here (if needed)
                        }

                        // Category Filter Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 8.dp)
                        ) {
                            categories.take(5).forEach { category ->
                                FilterChip(
                                    selected = selectedCategory == category,
                                    onClick = {
                                        selectedCategory = if (selectedCategory == category) null else category
                                    },
                                    label = { Text(category) },
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }
                        }

                        // Events list with pull-to-refresh
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            when (publicEvents) {
                                is Resource.Loading -> {
                                    LazyColumn(
                                        contentPadding = PaddingValues(16.dp),
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        items(5) {
                                            ShimmerEventCard()
                                            Spacer(modifier = Modifier.height(16.dp))
                                        }
                                    }
                                }
                                is Resource.Success -> {
                                    val events = publicEvents.data ?: emptyList()
                                    val filteredEvents = if (selectedCategory != null) {
                                        events.filter { it.category == selectedCategory }
                                    } else events

                                    if (filteredEvents.isEmpty()) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("No events found")
                                        }
                                    } else {
                                        LazyColumn(
                                            contentPadding = PaddingValues(16.dp),
                                            state = rememberLazyListState()
                                        ) {
                                            items(filteredEvents) { event ->
                                                AnimatedVisibility(
                                                    visibleState = itemVisibleState,
                                                    enter = fadeIn(
                                                        animationSpec = tween(durationMillis = 300)
                                                    ) + slideInVertically(
                                                        animationSpec = tween(durationMillis = 300)
                                                    ),
                                                    exit = fadeOut() + slideOutVertically()
                                                ) {
                                                    EventCard(
                                                        event = event,
                                                        dateTimeUtils = dateTimeUtils,
                                                        onClick = {
                                                            navController.navigate(Screen.EventDetails.createRoute(event.id))
                                                        }
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(16.dp))
                                            }
                                        }
                                    }
                                }
                                is Resource.Error -> {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = publicEvents.message ?: "An error occurred",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }

                            // Pull-to-refresh indicator
                            PullToRefreshContainer(
                                state = pullRefreshState,
                                modifier = Modifier.align(Alignment.TopCenter)
                            )
                        }
                    }
                }
                1 -> {
                    // My Events Tab (for organizers)
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "My Events",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(16.dp)
                        )

                        when (userEvents) {
                            is Resource.Loading -> {
                                LazyColumn(
                                    contentPadding = PaddingValues(16.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(3) {
                                        ShimmerEventCard()
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                }
                            }
                            is Resource.Success -> {
                                val events = userEvents.data ?: emptyList()
                                if (events.isEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("No events yet")
                                            if (viewModel.isUserOrganizer()) {
                                                Spacer(modifier = Modifier.height(16.dp))
                                                Button(
                                                    onClick = { navController.navigate(Screen.CreateEvent.route) }
                                                ) {
                                                    Text("Create your first event")
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    LazyColumn(
                                        contentPadding = PaddingValues(16.dp),
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        items(events) { event ->
                                            EventCard(
                                                event = event,
                                                dateTimeUtils = dateTimeUtils,
                                                onClick = {
                                                    navController.navigate(Screen.EventDetails.createRoute(event.id))
                                                }
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                        }
                                    }
                                }
                            }
                            is Resource.Error -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = userEvents.message ?: "An error occurred",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // My Tickets Tab
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "My Tickets",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(16.dp)
                        )

                        when (userTickets) {
                            is Resource.Loading -> {
                                LazyColumn(
                                    contentPadding = PaddingValues(16.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(3) {
                                        ShimmerEventCard()
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                }
                            }
                            is Resource.Success -> {
                                val tickets = userTickets.data ?: emptyList()
                                if (tickets.isEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("No tickets yet")
                                    }
                                } else {
                                    LazyColumn(
                                        contentPadding = PaddingValues(16.dp),
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        items(tickets) { ticket ->
                                            TicketCard(
                                                ticket = ticket,
                                                onClick = {
                                                    navController.navigate(Screen.TicketDetails.createRoute(ticket.id))
                                                }
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                        }
                                    }
                                }
                            }
                            is Resource.Error -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = userTickets.message ?: "An error occurred",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
                3 -> {
                    // Profile Tab
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Profile",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )

                        currentUser?.let { user ->
                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "${user.firstName} ${user.lastName}",
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = user.email,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = if (user.isOrganizer) "Organizer Account" else "Regular Account",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (user.isOrganizer)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = { showLogoutDialog = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.Default.ExitToApp,
                                    contentDescription = "Logout",
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text("Logout")
                            }
                        }
                    }
                }
            }
        }
    }
}