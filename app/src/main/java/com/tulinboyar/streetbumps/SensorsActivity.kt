package com.tulinboyar.streetbumps

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Half.EPSILON
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.activity_sensors.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class SensorsActivity : AppCompatActivity(),SensorEventListener {


    private lateinit var auth:FirebaseAuth
    private lateinit var sensorManager:SensorManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sensors)
        auth= FirebaseAuth.getInstance()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager



    }

    override fun onSensorChanged(event: SensorEvent) {
        // In this example, alpha is calculated as t / (t + dT),
        // where t is the low-pass filter's time-constant and
        // dT is the event delivery rate.




       val sidesx =  event.values[0]
       val sidesy =  event.values[1]
       val sidesz =  event.values[2]
        textView5.text=sidesx.toString()
        textView6.text=sidesy.toString()
        textView7.text=sidesz.toString()


    }
    override fun onPause(){
        if(sensorManager!= null)
            sensorManager.unregisterListener(this)
        super.onPause()


    }
    override fun onResume(){

        val sensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER).also { sensorManager.registerListener(
            this,it,SensorManager.SENSOR_DELAY_FASTEST,SensorManager.SENSOR_DELAY_FASTEST

        ) }
        super.onResume()
    }
    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
       println("accur")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater=menuInflater
        menuInflater.inflate(R.menu.options_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==R.id.addInfo){
            val intent= Intent(applicationContext,SensorsActivity::class.java)
            startActivity(intent)
            finish()
        }
        else if(item.itemId==R.id.logOut){
            auth.signOut()
            val intent= Intent(applicationContext,MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        else if(item.itemId==R.id.map){

            val intent= Intent(applicationContext,MapsActivity::class.java)
            startActivity(intent)
            finish()

        }
        return super.onOptionsItemSelected(item)
    }
}