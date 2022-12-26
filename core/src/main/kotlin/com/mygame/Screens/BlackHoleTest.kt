package com.mygame.Screens

import com.badlogic.gdx.Game
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
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Quaternion
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.bullet.collision.*
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody
import com.mygame.MotionState
import ktx.math.compareTo
import ktx.math.minus
import ktx.math.plusAssign
import kotlin.math.*

class BlackHoleTest(game: Game) : BaseScreen(game) {

    //lateinit var blaclHoleInstance: ModelInstance
    lateinit var floorTransform: Matrix4
    lateinit var bodies: ArrayList<btRigidBody>
    lateinit var blackHoleBody: btRigidBody
    lateinit var otherBody: btRigidBody
    lateinit var otherBodyTransform:Matrix4

    init {
        initializeBullet()
        initializeModels()
        //createBlackHole()
        createRandomBodies()
        println(
            "Black hole X: ${blackHoleBody.centerOfMassPosition.x} \n" +
                    "Black hole Y: ${blackHoleBody.centerOfMassPosition.y} \n" +
                    "Black hole Z: ${blackHoleBody.centerOfMassPosition.z} \n"
        )
    }

    private fun createRandomBodies() {
        bodies = ArrayList()

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

        BoxShapeBuilder.build(builder, 0f, 0f, 0f, 1f, 1f, 1f)

        shape = btBoxShape(Vector3(0.5f, 0.5f, 0.5f))
        val box = ModelInstance(modelBuilder.end())
        box.transform.setToTranslation(
            -5f,
            10f,
            3f
        )
        box.transform.rotate(Quaternion(Vector3.Z, MathUtils.random(0f, 270f)))
        val mass = 1f
        val localInertia = Vector3()
        shape.calculateLocalInertia(mass, localInertia)
        val info = btRigidBody.btRigidBodyConstructionInfo(mass, null, shape, localInertia)
        val body = btRigidBody(info)
        val motionState = MotionState(box.transform)
        otherBodyTransform=box.transform
        body.motionState = motionState
        otherBody=body
        renderInstances.add(box)
        bodies.add(otherBody)
        dynamicsWorld?.addRigidBody(otherBody)

    }

    fun initializeModels() {
        val modelBuilder = ModelBuilder()
        modelBuilder.begin()
        val meshBuilder = modelBuilder.part(
            "floor",
            GL20.GL_TRIANGLES,
            (VertexAttribute.Position().usage or VertexAttribute.Normal().usage or VertexAttribute.TexCoords(
                0
            ).usage).toLong(),
            Material()
        )
        //BoxShapeBuilder.build(meshBuilder, 1f, 1f, 1f)
        SphereShapeBuilder.build(meshBuilder, 2f, 2f, 2f, 8, 8)
        val btSPhereShape = btSphereShape(1f)
        val floor = modelBuilder.end()
        val floorInstance = ModelInstance(floor)
        floorInstance.transform.trn(0f, -0.5f, 0f)
        floorTransform = floorInstance.transform
        //setting a rigid body to the floor
        val info = btRigidBody.btRigidBodyConstructionInfo(0f, null, btSPhereShape, Vector3.Zero)
        val body = btRigidBody(info)
        blackHoleBody = body
        blackHoleBody.worldTransform = floorInstance.transform
        renderInstances.add(floorInstance)
        dynamicsWorld?.addRigidBody(blackHoleBody)
    }

    /*fun createBlackHole() {
        val modelBuilder = ModelBuilder()
        modelBuilder.begin()
        val meshBuilder = modelBuilder.part(
            "black hole",
            GL20.GL_TRIANGLES,
            (VertexAttribute.Position().usage or VertexAttribute.Normal().usage or VertexAttribute.TexCoords(
                0
            ).usage).toLong(),
            Material()
        )
        BoxShapeBuilder.build(meshBuilder, 2f, 2f, 2f)
        val btBoxShape = btBoxShape(Vector3(2 / 2f, 2 / 2f, 2f / 2f))
        val blackHole = modelBuilder.end()
        val blackHoleInstance = ModelInstance(blackHole)
        blackHoleInstance.transform.trn(0f, 10f, 0f)

        //setting a rigid body to the floor
        val mass = 1f
        val localInertia = Vector3()
        btBoxShape.calculateLocalInertia(mass, localInertia)
        val info = btRigidBody.btRigidBodyConstructionInfo(mass, null, btBoxShape, localInertia)
        val body = btRigidBody(info)
        body.worldTransform = blackHoleInstance.transform
        //body.gravity= Vector3(0f, 2f, 2f)
        //in the tes project/CollisionWorldTest it seems possible
        //to use ghost bodies, but there is no use of a dynamics world

        renderInstances.add(blackHoleInstance)
        dynamicsWorld.addRigidBody(body)


    }

     */
    override fun render(delta: Float) {
        super.render(delta)
        dynamicsWorld?.stepSimulation(delta)
        debugDrawer.begin(camera)
        dynamicsWorld?.debugDrawWorld()
        //debugDrawer.draw3dText(Vector3(0f, 1f, 0f),"Does this work?") //cool
        //code for the black hole logic
        calculateBlackHoleForce()
        debugDrawer.end()
        //println(dynamicsWorld.numCollisionObjects)

    }

    fun calculateBlackHoleForce() {

            var tmp = Vector3(0f, 0f, 0f)
            //getTranslation populates a vector3 with the transform 3d position
            otherBodyTransform.getTranslation(tmp)

            var floorPosition = blackHoleBody.centerOfMassPosition
            floorTransform.getTranslation(floorPosition)

            debugDrawer.drawLine(tmp, floorPosition, Vector3(1f, 1f, 1f))
            //now the hard part, calculate the force to be applied based
            //on the black hole's position
            //F=constant/distance*distance
            var pullingStrenght = 3f //the force with which the black hole reels bodies
            //var force=pullingStrenght/(distance*distance)
            var distanceVector = Vector3(tmp.sub(floorPosition))

        otherBody.applyCentralForce(Vector3(-distanceVector.x / pullingStrenght, -distanceVector.y / pullingStrenght, -distanceVector.z/pullingStrenght))

    }
}