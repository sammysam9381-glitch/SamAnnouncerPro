package com.sam.announcer

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(Modifier.fillMaxSize()) { Screen() }
            }
        }
    }
}

@Composable
private fun Screen() {
    val ctx = LocalContext.current
    val prefs = remember { Prefs(ctx) }
    var name by remember { mutableStateOf(prefs.userName) }
    var enabled by remember { mutableStateOf(prefs.enabled) }
    var hasAccess by remember { mutableStateOf(false) }

    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner) {
        val obs = LifecycleEventObserver { _, e ->
            if (e == Lifecycle.Event.ON_RESUME) {
                hasAccess = NotificationManagerCompat
                    .getEnabledListenerPackages(ctx).contains(ctx.packageName)
            }
        }
        owner.lifecycle.addObserver(obs)
        onDispose { owner.lifecycle.removeObserver(obs) }
    }

    Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Sam Announcer", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = name,
            onValueChange = { name = it; prefs.userName = it },
            label = { Text("Aapka naam") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Announce notifications")
            Switch(checked = enabled, onCheckedChange = { enabled = it; prefs.enabled = it })
        }

        Text(
            if (hasAccess) "Notification access: ON"
            else "Notification access: OFF - pehle ON karo"
        )
        if (!hasAccess) {
            Button(onClick = {
                ctx.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }) { Text("Notification access kholo") }
        }

        Text("Example: \"$name, you have a message from WhatsApp\"",
            style = MaterialTheme.typography.bodySmall)
    }
}
