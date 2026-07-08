package com.ernesto.playout.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ernesto.playout.R
import com.ernesto.playout.data.remote.UploadStatusTracker

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onEditFacility: (Int) -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val customInstalaciones by viewModel.customInstalaciones.collectAsStateWithLifecycle()
    val proposalStatuses by viewModel.proposalStatuses.collectAsStateWithLifecycle()
    val uploadStatuses by UploadStatusTracker.statuses.collectAsStateWithLifecycle()

    val isLoggedIn by authViewModel.isLoggedIn.collectAsStateWithLifecycle()
    val isEmailVerified by authViewModel.isEmailVerified.collectAsStateWithLifecycle()
    val userProfile by authViewModel.userProfile.collectAsStateWithLifecycle()
    val authError by authViewModel.errorMessage.collectAsStateWithLifecycle()
    val authLoading by authViewModel.isLoading.collectAsStateWithLifecycle()

    LaunchedEffect(customInstalaciones) {
        if (customInstalaciones.isNotEmpty()) {
            viewModel.fetchProposalStatuses(customInstalaciones.map { it.fid })
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF806B40))
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color(0xFFF5F5F5)
            )
        }
        Text(
            text = "Settings",
            color = Color(0xFFF5F5F5),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        HorizontalDivider(
            color = Color(0xFF00AEFF),
            thickness = 1.dp,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (!isLoggedIn) {
            var isRegisterMode by remember { mutableStateOf(false) }
            var email by remember { mutableStateOf("") }
            var password by remember { mutableStateOf("") }
            var username by remember { mutableStateOf("") }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF806B40)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isRegisterMode) "Create account" else "Sign in",
                        color = Color(0xFFF5F5F5),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email", color = Color(0xFFF5F5F5)) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF2C332D))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password", color = Color(0xFFF5F5F5)) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF2C332D))
                    )
                    if (isRegisterMode) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Username", color = Color(0xFFF5F5F5)) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF2C332D))
                        )
                    }
                    if (authError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(authError ?: "", color = Color(0xFFF44336), fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            if (isRegisterMode) authViewModel.register(email, password, username)
                            else authViewModel.login(email, password)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00AEFF)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (authLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFFF5F5F5)
                            )
                        } else {
                            Text(if (isRegisterMode) "Register" else "Login")
                        }
                    }
                    TextButton(onClick = {
                        isRegisterMode = !isRegisterMode
                        authViewModel.clearError()
                    }) {
                        Text(
                            text = if (isRegisterMode) "Already have an account? Sign in"
                                   else "No account? Register",
                            color = Color(0xFF00AEFF)
                        )
                    }
                }
            }
        } else {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF806B40)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = userProfile?.username ?: "",
                        color = Color(0xFFF5F5F5),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = userProfile?.email ?: "",
                        color = Color(0xFFF5F5F5),
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Reviews: ${userProfile?.reviewCount ?: 0}",
                        color = Color(0xFFF5F5F5),
                        fontSize = 14.sp
                    )
                    if (!isEmailVerified) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Please verify your email",
                            color = Color(0xFFFFC107),
                            fontSize = 13.sp
                        )
                        TextButton(onClick = { authViewModel.resendVerification() }) {
                            Text("Resend email", color = Color(0xFF00AEFF))
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { authViewModel.logout() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00AEFF)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Logout")
                    }
                }
            }
        }

        HorizontalDivider(
            color = Color(0xFF00AEFF),
            thickness = 1.dp,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = Color(0xFF00AEFF)
            )
            Text(text = "Version 1.0.0", color = Color(0xFFF5F5F5), fontSize = 16.sp)
        }
        HorizontalDivider(color = Color(0xFF00AEFF), thickness = 1.dp)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Place,
                contentDescription = null,
                tint = Color(0xFF00AEFF)
            )
            Text(text = "Aachen, Deutschland", color = Color(0xFFF5F5F5), fontSize = 16.sp)
        }
        HorizontalDivider(color = Color(0xFF00AEFF), thickness = 1.dp)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color(0xFF00AEFF)
            )
            Text(text = "Ernesto Gimeno García", color = Color(0xFFF5F5F5), fontSize = 16.sp)
        }

        HorizontalDivider(
            color = Color(0xFF00AEFF),
            thickness = 1.dp,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Text(
            "My locations",
            color = Color(0xFFF5F5F5),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (customInstalaciones.isEmpty()) {
            Text(
                "You haven't added any locations yet.",
                color = Color(0xFFF5F5F5),
                fontSize = 14.sp
            )
        } else {
            customInstalaciones.forEach { inst ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        val iconRes = when (inst.sport) {
                            "Football" -> R.drawable.football
                            "Basketball" -> R.drawable.basketball
                            "Ping-Pong", "Pingpong", "PingPong", "ping-pong" -> R.drawable.pingpong
                            "Volleyball" -> R.drawable.volleyball
                            "Chess" -> R.drawable.chess
                            "Fencing" -> R.drawable.fencing
                            "Skating" -> R.drawable.skate
                            "Calisthenics" -> R.drawable.calisthenics
                            "Athletics" -> R.drawable.atletism
                            "Minigolf" -> R.drawable.minigolf
                            "Pétanque" -> R.drawable.petanque
                            else -> R.drawable.other
                        }
                        Image(
                            painterResource(iconRes),
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            colorFilter = ColorFilter.tint(Color(0xFF00AEFF))
                        )
                        Column {
                            Text(
                                inst.sport ?: "No category",
                                color = Color(0xFFF5F5F5),
                                fontSize = 15.sp
                            )
                            val conditionText = when (inst.condition) {
                                1 -> "Good"; 2 -> "Fair"; 3 -> "Broken"; else -> ""
                            }
                            if (conditionText.isNotEmpty()) {
                                Text(conditionText, color = Color(0xFF8B949E), fontSize = 12.sp)
                            }
                        }
                    }
                    val uploadStatus = uploadStatuses[inst.fid]
                    val proposalStatus = proposalStatuses[inst.fid]

                    when {
                        uploadStatus == "uploading" -> {
                            // Show loading indicator
                            Row(verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFF00AEFF)
                                )
                                Text("Uploading...", color = Color(0xFF8B949E), fontSize = 10.sp)
                            }
                        }
                        uploadStatus == "error" -> {
                            // Show error + retry button
                            Row(verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF8B949E).copy(alpha = 0.2f),
                                            RoundedCornerShape(8.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("Failed", color = Color(0xFF8B949E), fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold)
                                }
                                TextButton(
                                    onClick = { viewModel.retryUpload(inst.fid) },
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                ) {
                                    Text("Retry", color = Color(0xFF00AEFF), fontSize = 10.sp)
                                }
                            }
                        }
                        proposalStatus != null -> {
                            // Show Firestore status
                            val (badgeText, badgeColor) = when (proposalStatus) {
                                "approved" -> "Approved" to Color(0xFF4CAF50)
                                "rejected" -> "Rejected" to Color(0xFFF44336)
                                else -> "Pending" to Color(0xFFFFC107)
                            }
                            Box(
                                modifier = Modifier
                                    .background(badgeColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(text = badgeText, color = badgeColor, fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    IconButton(onClick = { onEditFacility(inst.fid) }) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color(0xFF00AEFF)
                        )
                    }
                    // Only show delete if not approved
                    if (proposalStatus != "approved") {
                        IconButton(onClick = { viewModel.delete(inst) }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color(0xFFF44336)
                            )
                        }
                    }
                }
                HorizontalDivider(color = Color(0xFF2C332D), thickness = 1.dp)
            }
        }
    }
}
