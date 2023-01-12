package com.mygame.Screens

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher
import com.badlogic.gdx.physics.bullet.collision.btGImpactCollisionAlgorithm
import com.badlogic.gdx.physics.bullet.collision.btGImpactMeshShape
import com.badlogic.gdx.physics.bullet.collision.btTriangleIndexVertexArray
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
        //alienSlime.transform.trn(0f, 1f, 0f)

        //to make a shape that resembles the model we gotta first
        //do this:
        var modelVertexArray= btTriangleIndexVertexArray(alienSlime.model.meshParts)
        var slimeShape= btGImpactMeshShape(modelVertexArray)

        //I have to fix this
        slimeShape.localScaling= Vector3(1f,1f,1f)
        slimeShape.margin=1f
        //var shape= Bullet.obtainStaticNodeShape(alienSlime.nodes)
        var localInertia=Vector3()

        slimeShape.calculateLocalInertia(1f, localInertia)
        slimeShape.updateBound()

        var info= btRigidBody.btRigidBodyConstructionInfo(1f,null, slimeShape, localInertia)
        var body= btRigidBody(info)

        var motionState=MotionState(alienSlime.transform)
        body.motionState=motionState

        renderInstances.add(alienSlime)
        dynamicsWorld!!.addRigidBody(body)
        //var dispatcher= dynamicsWorld!!.dispatcher
        btGImpactCollisionAlgorithm.registerAlgorithm(dispatcher as btCollisionDispatcher)
        //btGImpactCollisionAlgorithm.registerAlgorithm(dispatcher as btCollisionDispatcher?)

    }

    override fun render(delta: Float) {
        super.render(delta)
        debugDrawer.begin(camera)
        dynamicsWorld!!.debugDrawWorld()
        debugDrawer.end()
    }
}