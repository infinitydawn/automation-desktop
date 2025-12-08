import comments
import image_tools
import utils.convert_coordinates as convert_coordinates
import utils.get_image_properties as get_image_properties
import agent
from dotenv import load_dotenv
import os
import json
import re
import pandas as pd

# Load environment variables from .env file
load_dotenv()

# Access environment variables (use the standard name expected by the agent)
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")
print("API Key loaded:", OPENAI_API_KEY is not None)

verbose = True
comments = comments.read_comments_from_pdf("test.pdf",verbose=False)

def ask_agent(query: str, image_path: str, api_key: str):
    try:
        out = agent.request_openai_with_image(
            query,
            image_path=image_path,
            api_key=api_key,
            model=os.getenv("OPENAI_MODEL", "gpt-4o-mini")
        )
        print("Model text output:\n", out.get("text"))
        out_txt = out.get("text")
        out_json = None
        # Parse JSON string to dict if it's a string. Model outputs often include
        # surrounding explanation or markdown code fences (```json ... ```), so
        # try robust cleaning/extraction before json.loads.
        if isinstance(out_txt, str):
            cleaned = out_txt.strip()

            # Remove surrounding triple-backtick code fences if present
            if cleaned.startswith("```") and cleaned.endswith("```"):
                # remove the first and last lines (```json and ```)
                parts = cleaned.splitlines()
                if len(parts) >= 3:
                    cleaned = "\n".join(parts[1:-1]).strip()

            # Try direct JSON parse
            try:
                out_json = json.loads(cleaned)
            except Exception:
                # Try to extract the first JSON object/array substring
                m = re.search(r"(\{.*\}|\[.*\])", cleaned, re.DOTALL)
                if m:
                    candidate = m.group(1)
                    try:
                        out_json = json.loads(candidate)
                    except Exception as e2:
                        print("JSON parse failed on extracted substring:", e2)
                        print("Model output (truncated):", cleaned[:1000])
                else:
                    print("No JSON-like substring found in model output. Raw output (truncated):", cleaned[:1000])

        return out_json
    except Exception as e:
        print("Request failed:", e)




zone_list = []
page_floors = {}

for i,annot in enumerate(comments):
    img_properties = get_image_properties.get_image_properties(f"./page_pngs"+f"/page_{comments[i]['page']}.png")

    if comments[i]['page'] not in page_floors:
        # Safely call the agent to get floor name. If the call fails or returns
        # invalid data, default to 'unknown' and continue processing. This
        # prevents crashes and avoids making additional retries that could
        # spend more credits.
        try:
            floor_json = ask_agent(
                "Return only the floor_name for the given page (Ex. cellar, 1FL etc.)",
                f"./page_pngs/page_{comments[i]['page']}.png",
                OPENAI_API_KEY,
            )
            if isinstance(floor_json, dict) and "floor_name" in floor_json:
                floor_name = floor_json["floor_name"]
            else:
                print("Floor extraction returned no valid data; defaulting to 'unknown'.")
                floor_name = "unknown"
        except Exception as e:
            print("Floor extraction error:", e)
            floor_name = "unknown"

        page_floors[comments[i]['page']] = floor_name
        print(page_floors)
    


    pixels = convert_coordinates.points_to_pixels(comments[i]["location"])
    crop_box = convert_coordinates.pixel_coordinates_to_box(pixels, img_properties["height"])
    cropped_image = image_tools.crop_image("./page_pngs", comments[i]["page"], comments[i]["contents"], crop_box, padding=600, verbose=verbose)

    # Ask the agent for the device room name. Wrap in try/except and validate
    # the response so we don't crash or re-invoke the model on failure.
    try:
        device_room = ask_agent(
            f"Return only the room_name for the device address #{comments[i]['contents']}",
            cropped_image,
            OPENAI_API_KEY,
        )
        print(f"Parsed device {comments[i]['contents']} room {device_room}")
        if isinstance(device_room, dict) and "room_name" in device_room:
            device_room_name = device_room["room_name"]
        else:
            print("Device room extraction returned no valid data; using empty string.")
            device_room_name = ""
    except Exception as e:
        print("Device room extraction error:", e)
        device_room_name = ""

    zone_list.append({
        "address": comments[i]["contents"],
        "room": device_room_name,
        "floor": page_floors[comments[i]["page"]],
        "page": comments[i]["page"],
        "coordinates": comments[i]["location"]
    })



print("Final zone list:")
print(zone_list)

df = pd.DataFrame(zone_list)
df.to_csv("./assets/out/zone_list.csv", index=False)