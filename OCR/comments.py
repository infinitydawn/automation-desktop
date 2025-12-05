from pypdf import PdfReader

def read_comments_from_pdf(file_path, verbose=False):
    reader = PdfReader(file_path)
    buffer = []
    for pageNum, page in enumerate(reader.pages):
        if "/Annots" in page:
            
            for i,annotation in enumerate(page["/Annots"]):
                obj = annotation.get_object()
                if(obj["/Subtype"]=="/FreeText"):
                    # location object [xLL, yLL, xUR, yUR]
                    location = obj["/Rect"]
                    # convert to (left, upper, right, lower)
                    comment_obj = {"location": location, "contents": obj["/Contents"], "page": pageNum}
                    if verbose:
                        print(f"Found comment on page {pageNum}, annotation {i}: {comment_obj}")
                    buffer.append(comment_obj)
    print("Done reading comments")
    return buffer
    
    
