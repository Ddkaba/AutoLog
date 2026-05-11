package com.example.autolog_20.ui.theme.data.screen

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.autolog_20.R
import com.example.autolog_20.ui.theme.data.api.RetrofitClient
import com.example.autolog_20.ui.theme.data.model.RouteInfo
import com.example.autolog_20.ui.theme.data.model.ServicePlace
import com.example.autolog_20.ui.theme.data.model.ServicesUiState
import com.example.autolog_20.ui.theme.data.model.viewmodel.ServicesViewModel
import com.example.autolog_20.ui.theme.data.model.OSMInitializer
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesScreen(
    navController: NavController,
    currentLat: Double,
    currentLon: Double
) {
    val context = LocalContext.current
    val viewModel: ServicesViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ServicesViewModel(RetrofitClient.api) as T
            }
        }
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedService by viewModel.selectedService.collectAsStateWithLifecycle()
    val routeInfo by viewModel.routeInfo.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    var searchQuery by remember { mutableStateOf("автосервис") }
    var selectedRadius by remember { mutableStateOf(5000) }
    var showRadiusSelector by remember { mutableStateOf(false) }
    var showRouteSheet by remember { mutableStateOf(false) }

    val radiusOptions = listOf(
        1000 to stringResource(R.string.radius_1km),
        3000 to stringResource(R.string.radius_3km),
        5000 to stringResource(R.string.radius_5km),
        10000 to stringResource(R.string.radius_10km),
        20000 to stringResource(R.string.radius_20km)
    )

    LaunchedEffect(Unit) {
        viewModel.searchServices(currentLat, currentLon, searchQuery, selectedRadius)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.services_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back_button_services))
                    }
                },
                actions = {
                    IconButton(onClick = { showRadiusSelector = true }) {
                        Icon(
                            imageVector = Icons.Default.RadioButtonChecked,
                            contentDescription = stringResource(R.string.select_radius_button)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text(stringResource(R.string.search_label)) },
                    placeholder = { Text(stringResource(R.string.search_placeholder)) },
                    modifier = Modifier.weight(1f),
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                viewModel.searchServices(currentLat, currentLon, searchQuery, selectedRadius)
                            }
                        ) {
                            Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search_button))
                        }
                    }
                )
            }

            when (uiState) {
                ServicesUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is ServicesUiState.Success -> {
                    val services = (uiState as ServicesUiState.Success).services

                    if (services.isEmpty()) {
                        EmptyServicesContent()
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            items(services) { service ->
                                ServiceCard(
                                    service = service,
                                    onClick = {
                                        viewModel.selectService(service)
                                        viewModel.buildRoute(currentLat, currentLon, service.lat, service.lon)
                                        showRouteSheet = true
                                    }
                                )
                            }
                        }
                    }
                }

                is ServicesUiState.Error -> {
                    ErrorContentServices(
                        message = (uiState as ServicesUiState.Error).message,
                        onRetry = {
                            viewModel.searchServices(currentLat, currentLon, searchQuery, selectedRadius)
                        }
                    )
                }
            }
        }
    }

    if (showRadiusSelector) {
        ModalBottomSheet(
            onDismissRequest = { showRadiusSelector = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.select_search_radius),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                radiusOptions.forEach { (radius, label) ->
                    val isSelected = selectedRadius == radius

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable {
                                selectedRadius = radius
                                viewModel.searchServices(currentLat, currentLon, searchQuery, selectedRadius)
                                showRadiusSelector = false
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant,
                        border = if (isSelected)
                            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                        else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = stringResource(R.string.selected_desc),
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = { showRadiusSelector = false }) {
                    Text(stringResource(R.string.cancel_button_radius))
                }
            }
        }
    }

    if (showRouteSheet && routeInfo != null && selectedService != null) {
        RouteInfoBottomSheet(
            routeInfo = routeInfo!!,
            currentLat = currentLat,
            currentLon = currentLon,
            onDismiss = { showRouteSheet = false },
            onOpenMaps = {
                openInYandexMaps(currentLat, currentLon, selectedService!!.lat, selectedService!!.lon, context)
            },
            context
        )
    }
}

private fun openInYandexMaps(startLat: Double, startLon: Double, destLat: Double, destLon: Double, context: Context) {
    val uri = Uri.parse("https://yandex.ru/maps/?rtext=$startLat,$startLon~$destLat,$destLon&rtt=auto")
    val intent = Intent(Intent.ACTION_VIEW, uri)
    context.startActivity(intent)
}

@Composable
fun EmptyServicesContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.nothing_found),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.nothing_found_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ErrorContentServices(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.error_title),
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Text(stringResource(R.string.retry_button_services))
        }
    }
}

