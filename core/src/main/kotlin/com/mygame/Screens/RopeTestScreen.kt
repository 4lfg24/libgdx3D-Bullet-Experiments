package com.mygame.Screens

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.VertexAttribute
import com.badlogic.gdx.graphics.g3d.Material
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.SphereShapeBuilder
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.bullet.collision.Collision
import com.badlogic.gdx.physics.bullet.collision.btSphereShape
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld
import com.badlogic.gdx.physics.bullet.dynamics.btPoint2PointConstraint
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody
import com.badlogic.gdx.physics.bullet.dynamics.btTypedConstraint
import com.badlogic.gdx.physics.bullet.linearmath.btIDebugDraw
import com.mygame.MotionState

class RopeTestScreen(game:Game):BaseScreen(game) {

    var constraints=ArrayList<btTypedConstraint>()
    lateinit var testBody:btRigidBody
    lateinit var testBody2: btRigidBody
    lateinit var testBody3:btRigidBody
    init {
        initializeBullet()
        createAxes()
        createFloor(30f, 1f, 30f)
        createBodies()
        var constraint= btPoint2PointConstraint(floorBody,testBody, Vector3(0f, 0f, 0f),
            Vector3(0f,0f,0f)
        )
        //the value of the two vectors are the distances from the center of the two bodies
        //where the constraints are applied

        //adding the constraint to the world
        (dynamicsWorld as btDynamicsWorld).addConstraint(constraint, false)
        constraints.add(constraint)
        debugDrawer.debugMode= btIDebugDraw.DebugDrawModes.DBG_MAX_DEBUG_DRAW_MODE
    }

    private fun createBodies() {
        var sphere2:btRigidBody?=null

        for (i in 1 until 5) {
            val modelBuilder = ModelBuilder()
            modelBuilder.begin()
            val meshBuilder = modelBuilder.part(
                "body",
                GL20.GL_TRIANGLES,
                (VertexAttribute.Position().usage or VertexAttribute.Normal().usage or VertexAttribute.TexCoords(
                    0
                ).usage).toLong(),
                Material()
            )
            SphereShapeBuilder.build(meshBuilder, 2f, 2f, 2f, 8, 8)
            val btSPhereShape = btSphereShape(1f)
            val box = modelBuilder.end()
            val boxInstance = ModelInstance(box)

            boxInstance.transform.trn(0f, -4f*i, 0f)
            var localInertia=Vector3()
            btSPhereShape.calculateLocalInertia(1f, localInertia)

            val info = btRigidBody.btRigidBodyConstructionInfo(1f, null, btSPhereShape, localInertia)
            val body = btRigidBody(info)

            var motionState= MotionState(boxInstance.transform)
            body.motionState=motionState
            body.activationState= Collision.DISABLE_DEACTIVATION

            renderInstances.add(boxInstance)
            dynamicsWorld!!.addRigidBody(body)
        }

        }


    override fun render(delta: Float) {
        super.render(delta)
        dynamicsWorld?.stepSimulation(delta)
        if(Gdx.input.isKeyJustPressed(Input.Keys.SPACE)){
            testBody.applyCentralImpulse(Vector3(0f, -1000f, 0f))
        }
        debugDrawer.begin(camera)
        dynamicsWorld!!.debugDrawWorld()
        debugDrawer.end()
    }
}