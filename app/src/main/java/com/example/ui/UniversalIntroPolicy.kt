package com.example.ui

const val UNIVERSAL_STUDIOS_INTRO_PACK_ID = "cj_universal_quiz"

/**
 * The cinematic intro belongs only to the opening of the Universal Studios pack.
 * Question changes inside the same run must never trigger it again.
 */
fun shouldPlayUniversalStudiosIntro(packId: String, currentIndex: Int): Boolean =
    packId == UNIVERSAL_STUDIOS_INTRO_PACK_ID && currentIndex == 0
