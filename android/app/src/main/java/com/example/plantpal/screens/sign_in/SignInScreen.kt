// UI code adapted from Firebase Notes Example App:
// Source: https://github.com/FirebaseExtended/firebase-video-samples/tree/main/fundamentals/android/auth-email-password/Notes

package com.example.plantpal.screens.sign_in


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plantpal.R
import com.example.plantpal.ui.theme.PlantPalTheme

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SignInScreen(
    openAndPopUp: (String, String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SignInViewModel
) {
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val signInLoading by viewModel.signInLoading.collectAsState()
    val signInStatus by viewModel.signInStatus.collectAsState()


    SignInScreenContent(
        modifier = modifier,
        email = email,
        password = password,
        signInLoading = signInLoading,
        signInStatus = signInStatus,
        onEmailChange = viewModel::updateEmail,
        onPasswordChange = viewModel::updatePassword,
        onSignInClick = { viewModel.onSignInClick(openAndPopUp) }
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SignInScreenContent(
    modifier: Modifier = Modifier,
    email: String,
    password: String,
    signInLoading: Boolean,
    signInStatus: String,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSignInClick: () -> Unit
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
            Image(
                painter = painterResource(id = R.mipmap.auth_image),
                contentDescription = "Auth image",
                modifier = modifier
                    .fillMaxWidth()
                    .padding(16.dp, 4.dp)
            )

            Text(
                text = "PlantPal",
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
                    .size(30.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val density = LocalDensity.current

                AnimatedVisibility(
                    visible = signInLoading,
                    enter = slideInVertically {
                        with(density) { 40.dp.roundToPx() }
                    } + expandVertically(
                        expandFrom = Alignment.Bottom
                    ) + fadeIn(
                        initialAlpha = 0.3f
                    ),
                    exit = slideOutVertically() + shrinkVertically() + fadeOut()
                )
                {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                }

                // Show a status message only when not loading and status is present
                AnimatedVisibility(
                    visible = !signInLoading && signInStatus.isNotEmpty(),
                    enter = slideInVertically {
                        with(density) { 40.dp.roundToPx() }
                    } + expandVertically(
                        expandFrom = Alignment.Bottom
                    ) + fadeIn(
                        initialAlpha = 0.3f
                    ),
                    exit = slideOutVertically() + shrinkVertically() + fadeOut()
                ) {
                    val isSuccess = signInStatus == "Success"
                    Text(
                        text = when {
                            signInStatus.isEmpty() -> ""
                            isSuccess -> "Account Authenticated! Logging In..."
                            else -> "Error: $signInStatus"
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
                placeholder = { Text(stringResource(R.string.email)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email"
                    )
                },
                enabled = !signInLoading && signInStatus != "Success"
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
                placeholder = { Text(stringResource(R.string.password)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Email"
                    )
                },
                visualTransformation = PasswordVisualTransformation(),
                enabled = !signInLoading && signInStatus != "Success"
            )

            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            )

            Button(
                onClick = { onSignInClick() },
                shape = RoundedCornerShape(50),
                modifier = modifier
                    .fillMaxWidth()
                    .padding(16.dp, 0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!signInLoading) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (!signInLoading) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                enabled = !signInLoading && signInStatus != "Success"
            ) {
                Text(
                    text = stringResource(R.string.sign_in),
                    fontSize = 16.sp,
                    modifier = modifier.padding(0.dp, 6.dp)
                )
            }

            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            )

        }

// TODO: SignUp flow needs to be defined once implemented
//        TextButton(onClick = { viewModel.onSignUpClick(openAndPopUp) }) {
//            Text(text = stringResource(R.string.sign_up_description), fontSize = 16.sp)
//        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AuthPreview() {
    PlantPalTheme (dynamicColor = false) {
        SignInScreenContent(
            email = "test@email.com",
            password = "password",
            onEmailChange = {},
            onPasswordChange = {},
            onSignInClick = {},
            signInLoading = false,
            signInStatus = ""
        )
    }
}
