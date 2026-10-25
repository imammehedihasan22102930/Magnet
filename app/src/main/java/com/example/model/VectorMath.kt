package com.example.model

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Vector2D(
  val x: Double = 0.0,
  val y: Double = 0.0
) {
  val magnitudeSquared: Double get() = x * x + y * y
  val magnitude: Double get() = sqrt(magnitudeSquared)
  val angle: Double get() = atan2(y, x)

  operator fun plus(other: Vector2D): Vector2D = Vector2D(x + other.x, y + other.y)
  operator fun minus(other: Vector2D): Vector2D = Vector2D(x - other.x, y - other.y)
  operator fun times(scalar: Double): Vector2D = Vector2D(x * scalar, y * scalar)
  operator fun div(scalar: Double): Vector2D = if (scalar != 0.0) Vector2D(x / scalar, y / scalar) else Vector2D()

  fun dot(other: Vector2D): Double = x * other.x + y * other.y
  fun crossZ(other: Vector2D): Double = x * other.y - y * other.x

  fun normalized(): Vector2D {
    val mag = magnitude
    return if (mag > 1e-12) Vector2D(x / mag, y / mag) else Vector2D(0.0, 0.0)
  }

  fun distanceTo(other: Vector2D): Double = (this - other).magnitude

  companion object {
    val ZERO = Vector2D(0.0, 0.0)
    fun fromPolar(r: Double, thetaRad: Double) = Vector2D(r * cos(thetaRad), r * sin(thetaRad))
  }
}

data class Vector3D(
  val x: Double = 0.0,
  val y: Double = 0.0,
  val z: Double = 0.0
) {
  val magnitudeSquared: Double get() = x * x + y * y + z * z
  val magnitude: Double get() = sqrt(magnitudeSquared)

  operator fun plus(other: Vector3D): Vector3D = Vector3D(x + other.x, y + other.y, z + other.z)
  operator fun minus(other: Vector3D): Vector3D = Vector3D(x - other.x, y - other.y, z - other.z)
  operator fun times(scalar: Double): Vector3D = Vector3D(x * scalar, y * scalar, z * scalar)
  operator fun div(scalar: Double): Vector3D = if (scalar != 0.0) Vector3D(x / scalar, y / scalar, z / scalar) else Vector3D()

  fun dot(other: Vector3D): Double = x * other.x + y * other.y + z * other.z

  fun cross(other: Vector3D): Vector3D = Vector3D(
    x = y * other.z - z * other.y,
    y = z * other.x - x * other.z,
    z = x * other.y - y * other.x
  )

  fun normalized(): Vector3D {
    val mag = magnitude
    return if (mag > 1e-12) Vector3D(x / mag, y / mag, z / mag) else Vector3D(0.0, 0.0, 0.0)
  }

  companion object {
    val ZERO = Vector3D(0.0, 0.0, 0.0)
  }
}
