from flask import Flask, request
import os

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
    # Check if the 'video' key is present in the request files
    if 'video' not in request.files:
        print("Error: No video field found in request.")
        return "Missing video field", 400

    file = request.files['video']

    # Check if a file was actually selected/sent
    if file.filename == '':
        print("Error: Empty filename received.")
        return "No selected file", 400

    # Generate the full save path
    file_path = os.path.join(UPLOAD_FOLDER, file.filename)

    # Save the file to the traindata folder
    file.save(file_path)

    print(f"--- Successfully received: {file.filename} ---")
    print(f"--- Saved to: {file_path} ---")

    return f"File {file.filename} uploaded successfully!", 200

if __name__ == '__main__':
    # Start the server on port 5000
    # host='0.0.0.0' allows connections from the Android Emulator (10.0.2.2)
    print("Starting Flask server on http://0.0.0.0:5000...")
    app.run(host='0.0.0.0', port=5000, debug=True)