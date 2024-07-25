package com.iskportal.kfs.binder

import java.lang.foreign.MemorySegment

actual class Pointer(
    val memorySegment: MemorySegment
)