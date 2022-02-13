package me.reckter.allRGB.cords


data class Cord2D<T : Number>(
    val x: T, val y: T
)

operator fun Cord2D<Int>.plus(other: Cord2D<Int>): Cord2D<Int> {
    return Cord2D(
        this.x + other.x, this.y + other.y
    )
}

fun Cord2D<Int>.getNeighbors(range: Int = 1): Sequence<Cord2D<Int>> {
    return (-range..range).flatMap { xOffset ->
        (-range..range).map { yOffset ->
            this + Cord2D(xOffset, yOffset)
        }
    }.filter { it != this }
        .asSequence()
}

fun Cord2D<Int>.manhattenDistance(to: Cord2D<Int>): Int {
    return Math.abs(this.x - to.x) + Math.abs(this.y - to.y)
}
