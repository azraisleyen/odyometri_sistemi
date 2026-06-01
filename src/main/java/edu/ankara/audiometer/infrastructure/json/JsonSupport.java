package edu.ankara.audiometer.infrastructure.json;

import edu.ankara.audiometer.domain.fp.Result;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class JsonSupport {
    private JsonSupport() {
    }

    public static Result<Object, String> parse(String json) {
        try {
            return Result.ok(new Parser(json).parse());
        } catch (RuntimeException exception) {
            return Result.err(exception.getMessage());
        }
    }

    public static String prettyPrint(Object value) {
        StringBuilder builder = new StringBuilder();
        write(value, builder, 0);
        return builder.toString();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> asObject(Object value) {
        return value instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
    }

    @SuppressWarnings("unchecked")
    public static List<Object> asArray(Object value) {
        return value instanceof List<?> list ? (List<Object>) list : List.of();
    }

    public static int intValue(Map<String, Object> object, String key, int fallback) {
        Object value = object.get(key);
        return value instanceof Number number ? number.intValue() : fallback;
    }

    public static boolean boolValue(Map<String, Object> object, String key, boolean fallback) {
        Object value = object.get(key);
        return value instanceof Boolean bool ? bool : fallback;
    }

    public static String stringValue(Map<String, Object> object, String key, String fallback) {
        Object value = object.get(key);
        return value instanceof String string ? string : fallback;
    }

    private static void write(Object value, StringBuilder builder, int indent) {
        if (value == null) {
            builder.append("null");
        } else if (value instanceof String string) {
            builder.append('"').append(escape(string)).append('"');
        } else if (value instanceof Number || value instanceof Boolean) {
            builder.append(value);
        } else if (value instanceof Map<?, ?> map) {
            writeObject(map, builder, indent);
        } else if (value instanceof Iterable<?> iterable) {
            writeArray(iterable, builder, indent);
        } else {
            builder.append('"').append(escape(String.valueOf(value))).append('"');
        }
    }

    private static void writeObject(Map<?, ?> map, StringBuilder builder, int indent) {
        builder.append("{");
        if (!map.isEmpty()) {
            boolean first = true;
            for (var entry : map.entrySet()) {
                if (!first) {
                    builder.append(',');
                }
                builder.append('\n').append("  ".repeat(indent + 1));
                write(String.valueOf(entry.getKey()), builder, indent + 1);
                builder.append(": ");
                write(entry.getValue(), builder, indent + 1);
                first = false;
            }
            builder.append('\n').append("  ".repeat(indent));
        }
        builder.append('}');
    }

    private static void writeArray(Iterable<?> iterable, StringBuilder builder, int indent) {
        builder.append('[');
        boolean first = true;
        for (Object item : iterable) {
            if (!first) {
                builder.append(',');
            }
            builder.append('\n').append("  ".repeat(indent + 1));
            write(item, builder, indent + 1);
            first = false;
        }
        if (!first) {
            builder.append('\n').append("  ".repeat(indent));
        }
        builder.append(']');
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static final class Parser {
        private final String text;
        private int index;

        Parser(String text) {
            this.text = text == null ? "" : text;
        }

        Object parse() {
            Object value = value();
            skipWhitespace();
            if (index != text.length()) {
                throw error("Trailing content");
            }
            return value;
        }

        private Object value() {
            skipWhitespace();
            if (index >= text.length()) throw error("Unexpected end of JSON");
            char c = text.charAt(index);
            return switch (c) {
                case '{' -> object();
                case '[' -> array();
                case '"' -> string();
                case 't' -> literal("true", Boolean.TRUE);
                case 'f' -> literal("false", Boolean.FALSE);
                case 'n' -> literal("null", null);
                default -> number();
            };
        }

        private Map<String, Object> object() {
            expect('{');
            Map<String, Object> object = new LinkedHashMap<>();
            skipWhitespace();
            if (consume('}')) return object;
            do {
                skipWhitespace();
                String key = string();
                skipWhitespace();
                expect(':');
                object.put(key, value());
                skipWhitespace();
            } while (consume(','));
            expect('}');
            return object;
        }

        private List<Object> array() {
            expect('[');
            List<Object> values = new ArrayList<>();
            skipWhitespace();
            if (consume(']')) return values;
            do {
                values.add(value());
                skipWhitespace();
            } while (consume(','));
            expect(']');
            return values;
        }

        private String string() {
            expect('"');
            StringBuilder builder = new StringBuilder();
            while (index < text.length()) {
                char c = text.charAt(index++);
                if (c == '"') return builder.toString();
                if (c == '\\') {
                    if (index >= text.length()) throw error("Bad escape");
                    char escaped = text.charAt(index++);
                    builder.append(switch (escaped) {
                        case '"' -> '"';
                        case '\\' -> '\\';
                        case '/' -> '/';
                        case 'b' -> '\b';
                        case 'f' -> '\f';
                        case 'n' -> '\n';
                        case 'r' -> '\r';
                        case 't' -> '\t';
                        default -> throw error("Unsupported escape: " + escaped);
                    });
                } else {
                    builder.append(c);
                }
            }
            throw error("Unterminated string");
        }

        private Object number() {
            int start = index;
            if (index < text.length() && text.charAt(index) == '-') index++;
            while (index < text.length() && Character.isDigit(text.charAt(index))) index++;
            if (index < text.length() && text.charAt(index) == '.') {
                index++;
                while (index < text.length() && Character.isDigit(text.charAt(index))) index++;
                return Double.parseDouble(text.substring(start, index));
            }
            if (start == index || (text.charAt(start) == '-' && start + 1 == index)) {
                throw error("Expected JSON value");
            }
            return Integer.parseInt(text.substring(start, index));
        }

        private Object literal(String literal, Object value) {
            if (!text.startsWith(literal, index)) throw error("Expected " + literal);
            index += literal.length();
            return value;
        }

        private void skipWhitespace() {
            while (index < text.length() && Character.isWhitespace(text.charAt(index))) index++;
        }

        private boolean consume(char expected) {
            if (index < text.length() && text.charAt(index) == expected) {
                index++;
                return true;
            }
            return false;
        }

        private void expect(char expected) {
            if (!consume(expected)) throw error("Expected '" + expected + "'");
        }

        private IllegalArgumentException error(String message) {
            return new IllegalArgumentException(message + " at character " + index);
        }
    }
}
