package me.reckter.allRGB

import me.reckter.allRGB.cords.Cord2D
import me.reckter.allRGB.cords.Cord3D
import me.reckter.allRGB.cords.getNeighbors
import me.reckter.allRGB.cords.plus
import me.tongfei.progressbar.ProgressBarBuilder
import java.lang.Math.log
import kotlin.math.ln

class Crystals : BasicGeneration() {

    val seenPositions =
        (0 until SIZE).map {
            (0 until SIZE).map { false }
                .toBooleanArray()
        }.toTypedArray()

    val seenColors by lazy {
        (0 until COLORS).map { r ->
            (0 until COLORS).map { g ->
                (0 until COLORS).map { b ->
                    false
                }.toBooleanArray()
            }
                .toTypedArray()
        }
            .toTypedArray()
    }

    fun colorSequence(color: Cord3D<Int>): Sequence<Cord3D<Int>> {
        return generateSequence(1) { it + 1 }.flatMap { range ->
            (0..range).flatMap { x ->
                (0..range - x).asSequence().flatMap { y ->
                    (0..range - x - y).asSequence().flatMap { z ->
                        listOf(
                            color + Cord3D(x, y, z),
                            color + Cord3D(x, y, -z),
                            color + Cord3D(x, -y, z),
                            color + Cord3D(x, -y, -z),

                            color + Cord3D(-x, y, z),
                            color + Cord3D(-x, y, -z),
                            color + Cord3D(-x, -y, z),
                            color + Cord3D(-x, -y, -z),
                        ).distinct()
                    }
                }
            }
                .shuffled()
        }
    }

    fun randomStartingPosition(): Pair<Cord2D<Int>, Cord3D<Int>> {
        return Cord2D(random.nextInt(SIZE), random.nextInt(SIZE)) to
                Cord3D(
                    random.nextInt(COLORS),
                    random.nextInt(COLORS),
                    random.nextInt(COLORS),
                )
    }

    fun randomValidStartingPosition(): Sequence<Pair<Cord2D<Int>, Cord3D<Int>>> {
        return generateSequence { randomStartingPosition() }
            .filter { (it) -> !seenPositions[it.x][it.x] }
            .filter { (_, it) -> !seenColors[it.x][it.y][it.z] }
    }

    fun getEmptyPositions(): Sequence<Cord2D<Int>> {
        return (0 until SIZE)
            .asSequence()
            .flatMap { x ->
                (0 until SIZE)
                    .asSequence()
                    .map { y ->
                        Cord2D(x, y)
                    }
            }
            .filter { !seenPositions[it.x][it.y] }
    }

    fun getEmptyColors(): Sequence<Cord3D<Int>> {
        return (0 until COLORS)
            .asSequence()
            .flatMap { r ->
                (0 until COLORS)
                    .asSequence()
                    .flatMap { g ->
                        (0 until COLORS)
                            .asSequence()
                            .map { b ->
                                Cord3D(r, g, b)
                            }
                    }
            }
            .filter { !seenColors[it.x][it.y][it.z] }
    }

    override fun render() {
        val queue = ArrayDeque<Pair<Cord2D<Int>, Cord3D<Int>>>(SIZE * SIZE)
        val walkingRange = 3

        print("setting up...")
        seenPositions.count()
        seenColors.count()
        println("done")

        val normal = 1
        val spawn = 2
        val chanceToSpawn = { size: Double, round: Double ->  25 / (ln(round) *ln(size))}
        val chanceToKill = { size: Double, round: Double -> (ln(size) * ln(round)) / 300 }
        val progressBar = ProgressBarBuilder()
            .setInitialMax(SIZE * SIZE.toLong())
            .build()

        var round = 0

        while (!(progressBar.current >= SIZE * SIZE || getEmptyPositions().count() == 0)) {
            val left = SIZE * SIZE - progressBar.current
            if (left < 2_000_000) {
                getEmptyPositions()
                    .zip(getEmptyColors())
                    .take(1025 - left.toInt() / 2000)
                    .forEach { queue.add(it) }
            } else {
                randomValidStartingPosition()
                    .take(10)
                    .forEach { queue.add(it) }
            }

            round = 0
            while (queue.isNotEmpty()) {
                round++

                val (pos, color) = queue.removeFirst()
                progressBar.step()
                if (round % 100 == 1) {
                    progressBar.extraMessage = "q:${queue.size} ${
                        (chanceToSpawn(
                            queue.size.toDouble(),
                            round.toDouble()
                        ) * 100).toInt()
                    }/${(chanceToKill(queue.size.toDouble(), round.toDouble()) * 100).toInt()}"
                }

                pixel[pos.x][pos.y][R] = color.x.toByte()
                pixel[pos.x][pos.y][G] = color.y.toByte()
                pixel[pos.x][pos.y][B] = color.z.toByte()
                val neighboursToSpawn = when {
                    random.nextDouble() < chanceToSpawn(
                        queue.size.toDouble(),
                        round.toDouble()
                    ) -> spawn
                    random.nextDouble() < chanceToKill(queue.size.toDouble(), round.toDouble()) -> 0
                    else -> normal
                }

                val posNeighbours = pos.getNeighbors(walkingRange)
                    .filter { it.x in (0 until SIZE) }
                    .filter { it.y in (0 until SIZE) }
                    .filter { !seenPositions[it.x][it.y] }
                    .shuffled()
                    .take(neighboursToSpawn)

                val colorNeighbour = colorSequence(color)
                    .filter { it.x in (0 until COLORS) }
                    .filter { it.y in (0 until COLORS) }
                    .filter { it.z in (0 until COLORS) }
                    .filter { !seenColors[it.x][it.y][it.z] }
                    .take(neighboursToSpawn)

                posNeighbours
                    .zip(colorNeighbour)
                    .forEach { (pos, color) ->
                        seenPositions[pos.x][pos.y] = true
                        seenColors[color.x][color.y][color.z] = true
                        queue.add(pos to color)

                    }
            }
        }
    }
}
