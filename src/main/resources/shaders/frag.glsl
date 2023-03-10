#version 330
out vec4 fragColor;

in vec2 vertexTexCoord;

uniform sampler3D world;

uniform ivec2 iResolution;
uniform ivec3 worldSize;

uniform vec3 cameraOrigin;
uniform vec3 cameraRotation;

//////////////////// Constants ////////////////////

// Do Jittered Anti-Aliasing (random)
// NOTE: Has a noticeable performance impact
// #define DO_ANTIALIASING
#undef DO_ANTIALIASING

// Number of random samples for jittered anti-aliasing
const int ANTIALIASING_SAMPLES = 2;



// Position of the light source
const vec3 LIGHT_POSITION = vec3(-100, 200, 8);

// Do specular highlights
#define DO_SPECULAR_HIGHLIGHTS
//#undef DO_SPECULAR_HIGHLIGHTS

// Shade of the shadow
const float SHADOW_SHADE = 0.03;

// Specular highlights
const float PHONG_EXPONENT = 100;
const float SPECULAR_STRENGTH_MULTIPLIER = 2.;



// Fresnel calculations
const float REFRACTIVE_INDEX_AIR =  1.00029;
const float REFRACTIVE_INDEX_OBJECT = 1.125;
const float OBJECT_REFLECTIVITY = 0.45;

// Do reflections
#define DO_REFLECTIONS
//#undef DO_REFLECTIONS

// Do glossy reflections
// NOTE: Has a noticeable performance impact
#define DO_GLOSS
//#undef DO_GLOSS

// Glossiness factor
const float OBJECT_GLOSSINESS = 0.04f;
const int GLOSS_SAMPLES = 2;



// Convert output color (RGB) to sRGB
#define CONVERT_TO_SRGB
//#undef CONVERT_TO_SRGB

///////////////////////////////////////////////////



float saturate(float value) {
    return clamp(value, 0.0, 1.0);
}

float frand(vec2 seed){
    return fract(sin(dot(seed, vec2(12.9898, 78.233))) * 43758.5453);
}

vec2 v2rand(vec2 seed) {
    return vec2(frand(seed.xy), frand(seed.yx + 1.));
}

vec3 v3rand(vec3 seed) {
    return vec3(v2rand(seed.xy), frand(seed.yz + 1.));
}



vec3 getNormalOfClosestHitCoordinatePlane(vec3 rayDirection) {
    vec3 directionAbs = abs(rayDirection);

    float maxComp = max(directionAbs.x, max(directionAbs.y, directionAbs.z));
    vec3 directionSign = sign(rayDirection);
    vec3 normal = directionSign * (1.0 - sign(maxComp - directionAbs));
    return normal;
}

vec4 getSkyboxBackground(vec3 rayDirection) {
    vec3 normal = getNormalOfClosestHitCoordinatePlane(rayDirection);
    float value = dot(rayDirection, normal);
    vec3 colorCorner = vec3(0.0);
    vec3 colorCenter = vec3(0.15, 0.15, 0.3);
    return vec4(mix(colorCorner, colorCenter, pow(value, 4)), 1.);
}

vec4 getBackground(vec3 rayDirection) {
    return getSkyboxBackground(rayDirection);
}



bool isInWorldBounds(vec3 coord) {
    return coord.x >= 0. && coord.x < worldSize.x &&
           coord.y >= 0. && coord.y < worldSize.y &&
           coord.z >= 0. && coord.z < worldSize.z;
}

vec3 mapToTexCoord(vec3 worldCoord) {
    // Derived from rescale:
    // worldCoord                     = coord * worldSize - 0.5
    // worldCoord + 0.5               = coord * worldSize
    // (worldCoord + 0.5) / worldSize = coord
    return (worldCoord + 0.5f) / worldSize;
}

bool sampleWorld(vec3 worldCoord, out vec4 worldData) {
    if (!isInWorldBounds(worldCoord))
        return false;

    vec3 texCoord = mapToTexCoord(worldCoord);
    worldData = texture(world, texCoord);
    return true;
}



