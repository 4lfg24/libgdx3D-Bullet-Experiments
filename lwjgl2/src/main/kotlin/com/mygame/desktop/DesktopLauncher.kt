@file:JvmName("DesktopLauncher")

package com.mygame.desktop

import com.badlogic.gdx.Files
import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import com.mygame.MyGame

/** Launches the desktop (LWJGL) application. */
fun main() {
    LwjglApplication(MyGame(), LwjglApplicationConfiguration().apply {
        title = "first-3d-game"
        width = 640
        height = 480
        intArrayOf(128, 64, 32, 16).forEach{
            addIcon("libgdx$it.png", Files.FileType.Internal)
        }
    })
}
