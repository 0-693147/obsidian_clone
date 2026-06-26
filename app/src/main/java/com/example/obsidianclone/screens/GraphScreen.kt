package com.example.obsidianclone.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.obsidianclone.Colors
import com.example.obsidianclone.GraphScreenViewModel
import com.example.obsidianclone.GraphScreenViewModel.GraphNodeLight
import com.example.obsidianclone.NoteRoute
import com.example.obsidianclone.R
import kotlin.math.ln
import kotlin.math.sqrt
import kotlin.random.Random


data class Node (
    val id: Int,
    var position: Vector,
    val title: String,
)

data class Vector(
    val x: Double,
    val y: Double,
) {
    companion object {
        fun from(node1: Node, node2: Node)  = node2.position.minus(node1.position)
    }
    operator fun plus(other: Vector) = Vector(x + other.x, y + other.y)
    operator fun minus(other: Vector) = Vector(x - other.x, y - other.y)
    operator fun times(scalar: Double) = Vector(x * scalar, y * scalar)
    operator fun div(scalar: Double) = Vector(x / scalar, y / scalar)
    fun dotProduct(v1: Vector, v2: Vector) = v1.x * v2.x + v1.y * v2.y
    fun angle(v1: Vector, v2: Vector) = this.dotProduct(v1, v2) / v1.magnitude() / v2.magnitude()
    fun magnitude() = sqrt(x * x + y * y)
    fun normalized() = if (magnitude() > 0) this / magnitude() else Vector(0.0, 0.0)
}

fun getForceDistance(node1: Node, node2: Node) : Vector {
    val scalar = 40.0
    return Vector.from(node1, node2) * scalar
}
fun getForceRepelling(node1: Node, node2: Node) : Vector {
    val forceScalar = 200000.0
    val direction = Vector.from(node1, node2).normalized()
    val distance = Vector.from(node1, node2).magnitude().coerceAtLeast(0.00000001)
    if (distance == 0.0) {
        node2.position += node2.position * 0.001
        return Vector(0.0, 0.0)
    }
    val force = direction * forceScalar / (distance * distance) * -1.0
    return force
}

fun getForcePulling(node1: Node, node2: Node) : Vector {
    val forcePulling = 50.0
    val neutralDistance = 200.0
    val distance = Vector.from(node1, node2).magnitude().coerceAtLeast(0.00000001)
    if (distance == 0.0) {
        node2.position += node2.position * 0.001
        return Vector(0.0, 0.0)
    }
    val direction = Vector.from(node1, node2).normalized() * -1.0
    val force = direction * forcePulling * ln((neutralDistance / distance))
    return force
}

fun getForceCenter(node: Node, center: Node) : Vector {
    val forceScalar = 20.0
    val direction = Vector.from(node, center).normalized()
    val distance = Vector.from(node, center).magnitude().coerceAtLeast(0.00000001)
    if (distance > 200) {
        val force = direction * forceScalar
        return force
    } else if (distance > 100){
        val forceScalar = (-1 + distance / 100) * forceScalar
        val force = direction * forceScalar
        return force
    } else {
        return Vector(0.0, 0.0)
    }
}

fun nextPositions(nodes: List<Node>, matrix: Map<Int, GraphNodeLight>, center: Node) : List<Node> {
    nodes.forEach{ node1 ->
        val connections = matrix[node1.id]?.neighbors
        val moveCoefficient = 0.1
        var resultingForce = Vector(0.0, 0.0)
        nodes.forEach{ node2 ->
            val connectedNode = connections?.find{it == node2.id}?: -1
            if (connectedNode > -1) {
                val force = getForcePulling(node1, node2)
                resultingForce += force
            }
            val force = getForceRepelling(node1, node2)
            resultingForce += force
        }
        val centerForceCoefficient = 1.0
        val centerForce = getForceCenter(node1, center) * centerForceCoefficient
        resultingForce += centerForce
        node1.position += resultingForce * moveCoefficient
    }
    return nodes.toList()
}

fun scatterNodes(matrix: Map<Int, GraphNodeLight>, width: Int = 1000, height: Int = 1000) : List<Node> {
    val nodes = matrix.map { (_, noteEntry) ->
        Node(
            id = noteEntry.note.id,
            position = Vector (
                x = Random.nextDouble() * width,
                y = Random.nextDouble() * height,
            ),
            title = noteEntry.note.title
        )
    }
    return nodes
}

