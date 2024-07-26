package com.iskportal.kfs.binder

import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

actual class NativeDouble(init: Double, val arena: Arena = Arena.ofAuto()) {
    actual constructor(init: Double) : this(init, Arena.ofAuto()) {}

    val segment: MemorySegment = arena.allocate(ValueLayout.JAVA_DOUBLE)

    actual var value: Double
        get() = segment.get(ValueLayout.JAVA_DOUBLE, 0)
        set(value) = segment.set(ValueLayout.JAVA_DOUBLE, 0, value)

    actual val pointer: Pointer
        get() = segment.ptr

}