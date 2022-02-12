import java.util.stream.Collectors
import java.util.stream.Stream

/**
 * Created by reckter on 17.12.2014.
 */
class PictureRandomBiasedSort(file: String) : PictureRandomSort(file) {
    override fun render() {
        randomizePixel()
        Log.info("repainting image")
        for (i in 0 until SIZE) {
            Log.info((i.toFloat() / SIZE.toFloat() * 100f).toString() + "%: " + calculateFittnes())
            for (j in 0 until SIZE) {
                var pixelToSwitch =
                    Stream.iterate(0) { n: Int -> n + 1 }
                        .limit(SIZE.toLong() * SIZE.toLong())
                        .parallel().map { n: Int ->
                            val ret =
                                listOf(
                                    (n.toLong() % SIZE.toLong()).toInt(),
                                    (n.toLong() / SIZE.toLong()).toInt()
                                )
                            ret
                        }.sorted { a, b ->
                            val value: Int = getDifference(b[0], b[1], b[0], b[1]) - getDifference(
                                a[0], a[1], a[0], a[1]
                            )
                            if (value > 0) {
                                return@sorted 1
                            } else if (value == 0) {
                                return@sorted 0
                            } else {
                                return@sorted -1
                            }
                        }.limit(SIZE.toLong())
                        .collect(Collectors.toList())
                Log.info("switching")
                val firsthalf = pixelToSwitch.take(pixelToSwitch.size / 2)
                pixelToSwitch = pixelToSwitch.drop(firsthalf.size)

                firsthalf
                    .zip(pixelToSwitch)
                    .stream()
                    .map { (a, b) -> listOf(a, b).flatten() }
                    .parallel().forEach { point ->
                        if (isChangedBetter(point[0], point[1], point[2], point[3])) {
                            val tmpR: Byte = pixel[point[0]][point[1]][R]
                            val tmpG: Byte = pixel[point[0]][point[1]][G]
                            val tmpB: Byte = pixel[point[0]][point[1]][B]
                            pixel[point[0]][point[1]][R] = pixel[point[2]][point[3]][R]
                            pixel[point[0]][point[1]][G] = pixel[point[2]][point[3]][G]
                            pixel[point[0]][point[1]][B] = pixel[point[2]][point[3]][B]
                            pixel[point[2]][point[3]][R] = tmpR
                            pixel[point[2]][point[3]][G] = tmpG
                            pixel[point[2]][point[3]][B] = tmpB
                        }
                    }
            }
        }
    }
}
