package com.example.aplikasita_kotlin

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.example.aplikasita_kotlin.database.userApps
import com.example.aplikasita_kotlin.etc.LoadingDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class Registrasi : AppCompatActivity(), View.OnClickListener {

    private lateinit var username: EditText
    private lateinit var email: EditText
    private lateinit var pass: EditText
    private lateinit var daftar_akun: Button
    private lateinit var login: Button
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var auth: FirebaseAuth
    private lateinit var db: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrasi)

        username = findViewById(R.id.registrasi_username)
        email = findViewById(R.id.registrasi_email)
        pass = findViewById(R.id.registrasi_password)
        daftar_akun = findViewById(R.id.registrasi_tombol_daftar)
        login = findViewById(R.id.registrasi_tombol_login)

        loadingDialog = LoadingDialog(this)
        db = FirebaseDatabase.getInstance().getReference("Users")
        auth = FirebaseAuth.getInstance()

        login.setOnClickListener(this)
        daftar_akun.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        val pilih = v?.id
        when (pilih) {
            R.id.registrasi_tombol_daftar -> signUp()
            R.id.registrasi_tombol_login -> startActivity(Intent(this, Login::class.java))
        }
    }

    private fun signUp() {
        val Email = email.text.toString()
        val Username = username.text.toString()
        val Password = pass.text.toString()

        if (Email.isEmpty()) {
            editTextAlert(email, "Field tidak boleh kosong")
            return
        }
        if (Username.isEmpty()) {
            editTextAlert(username, "Field tidak boleh kosong")
            return
        }
        if (Password.isEmpty()) {
            editTextAlert(pass, "Field tidak boleh kosong")
            return
        }
        if (Password.length < 8) {
            editTextAlert(pass, "Password minimal 8 karakter")
            return
        }
        loadingDialog.startLoadingDialog()
        auth.createUserWithEmailAndPassword(Email, Password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val users = userApps(Username, Email)
                db.child(FirebaseAuth.getInstance().currentUser!!.uid).setValue(users)
                    .addOnCompleteListener { task ->
                        loadingDialog.dismissDialog()
                        if (task.isSuccessful) {
                            pesanAlertDialog("Proses Registrasi Akun Berhasil, Silakan Menuju Halaman Login")
                        } else {
                            pesanAlertDialog("Proses Registrasi akun mengalami kegagalan, Silakan Coba Lagi")
                        }
                    }
            } else {
                pesanAlertDialog("Proses Registrasi akun mengalami kegagalan, Silakan Coba Lagi")
            }
        }
    }

    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
    }

    private fun editTextAlert(editText: EditText, pesanAlert: String) {
        editText.error = pesanAlert
        editText.requestFocus()
    }

    private fun pesanAlertDialog(message: String) {
        val dialog = AlertDialog.Builder(this)
            .setTitle(message)
            .setNegativeButton("Tutup") { dialogInterface, _ ->
                dialogInterface.dismiss()
                loadingDialog.dismissDialog()
            }.create()
        dialog.show()
    }
}