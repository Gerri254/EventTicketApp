# Comprehensive Migration & Completion Prompt for TicketWave (EventTicketApp)

## Introduction and Context

You are tasked with completing, migrating, and fixing an Android mobile application called TicketWave (package name: com.example.eventticketapp). This is an event ticket management application built with Kotlin and Jetpack Compose following MVVM architecture with Clean Architecture principles. The application serves two user roles: attendees who browse events, purchase tickets, and receive QR-coded tickets, and organizers who create events, configure ticket types, and scan tickets for validation.

The project is partially complete with some screens having placeholder implementations, syntax errors, and incomplete functionality. Your task is to understand the entire codebase, perform specific technology stack migrations, fix all errors, and ensure every feature works end-to-end.

---

## Part One: Complete Codebase Understanding

### Project Architecture Overview

The application follows MVVM with Clean Architecture and uses Dagger Hilt for dependency injection. The codebase is organized into four main layers: the data layer containing models, local database, remote services, and repositories; the dependency injection layer containing Hilt modules; the UI layer containing screens, viewmodels, navigation, components, and theming; and the utility layer containing helper classes for QR generation, PDF export, date formatting, and sharing.

### Data Layer Deep Dive

#### Data Models Location and Purpose

The Event model located in data/model/Event.kt represents the core event entity with fields for id, title, description, location, date, category, organizerId, isPublic flag, imageUrl, a list of ticketTypes, and timestamp fields for createdAt and updatedAt. This model is used throughout the application for displaying event information, creating new events, and associating tickets with events.

The Ticket model in data/model/Ticket.kt represents a purchased ticket with fields for id, ticketTypeId, eventId, userId, qrCodeData containing the encoded QR string, isScanned boolean flag, scannedAt timestamp, and purchasedAt timestamp. This model tracks the entire lifecycle of a ticket from purchase through scanning at the event.

The User model in data/model/User.kt contains user profile information including id, name, email, photoUrl, and importantly an isOrganizer boolean that determines whether the user can create events and access the scanner functionality.

The TicketType model in data/model/TicketType.kt represents pricing tiers for events with fields for id, name, description, price as a double, quantity as the total available, availableQuantity tracking remaining tickets, and eventId linking it to an event.

The Resource sealed class in data/model/Resource.kt provides a generic wrapper for API responses with three states: Loading for ongoing operations, Success containing the data of generic type T, and Error containing an error message string. This pattern is used consistently across all data operations for uniform error handling.

#### Local Database Implementation

The Room database implementation consists of three entity classes that mirror the domain models with additional Room annotations. EventEntity in data/local/entity/EventEntity.kt maps to the events table and includes a toEvent extension function for converting to the domain model and a companion object fromEvent function for reverse conversion. TicketEntity and UserEntity follow the same pattern in their respective files.

The DateConverter class in data/local/converter/DateConverter.kt handles conversion between Date objects and Long timestamps for Room storage using TypeConverter annotations. The TicketTypeConverter in the same directory uses Gson to serialize and deserialize List of TicketType objects to and from JSON strings for storage in a single database column.

The EventDao interface in data/local/dao/EventDao.kt defines twelve database operations: getAllEvents returning a Flow of all events, getPublicEvents filtering by isPublic flag, getEventsByOrganizer filtering by organizerId, getUpcomingEvents filtering by date greater than current time, getEventById for single event retrieval, getEventsByCategory for category filtering, searchEvents using LIKE queries on title and description, plus insert, update, and delete operations with appropriate conflict strategies.

The TicketDao in data/local/dao/TicketDao.kt defines fourteen operations including getTicketsByUser and getTicketsByEvent for filtered queries, getTicketById and getTicketByQrCode for specific lookups, count queries for getScannedTicketsCount and getTotalTicketsCount and getTicketsCountByType, plus standard CRUD operations.

The UserDao in data/local/dao/UserDao.kt provides seven operations for user management including getAllUsers, getUserById, getUserByEmail, insert with replace strategy, update, delete, and deleteAllUsers for logout cleanup.

