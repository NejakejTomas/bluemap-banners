# Bluemap banners

![Gradle build](https://github.com/natanfudge/fabric-example-mod-kotlin/workflows/Gradle%20build/badge.svg)

## Usage

### Banners

Place named banner to show it on the map. Banners with custom patterns will show its pattern when zoomed in.

### Maps

Place named name in a item frame **facing upwards**. The name has to be in this format:

The name format should follow the pattern: [\anchorX][\anchorY][\transparencyColor]<\name>

- Every part must start with a backslash ('\\')
- The first part is the optional anchorX value, which can be a floating-point ("0.5") number or a fraction ("1/3"). 0
  means left, 1 means right.
- The second part is the optional anchorY value, which can also be a floating-point number or a fraction. 0 means top, 1
  means bottom.
- The third part is the optional transparencyColor value, which can be a hexadecimal color in RGB format.
- The fourth part is the name.