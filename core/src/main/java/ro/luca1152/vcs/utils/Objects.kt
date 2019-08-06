package ro.luca1152.vcs.utils

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.kotcrab.vis.ui.widget.VisWindow
import ktx.inject.Context

class UICamera : OrthographicCamera()
class UIViewport(context: Context) : ExtendViewport(895f, 487f, context.inject<UICamera>())
class UIStage(context: Context) : Stage(context.inject<UIViewport>(), context.inject())

open class NonDraggingWindow(title: String) : VisWindow(title) {
    override fun act(delta: Float) {
        super.act(delta)
        dragging = false
    }
}