package upvictoria.pm_may_ago_2025.iti_271415.lab40jetpackcompose.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import upvictoria.pm_may_ago_2025.iti_271415.lab40jetpackcompose.composables.AnimatedCanvas

@Composable
fun MainScreen() {

    //Es una variable que sobrevive a las recompocisiones
    var estado by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Botón superior
        Button(
            modifier = Modifier.padding(16.dp),
            onClick = {
                estado = if (estado == 0) 1 else 0
            }
        ) {
            Text("Cambiar Color")
        }

        // Canvas principal
        AnimatedCanvas(
            estado = estado,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
    }
}