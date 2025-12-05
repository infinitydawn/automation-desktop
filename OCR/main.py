import comments
import image_tools
import utils.convert_coordinates as convert_coordinates
import utils.get_image_properties as get_image_properties

verbose = True
comments = comments.read_comments_from_pdf("test.pdf",verbose=False)

for i,annot in enumerate(comments):
    img_properties = get_image_properties.get_image_properties(f"./page_pngs"+f"/page_{comments[i]['page']}.png")
    pixels = convert_coordinates.points_to_pixels(comments[i]["location"])
    crop_box = convert_coordinates.pixel_coordinates_to_box(pixels, img_properties["height"])
    crop_test = image_tools.crop_image("./page_pngs", comments[i]["page"], comments[i]["contents"], crop_box, padding=600, verbose=verbose)
    if i == 5: exit()
