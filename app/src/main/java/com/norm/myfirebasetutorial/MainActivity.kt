package com.norm.myfirebasetutorial

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.norm.myfirebasetutorial.data.Technology
import com.norm.myfirebasetutorial.presentation.AuthScreen
import com.norm.myfirebasetutorial.presentation.deleteAccount
import com.norm.myfirebasetutorial.presentation.singIn
import com.norm.myfirebasetutorial.presentation.singOut
import com.norm.myfirebasetutorial.presentation.singUp
import com.norm.myfirebasetutorial.ui.theme.MyFirebaseTutorialTheme
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val fs = Firebase.firestore
        val storage = Firebase.storage.reference.child("images")
        val fa = Firebase.auth

        setContent {
            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.PickVisualMedia()
            ) { uri ->
                uri?.let {
                    val appName = application.getString(R.string.app_name)
                    val timeInMillis = System.currentTimeMillis()
                    val tasks = storage.child("${appName}_${timeInMillis}_image" + ".jpg").putBytes(
                        bitmapToByteArray(this, uri)
                    )
                    tasks.addOnSuccessListener { uploadTask ->
                        uploadTask.metadata?.reference?.downloadUrl?.addOnCompleteListener { uriTask ->
                            saveTechnology(fs, uriTask.result.toString())
                        }
                    }
                }
            }

            val scope = rememberCoroutineScope()
            val snackbarHostState = remember {
                SnackbarHostState()
            }
            var snackbarMessage by remember {
                mutableStateOf("")
            }

            LaunchedEffect(snackbarMessage) {
                scope.launch {
                    snackbarHostState.showSnackbar(snackbarMessage)
                }
            }

            Log.d("MyLog", "Current user's mail: ${fa.currentUser?.email ?: "Unknown"}")
            if (fa.currentUser?.email != null) {
                snackbarMessage = "Current user's mail: ${
                    fa.currentUser?.email
                }"
            }
            MyFirebaseTutorialTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = {
                        SnackbarHost(hostState = snackbarHostState)
                    }
                ) { padding ->
//                    MainScreen(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .padding(
//                                top = padding.calculateTopPadding(),
//                                bottom = padding.calculateBottomPadding(),
//                            ),
//                        onClick = {
//                            launcher.launch(
//                                PickVisualMediaRequest(
//                                    mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly
//                                )
//                            )
//                        }
//                    )
                    AuthScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                top = padding.calculateTopPadding(),
                                bottom = padding.calculateBottomPadding(),
                            ),
                        onSignUp = { email, password ->
                            snackbarMessage = singUp(
                                auth = fa,
                                email = email,
                                password = password,
                            )
                        },
                        onSignIn = { email, password ->
                            snackbarMessage = singIn(
                                auth = fa,
                                email = email,
                                password = password,
                            )
                        },
                        onSignOut = {
                            snackbarMessage = singOut(
                                auth = fa,
                            )
                        },
                        onDeleteAccount = { email, password ->
                            snackbarMessage = deleteAccount(
                                auth = fa,
                                email = email,
                                password = password,
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val context = LocalContext.current

    val fs = Firebase.firestore
    val storage = Firebase.storage.reference.child("images")

    val list = remember {
        mutableStateOf(emptyList<Technology>())
    }

    val listener = fs.collection("technologies").addSnapshotListener { snapShot, exeption ->
        list.value = snapShot?.toObjects(Technology::class.java) ?: emptyList()
    }
//    listener.remove()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
        ) {
            items(list.value) { technology ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        AsyncImage(
                            model = technology.logoUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentWidth(),
                            text = technology.name,
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            modifier = Modifier
                .fillMaxWidth(),
            onClick = {
                onClick()
            }
        ) {
            Text(
                text = "Add Technology"
            )
        }
    }
}

private fun bitmapToByteArray(context: Context, uri: Uri): ByteArray {
    val inputStream = context.contentResolver.openInputStream(uri)
    val bitmap = BitmapFactory.decodeStream(inputStream)
    val baos = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 75, baos)
    return baos.toByteArray()
}

private fun saveTechnology(fs: FirebaseFirestore, url: String) {
    fs
        .collection("technologies")
        .document()
        .set(
            Technology(
                name = "Room",
                description = "The Room persistence library provides an abstraction layer over SQLite to allow for more robust database access while harnessing the full power of SQLite.",
                platform = "Android/iOS",
                version = "2.6.1",
                mavenUrl = "androidx.room:room-runtime",
                logoUrl = url,
            )
        )
}