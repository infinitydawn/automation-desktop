from PIL import Image


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

    # Save the cropped image
    cropped_img.save(f"./assets/cropped_images/cropped_page-{page_number}_device-{device_number}_fov-{padding}px.png")
    if verbose: print(f"Cropped image saved as cropped_page-{page_number}_device-{device_number}_fov-{padding}px.png")