package com.mygame.Screens

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.bullet.dynamics.btPoint2PointConstraint
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody
import com.badlogic.gdx.physics.bullet.softbody.btSoftBody
import com.badlogic.gdx.physics.bullet.softbody.btSoftBodyHelpers
import com.badlogic.gdx.physics.bullet.softbody.btSoftBodyWorldInfo
import kotlin.random.Random

class RopeScreen(game:Game):BaseScreen(game) {
    var player: btRigidBody
    var rope: btSoftBody?=null
    var anchor:btRigidBody
    lateinit var worldInfo:btSoftBodyWorldInfo
    init {
        initializeBullet()
        createAxes()
        createFloor(20f, 1f, 20f)
        //need to create the softbody world info
        worldInfo= btSoftBodyWorldInfo()
        worldInfo.broadphase=this.broadphase
        worldInfo.dispatcher=this.dispatcher
        worldInfo.sparsesdf.Initialize()
        player=createCube(2f, 5f, 1f, 2f, 2f, 2f, true)
        anchor=createCube(0f, 7f, 0f, 1f, 1f,1f, false)
        rope=createRope(anchor.centerOfMassTransform.getTranslation(Vector3()),anchor.centerOfMassTransform.getTranslation(Vector3()))
        //createRope(player.centerOfMassPosition, Vector3(4f, 1f, 1f))
        //a possible approach would be to create a rope from the player position to the block position when the mouse is pressed
        //then create a constraint between the body and the center of the rope (we'll get more precise later)

    }

    private fun createRope(from:Vector3, to:Vector3):btSoftBody {
        //this is where the fun begins

        //now we can start creating the rope

        //the vectors 3 are the starting and ending position
        var rope= btSoftBodyHelpers.CreateRope(worldInfo, from, to, 15, 15)

        //idk yet
        rope.totalMass=100f
        dynamicsWorld?.addSoftBody(rope)
        //wasn't so hard now was it? All I need is a model to pair it with
        //and if possible find a way to make it anchored to only one point
        return rope
    }
    fun handleInput(){
        if(Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)){

            rope!!.appendAnchor(0, player, false)
            //ok basically I created a rope at the anchor point that has 0 lenght (the starting point and ending point
        // are the same, the center of the anchor), and then added the player body as an anchor, and it seems to work fine
        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.SPACE)){
            player.applyCentralImpulse(Vector3(Random.nextInt(-5, 5).toFloat(),
                Random.nextInt(-5, 5).toFloat(),Random.nextInt(-5, 5).toFloat()))
        }
    }

    override fun render(delta: Float) {
        super.render(delta)
        dynamicsWorld?.stepSimulation(delta, 5, 1f/60f)
        handleInput()

        debugDrawer.begin(camera)
        dynamicsWorld?.debugDrawWorld()
        debugDrawer.end()
    }
}