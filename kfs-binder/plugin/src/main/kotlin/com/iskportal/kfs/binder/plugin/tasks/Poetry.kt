package com.iskportal.kfs.binder.plugin.tasks

import com.github.javaparser.JavaParser
import com.iskportal.kfs.binder.Pointer
import com.iskportal.kfs.binder.plugin.dto.ProjectConfig
import com.iskportal.kfs.binder.ptr
import com.squareup.kotlinpoet.*
import org.eclipse.cdt.core.dom.ast.*
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage
import org.eclipse.cdt.core.parser.*
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDeclarator
import java.io.File
import java.lang.foreign.MemorySegment
import java.lang.foreign.SegmentAllocator
import java.lang.reflect.Method
import java.lang.reflect.Modifier

class Poetry(val clazz: Class<*>, val projectConfig: ProjectConfig) {

    val acceptableFunctions
        get() = clazz.methods.filter { Modifier.isStatic(it.modifiers) }.filterNot { it.isSynthetic }
            .filterNot { it.name.contains("$") }

    val jvmMain get() = projectConfig.locations.kfsSrcDir.resolve("jvmMain")
    val commonMain get() = projectConfig.locations.kfsSrcDir.resolve("commonMain")
    val jsMain get() = projectConfig.locations.kfsSrcDir.resolve("jsMain")


    val packageName = clazz.packageName.substringBeforeLast(".")
    val className = clazz.simpleName
    val klass = ClassName(packageName, className)

    fun makePoet() {
//        makeBindings()
        makeCommon()
        makeJvm()
    }

    fun makeBindings() {
        val unit = cppParseUnitFrom(getSignatures(projectConfig.locations.kfsJavaSourceDir, clazz).joinToString(";"))

        methodDeclarationVisitor(unit) {
            val methodName = it.name.toString()
            if (methodName.isNotBlank()) {
                println(
                    it.parameters.map {
                        val sp = it.declSpecifier as IASTSimpleDeclSpecifier
                        sp.rawSignature
                    }
                )
            }
        }
    }

    fun makeCommon() {
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
    }

    fun makeJvm() {
        val jvm = FileSpec.builder(klass)
            .addImport("com.iskportal.kfs.binder", "ptr")
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
                                                    it.parameters?.joinToString(",") {
                                                        if (SegmentAllocator::class.java.isAssignableFrom(it.type))
                                                            "java.lang.foreign.Arena.ofAuto()"
                                                        else if(MemorySegment::class.java.isAssignableFrom(it.type))
                                                            "${it.name}.memorySegment"
                                                        else
                                                            it.name
                                                    } ?: ""
                                                })${if(MemorySegment::class.java.isAssignableFrom(it.returnType)) ".ptr" else ""}"
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
            .addParameters(
                parameters
                    .filterNot { SegmentAllocator::class.java.isAssignableFrom(it.type) }
                    .map {
                        if(MemorySegment::class.java.isAssignableFrom(it.type))
                            Pointer::class.java to it.name
                        else it.type to it.name
                    }
                    .map { ParameterSpec.builder(it.second, it.first).build() })
            .returns(
                if(MemorySegment::class.java.isAssignableFrom(returnType))
                    Pointer::class.java
                else returnType
            )
}

private fun getSignatures(
    kfsJavaSourceDir: File,
    clazz: Class<*>
): List<String> {
    val classes = sequence<Class<*>> {
        var c = clazz
        while (true) {
            yield(c)
            if (c.superclass != Object::class.java)
                c = c.superclass
            else
                break
        }
    }

    fun locationByClassName(className: String): File = kfsJavaSourceDir.resolve("${className.replace(".", "/")}.java")

    val parser = JavaParser()
    val signatures = classes
        .map { it.name }
        .map { locationByClassName(it) }
        .flatMap {
            val p = parser.parse(it)
            p.commentsCollection.get().javadocComments
        }
        .map { it.content }
        .map { it.substringAfter("lang=c :").substringBeforeLast(")") + ")" }
        .map { it.trimIndent().substring(2).trim() }
        .distinct()
        .toList()

    return signatures
}

fun cppParseUnitFrom(content: String): IASTTranslationUnit {
    val fileContent = FileContent.create(
        "",
        content.toCharArray()
    )
    println(content)
    val definedSymbols = mapOf<String, String>()
    val includePaths = arrayOfNulls<String>(0)
    val info: IScannerInfo = ScannerInfo(definedSymbols, includePaths)
    val log: IParserLogService = DefaultLogService()
    val emptyIncludes = IncludeFileContentProvider.getEmptyFilesProvider()

    val unit: IASTTranslationUnit =
        GPPLanguage.getDefault().getASTTranslationUnit(fileContent, info, emptyIncludes, null, 0, log)
    return unit

}

fun methodDeclarationVisitor(unit: IASTTranslationUnit, action: (CPPASTFunctionDeclarator) -> Unit) {
    object : ASTVisitor() {
        init {
            shouldVisitDeclarators = true
        }

        override fun visit(node: IASTDeclarator): Int {
            println(node::class.java)
            if (node is CPPASTFunctionDeclarator) {
                action(node)
            }
            return 3
        }
    }.let {
        unit.accept(it)
    }
}