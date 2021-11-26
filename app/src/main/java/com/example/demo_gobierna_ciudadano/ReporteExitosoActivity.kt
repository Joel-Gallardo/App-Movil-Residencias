package com.example.demo_gobierna_ciudadano

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.demo_gobierna_ciudadano.databinding.ActivityReporteExitosoBinding

class ReporteExitosoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReporteExitosoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReporteExitosoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Recuperar datos de usuario y folio de reporte
        val bundle = intent.extras
        val email = bundle?.getString("email")
        val provider = bundle?.getString("provider")
        val folioRandom = bundle?.getString("folio")

        setup(email?: "",provider?: "", folioRandom?: "")
    }

    //desavilitar btn back
    override fun onBackPressed() {
        //super.onBackPressed()
    }

    private fun setup(email: String, provider: String, folioRandom: String) {

        binding.folioTextView.text = "FOLIO: $folioRandom"

        //btn volver a HomeActivity
        binding.buttonVolverHome.setOnClickListener {
            val homeIntent = Intent(this, HomeActivity::class.java).apply {
                putExtra("email", email)
                putExtra("provider", provider)
            }
            startActivity(homeIntent)
        }
    }
}