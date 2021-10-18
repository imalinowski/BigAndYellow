package com.malinowski.bigandyellow

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.malinowski.bigandyellow.customview.FlexBoxLayout
import com.malinowski.bigandyellow.data.Reaction
import io.reactivex.rxjava3.subjects.PublishSubject


class SmileBottomSheet : BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.smiles_bottom_sheet_content, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val store = (view as ViewGroup).findViewById<FlexBoxLayout>(R.id.smile_store)
        val smiles = resources.getStringArray(R.array.smiles);
        store.paddingRows = 5
        store.paddingColumns = 5
        val outValue = TypedValue()
        requireContext().theme.resolveAttribute(R.attr.selectableItemBackground, outValue, true)
        for (i in smiles.indices)
            Button(context).apply {
                setBackgroundColor(Color.TRANSPARENT)
                text = smiles[i]
                textSize = 40f
                store?.addView(this)
                setOnClickListener {
                    flow?.onNext(Reaction(smile = i, num = 1))
                    dismiss()
                }
                setBackgroundResource(outValue.resourceId)
            }
    }

    private var flow: PublishSubject<Reaction>? = null
    fun show(flow: PublishSubject<Reaction>, manager: FragmentManager) {
        this.flow = flow
        show(manager, TAG)
    }

    companion object {
        const val TAG = "SmileBottomSheet"
    }
}