The AppDatabase class in data/local/AppDatabase.kt extends RoomDatabase and defines the database with version 1, three entities, and uses fallbackToDestructiveMigration for schema changes. The database name is event_ticket_database. It uses a singleton pattern with synchronized block for thread-safe initialization.

#### Remote Services Implementation

The FirebaseAuthService class in data/remote/FirebaseAuthService.kt wraps Firebase Authentication operations. The signUp function takes email, password, and name parameters, creates a Firebase user, then immediately saves a User document to Firestore via FirestoreService. The signIn function authenticates with email and password. The signInWithGoogle function accepts an AuthCredential for Google OAuth flow. The resetPassword function sends a password reset email. The signOut function signs out the current user. The isUserAuthenticated function returns a boolean checking if currentUser is not null. The currentUser property exposes the FirebaseAuth currentUser.

The FirestoreService class in data/remote/FirestoreService.kt handles all Firestore operations across three collections: users, events, and tickets. For users, it provides saveUser that sets a document with the user id as document id, getUserById that retrieves a user document and maps it to User model, and updateUserRole that updates the isOrganizer field. For events, getAllEventsFlow uses callbackFlow with a snapshot listener to emit real-time updates as Resource states, getPublicEventsFlow adds a whereEqualTo filter for isPublic equals true, getEventsByOrganizerFlow filters by organizerId, getEventById performs a single document get, createEvent adds a new document and returns the generated id, updateEvent and deleteEvent modify existing documents. For tickets, createTicket adds a new ticket document, getTicketByQrCode queries by qrCodeData field, updateTicketScanStatus updates isScanned and scannedAt fields, getUserTicketsFlow returns a Flow filtered by userId, and getTicketsForEvent returns tickets filtered by eventId.

#### Repository Layer Implementation

The EventRepository class in data/repository/EventRepository.kt acts as the single source of truth for event data. It injects EventDao and FirestoreService. Local operations include getAllEventsFromLocal, getPublicEventsFromLocal, getEventByIdFromLocal, insertEventToLocal, updateEventInLocal, and deleteEventFromLocal, all delegating to the DAO. Remote operations include getAllEventsFromRemote, getPublicEventsFromRemote, getEventsByOrganizerFromRemote returning Flows from FirestoreService, createEventInRemote, updateEventInRemote, and deleteEventFromRemote. A sync operation fetches from remote and saves to local. An image URL saving method updates the event with the uploaded image URL.

The TicketRepository class in data/repository/TicketRepository.kt follows the same pattern for ticket data. It provides local CRUD operations through TicketDao, remote operations through FirestoreService including createTicketInRemote, getTicketByQrCodeFromRemote, updateTicketScanStatusInRemote, getUserTicketsFromRemote, and getTicketsForEventFromRemote. Count operations delegate to DAO methods for scanned count, total count, and count by type.

The UserRepository class in data/repository/UserRepository.kt manages user authentication and profile data. It injects UserDao and FirebaseAuthService. Local operations include saveUserToLocal, getUserByIdFromLocal, and deleteAllUsersFromLocal. Remote authentication operations include signUp, signIn, signInWithGoogle, resetPassword, signOut, isUserAuthenticated, and getCurrentUserId. The signUp operation creates the Firebase user and immediately creates a local user record.

### Dependency Injection Modules

The AppModule in di/AppModule.kt is annotated with Module and InstallIn SingletonComponent. It provides the EventTicketApplication instance for any component needing application context.

The DatabaseModule in di/DatabaseModule.kt provides the Room database and all three DAOs as singletons. The provideDatabase function builds AppDatabase with the application context. The provideEventDao, provideTicketDao, and provideUserDao functions extract DAOs from the database instance.

The NetworkModule in di/NetworkModule.kt provides Firebase service instances as singletons. It provides FirebaseAuth via getInstance, FirebaseFirestore via getInstance, and FirebaseStorage via getInstance. These are injected into the remote service classes.

### ViewModel Layer Implementation

