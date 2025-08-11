package com.jkpmediana.fleetnotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jkpmediana.fleetnotes.ui.theme.FleetNotesTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FleetNotesTheme {
                AppWithSplash()
            }
        }
    }
}

@Composable
fun AppWithSplash() {
    var showSplash by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(2000)
        showSplash = false
    }

    if (showSplash) {
        SplashScreen()
    } else {
        KontoApp()
    }
}

@Composable
fun SplashScreen() {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.splash_gold),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            CircularProgressIndicator(color = Color.White)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KontoApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // čitanje parametra brojKonta iz rute
    val brojKonta: String? =
        backStackEntry?.arguments?.getString("brojKonta")

//    var showAddDialog by remember { mutableStateOf(false) }
//    var newCommentText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when {
                            brojKonta != null -> stringResource(R.string.comments_for_account, brojKonta)
                            else -> stringResource(R.string.account_list)
                        }
                    )

                },
                navigationIcon = {
                    if (navController.previousBackStackEntry != null) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back)
                            )

                        }
                    }
                }

            )
        }
    ) { padding ->
        NavHost(
            navController,
            startDestination = "list",
            modifier = Modifier.padding(padding)
        ) {
            composable("list") {
                KontoListScreen(onKontoClick = { konto ->
                    navController.navigate("comments/$konto")
                })
            }
            composable("comments/{brojKonta}") { backStackEntry ->
                val brojKontaParam = backStackEntry.arguments?.getString("brojKonta") ?: ""
                CommentsScreen(brojKonta = brojKontaParam)
            }

        }
    }
}






