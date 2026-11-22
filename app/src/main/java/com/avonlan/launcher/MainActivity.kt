package com.avonlan.launcher

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.activity.compose.BackHandler

data class LauncherApp(
    val name: String,
    val packageName: String
)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val preferences = getSharedPreferences(
            "avonlan_settings",
            MODE_PRIVATE
        )

        val apps = loadApps()

        setContent {
            var themeMode by remember {
                mutableStateOf(
                    AvonlanThemeMode.valueOf(
                        preferences.getString("theme_mode", "SYSTEM")!!
                    )
                )
            }

            AvonlanTheme(mode = themeMode) {
                AvonlanHome(
                    apps = apps,
                    themeMode = themeMode,
                    onThemeChange = {
                        themeMode = it
                        preferences.edit()
                            .putString("theme_mode", it.name)
                            .apply()
                    },
                    onOpenApp = { packageName ->
                        val launchIntent =
                            packageManager.getLaunchIntentForPackage(packageName)
                        launchIntent?.let { startActivity(it) }
                    }
                )
            }
        }
    }

    private fun loadApps(): List<LauncherApp> {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        return packageManager.queryIntentActivities(intent, 0)
            .mapNotNull { info ->
                val packageName = info.activityInfo.packageName
                val name = info.loadLabel(packageManager).toString()

                if (packageName != this@MainActivity.packageName) {
                    LauncherApp(name, packageName)
                } else null
            }
            .distinctBy { it.packageName }
            .sortedBy { it.name.lowercase() }
    }
}

@Composable
fun AvonlanHome(
    apps: List<LauncherApp>,
    themeMode: AvonlanThemeMode,
    onThemeChange: (AvonlanThemeMode) -> Unit,
    onOpenApp: (String) -> Unit
) {
    var currentTime by remember { mutableStateOf("") }
    var showDrawer by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var menuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = SimpleDateFormat(
                "HH:mm",
                Locale.getDefault()
            ).format(Date())
            delay(1000)
        }
    }

    val currentDate = SimpleDateFormat(
        "EEEE, dd MMMM",
        Locale.getDefault()
    ).format(Date())

    val filteredApps = apps.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    BackHandler(enabled = showDrawer) {
        showDrawer = false
        searchQuery = ""
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(showDrawer) {
                var totalDrag = 0f
                detectVerticalDragGestures(
                    onVerticalDrag = { _, dragAmount ->
                        totalDrag += dragAmount
                    },
                    onDragEnd = {
                        if (totalDrag < -50f) showDrawer = true
                        if (totalDrag > 50f) showDrawer = false
                        totalDrag = 0f
                    }
                )
            },
        color = MaterialTheme.colorScheme.background
    ) {
        if (showDrawer) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Text(
                    "APPLICATIONS",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Medium
                )

                Spacer(Modifier.height(20.dp))

                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 16.sp
                    ),
                    decorationBox = { innerTextField ->
                        Box {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    "Search apps...",
                                    color = MaterialTheme.colorScheme.onBackground
                                        .copy(alpha = 0.4f)
                                )
                            }
                            innerTextField()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(20.dp))

                LazyColumn {
                    items(filteredApps) { app ->
                        TextButton(
                            onClick = { onOpenApp(app.packageName) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                app.name,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        currentTime,
                        fontSize = 76.sp,
                        fontWeight = FontWeight.Light,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        currentDate,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground
                            .copy(alpha = 0.6f)
                    )

                    Spacer(Modifier.height(28.dp))

                    Text(
                        "SWIPE UP",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onBackground
                            .copy(alpha = 0.45f)
                    )
                }

                Box(
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    TextButton(
                        onClick = { menuExpanded = true }
                    ) {
                        Text(
                            "THEME: ${themeMode.name}",
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        AvonlanThemeMode.values().forEach { mode ->
                            DropdownMenuItem(
                                text = { Text(mode.name) },
                                onClick = {
                                    onThemeChange(mode)
                                    menuExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
