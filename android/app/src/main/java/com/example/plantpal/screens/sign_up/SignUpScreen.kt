package com.example.plantpal.screens.sign_up

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plantpal.R
import com.example.plantpal.Screen
import com.example.plantpal.ui.theme.PlantPalTheme

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SignUpScreen(
    openAndPopUp: (String, String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SignUpViewModel
) {
    val email by viewModel.tempEmail.collectAsState()
    val password by viewModel.tempPassword.collectAsState()
    val signUpLoading by viewModel.signUpLoading.collectAsState()
    val signUpStatus by viewModel.signUpStatus.collectAsState()

    SignUpScreenContent(
        modifier = modifier,
        email = email,
        password = password,
        signUpLoading = signUpLoading,
        signUpStatus = signUpStatus,
        onEmailChange = viewModel::updateEmail,
        onPasswordChange = viewModel::updatePassword,
        onSignUpClick = { viewModel.onSignUpClick(openAndPopUp) },
        onBackToSignInClick = {
            viewModel.clearState()
            openAndPopUp(Screen.SignInRoute.route, Screen.SignUpRoute.route)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreenContent(
    modifier: Modifier = Modifier,
    email: String,
    password: String,
    signUpLoading: Boolean,
    signUpStatus: String,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSignUpClick: () -> Unit,
    onBackToSignInClick: () -> Unit
) {

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = "Create Account",
                fontSize = 32.sp,
                modifier = modifier.padding(16.dp, 0.dp),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )

            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(16.dp, 5.dp)
                    .size(60.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val density = LocalDensity.current

                AnimatedVisibility(
                    visible = signUpLoading,
                    enter = slideInVertically { with(density) { 40.dp.roundToPx() } } + expandVertically(expandFrom = Alignment.Bottom) + fadeIn(initialAlpha = 0.3f),
                    exit = slideOutVertically() + shrinkVertically() + fadeOut()
                ) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                }

                AnimatedVisibility(
                    visible = !signUpLoading && signUpStatus.isNotEmpty(),
                    enter = slideInVertically { with(density) { 40.dp.roundToPx() } } + expandVertically(expandFrom = Alignment.Bottom) + fadeIn(initialAlpha = 0.3f),
                    exit = slideOutVertically() + shrinkVertically() + fadeOut()
                ) {
                    val isSuccess = signUpStatus == "Success"
                    Text(
                        text = when {
                            signUpStatus.isEmpty() -> ""
                            isSuccess -> "Account created! Navigating to Sign In..."
                            else -> "Error: $signUpStatus"
                        },
                        color = if (isSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            }

            OutlinedTextField(
                singleLine = true,
                modifier = modifier
                    .fillMaxWidth()
                    .padding(16.dp, 4.dp)
                    .border(
                        BorderStroke(width = 2.dp, color = MaterialTheme.colorScheme.outline),
                        shape = RoundedCornerShape(50)
                    ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                value = email,
                onValueChange = { onEmailChange(it) },
                placeholder = { Text("Email") },
                leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = "Email") },
                enabled = !signUpLoading && signUpStatus != "Success"
            )

            OutlinedTextField(
                singleLine = true,
                modifier = modifier
                    .fillMaxWidth()
                    .padding(16.dp, 4.dp)
                    .border(
                        BorderStroke(width = 2.dp, color = MaterialTheme.colorScheme.outline),
                        shape = RoundedCornerShape(50)
                    ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                value = password,
                onValueChange = { onPasswordChange(it) },
                placeholder = { Text("Password") },
                leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = "Password") },
                visualTransformation = PasswordVisualTransformation(),
                enabled = !signUpLoading && signUpStatus != "Success"
            )

            Spacer(modifier = Modifier.fillMaxWidth().padding(12.dp))

            Button(
                onClick = onSignUpClick,
                shape = RoundedCornerShape(50),
                modifier = modifier
                    .fillMaxWidth()
                    .padding(16.dp, 0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!signUpLoading) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (!signUpLoading) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                enabled = !signUpLoading && signUpStatus != "Success"
            ) {
                Text(
                    text = "Create Account",
                    fontSize = 16.sp,
                    modifier = modifier.padding(0.dp, 6.dp)
                )
            }

            Spacer(modifier = Modifier.fillMaxWidth().padding(4.dp))

            TextButton(
                onClick = { onBackToSignInClick() },
                enabled = !signUpLoading && signUpStatus != "Success"
            ) {
                Text(text = "Back to Sign In", fontSize = 16.sp)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SignUpPreview() {
    PlantPalTheme(dynamicColor = false) {
        SignUpScreenContent(
            email = "test@email.com",
            password = "password",
            onEmailChange = {},
            onPasswordChange = {},
            onSignUpClick = {},
            onBackToSignInClick = {},
            signUpLoading = false,
            signUpStatus = ""
        )
    }
}
