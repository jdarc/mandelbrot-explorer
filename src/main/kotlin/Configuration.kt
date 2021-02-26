class Configuration(var palette: Palette, var iterations: Int = 256, var point: Vector3 = Vector3(0.0, 0.0, 0.0)) {
    fun reset() {
        point = Vector3(-0.25, 0.0, 1.0)
    }

    init {
        reset()
    }
}
