#version 330
out vec4 fragColor;

in vec2 vertexTexCoord;

uniform sampler3D world;

uniform ivec2 iResolution;
uniform ivec3 worldSize;

uniform vec3 cameraOrigin;
uniform vec3 cameraRotation;



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
    float value = dot(rayDirection, normal) * 0.25;
    vec3 color = vec3(0.6, 0.6, 1.0);
    return vec4(value * color, 1.);
}

vec4 getBackground(vec3 rayDirection) {
    return getSkyboxBackground(rayDirection);
}



bool isInWorldBounds(ivec3 coord) {
    return coord.x >= 0 && coord.x < worldSize.x &&
           coord.y >= 0 && coord.y < worldSize.y &&
           coord.z >= 0 && coord.z < worldSize.z;
}

vec3 mapToTexCoord(ivec3 worldCoord) {
    // Derived from rescale:
    // worldCoord                     = coord * worldSize - 0.5
    // worldCoord + 0.5               = coord * worldSize
    // (worldCoord + 0.5) / worldSize = coord
    return (vec3(worldCoord) + 0.5f) / worldSize;
}

bool sampleWorld(ivec3 worldCoord, out vec4 worldData) {
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
    vec3 rayEnd = rayOrigin + tFar * rayDirection;

    ivec3 currentVoxel = ivec3(floor(rayStart));
    ivec3 lastVoxel = ivec3(floor(rayEnd));
    ivec3 step = ivec3(sign(rayDirection));

    vec3 distanceToNext = vec3(
        step.x >= 0 ? (currentVoxel.x + 1.) - rayStart.x : rayStart.x - currentVoxel.x,
        step.y >= 0 ? (currentVoxel.y + 1.) - rayStart.y : rayStart.y - currentVoxel.y,
        step.z >= 0 ? (currentVoxel.z + 1.) - rayStart.z : rayStart.z - currentVoxel.z
    );

    // How far along the ray we must travel to cross the x, y or z grid line
    vec3 nearestVoxelBoundary = vec3(
        rayDirection.x != 0 ? distanceToNext.x / rayDirection.x : tFar,
        rayDirection.y != 0 ? distanceToNext.y / rayDirection.y : tFar,
        rayDirection.z != 0 ? distanceToNext.z / rayDirection.z : tFar
    );

    // How far along the ray we must travel for such movement to equal the cell size (1)
    vec3 stepDistance = vec3(
        rayDirection.x != 0 ? 1. / rayDirection.x : tFar,
        rayDirection.y != 0 ? 1. / rayDirection.y : tFar,
        rayDirection.z != 0 ? 1. / rayDirection.z : tFar
    );

    // TODO: implement smooth distance calculation between voxels
    distance = tNear;
    normalAxis = outerSideNormalAxis;
    while (isInWorldBounds(currentVoxel)) {
        // Sample world and return if data.z == 0
        vec4 voxelData;
        if (sampleWorld(currentVoxel, voxelData)) {
            if (voxelData.z != 0) {
                color = vec4(vec3(1./distance), 1.);
                return true;
            }
        }

        vec3 nearestVoxelBoundaryAbs = abs(nearestVoxelBoundary);
        if (nearestVoxelBoundaryAbs.x < nearestVoxelBoundaryAbs.y && nearestVoxelBoundaryAbs.x < nearestVoxelBoundaryAbs.z) {
            // X-axis traversal
            distance += nearestVoxelBoundaryAbs.x;
            nearestVoxelBoundary.x += stepDistance.x;
            currentVoxel.x += step.x;
            normalAxis = 0;
        } else if (nearestVoxelBoundaryAbs.y < nearestVoxelBoundaryAbs.z) {
            // Y-axis traversal
            distance += nearestVoxelBoundaryAbs.y;
            nearestVoxelBoundary.y += stepDistance.y;
            currentVoxel.y += step.y;
            normalAxis = 1;
        } else {
            // Z-axis traversal
            distance += nearestVoxelBoundaryAbs.z;
            nearestVoxelBoundary.z += stepDistance.z;
            currentVoxel.z += step.z;
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



vec4 calculateDiffuseLight(vec3 lightPos, vec3 hitPoint, vec3 normal) {
    vec3 lightVector = lightPos - hitPoint;
    float lightInclination = dot(normal, normalize(lightVector));

    return lightInclination > 0 ? vec4(lightInclination, lightInclination, lightInclination, 1) : vec4(.05, .05, .05, 1.);
}

vec4 castRay(vec3 rayOrigin, vec3 rayDirection) {
    vec3 lightPos = vec3(-100, 200, -100);

    vec4 voxelData;
    float distance;
    vec3 normal;
    bool hasIntersection = intersectWorld(rayOrigin, rayDirection, voxelData, distance, normal);
    if (!hasIntersection) {
        return getBackground(rayDirection);
    }

    return voxelData;

    /*
    vec3 hitPoint = rayOrigin + (distance - 0.01) * rayDirection;

    vec4 voxelData2;
    float distance2;
    vec3 normal2;
    bool hasIntersection2 = intersectWorld(hitPoint, normalize(lightPos - hitPoint), voxelData2, distance2, normal2);
    if (!hasIntersection2) {
        return vec4(0);
    }

    return voxelData;
    */
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
