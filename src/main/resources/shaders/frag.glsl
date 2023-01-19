#version 330
out vec4 fragColor;

in vec2 vertexTexCoord;

uniform sampler3D world;

uniform ivec2 iResolution;
uniform ivec3 worldSize;

uniform vec3 cameraOrigin;
uniform vec3 cameraRotation;

//////////////////// Parameters ////////////////////

// Fresnel calculations
const float REFRACTIVE_INDEX_AIR = 1.00029;
const float REFRACTIVE_INDEX_OBJECT = 1.125;
const float OBJECT_REFLECTIVITY = 0.05;

// Use reflections
const bool DO_REFLECTIONS = true;

// Light
const vec3 LIGHT_POSITION = vec3(-100, 200, 100);

///////////////////////////////////////////////////


vec3 getNormalOfClosestHitCoordinatePlane(vec3 rayDirection) {
    vec3 directionAbs = abs(rayDirection);

    // Elegant conversion to branchless code
    /*vec3 normal = vec3(
        maxComp == directionAbs.x ? directionSign.x : 0,
        maxComp == directionAbs.y ? directionSign.y : 0,
        maxComp == directionAbs.z ? directionSign.z : 0
    );*/
    float maxComp = max(directionAbs.x, max(directionAbs.y, directionAbs.z));
    vec3 directionSign = sign(rayDirection);
    vec3 normal = directionSign * (1.0 - sign(maxComp - directionAbs));
    return normal;
}

vec4 getSkyboxBackground(vec3 rayDirection) {
    vec3 normal = getNormalOfClosestHitCoordinatePlane(rayDirection);
    float value = dot(rayDirection, normal) * 0.5;
    vec3 color = vec3(0.6, 0.6, 1.0);
    return vec4(value * color, 1.);
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

// Implementation based on "A Fast Voxel Traversal Algorithm for Ray Tracing"
// by John Amanatides and Andrew Woo.
// Paper: http://www.cse.yorku.ca/~amana/research/grid.pdf
bool traverseWorld(vec3 rayOrigin, vec3 rayDirection, float tNear, float tFar, out vec4 color, int outerSideNormalAxis, out int normalAxis, out float distance) {
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

    normalAxis = outerSideNormalAxis;
    while (isInWorldBounds(currentVoxel)) {
        // Sample world and return if data.z == 0
        vec4 voxelData;
        if (sampleWorld(currentVoxel, voxelData)) {
            if (voxelData.z != 0) {
                distance = tNear + distance;
                color = voxelData;
                return true;
            }
        }

        distance = min(nearestVoxelBoundary.x, min(nearestVoxelBoundary.y, nearestVoxelBoundary.z));

        if (nearestVoxelBoundary.x < nearestVoxelBoundary.y && nearestVoxelBoundary.x < nearestVoxelBoundary.z) {
            // X-axis traversal
            currentVoxel.x += step.x;
            nearestVoxelBoundary.x += stepDelta.x;
            normalAxis = 0;
        } else if (nearestVoxelBoundary.y < nearestVoxelBoundary.z) {
            // Y-axis traversal
            currentVoxel.y += step.y;
            nearestVoxelBoundary.y += stepDelta.y;
            normalAxis = 1;
        } else {
            // Z-axis traversal
            currentVoxel.z += step.z;
            nearestVoxelBoundary.z += stepDelta.z;
            normalAxis = 2;
        }
    }

    return false;
}

vec3 getNormalFromAxisAndRayDirection(int normalAxis, vec3 rayDirection) {
    if (normalAxis == 0) { // X
        return vec3(-sign(rayDirection.x), 0., 0.);
    }
    if (normalAxis == 1) { // Y
        return vec3(0., -sign(rayDirection.y), 0.);
    }
    if (normalAxis == 2) { // Z
        return vec3(0., 0., -sign(rayDirection.z));
    }
    return vec3(0);
}

bool intersectWorld(vec3 rayOrigin, vec3 rayDirection, out vec4 voxelData, out float distance, out vec3 normal) {
    float tNear, tFar;
    int outerSideNormalAxis;
    if (!intersectsWorld(rayOrigin, 1. / rayDirection, tNear, tFar, outerSideNormalAxis)) {
        return false;
    }

    int normalAxis;
    if (!traverseWorld(rayOrigin, rayDirection, tNear, tFar, voxelData, outerSideNormalAxis, normalAxis, distance)) {
        return false;
    }

    normal = getNormalFromAxisAndRayDirection(normalAxis, rayDirection);
    return true;
}



vec4 calculateDiffuseMultiplier(vec3 lightPos, vec3 hitPoint, vec3 normal) {
    vec3 lightVector = lightPos - hitPoint;
    float lightInclination = dot(normal, normalize(lightVector));

    return lightInclination > 0.0 ? vec4(lightInclination, lightInclination, lightInclination, 1) : vec4(.05, .05, .05, 1.);
}

vec4 calculateShadowOrDiffuseMultiplier(vec3 lightPos, vec3 hitPoint, vec3 normal) {
    vec4 voxelData2;
    float distance2;
    vec3 normal2;
    bool hasIntersection2 = intersectWorld(hitPoint, normalize(lightPos - hitPoint), voxelData2, distance2, normal2);
    if (hasIntersection2) {
        return vec4(.05, .05, .05, 1.);
    }

    return calculateDiffuseMultiplier(lightPos, hitPoint, normal);
}

vec4 calculateShadingMultiplier(vec3 hitPoint, vec3 normal) {
    return calculateShadowOrDiffuseMultiplier(LIGHT_POSITION, hitPoint, normal);
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

vec4 calculateReflectionColor(vec3 rayDirection, vec3 hitPoint, vec3 hitNormal) {
    vec3 reflectDirection = reflect(rayDirection, hitNormal);

    vec4 reflectVoxelData;
    float reflectDistance;
    vec3 reflectNormal;
    bool hasIntersection = intersectWorld(hitPoint, reflectDirection, reflectVoxelData, reflectDistance, reflectNormal);
    if (!hasIntersection) {
        return getBackground(reflectDirection);
    }

    vec3 reflectHitPoint = hitPoint + reflectDistance * reflectDirection;
    return reflectVoxelData * calculateShadingMultiplier(reflectHitPoint, reflectNormal);
}

vec4 calculateColor(vec4 voxelData, vec3 rayDirection, vec3 hitPoint, vec3 hitNormal) {
    // Calculate shading
    vec4 shadingMultiplier = calculateShadingMultiplier(hitPoint, hitNormal);

    if (DO_REFLECTIONS) {
        // Calculate reflections
        vec4 reflectionSample = calculateReflectionColor(rayDirection, hitPoint, hitNormal);
        float reflectAmount = calculateFresnelReflectAmount(rayDirection, hitNormal, REFRACTIVE_INDEX_AIR, REFRACTIVE_INDEX_OBJECT);

        return mix(shadingMultiplier * voxelData, reflectionSample, reflectAmount);
    }

    return shadingMultiplier * voxelData;
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

void main() {
    vec2 uv = vertexTexCoord;
    uv = uv * 2.0 - 1.0; // Transform from [0,1] to [-1,1];
    uv.x *= float(iResolution.x) / iResolution.y; // Aspect fix
    uv *= 0.45; // FOV

    float pitch = cameraRotation.x;
    float yaw = cameraRotation.y;
    vec3 direction = pitchYawMatrix(pitch, yaw) * normalize(vec3(uv, 1));

    fragColor = castRay(cameraOrigin, direction);
}
