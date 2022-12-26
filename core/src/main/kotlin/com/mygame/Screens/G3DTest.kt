package com.mygame.Screens

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.bullet.Bullet
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody
import com.badlogic.gdx.utils.JsonReader
import com.mygame.MotionState

class G3DTest(game:Game):BaseScreen(game) {
    lateinit var alienSlimeModel:Model
    lateinit var alienSlime:ModelInstance
    init {
        initializeBullet()
        createFloor(30f, 1f, 30f)
        createAxes()
        initialize3DGModel()

    }

    private fun initialize3DGModel() {
        //initializing 3d model with g3d technique
        //val modelBuilder = ModelBuilder() //seems like it's not necessary
        alienSlimeModel = G3dModelLoader(JsonReader()).loadModel(Gdx.files.internal("3DModels/Alien Slime.g3dj"))
        alienSlime = ModelInstance(alienSlimeModel)
        alienSlime.transform.trn(0f, 10f, 0f)
        renderInstances.add(alienSlime)
        //giving it a rigid body
        var shape= Bullet.obtainStaticNodeShape(alienSlime.nodes)
        var localInertia=Vector3()

        shape.calculateLocalInertia(1f, localInertia)

        var info= btRigidBody.btRigidBodyConstructionInfo(1f,null, shape, localInertia)
        var body= btRigidBody(info)

        var motionState=MotionState(alienSlime.transform)
        body.motionState=motionState

        dynamicsWorld!!.addRigidBody(body)

    }

    override fun render(delta: Float) {
        super.render(delta)
        debugDrawer.begin(camera)
        dynamicsWorld!!.debugDrawWorld()
        debugDrawer.end()
    }
}