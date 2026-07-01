import csv
import os
import firebase_admin
from firebase_admin import credentials, firestore, storage

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.dirname(SCRIPT_DIR)

SERVICE_ACCOUNT_PATH = os.path.join(SCRIPT_DIR, "serviceAccountKey.json")
CSV_PATH = os.path.join(PROJECT_ROOT, "app", "src", "main", "assets", "facilities.csv")
PHOTOS_DIR = os.path.join(PROJECT_ROOT, "app", "src", "main", "assets", "photos")

cred = credentials.Certificate(SERVICE_ACCOUNT_PATH)
firebase_admin.initialize_app(cred, {
    "storageBucket": "playout-app.firebasestorage.app"
})

db = firestore.client()
bucket = storage.bucket()

PHOTO_SUFFIXES = ["main", "extra1", "extra2", "extra3"]


def upload_photo(fid: str, suffix: str) -> str:
    local_path = os.path.join(PHOTOS_DIR, f"{fid}_{suffix}.jpg")
    storage_path = f"facilities/{fid}/{suffix}.jpg"
    blob = bucket.blob(storage_path)
    blob.upload_from_filename(local_path, content_type="image/jpeg")
    blob.make_public()
    return blob.public_url


def upload_facility(row: dict) -> None:
    fid = row["fid"]
    print(f"[{fid}] Uploading photos...", flush=True)

    photo_urls = []
    for suffix in PHOTO_SUFFIXES:
        url = upload_photo(fid, suffix)
        print(f"  [{fid}] {suffix}.jpg -> {url}", flush=True)
        photo_urls.append(url)

    doc = {
        "fid": int(fid),
        "name": row["name"],
        "sport": row["sport"],
        "description": row["description"],
        "condition": int(row["condition"]),
        "water": int(row["water"]),
        "seats": int(row["seats"]),
        "experience": int(row["expirience"]),
        "longitude": float(row["longitude"]),
        "latitude": float(row["latitude"]),
        "photoUrls": photo_urls,
    }

    db.collection("facilities").document(fid).set(doc)
    print(f"[{fid}] Firestore document written.", flush=True)


def main() -> None:
    with open(CSV_PATH, newline="", encoding="utf-8") as f:
        reader = csv.DictReader(f)
        rows = list(reader)

    print(f"Found {len(rows)} facilities in CSV.")
    for row in rows:
        upload_facility(row)

    print("All facilities uploaded successfully.")


if __name__ == "__main__":
    main()
