package com.mygame.Screens

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.VertexAttribute
import com.badlogic.gdx.graphics.g3d.Material
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.collision.BoundingBox
import com.badlogic.gdx.physics.bullet.collision.ClosestRayResultCallback
import com.badlogic.gdx.physics.bullet.collision.Collision
import com.badlogic.gdx.physics.bullet.collision.btBoxShape
import com.badlogic.gdx.physics.bullet.collision.btCapsuleShape
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody
import com.mygame.MotionState
import com.mygame.utils.Utils3D
import ktx.math.div
import ktx.math.times

class GrapplingHookTest(game: Game):BaseScreen(game) {

    var rayFromWorld = Vector3()
    var rayToWorld = Vector3()
    var callback = ClosestRayResultCallback(Vector3(), Vector3())
    var lastRayFrom=Vector3()
    var lastRayTo=Vector3()
    var bodyHit:btRigidBody?=null

    lateinit var playerBody: btRigidBody

    init {
        initializeBullet()
        createFloor(40f, 1f, 40f)
        createAxes()
        createStaticBodies()
    }

    override fun render(delta: Float) {
        super.render(delta)
        dynamicsWorld?.stepSimulation(delta)

        debugDrawer.begin(camera)
        shootGrapple()
        debugDrawer.drawLine(lastRayFrom, lastRayTo,Vector3(1f, 0f, 0f))

        dynamicsWorld!!.debugDrawWorld()
        debugDrawer.end()
    }

    fun createStaticBodies() {
        val modelBuilder = ModelBuilder()
        modelBuilder.begin()
        val meshBuilder = modelBuilder.part(
            "static body",
            GL20.GL_TRIANGLES,
            (VertexAttribute.Position().usage or VertexAttribute.Normal().usage or VertexAttribute.TexCoords(
                0
            ).usage).toLong(),
            Material()
        )
        BoxShapeBuilder.build(meshBuilder, 2f, 2f, 2f)
        val btBoxShape = btBoxShape(Vector3(2f / 2f, 2 / 2f, 2 / 2f))
        val firstBody = modelBuilder.end()
        val firstBodyInstance = ModelInstance(firstBody)
        firstBodyInstance.transform.trn(8f, 10f, 0f)

        val secondBodyInstance = ModelInstance(firstBody)
        secondBodyInstance.transform.trn(-6f, 11f, 0f)

        val info = btRigidBody.btRigidBodyConstructionInfo(0f, null, btBoxShape, Vector3.Zero)
        val body1 = btRigidBody(info)
        val body2 = btRigidBody(info)

        body1.worldTransform = firstBodyInstance.transform
        body2.worldTransform = secondBodyInstance.transform


        renderInstances.add(firstBodyInstance)
        renderInstances.add(secondBodyInstance)
        dynamicsWorld?.addRigidBody(body1)
        dynamicsWorld?.addRigidBody(body2)
        createPlayer()
    }

    fun shootGrapple() {
        //the way I want to make it work is that if the ray hits a body (except the player) than calculate the distance between
        //the player and that body, then move it
        if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
            var ray = camera.getPickRay(Gdx.input.x.toFloat(), Gdx.input.y.toFloat())

            rayFromWorld.set(ray.origin)
            ray.getEndPoint(rayToWorld, 1000000f)
            //rayToWorld.set(ray.direction).scl(10000000000f).add(ray.origin)
            lastRayFrom.set(rayFromWorld).sub(0f, 5f, 0f)

            dynamicsWorld?.rayTest(rayFromWorld, rayToWorld, callback)
            if(callback.hasHit()){
                lastRayTo.set(rayFromWorld) //this is just for debugging
                lastRayTo.lerp(rayToWorld, callback.closestHitFraction)
                bodyHit= callback.collisionObject as btRigidBody?
                //now calculate the distance between the player and the body hit
                var playerPosition=playerBody.centerOfMassTransform.getTranslation(Vector3())
                var bodyPosition=bodyHit?.centerOfMassTransform?.getTranslation(Vector3())
                var distance=Vector3(bodyPosition?.sub(playerPosition))
                println(distance)
                var speed=5f
                playerBody.applyCentralImpulse(Vector3(distance*speed))
                //it almost works, the problem is, the ray doesn't go on forever,
                //it stops at a certain point
            }else{
                lastRayTo.set(rayToWorld)
            }
        }
    }

    private fun createPlayer() {

        var playerModelInstance = ModelInstance(Utils3D.buildCapsuleCharacter())
        //move him above the ground
        playerModelInstance.transform.setToTranslation(0f, 4f, 0f)
        //calculate dimensions
        var boundingBox = BoundingBox()
        playerModelInstance.calculateBoundingBox(boundingBox)
        var dimensions = Vector3()
        boundingBox.getDimensions(dimensions)
        //bullet uses half extent, so we scale it by half
        dimensions.scl(0.5f)

        var motionState = MotionState(playerModelInstance.transform)

        var capsuleShape = btCapsuleShape(dimensions.len() / 2.5f, dimensions.y)

        var mass = 2f

        var inertia = Vector3()

        capsuleShape.calculateLocalInertia(mass, inertia)

        var info = btRigidBody.btRigidBodyConstructionInfo(mass, motionState, capsuleShape, inertia)
        var body = btRigidBody(info)

        //this will keep the body from falling over
        body.angularFactor = Vector3.Y

        body.activationState = Collision.DISABLE_DEACTIVATION
        //kind of like friction but the body doesn't need to touch another
        //body for it to apply, you could use friction as well
        body.setDamping(0.75f, 0.99f)
        body.gravity= Vector3.Zero
        playerBody=body

        renderInstances.add(playerModelInstance)
        dynamicsWorld?.addRigidBody(playerBody)
    }
}