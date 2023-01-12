@file:JvmName("Lwjgl3Launcher")

package com.mygame.lwjgl3

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.mygame.MyGame
import com.mygame.otherStuff.AudioRecorderTest
import com.mygame.otherStuff.FramebufferTest

/** Launches the desktop (LWJGL3) application. */
fun main() {
    Lwjgl3Application(MyGame(), Lwjgl3ApplicationConfiguration().apply {
        setTitle("first-3d-game")
        setWindowedMode(640, 480)
        setWindowIcon(*(arrayOf(128, 64, 32, 16).map { "libgdx$it.png" }.toTypedArray()))
    })
}