bool intersectsWorld(vec3 rayOrigin, vec3 rayDirectionInv, out float tNear, out float tFar, out int outerSideNormalAxis) {
    vec3 tMin = -rayOrigin              * rayDirectionInv;
    vec3 tMax = (worldSize - rayOrigin) * rayDirectionInv;
    vec3 t1 = min(tMin, tMax);
    vec3 t2 = max(tMin, tMax);

    tNear = max(max(t1.x, t1.y), t1.z);
    tFar = min(min(t2.x, t2.y), t2.z);

    outerSideNormalAxis = 2;
    if (tNear == t1.x) outerSideNormalAxis = 0;
    if (tNear == t1.y) outerSideNormalAxis = 1;

    return tFar >= 0 && tFar >= tNear;
}

vec3 calculateNormal(int normalAxis, vec3 step) {
    if (normalAxis == 0) {
        return vec3(-step.x, 0., 0.);
    }
    if (normalAxis == 1) {
        return vec3(0., -step.y, 0.);
    }
    return vec3(0., 0., -step.z);
}

// Implementation based on "A Fast Voxel Traversal Algorithm for Ray Tracing"
// by John Amanatides and Andrew Woo.
// Paper: http://www.cse.yorku.ca/~amana/research/grid.pdf
bool traverseWorld(vec3 rayOrigin, vec3 rayDirection, float tNear, float tFar, out vec4 color, int outerSideNormalAxis, out vec3 normal, out float distance) {
    // Increase tNear by small amount to start *inside* the world
    // If we are inside the bounding box of the world, start at the ray origin
    tNear = max(0, tNear) + 0.001;
    vec3 rayStart = rayOrigin + tNear * rayDirection;
    vec3 currentVoxel = floor(rayStart);

    vec3 step = vec3(
        rayDirection.x >= 0.0 ? 1.0 : -1.0,
        rayDirection.y >= 0.0 ? 1.0 : -1.0,
        rayDirection.z >= 0.0 ? 1.0 : -1.0
    );

    vec3 distanceToNext = vec3(
        step.x > 0 ? (currentVoxel.x + 1.) - rayStart.x : rayStart.x - currentVoxel.x,
        step.y > 0 ? (currentVoxel.y + 1.) - rayStart.y : rayStart.y - currentVoxel.y,
        step.z > 0 ? (currentVoxel.z + 1.) - rayStart.z : rayStart.z - currentVoxel.z
    );

    // How far along the ray we must travel for such movement to equal the cell size (1)
    vec3 stepDelta = abs(vec3(
        rayDirection.x != 0.0 ? 1. / rayDirection.x : tFar,
        rayDirection.y != 0.0 ? 1. / rayDirection.y : tFar,
        rayDirection.z != 0.0 ? 1. / rayDirection.z : tFar
    ));

    // How far along the ray we must travel to cross the x, y or z grid line
    vec3 nearestVoxelBoundary = vec3(
        rayDirection.x != 0.0 ? stepDelta.x * distanceToNext.x : tFar,
        rayDirection.y != 0.0 ? stepDelta.y * distanceToNext.y : tFar,
        rayDirection.z != 0.0 ? stepDelta.z * distanceToNext.z : tFar
    );

    distance = 0.0;

    int normalAxis = outerSideNormalAxis;
    while (isInWorldBounds(currentVoxel)) {
        // Sample world and return if data.z == 0
        vec4 voxelData;
        if (sampleWorld(currentVoxel, voxelData)) {
            if (voxelData.w != 0) {
                distance = tNear + distance;
                color = voxelData;
                normal = calculateNormal(normalAxis, step);
                return true;
            }
        }

        if (nearestVoxelBoundary.x < nearestVoxelBoundary.y && nearestVoxelBoundary.x < nearestVoxelBoundary.z) {
            distance = nearestVoxelBoundary.x;

            // X-axis traversal
            currentVoxel.x += step.x;
            nearestVoxelBoundary.x += stepDelta.x;
            normalAxis = 0;
        } else if (nearestVoxelBoundary.y < nearestVoxelBoundary.z) {
            distance = nearestVoxelBoundary.y;

            // Y-axis traversal
            currentVoxel.y += step.y;
            nearestVoxelBoundary.y += stepDelta.y;
            normalAxis = 1;
        } else {
            distance = nearestVoxelBoundary.z;

            // Z-axis traversal
            currentVoxel.z += step.z;
            nearestVoxelBoundary.z += stepDelta.z;
            normalAxis = 2;
        }
    }

    return false;
}

