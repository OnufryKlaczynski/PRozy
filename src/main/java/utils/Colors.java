package utils;

public enum Colors {

    RED_DOT("🔴 "),
    BLUE_DOT("🔵 "),
    DOT("🟠 "),
    DOT3("🟣 "),
    DOT1("🟡 "),
    DOT4("🟤 "),
    DOT2("🟢 "),
    DOT5("⚫ "),
    DOT6("⚪ "),
    ;


    final String color;

    Colors(String color) {
        this.color = color;
    }

    public String getColor() {
        return color;
    }


}