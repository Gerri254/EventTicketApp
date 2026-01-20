package com.example.eventticketapp.ui.events.create

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.eventticketapp.data.model.Resource
import com.example.eventticketapp.ui.navigation.Screen
import com.example.eventticketapp.util.DateTimeUtils
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

@Composable
fun CreateEventScreen(
    navController: NavController,
    viewModel: CreateEventViewModel = hiltViewModel(),
    dateTimeUtils: DateTimeUtils = DateTimeUtils()
) {
    val context = LocalContext.current
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    val createEventState by viewModel.createEventState.collectAsState()
    val imageUploadState by viewModel.imageUploadState.collectAsState()
    val eventCategories by viewModel.eventCategories.collectAsState()
    val currentEvent by viewModel.currentEvent.collectAsState()

    var title by remember { mutableStateOf(currentEvent?.title ?: "") }
    var description by remember { mutableStateOf(currentEvent?.description ?: "") }
    var location by remember { mutableStateOf(currentEvent?.location ?: "") }
    var selectedCategory by remember { mutableStateOf(currentEvent?.category ?: eventCategories.firstOrNull() ?: "") }
    var isPublic by remember { mutableStateOf(currentEvent?.isPublic ?: true) }
    var eventDate by remember { mutableStateOf(currentEvent?.date ?: Date()) }
    var imageUrl by remember { mutableStateOf(currentEvent?.imageUrl) }
    
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.uploadImage(it) }
    }

    // Date and time picker
    val calendar = Calendar.getInstance()
    calendar.time = eventDate
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)

    val datePickerDialog = DatePickerDialog(
        context,
        { _, selectedYear, selectedMonth, selectedDay ->
            calendar.set(Calendar.YEAR, selectedYear)
            calendar.set(Calendar.MONTH, selectedMonth)
            calendar.set(Calendar.DAY_OF_MONTH, selectedDay)

            // After date is set, show time picker
            TimePickerDialog(
                context,
                { _, selectedHour, selectedMinute ->
                    calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                    calendar.set(Calendar.MINUTE, selectedMinute)
                    eventDate = calendar.time
                },
                hour, minute, false
            ).show()
        },
        year, month, day
    )

    // Handle image upload state
    LaunchedEffect(imageUploadState) {
        when (imageUploadState) {
            is Resource.Success -> {
                val url = (imageUploadState as Resource.Success<String>).data
                imageUrl = url
                viewModel.setImageUrl(url!!)
                viewModel.clearStates()
                scope.launch {
                    scaffoldState.snackbarHostState.showSnackbar("Image uploaded successfully")
                }
            }
            is Resource.Error -> {
                val errorMsg = (imageUploadState as Resource.Error).message ?: "Upload failed"
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

    // Handle event creation state
    LaunchedEffect(createEventState) {
        when (createEventState) {
            is Resource.Success -> {
                val eventId = (createEventState as Resource.Success<String>).data ?: ""
                viewModel.clearStates()

                // Navigate to ticket type setup
                navController.navigate(Screen.TicketTypeSetup.createRoute(eventId))
            }
            is Resource.Error -> {
                val errorMsg = (createEventState as Resource.Error).message ?: "Creation failed"
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

    // Update fields if currentEvent changes
    LaunchedEffect(currentEvent) {
        currentEvent?.let {
            title = it.title
            description = it.description
            location = it.location
            selectedCategory = it.category
            isPublic = it.isPublic
            eventDate = it.date
            imageUrl = it.imageUrl
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text(text = if (currentEvent != null) "Edit Event" else "Create Event") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Event image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(MaterialTheme.colors.surface)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (!imageUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "Event Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(getCategoryColor(selectedCategory)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (title.isNotEmpty()) title.first().toString() else "E",
                                style = MaterialTheme.typography.h2,
                                color = Color.White
                            )
                        }
                    }
                    
                    if (isLoading && imageUploadState is Resource.Loading) {
                        CircularProgressIndicator()
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = imageUrl ?: "",
                    onValueChange = { imageUrl = it },
                    label = { Text("Image URL (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter an image URL or upload one") }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Event Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Event Title") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Event Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Event Location
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.LocationOn, contentDescription = "Location")
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Event Date and Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = "Date")

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Date & Time: ${dateTimeUtils.formatDate(eventDate)} at ${dateTimeUtils.formatTime(eventDate)}",
                        modifier = Modifier.weight(1f)
                    )

                    Button(onClick = { datePickerDialog.show() }) {
                        Text("Change")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Event Category
                Box {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = { },
                        label = { Text("Category") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showCategoryDropdown = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Category")
                            }
                        }
                    )

                    DropdownMenu(
                        expanded = showCategoryDropdown,
                        onDismissRequest = { showCategoryDropdown = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        eventCategories.forEach { category ->
                            DropdownMenuItem(onClick = {
                                selectedCategory = category
                                showCategoryDropdown = false
                            }) {
                                Text(text = category)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Public Event Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Public Event")

                    Switch(
                        checked = isPublic,
                        onCheckedChange = { isPublic = it }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Create/Update Button
                Button(
                    onClick = {
                        if (validateInputs(context, title, description, location)) {
                            viewModel.createEvent(
                                title = title,
                                description = description,
                                location = location,
                                date = eventDate,
                                category = selectedCategory,
                                isPublic = isPublic,
                                imageUrl = imageUrl
                            )
                        } else {
                            scope.launch {
                                scaffoldState.snackbarHostState.showSnackbar("Please fill all required fields")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    if (isLoading && createEventState is Resource.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colors.onPrimary
                        )
                    } else {
                        Text(text = if (currentEvent != null) "Update Event" else "Create Event")
                    }
                }
            }
        }
    }
}

private fun getCategoryColor(category: String): Color {
    return when(category) {
        "Concert" -> Color(0xFF5E35B1)
        "Conference" -> Color(0xFF0288D1)
        "Festival" -> Color(0xFFEF6C00)
        else -> Color(0xFF2E7D32)
    }
}

private fun validateInputs(
    context: Context,
    title: String,
    description: String,
    location: String
): Boolean {
    return title.isNotBlank() && description.isNotBlank() && location.isNotBlank()
}