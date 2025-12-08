from PIL import Image
import os
import re


def crop_image(directory: str, page_number: int, device_number: int ,box: list, padding: int = 0, verbose: bool = False):
    # Open the image
    img = Image.open(f"{directory}/page_{page_number}.png")

    width, height = img.size

    # Define the bounding box for cropping (left, upper, right, lower)
    # For example, to crop a 100x100 pixel area starting at (50, 50)
    # Apply padding (expand right/bottom, shrink left/top) and clamp to image bounds
    left = int(max(0, box[0] - padding))
    upper = int(max(0, box[1] - padding))
    right = int(min(width, box[2] + padding))
    lower = int(min(height, box[3] + padding))
    box = (left, upper, right, lower)
    if verbose: print(f"Cropping with box: {box}, with image size: ({width}, {height}), padding: {padding} px added.")

    # Crop the image
    cropped_img = img.crop(box)

    # Ensure output directory exists
    out_dir = "./assets/cropped_images"
    os.makedirs(out_dir, exist_ok=True)

    # Sanitize device_number for use in filenames: remove control chars and
    # characters invalid on Windows, replace whitespace with underscore and
    # truncate to a reasonable length.
    dev_str = str(device_number)
    # Remove ASCII control characters and common invalid filename chars
    # Note: backslash must be escaped in the character class as `\\` when using a raw string.
    dev_str = re.sub(r'[<>:"/\\|?*\x00-\x1F]', '', dev_str)
    # Replace runs of whitespace with single underscore
    dev_str = re.sub(r"\s+", "_", dev_str).strip("_ ")
    # Truncate to 64 chars to avoid overly long filenames
    if len(dev_str) > 64:
        dev_str = dev_str[:64]

    out_path = os.path.join(out_dir, f"cropped_page-{page_number}_device-{dev_str}_fov-{padding}px.png")
    # Save the cropped image
    cropped_img.save(out_path)
    if verbose: print(f"Cropped image saved as {out_path}")
    return out_path