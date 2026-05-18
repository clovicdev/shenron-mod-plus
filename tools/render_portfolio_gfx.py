from __future__ import annotations

import math
import random
from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter, ImageFont


ROOT = Path(__file__).resolve().parents[1]
ASSET_ROOT = ROOT / "src/main/resources/assets/shenron/textures"
OUT_DIR = ROOT / "portfolio_gfx"
WIDTH = 3840
HEIGHT = 2160


def font(name: str, size: int) -> ImageFont.FreeTypeFont:
    candidates = [
        Path("/System/Library/Fonts/Supplemental") / name,
        Path("/Library/Fonts") / name,
    ]
    for path in candidates:
        if path.exists():
            return ImageFont.truetype(str(path), size)
    return ImageFont.load_default()


FONT_DISPLAY = font("DIN Alternate Bold.ttf", 172)
FONT_TITLE = font("DIN Alternate Bold.ttf", 112)
FONT_SUBTITLE = font("Arial Bold.ttf", 46)
FONT_BODY = font("Arial.ttf", 34)
FONT_SMALL = font("Arial Bold.ttf", 30)
FONT_TINY = font("Arial Bold.ttf", 24)


def new_canvas(top: tuple[int, int, int], bottom: tuple[int, int, int]) -> Image.Image:
    img = Image.new("RGB", (WIDTH, HEIGHT), top)
    px = img.load()
    for y in range(HEIGHT):
        t = y / (HEIGHT - 1)
        ease = t * t * (3 - 2 * t)
        for x in range(WIDTH):
            vignette = 1.0 - 0.27 * math.hypot((x - WIDTH / 2) / WIDTH, (y - HEIGHT / 2) / HEIGHT)
            color = tuple(int((top[i] * (1 - ease) + bottom[i] * ease) * vignette) for i in range(3))
            px[x, y] = color
    return img.convert("RGBA")


def add_noise_and_stars(img: Image.Image, seed: int, star_color: tuple[int, int, int]) -> None:
    rng = random.Random(seed)
    overlay = Image.new("RGBA", img.size, (0, 0, 0, 0))
    draw = ImageDraw.Draw(overlay)
    for _ in range(440):
        x = rng.randrange(0, WIDTH)
        y = rng.randrange(0, HEIGHT)
        alpha = rng.randrange(34, 150)
        radius = rng.choice([1, 1, 1, 2, 2, 3])
        draw.ellipse((x - radius, y - radius, x + radius, y + radius), fill=(*star_color, alpha))
    for _ in range(42):
        x = rng.randrange(0, WIDTH)
        y = rng.randrange(0, HEIGHT)
        r = rng.randrange(16, 58)
        draw.ellipse((x - r, y - r, x + r, y + r), fill=(*star_color, rng.randrange(8, 18)))
    img.alpha_composite(overlay)


def glow_circle(size: int, color: tuple[int, int, int], blur: int) -> Image.Image:
    layer = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    d = ImageDraw.Draw(layer)
    margin = size // 5
    d.ellipse((margin, margin, size - margin, size - margin), fill=(*color, 210))
    return layer.filter(ImageFilter.GaussianBlur(blur))


def paste_center(base: Image.Image, overlay: Image.Image, center: tuple[int, int]) -> None:
    x = int(center[0] - overlay.width / 2)
    y = int(center[1] - overlay.height / 2)
    base.alpha_composite(overlay, (x, y))


def text(draw: ImageDraw.ImageDraw, pos: tuple[int, int], copy: str, fnt, fill, shadow=(0, 0, 0, 185), offset=5) -> None:
    x, y = pos
    draw.text((x + offset, y + offset), copy, font=fnt, fill=shadow)
    draw.text((x, y), copy, font=fnt, fill=fill)


def star_points(cx: float, cy: float, outer: float, inner: float, points: int = 5, rotation: float = -math.pi / 2) -> list[tuple[float, float]]:
    pts = []
    for i in range(points * 2):
        r = outer if i % 2 == 0 else inner
        a = rotation + i * math.pi / points
        pts.append((cx + math.cos(a) * r, cy + math.sin(a) * r))
    return pts


