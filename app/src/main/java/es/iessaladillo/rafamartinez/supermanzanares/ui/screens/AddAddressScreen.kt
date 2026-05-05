package es.iessaladillo.rafamartinez.supermanzanares.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import android.Manifest
import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import com.mapbox.geojson.Point
import androidx.compose.ui.platform.LocalContext
import es.iessaladillo.rafamartinez.supermanzanares.viewmodel.MapboxViewModel
import kotlinx.coroutines.launch
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.viewinterop.AndroidView
import com.google.firebase.auth.FirebaseAuth
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import es.iessaladillo.rafamartinez.supermanzanares.R
import es.iessaladillo.rafamartinez.supermanzanares.data.model.User
import es.iessaladillo.rafamartinez.supermanzanares.utils.getCurrentLocation
import es.iessaladillo.rafamartinez.supermanzanares.utils.getCurrentLocationAndFetchAddress
import es.iessaladillo.rafamartinez.supermanzanares.viewmodel.UserViewModel

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAddressScreen(
    navController: NavController,
    userViewModel: UserViewModel,
    mapboxViewModel: MapboxViewModel,
    origin: String
) {
    val suggestions by mapboxViewModel.suggestions.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val addressKey = "${origin}_address"
    val numberKey = "${origin}_number"
    val floorKey = "${origin}_floor"

    val navBackStackEntry = navController.previousBackStackEntry
    val prefillAddress = navBackStackEntry?.savedStateHandle?.get<String>(addressKey) ?: ""
    val prefillNumber = navBackStackEntry?.savedStateHandle?.get<String>(numberKey) ?: ""
    val prefillFloor = navBackStackEntry?.savedStateHandle?.get<String>(floorKey) ?: ""


    var query by remember { mutableStateOf(prefillAddress) }
    var selectedAddress by remember { mutableStateOf(prefillAddress) }
    var number by remember { mutableStateOf(prefillNumber) }
    var floor by remember { mutableStateOf(prefillFloor) }
    var userLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var markerManagerRef by remember { mutableStateOf<PointAnnotationManager?>(null) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (granted) {
            coroutineScope.launch {
                val location = getCurrentLocation(context)
                if (location != null) {
                    userLocation = location
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    LaunchedEffect(query, selectedAddress, userLocation) {
        if (query.length >= 3 && query != selectedAddress) {
            mapboxViewModel.fetchSuggestions(query, userLocation)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Añadir dirección") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    selectedAddress = ""

                    if (it.isBlank()) {
                        mapboxViewModel.clearSuggestions()
                    }
                },
                label = { Text("Dirección") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                })
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (selectedAddress.isBlank()) {
                Text(
                    text = "📍 Usar ubicación actual",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .align(Alignment.CenterHorizontally)
                        .clickable {
                            coroutineScope.launch {
                                val address =
                                    getCurrentLocationAndFetchAddress(context, mapboxViewModel)
                                val location = getCurrentLocation(context)

                                if (address != null && location != null) {
                                    selectedAddress = address
                                    query = address
                                    mapboxViewModel.clearSuggestions()

                                    val numberMatch = Regex("""\b\d{1,4}(?=,)""").find(address)
                                    number = numberMatch?.value ?: ""


                                    val (lat, lon) = location
                                    val point = Point.fromLngLat(lon, lat)

                                    mapViewRef?.mapboxMap?.flyTo(
                                        CameraOptions.Builder().center(point).zoom(16.0).build(),
                                        MapAnimationOptions.mapAnimationOptions {
                                            duration(2000L)
                                        })

                                    val bitmap = BitmapFactory.decodeResource(
                                        context.resources, R.drawable.red_marker
                                    )
                                    markerManagerRef?.deleteAll()
                                    val annotationOptions =
                                        PointAnnotationOptions().withPoint(point)
                                            .withIconImage(bitmap).withIconSize(0.15)
                                    markerManagerRef?.create(annotationOptions)
                                }
                            }
                        })
            }

            if (suggestions.isNotEmpty()) {
                suggestions.forEach { suggestion ->
                    Text(
                        text = suggestion,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable {
                                coroutineScope.launch {
                                    selectedAddress = suggestion
                                    query = suggestion
                                    mapboxViewModel.clearSuggestions()

                                    keyboardController?.hide()
                                    focusManager.clearFocus()

                                    // Geocodificar la sugerencia para obtener coordenadas
                                    val point = mapboxViewModel.geocode(suggestion)
                                    if (point != null) {
                                        val bitmap = BitmapFactory.decodeResource(
                                            context.resources, R.drawable.red_marker
                                        )

                                        mapViewRef?.mapboxMap?.flyTo(
                                            CameraOptions.Builder().center(point).zoom(16.0)
                                                .build(), MapAnimationOptions.mapAnimationOptions {
                                                duration(2000L)
                                            })

                                        markerManagerRef?.deleteAll()
                                        markerManagerRef?.create(
                                            PointAnnotationOptions().withPoint(point)
                                                .withIconImage(bitmap).withIconSize(0.15)
                                        )

                                        // Extraer número si lo hay
                                        val numberMatch =
                                            Regex("""\b\d{1,4}(?=,)""").find(suggestion)
                                        number = numberMatch?.value ?: ""
                                    }
                                }
                            })
                }
            }

            if (selectedAddress.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = number,
                    onValueChange = { number = it },
                    label = { Text("Número") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number, imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    })
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = floor,
                    onValueChange = { floor = it },
                    label = { Text("Piso / Letra (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    })
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (origin == "google") {
                        val user = FirebaseAuth.getInstance().currentUser
                        if (user != null) {
                            val userData = User(
                                id = user.uid,
                                name = user.displayName ?: "",
                                email = user.email ?: "",
                                address = selectedAddress,
                                number = number,
                                floor = floor
                            )
                            coroutineScope.launch {
                                userViewModel.saveUser(userData)
                            }
                        }

                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    } else {
                        navController.previousBackStackEntry?.savedStateHandle?.apply {
                            set(addressKey, selectedAddress)
                            set(numberKey, number)
                            set(floorKey, floor)
                        }
                        navController.popBackStack()
                    }
                },
                enabled = selectedAddress.isNotBlank() && number.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar dirección")
            }

            Spacer(modifier = Modifier.height(24.dp))

            AndroidView(
                factory = { ctx ->
                    // En v11 el SDK usa automáticamente R.string.mapbox_access_token,
                    // no hace falta ResourceOptionsManager ni MapInitOptions personalizados.
                    MapView(ctx).apply {
                        mapViewRef = this

                        mapboxMap.loadStyleUri(Style.MAPBOX_STREETS) {
                            val annotationApi = annotations
                            val pointAnnotationManager = annotationApi.createPointAnnotationManager()
                            markerManagerRef = pointAnnotationManager

                            val bitmap = BitmapFactory.decodeResource(
                                ctx.resources,
                                R.drawable.red_marker
                            )

                            val defaultPoint = Point.fromLngLat(-5.4930, 36.1830)
                            val initialPoint = userLocation?.let { (lat, lon) ->
                                Point.fromLngLat(lon, lat)
                            } ?: defaultPoint

                            mapboxMap.flyTo(
                                CameraOptions.Builder()
                                    .center(initialPoint)
                                    .zoom(13.0)
                                    .build(),
                                MapAnimationOptions.mapAnimationOptions {
                                    duration(2000L)
                                }
                            )

                            // Tap en el mapa → marcador + dirección
                            mapboxMap.addOnMapClickListener { point ->
                                coroutineScope.launch {
                                    val address = mapboxViewModel.reverseGeocode(
                                        point.latitude(),
                                        point.longitude()
                                    )
                                    if (address != null) {
                                        selectedAddress = address
                                        query = address
                                        mapboxViewModel.clearSuggestions()

                                        val numberMatch =
                                            Regex("""\b\d{1,4}(?=,)""").find(address)
                                        number = numberMatch?.value ?: ""
                                    }

                                    mapboxMap.flyTo(
                                        CameraOptions.Builder()
                                            .center(point)
                                            .zoom(16.0)
                                            .build(),
                                        MapAnimationOptions.mapAnimationOptions {
                                            duration(2000L)
                                        }
                                    )

                                    pointAnnotationManager.deleteAll()
                                    val annotationOptions = PointAnnotationOptions()
                                        .withPoint(point)
                                        .withIconImage(bitmap)
                                        .withIconSize(0.15)

                                    pointAnnotationManager.create(annotationOptions)
                                }
                                true
                            }
                            }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            )
        }
    }
}