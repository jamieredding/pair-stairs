package dev.coldhands.pair.stairs.backend

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.support.ParameterDeclarations
import java.util.stream.Stream

open class InlineArgumentsProvider(
    private val block: Args.() -> Stream<Arguments>
) : ArgumentsProvider {
    override fun provideArguments(
        parameters: ParameterDeclarations,
        context: ExtensionContext
    ): Stream<out Arguments> {
        return block(Args(parameters, context))
    }
}

@Suppress("unused")
class Args(val parameters: ParameterDeclarations, val context: ExtensionContext)