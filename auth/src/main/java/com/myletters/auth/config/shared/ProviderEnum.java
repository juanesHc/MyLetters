package com.myletters.auth.config.shared;

public enum ProviderEnum {
    CLASSIC,
    GOOGLE,
    // El usuario se registró clásico y luego entró con Google: puede usar ambos métodos.
    MIXTO
}
