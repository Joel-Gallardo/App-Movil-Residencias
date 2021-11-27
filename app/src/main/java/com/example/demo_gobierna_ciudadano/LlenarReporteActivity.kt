package com.example.demo_gobierna_ciudadano

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.demo_gobierna_ciudadano.databinding.ActivityLlenarReporteBinding
import com.google.android.gms.location.*
import com.google.firebase.firestore.FirebaseFirestore

class LlenarReporteActivity : AppCompatActivity() {

    //variables para el gps
    lateinit var mFusedLocationClient: FusedLocationProviderClient
    val PERMISSION_ID = 42
    companion object {
        private val REQUIRED_PERMISSIONS_GPS= arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
    }

    //variable request code para tomar foto
    val REQUEST_IMAGE_CAPTURE = 1

    //instanciar bd firebaseFirestore
    private val db = FirebaseFirestore.getInstance()

   //Expresion regular para generar FOLIO random
    private val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

    private lateinit var binding: ActivityLlenarReporteBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLlenarReporteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Recuperar tipo de reporte y usuario
        val bundle = intent.extras
        val tipoDeReporte = bundle?.getString("tipoDeReporte")
        val email = bundle?.getString("email")
        val provider = bundle?.getString("provider")

        setup(tipoDeReporte ?: "", email ?: "", provider ?: "")

