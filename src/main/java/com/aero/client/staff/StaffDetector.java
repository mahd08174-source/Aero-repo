package com.aero.client.staff;

import com.aero.client.staff.StaffEntry;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_5251;

public final class StaffDetector {
    public static final String MODE_STAR_RANK = "Star + Rank";
    public static final String MODE_STAR = "Star Only";
    public static final String MODE_RANK = "Rank Only";
    public static final String MODE_NAMES = "Names Only";
    public static final String DEFAULT_SYMBOLS = "\u2605\u2606\u2726\u2727\u272a\u2729\u272b\u272c\u272d\u272e\u272f\u2b50\u2730\u2742\u269d\u2734\u2735\u2736\u2737\u2738\u2739\u235f\u2764\u2665\u25c6\u25cf\u25b6\u26a1\u2714\u2718\u00bb\u00ab\u2023\u203a\u2039\u00b7\u2022\u25aa\u25ab";
    public static final List<String> DEFAULT_RANK_KEYWORDS = List.of("owner", "admin", "manager", "moderator", "mod", "helper", "hmod", "support", "staff", "srmod", "operator", "dev", "developer", "partner", "builder", "trainee");
    public static final String DEFAULT_RANK_KEYWORDS_STRING = String.join((CharSequence)",", DEFAULT_RANK_KEYWORDS);
    private static final Map<String, String> RANK_LABELS = Map.ofEntries(Map.entry("owner", "Owner"), Map.entry("admin", "Admin"), Map.entry("manager", "Manager"), Map.entry("moderator", "Mod"), Map.entry("mod", "Mod"), Map.entry("helper", "Helper"), Map.entry("hmod", "HMod"), Map.entry("support", "Support"), Map.entry("staff", "Staff"), Map.entry("srmod", "SrMod"), Map.entry("operator", "Op"), Map.entry("dev", "Dev"), Map.entry("developer", "Dev"), Map.entry("partner", "Partner"), Map.entry("builder", "Builder"), Map.entry("trainee", "Trainee"));

    private StaffDetector() {
    }

    public static StaffEntry classify(String name, class_2561 display, class_2561 teamPrefix, class_2561 teamSuffix, String teamName, boolean vanished, int latency, DetectConfig cfg) {
        if (MODE_NAMES.equals(cfg.mode())) {
            if (!cfg.names().isEmpty() && cfg.names().contains(name.toLowerCase(Locale.ROOT))) {
                return new StaffEntry(name, "", 0, vanished, latency, 0, null);
            }
            return null;
        }
        boolean byName = !cfg.names().isEmpty() && cfg.names().contains(name.toLowerCase(Locale.ROOT));
        Integer markerColor = null;
        if (!MODE_RANK.equals(cfg.mode())) {
            markerColor = StaffDetector.scanMarker(display, cfg);
            if (markerColor == null) {
                markerColor = StaffDetector.scanMarker(teamPrefix, cfg);
            }
            if (markerColor == null) {
                markerColor = StaffDetector.scanMarker(teamSuffix, cfg);
            }
        }
        Rank rank = null;
        if (!MODE_STAR.equals(cfg.mode())) {
            String allText = StaffDetector.plain(display) + " " + StaffDetector.plain(teamPrefix) + " " + StaffDetector.plain(teamSuffix);
            rank = StaffDetector.deriveRank(StaffDetector.stripName(allText, name), cfg.rankKeywords());
        }
        if (!(switch (cfg.mode()) {
            case MODE_STAR_RANK -> markerColor != null && rank != null;
            case MODE_STAR -> markerColor != null;
            case MODE_RANK -> rank != null;
            default -> byName;
        }) && !byName) {
            return null;
        }
        return new StaffEntry(name, rank != null ? rank.label() : "", markerColor != null ? markerColor : 0, vanished, latency, rank != null ? rank.priority() : 0, null);
    }

    static Integer scanMarker(class_2561 text, DetectConfig cfg) {
        int cp;
        if (text == null) {
            return null;
        }
        Optional styled = text.method_27658((style, str) -> {
            int code;
            for (int i = 0; i < str.length(); i += Character.charCount(code)) {
                code = str.codePointAt(i);
                if (!StaffDetector.isMarker(code, cfg)) continue;
                class_5251 tc = style.method_10973();
                return Optional.of(tc != null ? 0xFF000000 | tc.method_27716() : 0);
            }
            return Optional.empty();
        }, class_2583.field_24360);
        if (styled.isPresent()) {
            return (Integer)styled.get();
        }
        String plain = text.getString();
        for (int i = 0; i < plain.length(); i += Character.charCount(cp)) {
            cp = plain.codePointAt(i);
            if (!StaffDetector.isMarker(cp, cfg)) continue;
            return 0;
        }
        return null;
    }

    private static boolean isMarker(int cp, DetectConfig cfg) {
        if (cfg.symbols().indexOf(cp) >= 0) {
            return true;
        }
        if (cfg.fontIcons()) {
            return cp >= 57344 && cp <= 63743 || cp >= 983040 && cp <= 1048573 || cp >= 0x100000 && cp <= 1114109;
        }
        return false;
    }

    private static Rank deriveRank(String tagText, List<String> keywords) {
        String cleaned = StaffDetector.lettersOnly(tagText.toLowerCase(Locale.ROOT));
        if (cleaned.isEmpty()) {
            return null;
        }
        for (int i = 0; i < keywords.size(); ++i) {
            String kw = keywords.get(i);
            if (kw.isEmpty() || !cleaned.contains(kw)) continue;
            return new Rank(StaffDetector.labelFor(kw), keywords.size() - i);
        }
        return null;
    }

    private static String labelFor(String keyword) {
        String known = RANK_LABELS.get(keyword);
        if (known != null) {
            return known;
        }
        if (keyword.isEmpty()) {
            return "Staff";
        }
        return Character.toUpperCase(keyword.charAt(0)) + keyword.substring(1);
    }

    private static String plain(class_2561 text) {
        return text == null ? "" : text.getString();
    }

    private static String lettersOnly(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); ++i) {
            char ch = s.charAt(i);
            if (ch < 'a' || ch > 'z') continue;
            sb.append(ch);
        }
        return sb.toString();
    }

    private static String stripName(String text, String name) {
        if (name == null || name.isEmpty()) {
            return text;
        }
        return text.replaceAll("(?i)" + Pattern.quote(name), "");
    }

    public record DetectConfig(String mode, Set<String> names, List<String> rankKeywords, String symbols, boolean fontIcons, boolean showVanished) {
    }

    private record Rank(String label, int priority) {
    }
}

