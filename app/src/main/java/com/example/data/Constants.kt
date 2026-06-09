package com.example.data

object CarpetConstants {
    val FELT_TYPES = listOf(
        "FELT GREEN",
        "FELT RED",
        "FELT REDBLACK",
        "FELT DARKGREY",
        "FELT LIGHTGREY",
        "FELT DARKBROWN",
        "FELT CAMEL",
        "FELT R.BLUE",
        "FELT BLACK",
        "FELT MEHROON",
        "FELT RANI",
        "S.FELT BLACK",
        "S.FELT D.GREY"
    )

    val VELLORE_TYPES = listOf(
        "VELLORE GREEN",
        "VELLORE RED",
        "VELLORE REDBLACK",
        "VELLORE D.GREY",
        "VELLORE L.GREY",
        "VELLORE D.BROWN",
        "VELLORE CAMEL",
        "VELLORE GOLDEN",
        "VELLORE R.BLUE",
        "VELLORE BLACK",
        "VELLORE MEHROON",
        "VELLORE RANI"
    )

    val OTHER_TYPES = listOf(
        "RUNNER",
        "RIBBED",
        "GALICHA",
        "ZALI MAT",
        "KUSHAN MAT",
        "GRASS MAT"
    )

    val DURRI_TYPES = listOf(
        "DURRI"
    )

    val ALL_TYPES = FELT_TYPES + VELLORE_TYPES + OTHER_TYPES + DURRI_TYPES

    // For Felt, Vellore, Others, standard widths in meters:
    val CARPET_SIZES = listOf(
        "0.61", "0.91", "1.22", "1.52", "1.83", "2", "3.05", "3.66", "4"
    )

    // For Durri, standard dimensions in feet/inches:
    val DURRI_SIZES = listOf(
        "10*10", "10*12", "12*12", "12*15", "15*15", "12*18", "15*18"
    )
}
