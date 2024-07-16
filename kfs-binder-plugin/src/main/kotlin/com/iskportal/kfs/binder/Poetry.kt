package com.iskportal.kfs.binder

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.jvm.jvmStatic
import org.gradle.api.Project
import java.io.File
import java.lang.reflect.Method
import java.lang.reflect.Modifier

class Poetry(val project: Project, val clazz: Class<*>, val srcDir: File) {

    val acceptableFunctions
        get() = clazz.methods.filter { Modifier.isStatic(it.modifiers) }.filterNot { it.isSynthetic }
            .filterNot { it.name.contains("$") }

    val jvmMain get() = srcDir.resolve("jvmMain")
    val commonMain get() = srcDir.resolve("commonMain")
    val jsMain get() = srcDir.resolve("jsMain")


    fun makePoet() {
        val packageName = clazz.packageName.substringBeforeLast(".")
        val className = clazz.simpleName
        val klass = ClassName(packageName, className)

        val common = FileSpec.builder(klass)
            .addType(
                TypeSpec.classBuilder(klass)
                    .addModifiers(KModifier.EXPECT)
                    .addType(
                        TypeSpec.companionObjectBuilder()
                            .apply { acceptableFunctions.forEach { addFunction(it.asFunctionSpec.build()) } }
                            .build()
                    )
                    .build()
            ).build()

        common.writeTo(commonMain)


        val jvm = FileSpec.builder(klass)
            .addType(
                TypeSpec
                    .classBuilder(klass)
                    .addModifiers(KModifier.ACTUAL)
                    .addType(
                        TypeSpec
                            .companionObjectBuilder()
                            .addModifiers(KModifier.ACTUAL)
                            .apply {
                                acceptableFunctions.forEach {
                                    addFunction(
                                        it.asFunctionSpec
                                            .addModifiers(KModifier.ACTUAL)
                                            .addStatement(
                                                "return ${clazz.canonicalName}.${it.name}(${
                                                    it.parameters?.joinToString(",") { it.name } ?: ""
                                                })"
                                            )
                                            .build()
                                    )
                                }
                            }
                            .build()
                    )
                    .build()
            )
            .build()

        jvm.writeTo(jvmMain)
    }

    private val Method.asFunctionSpec
        get() = FunSpec.builder(this.name)
            .addParameters(parameters.map { ParameterSpec.builder(it.name, it.type).build() })
            .returns(returnType)
}