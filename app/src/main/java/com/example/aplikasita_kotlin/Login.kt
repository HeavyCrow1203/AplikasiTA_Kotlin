package com.example.aplikasita_kotlin

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.aplikasita_kotlin.etc.LoadingDialog
import com.google.firebase.auth.FirebaseAuth

class Login : AppCompatActivity(), View.OnClickListener {

    private lateinit var masuk: Button
    private lateinit var registrasi: Button
    private lateinit var lupa_passwd: TextView
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        masuk = findViewById(R.id.login_tombol_masuk)
        registrasi = findViewById(R.id.login_tombol_daftar)
        lupa_passwd = findViewById(R.id.login_ganti_password)
        email = findViewById(R.id.login_email)
        password = findViewById(R.id.login_password)
        loadingDialog = LoadingDialog(this)
        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            startActivity(Intent(applicationContext, MainActivity::class.java))
            finish()
        }

        masuk.setOnClickListener(this)
        registrasi.setOnClickListener(this)
        lupa_passwd.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        val pilih = v?.id
        when (pilih) {
            R.id.login_tombol_masuk -> signIn()
            R.id.login_tombol_daftar -> startActivity(Intent(this, Registrasi::class.java))
            R.id.login_ganti_password -> startActivity(Intent(this, Lupa_Passwd::class.java))
        }
    }

    private fun signIn() {
        val get_email = email.text.toString()
        val get_pass = password.text.toString()

        if (get_email.isEmpty()) {
            errorEditText(email)
            return
        }
        if (get_pass.isEmpty()) {
            errorEditText(password)
            return
        }
        loadingDialog.startLoadingDialog()
        auth.signInWithEmailAndPassword(get_email, get_pass).addOnCompleteListener { task ->
            loadingDialog.dismissDialog()
            if (task.isSuccessful) {
                val user = auth.currentUser
                Toast.makeText(this, "Berhasil Login", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                val dialog = AlertDialog.Builder(this)
                    .setTitle("Username dan Password tidak cocok, Coba Lagi")
                    .setNegativeButton("Tutup") { dialogInterface, _ ->
                        dialogInterface.dismiss()
                        loadingDialog.dismissDialog()
                    }.create()
                dialog.show()
            }
        }
    }

    private fun errorEditText(editText: EditText) {
        editText.error = "Field tidak boleh kosong"
        editText.requestFocus()
    }
}