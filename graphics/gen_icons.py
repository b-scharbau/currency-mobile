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


# Glyph defined in a normalized 100x100 unit space: two opposing horizontal arrows
# (mirroring the in-app swap button's "exchange" glyph), well within the adaptive icon's
# inner safe zone (~66% of canvas, i.e. roughly the [17,83] range here).
def glyph_polygons():
    top_shaft = [(26, 35), (62, 35), (62, 41), (26, 41)]
    top_head = [(62, 29), (78, 38), (62, 47)]
    bottom_shaft = [(38, 59), (74, 59), (74, 65), (38, 65)]
    bottom_head = [(38, 53), (22, 62), (38, 71)]
    return [top_shaft, top_head, bottom_shaft, bottom_head]


def scale_poly(poly, size):
    return [(x / 100 * size, y / 100 * size) for x, y in poly]


def make_foreground(size, canvas_scale=1.0):
    """canvas_scale < 1 shrinks the glyph further for legacy full-bleed icons where the
    background is also visible (no OEM adaptive-icon masking to compensate for)."""
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    cx = cy = size / 2
    for poly in glyph_polygons():
        scaled = scale_poly(poly, size)
        if canvas_scale != 1.0:
            scaled = [(cx + (x - cx) * canvas_scale, cy + (y - cy) * canvas_scale) for x, y in scaled]
        draw.polygon(scaled, fill=PAPER)
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
