package dev.outfluencer.mcproxy.api.util;

import net.lenni0451.mcstructs.text.Style;
import net.lenni0451.mcstructs.text.TextComponent;
import net.lenni0451.mcstructs.text.TextFormatting;
import net.lenni0451.mcstructs.text.components.StringComponent;
import net.lenni0451.mcstructs.text.events.click.ClickEvent;
import net.lenni0451.mcstructs.text.events.hover.HoverEvent;

import java.awt.Color;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A fluent builder for creating {@link TextComponent} instances with support for
 * formatting, colors, gradients, click/hover events, and component chaining.
 *
 * <pre>{@code
 * // Simple colored bold text
 * TextComponent msg = ComponentBuilder.text("Hello").red().bold()
 *         .append(" World").green().italic()
 *         .build();
 *
 *
 * // Multi-stop gradient
 * TextComponent multi = ComponentBuilder.gradient("Fire", Color.RED, Color.ORANGE, Color.YELLOW)
 *         .clickRunCommand("/help")
 *         .build();
 *
 * // Mixed components
 * TextComponent mixed = ComponentBuilder.text("[").gray()
 *         .appendGradient("Server", Color.CYAN, Color.BLUE).bold()
 *         .append("] ").gray()
 *         .append("Welcome!").gold().bold()
 *         .build();
 * }</pre>
 */
public class ComponentBuilder {

    private final List<Part> parts = new ArrayList<>();
    private Part current;

    private ComponentBuilder(Part initial) {
        this.current = initial;
    }

    // ===== Static Factories =====

    /**
     * Start building a text component with the given string.
     */
    public static ComponentBuilder text(String text) {
        return new ComponentBuilder(new TextPart(text));
    }

    /**
     * Start building with an empty text component.
     */
    public static ComponentBuilder empty() {
        return text("");
    }

    /**
     * Wrap an existing {@link TextComponent} in this builder.
     * Formatting set on the builder takes priority; unset properties fall back to the component's original style.
     */
    public static ComponentBuilder of(TextComponent component) {
        return new ComponentBuilder(new ComponentPart(component));
    }

    /**
     * Start building a gradient text that interpolates between two colors.
     */
    public static ComponentBuilder gradient(String text, Color from, Color to) {
        return new ComponentBuilder(new GradientPart(text, new Color[]{from, to}));
    }

    /**
     * Start building a gradient text that interpolates across multiple color stops.
     *
     * @param colors at least 2 colors
     */
    public static ComponentBuilder gradient(String text, Color... colors) {
        if (colors.length < 2) {
            throw new IllegalArgumentException("Gradient requires at least 2 colors");
        }
        return new ComponentBuilder(new GradientPart(text, colors));
    }

    /**
     * Start building a translation component.
     */
    public static ComponentBuilder translation(String key, Object... args) {
        return new ComponentBuilder(new ComponentPart(TextComponent.translation(key, args)));
    }

    /**
     * Start building a keybind component.
     */
    public static ComponentBuilder keybind(String keybind) {
        return new ComponentBuilder(new ComponentPart(TextComponent.keybind(keybind)));
    }

    /**
     * Start building a selector component.
     */
    public static ComponentBuilder selector(String selector) {
        return new ComponentBuilder(new ComponentPart(TextComponent.selector(selector)));
    }

    /**
     * Start building a score component.
     */
    public static ComponentBuilder score(String name, String objective) {
        return new ComponentBuilder(new ComponentPart(TextComponent.score(name, objective)));
    }

    // ===== Formatting =====

    public ComponentBuilder bold() {
        current.style.setBold(true);
        return this;
    }

    public ComponentBuilder bold(boolean value) {
        current.style.setBold(value);
        return this;
    }

    public ComponentBuilder italic() {
        current.style.setItalic(true);
        return this;
    }

    public ComponentBuilder italic(boolean value) {
        current.style.setItalic(value);
        return this;
    }

    public ComponentBuilder underlined() {
        current.style.setUnderlined(true);
        return this;
    }

    public ComponentBuilder underlined(boolean value) {
        current.style.setUnderlined(value);
        return this;
    }

    public ComponentBuilder strikethrough() {
        current.style.setStrikethrough(true);
        return this;
    }

    public ComponentBuilder strikethrough(boolean value) {
        current.style.setStrikethrough(value);
        return this;
    }

    public ComponentBuilder obfuscated() {
        current.style.setObfuscated(true);
        return this;
    }

    public ComponentBuilder obfuscated(boolean value) {
        current.style.setObfuscated(value);
        return this;
    }

    /**
     * Apply a {@link TextFormatting} directly. Works for both colors and formatting flags.
     */
    public ComponentBuilder formatting(TextFormatting formatting) {
        current.style.setFormatting(formatting);
        return this;
    }

    // ===== Colors =====

