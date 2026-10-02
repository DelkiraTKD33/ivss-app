package com.example.ivss.server.model

import kotlinx.serialization.Serializable

@Serializable
data class SolicitudVacaciones(
    val id: Int? = null,
    val cedula: String,
    val apellidosNombres: String,
    val denominacionCargo: String,
    val numeroCargo: String,
    val fechaIngreso: String,
    val codigoOrigenServicio: String,
    val unidadServicio: String,
    val lugar: String,
    val horario: String,
    val fechaDesde: String,
    val fechaHasta: String,
    val periodo: String,
    val numeroDias: Int,
    val fechaReintegro: String,
    val observaciones: String? = null,
    val nota: String? = null,
    val supervisorInmediato: String? = null,
    val coordinadorRRHH: String? = null,
    val maximaAutoridad: String? = null,
    val estado: String = "PENDIENTE",
    val fechaSolicitud: String
)

@Serializable
data class DatosConstancia(
    val cedula: String,
    val apellidosNombres: String,
    val denominacionCargo: String,
    val numeroCargo: String,
    val fechaIngreso: String,
    val codigoOrigenServicio: String,
    val unidadServicio: String,
    val lugar: String,
    val horario: String,
    val fechaDesde: String,
    val fechaHasta: String,
    val periodo: String,
    val numeroDias: Int,
    val fechaReintegro: String,
    val observaciones: String? = null,
    val nota: String? = null,
    val supervisorInmediato: String? = null,
    val coordinadorRRHH: String? = null,
    val maximaAutoridad: String? = null
)
