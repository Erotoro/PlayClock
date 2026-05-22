package dev.maksg.playclock.core.ui;

import java.util.List;

public record HudSnapshot(String variant, String title, List<HudMetric> metrics) {
}
