package me.lucasardila.unabshop

import android.util.Patterns

fun validateEmail(email : String) : Pair<Boolean, String>{
    return when {
        email.isEmpty() -> Pair(false,"El correo es requerido.")
        !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> Pair(false,"El correo es invalido")
        !email.endsWith("@test.com") -> Pair(false, "Ese email no es corportativo.")
        else -> Pair(true,"")
    }
}

fun validatePassword(password : String) : Pair<Boolean, String>{
    return when{
        password.isEmpty() -> Pair(false, "La contraseña es requerida")
        password.length < 8 -> Pair(false,"La contraseña debe tener al menos 8 caracteres")
        !password.any { it.isDigit() } -> Pair(false,"La contraseña debe tener al menos un digito")
        else -> Pair(true,"")
    }
}