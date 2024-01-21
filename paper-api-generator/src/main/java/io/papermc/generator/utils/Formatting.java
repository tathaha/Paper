package io.papermc.generator.utils;

import org.apache.commons.lang3.math.NumberUtils;
import java.util.Comparator;
import java.util.Locale;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.regex.Pattern;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;

public final class Formatting {

    private static final Pattern ILLEGAL_FIELD_CHARACTERS = Pattern.compile("[.-/]");

    public static String formatKeyAsField(String path) {
        return ILLEGAL_FIELD_CHARACTERS.matcher(path.toUpperCase(Locale.ENGLISH)).replaceAll("_");
    }

    public static String formatTagFieldPrefix(String name, ResourceKey<? extends Registry<?>> registryKey) {
        if (registryKey == Registries.BLOCK) {
            return "";
        }
        if (registryKey == Registries.GAME_EVENT) {
            return "GAME_EVENT_"; // Paper doesn't follow the format (should be GAME_EVENTS_)
        }
        return name.toUpperCase(Locale.ENGLISH) + "_";
    }

    public static Comparator<String> ALPHABETIC_KEY_ORDER = alphabeticKeyOrder(path -> path);

    public static <T> Comparator<T> alphabeticKeyOrder(Function<T, String> mapper) {
        return (o1, o2) -> {
            String path1 = mapper.apply(o1);
            String path2 = mapper.apply(o2);

            OptionalInt trailingInt1 = tryParseTrailingInt(path1);
            OptionalInt trailingInt2 = tryParseTrailingInt(path2);

            if (trailingInt1.isPresent() && trailingInt2.isPresent()) {
                return Integer.compare(trailingInt1.getAsInt(), trailingInt2.getAsInt());
            }

            return path1.compareTo(path2);
        };
    }

    private static OptionalInt tryParseTrailingInt(String path) {
        int delimiterIndex = path.lastIndexOf('_');
        if (delimiterIndex != -1) {
            String score = path.substring(delimiterIndex + 1);
            if (NumberUtils.isDigits(score)) {
                return OptionalInt.of(Integer.parseInt(score));
            }
        }
        return OptionalInt.empty();
    }

    private Formatting() {
    }
}
