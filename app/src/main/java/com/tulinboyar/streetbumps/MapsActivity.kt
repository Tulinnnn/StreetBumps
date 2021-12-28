package com.tulinboyar.streetbumps

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.view.*
import android.view.Gravity.CENTER_VERTICAL
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tulinboyar.streetbumps.databinding.ActivityMapsBinding
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.activity_sensors.*
import java.security.Permission
import kotlin.String as String1
import android.widget.PopupWindow
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.lang.Thread.sleep
import java.util.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback,SensorEventListener {
    private val NS2S = 1.0f / 1000000000.0f;

    private var timestamp:Float = 0.0f;
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var locationManager:LocationManager
    private lateinit var locationListener: LocationListener
    private lateinit var auth:FirebaseAuth
    private lateinit var db:FirebaseFirestore
    private lateinit var buttonClicked: kotlin.String
    //Defining to calculate rotation
    private var old_axisX:Float=0f
    private var old_axisY:Float=0f
    private var old_axisZ:Float=0f
    private var axisX:Float =0f
    private var axisY:Float =0f
    private var axisZ:Float =0f
    val gravity = FloatArray(3)
    val linear_acceleration = FloatArray(3)
    private lateinit var sensorManager:SensorManager

    //End Of Defining
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        auth= FirebaseAuth.getInstance()
        db= FirebaseFirestore.getInstance()

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager


    }
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in the loacation


        locationManager=getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationListener=object:LocationListener{
            override fun onLocationChanged(p0: Location) {
                if(p0!=null){
                    mMap.clear()
                    val userLocation=LatLng(p0.latitude,p0.longitude);
                    mMap.addMarker(   MarkerOptions()
                        .position(userLocation)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_current))
                        .title("We are here!"))
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,16f))
                    getAllData()

                }
            }

        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),1)
        }else{
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1,1f,locationListener)
            val lastLocation=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if(lastLocation!=null){
                val lastLocationLatLng=LatLng(lastLocation.latitude,lastLocation.longitude);
                with(mMap) {
                    addMarker(MarkerOptions()
                        .position(lastLocationLatLng)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_current))
                        .title("Last Location!"))
                    moveCamera((CameraUpdateFactory.newLatLngZoom(lastLocationLatLng,14f)))

                }
            }

        }


    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String1>,
        grantResults: IntArray
    ) {
        if(requestCode==1){
            if(grantResults.size>0){
                if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1,1f,locationListener)
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

override fun onSensorChanged(event: SensorEvent) {
        val alpha: Float = 0.8f

        // Isolate the force of gravity with the low-pass filter.
        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0]
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1]
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2]

        // Remove the gravity contribution with the high-pass filter.
        linear_acceleration[0] = event.values[0] - gravity[0]
        linear_acceleration[1] = event.values[1] - gravity[1]
        linear_acceleration[2] = event.values[2] - gravity[2]
        //Hold old data

        old_axisX = axisX
        old_axisY=axisY
        old_axisZ=axisZ

        //Get filtered sensor inf
        axisX =  linear_acceleration[0]
        axisY =   linear_acceleration[1]
        axisZ =  linear_acceleration[2]

        //Checking for rotation
        if(old_axisX-axisX>3) {


            autoDialog("Attention!",
                "Is there any street anomaly in current location? We detect a hole on left side. Is it a correct detecting? ",
                "we put this info on the map, thank you.",
                "Sorry for that.",
                "This location is recorded, we will as againThis location is recorded, we will as again",
                "A Big Hole",
                "hole")

        }

         else if(old_axisX-axisX<-3){


           autoDialog("Attention!",
           "Is there any street anomaly in current location? We detect a hole on right side. Is it a correct detecting? ",
           "we put this info on the map, thank you.",
           "Sorry for that.",
           "This location is recorded, we will as againThis location is recorded, we will as again",
           "A Big Hole",
           "hole")



          }
        else if(old_axisY-axisY>3){

            autoDialog("Attention!",
                "Is there any street anomaly in current location?  We detect a big hole. Is it a correct detecting? ",
                "we put this info on the map, thank you.",
                "Sorry for that.",
                "This location is recorded, we will as againThis location is recorded, we will as again",
                "A Big Hole",
                "hole")
        }
    else if(old_axisY-axisY<-3){


            autoDialog("Attention!",
                "Is there any street anomaly in current location? We detect a bump. Is it a correct detecting? ",
                "we put this info on the map, thank you.",
                "Sorry for that.",
                "This location is recorded, we will as againThis location is recorded, we will as again",
                "Street Bumps",
                "stone")

    }
        else if(old_axisZ-axisZ>3){


            autoDialog("Attention!",
                "Is there any street anomaly in current location? Please classify this anomaly using buttons ",
                "Thank you.",
                "Sorry for that.",
                "This location is recorded, we will as againThis location is recorded, we will as again",
                null,null,
                )

        }

    }
    fun autoDialog(dlgtitle:kotlin.String,dlgMessage:kotlin.String,yesText:kotlin.String,noText:kotlin.String,remindText:kotlin.String,infoType:kotlin.String?,buttonType:kotlin.String?){
        val builder = AlertDialog.Builder(this)
        builder.setTitle(dlgtitle)
        builder.setMessage(dlgMessage)
//builder.setPositiveButton("OK", DialogInterface.OnClickListener(function = x))

        builder.setPositiveButton("Yes") { dialog, which ->
            if(infoType!=null&& buttonType!=null)
                addInfo(infoType,buttonType)

            Toast.makeText(applicationContext,
                yesText, Toast.LENGTH_SHORT).show()
        }

        builder.setNegativeButton("No") { dialog, which ->
            Toast.makeText(applicationContext,
                noText, Toast.LENGTH_SHORT).show()
        }

        builder.setNeutralButton("Remind me later") { dialog, which ->
            Toast.makeText(applicationContext,
                remindText, Toast.LENGTH_SHORT).show()
        }
        val dlg=builder.create()

        dlg.show();
        val t = Timer()
        t.schedule(object : TimerTask() {
            override fun run() {
                dlg.dismiss() // when the task active then close the dialog
                t.cancel() // also just top the timer thread, otherwise, you may receive a crash report
            }
        }, 5000) // after 2 sec
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        println("accur")

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
    fun getAllData(){

        db.collection("Post").addSnapshotListener{snapshot,exception->
            if(exception!=null){
                Toast.makeText(applicationContext,exception.localizedMessage,Toast.LENGTH_LONG).show()
            }
            else{
                if(snapshot!=null){
                    if(!snapshot.isEmpty){
                       val documents= snapshot.documents
                        for(document in documents){
                           val userEmail= document.get("userEmail") as kotlin.String
                           val timeStamp= document.get("date") as Timestamp
                            val date=timeStamp.toDate()
                           val infoType= document.get("infoType") as kotlin.String?
                           val latitude= document.get("latitude") as Double
                           val longitude= document.get("longitude") as Double
                            val buttonImage=document.get("buttonImage") as kotlin.String
                         /*   println(userEmail)
                            println(latitude)
                            println(longitude)
                            println(infoType)*/
                            val loc=LatLng(latitude,longitude)
                           if(buttonImage=="stone") {
                               mMap.addMarker(
                                   MarkerOptions().position(loc).title(infoType)
                                       .icon(BitmapDescriptorFactory.fromResource(R.drawable.bumps_b))
                               )

                               //  mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc,10f))
                           }
                            else if(buttonImage=="crack") {
                               mMap.addMarker(
                                   MarkerOptions().position(loc).title(infoType)
                                       .icon(BitmapDescriptorFactory.fromResource(R.drawable.crack_b))
                               )

                               //  mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc,10f))
                           }
                           else if(buttonImage=="hole") {
                               mMap.addMarker(
                                   MarkerOptions().position(loc).title(infoType)
                                       .icon(BitmapDescriptorFactory.fromResource(R.drawable.phole_b))
                               )

                               //  mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc,10f))
                           }
                            else if(buttonImage=="roadwork") {
                                mMap.addMarker(
                                    MarkerOptions().position(loc).title(infoType)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.work_b))
                                )

                                //  mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc,10f))
                            }
                            else if(buttonImage=="animalcorpse") {
                               mMap.addMarker(
                                   MarkerOptions().position(loc).title(infoType)
                                       .icon(BitmapDescriptorFactory.fromResource(R.drawable.animal_b))
                               )

                               //  mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc,10f))
                           }
                           else  {
                               mMap.addMarker(
                                   MarkerOptions().position(loc).title(infoType)
                                       .icon(BitmapDescriptorFactory.fromResource(R.drawable.crash_b))
                               )

                               //  mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc,10f))
                           }

                        }

                    }
                }
            }

        }
    }
    //Classify Anomaly Type With Button
    fun stoneClick(view:View){
        addInfo("Street Bumps","stone")

    }
    fun holeClick(view:View){
        addInfo("A Big Hole","hole")
    }
    fun crackClick(view:View){
        addInfo("Crack On Surface","crack")
    }
    fun workClick(view:View){
        addInfo("Road Work","roadwork")
    }
    fun animalClick(view:View){
        addInfo("Animal Corpse","animalcorpse")
    }
    fun accidentClick(view:View){
        addInfo("Car Accident","caraccident")
    }

    //End of Classify

    //Record Data To Firebase
    fun addInfo(infoType:kotlin.String,buttonType:kotlin.String){
            val postHashMap= hashMapOf<kotlin.String,Any>()
            postHashMap.put("infoType",infoType)
            postHashMap.put("buttonImage",buttonType)
            locationManager=getSystemService(Context.LOCATION_SERVICE) as LocationManager
            locationListener=object:LocationListener {
                override fun onLocationChanged(p0: Location) {
                    if (p0 != null) {

                        val userLocation = LatLng(p0.latitude, p0.longitude);


                    }
                }
            }
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),1)
            }else{
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1,1f,locationListener)
                val lastLocation=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if(lastLocation!=null){
                    val lastLocationLatLng=LatLng(lastLocation.latitude,lastLocation.longitude);
                   postHashMap.put("latitude",lastLocation.latitude)

                    postHashMap.put("longitude",lastLocation.longitude)

                }

            }
            postHashMap.put("userEmail",auth.currentUser!!.email.toString())
            postHashMap.put("date",Timestamp.now())

            db.collection("Post").add(postHashMap).addOnCompleteListener{task->
                if(task.isSuccessful){

                }

            }.addOnFailureListener{exception->
                Toast.makeText(applicationContext,exception.localizedMessage,Toast.LENGTH_LONG).show()
            }

        }
        //End Function


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
            val menuInflater=menuInflater
            menuInflater.inflate(R.menu.options_menu,menu)
            return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==R.id.addInfo){
            val intent=Intent(applicationContext,SensorsActivity::class.java)
            startActivity(intent)
            finish()
        }
        else if(item.itemId==R.id.logOut){
            auth.signOut()
            val intent=Intent(applicationContext,MainActivity::class.java)
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