    /**
     * Set the color using a named {@link TextFormatting} color constant.
     */
    public ComponentBuilder color(TextFormatting color) {
        current.style.setFormatting(color);
        return this;
    }

    /**
     * Set the color using an RGB value (e.g. {@code 0xFF5555}).
     */
    public ComponentBuilder color(int rgb) {
        current.style.setColor(rgb);
        return this;
    }

    /**
     * Set the color using a {@link Color} object.
     */
    public ComponentBuilder color(Color color) {
        return color(color.getRGB() & 0xFFFFFF);
    }

    public ComponentBuilder black() { return color(TextFormatting.BLACK); }
    public ComponentBuilder darkBlue() { return color(TextFormatting.DARK_BLUE); }
    public ComponentBuilder darkGreen() { return color(TextFormatting.DARK_GREEN); }
    public ComponentBuilder darkAqua() { return color(TextFormatting.DARK_AQUA); }
    public ComponentBuilder darkRed() { return color(TextFormatting.DARK_RED); }
    public ComponentBuilder darkPurple() { return color(TextFormatting.DARK_PURPLE); }
    public ComponentBuilder gold() { return color(TextFormatting.GOLD); }
    public ComponentBuilder gray() { return color(TextFormatting.GRAY); }
    public ComponentBuilder darkGray() { return color(TextFormatting.DARK_GRAY); }
    public ComponentBuilder blue() { return color(TextFormatting.BLUE); }
    public ComponentBuilder green() { return color(TextFormatting.GREEN); }
    public ComponentBuilder aqua() { return color(TextFormatting.AQUA); }
    public ComponentBuilder red() { return color(TextFormatting.RED); }
    public ComponentBuilder lightPurple() { return color(TextFormatting.LIGHT_PURPLE); }
    public ComponentBuilder yellow() { return color(TextFormatting.YELLOW); }
    public ComponentBuilder white() { return color(TextFormatting.WHITE); }

    /**
     * Set the text shadow color (1.21.4+).
     */
    public ComponentBuilder shadowColor(int rgb) {
        current.style.setShadowColor(rgb);
        return this;
    }

    /**
     * Set the text shadow color (1.21.4+).
     */
    public ComponentBuilder shadowColor(Color color) {
        return shadowColor(color.getRGB());
    }

    // ===== Click Events =====

    public ComponentBuilder clickRunCommand(String command) {
        current.style.setClickEvent(ClickEvent.runCommand(command));
        return this;
    }

    public ComponentBuilder clickSuggestCommand(String command) {
        current.style.setClickEvent(ClickEvent.suggestCommand(command));
        return this;
    }

    public ComponentBuilder clickOpenUrl(URI url) {
        current.style.setClickEvent(ClickEvent.openUrl(url));
        return this;
    }

    public ComponentBuilder clickOpenUrl(String url) {
        return clickOpenUrl(URI.create(url));
    }

    public ComponentBuilder clickCopyToClipboard(String value) {
        current.style.setClickEvent(ClickEvent.copyToClipboard(value));
        return this;
    }

    public ComponentBuilder clickChangePage(int page) {
        current.style.setClickEvent(ClickEvent.changePage(page));
        return this;
    }

    /**
     * Set a custom {@link ClickEvent} directly.
     */
    public ComponentBuilder clickEvent(ClickEvent event) {
        current.style.setClickEvent(event);
        return this;
    }

    // ===== Hover Events =====

    public ComponentBuilder hoverText(TextComponent text) {
        current.style.setHoverEvent(HoverEvent.text(text));
        return this;
    }

    public ComponentBuilder hoverText(String text) {
        return hoverText(TextComponent.of(text));
    }

    /**
     * Set a custom {@link HoverEvent} directly (e.g. item or entity hover).
     */
    public ComponentBuilder hoverEvent(HoverEvent event) {
        current.style.setHoverEvent(event);
        return this;
    }

    // ===== Other Style =====

    /**
     * Set the insertion text (inserted into chat when shift-clicked).
     */
    public ComponentBuilder insertion(String insertion) {
        current.style.setInsertion(insertion);
        return this;
    }

    /**
     * Replace the entire {@link Style} of the current part.
     */
    public ComponentBuilder style(Style style) {
        current.style = style;
        return this;
    }

    /**
     * Modify the current part's {@link Style} directly via a consumer.
     * Useful for setting properties without dedicated builder methods (e.g. font).
     *
     * <pre>{@code
     * ComponentBuilder.text("Custom").styled(s -> s.setFont(myFont)).build();
     * }</pre>
     */
    public ComponentBuilder styled(Consumer<Style> consumer) {
        consumer.accept(current.style);
        return this;
    }

    // ===== Append =====

    /**
     * Finalize the current part and start a new text part.
     */
    public ComponentBuilder append(String text) {
        finalizeCurrent();
        current = new TextPart(text);
        return this;
    }

