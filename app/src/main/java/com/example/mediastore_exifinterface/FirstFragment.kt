package com.example.mediastore_exifinterface

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mediastore_exifinterface.databinding.FragmentFirstBinding

/** The request code for requesting [Manifest.permission.READ_EXTERNAL_STORAGE] permission. */
private const val READ_EXTERNAL_STORAGE_REQUEST = 0x1045

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    companion object {
        var uri: Uri? = null
    }

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        uri?.let { loadingData(it) }

        binding.imageSelectionBtn.setOnClickListener {
            if (haveStoragePermission()) {
                pickImage()
            } else {
                requestPermission()
            }
//            pickImage()
        }

        binding.editBtn.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun haveStoragePermission() =
        ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PERMISSION_GRANTED

    private fun requestPermission() {
        if (!haveStoragePermission()) {
            val permissions = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_MEDIA_LOCATION
            )
            ActivityCompat.requestPermissions(requireActivity(), permissions, READ_EXTERNAL_STORAGE_REQUEST)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            READ_EXTERNAL_STORAGE_REQUEST -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED) {
                    pickImage()
                }
                return
            }
        }
    }

    private fun pickImage() {
        // Показываем все программы для запуска
//        val intent = Intent(MediaStore.ACTION_PICK_IMAGES)
//        val intent = Intent(Intent.ACTION_PICK)
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "image/*";
        requestUri.launch(intent)
    }

    private var requestUri = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val contentResolver = requireContext().contentResolver

        if (result != null && result.resultCode == Activity.RESULT_OK) {
            result.data?.let { intent ->
                intent.data?.let { fileUri ->
                    binding.constraintLayout.visibility = View.VISIBLE
                    binding.imageView.setImageURI(fileUri)
                    contentResolver.openInputStream(fileUri)?.use { stream ->
                        val exif = ExifInterface(stream)
                        binding.apply {
                            creationDateTv.text = exif.getAttribute(ExifInterface.TAG_DATETIME)
                            latitudeTv.text = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE) + ' ' + exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF)
                            longitudeTv.text = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE) + ' ' + exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF)
                            creationDeviceTv.text = exif.getAttribute(ExifInterface.TAG_MAKE)
                            modelCreationDeviceTv.text = exif.getAttribute(ExifInterface.TAG_MODEL)
                        }
                    }
                    uri = fileUri
                }
            }
        }
    }

    private fun loadingData(uriImg: Uri) {
        val contentResolver = requireContext().contentResolver
        binding.constraintLayout.visibility = View.VISIBLE
        binding.imageView.setImageURI(uriImg)
        contentResolver.openInputStream(uriImg!!)?.use { stream ->
            val exif = ExifInterface(stream)
            binding.apply {
                creationDateTv.text = exif.getAttribute(ExifInterface.TAG_DATETIME)
                latitudeTv.text = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE) + ' ' + exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF)
                longitudeTv.text = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE) + ' ' + exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF)
                creationDeviceTv.text = exif.getAttribute(ExifInterface.TAG_MAKE)
                modelCreationDeviceTv.text = exif.getAttribute(ExifInterface.TAG_MODEL)
            }
        }
    }
}