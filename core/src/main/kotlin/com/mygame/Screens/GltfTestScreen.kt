package com.mygame.Screens

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Cubemap
import com.badlogic.gdx.graphics.PerspectiveCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g3d.utils.FirstPersonCameraController
import net.mgsx.gltf.loaders.gltf.GLTFLoader
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx
import net.mgsx.gltf.scene3d.scene.Scene
import net.mgsx.gltf.scene3d.scene.SceneAsset
import net.mgsx.gltf.scene3d.scene.SceneManager
import net.mgsx.gltf.scene3d.scene.SceneSkybox
import net.mgsx.gltf.scene3d.utils.IBLBuilder


class GltfTestScreen(game:Game):BaseScreen(game) {

    private var sceneManager: SceneManager? = null
    private var sceneAsset: SceneAsset? = null
    private var scene: Scene? = null
    private var cam: PerspectiveCamera? = null
    private var diffuseCubemap: Cubemap? = null
    private var environmentCubemap: Cubemap? = null
    private var specularCubemap: Cubemap? = null
    private var brdfLUT: Texture? = null
    private val time = 0f
    private var skybox: SceneSkybox? = null
    private var light: DirectionalLightEx? = null
    private var camController: FirstPersonCameraController? = null



    init {
        //createAxes()
        initializeGltf()
        //initializeBullet()
        //it's lagging a lot, probably something about bullet
        //removing step simulation doesn't help
    }

    override fun render(delta: Float) {
        super.render(delta)
        camController?.update()
        sceneManager?.update(delta)
        sceneManager?.render()
        /*dynamicsWorld?.stepSimulation(delta, 5, 1f/60f)

        debugDrawer.begin(camera)
        dynamicsWorld?.debugDrawWorld()
        debugDrawer.end()

         */

    }




    fun initializeGltf(){
        sceneAsset = GLTFLoader().load(Gdx.files.internal("3DModels/mixamo-animation.gltf"))
        scene = Scene(sceneAsset?.scene)
        scene?.modelInstance?.transform?.scale(3f, 3f, 3f)
        sceneManager = SceneManager()
        sceneManager!!.addScene(scene)

        camera = PerspectiveCamera(60f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())

        camera.near = 1f
        camera.far = 200f
        sceneManager!!.setCamera(camera)
        camera.position[0f, 0.5f] = 4f

        cameraController = FirstPersonCameraController(camera)
        Gdx.input.inputProcessor = cameraController

        light = DirectionalLightEx()
        light!!.direction.set(1f, -3f, 1f).nor()
        light!!.color.set(Color.WHITE)
        sceneManager!!.environment.add(light)

        val iblBuilder = IBLBuilder.createOutdoor(light)
        environmentCubemap = iblBuilder.buildEnvMap(1024)
        diffuseCubemap = iblBuilder.buildIrradianceMap(256)
        specularCubemap = iblBuilder.buildRadianceMap(10)
        iblBuilder.dispose()


        brdfLUT = Texture(Gdx.files.classpath("net/mgsx/gltf/shaders/brdfLUT.png"))

        sceneManager!!.setAmbientLight(1f)
        sceneManager!!.environment.set(PBRTextureAttribute(PBRTextureAttribute.BRDFLUTTexture, brdfLUT))
        sceneManager!!.environment.set(PBRCubemapAttribute.createSpecularEnv(specularCubemap))
        sceneManager!!.environment.set(PBRCubemapAttribute.createDiffuseEnv(diffuseCubemap))

        skybox = SceneSkybox(environmentCubemap)
        sceneManager!!.skyBox = skybox

        scene!!.animationController.setAnimation("jump", -1)
    }

    override fun dispose() {
        super.dispose()
    }
}