    /**
     * Finalize the current part and append an existing {@link TextComponent}.
     */
    public ComponentBuilder append(TextComponent component) {
        finalizeCurrent();
        current = new ComponentPart(component);
        return this;
    }

    /**
     * Finalize the current part and append the result of another builder.
     */
    public ComponentBuilder append(ComponentBuilder builder) {
        finalizeCurrent();
        current = new ComponentPart(builder.build());
        return this;
    }

    /**
     * Finalize the current part and start a new gradient part.
     */
    public ComponentBuilder appendGradient(String text, Color from, Color to) {
        finalizeCurrent();
        current = new GradientPart(text, new Color[]{from, to});
        return this;
    }

    /**
     * Finalize the current part and start a new multi-stop gradient part.
     *
     * @param colors at least 2 colors
     */
    public ComponentBuilder appendGradient(String text, Color... colors) {
        if (colors.length < 2) {
            throw new IllegalArgumentException("Gradient requires at least 2 colors");
        }
        finalizeCurrent();
        current = new GradientPart(text, colors);
        return this;
    }

    /**
     * Finalize the current part and start a new translation part.
     */
    public ComponentBuilder appendTranslation(String key, Object... args) {
        finalizeCurrent();
        current = new ComponentPart(TextComponent.translation(key, args));
        return this;
    }

    /**
     * Finalize the current part and start a new keybind part.
     */
    public ComponentBuilder appendKeybind(String keybind) {
        finalizeCurrent();
        current = new ComponentPart(TextComponent.keybind(keybind));
        return this;
    }

    /**
     * Append a newline character as a separate part.
     */
    public ComponentBuilder newLine() {
        return append("\n");
    }

    private void finalizeCurrent() {
        if (current != null) {
            parts.add(current);
            current = null;
        }
    }

    // ===== Build =====

    /**
     * Build the final {@link TextComponent} from all accumulated parts.
     */
    public TextComponent build() {
        finalizeCurrent();

        if (parts.size() == 1) {
            return parts.getFirst().build();
        }

        TextComponent root = TextComponent.empty();
        for (Part part : parts) {
            root.append(part.build());
        }
        return root;
    }

    // ===== Internal Part types =====

    private static abstract class Part {
        Style style = new Style();

        abstract TextComponent build();
    }

    private static class TextPart extends Part {
        private final String text;

        TextPart(String text) {
            this.text = text;
        }

        @Override
        TextComponent build() {
            StringComponent component = new StringComponent(text);
            if (!style.isEmpty()) {
                component.setStyle(style);
            }
            return component;
        }
    }

    private static class ComponentPart extends Part {
        private final TextComponent component;

        ComponentPart(TextComponent component) {
            this.component = component;
        }

        @Override
        TextComponent build() {
            if (!style.isEmpty()) {
                Style original = component.getStyle();
                if (!original.isEmpty()) {
                    style.setParent(original);
                }
                component.setStyle(style);
            }
            return component;
        }
    }

    private static class GradientPart extends Part {
        private final String text;
        private final Color[] colors;

        GradientPart(String text, Color[] colors) {
            this.text = text;
            this.colors = colors;
        }

        @Override
        TextComponent build() {
            if (text.isEmpty()) {
                return TextComponent.empty();
            }

            int length = text.length();
            if (length == 1) {
                StringComponent component = new StringComponent(text);
                Style charStyle = style.copy();
                charStyle.setColor(colors[0].getRGB() & 0xFFFFFF);
                component.setStyle(charStyle);
                return component;
            }

            TextComponent root = TextComponent.empty();
            for (int i = 0; i < length; i++) {
                float progress = (float) i / (length - 1);
                Color interpolated = interpolateColor(progress, colors);

                StringComponent charComponent = new StringComponent(String.valueOf(text.charAt(i)));
                Style charStyle = style.copy();
                charStyle.setColor(interpolated.getRGB() & 0xFFFFFF);
                charComponent.setStyle(charStyle);
                root.append(charComponent);
            }

            return root;
        }
    }

    // ===== Color interpolation =====

    private static Color interpolateColor(float progress, Color[] colors) {
        if (progress <= 0f) return colors[0];
        if (progress >= 1f) return colors[colors.length - 1];

        float segment = progress * (colors.length - 1);
        int index = (int) segment;
        float t = segment - index;

        if (index >= colors.length - 1) return colors[colors.length - 1];

        Color from = colors[index];
        Color to = colors[index + 1];

        return new Color(
                Math.round(from.getRed() + (to.getRed() - from.getRed()) * t),
                Math.round(from.getGreen() + (to.getGreen() - from.getGreen()) * t),
                Math.round(from.getBlue() + (to.getBlue() - from.getBlue()) * t)
        );
    }
}