def star_layout(count: int, radius: int) -> list[tuple[float, float]]:
    if count == 1:
        return [(0, 0)]
    if count == 2:
        return [(-0.23 * radius, -0.06 * radius), (0.23 * radius, 0.06 * radius)]
    if count == 3:
        return [(-0.25 * radius, 0.1 * radius), (0, -0.18 * radius), (0.25 * radius, 0.1 * radius)]
    if count == 4:
        return [(-0.22 * radius, -0.18 * radius), (0.22 * radius, -0.18 * radius), (-0.22 * radius, 0.18 * radius), (0.22 * radius, 0.18 * radius)]
    if count == 5:
        return [(-0.27 * radius, -0.18 * radius), (0.27 * radius, -0.18 * radius), (0, 0), (-0.2 * radius, 0.23 * radius), (0.2 * radius, 0.23 * radius)]
    if count == 6:
        return [(-0.28 * radius, -0.2 * radius), (0, -0.2 * radius), (0.28 * radius, -0.2 * radius), (-0.28 * radius, 0.2 * radius), (0, 0.2 * radius), (0.28 * radius, 0.2 * radius)]
    return [(-0.3 * radius, -0.24 * radius), (0, -0.27 * radius), (0.3 * radius, -0.24 * radius), (-0.18 * radius, 0), (0.18 * radius, 0), (-0.24 * radius, 0.28 * radius), (0.24 * radius, 0.28 * radius)]


