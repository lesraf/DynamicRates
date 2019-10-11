package com.rl.dynamicrates.sources.mock

import android.content.Context

class MockDataFetcher(private val context: Context) : DataFetcher {

    override fun fetchData(): String {
        val resources = context.resources
        val inputStream = resources.openRawResource(
            resources.getIdentifier(
                mockFileName,
                mockFilePath,
                context.packageName
            )
        )

        val size = inputStream.available()
        val buffer = ByteArray(size)
        inputStream.read(buffer)
        inputStream.close()

        return String(buffer)
    }

    companion object {
        private const val mockFileName = "mock_data"
        private const val mockFilePath = "raw"
    }
}