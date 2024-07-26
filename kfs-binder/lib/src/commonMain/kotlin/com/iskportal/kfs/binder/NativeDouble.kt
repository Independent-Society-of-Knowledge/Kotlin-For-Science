package com.iskportal.kfs.binder

expect class NativeDouble(init: Double) {
    var value: Double
    val pointer: Pointer
}