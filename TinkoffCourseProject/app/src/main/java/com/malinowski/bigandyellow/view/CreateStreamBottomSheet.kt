package com.malinowski.bigandyellow.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.malinowski.bigandyellow.databinding.CreateStreamBinding


class CreateStreamBottomSheet() : BottomSheetDialogFragment() {

    private val binding by lazy { CreateStreamBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.createStream.setOnClickListener {
            parentFragmentManager.setFragmentResult(
                RESULT,
                bundleOf(
                    NAME to binding.name.text.toString(),
                    DESCRIPTION to binding.description.text.toString()
                )
            )
            dismiss()
        }
    }

    companion object {
        const val TAG = "CreateStreamBottomSheet"
        const val RESULT = "result"
        const val NAME = "name"
        const val DESCRIPTION = "description"
    }
}
