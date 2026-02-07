from flask import Flask, request
import os
import time

app = Flask(__name__)

# Define the directory where uploaded videos will be stored
UPLOAD_FOLDER = 'traindata'

# Create the traindata directory if it doesn't exist
if not os.path.exists(UPLOAD_FOLDER):
    os.makedirs(UPLOAD_FOLDER)
    print(f"Created directory: {UPLOAD_FOLDER}")

@app.route('/upload', methods=['POST'])
def upload_video():
    """
    Handle POST requests from the Android app.
    The app sends the MP4 file in a multipart/form-data field named 'video'.
    """
    # Task 2: Log all request headers
    print(f"\n{'='*50}")
    print(f"[{time.strftime('%Y-%m-%d %H:%M:%S')}] Incoming upload request")
    print(f"Headers: {request.headers}")

    # Check if the 'video' key is present in the request files
    if 'video' not in request.files:
        print("Error: No video field found in request.")
        return "Missing video field", 400

    file = request.files['video']

    # Check if a file was actually selected/sent
    if file.filename == '':
        print("Error: Empty filename received.")
        return "No selected file", 400

    # Task 2: Log received file size
    file.seek(0, os.SEEK_END)
    file_size = file.tell()
    print(f"Original filename: {file.filename}")
    print(f"Size: {file_size} bytes")
    file.seek(0)

    # Use original client filename directly (overwrite if exists)
    filename = file.filename
    file_path = os.path.join(UPLOAD_FOLDER, filename)

    if os.path.exists(file_path):
        print(f"File already exists, overwriting: {file_path}")

    try:
        file.save(file_path)
    except Exception as e:
        print(f"ERROR saving file: {e}")
        return f"Server failed to save file: {e}", 500

    print(f"--- Successfully received: {filename} ---")
    print(f"--- Saved to: {file_path} ---")
    print(f"{'='*50}\n")

    return f"File {file.filename} uploaded successfully!", 200

if __name__ == '__main__':
    # Start the server on port 5000
    # host='0.0.0.0' allows connections from the Android Emulator (10.0.2.2)
    print("Starting Flask server on http://0.0.0.0:5000...")
    # Task 4: threaded=True to handle concurrent uploads from Android
    app.run(host='0.0.0.0', port=5000, debug=True, threaded=True)
