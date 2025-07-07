package fr.sabry.jp2codec

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import fr.sabry.ipox.resizer.IPOXResizer
import fr.sabry.ipox.transformer.IPOXTransform
import fr.sabry.jp2.JP2Decoder
import fr.sabry.jp2.JP2Encoder
import fr.sabry.jp2codec.databinding.ActivityMainBinding
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(getLayoutInflater())
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        procImage()
    }

    private fun procImage() {
        try {
            val photo = assets.open("portrait.jpg")
            val bitmap = BitmapFactory.decodeStream(photo)

            binding.original.setImageBitmap(bitmap)
            binding.textOriginal.text = "Original (${bitmap.width} x ${bitmap.height}) ${bitmap.byteCount/1024} KBytes"

            // Resize Image
            val resized = IPOXResizer(bitmap).changeRatio().resizeByWidth(140).bitmap

            // Convert Image (GrayScale)
            val encoded = IPOXTransform(resized).grayscale().bitmap

            // JP2
            val jp2000: ByteArray = JP2Encoder(encoded).setVisualQuality(50F).encode()
            val decoded: Bitmap = JP2Decoder(jp2000).decode()

            binding.encoded.setImageBitmap(decoded)
            binding.textEncoded.text = "JP2 (${decoded.width} x ${decoded.height}) ${jp2000.size} Bytes"

        } catch (ex: Exception) {
            Timber.e("===> ${ex.message}")
            ex.printStackTrace()
        }
    }
}