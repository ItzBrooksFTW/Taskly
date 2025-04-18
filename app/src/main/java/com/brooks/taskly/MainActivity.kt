package com.brooks.taskly

import android.content.Context
import android.os.Bundle
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.brooks.taskly.utils.*


class MainActivity : AppCompatActivity() {
    private lateinit var spinnerTheme : Spinner
    private lateinit var buttonNewTask : Button
    private lateinit var buttonAllTasks : Button
    private lateinit var textViewCompleted: TextView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        checkForPerms(this, activityResultRegistry)  //u novijim verzijama je potrebna dozvola za alarm i notifikacije pa se provjerava
        val selectedTheme= checkTheme(this)  //provjerava se koja je tema postavljena
        buttonNewTask = findViewById(R.id.buttonNewTask)
        buttonAllTasks =findViewById(R.id.buttonAllTasks)
        spinnerTheme = findViewById(R.id.spinnerTheme)
        textViewCompleted = findViewById(R.id.textViewCompleted)
        val buttonInfo = findViewById<ImageButton>(R.id.imageButtonInfo)
        buttonInfo.setOnClickListener {
            showInfoDialog()
        }



      val taskList= TaskStorage(this).loadTasks().toMutableList()
        val totalTasks= taskList.size
        val completedTasks= taskList.filter { it.isComplete }.size

        textViewCompleted.text=getString(R.string.completed_tasks, completedTasks, totalTasks)

        //stvara se spinner za odabir teme
        ArrayAdapter.createFromResource(
            this,
            R.array.dropdown_values_theme,
            R.layout.spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(R.layout.spinner_item)
            spinnerTheme.adapter = adapter
        }

        spinnerTheme.setSelection(selectedTheme)  //postavlja se odabrana tema iz memorije


        spinnerTheme.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                when (position){

                    0 ->{
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

                    }
                    1 ->{
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

                    }
                    2 ->{
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

                    }

                }
                //sprema se odabrana tema u memoriju
                val sharedPreferences = getSharedPreferences("ThemePrefs", Context.MODE_PRIVATE)
                with(sharedPreferences.edit()) {
                    putInt("selectedTheme", position)
                    apply()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // write code to perform some action
            }
        }

        //prebacivanje na zaslon za dodavanje novog zadatka
        buttonNewTask.setOnClickListener(){
            switchScreens(this, ActivityNewTask::class.java, true, null)

        }
        //prebacivanje na zaslon za pregled svih zadataka
        buttonAllTasks.setOnClickListener(){
            switchScreens(this, ActivityAllTasks::class.java, true, null)

        }



    }

    //funkcija za prikazivanje informacija o aplikaciji
    private fun showInfoDialog() {
        val messageTextView = TextView(this).apply {
            text = SpannableString(
                "${getString(R.string.about_app_text)}\n\n" +
                        "${getString(R.string.about_app_developer)}\n\n" +
                        "${getString(R.string.about_app_version)}\n\n" +
                        getString(R.string.about_app_github)
            )

           setPadding(50, 20, 50, 20)
            autoLinkMask = Linkify.WEB_URLS
            movementMethod = LinkMovementMethod.getInstance()
        }

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.about_app))
            .setView(messageTextView)
            .setPositiveButton(getString(R.string.about_app_confirm)) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()


    }
}

//TODO -uređivanje, -obavijest, -sortiranje,
// biranje alarma ili bez alarma (nije dodano),
// -promjena teme (samo treba par stvari zamijenit i dodat postavku da se moze mijenjat tema)