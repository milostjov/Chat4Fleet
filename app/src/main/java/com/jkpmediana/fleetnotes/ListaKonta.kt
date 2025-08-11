package com.jkpmediana.fleetnotes

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun KontoListScreen(onKontoClick: (String) -> Unit) {
    val vm: KontoViewModel = viewModel()

    val konta by vm.konta.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    val error by vm.error.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showUserDialog by remember { mutableStateOf(false) }

    var newKontoNumber by remember { mutableStateOf("") }
    var newKontoComment by remember { mutableStateOf("") }
    var currentUserName by remember { mutableStateOf("Android") }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // JEDAN searchQuery, na vrhu
    var searchQuery by rememberSaveable { mutableStateOf("") }

    // Filtriranje vezano za JEDAN searchQuery
    val filteredKonta by remember(konta, searchQuery) {
        derivedStateOf {
            if (searchQuery.isBlank()) konta
            else konta.filter { it.broj_konta.contains(searchQuery, ignoreCase = true) }
        }
    }

    LaunchedEffect(Unit) {
        currentUserName = UserPreferencesManager.getUserName(context)
        vm.ensureLoaded()
    }

    val isRefreshing = isLoading && konta.isNotEmpty()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { vm.loadKonta(forceRefresh = true) }
    )

    Scaffold(
        floatingActionButton = {
            Row {
                FloatingActionButton(onClick = { showUserDialog = true }) {
                    Icon(Icons.Default.Person, contentDescription = stringResource(R.string.change_user))
                }
                Spacer(Modifier.width(16.dp))
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_vehicle_id))
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(8.dp)
                .pullRefresh(pullRefreshState),
            contentAlignment = Alignment.TopCenter
        ) {
            when {
                isLoading && konta.isEmpty() -> CircularProgressIndicator()
                error != null && konta.isEmpty() -> Text(error ?: "Greška", style = MaterialTheme.typography.bodyLarge)
                konta.isEmpty() -> Text(stringResource(R.string.no_available_vehicle_ids), style = MaterialTheme.typography.bodyLarge)
                else -> {
                    Column(Modifier.fillMaxSize()) {
                        // koristi spoljašnji searchQuery
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search)) },
                            placeholder = { Text(stringResource(R.string.search_vehicle_ids)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            singleLine = true
                        )

                        LazyColumn {
                            items(
                                items = filteredKonta,
                                key = { it.broj_konta } // ako imaš ID, koristi ga
                            ) { konto ->
                                Text(
                                    text = konto.broj_konta,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onKontoClick(konto.broj_konta) }
                                        .padding(16.dp)
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }





    //  Dialog za dodavanje konta
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(stringResource(R.string.add_vehicle_id)) },
            text = {
                Column {
                    TextField(
                        value = newKontoNumber,
                        onValueChange = { newKontoNumber = it },
                        placeholder = { Text(stringResource(R.string.enter_vehicle_id)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = newKontoComment,
                        onValueChange = { newKontoComment = it },
                        placeholder = { Text(stringResource(R.string.enter_initial_comment)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.addKonto(
                        brojKonta = newKontoNumber,
                        komentar = newKontoComment,
                        userName = currentUserName
                    ) {
                        newKontoNumber = ""
                        newKontoComment = ""
                        showAddDialog = false
                    }
                }) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    //  Dialog za promenu korisničkog imena
    if (showUserDialog) {
        var tempName by remember { mutableStateOf(currentUserName) }

        AlertDialog(
            onDismissRequest = { showUserDialog = false },
            title = { Text(stringResource(R.string.change_username)) },
            text = {
                Column {
                    TextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        placeholder = { Text(stringResource(R.string.enter_new_username)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        UserPreferencesManager.saveUserName(context, tempName)
                        currentUserName = tempName
                        Toast.makeText(context, context.getString(R.string.username_saved), Toast.LENGTH_SHORT).show()
                        showUserDialog = false
                    }
                }) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showUserDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
