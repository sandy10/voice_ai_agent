import os
from PIL import Image

src_img = r"C:\Users\hp\.gemini\antigravity\brain\24390e3b-d2d3-460d-b8c3-409841a759f7\agora_ai_app_icon_1787339226541.jpg"
res_dir = r"d:\AI_agent\voice_ai_agent\app\src\main\res"

sizes = {
    "mdpi": 48,
    "hdpi": 72,
    "xhdpi": 96,
    "xxhdpi": 144,
    "xxxhdpi": 192
}

# Delete old xml files to prevent conflict
fg_xml = os.path.join(res_dir, "drawable", "ic_launcher_foreground.xml")
bg_xml = os.path.join(res_dir, "drawable", "ic_launcher_background.xml")
if os.path.exists(fg_xml): os.remove(fg_xml)
if os.path.exists(bg_xml): os.remove(bg_xml)

with Image.open(src_img) as img:
    # We will use this image as the foreground for adaptive icons (108x108 is recommended, but we can just make it big)
    adaptive_fg = img.resize((432, 432), Image.Resampling.LANCZOS)
    os.makedirs(os.path.join(res_dir, "drawable"), exist_ok=True)
    adaptive_fg.save(os.path.join(res_dir, "drawable", "ic_launcher_foreground.webp"), format="webp")

    # For legacy icons
    for density, size in sizes.items():
        resized = img.resize((size, size), Image.Resampling.LANCZOS)
        
        # Save ic_launcher
        folder = os.path.join(res_dir, f"mipmap-{density}")
        os.makedirs(folder, exist_ok=True)
        resized.save(os.path.join(folder, "ic_launcher.webp"), format="webp")
        
        # Save ic_launcher_round (we'll just use the same image, it might be cropped by the OS anyway, or we could crop it to a circle)
        # To make it a circle:
        mask = Image.new("L", (size, size), 0)
        from PIL import ImageDraw
        draw = ImageDraw.Draw(mask)
        draw.ellipse((0, 0, size, size), fill=255)
        
        circular_img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
        circular_img.paste(resized.convert("RGBA"), (0, 0), mask=mask)
        
        circular_img.save(os.path.join(folder, "ic_launcher_round.webp"), format="webp")

print("Icons updated successfully!")
