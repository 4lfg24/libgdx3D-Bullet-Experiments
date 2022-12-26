package com.mygame

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Game
import com.badlogic.gdx.physics.bullet.Bullet
import com.mygame.Screens.*

/** [com.badlogic.gdx.ApplicationListener] implementation shared by all platforms. */
class MyGame : Game(){
    override fun create() {
        Bullet.init()
        setScreen(ExplosionTest(this))
    }

}
