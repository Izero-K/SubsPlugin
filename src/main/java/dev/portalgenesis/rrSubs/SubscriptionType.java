package dev.portalgenesis.rrSubs;

public enum SubscriptionType {
    NONE("§6Хомяк"),
    PRO("§6Про"),
    DELUXE("§bДелюкс");

    private final String displayName;

    SubscriptionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    // Новый метод - возвращает имя без цветовых кодов
    public String getCleanName() {
        return displayName.replaceAll("§[0-9a-f]", "");
    }
}