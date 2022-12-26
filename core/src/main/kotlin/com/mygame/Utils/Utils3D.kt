package com.mygame.utils

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.VertexAttributes
import com.badlogic.gdx.graphics.g3d.Material
import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.CapsuleShapeBuilder
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.ConeShapeBuilder
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.SphereShapeBuilder
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector3
import net.mgsx.gltf.loaders.gltf.GLTFLoader
import net.mgsx.gltf.scene3d.scene.Scene
import net.mgsx.gltf.scene3d.scene.SceneAsset
import net.mgsx.gltf.scene3d.scene.SceneManager


/**
 * @author JamesTKhan
 * @version October 01, 2022
 */
object Utils3D {
    private val tmpVec = Vector3()

    /**
     * Gets the urrent facing direction of transform, assuming the default forward is Z vector.
     *
     * @param transform modelInstance transform
     * @param out out vector to be populated with direction
     */
    fun getDirection(transform: Matrix4?, out: Vector3) {
        tmpVec.set(Vector3.Z)
        out.set(tmpVec.rot(transform).nor())
    }

    /**
     * Gets the world position of modelInstance and sets it on the out vector
     *
     * @param transform modelInstance transform
     * @param out out vector to be populated with position
     */
    fun getPosition(transform: Matrix4, out: Vector3?) {
        transform.getTranslation(out)
    }


    /**
     * Simple load of .obj model
     */
    fun loadOBJ(fileHandle: FileHandle?): Model {
        val loader = ObjLoader()
        return loader.loadModel(fileHandle)
    }
    //next: do the same but with a gltf model
    fun loadGltf(fileHandle: FileHandle, sceneManager: SceneManager):Scene{
        var sceneAsset=GLTFLoader().load(fileHandle)
        var scene=Scene(sceneAsset.scene)
        sceneManager.addScene(scene)
        return scene
    }

    fun buildCapsuleCharacter(): Model {
        val modelBuilder = ModelBuilder()
        modelBuilder.begin()
        val bodyMaterial = Material()
        bodyMaterial.set(ColorAttribute.createDiffuse(Color.YELLOW))
        val armMaterial = Material()
        armMaterial.set(ColorAttribute.createDiffuse(Color.BLUE))

        // Build the cylinder body
        var builder = modelBuilder.part(
            "body",
            GL20.GL_TRIANGLES,
            (VertexAttributes.Usage.Position or VertexAttributes.Usage.Normal).toLong(),
            bodyMaterial
        )
        CapsuleShapeBuilder.build(builder, .5f, 2f, 12)

        // Build the arms
        builder = modelBuilder.part(
            "arms",
            GL20.GL_TRIANGLES,
            (VertexAttributes.Usage.Position or VertexAttributes.Usage.Normal).toLong(),
            armMaterial
        )
        BoxShapeBuilder.build(builder, .5f, 0f, 0f, .25f, 1f, .25f)
        BoxShapeBuilder.build(builder, -.5f, 0f, 0f, .25f, 1f, .25f)

        // Hat
        builder.setVertexTransform(Matrix4().trn(0f, 1f, 0f))
        ConeShapeBuilder.build(builder, .75f, .5f, .75f, 12)

        // Left Eye
        builder.setVertexTransform(Matrix4().trn(-.15f, .5f, .5f))
        SphereShapeBuilder.build(builder, .15f, .15f, .15f, 12, 12)

        // Right Eye
        builder.setVertexTransform(Matrix4().trn(.15f, .5f, .5f))
        SphereShapeBuilder.build(builder, .15f, .15f, .15f, 12, 12)

        // Finish building
        return modelBuilder.end()
    }
}