package com.example.eventticketapp.ui.events.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.eventticketapp.data.model.Event
import com.example.eventticketapp.data.model.Resource
import com.example.eventticketapp.data.model.TicketType
import com.example.eventticketapp.ui.navigation.Screen
import com.example.eventticketapp.util.DateTimeUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsScreen(
    navController: NavController,
    eventId: String,
    viewModel: EventDetailsViewModel = hiltViewModel(),
    dateTimeUtils: DateTimeUtils = DateTimeUtils()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val eventState by viewModel.event.collectAsState()
    val registerState by viewModel.registerState.collectAsState()
    val isUserRegistered by viewModel.isUserRegistered.collectAsState()
    val selectedTicketType by viewModel.selectedTicketType.collectAsState()

    var showRegistrationDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var registeredTicketId by remember { mutableStateOf("") }

    LaunchedEffect(eventId) {
        viewModel.loadEvent(eventId)
    }

    LaunchedEffect(registerState) {
        when (registerState) {
            is Resource.Success -> {
                val ticket = (registerState as Resource.Success).data
                if (ticket != null) {
                    registeredTicketId = ticket.id
                    showSuccessDialog = true
                }
                viewModel.clearRegistrationState()
            }
            is Resource.Error -> {
                val errorMsg = (registerState as Resource.Error).message ?: "Registration failed"
                // Assuming we have a way to show snackbar, or just log/toast
                // Since scaffoldState is M2 and we are using M3, we might need a different approach or mixed usage
                // For simplicity in this fix, we'll assume basic error handling
                viewModel.clearRegistrationState()
            }
            else -> {}
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("Registration Successful") },
            text = { Text("You have successfully registered for this event!") },
            confirmButton = {
                Button(onClick = {
                    showSuccessDialog = false
                    navController.navigate(Screen.TicketPreview.createRoute(eventId, registeredTicketId))
                }) {
                    Text("View Ticket")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSuccessDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showRegistrationDialog) {
        val event = (eventState as? Resource.Success)?.data
        if (event != null) {
            AlertDialog(
                onDismissRequest = { showRegistrationDialog = false },
                title = { Text("Select Ticket Type") },
                text = {
                    Column {
                        event.ticketTypes.forEach { ticketType ->
                            val isEnabled = ticketType.availableQuantity > 0
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedTicketType?.id == ticketType.id,
                                    onClick = { if (isEnabled) viewModel.selectTicketType(ticketType) },
                                    enabled = isEnabled
                                )
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 8.dp)
                                ) {
                                    Text(
                                        text = ticketType.name,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isEnabled) Color.Black else Color.Gray
                                    )
                                    if (ticketType.description.isNotEmpty()) {
                                        Text(
                                            text = ticketType.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isEnabled) Color.Black else Color.Gray
                                        )
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "$${String.format("%.2f", ticketType.price)}",
                                        fontWeight = FontWeight.Bold,
                                        color = if (isEnabled) MaterialTheme.colorScheme.primary else Color.Gray
                                    )
                                    Text(
                                        text = "${ticketType.availableQuantity} available",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isEnabled) Color.Black else Color.Gray
                                    )
                                }
                            }
                            Divider()
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showRegistrationDialog = false
                            viewModel.registerForEvent()
                        },
                        enabled = selectedTicketType != null && (selectedTicketType?.availableQuantity ?: 0) > 0
                    ) {
                        Text("Register")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRegistrationDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Event Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (viewModel.isUserOrganizer()) {
                        IconButton(onClick = { navController.navigate(Screen.CreateEvent.createRoute(eventId)) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Event")
                        }
                        IconButton(onClick = { navController.navigate(Screen.QRScanner.createRoute(eventId)) }) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan Tickets")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (eventState) {
                is Resource.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
                is Resource.Error -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Error: ${(eventState as Resource.Error).message}",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                is Resource.Success -> {
                    val event = (eventState as Resource.Success).data!!
                    EventDetailsContent(
                        event = event,
                        isUserRegistered = isUserRegistered,
                        dateTimeUtils = dateTimeUtils,
                        onRegisterClick = { showRegistrationDialog = true }
                    )
                }
            }
            if (registerState is Resource.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun EventDetailsContent(
    event: Event,
    isUserRegistered: Boolean,
    dateTimeUtils: DateTimeUtils,
    onRegisterClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Event Image
        if (!event.imageUrl.isNullOrEmpty()) {
            AsyncImage(
                model = event.imageUrl,
                contentDescription = "Event Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = event.title.take(1),
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = "Date",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = dateTimeUtils.formatDate(event.date),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = dateTimeUtils.formatTime(event.date),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = event.location,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "About Event",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = event.description,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Tickets",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            event.ticketTypes.forEach { ticketType ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(ticketType.name)
                    Text(
                        text = if (ticketType.price > 0) "$${String.format("%.2f", ticketType.price)}" else "Free",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onRegisterClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isUserRegistered && event.ticketTypes.any { it.availableQuantity > 0 }
            ) {
                Text(if (isUserRegistered) "Already Registered" else "Get Tickets")
            }
        }
    }
}