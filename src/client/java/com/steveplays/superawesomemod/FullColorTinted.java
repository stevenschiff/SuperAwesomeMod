package com.steveplays.superawesomemod;

/**
 * Duck-type carrier bolted onto {@code LivingEntityRenderState} by mixin.
 *
 * <p>1.21 split entity rendering into an extract phase and a draw phase, and the
 * render state deliberately carries no {@code Entity} and no entity id. That means
 * {@code getModelTint} — the natural place to colour a body — cannot tell who it is
 * drawing or whether they are in range. The answer has to be worked out during
 * extraction, where the entity is still a parameter, and carried forward on the state
 * itself. This interface is that carrier.
 */
public interface FullColorTinted {

    /** ARGB tint for this entity, or 0 for "render normally". */
    int superawesomemod$getFullColorTint();

    void superawesomemod$setFullColorTint(int argb);
}
