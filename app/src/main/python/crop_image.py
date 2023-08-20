import cv2
from os.path import dirname, join

left_harr = join(dirname(__file__), "haarcascade_mcs_lefteye.xml")
right_harr = join(dirname(__file__), "haarcascade_mcs_righteye.xml")

def extract_eye(face):
   face = cv2.cvtColor(face, cv2.COLOR_BGR2GRAY)
   (thresh, face) = cv2.threshold(face, 100, 255, cv2.THRESH_BINARY)
   h = len(face)
   w = len(face[0])
      
   lt = tt = 0
   rt = w
   bt = h // 2
   
   x = h // 2
   y = w // 2
   passed = False
   while x >= 0:
      if passed and face[x][y] == 0:
         tt = x
         break
      if face[x][y] == 255:
         passed = True
      x -= 1

   x = h // 2
   passed = False
   while y >= 0:
      if passed and face[x][y] == 0:
         lt = y
         break
      if face[x][y] == 255:
         passed = True
      y -= 1

   x = h // 2
   y = w // 2
   passed = False
   while y < w:
      if face[x][y] == 255:
         rt = y
         break
      y += 1

   return tt, bt, lt, rt

def main(path: str, left: bool, rearCam: bool, dir: str, gallery: bool):
   img = cv2.imread(path)
   
   if rearCam and not gallery:
      img = cv2.rotate(img, cv2.ROTATE_90_CLOCKWISE)
   else:
      img = cv2.rotate(img, cv2.ROTATE_90_COUNTERCLOCKWISE) 
   
   x, y = len(img), len(img[0])
   l = round(y * 0.1)
   r = round(y * 0.9)
   t = round(x * 0.1)
   b = x // 2
   image = img[t:b, l:r]
      
   gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
   if left:
      face_cascade = cv2.CascadeClassifier(left_harr)
   else:
      face_cascade = cv2.CascadeClassifier(right_harr)
      
   faces = face_cascade.detectMultiScale(gray, 1.1, 4, minSize=(280, 280), maxSize=(500, 300))
      
   w_path = ""
   for i in range(len(path)-1, -1, -1):
         if path[i] == '/' or path[i] == '\\':
            w_path += path[0:i+1]
            break
         
   # w_path = dir
            
   for i, (x, y, w, h) in enumerate(faces):
      cv2.rectangle(image, (x, y), (x+w, y+h), (0, 0, 255), 2)
      face = image[y:y + h, x:x + w]
      i0, i1, j0, j1 = extract_eye(face)
      offsetX = 20
      m = max(0, i0+1-offsetX)
      face = face[m:i1, j0+1:j1]
      # face = cv2.cvtColor(face, cv2.COLOR_BGR2GRAY)
      w_path = w_path + "cropped" + str(i) + ".jpg"
      cv2.imwrite(w_path, face)
      
   return w_path