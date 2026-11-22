
package com.avonlan.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AvonlanHome()
        }
    }
}

@Composable
fun AvonlanHome() {
    val context = LocalContext.current
    val preferences = context.getSharedPreferences(
        "avonlan_settings",
        android.content.Context.MODE_PRIVATE
    )

    var darkMode by remember {
        mutableStateOf(preferences.getBoolean("dark_mode", true))
    }

    val background = if (darkMode) Color.Black else Color.White
    val foreground = if (darkMode) Color.White else Color.Black

    var currentTime by remember {
        mutableStateOf(
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        )
    }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = SimpleDateFormat(
                "HH:mm",
                Locale.getDefault()
            ).format(Date())
            kotlinx.coroutines.delay(1000)
        }
    }

    val currentDate = SimpleDateFormat(
        "EEEE, dd MMMM",
        Locale.getDefault()
    ).format(Date())

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = background
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
                    color = foreground,
                    fontSize = 76.sp,
                    fontWeight = FontWeight.Light
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = currentDate,
                    color = foreground.copy(alpha = 0.55f),
                    fontSize = 14.sp
                )
            }

            TextButton(
                onClick = {
                    darkMode = !darkMode
                    preferences.edit()
                        .putBoolean("dark_mode", darkMode)
                        .apply()
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Text(
                    text = if (darkMode) "LIGHT MODE" else "DARK MODE",
                    color = foreground
                )
            }
        }
    }
}
