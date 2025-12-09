import os
import base64
import json
import mimetypes
from io import BytesIO
try:
    from PIL import Image
except Exception:
    Image = None
from typing import Optional, Dict, Any
import argparse

# Official OpenAI Python SDK
try:
    from openai import OpenAI
except Exception:
    OpenAI = None

"""
agent.py

Small helper to call OpenAI's chat/completions endpoint with text + an image file.
The image is encoded as a data URL and appended to the user message so multimodal
models that accept inline images (e.g. vision-capable chat models) can see it.

Usage:
    text = "Please describe the text in this image and list any phone numbers."
    resp = request_openai_with_image(text, "receipt.jpg")
    print(resp["text"])
"""



OPENAI_API_URL = "https://api.openai.com/v1/chat/completions"
DEFAULT_MODEL = "gpt-4o-mini"  # change to the model you have access to


def _load_image_as_data_url(path: str, max_width: int = 1024, max_bytes: int = 200_000) -> str:
    """Read an image file and return a data URL (e.g. data:image/jpeg;base64,...).

    To avoid extremely large requests (token/payload limits), this helper will
    resize and recompress images using Pillow (if available). It attempts to
    produce an encoded image under `max_bytes` by progressively reducing JPEG
    quality. If Pillow isn't installed, it falls back to returning the raw
    file bytes (which may be too large).
    """
    mime, _ = mimetypes.guess_type(path)
    mime = mime or "application/octet-stream"

    # If PIL is available, try to open and compress/resize
    if Image is not None:
        with Image.open(path) as img:
            # Convert to RGB for consistent JPEG output (handles PNG/RGBA)
            if img.mode in ("RGBA", "P"):
                img = img.convert("RGB")

            # Resize if wider than max_width while keeping aspect ratio
            w, h = img.size
            if max_width and w > max_width:
                new_h = int(max_width * (h / w))
                img = img.resize((max_width, new_h), Image.LANCZOS)

            # Try saving to JPEG with decreasing quality until under max_bytes
            quality = 85
            buf = BytesIO()
            img.save(buf, format="JPEG", quality=quality)
            size = buf.tell()

            while size > max_bytes and quality >= 30:
                quality -= 10
                buf = BytesIO()
                img.save(buf, format="JPEG", quality=quality)
                size = buf.tell()

            # If still too big, attempt one more aggressive reduce
            if size > max_bytes and quality > 10:
                quality = 20
                buf = BytesIO()
                img.save(buf, format="JPEG", quality=quality)
                size = buf.tell()

            b = buf.getvalue()
            mime = "image/jpeg"
    else:
        # Pillow not installed: fall back to raw bytes (may be large)
        with open(path, "rb") as f:
            b = f.read()

    b64 = base64.b64encode(b).decode("ascii")
    return f"data:{mime};base64,{b64}"


