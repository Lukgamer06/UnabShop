package me.lucasardila.unabshop

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun NavigationApp() {
    val navController = rememberNavController()
    var startDestination = "login"

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("login") {
            LoginScreen(
                onClickRegister = {
                    navController.navigate("register")
                },
                onSuccessfullLogin = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("register") {
            RegisterScreen(
                onClickBack = {
                    navController.popBackStack()
                },
                onSuccesfullRegister = {
                    navController.navigate("home"){
                        popUpTo(0)
                    }
                })
        }
        composable("home") {
            HomeScreen(onClickLogout = {
                navController.navigate("login"){
                    popUpTo(0)
                }
            })
        }
    }
}