package com.example.eventfinder

import android.content.Context
import android.graphics.Bitmap
import android.media.Image
import android.util.LruCache
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.ImageLoader
import com.android.volley.toolbox.Volley

object VolleySingleton {
    private lateinit var applicationContext: Context
    private val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(applicationContext)
    }
//    val imageLoader: ImageLoader by lazy {
//        ImageLoader(requestQueue, object : ImageLoader.ImageCache {
//            private val cache = LruCache<String, Bitmap>(20)
//            override fun getBitmap(url: String?): Bitmap? {
//                return cache.get(url)
//            }
//
//            override fun putBitmap(url: String?, bitmap: Bitmap?) {
//                cache.put(url, bitmap)
//            }
//        })
//    }

    fun init(context: Context) {
        this.applicationContext = context.applicationContext
    }

    fun<T> pushQ(req: Request<T>) {
        requestQueue.add(req)
    }
    fun cancelAll(filter: Any) {
        when(filter) {
            is String -> requestQueue.cancelAll(filter)
            is Boolean -> requestQueue.cancelAll { true }
        }
    }
}