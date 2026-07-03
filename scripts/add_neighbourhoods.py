import firebase_admin
from firebase_admin import credentials, firestore

cred = credentials.Certificate("serviceAccountKey.json")
firebase_admin.initialize_app(cred)

db = firestore.client()

neighbourhoods = {
    "1": "Kruppstrasse",
    "2": "Kurpark",
    "3": "Kurpark",
    "4": "Kurpark",
    "5": "Kurpark",
    "6": "Kurpark",
    "7": "Kurpark",
    "8": "Westpark",
    "9": "Westpark",
    "10": "Westpark",
    "11": "Westpark",
    "12": "Westpark",
    "13": "Westpark",
    "14": "Melaten Nord",
    "15": "Koenigshuegel",
    "16": "Ruetscherstrasse",
    "17": "Ruetscherstrasse",
    "18": "Lousberg",
    "19": "Lousberg",
    "20": "Lousberg",
    "21": "Lousberg",
    "22": "Lousberg",
    "23": "Lousberg",
    "24": "Laurensberg",
    "25": "Laurensberg",
    "26": "Frankenberger Park",
    "27": "Frankenberger Park",
    "28": "Breslauerstrasse",
    "29": "Breslauerstrasse",
    "30": "Breslauerstrasse"
}

for fid, neighbourhood in neighbourhoods.items():
    db.collection("facilities").document(fid).update(
        {"neighbourhood": neighbourhood}
    )
    print(f"Updated facility {fid}: {neighbourhood}")

print("Done")