        //verificar que esten solicitados los permisos para el gps
        if (allPermissionsGrantedGPS()){
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_ID)
        }

        binding.btndetectar.setOnClickListener {
            leerubicacionactual()
            binding.btnmapa.visibility = View.VISIBLE
            binding.msgexito.visibility = View.VISIBLE
            binding.btndetectar.visibility = View.GONE
            binding.instruc.visibility = View.GONE
            binding.lbllatitud.visibility = View.GONE
            binding.lbllongitud.visibility = View.GONE
        }

        binding.btnmapa.setOnClickListener {
            val act = Intent(this, mapa::class.java)
            act.putExtra("latitud", binding.lbllatitud.text.toString())
            act.putExtra("longitud", binding.lbllongitud.text.toString())
            startActivity(act)
        }

        //Funcion para tomar foto
        setupListener()

    }

    //evento del btn Tomar Foto
    private fun setupListener() {
        binding.btnTakePhoto.setOnClickListener { dispatchTakePictureIntent() }
    }


    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            data?.extras?.let { bundle ->
                val imageBitmap = bundle.get("data") as Bitmap
                binding.imgTomada.setImageBitmap(imageBitmap)
            }
        }
    }

    //detectar ubicacion
    private fun allPermissionsGrantedGPS() = REQUIRED_PERMISSIONS_GPS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun leerubicacionactual(){
        if (checkPermissions()){
            if (isLocationEnabled()){
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mFusedLocationClient.lastLocation.addOnCompleteListener(this){ task ->
                        var location: Location? = task.result
                        if (location == null){
                            requestNewLocationData()
                        } else {
                            binding.lbllatitud.text = location.latitude.toString()
                            binding.lbllongitud.text = location.longitude.toString()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Activar ubicación", Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
                this.finish()
            }
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_ID)
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData(){
        var mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallBack, Looper.myLooper())
    }

    private val mLocationCallBack = object : LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult) {
            var mLastLocation : Location = locationResult.lastLocation
            binding.lbllatitud.text = mLastLocation.latitude.toString()
            binding.lbllongitud.text = mLastLocation.longitude.toString()
        }
    }

    private fun isLocationEnabled(): Boolean {
        var locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }


    //setup
    private fun setup(tipoDeReporte: String, email: String, provider: String) {

        //pintar en la iu el tipo de reporte que se selecciono en home
        binding.tipoDeReporteTextView.text = "Llena tu reporte de $tipoDeReporte"

        //variable para almacenar el tipo de reporte
        lateinit var tipo: String

        //variable para almacenar tipodeREPORTEsubmenu
        lateinit var tipoDeReporteSubmenu: String


        //Cuando Seleccionan alumbrado desde la pantalla home
        if (tipoDeReporte == "Alumbrado") {

            tipo = "Alumbrado"

            //creacion del spinner para alumbrado
            val listaAlumbrado = listOf(
                "Lámpara fundida",
                "Sin poste/Sin lámpara",
                "Poste dañado",
                "Red eléctrica dañada",
                "Otro"
            )
            val adaptador = ArrayAdapter(this, android.R.layout.simple_spinner_item, listaAlumbrado)
            binding.spinnerSubMenuFallas.adapter = adaptador

            binding.spinnerSubMenuFallas.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    tipoDeReporteSubmenu = listaAlumbrado[position]
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }
            }
        }

        //Cuando Seleccionan Vialidad y Calle desde la pantalla home
        if (tipoDeReporte == "Vialidad y Calle") {

            tipo = "Vialidad y Calle"

            val listaVialidadYCalle = listOf(
                "Bache(s)",
                "Falta de topes",
                "Exceso de topes",
                "Falla de semáforo",
                "Falta poner un semáforo",
                "Otro"
            )
            val adaptador = ArrayAdapter(this, android.R.layout.simple_spinner_item, listaVialidadYCalle)
            binding.spinnerSubMenuFallas.adapter = adaptador

            binding.spinnerSubMenuFallas.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    tipoDeReporteSubmenu = listaVialidadYCalle[position]
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }
            }
        }

        //Cuando Seleccionan Agua y Drenaje desde la pantalla home
        if (tipoDeReporte == "Agua y Drenaje") {

            tipo = "Agua y Drenaje"

            val listaAguaYDrenaje = listOf(
                "Fuga de agua",
                "Fuga de drenaje",
                "Sin servicio de agua",
                "Sin Servicio de drenaje",
                "Otro"
            )
            val adaptador = ArrayAdapter(this, android.R.layout.simple_spinner_item, listaAguaYDrenaje)
            binding.spinnerSubMenuFallas.adapter = adaptador

            binding.spinnerSubMenuFallas.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    tipoDeReporteSubmenu = listaAguaYDrenaje[position]
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }
            }
        }

        //Cuando Seleccionan Aseo y Limpieza desde la pantalla home
        if (tipoDeReporte == "Aseo y Limpieza") {

            tipo = "Aseo y Limpieza"

            val listaAseoyLimpieza = listOf(
                "Basura sin recolectar",
                "No pasó camión de basura",
                "Suciedad en acera",
                "Otro"
            )
            val adaptador = ArrayAdapter(this, android.R.layout.simple_spinner_item, listaAseoyLimpieza)
            binding.spinnerSubMenuFallas.adapter = adaptador

            binding.spinnerSubMenuFallas.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    tipoDeReporteSubmenu = listaAseoyLimpieza[position]
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }
            }
        }


        //boton Enviar
        binding.buttonEnviar.setOnClickListener {

            //generar folio random
            val folioRandom = (1..15)
                .map { i -> kotlin.random.Random.nextInt(0, charPool.size) }
                .map(charPool::get)
                .joinToString("");

           //Guardar reporte en la base de datos
            db.collection("usuarios").document(email).collection("fallas").document(folioRandom).set(
                hashMapOf(
                    "tipoDeReporte" to "$tipo: $tipoDeReporteSubmenu",
                    "descripcion" to binding.descripcionEditText.text.toString(),
                    "latitud de la direccion " to binding.lbllatitud.text.toString(),
                    "longitud de la direccion " to binding.lbllongitud.text.toString()
                )
            )

            //ir a pantalla reporteExitoso
            val reporteExitosoIntent = Intent(this, ReporteExitosoActivity::class.java).apply {
                putExtra("email", email)
                putExtra("provider", provider)
                putExtra("folio", folioRandom)
            }
            startActivity(reporteExitosoIntent)
        }
    }
}