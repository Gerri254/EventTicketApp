package com.example.eventticketapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.eventticketapp.data.model.Ticket
import com.example.eventticketapp.ui.theme.TicketShape
import com.example.eventticketapp.ui.theme.freeTicketColor
import com.example.eventticketapp.ui.theme.regularTicketColor
import com.example.eventticketapp.ui.theme.vipTicketColor
import kotlinx.coroutines.delay

@Composable
fun TicketCard(
    ticket: Ticket,
    onClick: () -> Unit,
    eventTitle: String? = null
) {
    var eventName by remember { mutableStateOf(eventTitle ?: "Loading...") }
    var ticketType by remember { mutableStateOf("") }

    // In a real app, you would fetch the event and ticket type details
    // This is just a placeholder for the animation
    LaunchedEffect(ticket) {
        delay(500)
        if (eventTitle == null) {
            eventName = "Event #${ticket.eventId.takeLast(4)}"
        }

        ticketType = when {
            ticket.ticketTypeId.contains("vip", ignoreCase = true) -> "VIP"
            ticket.ticketTypeId.contains("free", ignoreCase = true) -> "FREE"
            else -> "REGULAR"
        }
    }

    val ticketColor = when (ticketType) {
        "VIP" -> vipTicketColor
        "FREE" -> freeTicketColor
        else -> regularTicketColor
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = TicketShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column {
            // Ticket header with color
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ticketColor.copy(alpha = 0.2f))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = eventName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Ticket #${ticket.id.takeLast(8).toUpperCase()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(ticketColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ConfirmationNumber,
                            contentDescription = "Ticket",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Ticket body
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Ticket type badge
                    Surface(
                        color = ticketColor.copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = ticketType,
                            color = ticketColor,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Ticket status
                    Surface(
                        color = if (ticket.isScanned)
                            MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                        else
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = if (ticket.isScanned) "USED" else "VALID",
                            color = if (ticket.isScanned)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.tertiary,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onClick
                    ) {
                        Text("View Ticket")
                    }
                }
            }
        }
    }
}