@Composable
fun GraphScreen(
    view: GraphScreenViewModel,
    navController: NavController
) {
    LaunchedEffect(true) {
        view.buildAdjacencyMatrix()
    }
    val adjacencyMatrix by view.adjacencyMatrix.collectAsStateWithLifecycle()
    var nodes: List<Node> by remember(adjacencyMatrix) { mutableStateOf(scatterNodes(adjacencyMatrix)) }
    var frame by remember { mutableStateOf(0)}
    var scale = 1/2000.0


    Scaffold(
        bottomBar = {BottomBar(navController)},
        modifier = Modifier
            .fillMaxSize()
            .background(color = Colors.backgroundColor)
            .windowInsetsPadding(WindowInsets.systemBars),
    ) { innerPadding ->

        Box(modifier = Modifier.padding(innerPadding)) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Colors.backgroundColor)
                    .windowInsetsPadding(WindowInsets.systemBars),
            ) {
                val width = constraints.maxWidth.toDouble()
                val height = constraints.maxHeight.toDouble()
                var dimensions = Vector(width, height)
                val center = Node(-1, dimensions / 2.0, title = "centerNode")

                LaunchedEffect(adjacencyMatrix) {
                    println("new loop")
                    while (true) {
                        frame++
                        if (frame % 100 == 0) {
                        }
                        withFrameMillis {
                            nodes = nextPositions(nodes, adjacencyMatrix, center)
                        }

                    }
                }
//            VisualizeForces(nodes, center)

                DisplayEdges(nodes, center, adjacencyMatrix, frame)
                nodes.forEach { node ->
                    NoteNode(
                        navController = navController,
                        node = node,
                        frame = frame,
                        position = node.position,
                    )
                }
            }
        }
    }
}

@Composable
fun NoteNode(
    navController: NavController,
    node: Node,
    position: Vector,
    frame: Int) {
    val x = position.x
    val y = position.y
    val nodeRadius = 8.dp

    Box(
        modifier = Modifier
            .offset { IntOffset(
                x = (x - nodeRadius.toPx() / 2).toInt(),
                y = (y - nodeRadius.toPx() / 2).toInt()
            )}
            .size(nodeRadius)
            .background(color = Colors.textColor, shape = CircleShape)
            .pointerInput(node.id) {
                detectTapGestures(
                    onTap = {
                        navController.navigate(route = NoteRoute(node.id))
                    }
                )
            }
            .pointerInput(node.id) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val delta = Vector(
                        change.position.x.toDouble(),
                        change.position.y.toDouble(),
                    )
                    node.position += delta
                }
            },
        contentAlignment = Alignment.Center
    ){}


    var textWidth by remember { mutableStateOf(0f) }
    Text(
        text = node.title,
        color = Colors.textColor,
        style = MaterialTheme.typography.labelSmall,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .onSizeChanged { size ->
                textWidth = size.width.toFloat()
            }
            .offset { IntOffset(
                x = (x - textWidth / 2).toInt(),
                y = (y + nodeRadius.toPx() / 2).toInt()
            )}
    )
}

@Composable
fun VisualizeForces(nodes: List<Node>, center: Node) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        nodes.forEach { node1 ->
            val pos1 = node1.position
            val m1 = 0.001
            val m2 = 100.0

            // repulsion from all nodes
            nodes.forEach { node2 ->

                if (node1.id != node2.id) {
                    val force = getForceRepelling(node1, node2) * m2
//                            val force = Vector.from(node1, node2)
                    val force2 = Vector.from(node1, node2) * m1
                    val pos = node1.position
                    drawLine(
                        color = Color.Red,
                        start = Offset(
                            pos1.x.toFloat(),
                            pos1.y.toFloat()
                        ),
                        end = Offset(
                            (pos.x + force.x).toFloat(),
                            (pos.y + force.y).toFloat()
                        ),
                        strokeWidth = 2f
                    )
                    drawLine(
                        color = Color.Magenta,
                        start = Offset(
                            pos1.x.toFloat(),
                            pos1.y.toFloat()
                        ),
                        end = Offset(
                            (pos.x + force.x * m1).toFloat(),
                            (pos.y + force.y * m1).toFloat()
                        ),
                        strokeWidth = 2f
                    )
                }
            }

            drawCircle(
                color = Color.Green,
                radius = 200f,
                center = Offset(
                center.position.x.toFloat(),
                center.position.y.toFloat()
                ),
                style = Stroke(1f)
            )

            // pull toward center
            val centerForce = getForceCenter(node1, center) / 10.0
            drawLine(
                color = Color.Green,
                start = Offset(
                    pos1.x.toFloat(),
                    pos1.y.toFloat()
                ),
                end = Offset(
                    (pos1.x + centerForce.x * m2).toFloat(),
                    (pos1.y + centerForce.y * m2).toFloat(),
                ),
                strokeWidth = 2f
            )
        }
    }
}

@Composable
fun DisplayEdges(
    nodes: List<Node>,
    center: Node,
    adjacencyMatrix: Map<Int, GraphNodeLight>,
    frame: Int
    ) {
    key(frame) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val idNodes = nodes.associateBy { it.id }
            nodes.forEach { node1 ->
                val id1 = node1.id
                val graphNode = adjacencyMatrix[id1]
                graphNode?.neighbors?.forEach { id2 ->
                    val node2 = idNodes[id2]
                    node2?.let {
                        val position1 =
                            Offset(x = node1.position.x.toFloat(), y = node1.position.y.toFloat())
                        val position2 =
                            Offset(x = node2.position.x.toFloat(), y = node2.position.y.toFloat())
                        drawLine(
                            color = Color.White,
                            start = position1,
                            end = position2
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun BottomBar(
    navController: NavController,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .background(color = Colors.backgroundColor)
            .padding(18.dp)
            .fillMaxWidth(),
    ) {
        LazyRow(
            modifier = Modifier,
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item() {
                IconButton(
                    onClick = {
                        navController.popBackStack()
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.return_icon),
                        contentDescription = "Search Icon",
                        tint = Colors.textColor
                    )
                }
            }
        }
    }
}
