import os
import shutil
import re

# --- CONFIGURATION ---
SOURCE_DIR = r"C:\Users\Admin\Desktop\com.LittleSmiles.com Desktop"
DEST_DIR = r"C:\Users\Admin\Desktop\com.LittleSmiles.com"
BASE_PACKAGE = "com.LittleSmiles.com"

# Mapping logic: Current sub-project/folder -> Target layer
PACKAGE_MAPPING = {
    "core/model": "domain/model",
    "core/common": "core/common",
    "core/navigation": "presentation/navigation",
    "core/ui": "presentation/components",
    "data": "data",
    "feature/auth": "presentation/auth",
    "feature/games": "presentation/games",
    "feature/learning": "presentation/learning",
    "feature/loading": "presentation/loading",
    "feature/menu": "presentation/menu",
    "feature/parent-hub": "presentation/parenthub",
    "feature/stickers": "presentation/stickers",
    "app": "app"
}

def migrate():
    print(f"🚀 Starting migration to: {DEST_DIR}")

    # 1. Create essential structure
    if os.path.exists(DEST_DIR):
        print(f"⚠️ Destination exists. Cleaning...")
        shutil.rmtree(DEST_DIR)
    os.makedirs(DEST_DIR)

    # 2. Copy Global Build Configuration
    essentials = [
        "build.gradle.kts", "settings.gradle.kts", "gradle.properties",
        "gradlew", "gradlew.bat", "gradle/", "gradle/libs.versions.toml"
    ]
    for item in essentials:
        src = os.path.join(SOURCE_DIR, item)
        if os.path.exists(src):
            if os.path.isdir(src):
                shutil.copytree(src, os.path.join(DEST_DIR, item))
            else:
                shutil.copy2(src, os.path.join(DEST_DIR, item))

    # 3. Create Flattened App Module
    app_src_dest = os.path.join(DEST_DIR, "app", "src", "main", "java", *BASE_PACKAGE.split('.'))
    os.makedirs(app_src_dest, exist_ok=True)
    
    # Copy App build.gradle.kts and Manifest
    shutil.copytree(os.path.join(SOURCE_DIR, "app", "src", "main", "res"), os.path.join(DEST_DIR, "app", "src", "main", "res"))
    shutil.copy2(os.path.join(SOURCE_DIR, "app", "src", "main", "AndroidManifest.xml"), os.path.join(DEST_DIR, "app", "src", "main", "AndroidManifest.xml"))
    shutil.copy2(os.path.join(SOURCE_DIR, "app", "build.gradle.kts"), os.path.join(DEST_DIR, "app", "build.gradle.kts"))

    # 4. Process and Move Kotlin Files
    for subproject, layer in PACKAGE_MAPPING.items():
        src_path = os.path.join(SOURCE_DIR, subproject, "src", "main", "java")
        if not os.path.exists(src_path): continue

        target_layer_path = os.path.join(app_src_dest, *layer.split('/'))
        os.makedirs(target_layer_path, exist_ok=True)

        for root, _, files in os.walk(src_path):
            for file in files:
                if file.endswith(".kt"):
                    move_and_fix_kotlin(os.path.join(root, file), target_layer_path, layer)

    print("✅ Migration complete. Check the new directory!")

def move_and_fix_kotlin(file_path, target_dir, layer_pkg):
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()

    # Calculate new package name
    new_package = f"{BASE_PACKAGE}.{layer_pkg.replace('/', '.')}"
    
    # Update Package Declaration
    content = re.sub(r"package\s+[\w\.]+", f"package {new_package}", content, count=1)
    
    # Update Imports (Simple heuristic: update references to old internal packages)
    for old_sub, new_lay in PACKAGE_MAPPING.items():
        old_full = f"{BASE_PACKAGE}.{old_sub.replace('/', '.')}"
        new_full = f"{BASE_PACKAGE}.{new_lay.replace('/', '.')}"
        content = content.replace(f"import {old_full}", f"import {new_full}")

    dest_file = os.path.join(target_dir, os.path.basename(file_path))
    with open(dest_file, 'w', encoding='utf-8') as f:
        f.write(content)

if __name__ == "__main__":
    migrate()
