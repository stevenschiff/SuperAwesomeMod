#version 330

uniform sampler2D DiffuseSampler;
uniform sampler2D PrevSampler;

in vec2 texCoord;

layout(std140) uniform MotionBlurConfig {
    float BlendFactor;
};

out vec4 fragColor;

void main() {
    vec4 current = texture(DiffuseSampler, texCoord);
    vec4 previous = texture(PrevSampler, texCoord);
    fragColor = mix(current, previous, BlendFactor);
}
