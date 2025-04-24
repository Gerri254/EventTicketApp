package com.example.eventticketapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.eventticketapp.ui.auth.LoginScreen
import com.example.eventticketapp.ui.auth.SignupScreen
import com.example.eventticketapp.ui.events.create.CreateEventScreen
import com.example.eventticketapp.ui.events.create.TicketTypeSetupScreen
import com.example.eventticketapp.ui.events.details.EventDetailsScreen
import com.example.eventticketapp.ui.home.HomeScreen
import com.example.eventticketapp.ui.splash.SplashScreen
import com.example.eventticketapp.ui.tickets.preview.TicketPreviewScreen
import com.example.eventticketapp.ui.tickets.scanner.QRScannerScreen
import com.example.eventticketapp.ui.tickets.viewer.TicketViewerScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Signup : Screen("signup")
    object Home : Screen("home")
    object EventDetails : Screen("event_details/{eventId}") {
        fun createRoute(eventId: String) = "event_details/$eventId"
    }
    object CreateEvent : Screen("create_event")
    object TicketTypeSetup : Screen("ticket_type_setup/{eventId}") {
        fun createRoute(eventId: String) = "ticket_type_setup/$eventId"
    }
    object TicketPreview : Screen("ticket_preview/{eventId}/{ticketId}") {
        fun createRoute(eventId: String, ticketId: String) = "ticket_preview/$eventId/$ticketId"
    }
    object TicketViewer : Screen("ticket_viewer/{ticketId}") {
        fun createRoute(ticketId: String) = "ticket_viewer/$ticketId"
    }
    object QRScanner : Screen("qr_scanner/{eventId}") {
        fun createRoute(eventId: String) = "qr_scanner/$eventId"
    }
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }

        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }

        composable(Screen.Signup.route) {
            SignupScreen(navController = navController)
        }

        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }

        composable(Screen.EventDetails.route) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            EventDetailsScreen(
                navController = navController,
                eventId = eventId
            )
        }

        composable(Screen.CreateEvent.route) {
            CreateEventScreen(navController = navController)
        }

        composable(Screen.TicketTypeSetup.route) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            TicketTypeSetupScreen(
                navController = navController,
                eventId = eventId
            )
        }

        composable(Screen.TicketPreview.route) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            val ticketId = backStackEntry.arguments?.getString("ticketId") ?: ""
            TicketPreviewScreen(
                navController = navController,
                eventId = eventId,
                ticketId = ticketId
            )
        }

        composable(Screen.TicketViewer.route) { backStackEntry ->
            val ticketId = backStackEntry.arguments?.getString("ticketId") ?: ""
            TicketViewerScreen(
                navController = navController,
                ticketId = ticketId
            )
        }

        composable(Screen.QRScanner.route) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            QRScannerScreen(
                navController = navController,
                eventId = eventId
            )
        }
    }
}