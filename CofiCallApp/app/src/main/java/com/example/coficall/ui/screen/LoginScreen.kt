package com.example.coficall.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource
import com.example.coficall.R

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.CorporateFare
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.coficall.theme.CoficabBlue
import com.example.coficall.theme.CoficabRoyalBlue
import com.example.coficall.theme.CoficabYellow
import com.example.coficall.theme.CofiCallTheme
import com.example.coficall.theme.DarkBackground
import com.example.coficall.theme.LightGrayBorder
import com.example.coficall.theme.NeutralMedGrey
import com.example.coficall.theme.NeutralWhite
import com.example.coficall.theme.NeutralCharcoal

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onLoginExecute: (String, String, (Result<Unit>) -> Unit) -> Unit = { _, _, cb -> cb(Result.success(Unit)) },
    onRegisterExecute: (String, String, (Result<Unit>) -> Unit) -> Unit = { _, _, cb -> cb(Result.success(Unit)) },
    onForgotPasswordExecute: (String, (Result<Unit>) -> Unit) -> Unit = { _, cb -> cb(Result.success(Unit)) },
    modifier: Modifier = Modifier,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showResetSuccessDialog by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }


    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences("login_prefs", android.content.Context.MODE_PRIVATE) }

    LaunchedEffect(Unit) {
        val savedRemember = prefs.getBoolean("remember_me", false)
        if (savedRemember) {
            email = prefs.getString("saved_email", "") ?: ""
            password = prefs.getString("saved_password", "") ?: ""
            rememberMe = true
        }
    }

    // Novo estado para controlar se está em modo de registro (criação de conta)
    var isRegisterMode by remember { mutableStateOf(false) }

    val isDomainValid = email.endsWith("@coficab.com")
    
    // Detetar se o tema atual é escuro
    val isDark = MaterialTheme.colorScheme.background != Color(0xFFF5F7FA) && MaterialTheme.colorScheme.background != Color(0xFFF4F6FA)

    val backgroundColor = if (isDark) DarkBackground else Color.White
    val titleTextColor = if (isDark) NeutralWhite else NeutralCharcoal
    val appTextColor = if (isDark) CoficabYellow else CoficabRoyalBlue
    val subtitleTextColor = if (isDark) NeutralMedGrey else NeutralCharcoal.copy(alpha = 0.7f)
    
    val buttonColor = if (isDark) CoficabYellow else CoficabRoyalBlue
    val buttonContentColor = if (isDark) CoficabBlue else Color.White
    
    val fieldBorderColor = if (isDark) NeutralMedGrey else LightGrayBorder
    val fieldFocusedBorderColor = if (isDark) CoficabYellow else CoficabRoyalBlue
    val fieldTextColor = if (isDark) NeutralWhite else NeutralCharcoal
    val labelColor = if (isDark) NeutralMedGrey else NeutralCharcoal.copy(alpha = 0.5f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Logo / Branding vertical alinhado com o design
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "CofiCall Logo",
                modifier = Modifier
                    .size(90.dp)
                    .padding(bottom = 12.dp)
            )

            
            Text(
                text = "Cofi",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = titleTextColor,
                lineHeight = 32.sp
            )
            Text(
                text = "Call",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = appTextColor,
                lineHeight = 32.sp,
                modifier = Modifier.offset(y = (-4).dp)
            )
            
            Text(
                text = "Diretório Corporativo",
                style = MaterialTheme.typography.bodyMedium,
                color = subtitleTextColor,
                modifier = Modifier.padding(top = 4.dp, bottom = 40.dp),
            )

            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    errorMessage = null
                },
                label = { Text("E-mail corporativo", color = labelColor) },
                placeholder = { Text("nome@coficab.com") },
                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null, tint = labelColor) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = fieldFocusedBorderColor,
                    focusedLabelColor = fieldFocusedBorderColor,
                    focusedLeadingIconColor = fieldFocusedBorderColor,
                    unfocusedBorderColor = fieldBorderColor,
                    focusedTextColor = fieldTextColor,
                    unfocusedTextColor = fieldTextColor,
                    cursorColor = fieldFocusedBorderColor,
                    unfocusedLabelColor = labelColor,
                    focusedPlaceholderColor = labelColor
                ),
            )

            Spacer(Modifier.height(16.dp))

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = null
                },
                label = { Text("Palavra-passe", color = labelColor) },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = labelColor) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (passwordVisible) "Ocultar" else "Mostrar",
                            tint = labelColor
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = fieldFocusedBorderColor,
                    focusedLabelColor = fieldFocusedBorderColor,
                    focusedLeadingIconColor = fieldFocusedBorderColor,
                    unfocusedBorderColor = fieldBorderColor,
                    focusedTextColor = fieldTextColor,
                    unfocusedTextColor = fieldTextColor,
                    cursorColor = fieldFocusedBorderColor,
                    unfocusedLabelColor = labelColor
                ),
            )

            // Guardar Credenciais Checkbox
            if (!isRegisterMode) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = buttonColor,
                            uncheckedColor = labelColor,
                            checkmarkColor = if (isDark) CoficabBlue else Color.White
                        )
                    )
                    Text(
                        text = "Guardar Credenciais",
                        style = MaterialTheme.typography.bodyMedium,
                        color = titleTextColor,
                        modifier = Modifier
                            .clickable { rememberMe = !rememberMe }
                            .padding(start = 4.dp)
                    )
                }
            }

            // Error message
            AnimatedVisibility(visible = errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 10.dp),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(28.dp))

            // Button (Login / Register)
            Button(
                onClick = {
                    when {
                        email.isBlank() -> errorMessage = "Insira o seu email."
                        !isDomainValid -> errorMessage = "Apenas emails @coficab.com são permitidos."
                        password.isBlank() -> errorMessage = "Insira a sua palavra-passe."
                        password.length < 6 -> errorMessage = "A palavra-passe deve conter pelo menos 6 caracteres."
                        else -> {
                            isLoading = true
                            errorMessage = null
                            
                            val action = if (isRegisterMode) onRegisterExecute else onLoginExecute
                            action(email, password) { result ->
                                isLoading = false
                                if (result.isSuccess) {
                                    if (!isRegisterMode) {
                                        prefs.edit().apply {
                                            putBoolean("remember_me", rememberMe)
                                            if (rememberMe) {
                                                putString("saved_email", email)
                                                putString("saved_password", password)
                                            } else {
                                                remove("saved_email")
                                                remove("saved_password")
                                            }
                                            apply()
                                        }
                                    }
                                    onLoginSuccess()
                                } else {
                                    val err = result.exceptionOrNull()
                                    errorMessage = err?.localizedMessage ?: (if (isRegisterMode) "Falha ao criar conta." else "Credenciais incorretas.")
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColor,
                    contentColor = buttonContentColor,
                ),
                enabled = !isLoading,
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = buttonContentColor,
                        strokeWidth = 2.5.dp,
                    )
                } else {
                    Text(
                        text = if (isRegisterMode) "Criar Conta" else "Entrar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Text buttons
            if (isRegisterMode) {
                TextButton(onClick = { 
                    isRegisterMode = false 
                    errorMessage = null
                }) {
                    Text(
                        text = "Já tem conta? Iniciar Sessão",
                        color = if (isDark) CoficabYellow else CoficabRoyalBlue,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    TextButton(
                        onClick = {
                            val trimmedEmail = email.trim()
                            if (trimmedEmail.isBlank()) {
                                errorMessage = "Por favor, introduza o seu e-mail corporativo."
                            } else if (!trimmedEmail.endsWith("@coficab.com")) {
                                errorMessage = "Introduza um e-mail válido (@coficab.com)."
                            } else {
                                isLoading = true
                                errorMessage = null
                                onForgotPasswordExecute(trimmedEmail) { result ->
                                    isLoading = false
                                    if (result.isSuccess) {
                                        showResetSuccessDialog = true
                                    } else {
                                        errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Erro ao enviar e-mail de redefinição."
                                    }
                                }
                            }
                        },
                        enabled = !isLoading
                    ) {
                        Text(
                            text = "Esqueceu a palavra-passe?",
                            color = if (isDark) NeutralMedGrey else CoficabRoyalBlue,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    
                    Spacer(Modifier.height(8.dp))
                    
                    TextButton(onClick = { 
                        isRegisterMode = true 
                        errorMessage = null
                    }) {
                        Text(
                            text = "Não tem conta? Criar Conta",
                            color = if (isDark) CoficabYellow else CoficabRoyalBlue,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(Modifier.height(48.dp))

            Text(
                text = "ACESSO EXCLUSIVO A COLABORADORES\nCOFICALL",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = if (isDark) NeutralMedGrey.copy(alpha = 0.7f) else NeutralMedGrey,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        }
    }

    if (showResetSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showResetSuccessDialog = false },
            title = {
                Text(
                    text = "E-mail de Redefinição Enviado",
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) NeutralWhite else NeutralCharcoal
                )
            },
            text = {
                Text(
                    text = "Foi enviado um link de redefinição para o e-mail:\n$email\n\nPor favor, verifique a sua caixa de entrada.",
                    color = if (isDark) NeutralMedGrey else NeutralCharcoal.copy(alpha = 0.8f)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { showResetSuccessDialog = false }
                ) {
                    Text(
                        text = "OK",
                        color = if (isDark) CoficabYellow else CoficabRoyalBlue,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = if (isDark) DarkBackground else Color.White
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    CofiCallTheme(darkTheme = false) { LoginScreen(onLoginSuccess = {}) }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenDarkPreview() {
    CofiCallTheme(darkTheme = true) { LoginScreen(onLoginSuccess = {}) }
}
