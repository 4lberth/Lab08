@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.lab08

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.lab08.ui.theme.Lab08Theme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.room.Room
import com.example.lab08.ViewModel.TaskViewModel
import com.example.lab08.room.Task
import com.example.lab08.room.TaskDatabase
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Lab08Theme {
                val db = Room.databaseBuilder(
                    applicationContext,
                    TaskDatabase::class.java,
                    "task_db"
                ).build()


                val taskDao = db.taskDao()
                val viewModel = TaskViewModel(taskDao)


                TaskScreen(viewModel)

            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun TaskScreen(viewModel: TaskViewModel) {
    val tasks by viewModel.tasks.collectAsState()
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showDeleteAllConfirmation by remember { mutableStateOf(false) }  // Control del diálogo para eliminar todas las tareas

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tareas", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTaskDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar tarea", tint = Color.White)
            }
        },
        content = { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp) // Espacio entre tareas
            ) {
                items(tasks) { task ->
                    TaskItem(task = task, viewModel = viewModel)
                }

                if (tasks.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { showDeleteAllConfirmation = true },  // Mostrar diálogo de confirmación
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(text = "Eliminar todas las tareas", color = Color.White)
                        }
                    }
                }
            }
        }
    )

    if (showAddTaskDialog) {
        AddTaskDialog(
            onAdd = { taskDescription ->
                viewModel.addTask(taskDescription)
                showAddTaskDialog = false
            },
            onDismiss = { showAddTaskDialog = false }
        )
    }

    if (showDeleteAllConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteAllConfirmation = false },
            title = { Text("Confirmar eliminación") },
            text = { Text("¿Estás seguro de que deseas eliminar todas las tareas?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAllTasks()  // Eliminar todas las tareas
                        showDeleteAllConfirmation = false  // Cerrar el diálogo
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar todas", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDeleteAllConfirmation = false },  // Cancelar y cerrar el diálogo
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Cancelar", color = Color.White)
                }
            }
        )
    }
}


@Composable
fun AddTaskDialog(onAdd: (String) -> Unit, onDismiss: () -> Unit) {
    var newTaskDescription by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Nueva tarea") },
        text = {
            TextField(
                value = newTaskDescription,
                onValueChange = { newTaskDescription = it },
                label = { Text("Descripción de tarea") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newTaskDescription.isNotEmpty()) {
                        onAdd(newTaskDescription)
                    }
                },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Agregar", color = Color.White)
            }
        },
        dismissButton = {
            Button(
                onClick = { onDismiss() },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Cancelar", color = Color.White)
            }
        }
    )
}

@Composable
fun TaskItem(task: Task, viewModel: TaskViewModel) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color.Gray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Fila para los botones (parte superior de la tarjeta)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween  // Distribuir botones
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Botón para editar la tarea
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar tarea", tint = Color.Black)
                    }

                    // Botón para eliminar la tarea
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.error)
                    ) {
                        IconButton(
                            onClick = { showDeleteConfirmation = true }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar tarea", tint = Color.White)
                        }
                    }
                }

                // Botón para cambiar el estado de la tarea (Pendiente/Completada)
                Button(
                    onClick = { viewModel.toggleTaskCompletion(task) },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (task.isCompleted) Color.Green else Color.Gray
                    )
                ) {
                    Text(
                        text = if (task.isCompleted) "Completada" else "Pendiente",
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))  // Espacio entre los botones y la descripción

            // Descripción de la tarea (parte inferior de la tarjeta, sin límite de líneas)
            Text(
                text = task.description,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (showEditDialog) {
        EditTaskDialog(task = task, viewModel = viewModel, onDismiss = { showEditDialog = false })
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Confirmar eliminación") },
            text = { Text("¿Estás seguro de que deseas eliminar esta tarea?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTask(task)
                        showDeleteConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDeleteConfirmation = false },  // Cancelar y cerrar el diálogo
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Cancelar", color = Color.White)
                }
            }
        )
    }
}





@Composable
fun EditTaskDialog(task: Task, viewModel: TaskViewModel, onDismiss: () -> Unit) {
    var newDescription by remember { mutableStateOf(task.description) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Editar tarea") },
        text = {
            TextField(
                value = newDescription,
                onValueChange = { newDescription = it },
                label = { Text("Descripción de tarea") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    val updatedTask = task.copy(description = newDescription)
                    viewModel.updateTask(updatedTask)
                    onDismiss()
                },
                shape = RoundedCornerShape(50),  // Botón redondeado
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Guardar", color = Color.White)
            }
        },
        dismissButton = {
            Button(
                onClick = { onDismiss() },
                shape = RoundedCornerShape(50),  // Botón redondeado
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Cancelar", color = Color.White)
            }
        }
    )
}




