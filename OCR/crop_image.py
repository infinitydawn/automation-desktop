from PIL import Image

# Open the image
img = Image.open("page0.png")

# Define the bounding box for cropping (left, upper, right, lower)
# For example, to crop a 100x100 pixel area starting at (50, 50)
box = (500, 500, 1500, 1500) 

# Crop the image
cropped_img = img.crop(box)

# Save the cropped image
cropped_img.save("cropped_image_pillow.png")