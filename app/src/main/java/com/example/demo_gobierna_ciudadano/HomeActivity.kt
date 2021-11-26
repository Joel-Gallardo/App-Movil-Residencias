package com.example.demo_gobierna_ciudadano

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.demo_gobierna_ciudadano.databinding.ActivityHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

enum class ProviderType {
    BASIC
}

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //recuperar parametros que pase de actividad auth al iniciar sesion
        val bundle = intent.extras
        val email = bundle?.getString("email")
        val provider = bundle?.getString("provider")

        //setup
        setup(email ?: "",provider ?: "")


        //Guardado de datos para mantener sesion iniciada
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        prefs.putString("email", email)
        prefs.putString("provider", provider)
        prefs.apply()
    }

    //desavilitar btn back
    override fun onBackPressed() {
        //super.onBackPressed()
    }


    private fun setup(email: String, provider: String) {
        binding.nombreUsuarioTextView.text = "Bienvenido: $email"

        //boton cerrar sesion
        binding.logOutButton.setOnClickListener {

            //borrar datos locales de sesion activa
            val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
            prefs.clear()
            prefs.apply()

            //desloguearse de firebase
            FirebaseAuth.getInstance().signOut()

            //ir a AuthActivity
            val AuthActivityIntent = Intent(this, AuthActivity::class.java)
            startActivity(AuthActivityIntent)

        }

        //boton Alumbrado
        binding.btnAlumbrado.setOnClickListener {

            //ir a reporte de Alumbrado
            val AlumbradoIntent = Intent(this, LlenarReporteActivity::class.java).apply{
                putExtra("tipoDeReporte", "Alumbrado")
                putExtra("email", email)
                putExtra("provider", provider)
            }
            startActivity(AlumbradoIntent)
        }

        //boton vialidad y calle
        binding.btnVialidadYCalle.setOnClickListener {

            //ir a reporte de Vialidad y calle
            val VialidadYCalleIntent = Intent(this, LlenarReporteActivity::class.java).apply{
                putExtra("tipoDeReporte", "Vialidad y Calle")
                putExtra("email", email)
                putExtra("provider", provider)
            }
            startActivity(VialidadYCalleIntent)
        }

        //boton Agua y Drenaje
        binding.btnAguaYDrenaje.setOnClickListener {

            //ir a reporte de Agua y Drenaje
            val AguayDrenajeIntent = Intent(this, LlenarReporteActivity::class.java).apply{
                putExtra("tipoDeReporte", "Agua y Drenaje")
                putExtra("email", email)
                putExtra("provider", provider)
            }
            startActivity(AguayDrenajeIntent)
        }

        //boton Aseo y Limpieza
        binding.btnAseoYLimpieza.setOnClickListener {

            //ir a reporte de Aseo y Limpieza
            val AseoYLimpiezaIntent = Intent(this, LlenarReporteActivity::class.java).apply{
                putExtra("tipoDeReporte", "Aseo y Limpieza")
                putExtra("email", email)
                putExtra("provider", provider)
            }
            startActivity(AseoYLimpiezaIntent)
        }
    }
}