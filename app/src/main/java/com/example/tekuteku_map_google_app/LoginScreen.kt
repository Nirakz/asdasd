package com.example.tekuteku_map_google_app

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen(modifier: Modifier = Modifier, onSigninClick: () -> Unit) {
    Surface (modifier = modifier.fillMaxSize()) {
        Column (modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Image( modifier = Modifier.size(80.dp), painter = painterResource(id = R.drawable.google), contentDescription = "Google Login" )
            Spacer( modifier = Modifier.size(32.dp) )
            Button( onClick = { onSigninClick() } ) {
               Text( text = "Sign in with Google",
                   fontFamily = FontFamily.Monospace,
                   fontWeight = FontWeight.SemiBold,
                   fontSize = 20.sp)
            }
        }
    }
}