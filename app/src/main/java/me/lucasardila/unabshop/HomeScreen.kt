package me.lucasardila.unabshop

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.firestore


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onClickLogout: () -> Unit = {},
    viewModel: ProductoViewModel = viewModel()) {

    val auth = Firebase.auth
    val productos = viewModel.productos
    var showDialog by remember { mutableStateOf(false) }

    // Campos del nuevo producto
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var precioTexto by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.cargarProductos()
    }

    Scaffold(
        topBar = {
            MediumTopAppBar(
                title = {
                    Text(
                        "Unab Shop",
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    )
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Filled.Notifications, "Notificaciones")
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Filled.ShoppingCart, "Carrito")
                    }
                    IconButton(onClick = {
                        auth.signOut()
                        onClickLogout()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, "Carrito")
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = Color(0xFFFF9900),
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            Button(
                onClick = { showDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9900))
            ) {
                Text("Agregar Producto", color = Color.White)
            }
        }
    ){ padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(productos) { producto ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(producto.nombre, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(producto.descripcion)
                        Text("Precio: $${producto.precio}", fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { producto.id?.let { viewModel.eliminarProducto(it) } },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Text("Eliminar", color = Color.White)
                        }
                    }
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                confirmButton = {
                    Button(
                        onClick = {
                            val precio = precioTexto.toDoubleOrNull() ?: 0.0
                            viewModel.agregarProducto(
                                Producto(nombre = nombre, descripcion = descripcion, precio = precio)
                            )
                            // Resetear campos
                            nombre = ""
                            descripcion = ""
                            precioTexto = ""
                            showDialog = false
                        }
                    ) {
                        Text("Aceptar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancelar")
                    }
                },
                title = { Text("Nuevo Producto") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = nombre,
                            onValueChange = { nombre = it },
                            label = { Text("Nombre") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = descripcion,
                            onValueChange = { descripcion = it },
                            label = { Text("Descripción") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = precioTexto,
                            onValueChange = { precioTexto = it },
                            label = { Text("Precio") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            )
        }
    }
}

data class Producto(
    val id: String? = null,
    val nombre: String = "",
    val descripcion: String = "",
    val precio: Double = 0.0
)

fun DocumentSnapshot.toProducto(): Producto? {
    return try {
        Producto(
            id = id,
            nombre = getString("nombre") ?: "",
            descripcion = getString("descripcion") ?: "",
            precio = getDouble("precio") ?: 0.0
        )
    } catch (e: Exception) {
        null
    }
}


class ProductoViewModel : ViewModel() {
    private val db = Firebase.firestore
    private val productosRef = db.collection("productos")

    var productos by mutableStateOf(listOf<Producto>())
        private set

    fun cargarProductos() {
        productosRef.get()
            .addOnSuccessListener { result ->
                productos = result.mapNotNull { it.toProducto() }
            }
    }

    fun agregarProducto(producto: Producto) {
        productosRef.add(producto)
            .addOnSuccessListener { cargarProductos() }
    }

    fun eliminarProducto(id: String) {
        productosRef.document(id).delete()
            .addOnSuccessListener { cargarProductos() }
    }
}