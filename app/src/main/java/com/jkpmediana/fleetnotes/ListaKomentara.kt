package com.jkpmediana.fleetnotes

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState



@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CommentsScreen(
    brojKonta: String,

) {
    var komentari by remember { mutableStateOf<List<Komentar>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedCommentId by remember { mutableStateOf<Int?>(null) }
    var selectedCommentText by remember { mutableStateOf("") }
    var newCommentText by remember { mutableStateOf("") }
    val context = LocalContext.current
    val userName = UserPreferencesManager.getUserName(context)

    // 🔹 Učitavanje komentara
    fun loadComments() {
        isLoading = true
        ApiClient.api.getKomentari(brojKonta).enqueue(object : Callback<List<Komentar>> {
            override fun onResponse(call: Call<List<Komentar>>, response: Response<List<Komentar>>) {
                Log.d("DEBUG", "HTTP: ${response.code()}")
                Log.d("DEBUG", "Raw: ${response.raw()}")
                Log.d("DEBUG", "Body: ${response.body()}")
                Log.d("DEBUG", "Error: ${response.errorBody()?.string()}")
                komentari = response.body() ?: emptyList()
                isLoading = false
            }
            override fun onFailure(call: Call<List<Komentar>>, t: Throwable) {
                isLoading = false
            }
        })
    }






    //  Brisanje
    fun deleteComment(id: Int) {
        ApiClient.api.deleteComment(id).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) loadComments()
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {}
        })
    }

    //  Dodavanje

    fun addComment() {

        ApiClient.api.addComment(NewComment(brojKonta, newCommentText, userName))
            .enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful) {
                        loadComments()
                        newCommentText = ""
                        showAddDialog = false
                    }
                }
                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {}
            })
    }

    //  Editovanje
    fun editComment() {
        selectedCommentId?.let { id ->
            ApiClient.api.editComment(id, UpdatedComment(selectedCommentText, userName))
                .enqueue(object : Callback<ApiResponse> {
                    override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                        if (response.isSuccessful) {
                            loadComments()
                            showEditDialog = false
                        }
                    }
                    override fun onFailure(call: Call<ApiResponse>, t: Throwable) {}
                })
        }
    }

    LaunchedEffect(brojKonta) { loadComments() }
    val isRefreshing = isLoading && komentari.isNotEmpty()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { loadComments() }
    )
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.add_comment))

            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .pullRefresh(pullRefreshState),
        contentAlignment = Alignment.TopCenter
        ) {
        when {
            isLoading -> CircularProgressIndicator()
            komentari.isEmpty() ->  Text(stringResource(R.string.no_available_comments), style = MaterialTheme.typography.bodyLarge)
            else -> {
                LazyColumn {
                    items(komentari) { k ->
                        val author = k.kreirao ?: stringResource(R.string.unknown)
                        val date = formatDate(k.datum ?: "")
                        val authorText = stringResource(R.string.author_with_date, author, date)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Text(
                                text = authorText,
                                style = MaterialTheme.typography.labelSmall
                            )

                            if (!k.poslednji_modifikovao.isNullOrBlank()) {
                                val editor = k.poslednji_modifikovao
                                val editDate = formatDate(k.poslednja_izmena ?: "")
                                val editText = stringResource(R.string.edited_by_with_date, editor, editDate)

                                Text(
                                    text = editText,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            Text(text = k.komentar, style = MaterialTheme.typography.bodyLarge)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                IconButton(onClick = {
                                    selectedCommentId = k.id
                                    selectedCommentText = k.komentar
                                    showEditDialog = true
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                                }
                                IconButton(onClick = {
                                    selectedCommentId = k.id
                                    showDeleteDialog = true
                                }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                        HorizontalDivider()
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

    //  Dialog za dodavanje komentara
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(stringResource(R.string.add_comment)) },
            text = {
                TextField(
                    value = newCommentText,
                    onValueChange = { newCommentText = it },
                    placeholder = { Text(stringResource(R.string.enter_comment)) }

                )
            },
            confirmButton = { TextButton(onClick = { addComment() }) { Text(stringResource(R.string.save)) } },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }

    //  Dialog za editovanje
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text(stringResource(R.string.edit_comment)) },

            text = {
                TextField(
                    value = selectedCommentText,
                    onValueChange = { selectedCommentText = it },
                    placeholder = { Text(stringResource(R.string.edit_comment)) }
                )
            },
            confirmButton = { TextButton(onClick = { editComment() }) { Text(stringResource(R.string.save)) } },
            dismissButton = { TextButton(onClick = { showEditDialog = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }

    //  Dialog za brisanje
    if (showDeleteDialog && selectedCommentId != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_confirmation)) },

            text = { Text(stringResource(R.string.delete_comment_confirmation)) },
            confirmButton = {
                TextButton(onClick = {
                    selectedCommentId?.let { deleteComment(it) }
                    showDeleteDialog = false
                }) { Text(stringResource(R.string.yes)) }

            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text(stringResource(R.string.no)) } }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun formatDate(dateString: String): String {
    return try {
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val outputFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        val date = LocalDateTime.parse(dateString, inputFormatter)
        date.format(outputFormatter)
    } catch (e: Exception) {
        dateString // ako format ne odgovara, prikaži original
    }
}
