package com.jeva.jeva.images


import android.app.Activity
import android.app.AlertDialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import com.jeva.jeva.database.Database
import com.jeva.jeva.GestionarPermisos
import com.jeva.jeva.R
import com.jeva.jeva.home.EditRouteActivity
import com.jeva.jeva.images.adapters.ImageAdapter


class dataPointMenu {

    companion object {
        lateinit var title:String
        lateinit var description:String
        lateinit var fotos: ArrayList<Pair<String,Uri>>

        lateinit var dialogBuilder: AlertDialog.Builder
        lateinit var popUp: View
        lateinit var adapter: ImageAdapter
        private var photoId: ArrayList<String> = ArrayList()
        lateinit var activity: Activity
        lateinit var context: Context

        lateinit var routeId: String
        lateinit var markerId: String
        private var db = Database()

        val REQUEST_CODE = 1

        fun setInfo(title:String, description:String, routeId: String, markerId: String, activity: Activity, context: Context, layoutInflater: LayoutInflater) {
            this.title = title
            this.description = description
            this.fotos = ArrayList()
            this.markerId = markerId
            this.routeId = routeId
            this.activity = activity
            this.context = context
            this.popUp = layoutInflater.inflate(R.layout.popup_marker,null)
        }

        fun showMenu(editable: Boolean) {
            dialogBuilder = AlertDialog.Builder(activity)
            adapter = ImageAdapter(context, fotos, editable)
            var isUpdate = true

            //añadimos nombre y descripción
            var puntoname: EditText = popUp.findViewById(R.id.puntoName)
            var puntodescripcion: EditText = popUp.findViewById(R.id.puntoDescription)
            (puntoname as TextView).text = title
            (puntodescripcion as TextView).text = description

            editable(puntoname, editable)
            editable(puntodescripcion, editable)

            //creación de galería de imágenes
            var photogrid: GridView = popUp.findViewById(R.id.photo_grid)
            photogrid.adapter = adapter

            if(editable) {
                val uri : Uri = toUri(R.drawable.imagen_anadir)
                this.fotos.add(Pair("anadir",uri))
                this.photoId.add("editable")
                photogrid.setOnItemLongClickListener { parent, view, position, id ->
                    if (position+1 != adapter.getDataSource().size) {
                        val photoToDelete: String = adapter.getDataSource().get(position).first
                        deleteImage(photoToDelete,position)
                    }
                    true
                }
            }

            loadImagesFromDB(routeId, markerId, adapter)

            //creamos el cuadro de diálogo y añadimos listener al boton
            dialogBuilder.setView(popUp)
            var dialog = dialogBuilder.create()

            var cerrar: Button = popUp.findViewById(R.id.cerrar)
            cerrar.setOnClickListener {
                dialog.dismiss()
            }

            val deleteMarker: Button = popUp.findViewById(R.id.borrar_marcador)
            val spacebar: View = popUp.findViewById(R.id.spaceBetweenBtns)
            if(!editable){
                deleteMarker.visibility = View.GONE
                spacebar.visibility = View.GONE
            }
            deleteMarker.setOnClickListener {
                isUpdate = false
                EditRouteActivity.deleteMarker()
                dialog.dismiss()
            }

            photogrid.setOnItemClickListener { parent, view, position, id ->
                if(editable) {
                    if (position+1 != adapter.getDataSource().size) {
                        dialog.dismiss()
                        showImages(position,editable)
                    } else {
                        GestionarPermisos.requestStoragePermissions(activity)
                        if (GestionarPermisos.accessStorageIsGranted(activity)) {
                            pickImageFromGallery()
                        }
                    }
                } else {
                    dialog.dismiss()
                    showImages(position, editable)
                }
            }

            dialog.setOnDismissListener {
                if (isUpdate and editable){
                    EditRouteActivity.updateMarkersRoute(tit = puntoname.text.toString(), desc = puntodescripcion.text.toString())
                }
            }

            //mostramos el dialogo
            dialog.show()
        }

        private fun showImages(position: Int, editable: Boolean) {
            val intent = Intent(this.context,swipeImages :: class.java).apply {
                putExtra("pos", position)
                putExtra("edit", editable)
                putExtra("title", title)
            }
            this.activity.startActivity(intent)
        }

        //CARGAR IMÁGENES DE DB
        private fun loadImagesFromDB(routeId: String, markerId: String, adapter: ImageAdapter) {
            db.getMarkerPhotosRefs(routeId,markerId) {
                if (it!=null) {
                    it.forEach { it2 ->
                        it2.downloadUrl.addOnSuccessListener { Ref ->
                            adapter.add(it2.name, Ref)
                        }
                    }
                } else {
                    Toast.makeText(activity, context.getString(R.string.checkInternet), Toast.LENGTH_SHORT).show()
                }
            }
        }

        //SUBIDA DE IMÁGENES
        fun uploadImageShow(imageRef: Uri) {
            db.uploadMarkerPhoto(imageRef, routeId, markerId, context) {
                if(it != null) {
                    adapter.add(it,imageRef)
                } else {
                    Toast.makeText(activity, context.getString(R.string.errorUploadPhoto), Toast.LENGTH_SHORT).show()
                }
            }
        }

        //TOMAR IMAGEN DE GALERÍA
        private fun pickImageFromGallery() {
            //Cuando obtiene la imagen de la galería ActivityCompat llama a la clase del activity
            //(HomeActivity) al método  onActivityResult. Desde ahí se llamará al metodo uploadImageShow
            //de esta clase.
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            ActivityCompat.startActivityForResult(
                activity,
                intent,
                REQUEST_CODE,
                null
            )
        }

        //BORRAR IMAGEN
        private fun deleteImage(photoId: String, position: Int) {
            db.deleteMarkerPhoto(routeId,markerId,photoId) {
                if(it) {
                    adapter.remove(position)
                } else {
                    Toast.makeText(activity, context.getString(R.string.cannotDeletePhoto), Toast.LENGTH_SHORT).show()
                }
            }
        }

        //CAMBIA TAMAÑO GRID EN FUNCIÓN NUM FOTOS
        fun refreshTam() {
            var photogrid: GridView = popUp.findViewById(R.id.photo_grid)
            var tam: Int = DpToPixels(160)
            if (adapter.getDataSource().size>2) {
                tam = DpToPixels(300)
            }
            photogrid.layoutParams.height = tam
            photogrid.requestLayout()
        }

        //DADO UN INT RETORNA Uri
        private fun toUri(resource: Int) : Uri {
            val resources: Resources = context.resources
            val uri = Uri.Builder()
                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .authority(resources.getResourcePackageName(resource))
                .appendPath(resources.getResourceTypeName(resource))
                .appendPath(resources.getResourceEntryName(resource))
                .build()
            return uri
        }

        //AUXILIAR
        private fun editable(cuadroTexto: EditText, editable: Boolean) {
            cuadroTexto.setFocusable(editable)
            cuadroTexto.setClickable(editable)
            cuadroTexto.setFocusableInTouchMode(editable)
            cuadroTexto.setLongClickable(editable)
        }

        private fun DpToPixels(dp: Int) : Int {
            val escala: Float = context.resources.displayMetrics.density;
            var tam: Int = (dp * escala + 0.5f).toInt()
            return tam
        }

     }

}