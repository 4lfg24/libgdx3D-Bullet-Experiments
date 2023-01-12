package com.mygame.Screens

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g3d.Environment
import com.badlogic.gdx.graphics.g3d.Material
import com.badlogic.gdx.graphics.g3d.ModelBatch
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider
import com.badlogic.gdx.graphics.g3d.utils.FirstPersonCameraController
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
import com.badlogic.gdx.physics.bullet.dynamics.*
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody.btRigidBodyConstructionInfo
import com.badlogic.gdx.physics.bullet.linearmath.btIDebugDraw
import com.badlogic.gdx.physics.bullet.softbody.btSoftRigidDynamicsWorld
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.FitViewport
import com.kotcrab.vis.ui.VisUI
import com.kotcrab.vis.ui.widget.VisLabel
import com.mygame.MotionState
import ktx.assets.disposeSafely
import ktx.math.times


open class BaseScreen(var game:Game):ScreenAdapter() {
    //gltf and 3d
    protected var camera: PerspectiveCamera
    protected var cameraController: FirstPersonCameraController
    protected var modelBatch: ModelBatch
    protected var shadowBatch: ModelBatch
    protected var renderInstances: Array<ModelInstance>
    protected var environment: Environment
    protected var shadowLight: DirectionalShadowLight? = null
    private val colors: Array<Color>
    private val stage: Stage
    private val fpsLabel: VisLabel
    //bullet3d
    var broadphase: btBroadphaseInterface?=null
    var dynamicsWorld: btSoftRigidDynamicsWorld? =null
    var constraintSolver: btConstraintSolver?=null
    var collisionConfig: btCollisionConfiguration?=null
    var dispatcher: btDispatcher?=null
    //the floor's body
    var floorBody:btRigidBody?=null
    //lateinit var contactListener: MyContactListener
    //debug drawer
    lateinit var debugDrawer: DebugDrawer

    val GRID_MIN = -100f
    val GRID_MAX = 100f
    val GRID_STEP = 10f

    init {
        VisUI.load()

        //setting up the camera
        camera = PerspectiveCamera(
            60f, Gdx.graphics.width.toFloat(),
            Gdx.graphics.height.toFloat()
        )
        camera.near = 1f
        camera.far = 500f
        camera.position[0f, 10f] = 50f
        environment = Environment()
        environment.set(ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f))
        environment.add(DirectionalShadowLight(2048, 2048, 30f, 30f, 1f, 100f).also {
            shadowLight = it
        }.set(0.8f, 0.8f, 0.8f, -.4f, -.4f, -.4f))
        environment.shadowMap = shadowLight

        //setting up the stage
        stage = Stage(
            FitViewport(
                Gdx.graphics.width.toFloat(),
                Gdx.graphics.height.toFloat()
            )
        )
        //initializing ome labels
        fpsLabel = VisLabel()
        fpsLabel.setPosition(10f, 10f)
        stage.addActor(fpsLabel)
        //batch stuff
        modelBatch = ModelBatch()
        shadowBatch = ModelBatch(DepthShaderProvider())
        renderInstances = Array()
        //setting up the camera controller
        cameraController=FirstPersonCameraController(camera)
        Gdx.input.inputProcessor = cameraController
        colors = Array()
        colors.add(Color.PURPLE)
        colors.add(Color.BLUE)
        colors.add(Color.TEAL)
        colors.add(Color.BROWN)
        colors.add(Color.FIREBRICK)