The AuthViewModel in ui/auth/AuthViewModel.kt manages authentication state with three StateFlows: loginState, signupState, and passwordResetState, all typed as Resource of User or Unit wrapped in nullable. The login function launches a coroutine, emits Loading, calls userRepository signIn, and emits Success with the user or Error with the exception message. The signUp function follows the same pattern calling userRepository signUp. The loginWithGoogle function handles Google credential authentication. The resetPassword function calls the repository method and emits appropriate states. Clear functions reset each state to null for navigation or retry scenarios.

The SplashViewModel in ui/splash/SplashViewModel.kt is minimal, providing only an isUserLoggedIn function that returns the boolean result of userRepository isUserAuthenticated.

The HomeViewModel in ui/home/HomeViewModel.kt manages the main screen state with four StateFlows: currentUser as nullable User, userEvents as Resource of List of Event, publicEvents as Resource of List of Event, and userTickets as Resource of List of Ticket. The init block calls loadCurrentUser. The loadCurrentUser function gets the current user id, fetches the user from repository, and updates the StateFlow. The fetchUserEvents function collects from the organizer events Flow. The fetchPublicEvents function collects from the public events Flow. The fetchUserTickets function collects from the user tickets Flow. The searchEvents function filters publicEvents by query matching title or description. The refreshData function reloads all data. The signOut function calls repository signOut and clears local data. The isUserOrganizer function returns the isOrganizer flag from currentUser.

The CreateEventViewModel in ui/events/create/CreateEventViewModel.kt manages event creation state with StateFlows for createEventState as Resource of String representing the created event id, imageUrl as nullable String, eventCategories as List of String initialized from Constants, currentEvent as nullable Event for edit mode, and ticketTypes as List of TicketType. The loadEvent function fetches an existing event for editing. The setImageUrl function updates the image URL StateFlow. The createEvent function validates inputs, creates an Event object with the current user as organizer, calls repository createEventInRemote, and emits the event id on success. The addTicketType, updateTicketType, and removeTicketType functions manage the ticketTypes list. The saveTicketTypes function updates the event with the final ticket types list. The clearStates function resets all StateFlows.

The EventDetailsViewModel in ui/events/details/EventDetailsViewModel.kt manages single event display and registration with StateFlows for event as Resource of Event, registerState as nullable Resource of Ticket, isUserRegistered as Boolean, and selectedTicketType as nullable TicketType. The loadEvent function fetches the event by id. The checkIfUserRegistered function queries for existing tickets. The selectTicketType function updates the selection. The registerForEvent function generates QR code data using QRGeneratorUtil generateTicketQRCodeData with a new UUID for ticket id, creates a Ticket object, calls ticketRepository createTicketInRemote, updates the event availableQuantity for the selected ticket type, and emits success. The clearRegistrationState resets the register state. The isUserOrganizer checks if current user matches event organizerId.

The TicketViewModel in ui/tickets/viewer/TicketViewModel.kt manages ticket display with StateFlows for ticket as Resource of Ticket, event as Resource of Event, and qrCodeBitmap as nullable Bitmap. The loadTicket function first tries local repository, then remote if not found locally. The loadEvent function fetches the associated event. The generateQrCode function calls QRGeneratorUtil generateQRCode with the ticket qrCodeData and updates the bitmap StateFlow.

The ScannerViewModel in ui/tickets/scanner/ScannerViewModel.kt manages QR scanning with StateFlows for scanState as nullable Resource of Ticket, event as nullable Event, and scanStats as nullable ScanStats inner data class containing scannedCount and totalCount integers. The loadEvent function fetches the event and initial scan stats. The processQrCode function parses the QR data using QRGeneratorUtil parseQRCodeData, validates the eventId matches, fetches the ticket by QR code from repository, checks isScanned flag, updates scan status to true with current timestamp, updates scanStats, and emits success or appropriate error. The updateScanStats function queries scanned and total counts from repository. The clearScanState resets scan state to null.

### UI Screens Implementation

The SplashScreen in ui/splash/SplashScreen.kt displays the app logo and name for two seconds, then navigates to Login if not authenticated or Home if authenticated, using LaunchedEffect with delay and viewModel isUserLoggedIn check.

