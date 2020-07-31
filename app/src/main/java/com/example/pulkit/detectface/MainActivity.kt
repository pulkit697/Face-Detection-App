package com.example.pulkit.detectface

import android.app.Activity
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionPoint
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceContour
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException

class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    fun chooseImage(view:View) {
        Intent(Intent.ACTION_GET_CONTENT).also {
            it.type = "image/*"
            startActivityForResult(Intent.createChooser(it,"Choose image"),123)
            Log.d("debug", "button clicked")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?){
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode== Activity.RESULT_OK && requestCode==123)
        {
            Log.d("debug", "result ok")
            ivImage.setImageURI(data?.data)
            val image: FirebaseVisionImage
            try {
                val bmp:Bitmap
                if(Build.VERSION.SDK_INT >= 29) {
                    val source = ImageDecoder.createSource(this.contentResolver,data?.data!!)
                    bmp = ImageDecoder.decodeBitmap(source)
                }
                else {
                  bmp = MediaStore.Images.Media.getBitmap(this.contentResolver,data?.data!!)
                }
                val mutableBmp = Bitmap.createBitmap(bmp.width,bmp.height,Bitmap.Config.RGB_565)
                val canvas = Canvas(mutableBmp)
                canvas.drawBitmap(bmp,0f,0f,null)
                val paint = Paint()
                paint.apply {
                    color = Color.YELLOW
                    style = Paint.Style.STROKE
                }
                Log.d("debug", "bitmap created")

                image = FirebaseVisionImage.fromFilePath(applicationContext, data.data!!)
                //canvas.drawRoundRect( RectF(100F, 100F, 200F, 200F),2f,2f, paint);


                val options = FirebaseVisionFaceDetectorOptions.Builder().build()
                val detector = FirebaseVision.getInstance().getVisionFaceDetector(options)

                val result =
                    detector.detectInImage(image)
                        .addOnSuccessListener {
                            // Task completed successfully
                            // ...
                            Log.d("debug", "success listener")
                            for(face in it)
                            {
                                val bounds = face.boundingBox


                                canvas.drawRect(bounds,paint)

                                ivImage.setImageDrawable(BitmapDrawable(resources,mutableBmp))
                                Log.d("debug", bounds.toString())
                                Log.d("debug", "box drawn")
                                val rotY = face.headEulerAngleY  // Head is rotated to the right rotY degrees
                                val rotZ = face.headEulerAngleZ  // Head is tilted sideways rotZ degrees

                                // If landmark detection was enabled (mouth, ears, eyes, cheeks, and
                                // nose available):
                                val leftEar = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EAR)
                                val leftEarPos:FirebaseVisionPoint
                                if(leftEar != null)
                                    leftEarPos = leftEar.position
                                val leftEyeContour = face.getContour(FirebaseVisionFaceContour.LEFT_EYE).points
                                val upperLipBottomContour = face.getContour(FirebaseVisionFaceContour.UPPER_LIP_BOTTOM).points

                                val smileProb: Float
                                val rightEyeOpenProb:Float
                                if(face.smilingProbability != FirebaseVisionFace.UNCOMPUTED_PROBABILITY)
                                    smileProb = face.smilingProbability
                                if(face.rightEyeOpenProbability != FirebaseVisionFace.UNCOMPUTED_PROBABILITY)
                                    rightEyeOpenProb = face.rightEyeOpenProbability

                                val id:Int
                                if(face.trackingId != FirebaseVisionFace.INVALID_ID)
                                    id = face.trackingId
                            }
                            }
                        .addOnFailureListener {
                            // Task failed with an exception
                            // ...
                            Log.d("debug", "error")
                        }

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}