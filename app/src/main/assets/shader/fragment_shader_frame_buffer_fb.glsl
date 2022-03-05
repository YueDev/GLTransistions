#version 300 es

precision mediump float;

in vec2 textCoord;

uniform sampler2D texture0;
uniform sampler2D texture1;

uniform float progress;

out vec4  outColor;

void main() {
//    outColor = vec4(texture(texture0, textCoord).rgb, 0.0f);
    outColor = mix(texture(texture0, textCoord), texture(texture1, textCoord), progress);
}