The LoginScreen in ui/auth/LoginScreen.kt provides email and password text fields with validation, a login button that calls viewModel login, a Google Sign-In button using the Google Sign-In API and launching the intent then passing credential to viewModel loginWithGoogle, a forgot password text button opening a dialog that calls viewModel resetPassword, and a sign up navigation link. It observes loginState and passwordResetState for showing loading indicators, error snackbars, and navigation on success.

The SignupScreen in ui/auth/SignupScreen.kt provides name, email, password, and confirm password fields with validation including password match checking, a sign up button calling viewModel signUp, and a login navigation link. It observes signupState for loading, error, and success handling.

The HomeScreen in ui/home/HomeScreen.kt implements a Scaffold with bottom navigation bar containing four tabs: Explore at index 0, My Events at index 1, My Tickets at index 2, and Profile at index 3. The Explore tab shows a SearchBar component, CategoryFilterChips as a horizontally scrollable row of FilterChip composables for each category, and a LazyColumn of EventCard composables from publicEvents with pull-to-refresh using SwipeRefresh. The My Events tab is only visible when isUserOrganizer returns true and shows a LazyColumn of the organizer userEvents with EventCard items that include an onScanClick callback navigating to QRScanner. A FloatingActionButton for creating events is visible on this tab. The My Tickets tab shows a LazyColumn of TicketCard composables from userTickets with onClick navigating to TicketViewer. The Profile tab shows user information in a Card with avatar, name, email, and a logout button calling viewModel signOut then navigating to Login.

The EventDetailsScreen in ui/events/details/EventDetailsScreen.kt displays full event information with an AsyncImage or placeholder at the top with gradient overlay, event title, date formatted using DateTimeUtils, location with icon, description text, and a section listing available TicketTypes with name, price, and remaining quantity. A Register button opens a dialog with RadioButton selection for ticket types. On registration success, a success dialog with Lottie animation navigates to TicketPreview. For organizers viewing their own events, Edit and Scan Tickets buttons appear navigating to CreateEvent with eventId and QRScanner respectively.

The CreateEventScreen in ui/events/create/CreateEventScreen.kt provides a form for event creation with an image picker area that should launch gallery intent and upload to Firebase Storage, OutlinedTextField components for title, description, and location with validation, a date picker row using DatePickerDialog and TimePickerDialog, an ExposedDropdownMenuBox for category selection from eventCategories, a Switch for public/private toggle, and a Create Event button that validates all fields and calls viewModel createEvent. On success, it navigates to TicketTypeSetup with the new eventId. For editing, the screen loads existing event data via viewModel loadEvent.

The TicketTypeSetupScreen in ui/events/create/TicketTypeSetupScreen.kt allows adding multiple ticket types with fields for name, description, price as decimal input, and quantity as integer input. An Add Ticket Type button calls viewModel addTicketType. Existing types display in a LazyColumn with edit and delete icons. A Save button calls viewModel saveTicketTypes and navigates back to Home or EventDetails.

The TicketPreviewScreen in ui/tickets/preview/TicketPreviewScreen.kt shows a preview of the just-purchased ticket with event info card, the generated QR code using the bitmap from viewModel, and a View Ticket button navigating to TicketViewer.

The TicketViewerScreen in ui/tickets/viewer/TicketViewerScreen.kt displays the full ticket with event name, date, time, location, ticket type name, ticket ID, and a large QR code bitmap. A status badge shows VALID in green if not scanned or USED in red if scanned. Share and Download buttons use TicketSharingUtil and PDFExporter respectively. The share button offers text or PDF options in a bottom sheet.

The QRScannerScreen in ui/tickets/scanner/QRScannerScreen.kt implements the ticket validation scanner. It requests camera permission using rememberPermissionState from Accompanist. The camera preview uses CameraX with ZXing DecoratedBarcodeView for barcode detection. A custom overlay composable draws the scanning area with animated scanning line using InfiniteTransition. An event info card shows event name and scan statistics with a LinearProgressIndicator showing scanned divided by total. A flash toggle IconButton controls the camera flash. On successful scan, a dialog with Lottie success animation and the ticket details appears. On error such as already scanned or wrong event, a dialog with error animation and message appears. Sound effects play using MediaPlayer with raw resource files success_sound and error_sound. Haptic feedback triggers using LocalHapticFeedback.

