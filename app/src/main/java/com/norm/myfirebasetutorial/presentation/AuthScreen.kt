package com.norm.myfirebasetutorial.presentation

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.google.firebase.auth.EmailAuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AuthScreen(
    modifier: Modifier = Modifier,
    onSignUp: (login: String, password: String) -> Unit,
    onSignIn: (login: String, password: String) -> Unit,
    onSignOut: () -> Unit,
    onDeleteAccount: (login: String, password: String) -> Unit,
) {
    val context = LocalContext.current
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        var passwordVisibility by remember {
            mutableStateOf(false)
        }

        var login by remember {
            mutableStateOf("")
        }
        var password by remember {
            mutableStateOf("")
        }
        TextField(
            value = login,
            onValueChange = {
                login = it
            },
            modifier = Modifier
                .fillMaxWidth(0.7f),
            placeholder = {
                Text(
                    text = "Emial"
                )
            },
        )
        Spacer(
            modifier = Modifier
                .height(16.dp)
        )
        TextField(
            value = password,
            onValueChange = {
                password = it
            },
            modifier = Modifier
                .fillMaxWidth(0.7f),
            visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
            leadingIcon = {
                IconButton(
                    onClick = {
                        passwordVisibility = !passwordVisibility
                    }
                ) {
                    Icon(
                        imageVector = if (passwordVisibility) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = "visibility_state",
                    )
                }
            },
            placeholder = {
                Text(
                    text = "Password"
                )
            }
        )
        Spacer(
            modifier = Modifier
                .height(16.dp)
        )
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Button(
                onClick = {
                    onSignIn(
                        login,
                        password,
                    )
                }
            ) {
                Text(
                    text = "Sign In"
                )
            }
            Spacer(
                modifier = Modifier
                    .width(16.dp)
            )
            Button(
                onClick = {
                    onSignUp(
                        login,
                        password,
                    )
                }
            ) {
                Text(
                    text = "Sign Up"
                )
            }
        }
        Spacer(
            modifier = Modifier
                .height(16.dp)
        )
        Button(
            onClick = {
                onSignOut()
            }
        ) {
            Text(
                text = "Sign Out"
            )
        }
        Spacer(
            modifier = Modifier
                .height(16.dp)
        )
        Button(
            onClick = {
                onDeleteAccount(
                    login,
                    password,
                )
            }
        ) {
            Text(
                text = "Delete account"
            )
        }
    }
}

fun singUp(auth: FirebaseAuth, email: String, password: String): String {
    var status = ""
    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("MyLog", "Sign Up successful")
                status = "Sign Up successful"
            } else {
                Log.d("MyLog", "Sign Up failure")
                status = "Sign Up failure"
            }
        }
    return status
}

fun singIn(auth: FirebaseAuth, email: String, password: String): String {
    var status = ""
    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("MyLog", "Sign In successful")
                status = "Sign In successful"
            } else {
                Log.d("MyLog", "Sign Up failure")
                status = "Sign In failure"
            }
        }
    return status
}

fun singOut(auth: FirebaseAuth): String {
    auth.signOut()
    return "signOut"
}

fun deleteAccount(auth: FirebaseAuth, email: String, password: String): String {
    var status = ""
    val credential = EmailAuthProvider.getCredential(
        email,
        password,
    )
    auth.currentUser?.reauthenticate(credential)?.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            auth.currentUser?.delete()?.addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d("MyLog", "Delete successful")
                    status = "Delete successful"
                } else {
                    Log.d("MyLog", "Delete failure")
                    status = "Delete failure"
                }
            }
        } else {
            Log.d("MyLog", "Reauthenticate failure")
            status = "Reauthenticate failure"
        }
    }
    return status
}