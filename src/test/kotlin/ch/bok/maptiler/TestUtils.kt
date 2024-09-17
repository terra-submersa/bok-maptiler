package ch.bok.maptiler

import java.io.File
import java.io.FileNotFoundException

class TestUtils {
    companion object{
        fun getTestFile(path:String): File =
                TestUtils::class.java.classLoader.getResource(path)?.let{
                    return File(it.toURI())
                }?: throw FileNotFoundException(path)
    }
}