### Navigation Implementation

The NavGraph in ui/navigation/NavGraph.kt defines a NavHost with startDestination as Screen.Splash.route. The Screen sealed class defines all routes: Splash with route splash, Login with route login, Signup with route signup, Home with route home, EventDetails with route event_details/{eventId} and a createRoute function taking eventId, CreateEvent with route create_event and optional eventId query parameter, TicketTypeSetup with route ticket_type_setup/{eventId}, TicketPreview with route ticket_preview/{eventId}/{ticketId}, TicketViewer with route ticket_viewer/{ticketId}, and QRScanner with route qr_scanner/{eventId}. Each composable destination extracts arguments from backStackEntry and creates the appropriate ViewModel using hiltViewModel.

### Utility Classes Implementation

The Constants object in util/Constants.kt defines string constants for Firebase collection names USERS, EVENTS, and TICKETS. It defines SharedPreferences keys PREF_USER_ID, PREF_USER_NAME, PREF_USER_EMAIL, PREF_IS_LOGGED_IN, and PREF_IS_ORGANIZER. It defines the EVENT_CATEGORIES list containing Concert, Conference, Exhibition, Festival, Meeting, Party, Seminar, Sports, Theatre, Wedding, Workshop, and Other. It defines ticket type constants TICKET_TYPE_VIP, TICKET_TYPE_REGULAR, and TICKET_TYPE_FREE. It defines intent extra keys and request codes for various operations. It defines notification channel IDs CHANNEL_EVENT_REMINDERS and CHANNEL_TICKET_UPDATES.

The QRGeneratorUtil object in util/QRGeneratorUtil.kt provides QR code functionality. The generateQRCode function takes data string and optional size defaulting to 512, creates a QRCodeWriter, encodes with UTF_8 charset and ERROR_CORRECTION L level, creates a BitMatrix, and converts to Bitmap pixel by pixel returning the result. The generateTicketQRCodeData function takes ticketId, eventId, and userId strings and returns a JSON string in format {"ticketId":"...","eventId":"...","userId":"..."}. The parseQRCodeData function takes the QR string and uses regex to extract the three IDs, returning a Triple or null if parsing fails.

The PDFExporter object in util/PDFExporter.kt creates PDF tickets. The exportTicketToPdf function takes context, ticket, event, and qrCodeBitmap parameters. It creates a Document with PageSize A5, creates a PdfWriter with output file in context filesDir/tickets directory, opens the document, adds a Paragraph with event title in 18pt bold font, adds paragraphs for date formatted with DateTimeUtils, time, and location, adds the ticket ID, converts the QR bitmap to iText Image and adds it scaled to 200x200, adds footer text, closes the document, and returns a FileProvider URI for the created file.

The TicketSharingUtil object in util/TicketSharingUtil.kt provides sharing functionality. The shareTicketPdf function creates an ACTION_SEND intent with application/pdf mime type, adds the PDF URI as stream extra, sets flag for grant read permission, and starts chooser activity. The shareTicketText function creates an ACTION_SEND intent with text/plain mime type and formatted ticket information including event name, date, location, and ticket ID.

The DateTimeUtils object in util/DateTimeUtils.kt provides date formatting. It defines SimpleDateFormat instances for dateFormat as EEE, MMM dd, yyyy, timeFormat as hh:mm a, and datetimeFormat as yyyy-MM-dd'T'HH:mm:ss. The formatDate, formatTime, and formatDateTime functions format Date objects. The parseDate and parseDateTime functions parse strings to Date objects. The getCurrentDate function returns Date(). The isEventUpcoming function compares event date to current date. The getDaysUntilEvent function calculates day difference.

### Application Entry Points

The EventTicketApplication class in EventTicketApplication.kt extends Application and is annotated with HiltAndroidApp. The onCreate function creates two NotificationChannels: one for event reminders with id from Constants and DEFAULT importance, and one for ticket updates with HIGH importance, registering both with NotificationManager.

