
def get_image_properties(image_path):
    from PIL import Image
    img = Image.open(image_path)
    width, height = img.size
    dpi = img.info.get("dpi", (72, 72))  # Default to 72 if DPI info is not available
    return {
        "width": width,
        "height": height,
        "dpi": dpi
    }