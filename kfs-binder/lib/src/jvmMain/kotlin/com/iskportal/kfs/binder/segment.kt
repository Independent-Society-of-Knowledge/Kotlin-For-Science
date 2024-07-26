package com.iskportal.kfs.binder

import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.foreign.SegmentAllocator
import java.lang.foreign.ValueLayout
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaGetter


val MemorySegment.native: MemorySegment
    get() {
        val seg = Arena.ofAuto().allocate(byteSize())
        seg.copyFrom(this)
        return seg
    }

//region segment
val DoubleArray.segment: MemorySegment get() = MemorySegment.ofArray(this)
val IntArray.segment: MemorySegment get() = MemorySegment.ofArray(this)
val ShortArray.segment: MemorySegment get() = MemorySegment.ofArray(this)
val ByteArray.segment: MemorySegment get() = MemorySegment.ofArray(this)
val CharArray.segment: MemorySegment get() = MemorySegment.ofArray(this)
val FloatArray.segment: MemorySegment get() = MemorySegment.ofArray(this)
val LongArray.segment: MemorySegment get() = MemorySegment.ofArray(this)


val MemorySegment.asDoubleArray: DoubleArray
    get() = this.toArray(ValueLayout.JAVA_DOUBLE)
val MemorySegment.asIntArray: IntArray
    get() = this.toArray(ValueLayout.JAVA_INT)
val MemorySegment.asShortArray: ShortArray
    get() = this.toArray(ValueLayout.JAVA_SHORT)
val MemorySegment.asByteArray: ByteArray
    get() = this.toArray(ValueLayout.JAVA_BYTE)
val MemorySegment.asCharArray: CharArray
    get() = this.toArray(ValueLayout.JAVA_CHAR)
val MemorySegment.asFloatArray: FloatArray
    get() = this.toArray(ValueLayout.JAVA_FLOAT)
val MemorySegment.asLongArray: LongArray
    get() = this.toArray(ValueLayout.JAVA_LONG)


inline val <reified T : CStruct> T.segment: MemorySegment
    get() {

        val cType = T::class.companionObject!!.java.getDeclaredMethod("getCStructClass")
            .invoke(T::class.companionObjectInstance) as Class<*>


        val seg = cType.getMethod("allocate", SegmentAllocator::class.java)
            .invoke(null, Arena.ofAuto()) as MemorySegment

        T::class.memberProperties
            .filter { it.name != "Companion" }
            .map { it.name to it.javaGetter!! }
            .forEach {
                cType.getMethod(it.first, MemorySegment::class.java, it.second.returnType)
                    .invoke(null, seg, it.second.invoke(this))
            }
        return seg
    }

inline fun <reified T : CStruct> MemorySegment.read(): T {
    val clazz = T::class
    val cType = clazz.companionObject!!.java.getDeclaredMethod("getCStructClass")
        .invoke(clazz.companionObjectInstance) as Class<*>


    val constructor = clazz.constructors.first()
    val cParams = constructor.parameters

    val args = clazz.memberProperties
        .filter { it.name != "Companion" }
        .associate {
            cParams.first { p -> p.name == it.name } to cType.getMethod(it.name, MemorySegment::class.java)
                .invoke(null, this).also { a ->
                    println("resolved: ${it.name}: ${a}")
                }
        }


    return constructor.callBy(args)
}


// as array
inline val <reified T : CStruct> Array<T>.segment: MemorySegment
    get() {

        val cType = T::class.companionObject!!.java.getDeclaredMethod("getCStructClass")
            .invoke(T::class.companionObjectInstance) as Class<*>


        val segArray = cType.getMethod("allocateArray", Long::class.java, SegmentAllocator::class.java)
            .invoke(null, this.size.toLong(), Arena.ofAuto()) as MemorySegment

        val slicer = cType.getMethod("asSlice", MemorySegment::class.java, Long::class.java)
        val segSlices = this.indices.map { (slicer.invoke(null, segArray, it.toLong()) as MemorySegment) to it }

        T::class.memberProperties
            .filter { it.name != "Companion" }
            .map { it.name to it.javaGetter!! }
            .forEach {
                for (segAndIndex in segSlices) {
                    cType.getMethod(it.first, MemorySegment::class.java, it.second.returnType) //setter
                        .invoke(null, segAndIndex.first, it.second.invoke(this[segAndIndex.second]))
                }
            }
        return segArray
    }


inline fun <reified T : CStruct> MemorySegment.readArray(): Array<T> {
    val clazz = T::class
    val cType = clazz.companionObject!!.java.getDeclaredMethod("getCStructClass")
        .invoke(clazz.companionObjectInstance) as Class<*>

    val slicer = cType.getMethod("asSlice", MemorySegment::class.java, Long::class.java)

    val size = this.byteSize() / cType.getMethod("sizeof").invoke(null) as Long

    val constructor = clazz.constructors.first()
    val cParams = constructor.parameters


    val argsBuilder = clazz.memberProperties
        .filter { it.name != "Companion" }
        .associate {
            cParams.first { p -> p.name == it.name } to cType.getMethod(it.name, MemorySegment::class.java)
        }

    return (0 until size).map { slicer.invoke(null, this, it) as MemorySegment }
        .map { seg -> argsBuilder.entries.associate { it.key to it.value.invoke(null, seg) } }
        .map { constructor.callBy(it) }
        .toTypedArray()

}

//endregion


//region ptr
inline val MemorySegment.ptr: Pointer
    get() = Pointer(this)

inline val <reified T : CStruct> T.ptr: Pointer
    get() = segment.ptr


val DoubleArray.ptr: Pointer
    get() = segment.ptr
val IntArray.ptr: Pointer
    get() = segment.ptr
val ShortArray.ptr: Pointer
    get() = segment.ptr
val ByteArray.ptr: Pointer
    get() = segment.ptr
val CharArray.ptr: Pointer
    get() = segment.ptr
val FloatArray.ptr: Pointer
    get() = segment.ptr
val LongArray.ptr: Pointer
    get() = segment.ptr
//endregion