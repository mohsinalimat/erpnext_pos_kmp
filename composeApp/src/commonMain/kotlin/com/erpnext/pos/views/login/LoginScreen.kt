package com.erpnext.pos.views.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
@OptIn(ExperimentalMaterial3Api::class)
fun LoginScreen(
    state: LoginState, actions: LoginAction
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    val isLoading = state != LoginState.Loading

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Iniciar sesión", style = MaterialTheme.typography.headlineMedium)
        }

        Spacer(Modifier.height(24.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp, 6.dp),
                value = email,
                isError = isError,
                onValueChange = { email = it },
                label = { Text("Correo electronico") },
                singleLine = true,
                placeholder = { Text("Nombre de usuario en el centro") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )

            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp, 6.dp),
                value = password,
                isError = isError,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                singleLine = true,
                placeholder = { Text("Contraseña") },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (passwordVisible)
                        Icons.Filled.Visibility
                    else Icons.Filled.VisibilityOff

                    // Please provide localized description for accessibility services
                    val description = if (passwordVisible) "Hide password" else "Show password"

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, description)
                    }
                }
            )
        }

        Spacer(Modifier.height(16.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (errorMessage.isNotEmpty()) {
                Text(errorMessage, color = Color.Red)
                Spacer(Modifier.height(8.dp))
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(
                onClick = {
                    actions.onLogin()
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
                    .padding(25.dp, 16.dp),
                colors = ButtonColors(
                    Color.Black,
                    Color.White,
                    disabledContentColor = Color.Black,
                    disabledContainerColor = Color.Gray
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                } else {
                    Text("Ingresar")
                }
            }
        }
    }
}
