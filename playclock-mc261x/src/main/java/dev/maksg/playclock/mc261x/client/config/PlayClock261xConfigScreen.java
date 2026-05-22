package dev.maksg.playclock.mc261x.client.config;

import com.mojang.blaze3d.platform.NativeImage;
import dev.maksg.playclock.core.config.PlayClockColorCodec;
import dev.maksg.playclock.core.config.PlayClockColorPalette;
import dev.maksg.playclock.core.config.PlayClockColorSlot;
import dev.maksg.playclock.core.config.PlayClockConfigDraft;
import dev.maksg.playclock.core.ui.PlayClockTranslationKeys;
import dev.maksg.playclock.mc261x.client.PlayClock261xClient;
import dev.maksg.playclock.mc261x.client.PlayClock261xLocalization;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

@SuppressWarnings("null")
public final class PlayClock261xConfigScreen extends Screen {
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
    private static final Identifier COLOR_PALETTE_TEXTURE_ID = Identifier.fromNamespaceAndPath("playclock", "config/color_palette_261x");
    private static final Identifier HUE_BAR_TEXTURE_ID = Identifier.fromNamespaceAndPath("playclock", "config/color_hue_261x");

    private final Screen parent;
    private final PlayClockConfigDraft draft;
    private Section section = Section.GENERAL;
    private PlayClockColorSlot colorSlot = PlayClockColorSlot.HUD_VALUE;

    private EditBox redField;
    private EditBox greenField;
    private EditBox blueField;
    private EditBox hexField;
    private float colorHue;
    private float colorSaturation;
    private float colorValue;
    private boolean draggingPalette;
    private boolean draggingHue;
    private final List<Button> colorTargetButtons = new ArrayList<>();
    private DynamicTexture paletteTexture;
    private DynamicTexture hueTexture;
    private boolean paletteTextureDirty = true;
    private boolean hueTextureDirty = true;

    @FunctionalInterface
    private interface BooleanSetter {
        void accept(boolean value);
    }

    public PlayClock261xConfigScreen(Screen parent) {
        super(PlayClock261xLocalization.component(PlayClockTranslationKeys.CONFIG_TITLE));
        this.parent = parent;
        this.draft = PlayClockConfigDraft.from(PlayClock261xClient.runtimeService().snapshot().config());
        syncPickerFromDraft();
    }

    @Override
    protected void init() {
        clearWidgets();
        addSectionButtons();
        addSectionContent();
        addFooterButtons();
        syncColorInputsFromDraft();
    }

    @Override
    public void onClose() {
        releaseColorTextures();
        if (minecraft != null) {
            minecraft.setScreen(parent);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(0, 0, width, height, OUTER_BG);
        drawPanels(guiGraphics);
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
        drawStaticLabels(guiGraphics);
        drawPreview(guiGraphics);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();
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
        return super.mouseClicked(event, bl);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();
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
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0) {
            draggingPalette = false;
            draggingHue = false;
        }
        return super.mouseReleased(event);
    }

