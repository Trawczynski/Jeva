package com.jeva.jeva.home.tabs

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.jeva.jeva.*
import com.jeva.jeva.home.EditRouteActivity
import com.jeva.jeva.home.LocaleHelper
import com.jeva.jeva.home.ShowRouteActivity
import com.jeva.jeva.database.Database
import kotlinx.android.synthetic.main.fragment_my_routes.*
import java.io.Serializable

class MyRoutesFragment : Fragment(),Serializable {

    private val db : Database = Database()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    Log.e("Soy el boton patras", "Toy desactivado")
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root : View = inflater.inflate(R.layout.fragment_my_routes, container, false)
        val buttonContainer = root.findViewById(R.id.myRoutesButtonContainer) as LinearLayout

        addRoutesButtons(buttonContainer)
        setHasOptionsMenu(true)

        return root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        myRoutesBtnNewRoute.setOnClickListener {
            db.newRoute(listOf()){
                id ->
                if (id != null){
                    val intent = Intent(context, EditRouteActivity :: class.java).apply {
                        putExtra("newRoute", true)
                        putExtra("idRoute", id)
                    }
                    startActivity(intent)
                }
                else{
                    Log.e("ErrorDB", "Ha habido error en la subida")
                }
            }
        }
    }


    private fun addRoutesButtons(btnContainer: LinearLayout) {
        db.getCurrentUserRoutes { routes ->
            routes?.forEach { route ->
                val routeBtn = Button(context)
                val nameRoute = route["title"] as String
                routeBtn.text = "Este es el titulo: $nameRoute"//routeData["description"] as String
                routeBtn.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

                routeBtn.setOnClickListener{
                    val intent = Intent(context, ShowRouteActivity :: class.java).apply {
                        putExtra("routeData",  route as Serializable)
                    }
                    startActivity(intent)
                }

                btnContainer.addView(routeBtn)
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.settings_menu,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){

            R.id.set_Spanish -> {
                if(!item.isChecked){
                    item.isChecked = true
                    val cont = LocaleHelper.setLocale(context, "es");
                    val resources = cont?.resources

                }
            }

            R.id.set_English -> {
                if (!item.isChecked) {
                    item.isChecked = true
                    val cont = LocaleHelper.setLocale(context, "en");
                    val resources = cont?.resources

                }
            }
        }

        return super.onOptionsItemSelected(item)
    }



}