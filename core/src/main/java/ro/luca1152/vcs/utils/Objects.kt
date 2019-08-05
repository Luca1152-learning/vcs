package ro.luca1152.vcs.utils

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.ExtendViewport
import ktx.inject.Context

class UICamera : OrthographicCamera()
class UIViewport(context: Context) : ExtendViewport(700f, 700f, context.inject<UICamera>())
class UIStage(context: Context) : Stage(context.inject<UIViewport>(), context.inject())