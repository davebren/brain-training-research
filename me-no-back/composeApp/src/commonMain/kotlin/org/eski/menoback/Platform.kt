package org.eski.shadyback

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform