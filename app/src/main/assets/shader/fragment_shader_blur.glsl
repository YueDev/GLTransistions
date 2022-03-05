#version 300 es

precision highp float;

in vec2 textCoord;

uniform sampler2D texture0;
uniform sampler2D texture1;

in float progress1;

out vec4  outColor;


const float PI = 3.1415926;

float strength2 = 0.6;


vec4 getToColor(vec2 uv){
    return texture(texture1, uv);
}
vec4 getFromColor(vec2 uv){
    return texture(texture0, uv);
}

float Sinusoidal_easeInOut(in float begin, in float change, in float duration, in float time) {
    return -change / 2.0 * (cos(PI * time / duration) - 1.0) + begin;
}

float rand (vec2 co) {
    return fract(sin(dot(co.xy, vec2(12.9898, 78.233))) * 43758.5453);
}

vec3 crossFade(in vec2 uv, in float dissolve) {
    return mix(getFromColor(uv).rgb, getToColor(uv).rgb, dissolve);
}


//type 0 横向 1 纵向 34xy 都移动，3是左上+xy右下-xy， 4是左下+xy右上-xy
float type=1.0;
//单程的时间
float once=0.9;
//分成几个部分
float splitnum=2.0;


//获取相对的 once 的进度
float getusepro(){

    float splitpos=0.0;
    //    type移动方向由另一个位置决定
    if (type==0.0){
        splitpos=textCoord.y*splitnum;
    } else {
        splitpos=textCoord.x*splitnum;
    }
    //    计算出这是第几部分
    splitpos=floor(splitpos);

    //    计算splitpos位置的延迟的启动时间progress
    float delaypos=(1.0-once)*splitpos/(splitnum-1.0);//(1.0-once)

    return (progress1-delaypos)/once;
}

float getspeedpro(float usepro){
    return ((cos((usepro + 1.) * PI) / 2.0) + 0.5);
}





float getresetpro(float pro){
    pro=getspeedpro(pro);

    pro=getspeedpro(pro);
    pro=min(1.0, pro);
    return pro;
}
vec2 resetpos(vec2 texCoord){
    //    超出镜像

    bool isx=type==0.0;
    if (progress1<=0.5){
        if (isx){
            if (texCoord.x<0.0){
                texCoord.x=-texCoord.x;
            }
        } else {
            if (texCoord.y>1.0){
                texCoord.y=2.0-texCoord.y;
            }
        }


    }
    if (progress1>0.5){
        if (isx){
            if (texCoord.x>0.0){
                texCoord.x=1.0-texCoord.x;
            } else if (texCoord.x<0.0){
                texCoord.x=1.0+texCoord.x;
            }
        } else {
            if (texCoord.y>0.0&&texCoord.y<1.0){
                texCoord.y=1.0-texCoord.y;
            } else if (texCoord.y>1.0){
                texCoord.y=texCoord.y-1.0;
            }
        }
    }

    return texCoord;
}


void main() {

    vec2 texCoord = textCoord.xy / vec2(1.0).xy;
    float usepro=getusepro();

    if (usepro<=0.0){
        outColor=getFromColor(texCoord);
        return;
    } else if (usepro>=1.0){
        outColor=getToColor(texCoord);
        return;
    }
    vec2 move=vec2(0.0);
    float domove=0.0;
    domove=getresetpro(usepro);

    if (type==0.0){
        move.x=domove;
        texCoord=texCoord-move;
    } else {
        move.y=domove;
        texCoord=texCoord+move;
    }

    texCoord=resetpos(texCoord);


    if (type==0.0){
        move.x=domove/8.0;
    } else {
        move.y=domove/8.0;
    }
    vec2 center = texCoord+move;
    // Mirrored sinusoidal loop. 0->strength then strength->0
    domove=usepro*1.0;
    if (usepro>0.58){
        domove=max(0.65-usepro, 0.0)*4.0;
    }


    //    float strength = strength2*domove*2.0;
    //0 - 0.6  0.6 - 0
    float strength = strength2 * progress1 / 0.5;
    if (progress1 > 0.5) {
        float p = 1.0 - progress1;
        strength = max((p - 0.2), 0.0) / 0.3 * strength2 * 0.7;
    }

    vec3 color = vec3(0.0);
    float total = 0.0;
    vec2 toCenter = move;


    float offset = rand(texCoord);
    float dissolve = progress1>0.5?1.0:0.0;
    if (strength==0.0){
        outColor=getToColor(texCoord);
        return;
    }
    float num=10.0;
    for (float t = 0.0; t <= num; t++) {
        float percent = (t + offset) / num;
        float weight = 4.0 * (percent - percent * percent);
        color += crossFade(texCoord + toCenter * percent * strength, dissolve) * weight;
        total += weight;
    }


    outColor = vec4(color / total, 1.0);


}



