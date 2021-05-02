package br.com.gmfonseca.processors

import br.com.gmfonseca.annotations.CommandHandler
import org.yanex.takenoko.KoType
import org.yanex.takenoko.PRIVATE
import org.yanex.takenoko.PrettyPrinter
import org.yanex.takenoko.PrettyPrinterConfiguration
import org.yanex.takenoko.kotlinFile
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.annotation.processing.SupportedOptions
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("br.com.gmfonseca.annotations.CommandHandler")
@SupportedOptions(CommandHandlerAnnotationProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME)
class CommandHandlerAnnotationProcessor : AbstractProcessor() {

    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment): Boolean {
        val annotatedElements = roundEnv.getElementsAnnotatedWith(CommandHandler::class.java)

        if (annotatedElements.isEmpty()) return false

        val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME] ?: run {
            processingEnv.messager.printMessage(
                Diagnostic.Kind.ERROR,
                "Can't find the target directory for generated Kotlin files."
            )
            return false
        }

        val generatedKtFile = kotlinFile("br.com.gmfonseca.generated") {
            objectDeclaration("Statics") {
                val initCommands = "initCommands"

                property("COMMANDS") {
                    initializer("$initCommands()")
                }

                function(name = initCommands, modifiers = PRIVATE) {
                    returnType(KoType.Companion.parseType("List<$COMMAND_TYPE_NAME>"))

                    val names = annotatedElements.mapNotNull {
                        it.toTypeElementOrNull()?.run { "\t\t\"$qualifiedName\"" }
                    }.joinToString(separator = ",\n")

                    body(expressionBody = true) {
                        appendln("run {")
                        appendln("\tval names = listOf(")
                        append(names)
                        appendln("\n\t)")
                        appendln("\n\t$CLASS_MAPPER_MAPPING_METHOD_NAME(names)")
                        appendln("}")
                    }
                }
            }
        }

        File(kaptKotlinGeneratedDir, "Statics.kt").apply {
            parentFile.mkdirs()
            writeText(generatedKtFile.accept(PrettyPrinter(PrettyPrinterConfiguration())))
        }

        return true
    }

    private fun Element.toTypeElementOrNull(): TypeElement? {
        if (this !is TypeElement) {
            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Invalid element type, class expected", this)
            return null
        }

        return this
    }

    internal companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
        const val COMMAND_TYPE_NAME = "br.com.gmfonseca.shared.command.Command"
        const val CLASS_MAPPER_MAPPING_METHOD_NAME = "br.com.gmfonseca.shared.util.ClassMapper.mapClasses"
    }
}