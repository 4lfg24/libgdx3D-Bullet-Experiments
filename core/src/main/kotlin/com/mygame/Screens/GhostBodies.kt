package com.mygame.Screens

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.controllers.Controller
import com.badlogic.gdx.controllers.ControllerAdapter
import com.badlogic.gdx.controllers.ControllerListener
import com.badlogic.gdx.controllers.Controllers
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.VertexAttribute
import com.badlogic.gdx.graphics.g3d.Material
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.SphereShapeBuilder
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.collision.BoundingBox
import com.badlogic.gdx.physics.bullet.collision.btBoxShape
import com.badlogic.gdx.physics.bullet.collision.btSphereShape
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody
import com.mygame.MotionState

class GhostBodies(game:Game):BaseScreen(game){

    lateinit var ghostBodyBoundingBox:BoundingBox
    lateinit var playerBoundingBox:BoundingBox
    lateinit var playerBody: btRigidBody
    lateinit var playerModelInstance:ModelInstance
    //Controller section, just for testing
    lateinit var connetedController:Controller
    lateinit var controllerListener: ControllerListener

    init {
        initializeBullet()
        createAxes()
        createFloor(30f, 2f, 30f)
        //initializeGhostWorld()
        initializeGhostBody()
        initializePlayer()
        initializeController()
    }

    private fun initializeController() {
        controllerListener=object : ControllerAdapter(){
            override fun connected(controller: Controller?) {
                super.connected(controller)
                Gdx.app.log(
                    "Controller", "Controller connected: " + controller!!.name
                            + "/" + controller.uniqueId
                )
            }

            override fun disconnected(controller: Controller?) {
                super.disconnected(controller)
                Gdx.app.log(
                    "Controller", "Controller disconnected: " + controller!!.name
                            + "/" + controller.uniqueId
                )
            }

            override fun buttonDown(controller: Controller?, buttonIndex: Int): Boolean {
                println("Button has been pressed on controller")
                return super.buttonDown(controller, buttonIndex)
            }

            override fun buttonUp(controller: Controller?, buttonIndex: Int): Boolean {
                return super.buttonUp(controller, buttonIndex)
            }

            override fun axisMoved(controller: Controller?, axisIndex: Int, value: Float): Boolean {
                if(value<1&&value>0){
                    //i gotta dig into it deeper
                    playerBody.applyCentralForce(Vector3())
                }
                return super.axisMoved(controller, axisIndex, value)
            }
        }
        Controllers.addListener(object : ControllerAdapter(){
            override fun connected(controller: Controller?) {
                super.connected(controller)
                Gdx.app.log(
                    "Controller", "Controller connected: " + controller!!.name
                            + "/" + controller.uniqueId
                )
            }

            override fun disconnected(controller: Controller?) {
                super.disconnected(controller)
                Gdx.app.log(
                    "Controller", "Controller disconnected: " + controller!!.name
                            + "/" + controller.uniqueId
                )
            }

            override fun buttonDown(controller: Controller?, buttonIndex: Int): Boolean {
                //it works here, but not for controller listener
                println("Button has been pressed")
                return super.buttonDown(controller, buttonIndex)
            }

            override fun buttonUp(controller: Controller?, buttonIndex: Int): Boolean {
                return super.buttonUp(controller, buttonIndex)
            }

            override fun axisMoved(controller: Controller?, axisIndex: Int, value: Float): Boolean {
                if(controller==connetedController){
                    println("Axis index: $axisIndex, value: $value")
                }
                return true
            }
        })
        if(Controllers.getCurrent()!=null) {
            connetedController = Controllers.getCurrent()
            connetedController.startVibration(200, 1f)
        }
    }

