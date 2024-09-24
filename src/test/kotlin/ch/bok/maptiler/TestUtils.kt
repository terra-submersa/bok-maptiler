package ch.bok.maptiler

import java.io.File
import java.io.FileNotFoundException

class TestUtils {
    companion object{
        fun getTestFile(path:String): File =
                TestUtils::class.java.classLoader.getResource(path)?.let{
                    return File(it.toURI())
                }?: throw FileNotFoundException(path)

        fun mkdir(path: String):File {
            val directory = File(path)
            if (!directory.exists()) {
                directory.mkdirs()
            }
            return directory
        }

        fun rm(path: String) {
            val file = File(path)
            if (file.exists()) {
                file.delete()
            }
        }
    }
}
