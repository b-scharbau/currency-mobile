"""Generates the app's launcher icon assets (legacy per-density PNGs and API 26+ adaptive icon
foreground/background layers) plus a 512x512 reference render. Requires Pillow (`pip install
pillow`). Run from anywhere — paths are resolved relative to this file's location:

    python3 graphics/gen_icons.py
"""

from PIL import Image, ImageDraw
import os

TEAL = (0x2B, 0x6E, 0x68, 255)   # BrandColors.signal
PAPER = (0xF7, 0xF6, 0xF3, 255)  # BrandColors.paper

GRAPHICS = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.dirname(GRAPHICS)
RES = os.path.join(PROJECT_ROOT, "composeApp", "src", "androidMain", "res")


# Glyph defined in a normalized 100x100 unit space: the "signal divider" zigzag from the web
# design system (SignalDivider.kt / .signal-divider in frontend/src/styles.css) — combined with
# the in-app swap button's exchange arrows by terminating the line's two ends in arrowheads
# instead of rounded caps, rather than cramming two separate motifs side by side (which wouldn't
# stay legible at launcher-icon sizes). Simplified to a single blip (the web version has four,
# across a much wider aspect ratio) to leave room for the arrowheads within the adaptive icon's
# inner safe zone (~66% of canvas, i.e. roughly the [17,83] range here).
def line_points():
    return [(28, 50), (40, 50), (46, 30), (54, 70), (60, 50), (72, 50)]


def left_arrowhead():
    return [(18, 50), (30, 40), (30, 60)]


def right_arrowhead():
    return [(82, 50), (70, 40), (70, 60)]


def scale_points(points, size, cx, cy, canvas_scale):
    points = [(x / 100 * size, y / 100 * size) for x, y in points]
    if canvas_scale != 1.0:
        points = [(cx + (x - cx) * canvas_scale, cy + (y - cy) * canvas_scale) for x, y in points]
    return points


def make_foreground(size, canvas_scale=1.0):
    """canvas_scale < 1 shrinks the glyph further for legacy full-bleed icons where the
    background is also visible (no OEM adaptive-icon masking to compensate for)."""
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    cx = cy = size / 2

    def scale(points):
        return scale_points(points, size, cx, cy, canvas_scale)

    line = scale(line_points())
    width = round(size * 0.09)
    draw.line(line, fill=PAPER, width=width, joint="curve")
    # `joint="curve"` rounds internal joins but not the two end caps — round them off so the
    # stroke meets the arrowhead bases cleanly instead of being cut off square.
    radius = width / 2
    for x, y in (line[0], line[-1]):
        draw.ellipse((x - radius, y - radius, x + radius, y + radius), fill=PAPER)

    draw.polygon(scale(left_arrowhead()), fill=PAPER)
    draw.polygon(scale(right_arrowhead()), fill=PAPER)
    return img


def make_background(size):
    return Image.new("RGBA", (size, size), TEAL)


def make_legacy(size, round_mask):
    bg = make_background(size)
    fg = make_foreground(size, canvas_scale=0.78)
    combined = Image.alpha_composite(bg, fg)
    mask = Image.new("L", (size, size), 0)
    mdraw = ImageDraw.Draw(mask)
    if round_mask:
        mdraw.ellipse((0, 0, size, size), fill=255)
    else:
        radius = size * 0.18
        mdraw.rounded_rectangle((0, 0, size, size), radius=radius, fill=255)
    combined.putalpha(mask)
    return combined


DENSITIES = {
    "mdpi": 48,
    "hdpi": 72,
    "xhdpi": 96,
    "xxhdpi": 144,
    "xxxhdpi": 192,
}
ADAPTIVE_SCALE = 108 / 48  # adaptive icon canvas is 108dp vs legacy 48dp

if __name__ == "__main__":
    for density, legacy_size in DENSITIES.items():
        out_dir = os.path.join(RES, f"mipmap-{density}")
        os.makedirs(out_dir, exist_ok=True)

        make_legacy(legacy_size, round_mask=False).save(os.path.join(out_dir, "ic_launcher.png"))
        make_legacy(legacy_size, round_mask=True).save(os.path.join(out_dir, "ic_launcher_round.png"))

        adaptive_size = round(legacy_size * ADAPTIVE_SCALE)
        make_foreground(adaptive_size).save(os.path.join(out_dir, "ic_launcher_foreground.png"))
        make_background(adaptive_size).save(os.path.join(out_dir, "ic_launcher_background.png"))

    # A 512x512 reference icon for future store listings — not used by the app build itself.
    make_legacy(512, round_mask=False).save(os.path.join(GRAPHICS, "icon-512.png"))

    print("done")
