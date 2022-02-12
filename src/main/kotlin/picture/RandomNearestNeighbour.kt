package picture

import BasicPicture
import dev.reimer.progressbar.ktx.progressBar
import java.lang.Integer.max
import kotlin.streams.asStream

class RandomNearestNeighbour(file: String) : BasicPicture(file) {

    /**
     * randomizes the pixel. Takes care of allRGB things
     */
    fun randomPositions(): Sequence<Pair<Int, Int>> {
        return (0 until SIZE).asSequence().flatMap { x ->
            (0 until SIZE).asSequence().map { y ->
                x to y
            }
        }.shuffled()
    }

    val availableColors by lazy {
        (0 until COLORS).map { r ->
            (0 until COLORS).map { g ->
                (0 until COLORS).map { b -> true }.toBooleanArray()
            }.toTypedArray()
        }.toTypedArray()
    }

    val colorRange by lazy {
        (0 until COLORS).map { r ->
            (0 until COLORS).map { g ->
                (0 until COLORS).map { b -> 0 }.toIntArray()
            }.toTypedArray()
        }.toTypedArray()
    }

    fun Int.mod(m: Int): Int {
        return if (this < 0) (this % m) + m else (this % m)
    }

    fun findNearestColor(r: Byte, g: Byte, b: Byte): IntArray {
        val pos = Cord3D(r.toInt().mod(COLORS), g.toInt().mod(COLORS), b.toInt().mod(COLORS))
        val startRange =  colorRange[pos.x][pos.y][pos.z]
        val nextAvailable =
            generateSequence(startRange) { it + 1 }.flatMap { range ->
                (0..range).flatMap { x ->
                    (0..range - x).asSequence().flatMap { y ->
                        (0..range - x - y).asSequence().flatMap { z ->
                            listOf(
                                pos + Cord3D(x, y, z),
                                pos + Cord3D(x, y, -z),
                                pos + Cord3D(x, -y, z),
                                pos + Cord3D(x, -y, -z),

                                pos + Cord3D(-x, y, z),
                                pos + Cord3D(-x, y, -z),
                                pos + Cord3D(-x, -y, z),
                                pos + Cord3D(-x, -y, -z),
                            ).distinct()
                        }
                    }
                }
            }
                .filter { it.x in (0 until COLORS) }
                .filter { it.y in (0 until COLORS) }
                .filter { it.z in (0 until COLORS) }
                .filter { availableColors[it.x][it.y][it.z] }
                .first()

        val distance = max(nextAvailable.manhattenDistance(pos) - 1, 0)
        if(distance % 10 == 0) {
            (0 until distance).flatMap { range ->
                (0..range).flatMap { x ->
                    (0..range - x).asSequence().flatMap { y ->
                        (0..range - x - y).asSequence().flatMap { z ->
                            listOf(
                                pos + Cord3D(x, y, z),
                                pos + Cord3D(x, y, -z),
                                pos + Cord3D(x, -y, z),
                                pos + Cord3D(x, -y, -z),

                                pos + Cord3D(-x, y, z),
                                pos + Cord3D(-x, y, -z),
                                pos + Cord3D(-x, -y, z),
                                pos + Cord3D(-x, -y, -z),
                            ).distinct()
                        }
                    }
                }
            }
                .filter { it.x in (0 until COLORS) }
                .filter { it.y in (0 until COLORS) }
                .filter { it.z in (0 until COLORS) }
                .forEach {
                    colorRange[pos.x][pos.y][pos.z] =
                        max(distance - (nextAvailable.manhattenDistance(it)), 0)
                }
        }


        colorRange[pos.x][pos.y][pos.z] = max(nextAvailable.manhattenDistance(pos) - 1, 0)
        availableColors[nextAvailable.x][nextAvailable.y][nextAvailable.z] = false

        return listOf(nextAvailable.x, nextAvailable.y, nextAvailable.z).toIntArray()
    }

    override fun render() {
        randomPositions()
            .asStream()
            .parallel()
            .progressBar {
                initialMax = SIZE * SIZE.toLong()
                unitSize = 1
                showSpeed = true
            }
            .forEach { (x, y) ->
                val shouldColor = pixelShould[x][y]

                val chosenColor = findNearestColor(shouldColor[R], shouldColor[G], shouldColor[B])

                pixel[x][y][R] = chosenColor[R].toByte()
                pixel[x][y][G] = chosenColor[G].toByte()
                pixel[x][y][B] = chosenColor[B].toByte()

            }
    }
}

data class Cord3D<T : Number>(
    val x: T, val y: T, val z: T
)

operator fun Cord3D<Int>.plus(other: Cord3D<Int>): Cord3D<Int> {
    return Cord3D(
        this.x + other.x, this.y + other.y, this.z + other.z
    )
}

fun Cord3D<Int>.getNeighbors(): List<Cord3D<Int>> {
    return (-1..1).flatMap { xOffset ->
        (-1..1).flatMap { yOffset ->
            (-1..1).map { zOffset ->
                this + Cord3D(xOffset, yOffset, zOffset)
            }
        }
    }.filter { it != this }
}

fun Cord3D<Int>.manhattenDistance(to: Cord3D<Int>): Int {
    return Math.abs(this.x - to.x) + Math.abs(this.y - to.y) + Math.abs(this.z - to.z)
}

fun Cord3D<Long>.manhattenDistance(to: Cord3D<Long>): Long {
    return Math.abs(this.x - to.x) + Math.abs(this.y - to.y) + Math.abs(this.z - to.z)
}
