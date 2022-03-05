#version 300 es

precision mediump float;

in vec2 textCoord;

uniform sampler2D texture0;

out vec4 outColor;

void main() {
//    vec4 fragColor = texture(texture0, textCoord);
//    float average = 0.2126 * fragColor.r + 0.7152 * fragColor.g + 0.0722 * fragColor.b;
//    outColor = vec4(average, average, average, fragColor.a);
    outColor = texture(texture0, textCoord);
}