bool intersectWorld(vec3 rayOrigin, vec3 rayDirection, out vec4 voxelData, out float distance, out vec3 normal) {
    float tNear, tFar;
    int outerSideNormalAxis;
    if (!intersectsWorld(rayOrigin, 1. / rayDirection, tNear, tFar, outerSideNormalAxis)) {
        return false;
    }

    return traverseWorld(rayOrigin, rayDirection, tNear, tFar, voxelData, outerSideNormalAxis, normal, distance);
}



vec4 calculateDiffuseMultiplier(vec3 lightVector, vec3 normal) {
    float lightInclination = dot(normal, lightVector);

    float shadeFactor = max(SHADOW_SHADE, lightInclination);
    return vec4(shadeFactor, shadeFactor, shadeFactor, 1.);
}

vec4 calculateSpecularMultiplier(vec3 lightVector, vec3 normal, vec3 rayDirection) {
    vec3 reflectedDirection = reflect(rayDirection, normal);
    float baseIntensity = saturate(dot(reflectedDirection, lightVector));
    float intensity = pow(baseIntensity, PHONG_EXPONENT);
    return vec4(intensity, intensity, intensity, 1.);
}

vec4 calculateShadingMultiplier(vec3 hitPoint, vec3 normal, vec3 rayDirection) {
    vec3 lightVector = normalize(LIGHT_POSITION - hitPoint);

    vec4 voxelData2;
    float distance2;
    vec3 normal2;
    bool hasIntersection2 = intersectWorld(hitPoint, lightVector, voxelData2, distance2, normal2);
    if (hasIntersection2) {
        return vec4(SHADOW_SHADE, SHADOW_SHADE, SHADOW_SHADE, 1.);
    }

    #ifdef DO_SPECULAR_HIGHLIGHTS
    vec4 specular = calculateSpecularMultiplier(lightVector, normal, rayDirection);
    vec4 diffuse = calculateDiffuseMultiplier(lightVector, normal);
    return diffuse + specular * SPECULAR_STRENGTH_MULTIPLIER;
    #else
    return calculateDiffuseMultiplier(lightVector, normal);
    #endif
}

float calculateFresnelReflectAmount(vec3 incident, vec3 normal, float refractiveIndexEnter, float refractiveIndexLeave) {
    // Schlick approximation
    float r0 = (refractiveIndexEnter - refractiveIndexLeave) / (refractiveIndexEnter + refractiveIndexLeave);
    r0 *= r0;
    float cosX = -dot(normal, incident);

    if (refractiveIndexEnter > refractiveIndexLeave) {
        float n = refractiveIndexEnter / refractiveIndexLeave;
        float sinT2 = n * n * (1.0 - cosX * cosX);

        // Total internal reflection
        if (sinT2 > 1.0)
            return 1.0;
        cosX = sqrt(1.0 - sinT2);
    }

    float invCosX = 1.0 - cosX;
    float reflectAmount = r0 + (1.0 - r0) * invCosX * invCosX * invCosX * invCosX * invCosX;

    // Adjust reflect multiplier for object reflectivity
    reflectAmount = OBJECT_REFLECTIVITY + (1.0 - OBJECT_REFLECTIVITY) * reflectAmount;
    return reflectAmount;
}

vec4 calculateReflectedRayColor(vec3 hitPoint, vec3 reflectedDirection) {
    vec4 reflectedVoxelData;
    float reflectedDistance;
    vec3 reflectedNormal;
    bool hasIntersection = intersectWorld(hitPoint, reflectedDirection, reflectedVoxelData, reflectedDistance, reflectedNormal);
    if (!hasIntersection) {
        return getBackground(reflectedDirection);
    }

    vec3 reflectHitPoint = hitPoint + reflectedDirection * (reflectedDistance - 0.001);
    return reflectedVoxelData * calculateShadingMultiplier(reflectHitPoint, reflectedNormal, reflectedDirection);
}

