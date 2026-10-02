package com.example.ivss.server.security

import org.mindrot.jbcrypt.BCrypt

object PasswordUtil {
    fun hash(password: String): String = BCrypt.hashpw(password, BCrypt.gensalt(12))
    fun verify(password: String, hash: String): Boolean =
        try { BCrypt.checkpw(password, hash) } catch (e: Exception) { false }
}
