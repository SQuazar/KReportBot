package net.nullpointer.kreport.entity

data class Pageable<T>(
    val items: List<T>,
    val page: Int,
    val pageCount: Int,
    val hasNextPage: Boolean,
)
