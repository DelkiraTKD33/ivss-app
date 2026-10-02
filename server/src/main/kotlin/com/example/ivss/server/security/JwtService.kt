package com.example.ivss.server.security

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.example.ivss.server.model.Rol
import java.util.Date

object JwtService {
    private const val SECRET = "ivss-super-secret-key-change-me-in-prod-2026"
    private const val ISSUER = "ivss-app"
    private const val AUDIENCE = "ivss-users"
    private const val VALIDITY_MS = 24 * 60 * 60 * 1000L // 24 horas

    val algorithm: Algorithm = Algorithm.HMAC256(SECRET)

    val verifier: JWTVerifier = JWT.require(algorithm)
        .withIssuer(ISSUER)
        .withAudience(AUDIENCE)
        .build()

    fun generateToken(username: String, rol: Rol, cedula: String? = null, servicio: String? = null): String {
        return JWT.create()
            .withIssuer(ISSUER)
            .withAudience(AUDIENCE)
            .withSubject(username)
            .withClaim("rol", rol.name)
            .withClaim("cedula", cedula)
            .withClaim("servicio", servicio)
            .withIssuedAt(Date())
            .withExpiresAt(Date(System.currentTimeMillis() + VALIDITY_MS))
            .sign(algorithm)
    }
}
