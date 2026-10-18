package com.devqissy.pindrop.network

fun extractMediaUrls(html: String): List<String> {
    val extractedUrls = mutableListOf<String>()

    val ogVideoRegex = Regex("<meta[^>]*(?:property|name)=\"og:video\"[^>]*content=\"([^\"]+)\"|<meta[^>]*content=\"([^\"]+)\"[^>]*(?:property|name)=\"og:video\"")
    ogVideoRegex.find(html)?.let { match ->
        val url = if (match.groupValues[1].length > 0) match.groupValues[1] else match.groupValues[2]
        extractedUrls.add(url.replace("&amp;", "&").replace("\\/", "/"))
    }

    if (extractedUrls.isEmpty()) {
        val mp4Regex = Regex("https://v1\\.pinimg\\.com/[^\"'\\s>]+\\.mp4")
        mp4Regex.findAll(html).map { it.value.replace("\\/", "/") }.distinct().forEach {
            extractedUrls.add(it)
        }
    }

    if (extractedUrls.isEmpty()) {
        val carouselImagesRegex = Regex("\"carousel_data\"\\s*:\\s*\\{\\s*\"images\"\\s*:\\s*\\[(.*?)\\]")
        carouselImagesRegex.find(html)?.let { match ->
            val carouselBlock = match.groupValues[1]
            val urlPattern = Regex("\"url\"\\s*:\\s*\"([^\"]+)\"")
            val urls = urlPattern.findAll(carouselBlock)
                .map { it.groupValues[1].replace("\\/", "/") }
                .filter { it.contains("originals") || it.contains("736x") }
                .distinct()
                .toList()
            if (urls.isNotEmpty()) {
                extractedUrls.addAll(urls)
            }
        }
    }

    if (extractedUrls.isEmpty()) {
        val carouselVideosRegex = Regex("\"carousel_videos\"\\s*:\\s*\\[(.*?)\\]")
        carouselVideosRegex.find(html)?.let { match ->
            val carouselBlock = match.groupValues[1]
            val urlPattern = Regex("\"url\"\\s*:\\s*\"([^\"]+)\"")
            val urls = urlPattern.findAll(carouselBlock)
                .map { it.groupValues[1].replace("\\/", "/") }
                .filter { it.contains(".mp4") }
                .distinct()
                .toList()
            if (urls.isNotEmpty()) {
                extractedUrls.addAll(urls)
            }
        }
    }

    if (extractedUrls.isEmpty()) {
        val ogImageRegex = Regex("<meta[^>]*(?:property|name)=\"og:image\"[^>]*content=\"([^\"]+)\"|<meta[^>]*content=\"([^\"]+)\"[^>]*(?:property|name)=\"og:image\"")
        ogImageRegex.find(html)?.let { match ->
            val url = if (match.groupValues[1].length > 0) match.groupValues[1] else match.groupValues[2]
            extractedUrls.add(url.replace("&amp;", "&").replace("\\/", "/"))
        }
    }

    if (extractedUrls.isEmpty()) {
        val originalsRegex = Regex("https://i\\.pinimg\\.com/originals/[^\"'\\s>]+")
        val foundImages = originalsRegex.findAll(html)
            .map { it.value.replace("\\/", "/").trim('\\', '"', '\'') }
            .distinct()
            .toList()
        if (foundImages.isNotEmpty()) {
            extractedUrls.add(foundImages.first())
        }
    }

    if (extractedUrls.isEmpty()) {
        val backupImgRegex = Regex("https://i\\.pinimg\\.com/736x/[^\"'\\s>]+")
        backupImgRegex.find(html)?.let {
            extractedUrls.add(it.value.replace("\\/", "/").trim('\\', '"', '\''))
        }
    }

    return extractedUrls.distinct().filter { it.startsWith("http") }
}