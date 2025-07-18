package upvictoria.pm_may_ago_2025.iti_271415.lab40jetpackcompose.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt


//Creo que esta es necesaria por que es un figura personalizada
private fun DrawScope.drawRhombus(
    center: Offset,
    size: Float,
    color: Color,
    style: DrawStyle
) {
    val path = Path().apply {
        moveTo(center.x, center.y - size/2)
        lineTo(center.x + size/2, center.y)
        lineTo(center.x, center.y + size/2)
        lineTo(center.x - size/2, center.y)
        close()
    }
    drawPath(
        path = path,
        color = color,
        style = style
    )
}

private fun DrawScope.drawText(
    text: String,
    offset: Offset,
    color: Color,
    textSize: Float
) {
    val paint = android.graphics.Paint().apply {
        this.color = color.toArgb()
        this.textSize = textSize
        this.textAlign = android.graphics.Paint.Align.LEFT
    }
    drawContext.canvas.nativeCanvas.drawText(
        text,
        offset.x,
        offset.y,
        paint
    )
}


/*
* Aqui abajo se encuentra todo lo relacionado al movimiento de las lineas
*
* */

//Un enum es un tipo de dato especial que representa un conjunto fijo de constantes o valores posibles
private enum class Handle {
    //Clase para identificar si el circulo se esta arrastrando

    START, END, NONE
}

private fun distance(from: Offset, to: Offset): Float {
    return sqrt((from.x - to.x).pow(2) + (from.y - to.y).pow(2))
}



