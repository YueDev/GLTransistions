#version 300 es
precision highp float;

layout (location = 0) in vec2 aVertexCoord;
layout (location = 1) in vec2 aTexCoord;

out vec2 textCoord;

void main() {
    gl_Position = vec4(aVertexCoord,0.0f, 1.0f);  //只传了x y
    textCoord = aTexCoord;
}
