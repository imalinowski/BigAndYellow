package com.malinowski.bigandyellow.view.mvi

import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.malinowski.bigandyellow.view.mvi.states.State

abstract class FragmentMVI<T : State> : Fragment {

    constructor() : super()

    constructor(@LayoutRes contentLayoutId: Int) : super(contentLayoutId)

    abstract fun render(state: T)
}