package com.example.tekuteku_map_google_app

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun HomeScreen(modifier: Modifier = Modifier, currentUser: FirebaseUser?, onSignOutClick: () -> Unit) {
    val textStyle = TextStyle( fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium, fontSize = 18.sp )
    Surface(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column (modifier = modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center ) {
            if(currentUser == null) {
                Text( text = "Logout",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp )
            } else {
                Text( text = "Logined",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp )
                currentUser.let {
                        user ->
                    Text( text = "Name: ${user.providerData[1].displayName }", style = textStyle )
                    Text( text = "Email: ${user.email}", style = textStyle )
                    Text( text = "uid: ${user.uid}", style = textStyle )
                }
                CoroutineScope(Dispatchers.Main).launch {
                    val endpoints = endpointVersionProvider()
                    // Sử dụng endpoints
                }

            }
            Button(onClick = { onSignOutClick() }) {
                Text( text = "Logout", style = textStyle.copy(
                    fontWeight = FontWeight.SemiBold,
                ))

            }
        }

        }
}