    private void addSectionButtons() {
        int navX = 24;
        int navY = 52;
        int navButtonWidth = NAV_WIDTH - 16;

        for (int i = 0; i < Section.values().length; i++) {
            Section item = Section.values()[i];
            int y = navY + i * 24;
            addRenderableWidget(Button.builder(Component.literal(sectionLabel(item)), button -> switchSection(item))
                    .bounds(navX + 8, y, navButtonWidth, CONTROL_HEIGHT)
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
            case ADVANCED -> {
                addToggleButton(baseX, y + CONTROL_GAP * 2, controlWidth, PlayClockTranslationKeys.CONFIG_SHOW_HEADER_SUMMARY, draft.showHeaderSummary(), draft::setShowHeaderSummary);
            }
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
        redField = addColorField(baseX, fieldY, fieldWidth, PlayClockTranslationKeys.CONFIG_RED);
        greenField = addColorField(baseX + fieldWidth + 4, fieldY, fieldWidth, PlayClockTranslationKeys.CONFIG_GREEN);
        blueField = addColorField(baseX + (fieldWidth + 4) * 2, fieldY, fieldWidth, PlayClockTranslationKeys.CONFIG_BLUE);
        hexField = addColorField(baseX + (fieldWidth + 4) * 3, fieldY, fieldWidth, PlayClockTranslationKeys.CONFIG_HEX);
        hexField.setMaxLength(7);

        addRenderableWidget(Button.builder(
                        PlayClock261xLocalization.component(PlayClockTranslationKeys.CONFIG_APPLY_COLOR),
                        button -> applyColorFields())
                .bounds(baseX, fieldY + 32, controlWidth, CONTROL_HEIGHT)
                .build());

        addRenderableWidget(Button.builder(
                        PlayClock261xLocalization.component(PlayClockTranslationKeys.CONFIG_RESET_PRESET),
                        button -> {
                            draft.usePresetColors();
                            syncPickerFromDraft();
                            syncColorInputsFromDraft();
                        })
                .bounds(baseX, fieldY + 56, controlWidth, CONTROL_HEIGHT)
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
            Button button = Button.builder(
                            Component.literal(colorTargetButtonLabel(slot)),
                            clicked -> {
                                colorSlot = slot;
                                syncPickerFromDraft();
                                syncColorInputsFromDraft();
                                refreshColorTargetButtons();
                            })
                    .bounds(x + column * (buttonWidth + gap), y + row * (CONTROL_HEIGHT + gap), buttonWidth, CONTROL_HEIGHT)
                    .build();
            colorTargetButtons.add(button);
            addRenderableWidget(button);
        }
        refreshColorTargetButtons();
    }

    private EditBox addColorField(int x, int y, int width, String translationKey) {
        EditBox field = new EditBox(font, x, y, width, CONTROL_HEIGHT, PlayClock261xLocalization.component(translationKey));
        field.setBordered(true);
        field.setMaxLength(3);
        field.setTextColor(TITLE_COLOR);
        addRenderableWidget(field);
        return field;
    }

    private void addFooterButtons() {
        int rightX = previewX();
        int footerY = height - 38;
        int buttonWidth = 132;
        addRenderableWidget(Button.builder(
                        PlayClock261xLocalization.component(PlayClockTranslationKeys.CONFIG_CANCEL),
                        button -> onClose())
                .bounds(rightX, footerY, buttonWidth, CONTROL_HEIGHT)
                .build());
        addRenderableWidget(Button.builder(
                        PlayClock261xLocalization.component(PlayClockTranslationKeys.CONFIG_DONE),
                        button -> applyAndClose())
                .bounds(rightX + PREVIEW_WIDTH - buttonWidth, footerY, buttonWidth, CONTROL_HEIGHT)
                .build());
    }

    private void switchSection(Section next) {
        if (section != next) {
            section = next;
            init();
        }
    }

    private void addToggleButton(
            int x,
            int y,
            int width,
            String labelKey,
            boolean initialValue,
            BooleanSetter consumer) {
        addRenderableWidget(Button.builder(Component.literal(toggleLabel(labelKey, initialValue)), button -> {
                    boolean nextValue = !button.getMessage().getString().endsWith(yesLabel());
                    consumer.accept(nextValue);
                    button.setMessage(Component.literal(toggleLabel(labelKey, nextValue)));
                })
                .bounds(x, y, width, CONTROL_HEIGHT)
                .build());
    }

    private void addCycleButton(int x, int y, int width, String labelKey, java.util.function.Supplier<String> valueSupplier, Runnable action) {
        addRenderableWidget(Button.builder(Component.literal(cycleLabel(labelKey, valueSupplier.get())), button -> {
                    action.run();
                    button.setMessage(Component.literal(cycleLabel(labelKey, valueSupplier.get())));
                })
                .bounds(x, y, width, CONTROL_HEIGHT)
                .build());
    }

    private void applyAndClose() {
        PlayClock261xClient.runtimeService().updateConfig(draft.toConfig());
        onClose();
    }

    private void applyColorFields() {
        int fallback = draft.effectiveColor(colorSlot);
        int red = PlayClockColorCodec.parseRgbChannel(redField.getValue(), PlayClockColorCodec.red(fallback));
        int green = PlayClockColorCodec.parseRgbChannel(greenField.getValue(), PlayClockColorCodec.green(fallback));
        int blue = PlayClockColorCodec.parseRgbChannel(blueField.getValue(), PlayClockColorCodec.blue(fallback));
        int color = PlayClockColorCodec.fromRgb(red, green, blue);
        color = PlayClockColorCodec.parseHexRgb(hexField.getValue(), color);
        draft.setColor(colorSlot, color);
        draft.setColorMode("custom");
        syncPickerFromDraft();
        syncColorInputsFromDraft();
    }

    private void syncColorInputsFromDraft() {
        int color = draft.effectiveColor(colorSlot);
        if (redField != null) {
            redField.setValue(Integer.toString(PlayClockColorCodec.red(color)));
            greenField.setValue(Integer.toString(PlayClockColorCodec.green(color)));
            blueField.setValue(Integer.toString(PlayClockColorCodec.blue(color)));
            hexField.setValue(PlayClockColorCodec.toHexRgb(color));
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

    private void drawPanels(GuiGraphicsExtractor guiGraphics) {
        drawPanel(guiGraphics, 20, 44, NAV_WIDTH, height - 88);
        drawPanel(guiGraphics, contentX() - PANEL_PADDING, 44, contentWidth() + PANEL_PADDING * 2, height - 88);
        drawPanel(guiGraphics, previewX(), 44, PREVIEW_WIDTH, height - 88);
    }

    private void drawStaticLabels(GuiGraphicsExtractor guiGraphics) {
        String titleText = PlayClock261xLocalization.string(PlayClockTranslationKeys.CONFIG_TITLE);
        guiGraphics.text(font, titleText, 28, 20, TITLE_COLOR, true);
        guiGraphics.text(font, sectionLabel(section), contentX(), 56, VALUE_COLOR, true);
        if (section == Section.ADVANCED) {
            drawWrappedText(guiGraphics, PlayClock261xLocalization.string(PlayClockTranslationKeys.CONFIG_CONTROLS_HINT), contentX(), 84, contentWidth(), LABEL_COLOR);
        }
        if (section == Section.COLORS) {
            guiGraphics.text(font, PlayClock261xLocalization.string(PlayClockTranslationKeys.CONFIG_COLOR_TARGET), contentX(), 128, LABEL_COLOR, false);
            renderColorEditor(guiGraphics);
        }
    }

    private void renderColorEditor(GuiGraphicsExtractor guiGraphics) {
        ensureColorTextures();
        int paletteX = colorPaletteX();
        int paletteY = colorEditorY();
        int hueY = colorHueY();
        int controlWidth = contentWidth() - PANEL_PADDING * 2;
        int fieldWidth = (controlWidth - 12) / 4;
        int fieldY = hueY + HUE_BAR_HEIGHT + 22;

        guiGraphics.text(font, PlayClock261xLocalization.string(PlayClockTranslationKeys.CONFIG_RED), paletteX, fieldY - 12, LABEL_COLOR, false);
        guiGraphics.text(font, PlayClock261xLocalization.string(PlayClockTranslationKeys.CONFIG_GREEN), paletteX + fieldWidth + 4, fieldY - 12, LABEL_COLOR, false);
        guiGraphics.text(font, PlayClock261xLocalization.string(PlayClockTranslationKeys.CONFIG_BLUE), paletteX + (fieldWidth + 4) * 2, fieldY - 12, LABEL_COLOR, false);
        guiGraphics.text(font, PlayClock261xLocalization.string(PlayClockTranslationKeys.CONFIG_HEX), paletteX + (fieldWidth + 4) * 3, fieldY - 12, LABEL_COLOR, false);

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, COLOR_PALETTE_TEXTURE_ID, paletteX, paletteY, 0.0f, 0.0f, COLOR_PALETTE_WIDTH, COLOR_PALETTE_HEIGHT, COLOR_PALETTE_WIDTH, COLOR_PALETTE_HEIGHT);
        guiGraphics.outline(paletteX, paletteY, COLOR_PALETTE_WIDTH, COLOR_PALETTE_HEIGHT, PANEL_BORDER);

        int markerX = paletteX + Math.round(colorSaturation * (COLOR_PALETTE_WIDTH - 1));
        int markerY = paletteY + Math.round((1.0f - colorValue) * (COLOR_PALETTE_HEIGHT - 1));
        guiGraphics.outline(markerX - 2, markerY - 2, 5, 5, 0xFFFFFFFF);

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, HUE_BAR_TEXTURE_ID, paletteX, hueY, 0.0f, 0.0f, COLOR_PALETTE_WIDTH, HUE_BAR_HEIGHT, COLOR_PALETTE_WIDTH, HUE_BAR_HEIGHT);
        guiGraphics.outline(paletteX, hueY, COLOR_PALETTE_WIDTH, HUE_BAR_HEIGHT, PANEL_BORDER);
        int hueMarkerX = paletteX + Math.round(colorHue * (COLOR_PALETTE_WIDTH - 1));
        guiGraphics.outline(hueMarkerX - 1, hueY - 1, 3, HUE_BAR_HEIGHT + 2, 0xFFFFFFFF);

        int swatchX = paletteX + COLOR_PALETTE_WIDTH + 12;
        int swatchY = paletteY;
        int swatchColor = draft.effectiveColor(colorSlot);
        guiGraphics.text(font, colorTargetValueLabel(), swatchX, swatchY, LABEL_COLOR, false);
        guiGraphics.fill(swatchX, swatchY + 14, swatchX + SWATCH_SIZE, swatchY + 14 + SWATCH_SIZE, swatchColor);
        guiGraphics.outline(swatchX, swatchY + 14, SWATCH_SIZE, SWATCH_SIZE, PANEL_BORDER);
        guiGraphics.text(font, PlayClockColorCodec.toHexRgb(swatchColor), swatchX, swatchY + 14 + SWATCH_SIZE + 6, VALUE_COLOR, false);
    }

    private void drawPreview(GuiGraphicsExtractor guiGraphics) {
        int x = previewX() + 12;
        int y = 56;
        PlayClockColorPalette colors = draft.effectiveColors();

        guiGraphics.text(font, PlayClock261xLocalization.string(PlayClockTranslationKeys.CONFIG_PREVIEW), x, y, TITLE_COLOR, true);
        y += 18;

        if (draft.showHeaderSummary()) {
            drawLabelValueLine(
                    guiGraphics,
                    x,
                    y,
                    PlayClock261xLocalization.string(PlayClockTranslationKeys.LABEL_TOTAL_PLAYED_ON_SERVERS) + ": ",
                    "2h 25m",
                    colors.headerLabelColor(),
                    colors.headerValueColor());
            y += 16;
        }

        drawHudPreview(guiGraphics, x, y, colors);
        y += 54;

        if (draft.badgeEnabled()) {
            drawLabelValueLine(
                    guiGraphics,
                    x,
                    y,
                    PlayClock261xLocalization.string(PlayClockTranslationKeys.LABEL_TODAY_BADGE) + ": ",
                    "18m",
                    colors.markerLabelColor(),
                    colors.markerValueColor());
            y += 18;
        }

        if (draft.tooltipsEnabled()) {
            int tooltipX = x;
            int tooltipY = y;
            int tooltipW = PREVIEW_WIDTH - 24;
            int tooltipH = 74;
            guiGraphics.fill(tooltipX, tooltipY, tooltipX + tooltipW, tooltipY + tooltipH, 0xF0100010);
            guiGraphics.outline(tooltipX, tooltipY, tooltipW, tooltipH, 0xFF2E004C);
            guiGraphics.text(font, "best.blossomcraft.org", tooltipX + 8, tooltipY + 6, colors.tooltipTitleColor(), false);
            drawTooltipRow(guiGraphics, tooltipX + 8, tooltipY + 22, PlayClockTranslationKeys.LABEL_TOTAL, "02:07", colors);
            drawTooltipRow(guiGraphics, tooltipX + 8, tooltipY + 34, PlayClockTranslationKeys.LABEL_SESSION, "02:07", colors);
            drawTooltipRow(guiGraphics, tooltipX + 8, tooltipY + 46, PlayClockTranslationKeys.LABEL_LAST_PLAYED, "Today 2:02 AM", colors);
        }
    }

    private void drawHudPreview(GuiGraphicsExtractor guiGraphics, int x, int y, PlayClockColorPalette colors) {
        guiGraphics.text(font, "best.blossomcraft.org", x, y, colors.hudValueColor(), true);
        y += 12;
        if ("minimal".equals(draft.hudVariant())) {
            drawLabelValueLine(guiGraphics, x, y, PlayClock261xLocalization.string(PlayClockTranslationKeys.LABEL_TOTAL) + ": ", "2h 25m", colors.hudLabelColor(), colors.hudValueColor());
            return;
        }

        if ("compact".equals(draft.hudVariant())) {
            String line = metricPreviewLine();
            guiGraphics.text(font, line, x, y, colors.hudLabelColor(), true);
            return;
        }

        drawLabelValueLine(guiGraphics, x, y, PlayClock261xLocalization.string(PlayClockTranslationKeys.LABEL_TOTAL) + ": ", "2h 25m", colors.hudLabelColor(), colors.hudValueColor());
        y += 12;
        if (draft.showTodayInHud()) {
            drawLabelValueLine(guiGraphics, x, y, PlayClock261xLocalization.string(PlayClockTranslationKeys.LABEL_TODAY) + ": ", "18m", colors.hudLabelColor(), colors.hudValueColor());
            y += 12;
        }
        if (draft.showSessionInHud()) {
            drawLabelValueLine(guiGraphics, x, y, PlayClock261xLocalization.string(PlayClockTranslationKeys.LABEL_SESSION) + ": ", "00:42", colors.hudLabelColor(), colors.hudValueColor());
        }
    }

    private String metricPreviewLine() {
        StringBuilder builder = new StringBuilder();
        builder.append(PlayClock261xLocalization.string(PlayClockTranslationKeys.LABEL_TOTAL)).append(": 2h 25m");
        if (draft.showTodayInHud()) {
            builder.append("  ").append(PlayClock261xLocalization.string(PlayClockTranslationKeys.LABEL_TODAY)).append(": 18m");
        }
        if (draft.showSessionInHud()) {
            builder.append("  ").append(PlayClock261xLocalization.string(PlayClockTranslationKeys.LABEL_SESSION)).append(": 00:42");
        }
        return builder.toString();
    }

    private void drawTooltipRow(GuiGraphicsExtractor guiGraphics, int x, int y, String key, String value, PlayClockColorPalette colors) {
        drawLabelValueLine(guiGraphics, x, y, PlayClock261xLocalization.string(key) + " ", value, colors.tooltipLabelColor(), colors.tooltipValueColor());
    }

    private void drawLabelValueLine(GuiGraphicsExtractor guiGraphics, int x, int y, String label, String value, int labelColor, int valueColor) {
        guiGraphics.text(font, label, x, y, labelColor, true);
        guiGraphics.text(font, value, x + font.width(label), y, valueColor, true);
    }

    private void drawWrappedText(GuiGraphicsExtractor guiGraphics, String text, int x, int y, int maxWidth, int color) {
        List<String> lines = wrapText(text, maxWidth);
        for (int i = 0; i < lines.size(); i++) {
            guiGraphics.text(font, lines.get(i), x, y + i * 10, color, false);
        }
    }

    private List<String> wrapText(String text, int maxWidth) {
        List<String> lines = new java.util.ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            String candidate = line.isEmpty() ? word : line + " " + word;
            if (font.width(candidate) > maxWidth && !line.isEmpty()) {
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

    private void drawPanel(GuiGraphicsExtractor guiGraphics, int x, int y, int width, int height) {
        guiGraphics.fill(x, y, x + width, y + height, PANEL_BG);
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + 20, PANEL_INNER);
        guiGraphics.outline(x, y, width, height, PANEL_BORDER);
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
            case GENERAL -> PlayClock261xLocalization.string(PlayClockTranslationKeys.CONFIG_SECTION_GENERAL);
            case HUD -> PlayClock261xLocalization.string(PlayClockTranslationKeys.CONFIG_SECTION_HUD);
            case COLORS -> PlayClock261xLocalization.string(PlayClockTranslationKeys.CONFIG_SECTION_COLORS);
            case ADVANCED -> PlayClock261xLocalization.string(PlayClockTranslationKeys.CONFIG_SECTION_ADVANCED);
        };
    }

    private String toggleLabel(String labelKey, boolean enabled) {
        return PlayClock261xLocalization.string(labelKey) + ": " + (enabled ? yesLabel() : noLabel());
    }

    private String cycleLabel(String labelKey, String value) {
        return PlayClock261xLocalization.string(labelKey) + ": " + value;
    }

    private String yesLabel() {
        return PlayClock261xLocalization.string(PlayClockTranslationKeys.OPTION_YES);
    }

    private String noLabel() {
        return PlayClock261xLocalization.string(PlayClockTranslationKeys.OPTION_NO);
    }

    private String languageValueLabel() {
        return switch (draft.preferredLanguage()) {
            case "en_us" -> PlayClock261xLocalization.string(PlayClockTranslationKeys.OPTION_LANGUAGE_ENGLISH);
            case "ru_ru" -> PlayClock261xLocalization.string(PlayClockTranslationKeys.OPTION_LANGUAGE_RUSSIAN);
            case "uk_ua" -> PlayClock261xLocalization.string(PlayClockTranslationKeys.OPTION_LANGUAGE_UKRAINIAN);
            default -> PlayClock261xLocalization.string(PlayClockTranslationKeys.OPTION_LANGUAGE_AUTO);
        };
    }

    private String hudPositionValueLabel() {
        return switch (draft.hudAnchor()) {
            case "top_right" -> PlayClock261xLocalization.string(PlayClockTranslationKeys.OPTION_HUD_POSITION_TOP_RIGHT);
            case "bottom_left" -> PlayClock261xLocalization.string(PlayClockTranslationKeys.OPTION_HUD_POSITION_BOTTOM_LEFT);
            case "bottom_right" -> PlayClock261xLocalization.string(PlayClockTranslationKeys.OPTION_HUD_POSITION_BOTTOM_RIGHT);
            default -> PlayClock261xLocalization.string(PlayClockTranslationKeys.OPTION_HUD_POSITION_TOP_LEFT);
        };
    }

    private String timeFormatValueLabel() {
        return switch (draft.timeFormat()) {
            case "clock" -> PlayClock261xLocalization.string(PlayClockTranslationKeys.OPTION_TIME_FORMAT_CLOCK);
            case "localized" -> PlayClock261xLocalization.string(PlayClockTranslationKeys.OPTION_TIME_FORMAT_LOCALIZED);
            default -> PlayClock261xLocalization.string(PlayClockTranslationKeys.OPTION_TIME_FORMAT_COMPACT);
        };
    }

    private String hudVariantValueLabel() {
        return switch (draft.hudVariant()) {
            case "stacked" -> PlayClock261xLocalization.string(PlayClockTranslationKeys.OPTION_HUD_VARIANT_STACKED);
            case "minimal" -> PlayClock261xLocalization.string(PlayClockTranslationKeys.OPTION_HUD_VARIANT_MINIMAL);
            default -> PlayClock261xLocalization.string(PlayClockTranslationKeys.OPTION_HUD_VARIANT_COMPACT);
        };
    }

    private String colorModeValueLabel() {
        return "custom".equals(draft.colorMode())
                ? PlayClock261xLocalization.string(PlayClockTranslationKeys.OPTION_COLOR_MODE_CUSTOM)
                : PlayClock261xLocalization.string(PlayClockTranslationKeys.OPTION_COLOR_MODE_PRESET);
    }

    private String colorPresetValueLabel() {
        return switch (draft.colorPreset()) {
            case "grass" -> PlayClock261xLocalization.string(PlayClockTranslationKeys.OPTION_COLOR_PRESET_GRASS);
            case "sunset" -> PlayClock261xLocalization.string(PlayClockTranslationKeys.OPTION_COLOR_PRESET_SUNSET);
            default -> PlayClock261xLocalization.string(PlayClockTranslationKeys.OPTION_COLOR_PRESET_VANILLA);
        };
    }

    private String colorTargetValueLabel() {
        return switch (colorSlot) {
            case HUD_LABEL -> PlayClock261xLocalization.string(PlayClockTranslationKeys.OPTION_COLOR_TARGET_HUD_LABEL);
            case HUD_VALUE -> PlayClock261xLocalization.string(PlayClockTranslationKeys.OPTION_COLOR_TARGET_HUD_VALUE);
            case MARKER_LABEL -> PlayClock261xLocalization.string(PlayClockTranslationKeys.OPTION_COLOR_TARGET_MARKER_LABEL);
            case MARKER_VALUE -> PlayClock261xLocalization.string(PlayClockTranslationKeys.OPTION_COLOR_TARGET_MARKER_VALUE);
            case TOOLTIP_TITLE -> PlayClock261xLocalization.string(PlayClockTranslationKeys.OPTION_COLOR_TARGET_TOOLTIP_TITLE);
            case TOOLTIP_LABEL -> PlayClock261xLocalization.string(PlayClockTranslationKeys.OPTION_COLOR_TARGET_TOOLTIP_LABEL);
            case TOOLTIP_VALUE -> PlayClock261xLocalization.string(PlayClockTranslationKeys.OPTION_COLOR_TARGET_TOOLTIP_VALUE);
            case HEADER_LABEL -> PlayClock261xLocalization.string(PlayClockTranslationKeys.OPTION_COLOR_TARGET_HEADER_LABEL);
            case HEADER_VALUE -> PlayClock261xLocalization.string(PlayClockTranslationKeys.OPTION_COLOR_TARGET_HEADER_VALUE);
        };
    }

    private String colorTargetButtonLabel(PlayClockColorSlot slot) {
        String label = switch (slot) {
            case HUD_LABEL -> PlayClock261xLocalization.string(PlayClockTranslationKeys.OPTION_COLOR_TARGET_HUD_LABEL);
            case HUD_VALUE -> PlayClock261xLocalization.string(PlayClockTranslationKeys.OPTION_COLOR_TARGET_HUD_VALUE);
            case MARKER_LABEL -> PlayClock261xLocalization.string(PlayClockTranslationKeys.OPTION_COLOR_TARGET_MARKER_LABEL);
            case MARKER_VALUE -> PlayClock261xLocalization.string(PlayClockTranslationKeys.OPTION_COLOR_TARGET_MARKER_VALUE);
            case TOOLTIP_TITLE -> PlayClock261xLocalization.string(PlayClockTranslationKeys.OPTION_COLOR_TARGET_TOOLTIP_TITLE);
            case TOOLTIP_LABEL -> PlayClock261xLocalization.string(PlayClockTranslationKeys.OPTION_COLOR_TARGET_TOOLTIP_LABEL);
            case TOOLTIP_VALUE -> PlayClock261xLocalization.string(PlayClockTranslationKeys.OPTION_COLOR_TARGET_TOOLTIP_VALUE);
            case HEADER_LABEL -> PlayClock261xLocalization.string(PlayClockTranslationKeys.OPTION_COLOR_TARGET_HEADER_LABEL);
            case HEADER_VALUE -> PlayClock261xLocalization.string(PlayClockTranslationKeys.OPTION_COLOR_TARGET_HEADER_VALUE);
        };
        return (slot == colorSlot ? "> " : "") + label;
    }

    private void refreshColorTargetButtons() {
        for (int i = 0; i < colorTargetButtons.size(); i++) {
            PlayClockColorSlot slot = PlayClockColorSlot.values()[i];
            Button button = colorTargetButtons.get(i);
            button.setMessage(Component.literal(colorTargetButtonLabel(slot)));
            button.active = slot != colorSlot;
        }
    }

    private void ensureColorTextures() {
        var game = Objects.requireNonNull(minecraft, "Minecraft client must be available while the config screen is open");

        if (paletteTexture == null) {
            paletteTexture = new DynamicTexture(() -> "playclock_config_palette_261x", COLOR_PALETTE_WIDTH, COLOR_PALETTE_HEIGHT, false);
            game.getTextureManager().register(COLOR_PALETTE_TEXTURE_ID, paletteTexture);
            paletteTextureDirty = true;
        }
        if (hueTexture == null) {
            hueTexture = new DynamicTexture(() -> "playclock_config_hue_261x", COLOR_PALETTE_WIDTH, HUE_BAR_HEIGHT, false);
            game.getTextureManager().register(HUE_BAR_TEXTURE_ID, hueTexture);
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
        NativeImage image = Objects.requireNonNull(paletteTexture.getPixels(), "Palette texture must expose backing pixels");
        for (int x = 0; x < COLOR_PALETTE_WIDTH; x++) {
            float saturation = x / (float) Math.max(1, COLOR_PALETTE_WIDTH - 1);
            for (int y = 0; y < COLOR_PALETTE_HEIGHT; y++) {
                float value = 1.0f - (y / (float) Math.max(1, COLOR_PALETTE_HEIGHT - 1));
                image.setPixel(x, y, PlayClockColorCodec.fromHsv(colorHue, saturation, value));
            }
        }
        paletteTexture.upload();
        paletteTextureDirty = false;
    }

    private void refreshHueTexture() {
        NativeImage image = Objects.requireNonNull(hueTexture.getPixels(), "Hue texture must expose backing pixels");
        for (int x = 0; x < COLOR_PALETTE_WIDTH; x++) {
            int color = PlayClockColorCodec.fromHsv(x / (float) Math.max(1, COLOR_PALETTE_WIDTH - 1), 1.0f, 1.0f);
            for (int y = 0; y < HUE_BAR_HEIGHT; y++) {
                image.setPixel(x, y, color);
            }
        }
        hueTexture.upload();
        hueTextureDirty = false;
    }

    private void releaseColorTextures() {
        if (minecraft != null) {
            minecraft.getTextureManager().release(COLOR_PALETTE_TEXTURE_ID);
            minecraft.getTextureManager().release(HUE_BAR_TEXTURE_ID);
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