def draw_dragon_ball(base: Image.Image, center: tuple[int, int], radius: int, stars: int, glow: bool = True) -> None:
    if glow:
        aura = glow_circle(radius * 5, (255, 157, 23), radius // 2)
        paste_center(base, aura, center)

    ball = Image.new("RGBA", (radius * 2 + 8, radius * 2 + 8), (0, 0, 0, 0))
    d = ImageDraw.Draw(ball)
    cx = cy = radius + 4

    for r in range(radius, 0, -1):
        t = r / radius
        light = 1 - t
        shade = 0.55 + 0.45 * t
        rr = int(240 * shade + 15 * light)
        gg = int(118 * shade + 120 * light)
        bb = int(24 * shade + 28 * light)
        d.ellipse((cx - r, cy - r, cx + r, cy + r), fill=(rr, gg, bb, 255))

    d.ellipse((cx - radius + 8, cy - radius + 8, cx + radius - 8, cy + radius - 8), outline=(255, 228, 112, 185), width=max(2, radius // 25))
    d.ellipse((cx - radius * 0.56, cy - radius * 0.62, cx - radius * 0.08, cy - radius * 0.15), fill=(255, 246, 171, 78))
    d.ellipse((cx - radius * 0.9, cy + radius * 0.56, cx + radius * 0.7, cy + radius * 0.98), fill=(64, 20, 8, 54))

    for ox, oy in star_layout(stars, radius):
        d.polygon(star_points(cx + ox, cy + oy, radius * 0.112, radius * 0.048), fill=(161, 24, 9, 255))
        d.polygon(star_points(cx + ox - radius * 0.012, cy + oy - radius * 0.012, radius * 0.035, radius * 0.016), fill=(255, 84, 35, 190))

    shadow = Image.new("RGBA", ball.size, (0, 0, 0, 0))
    ImageDraw.Draw(shadow).ellipse((cx - radius + 5, cy - radius + 5, cx + radius + 5, cy + radius + 5), fill=(0, 0, 0, 120))
    shadow = shadow.filter(ImageFilter.GaussianBlur(radius // 10))
    paste_center(base, shadow, (center[0] + radius // 9, center[1] + radius // 7))
    paste_center(base, ball, center)


def draw_dragon(base: Image.Image, origin: tuple[int, int], scale: float, seed: int = 0, silhouette: bool = False) -> None:
    rng = random.Random(seed)
    layer = Image.new("RGBA", base.size, (0, 0, 0, 0))
    draw = ImageDraw.Draw(layer)

    points = []
    for i in range(18):
        t = i / 17
        x = origin[0] + math.sin(t * math.pi * 3.5 + 0.45) * (500 * scale) * (1 - t * 0.28)
        y = origin[1] - t * 1260 * scale + math.cos(t * math.pi * 2.0) * 90 * scale
        w = (170 - t * 58) * scale
        points.append((x, y, w, t))

    glow_col = (56, 238, 139) if not silhouette else (28, 146, 86)
    for width, alpha, blur in [(150, 90, 28), (90, 120, 16), (46, 150, 7)]:
        line = Image.new("RGBA", base.size, (0, 0, 0, 0))
        ld = ImageDraw.Draw(line)
        ld.line([(p[0], p[1]) for p in points], fill=(*glow_col, alpha), width=int(width * scale), joint="curve")
        layer.alpha_composite(line.filter(ImageFilter.GaussianBlur(int(blur * scale))))

    body_fill = (24, 152, 78, 230) if not silhouette else (12, 74, 47, 135)
    body_edge = (92, 255, 153, 125) if not silhouette else (47, 178, 103, 52)
    draw.line([(p[0], p[1]) for p in points], fill=body_edge, width=int(132 * scale), joint="curve")
    draw.line([(p[0], p[1]) for p in points], fill=body_fill, width=int(112 * scale), joint="curve")

    for x, y, w, t in points:
        if silhouette:
            color = (14, 82, 51)
            belly = (196, 174, 95, 120)
        else:
            color = (
                int(26 + 22 * (1 - t)),
                int(145 + 58 * (1 - t)),
                int(74 + 38 * (1 - t)),
            )
            belly = (234, 211, 129, 220)

        dx = rng.uniform(-6, 6) * scale
        dy = rng.uniform(-4, 4) * scale
        draw.ellipse((x - w * 0.72 + dx, y - w * 0.45 + dy, x + w * 0.72 + dx, y + w * 0.45 + dy), fill=(*color, 235 if not silhouette else 170), outline=(91, 255, 153, 130 if not silhouette else 60), width=max(1, int(3 * scale)))
        draw.ellipse((x - w * 0.3 + dx, y + w * 0.1 + dy, x + w * 0.32 + dx, y + w * 0.38 + dy), fill=belly)
        if t < 0.87:
            spine = [
                (x - 0.12 * w, y - 0.38 * w),
                (x, y - 0.76 * w),
                (x + 0.12 * w, y - 0.38 * w),
            ]
            draw.polygon(spine, fill=(229, 214, 137, 190 if not silhouette else 70))

    hx, hy, hw, _ = points[-1]
    head_color = (31, 171, 88, 250) if not silhouette else (18, 92, 56, 190)
    cream = (239, 222, 150, 245) if not silhouette else (202, 183, 113, 110)
    draw.rounded_rectangle((hx - 150 * scale, hy - 108 * scale, hx + 155 * scale, hy + 98 * scale), radius=int(34 * scale), fill=head_color, outline=(103, 255, 167, 160), width=max(2, int(7 * scale)))
    draw.rounded_rectangle((hx - 90 * scale, hy + 25 * scale, hx + 98 * scale, hy + 93 * scale), radius=int(28 * scale), fill=cream)
    draw.polygon([(hx - 90 * scale, hy - 96 * scale), (hx - 48 * scale, hy - 244 * scale), (hx - 7 * scale, hy - 82 * scale)], fill=cream)
    draw.polygon([(hx + 90 * scale, hy - 96 * scale), (hx + 48 * scale, hy - 244 * scale), (hx + 7 * scale, hy - 82 * scale)], fill=cream)
    draw.ellipse((hx - 75 * scale, hy - 35 * scale, hx - 35 * scale, hy + 5 * scale), fill=(235, 37, 34, 240))
    draw.ellipse((hx + 35 * scale, hy - 35 * scale, hx + 75 * scale, hy + 5 * scale), fill=(235, 37, 34, 240))
    draw.line((hx - 138 * scale, hy + 18 * scale, hx - 500 * scale, hy - 35 * scale), fill=cream, width=max(3, int(9 * scale)))
    draw.line((hx + 138 * scale, hy + 18 * scale, hx + 515 * scale, hy - 28 * scale), fill=cream, width=max(3, int(9 * scale)))

    if not silhouette:
        layer = layer.filter(ImageFilter.UnsharpMask(radius=2, percent=140, threshold=4))
    base.alpha_composite(layer)


def draw_ground(img: Image.Image) -> None:
    layer = Image.new("RGBA", img.size, (0, 0, 0, 0))
    d = ImageDraw.Draw(layer)
    y = 1770
    d.polygon([(0, y), (3840, y - 120), (3840, 2160), (0, 2160)], fill=(20, 44, 34, 240))
    for x in range(-80, WIDTH + 200, 180):
        d.line((x, y + 8, x + 260, HEIGHT), fill=(56, 90, 63, 70), width=3)
    for yy in range(y + 50, HEIGHT, 105):
        d.line((0, yy, WIDTH, yy - 120), fill=(60, 92, 66, 45), width=3)
    d.line((0, y, WIDTH, y - 120), fill=(87, 194, 117, 105), width=5)
    img.alpha_composite(layer.filter(ImageFilter.GaussianBlur(0.4)))


def pill(draw: ImageDraw.ImageDraw, box: tuple[int, int, int, int], label: str, fill=(20, 34, 35, 170), outline=(107, 255, 173, 150)) -> None:
    draw.rounded_rectangle(box, radius=28, fill=fill, outline=outline, width=2)
    tw = draw.textbbox((0, 0), label, font=FONT_SMALL)
    draw.text((box[0] + (box[2] - box[0] - (tw[2] - tw[0])) / 2, box[1] + 19), label, font=FONT_SMALL, fill=(224, 246, 225, 255))


def render_ritual() -> Path:
    img = new_canvas((4, 15, 18), (13, 35, 29))
    add_noise_and_stars(img, 404, (104, 241, 197))

    bg_glow = Image.new("RGBA", img.size, (0, 0, 0, 0))
    gd = ImageDraw.Draw(bg_glow)
    gd.ellipse((1120, 360, 2770, 2020), fill=(23, 194, 94, 74))
    gd.ellipse((1420, 890, 2450, 2050), fill=(255, 149, 32, 54))
    img.alpha_composite(bg_glow.filter(ImageFilter.GaussianBlur(115)))

    draw_dragon(img, (1940, 1770), 1.05, seed=12)
    draw_ground(img)

    cluster = [(1685, 1648, 1), (1935, 1648, 2), (1434, 1790, 3), (1810, 1790, 4), (2186, 1790, 5), (1685, 1920, 6), (1935, 1920, 7)]
    for x, y, star in cluster:
        draw_dragon_ball(img, (x, y), 126, star, True)

    d = ImageDraw.Draw(img)
    text(d, (190, 185), "SHENRON", FONT_DISPLAY, (235, 255, 232, 255), shadow=(0, 0, 0, 210), offset=8)
    text(d, (198, 354), "DRAGON BALLS", FONT_TITLE, (255, 176, 50, 255), shadow=(0, 0, 0, 190), offset=6)
    d.rectangle((204, 493, 780, 501), fill=(74, 255, 160, 220))
    text(d, (202, 545), "FORGE MOD SHOWCASE", FONT_SUBTITLE, (194, 236, 213, 245), shadow=(0, 0, 0, 160), offset=4)

    pill(d, (204, 645, 590, 716), "7 DRAGON BALLS")
    pill(d, (620, 645, 936, 716), "WISH SYSTEM")
    pill(d, (966, 645, 1296, 716), "CUSTOM ENTITY")
    text(d, (2660, 178), "RITUAL VARIANT", FONT_SUBTITLE, (202, 237, 216, 230), shadow=(0, 0, 0, 160), offset=4)
    text(d, (2665, 236), "2-3-2 SUMMON PATTERN", FONT_SMALL, (255, 178, 65, 230), shadow=(0, 0, 0, 150), offset=3)

    vignette = Image.new("RGBA", img.size, (0, 0, 0, 0))
    vd = ImageDraw.Draw(vignette)
    vd.rectangle((0, 0, WIDTH, 130), fill=(0, 0, 0, 68))
    vd.rectangle((0, HEIGHT - 170, WIDTH, HEIGHT), fill=(0, 0, 0, 72))
    img.alpha_composite(vignette)

    OUT_DIR.mkdir(exist_ok=True)
    path = OUT_DIR / "shenron-gfx-variant-01-ritual-4k.png"
    img.convert("RGB").save(path, quality=98)
    return path


def draw_radar(base: Image.Image, center: tuple[int, int], scale: float) -> None:
    size = int(820 * scale)
    x = center[0] - size // 2
    y = center[1] - size // 2
    layer = Image.new("RGBA", (size + 220, size + 220), (0, 0, 0, 0))
    d = ImageDraw.Draw(layer)
    ox = oy = 110

    d.rounded_rectangle((ox + 38, oy + 70, ox + size - 38, oy + size - 30), radius=int(72 * scale), fill=(11, 17, 18, 255), outline=(82, 255, 174, 155), width=int(7 * scale))
    d.rounded_rectangle((ox + 86, oy + 112, ox + size - 86, oy + size - 142), radius=int(44 * scale), fill=(12, 43, 34, 255), outline=(146, 255, 191, 100), width=int(5 * scale))

    screen = (ox + 124, oy + 150, ox + size - 124, oy + size - 182)
    d.rectangle(screen, fill=(9, 66, 43, 255))
    for gx in range(screen[0], screen[2], int(48 * scale)):
        d.line((gx, screen[1], gx, screen[3]), fill=(68, 255, 143, 55), width=1)
    for gy in range(screen[1], screen[3], int(48 * scale)):
        d.line((screen[0], gy, screen[2], gy), fill=(68, 255, 143, 55), width=1)

    cx = (screen[0] + screen[2]) / 2
    cy = (screen[1] + screen[3]) / 2
    maxr = (screen[2] - screen[0]) * 0.42
    for r in [0.25, 0.5, 0.75, 1.0]:
        rr = maxr * r
        d.ellipse((cx - rr, cy - rr, cx + rr, cy + rr), outline=(126, 255, 166, 90), width=max(1, int(2 * scale)))
    d.line((cx, cy, cx + maxr * 0.86, cy - maxr * 0.34), fill=(126, 255, 166, 190), width=max(2, int(5 * scale)))
    d.ellipse((cx + maxr * 0.77 - 16 * scale, cy - maxr * 0.37 - 16 * scale, cx + maxr * 0.77 + 16 * scale, cy - maxr * 0.37 + 16 * scale), fill=(255, 181, 38, 255))
    d.ellipse((cx - 17 * scale, cy - 17 * scale, cx + 17 * scale, cy + 17 * scale), fill=(147, 255, 176, 230))

    d.ellipse((ox + size * 0.33, oy + size - 145 * scale, ox + size * 0.67, oy + size - 25 * scale), fill=(18, 28, 29, 255), outline=(102, 255, 177, 120), width=max(3, int(4 * scale)))
    d.ellipse((ox + size * 0.43, oy + size - 116 * scale, ox + size * 0.57, oy + size - 52 * scale), fill=(255, 168, 36, 245))
    d.ellipse((ox + size * 0.74, oy + size - 136 * scale, ox + size * 0.86, oy + size - 54 * scale), fill=(24, 34, 36, 255), outline=(90, 255, 175, 95), width=max(2, int(3 * scale)))

    shadow = Image.new("RGBA", layer.size, (0, 0, 0, 0))
    ImageDraw.Draw(shadow).rounded_rectangle((ox + 40, oy + 90, ox + size - 20, oy + size - 10), radius=int(90 * scale), fill=(0, 0, 0, 170))
    shadow = shadow.filter(ImageFilter.GaussianBlur(int(34 * scale)))
    base.alpha_composite(shadow, (x - 110 + int(42 * scale), y - 110 + int(62 * scale)))
    base.alpha_composite(layer, (x - 110, y - 110))


def render_radar() -> Path:
    img = new_canvas((7, 9, 15), (10, 18, 21))
    add_noise_and_stars(img, 923, (85, 201, 255))

    glow = Image.new("RGBA", img.size, (0, 0, 0, 0))
    gd = ImageDraw.Draw(glow)
    gd.ellipse((250, 430, 1900, 1940), fill=(26, 174, 97, 64))
    gd.ellipse((2070, 200, 3820, 1600), fill=(255, 150, 40, 42))
    img.alpha_composite(glow.filter(ImageFilter.GaussianBlur(130)))

    draw_dragon(img, (2980, 1840), 0.74, seed=80, silhouette=True)

    d = ImageDraw.Draw(img)
    panel = Image.new("RGBA", img.size, (0, 0, 0, 0))
    pd = ImageDraw.Draw(panel)
    pd.rounded_rectangle((1960, 260, 3580, 1760), radius=54, fill=(8, 16, 17, 178), outline=(83, 255, 174, 92), width=3)
    for x in range(2020, 3540, 88):
        pd.line((x, 310, x - 320, 1720), fill=(74, 255, 167, 21), width=2)
    for y in range(360, 1710, 88):
        pd.line((2020, y, 3540, y - 95), fill=(74, 255, 167, 18), width=2)
    img.alpha_composite(panel)

    draw_radar(img, (1025, 1260), 1.13)
    orbit = [(760, 495, 1), (1115, 410, 2), (1468, 552, 3), (1580, 880, 4), (1418, 1602, 5), (970, 1748, 6), (570, 1545, 7)]
    for x, y, star in orbit:
        draw_dragon_ball(img, (x, y), 82, star, True)

    texture = Image.open(ASSET_ROOT / "item/dragon_radar_center.png").convert("RGBA").resize((168, 168), Image.Resampling.NEAREST)
    card = Image.new("RGBA", (320, 268), (0, 0, 0, 0))
    cd = ImageDraw.Draw(card)
    cd.rounded_rectangle((0, 0, 320, 268), radius=28, fill=(9, 20, 22, 218), outline=(113, 255, 178, 118), width=2)
    card.alpha_composite(texture, (76, 34))
    cd.text((44, 217), "IN-GAME RADAR", font=FONT_TINY, fill=(214, 238, 220, 255))
    img.alpha_composite(card, (284, 300))

    text(d, (2085, 470), "DRAGON", FONT_TITLE, (240, 255, 235, 255), shadow=(0, 0, 0, 205), offset=7)
    text(d, (2085, 590), "RADAR", FONT_DISPLAY, (93, 255, 169, 255), shadow=(0, 0, 0, 210), offset=8)
    text(d, (2095, 790), "TRACK THE SEVEN BALLS", FONT_SUBTITLE, (255, 180, 56, 245), shadow=(0, 0, 0, 160), offset=4)
    d.rectangle((2098, 875, 3200, 883), fill=(77, 255, 168, 205))
    body = "Locator-style tracking, 3D Dragon Balls,\nShenron ritual summon, and item-based wishes."
    d.multiline_text((2096, 945), body, font=FONT_BODY, fill=(211, 237, 224, 235), spacing=13)
    pill(d, (2096, 1138, 2452, 1210), "FORGE 1.20.1", fill=(14, 29, 31, 190))
    pill(d, (2485, 1138, 2848, 1210), "WORLD SPAWN", fill=(14, 29, 31, 190))
    pill(d, (2880, 1138, 3268, 1210), "ADMIN TESTING", fill=(14, 29, 31, 190))
    text(d, (2100, 1500), "PORTFOLIO GFX 02", FONT_SMALL, (174, 220, 202, 210), shadow=(0, 0, 0, 150), offset=3)

    OUT_DIR.mkdir(exist_ok=True)
    path = OUT_DIR / "shenron-gfx-variant-02-radar-4k.png"
    img.convert("RGB").save(path, quality=98)
    return path


def main() -> None:
    paths = [render_ritual(), render_radar()]
    for path in paths:
        print(path)


if __name__ == "__main__":
    main()
