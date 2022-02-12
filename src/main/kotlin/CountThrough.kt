package me.reckter.allRGB

class CountThrough : BasicGeneration() {
    override fun render() {
        for (x in 0 until SIZE) {
            for (y in 0 until SIZE) {
                pixel[x][y][R] = ((x * SIZE + y) % 256).toByte()
                pixel[x][y][G] = ((x * SIZE + y) / 256 % 256).toByte()
                pixel[x][y][B] = ((x * SIZE + y) / 256 / 256).toByte()
            }
        }
    }
}
