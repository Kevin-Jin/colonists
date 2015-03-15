#ifdef GL_ES
#define LOWP lowp
    precision mediump float;
#else
    #define LOWP
#endif

varying LOWP vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_texture;

void main()
{
    vec4 textureColor = texture2D(u_texture, v_texCoords);
    //FIXME: this causes the entire texture to be discarded
    //if (textureColor.a == 0) discard;
    gl_FragColor = v_color * textureColor;
}