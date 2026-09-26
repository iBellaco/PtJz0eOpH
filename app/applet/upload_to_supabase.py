import os
import re
import json
import urllib.request
import urllib.error
import mimetypes

SUPABASE_URL = "https://yreknglctxujpetgqhnw.supabase.co"
SUPABASE_KEY = "sb_publishable_bQJGpyYVR-uxtBmN03F5yA_ZuibUcAr"
BUCKET = "wildrift_assets"

# 1. Identify User Avatars and Frames
user_local_files = set()

# Read AvatarCatalog.kt
catalog_path = "app/src/main/java/com/example/data/AvatarCatalog.kt"
if os.path.exists(catalog_path):
    with open(catalog_path, "r", encoding="utf-8") as f:
        content = f.read()
        for match in re.findall(r"offline_images/([^\s\"\'\)\>]+)", content):
            user_local_files.add(match)

# Read UserAvatarView.kt
avatar_view_path = "app/src/main/java/com/example/ui/components/UserAvatarView.kt"
if os.path.exists(avatar_view_path):
    with open(avatar_view_path, "r", encoding="utf-8") as f:
        content = f.read()
        for match in re.findall(r"offline_images/([^\s\"\'\)\>]+)", content):
            user_local_files.add(match)

print(f"User local files identified: {len(user_local_files)}")

# 2. Ensure Bucket Exists
def make_request(url, method="GET", headers=None, data=None):
    if headers is None:
        headers = {}
    headers["apikey"] = SUPABASE_KEY
    headers["Authorization"] = f"Bearer {SUPABASE_KEY}"
    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req) as resp:
            body = resp.read().decode("utf-8")
            return resp.status, body
    except urllib.error.HTTPError as e:
        body = e.read().decode("utf-8")
        return e.code, body

print("Checking Supabase Storage Bucket...")
status, body = make_request(f"{SUPABASE_URL}/storage/v1/bucket")
print(f"List buckets status: {status}")

# Create bucket if not exists
create_payload = json.dumps({"id": BUCKET, "name": BUCKET, "public": True}).encode("utf-8")
status, body = make_request(f"{SUPABASE_URL}/storage/v1/bucket", method="POST", headers={"Content-Type": "application/json"}, data=create_payload)
print(f"Create bucket status: {status}")

# 3. List and Clean Existing Files in Bucket
print("Cleaning existing files in bucket...")
status, body = make_request(f"{SUPABASE_URL}/storage/v1/object/list/{BUCKET}", method="POST", headers={"Content-Type": "application/json"}, data=json.dumps({"prefix": "", "limit": 1000}).encode("utf-8"))
if status == 200:
    existing_items = json.loads(body)
    names_to_delete = [item["name"] for item in existing_items if "name" in item]
    if names_to_delete:
        print(f"Deleting {len(names_to_delete)} old items from Supabase...")
        del_payload = json.dumps({"prefixes": names_to_delete}).encode("utf-8")
        status, body = make_request(f"{SUPABASE_URL}/storage/v1/object/{BUCKET}", method="DELETE", headers={"Content-Type": "application/json"}, data=del_payload)
        print(f"Delete response: {status}")

# 4. Upload Game Images to Supabase
assets_dir = "app/src/main/assets"
uploaded_count = 0
skipped_count = 0

for root, dirs, files in os.walk(assets_dir):
    for file in files:
        file_path = os.path.join(root, file)
        filename = file
        
        # Determine target name in bucket
        if "offline_images" in root:
            target_name = filename
        elif "champions" in root:
            target_name = f"champions_{filename}"
        else:
            rel_dir = os.path.relpath(root, assets_dir).replace("/", "_")
            target_name = f"{rel_dir}_{filename}" if rel_dir != "." else filename
            
        # Check if excluded (user avatar or user frame)
        if filename.startswith("frame_") or filename in user_local_files or "avatar_" in filename:
            skipped_count += 1
            print(f"LOCAL ONLY (User Avatar/Frame): {filename}")
            continue
            
        # Upload
        mime_type, _ = mimetypes.guess_type(file_path)
        if not mime_type:
            if filename.endswith(".webp"):
                mime_type = "image/webp"
            elif filename.endswith(".png"):
                mime_type = "image/png"
            elif filename.endswith(".jpg") or filename.endswith(".jpeg"):
                mime_type = "image/jpeg"
            else:
                mime_type = "application/octet-stream"
                
        with open(file_path, "rb") as f_file:
            file_data = f_file.read()
            
        upload_url = f"{SUPABASE_URL}/storage/v1/object/{BUCKET}/{target_name}"
        status, body = make_request(upload_url, method="POST", headers={"Content-Type": mime_type, "x-upsert": "true"}, data=file_data)
        if status in (200, 201):
            uploaded_count += 1
            if uploaded_count % 25 == 0:
                print(f"Uploaded {uploaded_count} game images to Supabase Storage...")
        else:
            print(f"Error uploading {target_name}: {status} {body}")

print(f"\nFINISHED: Uploaded {uploaded_count} game images to Supabase Storage!")
print(f"Kept local only: {skipped_count} user avatar/frame files.")