@Composable
fun OSMViewHolder(
    modifier: Modifier = Modifier,
    polyline: List<List<Double>>,
    startLat: Double,
    startLon: Double,
    endLat: Double,
    endLon: Double
) {
    val context = LocalContext.current
    val startMarkerText = stringResource(R.string.start_marker)
    val endMarkerText = stringResource(R.string.end_marker)

    LaunchedEffect(Unit) {
        OSMInitializer.initialize(context.applicationContext)
    }

    AndroidView(
        factory = { ctx ->
            MapView(ctx).apply {
                setMultiTouchControls(true)

                val mapController = controller

                if (polyline.isNotEmpty()) {
                    var minLat = polyline[0][0]
                    var maxLat = polyline[0][0]
                    var minLon = polyline[0][1]
                    var maxLon = polyline[0][1]

                    for (point in polyline) {
                        val lat = point[0]
                        val lon = point[1]
                        if (lat < minLat) minLat = lat
                        if (lat > maxLat) maxLat = lat
                        if (lon < minLon) minLon = lon
                        if (lon > maxLon) maxLon = lon
                    }

                    val latMargin = (maxLat - minLat) * 0.1
                    val lonMargin = (maxLon - minLon) * 0.1

                    val north = maxLat + latMargin
                    val south = minLat - latMargin
                    val east = maxLon + lonMargin
                    val west = minLon - lonMargin

                    val centerLat = (north + south) / 2
                    val centerLon = (east + west) / 2
                    mapController.setCenter(GeoPoint(centerLat, centerLon))

                    val latSpan = north - south
                    val lonSpan = east - west
                    val maxSpan = maxOf(latSpan, lonSpan)

                    val zoom = when {
                        maxSpan > 0.5 -> 10
                        maxSpan > 0.2 -> 11
                        maxSpan > 0.1 -> 12
                        maxSpan > 0.05 -> 13
                        maxSpan > 0.02 -> 14
                        maxSpan > 0.01 -> 15
                        else -> 16
                    }

                    mapController.setZoom(zoom.toDouble())
                } else {
                    val centerLat = (startLat + endLat) / 2
                    val centerLon = (startLon + endLon) / 2
                    mapController.setCenter(GeoPoint(centerLat, centerLon))
                    mapController.setZoom(13.0)
                }

                val startMarker = Marker(this)
                startMarker.position = GeoPoint(startLat, startLon)
                startMarker.title = startMarkerText
                startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                overlays.add(startMarker)

                val endMarker = Marker(this)
                endMarker.position = GeoPoint(endLat, endLon)
                endMarker.title = endMarkerText
                endMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                overlays.add(endMarker)

                val lineOverlay = Polyline()
                val points = polyline.map { GeoPoint(it[0], it[1]) }
                lineOverlay.setPoints(points)
                lineOverlay.color = Color.BLUE
                lineOverlay.width = 5f
                overlays.add(lineOverlay)

                invalidate()
            }
        },
        modifier = modifier
    )
}

@Composable
fun ServiceCard(
    service: ServicePlace,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = service.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = service.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row {
                    Text(
                        text = formatDistance(service.distance),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.Directions,
                contentDescription = stringResource(R.string.route_button),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteInfoBottomSheet(
    routeInfo: RouteInfo,
    currentLat: Double,
    currentLon: Double,
    onDismiss: () -> Unit,
    onOpenMaps: () -> Unit,
    context: Context
) {
    val startPoint = routeInfo.polyline.firstOrNull()
    val endPoint = routeInfo.polyline.lastOrNull()

    val startLat = startPoint?.get(0) ?: currentLat
    val startLon = startPoint?.get(1) ?: currentLon
    val endLat = endPoint?.get(0) ?: currentLat
    val endLon = endPoint?.get(1) ?: currentLon

    LaunchedEffect(Unit) {
        OSMInitializer.initialize(context.applicationContext)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = stringResource(R.string.route_info_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(5.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                OSMViewHolder(
                    modifier = Modifier.fillMaxSize(),
                    polyline = routeInfo.polyline,
                    startLat = startLat,
                    startLon = startLon,
                    endLat = endLat,
                    endLon = endLon
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatDistance(routeInfo.distance),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.distance_label),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatDuration(routeInfo.duration),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.duration_label),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (routeInfo.maneuvers.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.instructions_label),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.height(150.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(routeInfo.maneuvers) { maneuver ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Directions,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = maneuver.text,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onOpenMaps,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Directions,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(30.dp))
                Text(stringResource(R.string.open_yandex_maps))
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.close_button))
            }
        }
    }
}

@Composable
private fun formatDistance(meters: Double): String {
    return if (meters < 1000) {
        stringResource(R.string.distance_meters, meters.toInt())
    } else {
        stringResource(R.string.distance_kilometers, meters / 1000)
    }
}

@Composable
private fun formatDuration(seconds: Double): String {
    val minutes = (seconds / 60).toInt()
    val secs = (seconds % 60).toInt()
    return when {
        minutes > 0 && secs > 0 -> stringResource(R.string.duration_minutes_seconds, minutes, secs)
        minutes > 0 -> stringResource(R.string.duration_minutes, minutes)
        else -> stringResource(R.string.duration_seconds, secs)
    }
}