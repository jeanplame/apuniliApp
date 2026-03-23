"""
Script pour générer les icônes Android à partir du logo Apunili.
Génère ic_launcher, ic_launcher_round, ic_launcher_foreground,
ic_launcher_background et ic_launcher_monochrome pour toutes les densités.
"""
from PIL import Image, ImageDraw, ImageFilter
import os

LOGO_PATH = r"C:\Users\jeanm\OneDrive\Desktop\logo-apunili.png"
RES_DIR = r"C:\Users\jeanm\StudioProjects\apuniliApp\app\src\main\res"

# Tailles des icônes launcher par densité
LAUNCHER_SIZES = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192,
}

# Tailles des couches adaptives (foreground/background) par densité
ADAPTIVE_SIZES = {
    "mipmap-mdpi": 108,
    "mipmap-hdpi": 162,
    "mipmap-xhdpi": 216,
    "mipmap-xxhdpi": 324,
    "mipmap-xxxhdpi": 432,
}

def create_round_icon(img, size):
    """Crée une icône ronde à partir d'une image carrée."""
    img = img.resize((size, size), Image.LANCZOS)
    mask = Image.new("L", (size, size), 0)
    draw = ImageDraw.Draw(mask)
    draw.ellipse((0, 0, size - 1, size - 1), fill=255)
    result = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    result.paste(img, (0, 0), mask)
    return result

def create_foreground(logo, size):
    """
    Crée la couche foreground adaptative.
    Le logo est centré dans la zone safe (66% du centre).
    """
    canvas = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    # La zone safe est environ 66% de la taille totale (au centre)
    safe_zone = int(size * 0.66)
    logo_resized = logo.resize((safe_zone, safe_zone), Image.LANCZOS)
    offset = (size - safe_zone) // 2
    canvas.paste(logo_resized, (offset, offset), logo_resized if logo_resized.mode == 'RGBA' else None)
    return canvas

def create_background(size, color=(255, 255, 255)):
    """Crée la couche background (couleur unie blanche)."""
    return Image.new("RGBA", (size, size), (*color, 255))

def create_monochrome(logo, size):
    """Crée une version monochrome du logo pour Android 13+."""
    fg = create_foreground(logo, size)
    # Convertir en niveaux de gris puis en mode L
    mono = fg.convert("L").convert("RGBA")
    # Rendre les pixels transparents là où c'était transparent
    if fg.mode == "RGBA":
        r, g, b, a = fg.split()
        mono.putalpha(a)
    return mono

def save_webp(img, path):
    """Sauvegarde en WebP (format utilisé par Android Studio par défaut)."""
    img.save(path, "WEBP", quality=90)

def save_png(img, path):
    """Sauvegarde en PNG."""
    img.save(path, "PNG")

def main():
    print(f"Chargement du logo: {LOGO_PATH}")
    logo = Image.open(LOGO_PATH).convert("RGBA")
    print(f"Taille originale: {logo.size}")

    # Rendre le logo carré si nécessaire
    w, h = logo.size
    if w != h:
        max_dim = max(w, h)
        square = Image.new("RGBA", (max_dim, max_dim), (0, 0, 0, 0))
        offset_x = (max_dim - w) // 2
        offset_y = (max_dim - h) // 2
        square.paste(logo, (offset_x, offset_y), logo)
        logo = square
        print(f"Logo rendu carré: {logo.size}")

    for density, size in LAUNCHER_SIZES.items():
        folder = os.path.join(RES_DIR, density)
        os.makedirs(folder, exist_ok=True)

        # ic_launcher.webp - icône carrée
        launcher = logo.resize((size, size), Image.LANCZOS)
        save_webp(launcher, os.path.join(folder, "ic_launcher.webp"))
        print(f"  {density}/ic_launcher.webp ({size}x{size})")

        # ic_launcher_round.webp - icône ronde
        round_icon = create_round_icon(logo, size)
        save_webp(round_icon, os.path.join(folder, "ic_launcher_round.webp"))
        print(f"  {density}/ic_launcher_round.webp ({size}x{size})")

    for density, size in ADAPTIVE_SIZES.items():
        folder = os.path.join(RES_DIR, density)
        os.makedirs(folder, exist_ok=True)

        # ic_launcher_background.webp
        bg = create_background(size)
        save_webp(bg, os.path.join(folder, "ic_launcher_background.webp"))
        print(f"  {density}/ic_launcher_background.webp ({size}x{size})")

        # ic_launcher_monochrome.webp
        mono = create_monochrome(logo, size)
        save_webp(mono, os.path.join(folder, "ic_launcher_monochrome.webp"))
        print(f"  {density}/ic_launcher_monochrome.webp ({size}x{size})")

    # Aussi copier le logo dans drawable pour l'utiliser dans l'app
    drawable_dir = os.path.join(RES_DIR, "drawable")
    logo_app = logo.resize((512, 512), Image.LANCZOS)
    save_png(logo_app, os.path.join(drawable_dir, "logo_apunili.png"))
    print(f"  drawable/logo_apunili.png (512x512)")

    # Créer aussi un ic_launcher_foreground.png dans drawable
    fg_512 = create_foreground(logo, 432)
    save_png(fg_512, os.path.join(drawable_dir, "ic_launcher_foreground.png"))
    print(f"  drawable/ic_launcher_foreground.png (432x432)")

    print("\n✅ Toutes les icônes ont été générées avec succès!")

if __name__ == "__main__":
    main()

