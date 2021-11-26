package com.example.demo_gobierna_ciudadano

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.example.demo_gobierna_ciudadano.databinding.ActivityAuthBinding
import com.google.firebase.auth.FirebaseAuth

class AuthActivity : AppCompatActivity() {

    //view binding
    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        //splash
        setTheme(R.style.Theme_DEMO_Gobierna_Ciudadano)

        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setup()
        sesion()
    }

    override fun onStart() {
        super.onStart()

        binding.authLayout.visibility = View.VISIBLE
    }

    //comprobar si hay una sesin iniciada
    private fun sesion() {

        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val email :String? = prefs.getString("email", null)
        val provider :String? = prefs.getString("provider", null)

        if (email != null && provider != null) {
            binding.authLayout.visibility = View.INVISIBLE
            showHome(email, ProviderType.valueOf(provider))
        }
    }

    //desavilitar btn back
    override fun onBackPressed() {
        //super.onBackPressed()
    }

    private fun setup() {

        //boton registrarse
        binding.signUpButton.setOnClickListener {
            if (binding.emailEditText.text.isNotEmpty() && binding.PasswordEditText.text.isNotEmpty()) {
                if (binding.PasswordEditText.text.length >= 6) {

                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(
                        binding.emailEditText.text.toString(),
                        binding.PasswordEditText.text.toString()
                    ).addOnCompleteListener {
                        if (it.isSuccessful) {
                            showHome(it.result?.user?.email ?: "", ProviderType.BASIC)
                        }else {
                            showAlertError()
                        }
                    }

                } else {
                    showAlertIvalidPass()
                }

            } else {
                showAlertVacios()
            }
        }


        //boton acceder
        binding.LogInButton.setOnClickListener {
            if (binding.emailEditText.text.isNotEmpty() && binding.PasswordEditText.text.isNotEmpty()) {

                FirebaseAuth.getInstance().signInWithEmailAndPassword(
                    binding.emailEditText.text.toString(),
                    binding.PasswordEditText.text.toString()
                ).addOnCompleteListener {
                    if (it.isSuccessful) {
                        showHome(it.result?.user?.email ?: "", ProviderType.BASIC)
                    } else {
                        showAlertDatosErroneos()
                    }
                }
            } else {
                showAlertVacios()
            }
        }
    }

    //funcion para ir a activity home si el usuario y contraseña son correctos
    private fun showHome(email: String, provider: ProviderType) {
        val homeIntent = Intent(this, HomeActivity::class.java).apply {
            putExtra("email", email)
            putExtra("provider", provider.name)
        }
        startActivity(homeIntent)
    }


    ////ALERTAS
    private fun showAlertDatosErroneos() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("El Correo o Contraseña son incorrectos")
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showAlertIvalidPass() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("La Contraseña debe tener por lo menos 6 caracteres")
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showAlertVacios() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Alerta")
        builder.setMessage("Rellena todos los campos para continuar")
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    //mostrar alerta de error
    private fun showAlertError() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Se ha producido un error autenticando al usuario :(")
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }


}