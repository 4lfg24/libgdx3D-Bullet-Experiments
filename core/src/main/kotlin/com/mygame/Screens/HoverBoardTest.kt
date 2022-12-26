package com.mygame.Screens

import com.badlogic.gdx.Game
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.VertexAttribute
import com.badlogic.gdx.graphics.g3d.Material
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.SphereShapeBuilder
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.bullet.collision.*
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody
import com.mygame.MotionState
import com.mygame.utils.Utils3D

class HoverBoardTest(game:Game):BaseScreen(game) {

    lateinit var bodies: ArrayList<btRigidBody>
    lateinit var hoverBoardBody:btRigidBody
    lateinit var hoverBoardCallback: ClosestNotMeRayResultCallback
    lateinit var hoverBoardTransform:Matrix4
    var lastRayFrom=Vector3()
    var lastRayTo=Vector3()
    var rayColor=Vector3(1f, 0f, 1f)
    var rayPosition1=Vector3()

    init {
        initializeBullet()
        createAxes()
        createFloor(50f, 1f, 50f)
        createHoverBoard()
        hoverBoardCallback= ClosestNotMeRayResultCallback(hoverBoardBody)
    }

    fun createHoverBoard(){
        val modelBuilder = ModelBuilder()
        modelBuilder.begin()
        val meshBuilder = modelBuilder.part(
            "hoverboard",
            GL20.GL_TRIANGLES,
            (VertexAttribute.Position().usage or VertexAttribute.Normal().usage or VertexAttribute.TexCoords(
                0
            ).usage).toLong(),
            Material()
        )
        BoxShapeBuilder.build(meshBuilder, 3f, 1f, 3f)
        val boxShape= btBoxShape(Vector3(3f/2f, 1f/2f, 3f/2f))
        val hoverBoard = modelBuilder.end()
        val hoverBoardInstance = ModelInstance(hoverBoard)
        hoverBoardTransform=hoverBoardInstance.transform

        hoverBoardInstance.transform.trn(0f, 10f, 0f)
        //floorTransform = floorInstance.transform
        //setting a rigid body to the floor
        val info = btRigidBody.btRigidBodyConstructionInfo(1f, null, boxShape, Vector3.Zero)
        val body = btRigidBody(info)
        //blackHoleBody = body
        //blackHoleBody.worldTransform = floorInstance.transform
        val motionState = MotionState(hoverBoardInstance.transform)
        body.motionState = motionState

        renderInstances.add(hoverBoardInstance)
        dynamicsWorld?.addRigidBody(body)
        hoverBoardBody=body
    }
    fun rayTouchesGround():Boolean{
        hoverBoardCallback.closestHitFraction=1.0f
        hoverBoardCallback.collisionObject=null
        Utils3D.getPosition(hoverBoardTransform, rayPosition1)
        var tempPosition=Vector3()
        //the distance of the ray is determined by this
        tempPosition.set(rayPosition1).sub(0f, 2f, 0f)
        rayCast(rayPosition1, tempPosition, hoverBoardCallback)

        return hoverBoardCallback.hasHit()
    }
    fun rayCast(from:Vector3, to:Vector3, callBack: RayResultCallback){

        //subtract the position just because the line is thin, and so you
        //wouldn't be able to see it from the camera's position
        lastRayFrom.set(from).sub(0f, 5f, 0f)
        dynamicsWorld?.rayTest(from, to, callBack)

        if (callBack.hasHit()){
            //we want to figure out where the ray hit, if it did
            lastRayTo.set(from)
            //closest hit fraction is the space between the start of the ray to the point it hit
            lastRayTo.lerp(to, callBack.closestHitFraction)

        }
        else{
            lastRayTo.set(to)
        }

    }
    override fun render(delta: Float) {
        super.render(delta)

        dynamicsWorld?.stepSimulation(delta)
        debugDrawer.begin(camera)
        dynamicsWorld?.debugDrawWorld()
        if(rayTouchesGround()){
            //apply force to make the hoverboard float
            //now it works it really depends on the force applied to it
            //(has to be kinda big since it's going in the opposite direction of gravity)
            //and the distance of the ray (how much earlier it detects the ground)
            hoverBoardBody.applyCentralImpulse(Vector3(0f, 0.25f, 0f))
            //remember impulse is applied immediately, while force is applied over time
            //ex.: if you add an impulse of 10 to a body with a speed of 0, that body will immediately reach a speed of 10
            //on the other hand, if you apply a force of 10, it will make the velocity 10 in the span of 1 second (or whatever unit you're using)
        }
        debugDrawer.end()
    }

}