/*
* Animación completa
* */
@Composable
fun AnimatedCanvas(
    estado: Int,
    modifier: Modifier = Modifier
) {
    //Estados para la animación del circulo
    var lap by remember { mutableStateOf(0) }
    var extra by remember { mutableStateOf(0) }


    //Estados para la linea interactiva
    var startPoint by remember { mutableStateOf(Offset.Zero) }
    var endPoint by remember { mutableStateOf(Offset.Zero) }
    var draggedHandle by remember { mutableStateOf(Handle.NONE) }
    var lineFlag by remember { mutableStateOf(false) }
    val handleRadius = 27.dp


    //Este es el "motor" de la animación
    LaunchedEffect(Unit) {
        while(true) {
            lap++
            if (lap >= 200) lap = 0
            delay(1000L/60) // Corresponde a FPS = 24 del código original
        }
    }

    //Esto es un lienzo donde tienes el control total de lo que hay en el, no cuenta como una funcion composable
    Canvas(modifier = modifier
        //Esto es un listener de eventos tactiles
        .pointerInput(Unit) {
            val handleRadiusPx = handleRadius.toPx()
            detectDragGestures(
                onDragStart = { touchOffset ->
                    val distToStart = distance(touchOffset, startPoint)
                    val distToEnd = distance(touchOffset, endPoint)
                    draggedHandle = when {
                        distToStart <= handleRadiusPx -> Handle.START
                        distToEnd <= handleRadiusPx -> Handle.END
                        else -> Handle.NONE
                    }
                },onDrag = { change, dragAmount ->
                    val canvasWidth = size.width.toFloat()
                    val canvasHeight = size.height.toFloat()
                    when (draggedHandle) {
                        Handle.START -> {val newPos = startPoint + dragAmount

                            startPoint = Offset(
                                x = newPos.x.coerceIn(0f, canvasWidth),
                                y = newPos.y.coerceIn(0f, canvasHeight)
                            )}
                        Handle.END -> {
                            val newPos = endPoint + dragAmount

                            endPoint = Offset(
                                x = newPos.x.coerceIn(0f, canvasWidth),
                                y = newPos.y.coerceIn(0f, canvasHeight)
                            )
                        }
                        Handle.NONE -> { /* No hacer nada */ }
                    }
                    change.consume()
                },
                onDragEnd = { draggedHandle = Handle.NONE },
            )
        }
    ) {
        val width = size.width
        val height = size.height

        // Configuración inicial exactamente como en Java
        val x = width.toInt()
        val y = height.toInt()
        val l = y/3

        //Revisamos que ya se inicializo el canvas y inicializamos los extremos de la linea solo 1 vez
        if(!lineFlag && size.width > 0){
            startPoint = Offset(1f, (y/3)*2f)
            endPoint = Offset(width, (y/3)*2f)
            lineFlag = true
        }

        // Color de fondo según estado
        drawRect(
            color = if (estado == 0) Color.White else Color.Magenta,
            size = size,
            style = Fill // Cambiado de PaintingStyle.Stroke a Fill
        )

        // Línea base
        val handleColor = Color(255,255,255)

        drawLine(
            color = Color.Black,
            start = startPoint,
            end = endPoint,
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )
        drawCircle(color = handleColor, radius = handleRadius.toPx(), center = startPoint)
        drawCircle(color = Color.Black, radius = handleRadius.toPx(), center = startPoint, style = Stroke(width = 2.dp.toPx()))

        drawCircle(color = handleColor, radius = handleRadius.toPx(), center = endPoint)
        drawCircle(color = Color.Black, radius = handleRadius.toPx(), center = endPoint, style = Stroke(width = 2.dp.toPx()))

        /*
        *
        * Aqui estan los calculos y logica necesarias para animar las lineas verticales y circulos
        *
        * */
        val lineVector = endPoint - startPoint
        val lineLength = distance(startPoint, endPoint)

        // Cálculos estáticos (originales) necesarios para la SEGUNDA fase de la animación
        val radius = width / 10f
        val perpVectorNormalized = if (lineLength > 0) {
            // Para un vector (dx, dy), el perpendicular es (-dy, dx).
            Offset(-lineVector.y / lineLength, lineVector.x / lineLength)
        } else {
            Offset(0f, 1f) // Vector vertical hacia abajo por defecto
        }


        // Animación inicial
        for(i in 0..4) {
            // Se calcula la posición de cada marcador como una fracción a lo largo de la línea interactiva
            val fraction = (2 * i + 1) / 10.0f
            val basePoint = startPoint + (lineVector * fraction)
            val verticalLineHeight = height / 3f
            val verticalLineHalfHeight = height / 7f

            if(lap >= 20 && i <= lap/20) {
                if(lap >= 20 && i <= lap/20) {
                    val lineStart = basePoint - (perpVectorNormalized * verticalLineHalfHeight)
                    val lineEnd = basePoint + (perpVectorNormalized * verticalLineHalfHeight)

                    // Números: se colocan encima de la línea vertical
                    drawText(
                        text = i.toString(),
                        offset = lineStart - (perpVectorNormalized * 40f) - Offset(8f, 12f),
                        color = Color.Blue,
                        textSize = 25f
                    )

                    // Líneas verticales: siempre verticales, pero su base está en la línea interactiva
                    drawLine(
                        color = Color.Blue,
                        start = lineStart,
                        end = lineEnd,
                        strokeWidth = 3f
                    )
                }

            }

            // Círculos

            if (i < 4) {
                val dynamicRadius = lineLength / 10f

                // La posición es en el centro del hueco entre la línea 'i' y la 'i+1'.
                val circleFraction = (2 * i + 2) / 10.0f
                val circleBasePoint = startPoint + (lineVector * circleFraction)

                // El centro del círculo se desplaza para que su borde inferior descanse sobre la línea base.
                val circleCenter = circleBasePoint - (perpVectorNormalized * dynamicRadius)

                if(lap <= 99 && lap >= 20 && i < lap/20) {
                    val circleColor = when {
                        i == (lap/20 - 1) -> Color.Red
                        lap >= 93 -> Color.Blue
                        else -> Color.Black
                    }

                    drawCircle(
                        color = circleColor,
                        radius = dynamicRadius,
                        center = circleCenter,
                        style = Stroke(width = 3f)
                    )
                }
            }

        }

        // Segunda parte de la animación
        if(lap >= 100) {
            val firstRhombusCenter = startPoint + (lineVector * 0.1f)
            // Rombo inicial
            if (lineLength > 0) {
                // Se calcula la posición del rombo a un 10% del inicio de la línea.


                drawRhombus(
                    center = firstRhombusCenter,
                    size = 30f,
                    color = Color.Magenta,
                    style = Fill
                )
            }


            //Valores de la linea roja
            val lineDirection = if (lineLength > 0) {
                lineVector / lineLength
            } else {
                Offset(1f, 0f)
            }

            val factorDeEscala = if (width > 0) lineLength / width else 1f

            val longitudAnimadaBase = (lap - 100) * 6f
            // El límite máximo que la animación PUEDE alcanzar.
            val longitudMaximaBase = (radius * 2) * PI.toFloat()
            val longitudAnimadaEscalada = (longitudAnimadaBase * factorDeEscala)
                .coerceAtMost(longitudMaximaBase * factorDeEscala)

            val redLineEndPoint = firstRhombusCenter + (lineDirection * longitudAnimadaEscalada)


            drawLine(
                color = Color.Red,
                start = firstRhombusCenter, // Comienza donde está el rombo magenta
                end = redLineEndPoint,      // Termina en el punto calculado
                strokeWidth = 5f
            )

            val radioOriginal = width / 10f
            val radioEscalado = radioOriginal * factorDeEscala

            val inicioTrayectoriaRueda = firstRhombusCenter - (perpVectorNormalized * radioEscalado)
            // La posición actual es el punto de partida + la distancia animada en la dirección de la línea.
            val centroDeLaRueda = inicioTrayectoriaRueda + (lineDirection * longitudAnimadaEscalada)

            var gradosDeRotacion = 0f
            val circunferenciaEscalada = (radioEscalado * 2 * PI.toFloat())
            if (circunferenciaEscalada > 0) {
                gradosDeRotacion = (longitudAnimadaEscalada * 360f / circunferenciaEscalada) % 360f
            }

            // Línea roja horizontal
//            drawLine(
//                color = Color.Red,
//                start = Offset(radius.toFloat(), (l*2).toFloat()),
//                end = Offset(px.toFloat(), (l*2).toFloat()),
//                strokeWidth = 5f
//            )

            val px = if ((lap - 100) * 6 <= (radius * 2) * PI.toFloat()) {
                radius + ((lap - 100) * 6)
            } else {
                (radius + ((radius * 2) * PI.toFloat()))
            }

            // Rueda
            val z = radius + ((lap - 100) * 6)

            // Círculo envolvente rojo que se consume
            val path = Path().apply {
                val angulo = 360 - ((px - radius) * 360 / (radius * 2 * PI.toFloat()))
                if (angulo > 0) {
                    moveTo(z + radius.toFloat(), (l * 2 - radius).toFloat())
                    for (i in 0..angulo.toInt()) {
                        val radianes = Math.toRadians(i.toDouble())
                        val sx = z + sin(radianes) * radius
                        val sy = (l * 2 - radius) + cos(radianes) * radius
                        lineTo(sx.toFloat(), sy.toFloat())
                    }
                }
            }
            drawPath(
                path = path,
                color = Color.Red,
                style = Stroke(width = 5f)
            )

            // Rueda verde con rayos
            withTransform({
                rotate(
                    degrees = gradosDeRotacion,
                    pivot = centroDeLaRueda
                )
            }) {
                drawCircle(
                    color = Color.Green,
                    radius = (radioEscalado - (8f * factorDeEscala)), // El grosor del borde también escala
                    center = centroDeLaRueda,
                    style = Stroke(width = (10f * factorDeEscala)) // El ancho de la línea también escala
                )

                // Rayos
                for (i in 0..360 step (360/7)) {
                    val radians = Math.toRadians(i.toDouble())
                    val endX = centroDeLaRueda.x + sin(radians) * (radioEscalado - (8f * factorDeEscala))
                    val endY = centroDeLaRueda.y + cos(radians) * (radioEscalado - (8f * factorDeEscala))

                    drawLine(
                        color = Color.Green,
                        start = centroDeLaRueda,
                        end = Offset(endX.toFloat(), endY.toFloat()),
                        strokeWidth = (10f * factorDeEscala)
                    )
                }
            }

            // Círculos interiores verdes
            drawCircle(
                color = Color.Green,
                radius = (radioEscalado / 2.3f),
                center = centroDeLaRueda,
                style = Stroke(width = (3f * factorDeEscala))
            )

            drawCircle(
                color = Color.Green,
                radius = (radioEscalado / 5f),
                center = centroDeLaRueda,
                style = Stroke(width = (10f * factorDeEscala))
            )

            // Rombo móvil
            drawRhombus(
                center = redLineEndPoint,
                size = 30f * factorDeEscala,
                color = Color.Red,
                style = Fill
            )

            // Resetear la animación si es necesario
            if ((lap - 100) * 6 > longitudMaximaBase) {
                extra++
                if (extra >= 60) {
                    extra = 0
                    lap = 0
                }
            }

        }
    }
}





