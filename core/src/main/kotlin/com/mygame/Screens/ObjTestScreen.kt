package com.mygame.Screens

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.bullet.Bullet
import com.badlogic.gdx.physics.bullet.DebugDrawer
import com.badlogic.gdx.physics.bullet.collision.*
import com.badlogic.gdx.physics.bullet.dynamics.*
import com.badlogic.gdx.physics.bullet.linearmath.btIDebugDraw
import com.mygame.utils.Utils3D
import ktx.assets.disposeSafely

class ObjTestScreen(game: Game):BaseScreen(game) {
    private var sceneInstance: ModelInstance
    var sceneModel:Model

    init {
        createAxes()
        sceneModel=Utils3D.loadOBJ(Gdx.files.internal("3DModels/greenhill.obj"))
        sceneInstance=ModelInstance(sceneModel)

        renderInstances.add(sceneInstance)
        initializeBullet()
    }

    override fun render(delta: Float) {
        super.render(delta)
        dynamicsWorld?.stepSimulation(delta, 5, 1f/60f)

        debugDrawer.begin(camera)
        dynamicsWorld?.debugDrawWorld()
        debugDrawer.end()
    }
    /*fun initializeBullet() {
        collisionConfig = btDefaultCollisionConfiguration()
        dispatcher = btCollisionDispatcher(collisionConfig)
        broadphase = btDbvtBroadphase()
        //setting the dynamic world
        constraintSolver= btSequentialImpulseConstraintSolver()
        dynamicsWorld= btDiscreteDynamicsWorld(dispatcher,broadphase, constraintSolver, collisionConfig)
        dynamicsWorld.gravity= Vector3(0f, -1.8f, 0f)

        //contactListener = MyContactListener()

        debugDrawer=DebugDrawer().apply {
            debugMode= btIDebugDraw.DebugDrawModes.DBG_DrawWireframe
        }
        dynamicsWorld.debugDrawer=debugDrawer
        //initializing the model
        var shape= Bullet.obtainStaticNodeShape(sceneInstance.nodes)

        var info= btRigidBody.btRigidBodyConstructionInfo(0f,null, shape, Vector3.Zero)
        var body= btRigidBody(info)
        dynamicsWorld.addRigidBody(body)
        //if you don't dispose it lags a lot
        shape.disposeSafely()
    }

     */
}