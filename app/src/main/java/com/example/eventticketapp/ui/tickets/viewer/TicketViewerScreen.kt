package com.example.eventticketapp.ui.tickets.viewer

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.eventticketapp.data.model.Resource
import com.example.eventticketapp.util.DateTimeUtils
import com.example.eventticketapp.util.PDFExporter
import com.example.eventticketapp.util.QRGeneratorUtil
import com.example.eventticketapp.util.TicketSharingUtil
import kotlinx.coroutines.launch

@Composable
fun TicketViewerScreen(
    navController: NavController,
    ticketId: String,
    viewModel: TicketViewModel = hiltViewModel(),
    dateTimeUtils: DateTimeUtils = DateTimeUtils(),
    pdfExporter: PDFExporter = PDFExporter(dateTimeUtils),
    ticketSharingUtil: TicketSharingUtil = TicketSharingUtil(pdfExporter, QRGeneratorUtil(), dateTimeUtils)
) {
    val context = LocalContext.current
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    val ticketState by viewModel.ticket.collectAsState()
    val eventState by viewModel.event.collectAsState()
    val qrCodeBitmap by viewModel.qrCodeBitmap.collectAsState()

    var pdfUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(ticketId) {
        viewModel.loadTicket(ticketId)
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text("Ticket") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Share Button
                    if (ticketState is Resource.Success && eventState is Resource.Success && qrCodeBitmap != null) {
                        IconButton(
                            onClick = {
                                val ticket = (ticketState as Resource.Success).data!!
                                val event = (eventState as Resource.Success).data!!

                                // Generate PDF first if not yet generated
                                if (pdfUri == null) {
                                    pdfUri = pdfExporter.exportTicketToPdf(
                                        context,
                                        ticket,
                                        event,
                                        qrCodeBitmap!!
                                    )
                                }

                                // Then share it
                                pdfUri?.let {
                                    val shareIntent = ticketSharingUtil.shareTicketPdf(
                                        context,
                                        ticket,
                                        event,
                                        it
                                    )
                                    context.startActivity(shareIntent)
                                }
                            }
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share")
                        }
                    }

                    // Download PDF Button
                    if (ticketState is Resource.Success && eventState is Resource.Success && qrCodeBitmap != null) {
                        IconButton(
                            onClick = {
                                val ticket = (ticketState as Resource.Success).data!!
                                val event = (eventState as Resource.Success).data!!

                                // Generate PDF if not yet generated
                                if (pdfUri == null) {
                                    pdfUri = pdfExporter.exportTicketToPdf(
                                        context,
                                        ticket,
                                        event,
                                        qrCodeBitmap!!
                                    )
                                }

                                // Open PDF viewer
                                pdfUri?.let {
                                    val intent = Intent(Intent.ACTION_VIEW)
                                    intent.setDataAndType(it, "application/pdf")
                                    intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    context.startActivity(intent)
                                }
                            }
                        ) {
                            Icon(Icons.Default.Download, contentDescription = "Download PDF")
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
                            color = MaterialTheme.colors.error
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
                            color = MaterialTheme.colors.error
                        )
                    }
                }

                // Success state - display ticket
                ticketState is Resource.Success && eventState is Resource.Success -> {
                    val ticket = (ticketState as Resource.Success).data!!
                    val event = (eventState as Resource.Success).data!!

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Ticket Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = 4.dp,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Event Title
                                Text(
                                    text = event.title,
                                    style = MaterialTheme.typography.h5,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Event Details
                                Text(
                                    text = "Date: ${dateTimeUtils.formatDate(event.date)}",
                                    style = MaterialTheme.typography.body1
                                )

                                Text(
                                    text = "Time: ${dateTimeUtils.formatTime(event.date)}",
                                    style = MaterialTheme.typography.body1
                                )

                                Text(
                                    text = "Location: ${event.location}",
                                    style = MaterialTheme.typography.body1
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Divider()

                                Spacer(modifier = Modifier.height(16.dp))

                                // QR Code
                                qrCodeBitmap?.let {
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = Color.White,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .padding(8.dp)
                                    ) {
                                        Image(
                                            bitmap = it.asImageBitmap(),
                                            contentDescription = "Ticket QR Code",
                                            modifier = Modifier.size(250.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Ticket Status
                                val statusBgColor = if (ticket.isScanned) {
                                    Color.Red.copy(alpha = 0.2f)
                                } else {
                                    Color.Green.copy(alpha = 0.2f)
                                }

                                val statusText = if (ticket.isScanned) "USED" else "VALID"

                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = statusBgColor,
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = statusText,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Ticket ID
                                Text(
                                    text = "Ticket ID: ${ticket.id}",
                                    style = MaterialTheme.typography.caption,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Share Button
                        Button(
                            onClick = {
                                val shareIntent = ticketSharingUtil.shareTicketText(
                                    context,
                                    ticket,
                                    event
                                )
                                context.startActivity(shareIntent)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = "Share",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.padding(4.dp))
                            Text("Share Ticket Details")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Download PDF Button
                        Button(
                            onClick = {
                                // Generate PDF if not yet generated
                                if (pdfUri == null && qrCodeBitmap != null) {
                                    pdfUri = pdfExporter.exportTicketToPdf(
                                        context,
                                        ticket,
                                        event,
                                        qrCodeBitmap!!
                                    )
                                }

                                // Open PDF viewer
                                pdfUri?.let {
                                    val intent = Intent(Intent.ACTION_VIEW)
                                    intent.setDataAndType(it, "application/pdf")
                                    intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    context.startActivity(intent)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.Download,
                                contentDescription = "Download",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.padding(4.dp))
                            Text("Save as PDF")
                        }
                    }
                }
            }
        }
    }
}