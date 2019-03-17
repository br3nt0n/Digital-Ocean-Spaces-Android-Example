package thecloudhub.com.digitaloceanspacesexample

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.internal.StaticCredentialsProvider
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Region
import com.amazonaws.services.s3.AmazonS3Client
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.util.*

interface SpaceRegionRepresentable {
    fun endpoint(): String
}

/**
 * Represents a region in which a Digital Ocean Space can be created
 */
enum class SpaceRegion: SpaceRegionRepresentable {
    SFO {
        override fun endpoint(): String {
            return "https://sfo2.digitaloceanspaces.com"
        }
    }, AMS {
        override fun endpoint(): String {
            return "https://ams3.digitaloceanspaces.com"
        }
    }, SGP {
        override fun endpoint(): String {
            return "https://sgp1.digitaloceanspaces.com"
        }
    }
}

class SpacesFileRepository (context: Context) {
    private val accesskey = "YOUR_KEY_HERE"
    private val secretkey = "YOUR_SECRET_HERE"
    private val spacename = "YOUR_BUCKET_NAME_HERE"
    private val spaceregion = SpaceRegion.SFO

    private val filename = "example_image"
    private val filetype = "jpg"

    private var transferUtility: TransferUtility
    private var appContext: Context

    init {
        val credentials = StaticCredentialsProvider(BasicAWSCredentials(accesskey, secretkey))
        val client = AmazonS3Client(credentials, Region.getRegion("us-east-1"))
        client.endpoint = spaceregion.endpoint()

        transferUtility = TransferUtility.builder().s3Client(client).context(context).build()
        appContext = context
    }

    /**
     * Converts a APK resource to a file for uploading with the S3 SDK
     */
    private fun convertResourceToFile(): File {
        val exampleIdentifier = appContext.resources.getIdentifier(filename, "drawable", appContext.packageName)
        val exampleBitmap = BitmapFactory.decodeResource(appContext.resources, exampleIdentifier)

        val exampleFile = File(appContext.filesDir, Date().toString())
        exampleFile.createNewFile()

        val outputStream = ByteArrayOutputStream()
        exampleBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        val exampleBitmapData = outputStream.toByteArray()

        val fileOutputStream = FileOutputStream(exampleFile)
        fileOutputStream.write(exampleBitmapData)
        fileOutputStream.flush()
        fileOutputStream.close()

        return exampleFile
    }

    /**
     * Uploads the example file to a DO Space
     */
    fun uploadExampleFile(){
        //Starts the upload of our file
        var listener = transferUtility.upload(spacename, "$filename.$filetype", convertResourceToFile())

        //Listens to the file upload progress, or any errors that might occur
        listener.setTransferListener(object: TransferListener {
            override fun onError(id: Int, ex: Exception?) {
                Log.e("S3 Upload", ex.toString())
            }

            override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
                Log.i("S3 Upload", "Progress ${((bytesCurrent/bytesTotal)*100)}")
            }

            override fun onStateChanged(id: Int, state: TransferState?) {
                if (state == TransferState.COMPLETED){
                    Log.i("S3 Upload", "Completed")
                }
            }
        })
    }

    /**
     * Downloads example file from a DO Space
     */
    fun downloadExampleFile(callback: (File?, Exception?) -> Unit) {
        //Create a local File object to save the remote file to
        val file = File("${appContext.cacheDir}/$filename.$filetype")

        //Download the file from DO Space
        var listener = transferUtility.download(spacename, "$filename.$filetype", file)

        //Listen to the progress of the download, and call the callback when the download is complete
        listener.setTransferListener(object: TransferListener {
            override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
                Log.i("S3 Download", "Progress ${((bytesCurrent/bytesTotal)*100)}")
            }

            override fun onStateChanged(id: Int, state: TransferState?) {
                if (state == TransferState.COMPLETED){
                    Log.i("S3 Download", "Completed")
                    callback(file, null)
                }
            }

            override fun onError(id: Int, ex: Exception?) {
                Log.e("S3 Download", ex.toString())
                callback(null, ex)
            }
        })
    }
}