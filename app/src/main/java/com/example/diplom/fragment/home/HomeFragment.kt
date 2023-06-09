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
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.diplom.R
import com.example.diplom.entity.compareimages.PerceptualHash
import com.example.diplom.fragment.chooseimagefromlink.ChooseImageFromLinkFragment
import com.example.diplom.fragment.home.callback.HomeCallback
import com.example.diplom.fragment.home.model.CurrentImage
import com.example.diplom.fragment.showcompareprocess.ShowCompareProcessFragment
import com.example.diplom.fragment.showcompareprocess.model.ShowCompareProcessParams
import com.google.android.material.switchmaterial.SwitchMaterial

class HomeFragment : Fragment(), HomeContract, HomeCallback {

	/** Init class for alg process */
	private val perceptualHash = PerceptualHash()

	private var imageOne: CurrentImage = CurrentImage(null, null)
	private var imageTwo: CurrentImage = CurrentImage(null, null)

	companion object {
		const val LOAD_IMAGE_FROM_LINK_FRAGMENT = "LOAD_IMAGE_FROM_LINK_FRAGMENT"
		const val SHOW_COMPARE_PROCESS = "SHOW_COMPARE_PROCESS"
		private const val PICK_IMAGE_ONE = 1
		private const val PICK_IMAGE_TWO = 2
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

	override fun onResume() {
		super.onResume()
		val backgroundColor = ContextCompat.getColor(requireContext(), R.color.transparent)
		requireActivity().window.decorView.systemUiVisibility =
			requireActivity().window.decorView.systemUiVisibility or View.STATUS_BAR_HIDDEN
		requireActivity().window.statusBarColor = backgroundColor
	}

	// endregion

	// region ==================== Override ====================

	// Обрабатываем результат выбора изображения
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		(data != null && data.data != null).let {
			// Получаем URI выбранного изображения
			val imageUri = data?.data

			// Преобразуем URI в Bitmap
			val bitmap =
				MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, imageUri)

			when {
				requestCode == PICK_IMAGE_ONE && resultCode == AppCompatActivity.RESULT_OK -> {
					view?.findViewById<TextView>(R.id.tvImageOneNotSelectedMsg)?.isVisible = false
					imageOne.bitmap = bitmap
					view?.findViewById<ImageView>(R.id.ivCurrentImageOne)?.setImageBitmap(bitmap)
				}
				requestCode == PICK_IMAGE_TWO && resultCode == AppCompatActivity.RESULT_OK -> {
					view?.findViewById<TextView>(R.id.tvImageTwoNotSelectedMsg)?.isVisible = false
					imageTwo.bitmap = bitmap
					view?.findViewById<ImageView>(R.id.ivCurrentImageTwo)?.setImageBitmap(bitmap)
				}
				else -> {}
			}
		}
		checkToReadyCompare()
	}

	// endregion

	// region ==================== HomeCallback ====================

	override fun setImageFromLink(bitmapImage: Bitmap, link: String, imageNumber: Int) {
		when (imageNumber) {
			PICK_IMAGE_ONE -> {
				this.view?.findViewById<TextView>(R.id.tvImageOneNotSelectedMsg)?.isVisible = false
				imageOne.bitmap = bitmapImage
				this.view?.findViewById<ImageView>(R.id.ivCurrentImageOne)
					?.setImageBitmap(imageOne.bitmap)
				imageOne.link = link
			}
			PICK_IMAGE_TWO -> {
				this.view?.findViewById<TextView>(R.id.tvImageTwoNotSelectedMsg)?.isVisible = false
				imageTwo.bitmap = bitmapImage
				this.view?.findViewById<ImageView>(R.id.ivCurrentImageTwo)
					?.setImageBitmap(imageTwo.bitmap)
				imageTwo.link = link
			}
		}
		val fragment = childFragmentManager.findFragmentByTag(LOAD_IMAGE_FROM_LINK_FRAGMENT)
		if (fragment != null) {
			childFragmentManager.beginTransaction().remove(fragment).commit()
		}
		checkToReadyCompare()
	}

	override fun setCompareResult(isDuplicate: Boolean) {
		val ivResult = view?.findViewById<ImageView>(R.id.ivResultOfCompare)
		if (isDuplicate) {
			ivResult?.let {
				ivResult.setImageResource(R.drawable.ic_equal_green)
			}
		} else {
			ivResult?.let {
				ivResult.setImageResource(R.drawable.ic_not_equal_red)
			}
		}
	}

	// endregion

	// region ==================== HomeContract ====================

	override fun onBtnCompareClicked() {
		if (imageOne.bitmap != null && imageTwo.bitmap != null) {
			val imageOneBitmap: Bitmap = imageOne.bitmap!!
			val imageTwoBitmap: Bitmap = imageTwo.bitmap!!

			val switcher = view?.findViewById<SwitchMaterial>(R.id.switchShowCompare)

			when (switcher?.isChecked) {
				true -> {
					val fragment: Fragment = ShowCompareProcessFragment.newInstance(
						ShowCompareProcessParams(
							imageOneBitmap,
							imageTwoBitmap
						)
					)

					childFragmentManager.beginTransaction()
						.add(R.id.rootElement, fragment, SHOW_COMPARE_PROCESS)
						.commitNow()
				}
				false -> {
					setCompareResult(
						perceptualHash.getPerceptualHashCompare(
							imageOneBitmap,
							imageTwoBitmap
						)
					)
				}
				else -> {
					setCompareResult(
						perceptualHash.getPerceptualHashCompare(
							imageOneBitmap,
							imageTwoBitmap
						)
					)
				}
			}
		}
	}

	override fun navigateToChooseImageFromLink(imageNumber: Int) {
		val fragment: Fragment = if (imageNumber == PICK_IMAGE_ONE) {
			ChooseImageFromLinkFragment.newInstance(
				this,
				imageOne.bitmap,
				imageOne.link,
				imageNumber
			)
		} else {
			ChooseImageFromLinkFragment.newInstance(
				this,
				imageTwo.bitmap,
				imageTwo.link,
				imageNumber
			)
		}

		childFragmentManager.beginTransaction()
			.add(R.id.rootElement, fragment, LOAD_IMAGE_FROM_LINK_FRAGMENT)
			.commitNow()
	}

	override fun setImageOneFromGallery() {
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

	override fun setImageTwoFromGallery() {
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

	override fun isAllImageLoaded(): Boolean {
		if (imageOne.bitmap != null && imageTwo.bitmap != null) {
			return true
		}
		return false
	}

	override fun checkToReadyCompare() {
		if (isAllImageLoaded()) {
			this.view?.findViewById<TextView>(R.id.btnCompare)?.isEnabled = true
			this.view?.findViewById<SwitchMaterial>(R.id.switchShowCompare)?.isVisible = true
		}
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
			val btnCompare = findViewById<TextView>(R.id.btnCompare)
			btnLinkOne.setOnClickListener { navigateToChooseImageFromLink(PICK_IMAGE_ONE) }
			btnLinkTwo.setOnClickListener { navigateToChooseImageFromLink(PICK_IMAGE_TWO) }
			btnGalleryOne.setOnClickListener { setImageOneFromGallery() }
			btnGalleryTwo.setOnClickListener { setImageTwoFromGallery() }
			btnCompare.setOnClickListener { onBtnCompareClicked() }
		}
	}

	// endregion
}