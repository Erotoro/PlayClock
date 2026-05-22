package dev.maksg.playclock.mc12111.client.config;

import dev.maksg.playclock.core.config.PlayClockColorCodec;
import dev.maksg.playclock.core.config.PlayClockColorPalette;
import dev.maksg.playclock.core.config.PlayClockColorSlot;
import dev.maksg.playclock.core.config.PlayClockConfigDraft;
import dev.maksg.playclock.core.ui.PlayClockTranslationKeys;
import dev.maksg.playclock.mc12111.client.PlayClock12111Client;
import dev.maksg.playclock.mc12111.client.PlayClock12111Localization;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class PlayClock12111ConfigScreen extends Screen {
    private static final int OUTER_BG = 0xD0101014;
    private static final int PANEL_BG = 0xAA141218;
    private static final int PANEL_BORDER = 0xCC5A4A32;
    private static final int PANEL_INNER = 0x402B2218;
    private static final int TITLE_COLOR = 0xFFF2ECE0;
    private static final int LABEL_COLOR = 0xFFB8B1A1;
    private static final int VALUE_COLOR = 0xFFFFD75E;
    private static final int NAV_WIDTH = 96;
    private static final int PREVIEW_WIDTH = 286;
    private static final int PANEL_PADDING = 12;
    private static final int CONTROL_HEIGHT = 20;
    private static final int CONTROL_GAP = 24;
    private static final int COLOR_PALETTE_WIDTH = 220;
    private static final int COLOR_PALETTE_HEIGHT = 124;
    private static final int HUE_BAR_HEIGHT = 12;
    private static final int SWATCH_SIZE = 36;
    private static final Identifier COLOR_PALETTE_TEXTURE_ID = Identifier.of("playclock", "config/color_palette_12111");
    private static final Identifier HUE_BAR_TEXTURE_ID = Identifier.of("playclock", "config/color_hue_12111");

    private final Screen parent;
    private final PlayClockConfigDraft draft;
    private Section section = Section.GENERAL;
    private PlayClockColorSlot colorSlot = PlayClockColorSlot.HUD_VALUE;

    private TextFieldWidget redField;
    private TextFieldWidget greenField;
    private TextFieldWidget blueField;
    private TextFieldWidget hexField;
    private float colorHue;
    private float colorSaturation;
    private float colorValue;
    private boolean draggingPalette;
    private boolean draggingHue;
    private final List<ButtonWidget> colorTargetButtons = new ArrayList<>();
    private NativeImageBackedTexture paletteTexture;
    private NativeImageBackedTexture hueTexture;
    private boolean paletteTextureDirty = true;
    private boolean hueTextureDirty = true;

    @FunctionalInterface
    private interface BooleanSetter {
        void accept(boolean value);
    }

    public PlayClock12111ConfigScreen(Screen parent) {
        super(PlayClock12111Localization.text(PlayClockTranslationKeys.CONFIG_TITLE));
        this.parent = parent;
        this.draft = PlayClockConfigDraft.from(PlayClock12111Client.runtimeService().snapshot().config());
        syncPickerFromDraft();
    }

    @Override
    protected void init() {
        clearChildren();
        addSectionButtons();
        addSectionContent();
        addFooterButtons();
        syncColorInputsFromDraft();
    }

    @Override
    public void close() {
        releaseColorTextures();
        if (client != null) {
            client.setScreen(parent);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, OUTER_BG);
        drawPanels(context);
        super.render(context, mouseX, mouseY, delta);
        drawStaticLabels(context);
        drawPreview(context);
    }

    @Override
    public boolean mouseClicked(Click click, boolean bl) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();
        if (section == Section.COLORS && button == 0) {
            if (contains(mouseX, mouseY, colorPaletteX(), colorEditorY(), COLOR_PALETTE_WIDTH, COLOR_PALETTE_HEIGHT)) {
                draggingPalette = true;
                updateColorFromPalette(mouseX, mouseY);
                return true;
            }
            if (contains(mouseX, mouseY, colorPaletteX(), colorHueY(), COLOR_PALETTE_WIDTH, HUE_BAR_HEIGHT)) {
                draggingHue = true;
                updateColorFromHue(mouseX);
                return true;
            }
        }
        return super.mouseClicked(click, bl);
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();
        if (section == Section.COLORS && button == 0) {
            if (draggingPalette) {
                updateColorFromPalette(mouseX, mouseY);
                return true;
            }
            if (draggingHue) {
                updateColorFromHue(mouseX);
                return true;
            }
        }
        return super.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (click.button() == 0) {
            draggingPalette = false;
            draggingHue = false;
        }
        return super.mouseReleased(click);
    }

    private void addSectionButtons() {
        int navX = 24;
        int navY = 52;
        int navButtonWidth = NAV_WIDTH - 16;
        for (int i = 0; i < Section.values().length; i++) {
            Section item = Section.values()[i];
            addDrawableChild(ButtonWidget.builder(Text.literal(sectionLabel(item)), button -> switchSection(item))
                    .dimensions(navX + 8, navY + i * 24, navButtonWidth, CONTROL_HEIGHT)
                    .build());
        }
    }

    private void addSectionContent() {
        int baseX = contentX();
        int y = 80;
        int controlWidth = contentWidth() - PANEL_PADDING * 2;

        switch (section) {
            case GENERAL -> {
                addToggleButton(baseX, y, controlWidth, PlayClockTranslationKeys.CONFIG_HUD, draft.hudEnabled(), draft::setHudEnabled);
                addToggleButton(baseX, y + CONTROL_GAP, controlWidth, PlayClockTranslationKeys.CONFIG_BADGES, draft.badgeEnabled(), draft::setBadgeEnabled);
                addToggleButton(baseX, y + CONTROL_GAP * 2, controlWidth, PlayClockTranslationKeys.CONFIG_TOOLTIPS, draft.tooltipsEnabled(), draft::setTooltipsEnabled);
                addCycleButton(baseX, y + CONTROL_GAP * 3, controlWidth, PlayClockTranslationKeys.CONFIG_LANGUAGE, this::languageValueLabel, draft::nextLanguage);
            }
            case HUD -> {
                addCycleButton(baseX, y, controlWidth, PlayClockTranslationKeys.CONFIG_HUD_VARIANT, this::hudVariantValueLabel, draft::nextHudVariant);
                addCycleButton(baseX, y + CONTROL_GAP, controlWidth, PlayClockTranslationKeys.CONFIG_HUD_POSITION, this::hudPositionValueLabel, draft::nextHudAnchor);
                addCycleButton(baseX, y + CONTROL_GAP * 2, controlWidth, PlayClockTranslationKeys.CONFIG_TIME_FORMAT, this::timeFormatValueLabel, draft::nextTimeFormat);
                addToggleButton(baseX, y + CONTROL_GAP * 3, controlWidth, PlayClockTranslationKeys.CONFIG_SHOW_TODAY_IN_HUD, draft.showTodayInHud(), draft::setShowTodayInHud);
                addToggleButton(baseX, y + CONTROL_GAP * 4, controlWidth, PlayClockTranslationKeys.CONFIG_SHOW_SESSION_IN_HUD, draft.showSessionInHud(), draft::setShowSessionInHud);
            }
            case COLORS -> addColorControls(baseX, y, controlWidth);
            case ADVANCED -> addToggleButton(baseX, y + CONTROL_GAP * 2, controlWidth, PlayClockTranslationKeys.CONFIG_SHOW_HEADER_SUMMARY, draft.showHeaderSummary(), draft::setShowHeaderSummary);
        }
    }

    private void addColorControls(int baseX, int y, int controlWidth) {
        addCycleButton(baseX, y, controlWidth, PlayClockTranslationKeys.CONFIG_COLOR_MODE, this::colorModeValueLabel, () -> {
            draft.nextColorMode();
            if ("preset".equals(draft.colorMode())) {
                draft.usePresetColors();
            }
            syncPickerFromDraft();
            syncColorInputsFromDraft();
        });
        addCycleButton(baseX, y + CONTROL_GAP, controlWidth, PlayClockTranslationKeys.CONFIG_COLOR_PRESET, this::colorPresetValueLabel, () -> {
            draft.nextColorPresetAndApply();
            syncPickerFromDraft();
            syncColorInputsFromDraft();
        });
        addColorTargetButtons(baseX, y + CONTROL_GAP * 2 + 14, controlWidth);

        int fieldY = colorHueY() + HUE_BAR_HEIGHT + 22;
        int fieldWidth = (controlWidth - 12) / 4;
        redField = addColorField(baseX, fieldY, fieldWidth, PlayClockTranslationKeys.CONFIG_RED, 3);
        greenField = addColorField(baseX + fieldWidth + 4, fieldY, fieldWidth, PlayClockTranslationKeys.CONFIG_GREEN, 3);
        blueField = addColorField(baseX + (fieldWidth + 4) * 2, fieldY, fieldWidth, PlayClockTranslationKeys.CONFIG_BLUE, 3);
        hexField = addColorField(baseX + (fieldWidth + 4) * 3, fieldY, fieldWidth, PlayClockTranslationKeys.CONFIG_HEX, 7);

        addDrawableChild(ButtonWidget.builder(
                        PlayClock12111Localization.text(PlayClockTranslationKeys.CONFIG_APPLY_COLOR),
                        button -> applyColorFields())
                .dimensions(baseX, fieldY + 32, controlWidth, CONTROL_HEIGHT)
                .build());

        addDrawableChild(ButtonWidget.builder(
                        PlayClock12111Localization.text(PlayClockTranslationKeys.CONFIG_RESET_PRESET),
                        button -> {
                            draft.usePresetColors();
                            syncPickerFromDraft();
                            syncColorInputsFromDraft();
                        })
                .dimensions(baseX, fieldY + 56, controlWidth, CONTROL_HEIGHT)
                .build());
    }

    private void addColorTargetButtons(int x, int y, int width) {
        colorTargetButtons.clear();
        int columns = 3;
        int gap = 4;
        int buttonWidth = (width - gap * (columns - 1)) / columns;
        PlayClockColorSlot[] slots = PlayClockColorSlot.values();
        for (int i = 0; i < slots.length; i++) {
            PlayClockColorSlot slot = slots[i];
            int row = i / columns;
            int column = i % columns;
            ButtonWidget button = ButtonWidget.builder(
                            Text.literal(colorTargetButtonLabel(slot)),
                            clicked -> {
                                colorSlot = slot;
                                syncPickerFromDraft();
                                syncColorInputsFromDraft();
                                refreshColorTargetButtons();
                            })
                    .dimensions(x + column * (buttonWidth + gap), y + row * (CONTROL_HEIGHT + gap), buttonWidth, CONTROL_HEIGHT)
                    .build();
            colorTargetButtons.add(button);
            addDrawableChild(button);
        }
        refreshColorTargetButtons();
    }

    private TextFieldWidget addColorField(int x, int y, int width, String translationKey, int maxLength) {
        TextFieldWidget field = new TextFieldWidget(textRenderer, x, y, width, CONTROL_HEIGHT, PlayClock12111Localization.text(translationKey));
        field.setMaxLength(maxLength);
        field.setDrawsBackground(true);
        field.setEditableColor(TITLE_COLOR);
        addDrawableChild(field);
        return field;
    }

    private void addFooterButtons() {
        int rightX = previewX();
        int footerY = height - 38;
        int buttonWidth = 132;
        addDrawableChild(ButtonWidget.builder(PlayClock12111Localization.text(PlayClockTranslationKeys.CONFIG_CANCEL), button -> close())
                .dimensions(rightX, footerY, buttonWidth, CONTROL_HEIGHT)
                .build());
        addDrawableChild(ButtonWidget.builder(PlayClock12111Localization.text(PlayClockTranslationKeys.CONFIG_DONE), button -> applyAndClose())
                .dimensions(rightX + PREVIEW_WIDTH - buttonWidth, footerY, buttonWidth, CONTROL_HEIGHT)
                .build());
    }

    private void switchSection(Section next) {
        if (section != next) {
            section = next;
            init();
        }
    }

    private void addToggleButton(int x, int y, int width, String labelKey, boolean initialValue, BooleanSetter consumer) {
        addDrawableChild(ButtonWidget.builder(Text.literal(toggleLabel(labelKey, initialValue)), button -> {
                    boolean nextValue = !button.getMessage().getString().endsWith(yesLabel());
                    consumer.accept(nextValue);
                    button.setMessage(Text.literal(toggleLabel(labelKey, nextValue)));
                })
                .dimensions(x, y, width, CONTROL_HEIGHT)
                .build());
    }

    private void addCycleButton(int x, int y, int width, String labelKey, java.util.function.Supplier<String> valueSupplier, Runnable action) {
        addDrawableChild(ButtonWidget.builder(Text.literal(cycleLabel(labelKey, valueSupplier.get())), button -> {
                    action.run();
                    button.setMessage(Text.literal(cycleLabel(labelKey, valueSupplier.get())));
                })
                .dimensions(x, y, width, CONTROL_HEIGHT)
                .build());
    }

    private void applyAndClose() {
        PlayClock12111Client.runtimeService().updateConfig(draft.toConfig());
        close();
    }

    private void applyColorFields() {
        int fallback = draft.effectiveColor(colorSlot);
        int red = PlayClockColorCodec.parseRgbChannel(redField.getText(), PlayClockColorCodec.red(fallback));
        int green = PlayClockColorCodec.parseRgbChannel(greenField.getText(), PlayClockColorCodec.green(fallback));
        int blue = PlayClockColorCodec.parseRgbChannel(blueField.getText(), PlayClockColorCodec.blue(fallback));
        int color = PlayClockColorCodec.fromRgb(red, green, blue);
        color = PlayClockColorCodec.parseHexRgb(hexField.getText(), color);
        draft.setColor(colorSlot, color);
        draft.setColorMode("custom");
        syncPickerFromDraft();
        syncColorInputsFromDraft();
    }

    private void syncColorInputsFromDraft() {
        int color = draft.effectiveColor(colorSlot);
        if (redField != null) {
            redField.setText(Integer.toString(PlayClockColorCodec.red(color)));
            greenField.setText(Integer.toString(PlayClockColorCodec.green(color)));
            blueField.setText(Integer.toString(PlayClockColorCodec.blue(color)));
            hexField.setText(PlayClockColorCodec.toHexRgb(color));
        }
    }

    private void syncPickerFromDraft() {
        float[] hsv = PlayClockColorCodec.toHsv(draft.effectiveColor(colorSlot));
        colorHue = hsv[0];
        colorSaturation = hsv[1];
        colorValue = hsv[2];
        paletteTextureDirty = true;
    }

    private void updateColorFromPalette(double mouseX, double mouseY) {
        colorSaturation = clamp01((float) ((mouseX - colorPaletteX()) / Math.max(1.0, COLOR_PALETTE_WIDTH - 1.0)));
        colorValue = 1.0f - clamp01((float) ((mouseY - colorEditorY()) / Math.max(1.0, COLOR_PALETTE_HEIGHT - 1.0)));
        applyPickerColor();
    }

    private void updateColorFromHue(double mouseX) {
        colorHue = clamp01((float) ((mouseX - colorPaletteX()) / Math.max(1.0, COLOR_PALETTE_WIDTH - 1.0)));
        paletteTextureDirty = true;
        applyPickerColor();
    }

    private void applyPickerColor() {
        draft.setColorMode("custom");
        draft.setColor(colorSlot, PlayClockColorCodec.fromHsv(colorHue, colorSaturation, colorValue));
        syncColorInputsFromDraft();
    }

    private void drawPanels(DrawContext context) {
        drawPanel(context, 20, 44, NAV_WIDTH, height - 88);
        drawPanel(context, contentX() - PANEL_PADDING, 44, contentWidth() + PANEL_PADDING * 2, height - 88);
        drawPanel(context, previewX(), 44, PREVIEW_WIDTH, height - 88);
    }

    private void drawStaticLabels(DrawContext context) {
        context.drawText(textRenderer, PlayClock12111Localization.string(PlayClockTranslationKeys.CONFIG_TITLE), 28, 20, TITLE_COLOR, true);
        context.drawText(textRenderer, sectionLabel(section), contentX(), 56, VALUE_COLOR, true);
        if (section == Section.ADVANCED) {
            drawWrappedText(context, PlayClock12111Localization.string(PlayClockTranslationKeys.CONFIG_CONTROLS_HINT), contentX(), 84, contentWidth(), LABEL_COLOR);
        }
        if (section == Section.COLORS) {
            context.drawText(textRenderer, PlayClock12111Localization.string(PlayClockTranslationKeys.CONFIG_COLOR_TARGET), contentX(), 128, LABEL_COLOR, false);
            renderColorEditor(context);
        }
    }

    private void renderColorEditor(DrawContext context) {
        ensureColorTextures();
        int paletteX = colorPaletteX();
        int paletteY = colorEditorY();
        int hueY = colorHueY();
        int controlWidth = contentWidth() - PANEL_PADDING * 2;
        int fieldWidth = (controlWidth - 12) / 4;
        int fieldY = hueY + HUE_BAR_HEIGHT + 22;

        context.drawText(textRenderer, PlayClock12111Localization.string(PlayClockTranslationKeys.CONFIG_RED), paletteX, fieldY - 12, LABEL_COLOR, false);
        context.drawText(textRenderer, PlayClock12111Localization.string(PlayClockTranslationKeys.CONFIG_GREEN), paletteX + fieldWidth + 4, fieldY - 12, LABEL_COLOR, false);
        context.drawText(textRenderer, PlayClock12111Localization.string(PlayClockTranslationKeys.CONFIG_BLUE), paletteX + (fieldWidth + 4) * 2, fieldY - 12, LABEL_COLOR, false);
        context.drawText(textRenderer, PlayClock12111Localization.string(PlayClockTranslationKeys.CONFIG_HEX), paletteX + (fieldWidth + 4) * 3, fieldY - 12, LABEL_COLOR, false);

        context.drawTexture(RenderPipelines.GUI_TEXTURED, COLOR_PALETTE_TEXTURE_ID, paletteX, paletteY, 0.0f, 0.0f, COLOR_PALETTE_WIDTH, COLOR_PALETTE_HEIGHT, COLOR_PALETTE_WIDTH, COLOR_PALETTE_HEIGHT);
        drawBorder(context, paletteX, paletteY, COLOR_PALETTE_WIDTH, COLOR_PALETTE_HEIGHT, PANEL_BORDER);

        int markerX = paletteX + Math.round(colorSaturation * (COLOR_PALETTE_WIDTH - 1));
        int markerY = paletteY + Math.round((1.0f - colorValue) * (COLOR_PALETTE_HEIGHT - 1));
        drawBorder(context, markerX - 2, markerY - 2, 5, 5, 0xFFFFFFFF);

        context.drawTexture(RenderPipelines.GUI_TEXTURED, HUE_BAR_TEXTURE_ID, paletteX, hueY, 0.0f, 0.0f, COLOR_PALETTE_WIDTH, HUE_BAR_HEIGHT, COLOR_PALETTE_WIDTH, HUE_BAR_HEIGHT);
        drawBorder(context, paletteX, hueY, COLOR_PALETTE_WIDTH, HUE_BAR_HEIGHT, PANEL_BORDER);

        int hueMarkerX = paletteX + Math.round(colorHue * (COLOR_PALETTE_WIDTH - 1));
        drawBorder(context, hueMarkerX - 1, hueY - 1, 3, HUE_BAR_HEIGHT + 2, 0xFFFFFFFF);

        int swatchX = paletteX + COLOR_PALETTE_WIDTH + 12;
        int swatchY = paletteY;
        int swatchColor = draft.effectiveColor(colorSlot);
        context.drawText(textRenderer, colorTargetValueLabel(), swatchX, swatchY, LABEL_COLOR, false);
        context.fill(swatchX, swatchY + 14, swatchX + SWATCH_SIZE, swatchY + 14 + SWATCH_SIZE, swatchColor);
        drawBorder(context, swatchX, swatchY + 14, SWATCH_SIZE, SWATCH_SIZE, PANEL_BORDER);
        context.drawText(textRenderer, PlayClockColorCodec.toHexRgb(swatchColor), swatchX, swatchY + 14 + SWATCH_SIZE + 6, VALUE_COLOR, false);
    }

    private void drawPreview(DrawContext context) {
        int x = previewX() + 12;
        int y = 56;
        PlayClockColorPalette colors = draft.effectiveColors();

        context.drawText(textRenderer, PlayClock12111Localization.string(PlayClockTranslationKeys.CONFIG_PREVIEW), x, y, TITLE_COLOR, true);
        y += 18;

        if (draft.showHeaderSummary()) {
            drawLabelValueLine(context, x, y, PlayClock12111Localization.string(PlayClockTranslationKeys.LABEL_TOTAL_PLAYED_ON_WORLDS) + ": ", "2h 25m", colors.headerLabelColor(), colors.headerValueColor());
            y += 16;
        }

        drawHudPreview(context, x, y, colors);
        y += 54;

        if (draft.badgeEnabled()) {
            drawLabelValueLine(context, x, y, PlayClock12111Localization.string(PlayClockTranslationKeys.LABEL_TODAY_BADGE) + ": ", "18m", colors.markerLabelColor(), colors.markerValueColor());
            y += 18;
        }

        if (draft.tooltipsEnabled()) {
            int tooltipX = x;
            int tooltipY = y;
            int tooltipW = PREVIEW_WIDTH - 24;
            int tooltipH = 74;
            context.fill(tooltipX, tooltipY, tooltipX + tooltipW, tooltipY + tooltipH, 0xF0100010);
            drawBorder(context, tooltipX, tooltipY, tooltipW, tooltipH, 0xFF2E004C);
            context.drawText(textRenderer, "best.blossomcraft.org", tooltipX + 8, tooltipY + 6, colors.tooltipTitleColor(), false);
            drawTooltipRow(context, tooltipX + 8, tooltipY + 22, PlayClockTranslationKeys.LABEL_TOTAL, "02:07", colors);
            drawTooltipRow(context, tooltipX + 8, tooltipY + 34, PlayClockTranslationKeys.LABEL_SESSION, "02:07", colors);
            drawTooltipRow(context, tooltipX + 8, tooltipY + 46, PlayClockTranslationKeys.LABEL_LAST_PLAYED, "Today 2:02 AM", colors);
        }
    }

    private void drawHudPreview(DrawContext context, int x, int y, PlayClockColorPalette colors) {
        context.drawText(textRenderer, "best.blossomcraft.org", x, y, colors.hudValueColor(), true);
        y += 12;
        if ("minimal".equals(draft.hudVariant())) {
            drawLabelValueLine(context, x, y, PlayClock12111Localization.string(PlayClockTranslationKeys.LABEL_TOTAL) + ": ", "2h 25m", colors.hudLabelColor(), colors.hudValueColor());
            return;
        }

        if ("compact".equals(draft.hudVariant())) {
            context.drawText(textRenderer, metricPreviewLine(), x, y, colors.hudLabelColor(), true);
            return;
        }

        drawLabelValueLine(context, x, y, PlayClock12111Localization.string(PlayClockTranslationKeys.LABEL_TOTAL) + ": ", "2h 25m", colors.hudLabelColor(), colors.hudValueColor());
        y += 12;
        if (draft.showTodayInHud()) {
            drawLabelValueLine(context, x, y, PlayClock12111Localization.string(PlayClockTranslationKeys.LABEL_TODAY) + ": ", "18m", colors.hudLabelColor(), colors.hudValueColor());
            y += 12;
        }
        if (draft.showSessionInHud()) {
            drawLabelValueLine(context, x, y, PlayClock12111Localization.string(PlayClockTranslationKeys.LABEL_SESSION) + ": ", "00:42", colors.hudLabelColor(), colors.hudValueColor());
        }
    }

    private String metricPreviewLine() {
        StringBuilder builder = new StringBuilder();
        builder.append(PlayClock12111Localization.string(PlayClockTranslationKeys.LABEL_TOTAL)).append(": 2h 25m");
        if (draft.showTodayInHud()) {
            builder.append("  ").append(PlayClock12111Localization.string(PlayClockTranslationKeys.LABEL_TODAY)).append(": 18m");
        }
        if (draft.showSessionInHud()) {
            builder.append("  ").append(PlayClock12111Localization.string(PlayClockTranslationKeys.LABEL_SESSION)).append(": 00:42");
        }
        return builder.toString();
    }

    private void drawTooltipRow(DrawContext context, int x, int y, String key, String value, PlayClockColorPalette colors) {
        drawLabelValueLine(context, x, y, PlayClock12111Localization.string(key) + " ", value, colors.tooltipLabelColor(), colors.tooltipValueColor());
    }

    private void drawLabelValueLine(DrawContext context, int x, int y, String label, String value, int labelColor, int valueColor) {
        context.drawText(textRenderer, label, x, y, labelColor, true);
        context.drawText(textRenderer, value, x + textRenderer.getWidth(label), y, valueColor, true);
    }

    private void drawWrappedText(DrawContext context, String text, int x, int y, int maxWidth, int color) {
        List<String> lines = wrapText(text, maxWidth);
        for (int i = 0; i < lines.size(); i++) {
            context.drawText(textRenderer, lines.get(i), x, y + i * 10, color, false);
        }
    }

    private void drawPanel(DrawContext context, int x, int y, int width, int height) {
        context.fill(x, y, x + width, y + height, PANEL_BG);
        context.fill(x + 1, y + 1, x + width - 1, y + 20, PANEL_INNER);
        drawBorder(context, x, y, width, height, PANEL_BORDER);
    }

    private void drawBorder(DrawContext context, int x, int y, int width, int height, int color) {
        context.drawHorizontalLine(x, x + width - 1, y, color);
        context.drawHorizontalLine(x, x + width - 1, y + height - 1, color);
        context.drawVerticalLine(x, y, y + height - 1, color);
        context.drawVerticalLine(x + width - 1, y, y + height - 1, color);
    }

    private List<String> wrapText(String text, int maxWidth) {
        List<String> lines = new java.util.ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            String candidate = line.isEmpty() ? word : line + " " + word;
            if (textRenderer.getWidth(candidate) > maxWidth && !line.isEmpty()) {
                lines.add(line.toString());
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(candidate);
            }
        }
        if (!line.isEmpty()) {
            lines.add(line.toString());
        }
        return lines;
    }

    private int contentX() {
        return 20 + NAV_WIDTH + 20;
    }

    private int previewX() {
        return width - PREVIEW_WIDTH - 24;
    }

    private int colorPaletteX() {
        return contentX();
    }

    private int colorEditorY() {
        return 218;
    }

    private int colorHueY() {
        return colorEditorY() + COLOR_PALETTE_HEIGHT + 10;
    }

    private int contentWidth() {
        return previewX() - contentX() - 20;
    }

    private String sectionLabel(Section value) {
        return switch (value) {
            case GENERAL -> PlayClock12111Localization.string(PlayClockTranslationKeys.CONFIG_SECTION_GENERAL);
            case HUD -> PlayClock12111Localization.string(PlayClockTranslationKeys.CONFIG_SECTION_HUD);
            case COLORS -> PlayClock12111Localization.string(PlayClockTranslationKeys.CONFIG_SECTION_COLORS);
            case ADVANCED -> PlayClock12111Localization.string(PlayClockTranslationKeys.CONFIG_SECTION_ADVANCED);
        };
    }

    private String toggleLabel(String labelKey, boolean enabled) {
        return PlayClock12111Localization.string(labelKey) + ": " + (enabled ? yesLabel() : noLabel());
    }

    private String cycleLabel(String labelKey, String value) {
        return PlayClock12111Localization.string(labelKey) + ": " + value;
    }

    private String yesLabel() {
        return PlayClock12111Localization.string(PlayClockTranslationKeys.OPTION_YES);
    }

    private String noLabel() {
        return PlayClock12111Localization.string(PlayClockTranslationKeys.OPTION_NO);
    }

    private String languageValueLabel() {
        return switch (draft.preferredLanguage()) {
            case "en_us" -> PlayClock12111Localization.string(PlayClockTranslationKeys.OPTION_LANGUAGE_ENGLISH);
            case "ru_ru" -> PlayClock12111Localization.string(PlayClockTranslationKeys.OPTION_LANGUAGE_RUSSIAN);
            case "uk_ua" -> PlayClock12111Localization.string(PlayClockTranslationKeys.OPTION_LANGUAGE_UKRAINIAN);
            default -> PlayClock12111Localization.string(PlayClockTranslationKeys.OPTION_LANGUAGE_AUTO);
        };
    }

    private String hudPositionValueLabel() {
        return switch (draft.hudAnchor()) {
            case "top_right" -> PlayClock12111Localization.string(PlayClockTranslationKeys.OPTION_HUD_POSITION_TOP_RIGHT);
            case "bottom_left" -> PlayClock12111Localization.string(PlayClockTranslationKeys.OPTION_HUD_POSITION_BOTTOM_LEFT);
            case "bottom_right" -> PlayClock12111Localization.string(PlayClockTranslationKeys.OPTION_HUD_POSITION_BOTTOM_RIGHT);
            default -> PlayClock12111Localization.string(PlayClockTranslationKeys.OPTION_HUD_POSITION_TOP_LEFT);
        };
    }

    private String timeFormatValueLabel() {
        return switch (draft.timeFormat()) {
            case "clock" -> PlayClock12111Localization.string(PlayClockTranslationKeys.OPTION_TIME_FORMAT_CLOCK);
            case "localized" -> PlayClock12111Localization.string(PlayClockTranslationKeys.OPTION_TIME_FORMAT_LOCALIZED);
            default -> PlayClock12111Localization.string(PlayClockTranslationKeys.OPTION_TIME_FORMAT_COMPACT);
        };
    }

    private String hudVariantValueLabel() {
        return switch (draft.hudVariant()) {
            case "stacked" -> PlayClock12111Localization.string(PlayClockTranslationKeys.OPTION_HUD_VARIANT_STACKED);
            case "minimal" -> PlayClock12111Localization.string(PlayClockTranslationKeys.OPTION_HUD_VARIANT_MINIMAL);
            default -> PlayClock12111Localization.string(PlayClockTranslationKeys.OPTION_HUD_VARIANT_COMPACT);
        };
    }

    private String colorModeValueLabel() {
        return "custom".equals(draft.colorMode())
                ? PlayClock12111Localization.string(PlayClockTranslationKeys.OPTION_COLOR_MODE_CUSTOM)
                : PlayClock12111Localization.string(PlayClockTranslationKeys.OPTION_COLOR_MODE_PRESET);
    }

    private String colorPresetValueLabel() {
        return switch (draft.colorPreset()) {
            case "grass" -> PlayClock12111Localization.string(PlayClockTranslationKeys.OPTION_COLOR_PRESET_GRASS);
            case "sunset" -> PlayClock12111Localization.string(PlayClockTranslationKeys.OPTION_COLOR_PRESET_SUNSET);
            default -> PlayClock12111Localization.string(PlayClockTranslationKeys.OPTION_COLOR_PRESET_VANILLA);
        };
    }

    private String colorTargetValueLabel() {
        return switch (colorSlot) {
            case HUD_LABEL -> PlayClock12111Localization.string(PlayClockTranslationKeys.OPTION_COLOR_TARGET_HUD_LABEL);
            case HUD_VALUE -> PlayClock12111Localization.string(PlayClockTranslationKeys.OPTION_COLOR_TARGET_HUD_VALUE);
            case MARKER_LABEL -> PlayClock12111Localization.string(PlayClockTranslationKeys.OPTION_COLOR_TARGET_MARKER_LABEL);
            case MARKER_VALUE -> PlayClock12111Localization.string(PlayClockTranslationKeys.OPTION_COLOR_TARGET_MARKER_VALUE);
            case TOOLTIP_TITLE -> PlayClock12111Localization.string(PlayClockTranslationKeys.OPTION_COLOR_TARGET_TOOLTIP_TITLE);
            case TOOLTIP_LABEL -> PlayClock12111Localization.string(PlayClockTranslationKeys.OPTION_COLOR_TARGET_TOOLTIP_LABEL);
            case TOOLTIP_VALUE -> PlayClock12111Localization.string(PlayClockTranslationKeys.OPTION_COLOR_TARGET_TOOLTIP_VALUE);
            case HEADER_LABEL -> PlayClock12111Localization.string(PlayClockTranslationKeys.OPTION_COLOR_TARGET_HEADER_LABEL);
            case HEADER_VALUE -> PlayClock12111Localization.string(PlayClockTranslationKeys.OPTION_COLOR_TARGET_HEADER_VALUE);
        };
    }

    private String colorTargetButtonLabel(PlayClockColorSlot slot) {
        String label = switch (slot) {
            case HUD_LABEL -> PlayClock12111Localization.string(PlayClockTranslationKeys.OPTION_COLOR_TARGET_HUD_LABEL);
            case HUD_VALUE -> PlayClock12111Localization.string(PlayClockTranslationKeys.OPTION_COLOR_TARGET_HUD_VALUE);
            case MARKER_LABEL -> PlayClock12111Localization.string(PlayClockTranslationKeys.OPTION_COLOR_TARGET_MARKER_LABEL);
            case MARKER_VALUE -> PlayClock12111Localization.string(PlayClockTranslationKeys.OPTION_COLOR_TARGET_MARKER_VALUE);
            case TOOLTIP_TITLE -> PlayClock12111Localization.string(PlayClockTranslationKeys.OPTION_COLOR_TARGET_TOOLTIP_TITLE);
            case TOOLTIP_LABEL -> PlayClock12111Localization.string(PlayClockTranslationKeys.OPTION_COLOR_TARGET_TOOLTIP_LABEL);
            case TOOLTIP_VALUE -> PlayClock12111Localization.string(PlayClockTranslationKeys.OPTION_COLOR_TARGET_TOOLTIP_VALUE);
            case HEADER_LABEL -> PlayClock12111Localization.string(PlayClockTranslationKeys.OPTION_COLOR_TARGET_HEADER_LABEL);
            case HEADER_VALUE -> PlayClock12111Localization.string(PlayClockTranslationKeys.OPTION_COLOR_TARGET_HEADER_VALUE);
        };
        return (slot == colorSlot ? "> " : "") + label;
    }

    private void refreshColorTargetButtons() {
        for (int i = 0; i < colorTargetButtons.size(); i++) {
            PlayClockColorSlot slot = PlayClockColorSlot.values()[i];
            ButtonWidget button = colorTargetButtons.get(i);
            button.setMessage(Text.literal(colorTargetButtonLabel(slot)));
            button.active = slot != colorSlot;
        }
    }

    private void ensureColorTextures() {
        if (client == null) {
            return;
        }

        if (paletteTexture == null) {
            paletteTexture = new NativeImageBackedTexture("playclock_config_palette_12111", COLOR_PALETTE_WIDTH, COLOR_PALETTE_HEIGHT, false);
            client.getTextureManager().registerTexture(COLOR_PALETTE_TEXTURE_ID, paletteTexture);
            paletteTextureDirty = true;
        }
        if (hueTexture == null) {
            hueTexture = new NativeImageBackedTexture("playclock_config_hue_12111", COLOR_PALETTE_WIDTH, HUE_BAR_HEIGHT, false);
            client.getTextureManager().registerTexture(HUE_BAR_TEXTURE_ID, hueTexture);
            hueTextureDirty = true;
        }
        if (paletteTextureDirty) {
            refreshPaletteTexture();
        }
        if (hueTextureDirty) {
            refreshHueTexture();
        }
    }

    private void refreshPaletteTexture() {
        NativeImage image = paletteTexture.getImage();
        if (image == null) {
            return;
        }
        for (int x = 0; x < COLOR_PALETTE_WIDTH; x++) {
            float saturation = x / (float) Math.max(1, COLOR_PALETTE_WIDTH - 1);
            for (int y = 0; y < COLOR_PALETTE_HEIGHT; y++) {
                float value = 1.0f - (y / (float) Math.max(1, COLOR_PALETTE_HEIGHT - 1));
                image.setColorArgb(x, y, PlayClockColorCodec.fromHsv(colorHue, saturation, value));
            }
        }
        paletteTexture.upload();
        paletteTextureDirty = false;
    }

    private void refreshHueTexture() {
        NativeImage image = hueTexture.getImage();
        if (image == null) {
            return;
        }
        for (int x = 0; x < COLOR_PALETTE_WIDTH; x++) {
            int color = PlayClockColorCodec.fromHsv(x / (float) Math.max(1, COLOR_PALETTE_WIDTH - 1), 1.0f, 1.0f);
            for (int y = 0; y < HUE_BAR_HEIGHT; y++) {
                image.setColorArgb(x, y, color);
            }
        }
        hueTexture.upload();
        hueTextureDirty = false;
    }

    private void releaseColorTextures() {
        if (client != null) {
            client.getTextureManager().destroyTexture(COLOR_PALETTE_TEXTURE_ID);
            client.getTextureManager().destroyTexture(HUE_BAR_TEXTURE_ID);
        }
        if (paletteTexture != null) {
            paletteTexture.close();
            paletteTexture = null;
        }
        if (hueTexture != null) {
            hueTexture.close();
            hueTexture = null;
        }
        paletteTextureDirty = true;
        hueTextureDirty = true;
    }

    private static boolean contains(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private static float clamp01(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }

    private enum Section {
        GENERAL,
        HUD,
        COLORS,
        ADVANCED
    }
}
