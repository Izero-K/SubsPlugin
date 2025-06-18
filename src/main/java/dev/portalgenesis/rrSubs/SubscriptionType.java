package dev.portalgenesis.rrSubs;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public enum SubscriptionType {
    NONE(NamedTextColor.WHITE),
    PRO(NamedTextColor.AQUA),
    DELUXE(NamedTextColor.GOLD);

    private final NamedTextColor color;

    SubscriptionType(NamedTextColor color) {
        this.color = color;
    }

    public NamedTextColor getColor() {
        return color;
    }

    public Component getDisplayName() {
        return Component.text(name()).color(getColor());
    }
}