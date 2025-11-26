from pypdf import PdfReader

reader = PdfReader("test.pdf")

for pageNum, page in enumerate(reader.pages):
    if "/Annots" in page:
        
        for i,annotation in enumerate(page["/Annots"]):
            obj = annotation.get_object()
            if(obj["/Subtype"]=="/FreeText"):
                print({"location": obj["/Rect"], "contents": obj["/Contents"], "page": pageNum})
                print("-----")