vec4 calculateReflectionColor(vec3 rayDirection, vec3 hitPoint, vec3 hitNormal) {
    #ifdef DO_GLOSS
        // Sample reflections from the same starting point with randomly offset direction vectors
        vec4 reflectionSampleSum = vec4(0.0);
        vec3 reflectedDirection = reflect(rayDirection, hitNormal);
        for (int i = 0; i < GLOSS_SAMPLES; ++i) {
            vec3 offset = v3rand(hitPoint);
            vec3 reflectedDirection = reflectedDirection + offset * OBJECT_GLOSSINESS;
            reflectionSampleSum += calculateReflectedRayColor(hitPoint, reflectedDirection);
        }
        return reflectionSampleSum / GLOSS_SAMPLES;
    #else
        vec3 reflectedDirection = reflect(rayDirection, hitNormal);
        return calculateReflectedRayColor(hitPoint, reflectedDirection);
    #endif
}

vec4 calculateColor(vec4 voxelData, vec3 rayDirection, vec3 hitPoint, vec3 hitNormal) {
    // Calculate shading
    vec4 shadingMultiplier = calculateShadingMultiplier(hitPoint, hitNormal, rayDirection);

    #ifdef DO_REFLECTIONS
        vec4 reflectionSample = calculateReflectionColor(rayDirection, hitPoint, hitNormal);
        float reflectAmount = calculateFresnelReflectAmount(rayDirection, hitNormal, REFRACTIVE_INDEX_AIR, REFRACTIVE_INDEX_OBJECT);

        return shadingMultiplier * mix(voxelData, reflectionSample, reflectAmount);
    #else
        return shadingMultiplier * voxelData;
    #endif
}

vec4 castRay(vec3 rayOrigin, vec3 rayDirection) {
    vec4 voxelData;
    float distance;
    vec3 normal;
    bool hasIntersection = intersectWorld(rayOrigin, rayDirection, voxelData, distance, normal);
    if (!hasIntersection) {
        return getBackground(rayDirection);
    }

    vec3 hitPoint = rayOrigin + rayDirection * (distance - 0.001);
    return calculateColor(voxelData, rayDirection, hitPoint, normal);
}



vec3 lessThan(vec3 f, float value) {
    return vec3(
        f.x < value ? 1.0f : 0.0f,
        f.y < value ? 1.0f : 0.0f,
        f.z < value ? 1.0f : 0.0f
    );
}

vec3 linearToSRGB(vec3 rgb) {
    rgb = clamp(rgb, 0.0f, 1.0f);

    return mix(
        pow(rgb, vec3(1.0f / 2.4f)) * 1.055f - 0.055f,
        rgb * 12.92f,
        lessThan(rgb, 0.0031308f)
    );
}

mat3 pitchYawMatrix(float pitch, float yaw) {
    mat3 yawMatrix = mat3(
        cos(yaw), 0, sin(yaw),
        0, 1, 0,
        -sin(yaw), 0, cos(yaw)
    );
    mat3 pitchMatrix = mat3(
        1, 0, 0,
        0, cos(pitch), -sin(pitch),
        0, sin(pitch), cos(pitch)
    );
    return yawMatrix * pitchMatrix;
}

vec2 getUV(vec2 texCoord) {
    vec2 uv = texCoord;
    uv = uv * 2.0 - 1.0; // Transform from [0,1] to [-1,1];
    uv.x *= float(iResolution.x) / iResolution.y; // Aspect fix
    uv *= 0.45; // FOV
    return uv;
}

vec4 performRaytrace(vec2 rayStartOnScreen) {
    float pitch = cameraRotation.x;
    float yaw = cameraRotation.y;
    vec3 direction = pitchYawMatrix(pitch, yaw) * normalize(vec3(rayStartOnScreen, 1.));

    vec4 color = castRay(cameraOrigin, direction);
    #ifdef CONVERT_TO_SRGB
        color = vec4(linearToSRGB(color.xyz), color.w);
    #endif
    return color;
}



void doNormalRaytrace() {
    vec2 uv = getUV(vertexTexCoord);
    fragColor = performRaytrace(uv);
}

void doRaytraceWithAntialiasing() {
    vec4 totalColor = vec4(0);
    vec2 uv = getUV(vertexTexCoord);
    for (int i = 0; i < ANTIALIASING_SAMPLES; ++i) {
        vec2 jitteredUV = uv + v2rand(vertexTexCoord * (i + 1)) * 0.0007;
        totalColor += performRaytrace(jitteredUV);
    }
    fragColor = totalColor / ANTIALIASING_SAMPLES;
}

void main() {
    #ifdef DO_ANTIALIASING
        doRaytraceWithAntialiasing();
    #else
        doNormalRaytrace();
    #endif
}