def request_openai_with_image(
    text: str,
    image_path: Optional[str] = None,
    image_url: Optional[str] = None,
    model: str = DEFAULT_MODEL,
    api_key: Optional[str] = None,
    temperature: float = 0.0,
    max_tokens: int = 800,
    timeout: int = 60,
) -> Dict[str, Any]:
    """
    Send a single chat request containing text + an image (embedded as a data URL).
    Returns a dict with parsed response and the raw JSON.

    Returned dict format:
      {
        "text": "<model's text output>",
        "raw": { ... full API JSON ... }
      }

    Note: Some OpenAI models expect images in a particular format or endpoint.
    If your model or account uses a different interface, adjust accordingly.
    """
    api_key = api_key or os.getenv("OPENAI_API_KEY")
    if not api_key:
        raise RuntimeError("OpenAI API key not provided. Set OPENAI_API_KEY env or pass api_key.")

    # Allow overriding default model via environment variable
    model = os.getenv("OPENAI_MODEL", model)

    # If an image path was provided and no image_url, compress & convert it to a data URL.
    if image_path and not image_url:
        data_url = _load_image_as_data_url(image_path)
        image_url = data_url

    # Build structured input matching the Responses API multimodal example.
    # We'll send a system message with a small instruction and a user message containing
    # the prompt and the image (as an image_url value; can be a data URL).
    system_block = {
        "role": "system",
        "content": [
            {"type": "input_text", "text": "You are a precise data extraction assistant that outputs JSON matching the provided schema."}
        ],
    }

    user_content_list = [{"type": "input_text", "text": text.strip()}]
    if image_url:
        user_content_list.append({"type": "input_image", "image_url": image_url})

    user_block = {"role": "user", "content": user_content_list}

    responses_input = [system_block, user_block]

    # Use the official OpenAI Python SDK when available (preferred).
    if OpenAI is None:
        raise RuntimeError("OpenAI SDK not installed. Please install with: pip install openai")

    client = OpenAI(api_key=api_key)

    raw = None
    # Prefer the newer Responses API; fall back to chat completions if needed.
    try:
        resp = client.responses.create(model=model, input=responses_input, temperature=temperature, max_output_tokens=max_tokens)
        try:
            raw = resp.to_dict()
        except Exception:
            try:
                raw = json.loads(str(resp))
            except Exception:
                raw = resp
    except Exception:
        # Fall back to chat completions if available in the SDK
        try:
            # Fallback to chat completions: convert our structured blocks to simple text messages
            system_text = system_block["content"][0]["text"] if system_block.get("content") else ""
            user_text = text.strip()
            if image_url:
                user_text = user_text + "\n\n" + f"Image (base64 or URL): {image_url}"
            messages = [
                {"role": "system", "content": system_text},
                {"role": "user", "content": user_text},
            ]
            resp = client.chat.completions.create(model=model, messages=messages, temperature=temperature, max_tokens=max_tokens)
            try:
                raw = resp.to_dict()
            except Exception:
                try:
                    raw = json.loads(str(resp))
                except Exception:
                    raw = resp
        except Exception as e:
            # Surface the error to the caller with details
            raise RuntimeError(f"OpenAI SDK request failed: {e}")

    # Extract text from the returned JSON. Different OpenAI endpoints return different shapes.
    def _extract_text_from_raw(raw_json: Dict[str, Any]) -> str:
        if not raw_json:
            return ""

        # Chat completion style
        try:
            choices = raw_json.get("choices")
            if choices and isinstance(choices, list) and len(choices) > 0:
                first = choices[0]
                # chat completion message style
                msg = first.get("message")
                if msg and "content" in msg:
                    return msg["content"]
                # older style has 'text'
                if "text" in first:
                    return first.get("text", "")
        except Exception:
            pass

        # Responses API style: look for 'output' or 'output_text' or nested content
        try:
            output = raw_json.get("output") or raw_json.get("outputs")
            if output and isinstance(output, list):
                for item in output:
                    # item may be a dict with 'content' list
                    if isinstance(item, dict):
                        content = item.get("content")
                        if content and isinstance(content, list):
                            for c in content:
                                if isinstance(c, dict) and "text" in c:
                                    return c.get("text", "")
                                if isinstance(c, str):
                                    return c
                        # sometimes the item itself is plain text
                        if "text" in item:
                            return item.get("text", "")
                    elif isinstance(item, str):
                        return item
        except Exception:
            pass

        # Some responses include 'output_text'
        if "output_text" in raw_json and isinstance(raw_json.get("output_text"), str):
            return raw_json.get("output_text")

        # Last resort: stringify parts of the choices
        try:
            if isinstance(raw_json, dict):
                return json.dumps(raw_json)
        except Exception:
            return ""

    text_out = _extract_text_from_raw(raw)

    return {"text": text_out, "raw": raw}


if __name__ == "__main__":
    # Example quick test. Set OPENAI_API_KEY in your environment first.
    parser = argparse.ArgumentParser(description="Send text+image to OpenAI chat API")
    parser.add_argument("image", help="Path to image file")
    parser.add_argument("--text", "-t", default="Describe the image and extract any text you see.", help="Prompt text")
    parser.add_argument("--model", "-m", default=DEFAULT_MODEL, help="OpenAI model to use")
    args = parser.parse_args()

    out = request_openai_with_image(args.text, args.image, model=args.model)
    print("Model text output:\n", out["text"])