        createAxes()
    }

    override fun render(delta: Float) {
        cameraController.update()
        ScreenUtils.clear(Color.BLACK, true)
        shadowLight!!.begin(Vector3.Zero, camera.direction)
        shadowBatch.begin(shadowLight!!.camera)
        shadowBatch.render(renderInstances)
        shadowBatch.end()
        shadowLight!!.end()
        modelBatch.begin(camera)
        modelBatch.render(renderInstances, environment)
        modelBatch.end()

        //setting up the debug renderer

        stage.act()
        stage.draw()
        fpsLabel.setText("FPS: " + Gdx.graphics.framesPerSecond)
    }

    protected fun createFloor(width: Float, height: Float, depth: Float) {
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
        BoxShapeBuilder.build(meshBuilder, width, height, depth)
        val btBoxShape = btBoxShape(Vector3(width / 2f, height / 2f, depth / 2f))
        val floor = modelBuilder.end()
        val floorInstance = ModelInstance(floor)
        floorInstance.transform.trn(0f, -0.5f, 0f)

        //setting a rigid body to the floor
        val info = btRigidBodyConstructionInfo(0f, null, btBoxShape, Vector3.Zero)
        val body = btRigidBody(info)
        body.worldTransform = floorInstance.transform
        floorBody=body
        renderInstances.add(floorInstance)
        dynamicsWorld?.addRigidBody(body)
    }



    fun createAxes() {
        //you can search it in the libgdx test
        val modelBuilder = ModelBuilder()
        modelBuilder.begin()
        var builder = modelBuilder.part(
            "grid",
            GL20.GL_LINES,
            (VertexAttributes.Usage.Position or VertexAttributes.Usage.ColorUnpacked).toLong(),
            Material()
        )
        builder.setColor(Color.LIGHT_GRAY)
        var t = GRID_MIN
        while (t <= GRID_MAX) {
            builder.line(t, 0f, GRID_MIN, t, 0f, GRID_MAX)
            builder.line(GRID_MIN, 0f, t, GRID_MAX, 0f, t)
            t += GRID_STEP
        }
        builder = modelBuilder.part(
            "axes",
            GL20.GL_LINES,
            (VertexAttributes.Usage.Position or VertexAttributes.Usage.ColorUnpacked).toLong(),
            Material()
        )
        builder.setColor(Color.RED)
        builder.line(0f, .1f, 0f, 100f, 0f, 0f)
        builder.setColor(Color.GREEN)
        builder.line(0f, .1f, 0f, 0f, 100f, 0f)
        builder.setColor(Color.BLUE)
        builder.line(0f, .1f, 0f, 0f, 0f, 100f)
        val axesModel = modelBuilder.end()
        val axesInstance = ModelInstance(axesModel)
        renderInstances.add(axesInstance)
    }
    fun initializeBullet() {
        collisionConfig = btDefaultCollisionConfiguration()
        dispatcher = btCollisionDispatcher(collisionConfig)
        broadphase = btDbvtBroadphase()
        //setting the dynamic world
        constraintSolver = btSequentialImpulseConstraintSolver()
        dynamicsWorld =
            btSoftRigidDynamicsWorld(dispatcher, broadphase, constraintSolver, collisionConfig)
        dynamicsWorld!!.gravity = Vector3(0f, -9.81f, 0f)

        //contactListener = MyContactListener()

        debugDrawer = DebugDrawer().apply {
            debugMode = btIDebugDraw.DebugDrawModes.DBG_DrawWireframe
        }
        (dynamicsWorld as btDiscreteDynamicsWorld).debugDrawer = debugDrawer

    }
    fun createCube(x:Float, y:Float, z:Float, width: Float, height: Float, depth: Float, isDynamic:Boolean):btRigidBody{
        val modelBuilder = ModelBuilder()
        modelBuilder.begin()
        val meshBuilder = modelBuilder.part(
            "cube",
            GL20.GL_TRIANGLES,
            (VertexAttribute.Position().usage or VertexAttribute.Normal().usage or VertexAttribute.TexCoords(
                0
            ).usage).toLong(),
            Material()
        )
        BoxShapeBuilder.build(meshBuilder, width, height, depth)
        val btBoxShape = btBoxShape(Vector3(width/2, height/2, depth/2))
        val cube = modelBuilder.end()
        val cubeInstance = ModelInstance(cube)
        cubeInstance.transform.trn(x, y, z)

        //setting a rigid body to the floor
        var info: btRigidBodyConstructionInfo
        info = if(isDynamic){
            var localInertia=Vector3()
            btBoxShape.calculateLocalInertia(1f, localInertia)
            btRigidBodyConstructionInfo(1f, null, btBoxShape, localInertia)
        }else{
            btRigidBodyConstructionInfo(0f, null, btBoxShape, Vector3.Zero)
        }

        val body = btRigidBody(info)
        var motionState=MotionState(cubeInstance.transform)
        body.motionState=motionState
        floorBody=body
        renderInstances.add(cubeInstance)
        dynamicsWorld?.addRigidBody(body)
        return body
    }
    fun shoot(){
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

        //body.collisionFlags=body.collisionFlags or btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK

        renderInstances.add(rocket)
        dynamicsWorld?.addRigidBody(body)

        body!!.applyCentralForce(pickRay.direction*100f)

    }

    protected val randomColor: Color
        protected get() = colors[MathUtils.random(0, colors.size - 1)]

    companion object {
        private var drawDebug = false
    }

    override fun dispose() {
        modelBatch.disposeSafely()
        shadowBatch.disposeSafely()
    }
}