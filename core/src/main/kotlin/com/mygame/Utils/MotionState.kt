package com.mygame

import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.physics.bullet.linearmath.btMotionState
import java.awt.geom.AffineTransform

class MotionState(transform: Matrix4): btMotionState() {

    var transform:Matrix4

    init {
        this.transform=transform
    }

    override fun getWorldTransform(worldTrans: Matrix4?) {
        worldTrans!!.set(transform)

    }

    override fun setWorldTransform(worldTrans: Matrix4?) {
        transform.set(worldTrans)

    }
}