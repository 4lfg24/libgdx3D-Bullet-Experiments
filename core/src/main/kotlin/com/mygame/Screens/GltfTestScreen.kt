package com.mygame.Screens

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Cubemap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.bullet.Bullet
import com.badlogic.gdx.physics.bullet.DebugDrawer
import com.badlogic.gdx.physics.bullet.collision.*
import com.badlogic.gdx.physics.bullet.dynamics.*
import com.badlogic.gdx.physics.bullet.linearmath.btIDebugDraw
import com.mygame.utils.Utils3D
import ktx.assets.disposeSafely
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx
import net.mgsx.gltf.scene3d.scene.Scene
import net.mgsx.gltf.scene3d.scene.SceneManager
import net.mgsx.gltf.scene3d.scene.SceneSkybox
import net.mgsx.gltf.scene3d.utils.IBLBuilder

class GltfTestScreen(game:Game):BaseScreen(game) {

lateinit var sceneManager:SceneManager
    lateinit var floorScene: Scene
    lateinit var diffuseCubemap: Cubemap
    lateinit var environmentCubemap: Cubemap
    lateinit var specularCubemap: Cubemap
    lateinit var brdfLUT: Texture
    lateinit var skybox: SceneSkybox
    lateinit var light: DirectionalLightEx



    init {
        createAxes()
        initializeGltf()
        initializeBullet()
        //it's lagging a lot, probably something about bullet
        //removing step simulation doesn't help
    }

    override fun render(delta: Float) {
        super.render(delta)

        sceneManager.update(delta)
        sceneManager.render()
        dynamicsWorld?.stepSimulation(delta, 5, 1f/60f)

        debugDrawer.begin(camera)
        dynamicsWorld?.debugDrawWorld()
        debugDrawer.end()

    }




    fun initializeGltf(){
        sceneManager= SceneManager()
        floorScene=Utils3D.loadGltf(Gdx.files.internal("greenhill/greenhills.gltf"), sceneManager)
        sceneManager.camera=camera
        // setup light
        light = DirectionalLightEx()
        light.direction.set(1f, -3f, 1f).nor()
        light.color.set(Color.WHITE)
        sceneManager.environment.add(light)

        // setup quick IBL (image based lighting)
        val iblBuilder = IBLBuilder.createOutdoor(light)
        environmentCubemap = iblBuilder.buildEnvMap(1024)
        diffuseCubemap = iblBuilder.buildIrradianceMap(256)
        specularCubemap = iblBuilder.buildRadianceMap(10)
        iblBuilder.dispose()

        // This texture is provided by the library, no need to have it in your assets.
        brdfLUT = Texture(Gdx.files.classpath("net/mgsx/gltf/shaders/brdfLUT.png"))
        sceneManager.setAmbientLight(1f)
        sceneManager.environment.set(
            PBRTextureAttribute(
                PBRTextureAttribute.BRDFLUTTexture,
                brdfLUT
            )
        )
        sceneManager.environment.set(PBRCubemapAttribute.createSpecularEnv(specularCubemap))
        sceneManager.environment.set(PBRCubemapAttribute.createDiffuseEnv(diffuseCubemap))

        // setup skybox
        skybox = SceneSkybox(environmentCubemap)
        sceneManager.skyBox = skybox
    }

    override fun dispose() {
        super.dispose()

    }
}