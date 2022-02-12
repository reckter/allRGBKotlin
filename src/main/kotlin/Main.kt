package me.reckter.allRGB

import picture.RandomColorDistributor

fun main(args: Array<String>) {

    // Try adding program arguments via Run/Debug configuration.
    // Learn more about running applications: https://www.jetbrains.com/help/idea/running-applications.html.
    val gen = RandomColorDistributor("jungle.JPG")
    gen.render()
    gen.writePicture()
}
