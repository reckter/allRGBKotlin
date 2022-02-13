package me.reckter.allRGB.cords

data class Cord3D<T : Number>(
    val x: T, val y: T, val z: T
)

operator fun Cord3D<Int>.plus(other: Cord3D<Int>): Cord3D<Int> {
    return Cord3D(
        this.x + other.x, this.y + other.y, this.z + other.z
    )
}

fun Cord3D<Int>.getNeighbors(range: Int = 1): List<Cord3D<Int>> {
    return (-range..range).flatMap { xOffset ->
        (-range..range).flatMap { yOffset ->
            (-range..range).map { zOffset ->
                this + Cord3D(xOffset, yOffset, zOffset)
            }
        }
    }.filter { it != this }
}

fun Cord3D<Int>.manhattenDistance(to: Cord3D<Int>): Int {
    return Math.abs(this.x - to.x) + Math.abs(this.y - to.y) + Math.abs(this.z - to.z)
}
