#version 300 es

precision highp float;

in vec2 textCoord;

uniform sampler2D texture0;
uniform sampler2D texture1;

uniform float progress;

out vec4  outColor;
//x 和 y 的 分割数量
uniform vec2 num;

vec4 fade(vec2 uv) {
    return mix(texture(texture0, uv), texture(texture1, uv), progress);
}


//黑白分屏
void main() {

    if(textCoord.y > 0.2 && textCoord.y < 0.8) {
        outColor =  fade(textCoord);
        return;
    }
    //先图片按照中心放大1.7倍，再转换成黑白色
    vec2 uv = (textCoord - 0.5f) / 1.7f + 0.5f;


    vec4 src = fade(uv);
    // 先把彩色转换为黑白色
    float f1 = src.r * 0.3f + src.g * 0.59f + src.b * 0.11f;
    //再用颜色表映射下，给定的颜色表是ffffff-2a2a2a的线性灰度条
    //因此把FF-00映射到FF-2A，即255-0映射到255-42
    float f2 = (f1 * 213.0f + 42.0f) / 255.0f;

    outColor = vec4(f2, f2, f2, src.a);
}


//分屏
//void main() {
//    //纹理如果有mipmap，会导致竖线和横线，没找到解决方法，关了mipmap就没了
//    vec2 uv = mod(textCoord, 1.0 / num);
//
//    if (num.x == num.y) {
//        uv *= num;
//        outColor = fade(uv);
//        return;
//    }
//
//    if (num.x > num.y) {
//        uv *= num.y;
//        uv.x += (1.0 - num.y / num.x) * 0.5;
//    } else {
//        uv *= num.x;
//        uv.y += (1.0 - num.x / num.y) * 0.5;
//    }
//
//    outColor = fade(uv);
//}

////左右镜像
//void main() {
//
//    float x = min(1.0f - textCoord.x, textCoord.x);
//
//    vec2 uv = vec2(x, textCoord.y);
//
//    outColor = fade(uv);
//}

////上下镜像
//
//void main() {
//
//    float y = min(1.0f - textCoord.y, textCoord.y);
//
//    vec2 uv = vec2(textCoord.x, y);
//
//    outColor = fade(uv);
//}




