package com.naci.sdktestapplication

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.naci.sdktestlibrary.ConsumeTokenListener
import com.naci.sdktestlibrary.SDKStarter

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val imgProfile = findViewById<ImageView>(R.id.img_profile)

        /*val views = RemoteViews(this.packageName, R.layout.activity_main)

        Glide.with(this).asBitmap().load("http://www.gravatar.com/avatar/?d=identicon")
            .into(object : CustomTarget<Bitmap?>(96, 96) {
                override fun onLoadCleared(placeholder: Drawable?) {}
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap?>?
                ) {
                    views.setImageViewBitmap(R.id.img_profile, resource)
                }
            })*/

        SDKStarter.startSDKForSubmitConsumer(this, "MERCHANT_NAME", object : ConsumeTokenListener {
            override fun onSuccess(first6: String, last2: String) {
                Toast.makeText(this@MainActivity, "first6: $first6", Toast.LENGTH_LONG).show()
            }

            override fun onCancelled() {
                Toast.makeText(this@MainActivity, "onCancelled", Toast.LENGTH_LONG).show()
            }

            override fun onFailure(errId: Int, errMessage: String) {
                Toast.makeText(this@MainActivity, "onFailure", Toast.LENGTH_LONG).show()
            }

        })
    }
}