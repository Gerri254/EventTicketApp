package com.example.eventticketapp.ui.events.create

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.rememberScaffoldState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.eventticketapp.data.model.Resource
import com.example.eventticketapp.data.model.TicketType
import com.example.eventticketapp.ui.navigation.Screen
import com.example.eventticketapp.util.Constants
import kotlinx.coroutines.launch

@Composable
fun TicketTypeSetupScreen(
    navController: NavController,
    eventId: String,
    viewModel: CreateEventViewModel = hiltViewModel()
) {
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    val ticketTypes by viewModel.ticketTypes.collectAsState()
    val createEventState by viewModel.createEventState.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var ticketTypeToEdit by remember { mutableStateOf<TicketType?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Load event data
    LaunchedEffect(eventId) {
        viewModel.loadEvent(eventId)
    }

    // Handle state changes
    LaunchedEffect(createEventState) {
        when (createEventState) {
            is Resource.Success -> {
                viewModel.clearStates()
                // Navigate to event details or home
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.CreateEvent.route) { inclusive = true }
                }
            }
            is Resource.Error -> {
                val errorMsg = (createEventState as Resource.Error).message ?: "Failed to save ticket types"
                scope.launch {
                    scaffoldState.snackbarHostState.showSnackbar(errorMsg)
                }
                viewModel.clearStates()
            }
            is Resource.Loading -> {
                isLoading = true
            }
            null -> {
                isLoading = false
            }
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text("Set Up Ticket Types") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Ticket Type")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (ticketTypes.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No ticket types added yet",
                        style = MaterialTheme.typography.h6
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Add at least one ticket type for your event",
                        style = MaterialTheme.typography.body1
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showAddDialog = true }
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.padding(4.dp))
                        Text("Add Ticket Type")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ticketTypes) { ticketType ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = 4.dp,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = ticketType.name,
                                        style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "Price: Ksh ${ticketType.price}",
                                        style = MaterialTheme.typography.body2
                                    )
                                    Text(
                                        text = "Quantity: ${ticketType.quantity}",
                                        style = MaterialTheme.typography.body2
                                    )
                                }
                                Row {
                                    IconButton(onClick = {
                                        ticketTypeToEdit = ticketType
                                        showAddDialog = true
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                                    }
                                    IconButton(onClick = {
                                        viewModel.deleteTicketType(ticketType)
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (showAddDialog) {
                AddEditTicketTypeDialog(
                    ticketType = ticketTypeToEdit,
                    onDismiss = {
                        showAddDialog = false
                        ticketTypeToEdit = null
                    },
                    onSave = { type ->
                        if (ticketTypeToEdit == null) {
                            viewModel.addTicketType(type)
                        } else {
                            viewModel.updateTicketType(type)
                        }
                        showAddDialog = false
                        ticketTypeToEdit = null
                    }
                )
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun AddEditTicketTypeDialog(
    ticketType: TicketType?,
    onDismiss: () -> Unit,
    onSave: (TicketType) -> Unit
) {
    var name by remember { mutableStateOf(ticketType?.name ?: "") }
    var price by remember { mutableStateOf(ticketType?.price?.toString() ?: "") }
    var quantity by remember { mutableStateOf(ticketType?.quantity?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (ticketType == null) "Add Ticket Type" else "Edit Ticket Type") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val finalTicket = TicketType(
                        id = ticketType?.id ?: "",
                        name = name.trim(),
                        price = price.toDoubleOrNull() ?: 0.0,
                        quantity = quantity.toIntOrNull() ?: 0
                    )
                    onSave(finalTicket)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
