package com.malinowski.bigandyellow

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.malinowski.bigandyellow.customview.FlexBoxLayout
import io.reactivex.rxjava3.subjects.PublishSubject

class SmileBottomSheet: BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.smiles_bottom_sheet_content, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val store = (view as ViewGroup).findViewById<FlexBoxLayout>(R.id.smile_store)
        val smiles = resources.getStringArray(R.array.smiles);
        store.paddingRows = 10
        store.paddingColumns = 10
        for (i in smiles.indices)
            Button(context).apply {
                setBackgroundColor(Color.TRANSPARENT)
                text = smiles[i]
                textSize = 30f
                store?.addView(this)
                setOnClickListener {
                    flow?.onNext(i)
                    dismiss()
                }
            }
    }

    private var flow: PublishSubject<Int>? = null
    fun show(flow: PublishSubject<Int>, manager: FragmentManager) {
        this.flow = flow
        show(manager, TAG)
    }

    companion object {
        const val TAG = "SmileBottomSheet"
    }
}