The MainActivity class in MainActivity.kt is annotated with AndroidEntryPoint. The onCreate function calls setContent with EventTicketAppTheme wrapping a Surface containing NavGraph with rememberNavController.

---

## Part Two: Technology Stack Migration Tasks

### Migration One: GSON to Kotlin Serialization

You must replace the GSON library with Kotlin Serialization throughout the codebase. This migration improves performance through compile-time code generation instead of runtime reflection, provides better type safety with compile-time error checking, and aligns with Kotlin-first development practices.

In the build.gradle.kts file at the app level, remove the GSON dependency line containing com.google.code.gson:gson:2.10.1. Add the Kotlin Serialization plugin in the plugins block as kotlin("plugin.serialization") with the same version as the Kotlin plugin. Add the Kotlin Serialization JSON dependency as implementation for org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0.

In each data model class including Event, Ticket, User, TicketType, and Resource, add the Serializable annotation from kotlinx.serialization. Replace any SerializedName annotations with SerialName annotations from kotlinx.serialization. The annotation syntax changes from @SerializedName("field_name") to @SerialName("field_name").

In the TicketTypeConverter class that handles Room type conversion, replace Gson usage with Json from Kotlin Serialization. The toJson function changes from using Gson().toJson(ticketTypes) to using Json.encodeToString(ticketTypes). The fromJson function changes from using Gson().fromJson with TypeToken to using Json.decodeFromString with the appropriate type parameter.

In the QRGeneratorUtil class, the generateTicketQRCodeData function should use Kotlin Serialization to create the JSON string. Create a serializable data class for QRCodeContent with ticketId, eventId, and userId fields. Use Json.encodeToString with this class instead of manual string concatenation. The parseQRCodeData function should use Json.decodeFromString to parse the JSON into the QRCodeContent class instead of regex parsing.

In any Firebase mapping code where GSON might be used for JSON conversion, replace with Kotlin Serialization equivalents. Firestore typically handles its own serialization, so verify that model classes work correctly with Firestore document mapping after adding Serializable annotations.

### Migration Two: iText to Apache PDFBox

You must replace iText PDF library with Apache PDFBox to eliminate AGPL licensing concerns. Apache PDFBox uses the permissive Apache 2.0 license which allows commercial use without open-sourcing your application.

In the build.gradle.kts file, remove the iText dependency line containing com.itextpdf:itextg:5.5.10. Add the PDFBox Android dependency as implementation for com.tom-roush:pdfbox-android:2.0.27.0.

In the EventTicketApplication onCreate or MainActivity onCreate, initialize PDFBox by calling PDFBoxResourceLoader.init(applicationContext) before any PDF operations occur.

In the PDFExporter utility class, completely rewrite the exportTicketToPdf function using PDFBox API. Create a new PDDocument instance. Create a new PDPage with PDRectangle A5 size. Add the page to the document. Create a PDPageContentStream with the document and page. Begin text operations with beginText. Set font using PDType1Font HELVETICA_BOLD for the title at size 18. Use newLineAtOffset to position text and showText to render the event title. Change to regular font for subsequent lines. Add date, time, location, and ticket ID information with appropriate positioning. End text operations with endText.

For adding the QR code bitmap, convert the Android Bitmap to a PDImageXObject. Create a ByteArrayOutputStream, compress the bitmap as PNG to the stream, create a PDImageXObject from the byte array using createFromByteArray method. Draw the image on the page using drawImage with x, y coordinates and width, height for scaling to 200x200 points.

Close the content stream. Save the document to the output file using save method with the file path. Close the document. Return the FileProvider URI for the created file.

Ensure error handling wraps all PDF operations in try-catch blocks, properly closing resources in finally blocks or using use extension function for automatic resource management.

### Migration Three: ZXing Scanner to ML Kit Barcode

You must replace ZXing Android Embedded for scanning with Google ML Kit Barcode Scanning while keeping ZXing Core for QR code generation. ML Kit provides better scanning accuracy, faster processing, and seamless integration with CameraX.

