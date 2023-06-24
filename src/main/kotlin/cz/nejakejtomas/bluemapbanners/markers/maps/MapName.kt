package cz.nejakejtomas.bluemapbanners.markers.maps

class MapName(val name: String, _anchorX: Float, _anchorY: Float, val transparencyColor: Int?) {
    val anchorX = _anchorX.clamp(0f, 1f)
    val anchorY = _anchorY.clamp(0f, 1f)

    companion object {
        private const val defaultAnchorX = 0.5f
        private const val defaultAnchorY = 0.5f

        fun fromName(nameInGame: String): MapName? {
            fun getTransparencyColor(input: String): Int? {
                val num = input.toIntOrNull(16) ?: return null
                if (num > 0x00FFFFFF) return null
                if (num < 0) return null

                return num
            }

            val parts = nameInGame.split("\\")

            // Has to have starting \ and name
            if (parts.size < 2) return null

            // Has to start with \
            if (parts[0].isNotEmpty()) return null
            val name = parts[parts.size - 1]
            if (name.isEmpty()) return null
            val anchorX = if (parts.size < 3) defaultAnchorX else (parts[1].myToFloatOrNull() ?: return null)
            val anchorY = if (parts.size < 4) defaultAnchorY else (parts[2].myToFloatOrNull() ?: return null)


            val transparencyColor = if (parts.size < 5) null
            else getTransparencyColor(parts[3])

            return MapName(name, anchorX, anchorY, transparencyColor)
        }

        private fun String.myToFloatOrNull(): Float? {
            if (contains("/")) {
                val ratio = split("/")
                if (ratio.size != 2) return null
                val a = ratio[0].toFloatOrNull() ?: return null
                val b = ratio[1].toFloatOrNull() ?: return null

                return a / b
            } else {
                return this.toFloatOrNull()
            }
        }
    }
}

private fun Float.clamp(min: Float, max: Float): Float {
    if (this < min) return min
    if (this > max) return max

    return this
}