from flask import Flask, request
import os

app = Flask(__name__)

UPLOAD_FOLDER = 'traindata'
os.makedirs(UPLOAD_FOLDER, exist_ok=True)


@app.route('/upload', methods=['POST'])
def receive_clip():
    if 'video' not in request.files:
        return "No clip attached.", 400

    clip = request.files['video']

    if clip.filename == '':
        return "Empty filename.", 400

    clip_path = os.path.join(UPLOAD_FOLDER, clip.filename)

    try:
        clip.save(clip_path)
    except Exception as e:
        print(f"Save failed: {e}")
        return f"Save failed: {e}", 500

    print(f"Received: {clip.filename} -> {clip_path}")
    return f"Received: {clip.filename}", 200


if __name__ == '__main__':
    # host='0.0.0.0' allows connections from the Android emulator (10.0.2.2)
    app.run(host='0.0.0.0', port=5000, debug=True, threaded=True)
