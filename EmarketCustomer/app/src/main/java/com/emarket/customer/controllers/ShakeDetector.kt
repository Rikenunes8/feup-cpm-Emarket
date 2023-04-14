package com.emarket.customer.controllers

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.abs

class ShakeDetector(ctx: Context, listener: () -> Unit) {
  private val MIN_SHAKE_ACCELERATION = 12
  private val MIN_MOVEMENTS = 8
  private val MAX_SHAKE_DURATION = 800
  private val X = 0
  private val Y = 1
  private val Z = 2

  private val sm = ctx.getSystemService(Context.SENSOR_SERVICE) as SensorManager
  private var startTime = 0L
  private var moveCount = 0

  private val sensorListener = object: SensorEventListener {
      override fun onSensorChanged(event: SensorEvent) {
          if (getMaxCurrentLinearAcceleration(event.values) <= MIN_SHAKE_ACCELERATION) return
          val now = System.currentTimeMillis()
          if (startTime == 0L) startTime = now
          val elapsedTime = now - startTime
          if (elapsedTime > MAX_SHAKE_DURATION) resetShakes()
          else {
              moveCount++
              if (moveCount < MIN_MOVEMENTS) return
              listener()
              resetShakes()
          }
      }

      override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
 }

  fun startSensing() {
      val linAccel = sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
      linAccel?.let {
          sm.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_UI)
      }
  }

  fun stopSensing() {
      sm.unregisterListener(sensorListener)
  }

  private fun getMaxCurrentLinearAcceleration(acceleration: FloatArray): Float {
      return maxOf(abs(acceleration[X]), abs(acceleration[Y]), abs(acceleration[Z]))
  }

  private fun resetShakes() {
      startTime = 0L
      moveCount = 0
  }
}