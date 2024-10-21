package ch.bok.maptiler

import ch.bok.maptiler.models.*

interface GeoImageFixtures {
    fun anOrthoPhotoTifFile() = TestUtils.getTestFile("odm_orthophoto.tif")
    fun anOrthoPhotoImage(crsCode: String? = null, file: String = "odm_orthophoto.tif") =
        GeoImage.fromFile(TestUtils.getTestFile(file), crsCode = crsCode)

    fun aNWCornerWGS84() = Coords.build(23.1332108258033, 37.428434250438066, "EPSG:4326")
    fun aSECornerWGS84() = Coords.build(23.133332212431817, 37.42833060029414, "EPSG:4326")

    fun aBoundingBoxWGS84() = BoundingBox(nw = aNWCornerWGS84(), se = aSECornerWGS84())
    fun aNWCornerUTM34M() = Coords.build(688745.9704086968, 4144537.7520394064, "EPSG:32634")
    fun aSECornerUTM34M() = Coords.build(688756.9723625132, 4144526.4940400003, "EPSG:32634")
    fun aBoundingBoxUTM34M() = BoundingBox(nw = aNWCornerUTM34M(), se = aSECornerUTM34M())

    fun anMBTilesMetadata() = MBTilesMetadata(
        name = "paf le chien",
        bounds = BoundingBox(nw = aNWCornerWGS84(), se = aSECornerWGS84()),
        minZoom = 16,
        maxZoom = 24,
        attribution = "© Julien Beck - University of Geneva",
        attributes = mapOf(
            "pouet" to "42",
            "flapflap" to "la girafe"
        )
    )

    fun aKouverta500TileList() =
        """
            17|74001|80216|40070
            18|148002|160432|42469
            18|148002|160433|57242
            18|148003|160432|15660
            18|148003|160433|28995
            19|296004|320865|32543
            19|296004|320866|54525
            19|296004|320867|15925
            19|296005|320865|108971
            19|296005|320866|81698
            19|296005|320867|26428
            19|296006|320865|51156
            19|296006|320866|90733
            19|296006|320867|1868
        """.trimIndent()
            .let { it.split("\n") }
            .map { it.split(("|")) }
            .map { (z, x, y) ->
                TileCoords(x.toLong(), y.toLong(), z.toInt())
            }
}