In the build.gradle.kts file, remove the ZXing Android Embedded dependency line containing com.journeyapps:zxing-android-embedded:4.3.0. Keep the ZXing Core dependency com.google.zxing:core for QR code generation. Add the ML Kit Barcode Scanning dependency as implementation for com.google.mlkit:barcode-scanning:17.2.0.

In the QRScannerScreen composable, remove all ZXing DecoratedBarcodeView references and replace with a pure CameraX and ML Kit implementation. Create the barcode scanner options using BarcodeScannerOptions.Builder with setFormats for FORMAT_QR_CODE only, then build. Get the BarcodeScanning client using BarcodeScanning.getClient with the options.

Set up CameraX with a preview use case and an image analysis use case. For image analysis, create an ImageAnalysis instance with STRATEGY_KEEP_ONLY_LATEST. Set the analyzer with an executor and an ImageAnalysis.Analyzer implementation. In the analyzer, get the InputImage from the media image using InputImage.fromMediaImage with the image and rotation degrees. Process the image with scanner.process(inputImage), adding success and failure listeners. On success, iterate through detected barcodes, get the rawValue, and if not null, call the viewModel processQrCode function. Ensure proper image closing in completion listener.

Bind the camera lifecycle with ProcessCameraProvider, selecting the back camera, and binding preview and analysis use cases. Handle camera initialization errors gracefully.

Maintain the existing overlay composable with scanning animation, flash toggle functionality using Camera.CameraControl, and the event info card with statistics.

Update the ScannerViewModel if any ZXing-specific code exists, though the processQrCode function should remain unchanged as it processes the raw string data regardless of scanning library.

---

## Part Three: Codebase Fixes and Completion

### Identifying and Fixing Syntax Errors

In CreateEventScreen.kt, the imagePickerLauncher variable is referenced but not defined. You must implement the image picker using rememberLauncherForActivityResult with ActivityResultContracts.GetContent for selecting images from gallery. The launcher result should upload the selected image URI to Firebase Storage using the storage reference, get the download URL on success, and call viewModel setImageUrl with the URL. Handle loading state during upload with a progress indicator overlay on the image area.

In CreateEventScreen.kt, the getCategoryColor function is defined inside the composable which is inefficient. Move this function to a utility file or make it a top-level function. Alternatively, convert it to a when expression directly where used or create a map of category to color in Constants.

In HomeScreen.kt, verify that ShimmerEventCard composable exists and is properly implemented for loading states. If missing, create a shimmer placeholder using the Accompanist Placeholder library with shimmer highlight. The shimmer card should match EventCard dimensions with placeholder shapes for image, title, date, and description areas.

In HomeScreen.kt, the ticket navigation uses Screen.TicketDetails which does not exist in the navigation routes. Change this to Screen.TicketViewer.createRoute(ticket.id) to match the defined navigation route.

In EventDetailsScreen.kt, verify the EventDetailsContent composable is fully implemented. If it is a separate extracted composable, ensure it receives all necessary parameters including event, onRegisterClick, and isOrganizer flag. If the content is meant to be inline, ensure all UI elements are properly placed within the screen composable.

### Completing Partial Implementations

For the image upload functionality in CreateEventScreen, implement the complete flow: user taps image area, launcher opens gallery, user selects image, show loading indicator, upload to Firebase Storage at path events/{eventId}/cover.jpg, retrieve download URL, update viewModel imageUrl state, display uploaded image with AsyncImage. Handle upload failures with error snackbar and retry option.

For TicketTypeSetupScreen, ensure the complete implementation includes: displaying existing ticket types in an editable list, input fields for new ticket type with name, description, price with decimal keyboard, and quantity with number keyboard, validation preventing empty names or negative prices, an add button that validates and calls viewModel addTicketType, edit functionality that populates fields and updates via viewModel updateTicketType, delete functionality with confirmation dialog calling viewModel removeTicketType, and a save button that calls viewModel saveTicketTypes and handles navigation.

For TicketPreviewScreen, ensure it displays: event name from loaded event, event date and time formatted, event location, the generated QR code bitmap at reasonable size, ticket type name and price, a prominent View Ticket button navigating to TicketViewer with the ticketId, and optionally a share button for immediate sharing.

