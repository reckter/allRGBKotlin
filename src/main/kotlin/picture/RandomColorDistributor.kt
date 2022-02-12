package picture

import BasicPicture
import me.tongfei.progressbar.InteractiveConsoleProgressBarConsumer
import me.tongfei.progressbar.ProgressBar
import me.tongfei.progressbar.ProgressBarBuilder
import me.tongfei.progressbar.ProgressBarConsumer
import java.util.LinkedList
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.sqrt
import kotlin.streams.asStream

class RandomColorDistributor(file: String) : BasicPicture(file) {

    fun randomColors(): Sequence<Triple<Int, Int, Int>> {
        return (0 until COLORS).asSequence().flatMap { r ->
            (0 until COLORS).asSequence().flatMap { g ->
                (0 until COLORS).map { b -> Triple(r, g, b) }
            }
        }.shuffled()
    }

    val availablePositions by lazy {
        (0 until SIZE).map { x ->
            (0 until SIZE).map { y -> true }.toBooleanArray()
        }.toTypedArray()
    }

    val colorRange by lazy {
        (0 until COLORS).map { r ->
            (0 until COLORS).map { g ->
                (0 until COLORS).map { b -> 0 }.toIntArray()
            }.toTypedArray()
        }.toTypedArray()
    }

    val allPositions by lazy {
        (0 until SIZE).asSequence().flatMap { x ->
            (0 until SIZE).asSequence().map { y ->
                x to y
            }
        }
            .map {
                val shouldColor = pixelShould[it.first][it.second]
                it to Triple(
                    shouldColor[R].toInt().mod(COLORS),
                    shouldColor[G].toInt().mod(COLORS),
                    shouldColor[B].toInt().mod(COLORS)
                )
            }
            .let { ConcurrentLinkedQueue(it.toList()) }
//            .toMutableList()
    }

    fun Int.mod(m: Int): Int {
        return if (this < 0) (this % m) + m else (this % m)
    }

    override fun render() {

        val size = SIZE * SIZE
        val threads = 8

        val positions = allPositions
            .shuffled()
            .chunked(size / threads)
            .asSequence()

        randomColors()
            .chunked(size / threads)
            .zip(positions)
            .mapIndexed { index, it -> it to index }
            .asStream()
            .unordered()
            .parallel()
            .forEach { (it, index) ->
                val (colors, positions) = it
                val allPositions = LinkedList(positions)
                println("$index started")
                colors
                    .let {
                        ProgressBar.wrap(
                            it,
                            ProgressBarBuilder()
                                .setTaskName("$index")
                                .showSpeed()
                        )
                    }
                    .forEach { color ->
                        val it = allPositions
                            .minByOrNull { (_, shouldColor) ->
                                distanceSquared(color, shouldColor)
                            } ?: error("no  candidate found $color")
                        val (x, y) = it.first

                        allPositions.remove(it)
                        pixel[x][y][R] = color.first.toByte()
                        pixel[x][y][G] = color.second.toByte()
                        pixel[x][y][B] = color.third.toByte()
                    }
            }

//        randomColors()
//            .asStream()
//            .parallel()
//            .progressBar {
//                unitSize = 1
//                initialMax = SIZE * SIZE.toLong()
//                showSpeed = true
//            }
//            .forEach { color ->
//                val it = allPositions
//                    .minByOrNull { (_, shouldColor) ->
//                        distanceSquared( color, shouldColor )
//                    } ?: error("no  candidate found $color")
//                val (x,y) = it.first
//
//                allPositions.remove(it)
//                pixel[x][y][R] = color.first.toByte()
//                pixel[x][y][G] = color.second.toByte()
//                pixel[x][y][B] = color.third.toByte()
//            }
    }

    fun distance(first: Triple<Int, Int, Int>, second: Triple<Int, Int, Int>): Double {
        return sqrt(distanceSquared(first, second))
    }

    fun distanceSquared(first: Triple<Int, Int, Int>, second: Triple<Int, Int, Int>): Double {
        val distanceR = (first.first - second.first).toDouble()
        val distanceG = (first.second - second.second).toDouble()
        val distanceB = (first.third - second.third).toDouble()
        return distanceR * distanceR + distanceG * distanceG + distanceB * distanceB
    }
}
