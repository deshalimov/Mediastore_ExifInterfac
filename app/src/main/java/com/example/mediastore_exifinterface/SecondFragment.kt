package com.example.mediastore_exifinterface

import android.app.Activity
import android.app.RecoverableSecurityException
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.exifinterface.media.ExifInterface
import androidx.navigation.fragment.findNavController
import com.example.mediastore_exifinterface.databinding.FragmentSecondBinding
import java.io.IOException

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val contentResolver = requireContext().contentResolver

        binding.imageViewEdit.setImageURI(FirstFragment.uri)
        contentResolver.openInputStream(FirstFragment.uri!!)?.use { stream ->
            val exif = ExifInterface(stream)
            binding.apply {
                creationDataEt.setText(exif.getAttribute(ExifInterface.TAG_DATETIME))
                latitudeEt.setText(
                    exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE) + ' ' + exif.getAttribute(
                        ExifInterface.TAG_GPS_LATITUDE_REF
                    )
                )
                longitudeEt.setText(
                    exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE) + ' ' + exif.getAttribute(
                        ExifInterface.TAG_GPS_LONGITUDE_REF
                    )
                )
                creationDeviceEt.setText(exif.getAttribute(ExifInterface.TAG_MAKE))
                modelCreationDeviceEt.setText(exif.getAttribute(ExifInterface.TAG_MODEL))
            }
        }

        binding.saveChangeBtn.setOnClickListener {
            try {
                saveChanges()
            } catch (securityException: SecurityException) {
                val recoverableSecurityException =
                    securityException as? RecoverableSecurityException
                        ?: throw securityException

                val intentSender = recoverableSecurityException.userAction.actionIntent.intentSender
                requestUri.launch(IntentSenderRequest.Builder(intentSender).build())
            }
            catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private var requestUri = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result != null && result.resultCode == Activity.RESULT_OK) {
            saveChanges()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun saveData() {
        val contentResolver = requireContext().contentResolver
        contentResolver.openFileDescriptor(FirstFragment.uri!!, "rw", null)?.use { fileDescriptor ->
            val exif = ExifInterface(fileDescriptor.fileDescriptor)
            binding.apply {
                exif.setAttribute(ExifInterface.TAG_DATETIME, creationDataEt.text.toString())
                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, saveCoordinate(latitudeEt.text.toString())[0])
                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, saveCoordinate(latitudeEt.text.toString())[1])
                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, saveCoordinate(longitudeEt.text.toString())[0])
                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, saveCoordinate(longitudeEt.text.toString())[1])
                exif.setAttribute(ExifInterface.TAG_MAKE, creationDeviceEt.text.toString())
                exif.setAttribute(ExifInterface.TAG_MODEL, modelCreationDeviceEt.text.toString())
            }
            exif.saveAttributes()
        }
        findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
    }

    private fun saveChanges() {
        val contentResolver = requireContext().contentResolver
        val fileDescriptor = contentResolver.openFileDescriptor(FirstFragment.uri!!, "rw", null)!!
        val exif = ExifInterface(fileDescriptor.fileDescriptor)
        binding.apply {
            exif.setAttribute(ExifInterface.TAG_DATETIME, creationDataEt.text.toString())
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, saveCoordinate(latitudeEt.text.toString())[0])
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, saveCoordinate(latitudeEt.text.toString())[1])
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, saveCoordinate(longitudeEt.text.toString())[0])
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, saveCoordinate(longitudeEt.text.toString())[1])
            exif.setAttribute(ExifInterface.TAG_MAKE, creationDeviceEt.text.toString())
            exif.setAttribute(ExifInterface.TAG_MODEL, modelCreationDeviceEt.text.toString())
        }
        exif.saveAttributes()
        fileDescriptor.close()
        findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
    }

    private fun saveCoordinate(attribute: String): List<String> {
        return if (attribute.isBlank()) {
            listOf("", "")
        } else {
            val coordinate = attribute.substring(0 until attribute.indexOf(' '))
            listOf("$coordinate", attribute.substring(attribute.length - 1))
        }

    }
}