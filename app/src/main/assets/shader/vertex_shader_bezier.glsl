#version 300 es
precision highp float;

layout (location = 0) in vec2 aVertexCoord;
layout (location = 1) in vec2 aTexCoord;

out vec2 textCoord;
out float progress1;

uniform float progress;

//====================================================================================
//java层写的哪个cpu的算法移植过来不太行，网上找了一个js的算法可以用。
//https://github.com/gre/bezier-easing/blob/master/src/index.js

uniform float c1x;
uniform float c1y;
uniform float c2x;
uniform float c2y;

float sampleValues[11];
const float NEWTON_ITERATIONS = 10.;
const float NEWTON_MIN_SLOPE = 0.001;
const float SUBDIVISION_PRECISION = 0.0000001;
const float SUBDIVISION_MAX_ITERATIONS = 10.;


float A(float aA1, float aA2) {
    return 1.0 - 3.0 * aA2 + 3.0 * aA1;
}

float B(float aA1, float aA2) {
    return 3.0 * aA2 - 6.0 * aA1;
}

float C(float aA1) {
    return 3.0 * aA1;
}

float getSlope(float aT, float aA1, float aA2) {
    return 3.0 * A(aA1, aA2)*aT*aT + 2.0 * B(aA1, aA2) * aT + C(aA1);
}

float calcBezier(float aT, float aA1, float aA2) {
    return ((A(aA1, aA2)*aT + B(aA1, aA2))*aT + C(aA1))*aT;
}

float newtonRaphsonIterate(float aX, float aGuessT, float mX1, float mX2) {
    for (float i = 0.; i < NEWTON_ITERATIONS; ++i) {
        float currentSlope = getSlope(aGuessT, mX1, mX2);
        if (currentSlope == 0.0) {
            return aGuessT;
        }
        float currentX = calcBezier(aGuessT, mX1, mX2) - aX;
        aGuessT -= currentX / currentSlope;
    }
    return aGuessT;
}

float binarySubdivide(float aX, float aA, float aB, float mX1, float mX2) {
    float currentX, currentT;

    currentT = aA + (aB - aA) / 2.0;
    currentX = calcBezier(currentT, mX1, mX2) - aX;
    if (currentX > 0.0) {
        aB = currentT;
    } else {
        aA = currentT;
    }

    for(float i=0.; i<SUBDIVISION_MAX_ITERATIONS; ++i) {
        if (abs(currentX)>SUBDIVISION_PRECISION) {
            currentT = aA + (aB - aA) / 2.0;
            currentX = calcBezier(currentT, mX1, mX2) - aX;
            if (currentX > 0.0) {
                aB = currentT;
            } else {
                aA = currentT;
            }
        } else {
            break;
        }
    }

    return currentT;
}

float GetTForX(float aX, float mX1, float mX2, int kSplineTableSize, float kSampleStepSize) {
    float intervalStart = 0.0;
    int lastSample = kSplineTableSize - 1;
    int currentSample = 1;


    while (currentSample != lastSample && sampleValues[currentSample] <= aX ) {
        currentSample++;
        intervalStart += kSampleStepSize;
    }

    --currentSample;

    // Interpolate to provide an initial guess for t
    float dist = (aX - sampleValues[currentSample]) / (sampleValues[currentSample + 1] - sampleValues[currentSample]);

    float guessForT = intervalStart + dist * kSampleStepSize;

    float initialSlope = getSlope(guessForT, mX1, mX2);

    if (initialSlope >= NEWTON_MIN_SLOPE) {
        return newtonRaphsonIterate(aX, guessForT, mX1, mX2);
    } else if (initialSlope == 0.0) {
        return guessForT;
    } else {
        return binarySubdivide(aX, intervalStart, intervalStart + kSampleStepSize, mX1, mX2);
    }
}

float KeySpline(float aX, float mX1, float mY1, float mX2, float mY2) {
    const int kSplineTableSize = 11;
    float kSampleStepSize = 1. / (float(kSplineTableSize) - 1.);

    if (!(0. <= mX1 && mX1 <= 1. && 0. <= mX2 && mX2 <= 1.)) {
        // bezier x values must be in [0, 1] range
        return 0.;
    }
    if (mX1 == mY1 && mX2 == mY2) return aX; // linear

    for (int i = 0; i < kSplineTableSize; ++i) {
        sampleValues[i] = calcBezier(float(i)*kSampleStepSize, mX1, mX2);
    }

    if (aX == 0.) return 0.;
    if (aX == 1.) return 1.;

    return calcBezier(GetTForX(aX, mX1, mX2, kSplineTableSize, kSampleStepSize), mY1, mY2);
}


float getBezierProgress(float pro) {
    return KeySpline(pro, c1x, c1y, c2x, c2y);
}

//====================================================================================


void main() {
    gl_Position = vec4(aVertexCoord,0.0f, 1.0f);  //只传了x y
    textCoord = aTexCoord;
    progress1 = getBezierProgress(progress);
}
