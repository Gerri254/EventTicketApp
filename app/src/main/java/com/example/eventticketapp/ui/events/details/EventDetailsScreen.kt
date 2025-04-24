import android.app.AlertDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.eventticketapp.data.model.Event
import com.example.eventticketapp.data.model.Resource
import com.example.eventticketapp.data.model.TicketType
import com.example.eventticketapp.ui.events.details.EventDetailsViewModel
import com.example.eventticketapp.ui.navigation.Screen
import com.example.eventticketapp.util.DateTimeUtils
import kotlinx.coroutines.launch

@Composable
fun EventDetailsScreen(
    navController: NavController,
    eventId: String,
    viewModel: EventDetailsViewModel = hiltViewModel(),
    dateTimeUtils: DateTimeUtils = DateTimeUtils()
) {
    val context = LocalContext.current
    val scaffoldState = rememberScaffoldState()
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
                scope.launch {
                    scaffoldState.snackbarHostState.showSnackbar(errorMsg)
                }
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
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedTicketType?.id == ticketType.id,
                                    onClick = { if (isEnabled) viewModel.selectTicketType(ticketType) },
                                    enabled = isEnabled
                                )
                                Column(
                                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                                ) {
                                    Text(
                                        text = ticketType.name,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isEnabled) Color.Black else Color.Gray
                                    )
                                    if (ticketType.description.isNotEmpty()) {
                                        Text(
                                            text = ticketType.description,
                                            style = MaterialTheme.typography.caption,
                                            color = if (isEnabled) Color.Black else Color.Gray
                                        )
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "$${String.format("%.2f", ticketType.price)}",
                                        fontWeight = FontWeight.Bold,
                                        color = if (isEnabled) MaterialTheme.colors.primary else Color.Gray
                                    )
                                    Text(
                                        text = "${ticketType.availableQuantity} available",
                                        style = MaterialTheme.typography.caption,
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
                        enabled = selectedTicketType != null && selectedTicketType?.availableQuantity ?: 0 > 0
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
        scaffoldState = scaffoldState,
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
                        IconButton(onClick = { navController.navigate(Screen.CreateEvent.route) }) {
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
            modifier = Modifier.fillMaxSize().padding(paddingValues)
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
                        color = MaterialTheme.colors.error
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
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colors.primary)
                }
            }
        }
    }
}
