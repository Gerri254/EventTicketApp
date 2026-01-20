package com.example.eventticketapp.ui.tickets.preview

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.eventticketapp.R
import com.example.eventticketapp.data.model.Resource
import com.example.eventticketapp.ui.navigation.Screen
import com.example.eventticketapp.ui.theme.TicketShape
import com.example.eventticketapp.ui.theme.freeTicketColor
import com.example.eventticketapp.ui.theme.regularTicketColor
import com.example.eventticketapp.ui.theme.vipTicketColor
import com.example.eventticketapp.util.DateTimeUtils
import com.example.eventticketapp.util.PDFExporter
import com.example.eventticketapp.util.TicketSharingUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketPreviewScreen(
    navController: NavController,
    eventId: String,
    ticketId: String,
    viewModel: TicketPreviewViewModel = hiltViewModel(),
    dateTimeUtils: DateTimeUtils = DateTimeUtils(),
    pdfExporter: PDFExporter = PDFExporter(dateTimeUtils),
    ticketSharingUtil: TicketSharingUtil = TicketSharingUtil(pdfExporter, viewModel.qrGeneratorUtil, dateTimeUtils)
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    val ticketState by viewModel.ticket.collectAsState()
    val eventState by viewModel.event.collectAsState()
    val qrCodeBitmap by viewModel.qrCodeBitmap.collectAsState()

    var pdfUri by remember { mutableStateOf<Uri?>(null) }
    var showShareSheet by remember { mutableStateOf(false) }
    var showSuccessAnimation by remember { mutableStateOf(false) }

    // QR code animation
    var showQr by remember { mutableStateOf(false) }
    val qrAlpha by animateFloatAsState(
        targetValue = if (showQr) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "qr_alpha"
    )

    LaunchedEffect(ticketId) {
        viewModel.loadTicket(ticketId)
        delay(800) // Delay for visual effect
        showQr = true
    }

    // Share bottom sheet state
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Ticket") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                // Loading state
                ticketState is Resource.Loading || eventState is Resource.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                // Error state
                ticketState is Resource.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Error: ${(ticketState as Resource.Error).message}",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                eventState is Resource.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Error loading event: ${(eventState as Resource.Error).message}",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                // Success state - display ticket
                ticketState is Resource.Success && eventState is Resource.Success -> {
                    val ticket = (ticketState as Resource.Success).data!!
                    val event = (eventState as Resource.Success).data!!

                    // Determine ticket type color based on ticket type ID
                    val ticketColor = when {
                        ticket.ticketTypeId.contains("vip", ignoreCase = true) -> vipTicketColor
                        ticket.ticketTypeId.contains("free", ignoreCase = true) -> freeTicketColor
                        else -> regularTicketColor
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Ticket Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(
                                    elevation = 8.dp,
                                    shape = TicketShape,
                                    spotColor = ticketColor.copy(alpha = 0.3f)
                                ),
                            shape = TicketShape,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Header with event info
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(ticketColor.copy(alpha = 0.1f))
                                        .padding(16.dp)
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = event.title,
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Event details in a row
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.CalendarToday,
                                                contentDescription = "Date",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )

                                            Spacer(modifier = Modifier.width(4.dp))

                                            Text(
                                                text = dateTimeUtils.formatDate(event.date),
                                                style = MaterialTheme.typography.bodyMedium
                                            )

                                            Spacer(modifier = Modifier.width(16.dp))

                                            Icon(
                                                Icons.Default.LocationOn,
                                                contentDescription = "Location",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )

                                            Spacer(modifier = Modifier.width(4.dp))

                                            Text(
                                                text = event.location,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Ticket Type Badge
                                Surface(
                                    color = ticketColor.copy(alpha = 0.1f),
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        text = ticket.ticketTypeId.uppercase().replace("_", " "),
                                        color = ticketColor,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                // QR Code with animation
                                Box(
                                    modifier = Modifier
                                        .size(250.dp)
                                        .alpha(qrAlpha)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White)
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    qrCodeBitmap?.let {
                                        androidx.compose.foundation.Image(
                                            bitmap = it.asImageBitmap(),
                                            contentDescription = "Ticket QR Code",
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                // Ticket Status
                                val statusBgColor = if (ticket.isScanned) {
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                                } else {
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                                }

                                val statusText = if (ticket.isScanned) "USED" else "VALID"
                                val statusColor = if (ticket.isScanned) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.tertiary
                                }

                                Surface(
                                    color = statusBgColor,
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        text = statusText,
                                        color = statusColor,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Ticket ID
                                Text(
                                    text = "Ticket ID: ${ticket.id}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                // Present this ticket at the entrance
                                Text(
                                    text = "Present this ticket at the entrance",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Action buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Save as PDF button
                            FilledTonalButton(
                                onClick = {
                                    // Generate PDF if not yet generated
                                    if (pdfUri == null && qrCodeBitmap != null) {
                                        scope.launch {
                                            pdfUri = pdfExporter.exportTicketToPdf(
                                                context,
                                                ticket,
                                                event,
                                                qrCodeBitmap!!
                                            )

                                            // Show success animation
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            showSuccessAnimation = true
                                            delay(1500)
                                            showSuccessAnimation = false

                                            // Open PDF viewer
                                            pdfUri?.let {
                                                val intent = Intent(Intent.ACTION_VIEW)
                                                intent.setDataAndType(it, "application/pdf")
                                                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                                context.startActivity(intent)
                                            }
                                        }
                                    } else {
                                        // Open PDF viewer if already generated
                                        pdfUri?.let {
                                            val intent = Intent(Intent.ACTION_VIEW)
                                            intent.setDataAndType(it, "application/pdf")
                                            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                            context.startActivity(intent)
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Default.Download,
                                    contentDescription = "Download",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Save as PDF")
                            }

                            // Share button
                            Button(
                                onClick = { showShareSheet = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Default.Share,
                                    contentDescription = "Share",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Share Ticket")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // View Ticket Button
                        Button(
                            onClick = {
                                navController.navigate(Screen.TicketViewer.createRoute(ticket.id))
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("View Ticket")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Add to calendar button
                        OutlinedButton(
                            onClick = {
                                // Create calendar intent
                                val intent = Intent(Intent.ACTION_INSERT).apply {
                                    data = android.provider.CalendarContract.Events.CONTENT_URI
                                    putExtra(android.provider.CalendarContract.Events.TITLE, event.title)
                                    putExtra(android.provider.CalendarContract.Events.DESCRIPTION, event.description)
                                    putExtra(android.provider.CalendarContract.Events.EVENT_LOCATION, event.location)
                                    putExtra(android.provider.CalendarContract.EXTRA_EVENT_BEGIN_TIME, event.date.time)
                                    putExtra(android.provider.CalendarContract.EXTRA_EVENT_END_TIME, event.date.time + 3600000) // +1 hour
                                }
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = "Calendar",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add to Calendar")
                        }
                    }

                    // Share bottom sheet
                    if (showShareSheet) {
                        ModalBottomSheet(
                            onDismissRequest = { showShareSheet = false },
                            sheetState = bottomSheetState,
                            dragHandle = { BottomSheetDefaults.DragHandle() }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Share Ticket",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                // Share options
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    // WhatsApp
                                    ShareOption(
                                        icon = R.drawable.ic_whatsapp,
                                        label = "WhatsApp",
                                        onClick = {
                                            if (pdfUri == null && qrCodeBitmap != null) {
                                                scope.launch {
                                                    pdfUri = pdfExporter.exportTicketToPdf(
                                                        context,
                                                        ticket,
                                                        event,
                                                        qrCodeBitmap!!
                                                    )

                                                    pdfUri?.let {
                                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                            type = "application/pdf"
                                                            putExtra(Intent.EXTRA_STREAM, it)
                                                            setPackage("com.whatsapp")
                                                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                                        }

                                                        try {
                                                            context.startActivity(shareIntent)
                                                        } catch (e: Exception) {
                                                            // WhatsApp not installed
                                                            val generalShareIntent = Intent(Intent.ACTION_SEND).apply {
                                                                type = "application/pdf"
                                                                putExtra(Intent.EXTRA_STREAM, it)
                                                                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                                            }
                                                            context.startActivity(Intent.createChooser(generalShareIntent, "Share Ticket"))
                                                        }
                                                    }
                                                }
                                            }
                                            showShareSheet = false
                                        }
                                    )

                                    // Email
                                    ShareOption(
                                        icon = R.drawable.ic_email,
                                        label = "Email",
                                        onClick = {
                                            if (pdfUri == null && qrCodeBitmap != null) {
                                                scope.launch {
                                                    pdfUri = pdfExporter.exportTicketToPdf(
                                                        context,
                                                        ticket,
                                                        event,
                                                        qrCodeBitmap!!
                                                    )

                                                    pdfUri?.let {
                                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                            type = "application/pdf"
                                                            putExtra(Intent.EXTRA_SUBJECT, "Ticket for ${event.title}")
                                                            putExtra(Intent.EXTRA_TEXT, "Your ticket for ${event.title} is attached.")
                                                            putExtra(Intent.EXTRA_STREAM, it)
                                                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                                        }
                                                        context.startActivity(Intent.createChooser(shareIntent, "Send Email"))
                                                    }
                                                }
                                            }
                                            showShareSheet = false
                                        }
                                    )

                                    // More
                                    ShareOption(
                                        icon = R.drawable.ic_more,
                                        label = "More",
                                        onClick = {
                                            if (pdfUri == null && qrCodeBitmap != null) {
                                                scope.launch {
                                                    pdfUri = pdfExporter.exportTicketToPdf(
                                                        context,
                                                        ticket,
                                                        event,
                                                        qrCodeBitmap!!
                                                    )

                                                    pdfUri?.let {
                                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                            type = "application/pdf"
                                                            putExtra(Intent.EXTRA_STREAM, it)
                                                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                                        }
                                                        context.startActivity(Intent.createChooser(shareIntent, "Share Ticket"))
                                                    }
                                                }
                                            } else {
                                                pdfUri?.let {
                                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                        type = "application/pdf"
                                                        putExtra(Intent.EXTRA_STREAM, it)
                                                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                                    }
                                                    context.startActivity(Intent.createChooser(shareIntent, "Share Ticket"))
                                                }
                                            }
                                            showShareSheet = false
                                        }
                                    )
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                Button(
                                    onClick = { showShareSheet = false },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Cancel")
                                }

                                Spacer(modifier = Modifier.height(24.dp))
                            }
                        }
                    }
                }
            }

            // Success animation overlay
            AnimatedVisibility(
                visible = showSuccessAnimation,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Lottie success animation
                        val composition by rememberLottieComposition(
                            LottieCompositionSpec.RawRes(R.raw.success_animation)
                        )
                        LottieAnimation(
                            composition = composition,
                            iterations = 1,
                            modifier = Modifier.size(200.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "PDF Saved Successfully!",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ShareOption(
    icon: Int,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = androidx.compose.ui.res.painterResource(id = icon),
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}