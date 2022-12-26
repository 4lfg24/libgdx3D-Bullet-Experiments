package com.mygame.Screens

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.VertexAttribute
import com.badlogic.gdx.graphics.VertexAttributes
import com.badlogic.gdx.graphics.g3d.Material
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.ConeShapeBuilder
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.CylinderShapeBuilder
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.SphereShapeBuilder
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Quaternion
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.bullet.DebugDrawer
import com.badlogic.gdx.physics.bullet.collision.*
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody
import com.mygame.MotionState
import ktx.math.times

class ExplosionTest(game: Game):BaseScreen(game) {

    lateinit var bodies:ArrayList<btRigidBody>
    var rocketBody:btRigidBody?=null
    lateinit var contactListener: MyContactListener
    init {
        bodies= ArrayList()
        initializeBullet()
        createFloor(40f, 1f, 40f)
        createAxes()
        createRandomBodies()
        contactListener=MyContactListener(dynamicsWorld, bodies, debugDrawer)
    }

    override fun render(delta: Float) {
        super.render(delta)
        dynamicsWorld?.stepSimulation(delta)
        debugDrawer.begin(camera)
        dynamicsWorld?.debugDrawWorld()
        shootRocket()
        //if the rocket touches the ground, make it explode (need a contact listener)
        debugDrawer.end()
    }

    private fun createRandomBodies() {
        var i = -6
        while (i < 6) {
            var j = -6
            while (j < 6) {
                val modelBuilder = ModelBuilder()
                modelBuilder.begin()
                val material = Material()
                material.set(
                    ColorAttribute.createDiffuse(
                        randomColor
                    )
                )
                val builder = modelBuilder.part(
                    "box",
                    GL20.GL_TRIANGLES,
                    (VertexAttributes.Usage.Position or VertexAttributes.Usage.Normal).toLong(),
                    material
                )
                var shape: btCollisionShape
                val random = MathUtils.random(1, 4)
                shape = when (random) {
                    1 -> {
                        BoxShapeBuilder.build(builder, 0f, 0f, 0f, 1f, 1f, 1f)
                        btBoxShape(Vector3(0.5f, 0.5f, 0.5f))
                    }
                    2 -> {
                        ConeShapeBuilder.build(builder, 1f, 1f, 1f, 8)
                        btConeShape(0.5f, 1f)
                    }
                    3 -> {
                        SphereShapeBuilder.build(builder, 1f, 1f, 1f, 8, 8)
                        btSphereShape(0.5f)
                    }
                    4 -> {
                        CylinderShapeBuilder.build(builder, 1f, 1f, 1f, 8)
                        btCylinderShape(Vector3(0.5f, 0.5f, 0.5f))
                    }
                    else -> {
                        CylinderShapeBuilder.build(builder, 1f, 1f, 1f, 8)
                        btCylinderShape(Vector3(0.5f, 0.5f, 0.5f))
                    }
                }
                val box = ModelInstance(modelBuilder.end())
                box.transform.setToTranslation(
                    i.toFloat(),
                    MathUtils.random(10, 20).toFloat(),
                    j.toFloat()
                )
                box.transform.rotate(Quaternion(Vector3.Z, MathUtils.random(0f, 270f)))
                val mass = 1f
                val localInertia = Vector3()
                shape.calculateLocalInertia(mass, localInertia)
                val info = btRigidBody.btRigidBodyConstructionInfo(mass, null, shape, localInertia)
                val body = btRigidBody(info)
                val motionState = MotionState(box.transform)
                body.motionState = motionState
                //body.collisionFlags=body.collisionFlags or btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK
                body.activationState=Collision.DISABLE_DEACTIVATION
                renderInstances.add(box)
                bodies.add(body)
                dynamicsWorld!!.addRigidBody(body)
                //println("User value: "+body.userValue)//it's 0 by default
                j += 2
            }
            i += 2
        }
    }
    fun shootRocket(){
        if(Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)&&rocketBody==null){
            //create a body and set its linear speed to the mouse direction*speed
            rocketBody=createRocket()
        }
    }
    fun createRocket():btRigidBody{
        val modelBuilder = ModelBuilder()
        modelBuilder.begin()
        val meshBuilder = modelBuilder.part(
            "rocket",
            GL20.GL_TRIANGLES,
            (VertexAttribute.Position().usage or VertexAttribute.Normal().usage or VertexAttribute.TexCoords(
                0
            ).usage).toLong(),
            Material()
        )
        BoxShapeBuilder.build(meshBuilder, 2f, 2f, 2f)
        val btBoxShape = btBoxShape(Vector3(2f / 2f, 2f / 2f, 2f / 2f))
        val floor = modelBuilder.end()
        val rocket = ModelInstance(floor)
        //getting the ray from the screen
        var pickRay=camera.getPickRay(Gdx.input.x.toFloat(),Gdx.input.y.toFloat())
        //it's actually ez
        rocket.transform.trn(camera.position)
        //setting a rigid body for the rocket
        val info = btRigidBody.btRigidBodyConstructionInfo(1f, null, btBoxShape, Vector3.Zero)
        val body = btRigidBody(info)
        val motionState = MotionState(rocket.transform)
        body.motionState = motionState

        body.collisionFlags=body.collisionFlags or btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK
        body.userValue=1 //to use the listener

        renderInstances.add(rocket)
        dynamicsWorld?.addRigidBody(body)
        bodies.add(body)

        body!!.linearVelocity= pickRay.direction*100f
        body!!.gravity=Vector3(0f,0f,0f)
        return body
    }
    fun removeRocket(){
        rocketBody=null
    }

    inner class MyContactListener(var world: btDynamicsWorld?, var bodies:ArrayList<btRigidBody>, var debugDrawer: DebugDrawer):ContactListener() {

        override fun onContactAdded(
            userValue0: Int,
            partId0: Int,
            index0: Int,
            userValue1: Int,
            partId1: Int,
            index1: Int
        ): Boolean {

            var objs = world?.collisionObjectArray
            if (objs != null) {
                loop@ for (i in 0 until objs.size()) {
                    if (objs.atConst(i).userValue == 1) {
                        explodeRocket(objs.atConst(i) as btRigidBody)
                        world?.removeRigidBody(objs.atConst(i) as btRigidBody)

                        break@loop
                    }
                }

            }
            return true
        }

        private fun explodeRocket(rocket:btRigidBody) {
            var objs = world?.collisionObjectArray
            if (objs!=null){
                for (i in 0 until objs.size()) {
                    if(objs.atConst(i).userValue!=1) {
                        var tmp = (objs.atConst(i) as btRigidBody).centerOfMassTransform.getTranslation(Vector3())
                        //ALWAYS USE CENTER OF MASS TRANSFORM GOD DAMMIT(for everyone who'll read this, sorry
                        // I've been trying to figure out the problem for hours and it wasn't working
                        // because I kept using centerofmassposition)
                        println(tmp)
                        var distanceVector = Vector3(tmp.sub(rocket.centerOfMassPosition))

                        var explosionForce = 1f
                        (objs.atConst(i) as btRigidBody).applyCentralImpulse(distanceVector * explosionForce)
                    }
                    removeRocket()
                }
            }
        }

    }
}