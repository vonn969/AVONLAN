package com.avonlan.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val preferences = getSharedPreferences(
            "avonlan_settings",
            MODE_PRIVATE
        )

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
                    themeMode = themeMode,
                    onThemeChange = {
                        themeMode = it
                        preferences.edit()
                            .putString("theme_mode", it.name)
                            .apply()
                    }
                )
            }
        }
    }
}

@Composable
fun AvonlanHome(
    themeMode: AvonlanThemeMode,
    onThemeChange: (AvonlanThemeMode) -> Unit
) {
    var currentTime by remember { mutableStateOf("") }
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

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
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
                    text = currentTime,
                    fontSize = 76.sp,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = currentDate,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(
                        alpha = 0.6f
                    )
                )
            }

            Box(
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                TextButton(
                    onClick = { menuExpanded = true }
                ) {
                    Text(
                        text = "THEME: ${themeMode.name}",
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