    fun initializeGhostBody(){
        //i think it's just better to use bounding boxes
        val modelBuilder = ModelBuilder()
        modelBuilder.begin()
        val meshBuilder = modelBuilder.part(
            "ghostBody",
            GL20.GL_TRIANGLES,
            (VertexAttribute.Position().usage or VertexAttribute.Normal().usage or VertexAttribute.TexCoords(
                0
            ).usage).toLong(),
            Material()
        )
        BoxShapeBuilder.build(meshBuilder, 3f, 1f, 3f)
        val boxShape= btBoxShape(Vector3(3f/2f, 1f/2f, 3f/2f))
        val hoverBoard = modelBuilder.end()
        val ghostBodyInstance = ModelInstance(hoverBoard)

        ghostBodyInstance.transform.trn(-5f, 5f, 0f)

        ghostBodyBoundingBox=ghostBodyInstance.calculateBoundingBox(BoundingBox())
        //ok it initializes properly
        println(ghostBodyBoundingBox.height*ghostBodyBoundingBox.width*ghostBodyBoundingBox.depth)
        //using mul you can set iy's matrix to another one
        ghostBodyBoundingBox.mul(ghostBodyInstance.transform)
        ghostBodyBoundingBox.update()
        renderInstances.add(ghostBodyInstance)
    }
    fun initializePlayer(){
        val modelBuilder = ModelBuilder()
        modelBuilder.begin()
        val meshBuilder = modelBuilder.part(
            "player",
            GL20.GL_TRIANGLES,
            (VertexAttribute.Position().usage or VertexAttribute.Normal().usage or VertexAttribute.TexCoords(
                0
            ).usage).toLong(),
            Material()
        )
        SphereShapeBuilder.build(meshBuilder,2f, 2f, 2f, 8, 8)
        val sphereShape= btSphereShape(1f)
        val player = modelBuilder.end()
        val playerInstance = ModelInstance(player)

        playerInstance.transform.trn(0f, 4f, 0f)

        playerBoundingBox=playerInstance.calculateBoundingBox(BoundingBox())
        //ok it initializes properly
        println(playerBoundingBox.height*playerBoundingBox.width*playerBoundingBox.depth)
        playerBoundingBox.mul(playerInstance.transform)

        val info = btRigidBody.btRigidBodyConstructionInfo(1f, null, sphereShape, Vector3.Zero)
        val body = btRigidBody(info)

        val motionState = MotionState(playerInstance.transform)
        body.motionState = motionState
        playerBody=body
        playerModelInstance=playerInstance

        renderInstances.add(playerInstance)
        dynamicsWorld?.addRigidBody(playerBody)
    }


    override fun render(delta: Float) {
        super.render(delta)
        dynamicsWorld?.stepSimulation(delta)
        //I dunno why but it works like this
        playerModelInstance.calculateBoundingBox(playerBoundingBox)
        playerBoundingBox.mul(playerModelInstance.transform)
        playerBoundingBox.update()
        //render everything
        debugDrawer.begin(camera)
        dynamicsWorld?.debugDrawWorld()
        debugDrawer.drawBox(ghostBodyBoundingBox.min, ghostBodyBoundingBox.max, Vector3(1f, 0f, 0f))
        debugDrawer.drawBox(playerBoundingBox.min, playerBoundingBox.max, Vector3(0f, 0f, 1f))
        //player movement
        if(Gdx.input.isKeyPressed(Input.Keys.LEFT)){
            playerBody.activate()
            playerBody.applyCentralForce(Vector3(-10f, 0f, 0f))
        }
        if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)){
            playerBody.activate()
            playerBody.applyCentralForce(Vector3(10f, 0f, 0f))
        }
        if(Gdx.input.isKeyPressed(Input.Keys.UP)){
            playerBody.activate()
            playerBody.applyCentralForce(Vector3(0f, 0f, -10f))
        }
        if(Gdx.input.isKeyPressed(Input.Keys.DOWN)){
            playerBody.activate()
            playerBody.applyCentralForce(Vector3(0f, 0f, 10f))
        }
        //jump mechanic
        if(Gdx.input.isKeyJustPressed(Input.Keys.SPACE)){
            playerBody.activate()
            playerBody.applyCentralImpulse(Vector3(0f, 10f, 0f))
        }
        //check if the two bounding boxes collide
        if(playerBoundingBox.intersects(ghostBodyBoundingBox)){
            println("Collision detected")
        } //it works, gg
        //code for movement with the controller
        if(connetedController.isConnected){
            connetedController.addListener(controllerListener)
        }
        debugDrawer.end()
    }
}