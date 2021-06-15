package utils;

public enum Colors {

    RED_DOT("🔴 "),
    DOT19("💎 "),
    BLUE_DOT("🔵 "),
    DOT("🟠 "),
    DOT7("🍕 "),
    DOT3("🟣 "),
    DOT10("🧀 "),
    DOT1("🟡 "),
    DOT8("🎃 "),
    DOT12("🚗 "),
    DOT4("🟤 "),
    DOT15("⛔ "),
    DOT2("🟢 "),
    DOT5("⚫ "),
    DOT11("🧨 "),
    DOT6("⚪ "),

    DOT17("🐵 "),
    DOT13("🎂 "),
    DOT14("🍺 "),
    DOT9("🎗️ "),

    DOT16("🥇 "),
    DOT18("⚽ "),
    DOT120("🆗 "),
    ;


    final String color;

    Colors(String color) {
        this.color = color;
    }

    public String getColor() {
        return color;
    }


}