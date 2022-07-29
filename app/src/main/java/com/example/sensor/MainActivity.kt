package com.example.sensor

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sensor.ui.theme.SensorTheme

const val SENSOR_ERROR = "Sensor not found"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager


        setContent {
            SensorTheme {
                // A surface container using the 'background' color from the theme
                LazyColumn(content = {
//                    items(sensorManager.getSensorList(Sensor.TYPE_ALL)){item : Sensor ->
//                        Text(text = item.name)
//                    }
                    item {
                        val proximitySensorValue = remember{ mutableStateOf("Proxy value : 0f ")}
                        val proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
                        val proximitySensorListener = object :  SensorEventListener{
                            override fun onSensorChanged(p0: SensorEvent?) {
                                proximitySensorValue.value = "proxy value : ${p0?.values?.get(0) ?: 0f}"  // p0.vales[0] with null safety
                            }
                            override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

                            }
                        }
                        SensorCompose(
                            sensorName = "Proximity Sensor",
                            onStartClick = {
                                 if (proximitySensor != null){
                                     sensorManager.registerListener(proximitySensorListener,proximitySensor,SensorManager.SENSOR_DELAY_NORMAL)
                                 }else{
                                     Toast.makeText(this@MainActivity, SENSOR_ERROR,Toast.LENGTH_LONG).show()
                                 }
                            },
                            onStopClick = {
                                sensorManager.unregisterListener(proximitySensorListener)
                            },
                            values = proximitySensorValue
                        )
                    }
                    item {

                        val value = remember{ mutableStateOf("gyro value : 0f")}
                        val gyroScopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
                        val gyroSensorListener = object : SensorEventListener{
                            override fun onSensorChanged(p0: SensorEvent?) {
                                value.value = "gyro value : ${p0?.values?.get(0) ?: 0f}"
                            }

                            override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
                            }

                        }

                        SensorCompose(
                            sensorName = "Gyro Scope",
                            onStartClick = {
                                sensorManager.registerListener(gyroSensorListener,gyroScopeSensor,SensorManager.SENSOR_DELAY_NORMAL)
                            },
                            onStopClick = {
                                sensorManager.unregisterListener(gyroSensorListener)
                            },
                            values = value
                        )
                    }
                    item {

                        val value = remember{ mutableStateOf("rotation value : 0f")}
                        val rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
                        val rotationVectorListener = object : SensorEventListener{
                            override fun onSensorChanged(p0: SensorEvent?) {
                                // values to degree
                                val rotationMatrix = FloatArray(16)
                                SensorManager.getRotationMatrixFromVector(rotationMatrix, p0?.values)
                                // map from 3d to xz 2d
                                val remappedRotationMatrix = FloatArray(16)
                                SensorManager.remapCoordinateSystem(
                                    rotationMatrix,
                                    SensorManager.AXIS_X,
                                    SensorManager.AXIS_Z,
                                    remappedRotationMatrix
                                )
                                // convert to orientation
                                val orientations = FloatArray(3)
                                SensorManager.getOrientation(remappedRotationMatrix, orientations)
                                for (i in 0..2) {
                                    orientations[i] =
                                        Math.toDegrees(orientations[i].toDouble()).toFloat()
                                }
                                // get degree angle
                                value.value = " rotation value : ${orientations[2]}"
                            }

                            override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
                            }

                        }

                        SensorCompose(
                            sensorName = "Rotation Vector sensor",
                            onStartClick = {
                                sensorManager.registerListener(rotationVectorListener,rotationVectorSensor,SensorManager.SENSOR_DELAY_NORMAL)
                            },
                            onStopClick = {
                                sensorManager.unregisterListener(rotationVectorListener)
                            },
                            values = value
                        )
                    }

                })
            }
        }
    }
}

@Composable
fun SensorCompose(
    sensorName : String,
    onStartClick : ()->Unit,
    onStopClick : ()->Unit,
    values : MutableState<String> = mutableStateOf("")
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 10.dp)
            .padding(all = 10.dp)
            .border(border = BorderStroke(width = 2.dp, color = Color.Black)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = sensorName , fontSize = 20.sp)
        Button(onClick = onStartClick) {
            Text(text = "start Sensor")
        }
        Button(onClick = onStopClick) {
            Text(text = "stop Sensor")
        }
        Text(text = values.value)
    }
}