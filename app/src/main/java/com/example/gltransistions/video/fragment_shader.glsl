#version 300 es

precision mediump float;

in vec2 texCoord;

uniform sampler2D hanaTexture;
uniform float texAlpha;

out vec4 outColor;

void main() {
    outColor = texture(hanaTexture, texCoord) * texAlpha;
}