### Ensuring Feature Completeness

Verify the complete authentication flow works: new user can sign up with validation, existing user can log in, Google Sign-In completes successfully and creates user document, password reset sends email, logout clears local data and navigates to login, auth state persists across app restarts.

Verify the complete event management flow works: organizer can create event with all fields, image uploads successfully, navigation to ticket type setup occurs, ticket types can be added and saved, event appears in organizer My Events tab, event appears in public Explore tab if marked public, organizer can edit existing event, organizer can delete event with confirmation.

Verify the complete ticket purchase flow works: user can view event details, user can select ticket type, registration creates ticket with QR data, available quantity decrements, success animation plays, navigation to ticket preview occurs, ticket appears in user My Tickets tab, ticket viewer displays all information, QR code generates correctly, share and download functions work.

Verify the complete scanning flow works: organizer can access scanner from event, camera permission requests properly, camera preview displays, QR codes scan and process, valid tickets mark as scanned, already scanned tickets show error, wrong event tickets show error, scan statistics update correctly, success and error sounds play, haptic feedback triggers.

### Error Handling Improvements

Add comprehensive error handling to all repository operations. Wrap remote calls in try-catch blocks. Emit Error state with meaningful messages. Log exceptions for debugging. Provide user-friendly error messages in UI.

Add network connectivity checking before remote operations. Create a connectivity utility using ConnectivityManager. Show offline indicator when disconnected. Queue operations for retry when connection restores or show appropriate messaging.

Add input validation in all form screens. Validate email format using Patterns.EMAIL_ADDRESS. Validate password minimum length and complexity. Validate required fields are not empty. Validate numeric inputs are within reasonable ranges. Show inline error messages on fields.

Add null safety checks throughout the codebase. Handle nullable returns from Firebase queries. Provide default values where appropriate. Use Elvis operators for fallbacks. Add null checks before navigation with parameters.

---

## Part Four: Testing and Verification

After completing all migrations and fixes, verify each component functions correctly.

Test Kotlin Serialization migration by creating and parsing QR code data, verifying ticket type converter saves and loads correctly, ensuring all model classes serialize without errors.

Test PDFBox migration by generating a PDF ticket, verifying it opens correctly in PDF viewer, checking all content appears properly formatted, testing sharing the PDF via intent.

Test ML Kit migration by scanning various QR codes, verifying scanning speed and accuracy, testing flash toggle functionality, confirming proper error handling for invalid codes.

Test all user flows end-to-end by walking through signup, event creation, ticket purchase, and scanning as both attendee and organizer roles.

Verify no runtime crashes occur by testing edge cases like empty lists, network failures, and permission denials.

---

## Part Five: Build and Dependency Management

Ensure the build.gradle.kts files are properly configured after all dependency changes. Run gradle sync to verify all dependencies resolve. Fix any version conflicts by checking compatible version combinations. Update any deprecated API usages flagged by lint.

The final dependencies should include: Kotlin Serialization plugin and JSON library, Apache PDFBox Android library, ML Kit Barcode Scanning library, ZXing Core for generation only, and all existing Jetpack, Firebase, and utility libraries that remain unchanged.

Run a clean build to verify compilation succeeds. Run the application on an emulator or device to verify runtime behavior. Test on minimum SDK version 24 device to verify compatibility.

---

## Summary of Required Actions

First, understand the complete codebase structure including all fifty-five Kotlin files, their purposes, and their interactions as documented in Part One.

Second, perform the three technology migrations: GSON to Kotlin Serialization affecting models and type converters, iText to Apache PDFBox affecting PDF export utility, and ZXing scanner to ML Kit affecting the QR scanner screen.

Third, fix all identified syntax errors and complete all partial implementations as documented in Part Three.

Fourth, ensure all features work end-to-end including authentication, event management, ticket purchase, and scanning flows.

Fifth, verify builds succeed and the application runs without crashes on target devices.

The goal is a fully functional, properly licensed, modern Android application using current best practices and libraries.
