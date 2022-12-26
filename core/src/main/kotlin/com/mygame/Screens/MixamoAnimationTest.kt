package com.mygame.Screens

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader
import com.badlogic.gdx.graphics.g3d.utils.AnimationController
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.bullet.Bullet
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody
import com.badlogic.gdx.utils.JsonReader
import com.mygame.MotionState

class MixamoAnimationTest(game:Game):BaseScreen(game) {
    lateinit var characterModel: Model
    lateinit var character: ModelInstance
    lateinit var controller:AnimationController
    init {
        //initializeBullet()
        createFloor(30f, 1f, 30f)
        createAxes()
        initializeAnimatedModel()

    }

    private fun initializeAnimatedModel() {
        //initializing 3d model with g3d technique
        //val modelBuilder = ModelBuilder() //seems like it's not necessary
        characterModel = G3dModelLoader(JsonReader()).loadModel(Gdx.files.internal("3DModels/mixamo-test1.g3dj"))
        character = ModelInstance(characterModel)
        character.transform.trn(0f, 5f, 0f)
        renderInstances.add(character)
        //preparing the animation
        controller= AnimationController(character)

        controller.setAnimation("Action.001")

    }

    override fun render(delta: Float) {
        super.render(delta)
        debugDrawer.begin(camera)
        //dynamicsWorld!!.debugDrawWorld()
        controller.update(delta)
        debugDrawer.end()
    }
}