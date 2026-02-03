package dev.coldhands.pair.stairs.backend

import com.github.underscore.Json
import org.http4k.core.ContentType.Companion.APPLICATION_JSON
import org.http4k.testing.ApprovalContent.Companion.HttpTextBody
import org.http4k.testing.BaseApprovalTest
import org.http4k.testing.FileSystemApprovalSource
import org.http4k.testing.NamedResourceApprover
import org.http4k.testing.checkingContentType
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.platform.commons.support.AnnotationSupport
import org.opentest4j.AssertionFailedError
import java.io.File

class ParameterizedJsonApprovalTest : BaseApprovalTest {
    override fun approverFor(context: ExtensionContext) = checkingContentType(
        NamedResourceApprover(
            context.resourceName(),
            HttpTextBody(::format),
            FileSystemApprovalSource(File("src/test/resources"))
        ),
        APPLICATION_JSON
    )

    private fun ExtensionContext.resourceName(): String {
        val testClass = requiredTestClass
        val testMethod = requiredTestMethod
        val nestedClassName = testClass.name.split('.').last()
        val nestedClassInSubDirectories = nestedClassName.replace("$", "/")
        val simpleApprovalFilePath =
            testClass.`package`.name.replace('.', '/') + '/' + nestedClassInSubDirectories + "/" + testMethod.name

        return if (AnnotationSupport.isAnnotated(testMethod, ParameterizedTest::class.java)) {
            // adding the display name here is extremely hacky based on the issue raised
            // here: https://github.com/junit-team/junit-framework/issues/1139
            // Using org.junit.jupiter.params.ParameterInfo.get(...) is returning null for now
            // so leaving with a basic working version
            val annotation = AnnotationSupport.findAnnotation(testMethod, ParameterizedTest::class.java).get()
            if (annotation.name == "{default_display_name}") {
                throw IllegalArgumentException("$nestedClassName#${testMethod.name}'s @ParameterizedTest annotation must specify a name so that you have a reliable file name generated in your approval file.")
            }

            "$simpleApprovalFilePath/$displayName"
        } else {
            simpleApprovalFilePath
        }
    }

    fun format(input: String): String = try {
        Json.formatJson(input, Json.JsonStringBuilder.Step.TWO_SPACES)
    } catch (e: Json.ParseException) {
        throw AssertionFailedError("Invalid JSON generated", "<valid JSON>", input, e)
    }
}