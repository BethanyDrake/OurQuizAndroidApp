package com.sycorax.ourquiz

import android.text.Editable
import android.widget.EditText
import io.mockk.every
import io.mockk.mockk

fun createMockEditText(text: String): EditText {
    val textView: EditText = mockk()
    val textViewText: Editable = mockk()
    every { textViewText.toString() } returns text
    every { textView.text } returns textViewText
    return textView

}
