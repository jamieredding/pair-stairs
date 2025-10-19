package dev.coldhands.pair.stairs.backend.domain

data class Page<T>(
    val metadata: Metadata,
    val data: List<T>,
) {
    data class Metadata(
        val nextPageNumber: Int?,
    )

    companion object {
        fun <T> empty() = Page<T>(
            metadata = Metadata(
                nextPageNumber = null,
            ),
            data = emptyList()
        )
    }
}
