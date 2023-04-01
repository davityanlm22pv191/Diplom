package com.example.diplom.fragment.home

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.diplom.MainActivity
import com.example.diplom.R

class HomeFragment : Fragment(), HomeContract {

	private var lastSelectedImageOneBitmap: Bitmap? = null
	private var lastSelectedImageTwoBitmap: Bitmap? = null

	private companion object {
		const val PICK_IMAGE_ONE = 1
		const val PICK_IMAGE_TWO = 2
	}

	// region ==================== Lifecycle ====================

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		val view = inflater.inflate(R.layout.fragment_home, container, false)
		initUI(view)

		return view
	}

	// endregion

	// region ==================== Internal ====================

	private fun initUI(view: View) {
		setClickListeners(view)
	}

	private fun setClickListeners(view: View) {
		view.apply {
			val btnGalleryOne = findViewById<TextView>(R.id.btnChooseOneFromGallery)
			val btnGalleryTwo = findViewById<TextView>(R.id.btnChooseTwoFromGallery)
			val btnLinkOne = findViewById<TextView>(R.id.btnChooseOneFromLink)
			val btnLinkTwo = findViewById<TextView>(R.id.btnChooseTwoFromLink)
//			btnLinkOne.setOnClickListener { this.loadImageOneFromLink() }
//			btnLinkTwo.setOnClickListener { this.loadImageTwoFromLink() }
			btnGalleryOne.setOnClickListener { setImageOneFromGallery() }
			btnGalleryTwo.setOnClickListener { setImageTwoFromGallery() }
		}
	}

	private fun setImageOneFromGallery() {
		// Создаем Intent для открытия галереи
		val intent = Intent()
		intent.type = "image/*"
		intent.action = Intent.ACTION_GET_CONTENT

		// Запускаем Intent и ждем результат
		startActivityForResult(
			Intent.createChooser(intent, "Select Picture"),
			PICK_IMAGE_ONE
		)
	}

	private fun setImageTwoFromGallery() {
		// Создаем Intent для открытия галереи
		val intent = Intent()
		intent.type = "image/*"
		intent.action = Intent.ACTION_GET_CONTENT

		// Запускаем Intent и ждем результат
		startActivityForResult(
			Intent.createChooser(intent, "Select Picture"),
			PICK_IMAGE_TWO
		)
	}

	// Обрабатываем результат выбора изображения
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		(data != null && data.data != null).let {
			// Получаем URI выбранного изображения
			val imageUri = data?.data

			// Преобразуем URI в Bitmap
			val bitmap =
				MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, imageUri)

			if (requestCode == PICK_IMAGE_ONE && resultCode == AppCompatActivity.RESULT_OK) {
				view?.findViewById<TextView>(R.id.tvImageOneNotSelectedMsg)?.isVisible = false
				lastSelectedImageOneBitmap = bitmap
				view?.findViewById<ImageView>(R.id.ivCurrentImageOne)?.setImageBitmap(bitmap)
			} else if (requestCode == PICK_IMAGE_TWO && resultCode == AppCompatActivity.RESULT_OK) {
				view?.findViewById<TextView>(R.id.tvImageTwoNotSelectedMsg)?.isVisible = false
				lastSelectedImageTwoBitmap = bitmap
				view?.findViewById<ImageView>(R.id.ivCurrentImageTwo)?.setImageBitmap(bitmap)
			} else {

			}
		}
	}

	// endregion
}