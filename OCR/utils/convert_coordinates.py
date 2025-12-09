
def points_to_pixels(point_coordinates, dpi=200):
    # incoming coordinates are in points (1/72 inch) 
    # location object [xLL, yLL, xUR, yUR]
    # pixels = points * (DPI / 72)
    print(f"Converting coordinates {point_coordinates} from points to pixels with DPI={dpi}")
    pixels_xLL = point_coordinates[0] * (dpi / 72)
    pixels_yLL = point_coordinates[1] * (dpi / 72)
    pixels_xUR = point_coordinates[2] * (dpi / 72)
    pixels_yUR = point_coordinates[3] * (dpi / 72)
    pixel_coordinates = [int(pixels_xLL), int(pixels_yLL), int(pixels_xUR), int(pixels_yUR)]
    print(f"Converted to pixel coordinates: {pixel_coordinates}")
    return pixel_coordinates

def pixel_coordinates_to_box(pixel_coordinates, height):
    # convert to (left, upper, right, lower)
    box = (pixel_coordinates[0], height - pixel_coordinates[3], pixel_coordinates[2], height - pixel_coordinates[1])
    return box