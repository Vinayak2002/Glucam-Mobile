import cv2
import numpy as np
import cosfire as c
from skimage.segmentation import chan_vese
import cv2
import math 

ALLOWED_EXTENSIONS = set(['txt', 'pdf', 'png', 'jpg', 'jpeg', 'gif'])
DIR_PATH = ""
OUTPUT = []

def directory(path: str):
    w_path = ""
    for i in range(len(path)-1, -1, -1):
        if path[i] == '/' or path[i] == '\\':
            w_path += path[0:i+1]
            break    
        
    
        
    return w_path

def main(path: str, dir:str):
	global DIR_PATH
	DIR_PATH = directory(path)
	# DIR_PATH = dir

	img = cv2.imread(path)  
  
	img1,img2,img3,img4,torsion=bloodGlucose(img)
	# img1,img2,imgc3,img4=encodeImg(img1),encodeImg(img2),encodeImg(img3),encodeImg(img4)

	returnStr = ""
	for i in OUTPUT:
		returnStr += i + "#"
	if len(str(torsion)) == 0:
		returnStr += "Error"
	else:
		returnStr += str(torsion)

	return returnStr

def allowed_file(filename):
	return '.' in filename and filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS

def findSide(tempSide):
    horiDir=0
    VerDir=0
    if(tempSide==0):
        horiDir=0
        VerDir=-1
    elif(tempSide==1):
        horiDir=1
        VerDir=-2
    elif(tempSide==2):
        horiDir=1
        VerDir=-1
    elif(tempSide==3):
        horiDir=2
        VerDir=-1
    elif(tempSide==4):
        horiDir=1
        VerDir=0
    elif(tempSide==5):
        horiDir=2
        VerDir=1
    elif(tempSide==6):
        horiDir=1
        VerDir=1
    elif(tempSide==7):
        horiDir=1
        VerDir=2
    elif(tempSide==8):
        horiDir=0
        VerDir=1
    elif(tempSide==9):
        horiDir=-1
        VerDir=2
    elif(tempSide==10):
        horiDir=-1
        VerDir=1
    elif(tempSide==11):
        horiDir=-2
        VerDir=1
    elif(tempSide==12):
        horiDir=-1
        VerDir=0
    elif(tempSide==13):
        horiDir=-2
        VerDir=-1
    elif(tempSide==14):
        horiDir=-1
        VerDir=-1
    elif(tempSide==15):
        horiDir=-1
        VerDir=-2
    return horiDir,VerDir 

def drawLine(side,out2,maxCount,i,j,out):
    horiDir,VerDir=findSide(side)
    iteri,iterj=i,j
    noOfIter=int(maxCount/2)
    if(side%8==0):
        while(noOfIter):
            noOfIter-=1
            out2[iteri][iterj]=255
            out[iteri][iterj]=0
            out[iteri][iterj-VerDir],out[iteri-1][iterj-VerDir],out[iteri+1][iterj-VerDir]=0,0,0
            out[iteri][iterj-2*VerDir],out[iteri-1][iterj-2*VerDir],out[iteri+1][iterj-2*VerDir],out[iteri-2][iterj-2*VerDir],out[iteri+2][iterj-2*VerDir]=0,0,0,0,0
            iterj+=VerDir
    elif(side%8==4):
        while(noOfIter):
            noOfIter-=1
            out2[iteri][iterj]=255
            out[iteri][iterj]=0
            out[iteri-horiDir][iterj],out[iteri-horiDir][iterj-1],out[iteri-horiDir][iterj+1]=0,0,0
            out[iteri-2*horiDir][iterj],out[iteri-2*horiDir][iterj-1],out[iteri-2*horiDir][iterj+1],out[iteri-2*horiDir][iterj-2],out[iteri-2*horiDir][iterj+2]=0,0,0,0,0
            iteri+=horiDir
    elif(side%4==2):
        while(noOfIter):
            noOfIter-=1
            out2[iteri][iterj]=255
            out[iteri][iterj]=0
            out[iteri][iterj-VerDir],out[iteri-horiDir][iterj],out[iteri-2*horiDir][iterj],out[iteri][iterj-2*VerDir]=0,0,0,0
            out[iteri-horiDir][iterj-VerDir],out[iteri-horiDir][iterj-2*VerDir],out[iteri-2*horiDir][iterj-VerDir],out[iteri-2*VerDir][iterj-2*VerDir]=0,0,0,0
            iteri+=horiDir
            iterj+=VerDir
    elif(side%2==1):
        noOfIter=int(noOfIter/2)
        dirFlag=0
        horiDir1,VerDir1=0,0
        if(side==13 or side==15):
            horiDir1,VerDir1=1,1
        elif(side==1 or side==3):
            horiDir1,VerDir1=-1,1
        elif(side==5 or side==7):
            horiDir1,VerDir1=-1,-1
        elif(side==9 or side==11):
            horiDir1,VerDir1=1,-1
        
        while(noOfIter):
            noOfIter-=1
            out2[iteri][iterj]=255
            if(side%8==1 or side%8==7):
                out2[iteri][iterj+VerDir1]=255
                out[iteri-horiDir1][iterj+VerDir1],out[iteri-2*horiDir1][iterj+VerDir1]=0,0
            else:
                out2[iteri+horiDir1][iterj]=255
                out[iteri+horiDir1][iterj-VerDir1],out[iteri+horiDir1][iterj-2*VerDir1]=0,0
            out[iteri][iterj]=0
            out[iteri][iterj-VerDir1],out[iteri-horiDir1][iterj],out[iteri-2*horiDir1][iterj],out[iteri][iterj-2*VerDir1]=0,0,0,0
            out[iteri-horiDir1][iterj-VerDir1],out[iteri-horiDir1][iterj-2*VerDir1],out[iteri-2*horiDir1][iterj-VerDir1],out[iteri-2*VerDir1][iterj-2*VerDir1]=0,0,0,0
            iteri+=horiDir
            iterj+=VerDir

    return iteri,iterj

def start(imageOrg):	
	image = cv2.cvtColor(imageOrg, cv2.COLOR_BGR2GRAY)
	imgshape=image.shape
	# cv = chan_vese(image, mu=0.25, lambda1=1, lambda2=1, tol=1e-3, max_num_iter=200, dt=0.5, init_level_set="checkerboard", extended_output=True)
	cv = chan_vese(image, mu=0.25, lambda1=1, lambda2=1, tol=1e-3, max_iter=200, dt=0.5, init_level_set="checkerboard", extended_output=True)

	filename = 'savedImagechanvasa.jpg'
	chanVese=cv[0]*255
	# cv2.imwrite(filename,chanVese)	
	
	img = np.zeros_like(imageOrg)
	img[:,:,0] = chanVese
	img[:,:,1] = chanVese
	img[:,:,2] = chanVese
	if (img[int((imgshape[0])/2)][int((imgshape[1])/3)][0]==0):
		img[img==255]=1
		img[img==0]=255
		img[img==1]=0

	kernel = np.ones((40,40),np.uint8)
	erosion = cv2.erode(img,kernel,iterations = 1)	
	erosion = cv2.bitwise_not(erosion)
	gray = cv2.cvtColor(erosion, cv2.COLOR_BGR2GRAY)

	thresh = cv2.threshold(gray, 0, 255, cv2.THRESH_BINARY_INV + cv2.THRESH_OTSU)[1]

	cnts = cv2.findContours(thresh, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
	cnts = cnts[0] if len(cnts) == 2 else cnts[1]
	cnts = sorted(cnts, key=cv2.contourArea, reverse=True)
	area = 0
	for c in cnts:
		cv2.drawContours(erosion, [c], -1, (36,255,12), 3)
		perimeter = cv2.arcLength(c,True)
		area = cv2.contourArea(c)
		k = cv2.isContourConvex(c)
		leftmost = tuple(c[c[:,:,0].argmin()][0])
		rightmost = tuple(c[c[:,:,0].argmax()][0])
		topmost = tuple(c[c[:,:,1].argmin()][0])
		bottommost = tuple(c[c[:,:,1].argmax()][0])
		shape=erosion.shape
		erosion[:topmost[1],:,:] = 255
		erosion[bottommost[1]:,:,:] = 255
		erosion[:,:leftmost[0],:] = 255
		erosion[:,rightmost[0]:,:] = 255
		break

	# cv2.imwrite('bulbimage.jpg',erosion)
	
	img = image
	img4 = cv2.cvtColor(erosion, cv2.COLOR_BGR2GRAY)

	img[img4==255]=0
 
	global DIR_PATH
	imgName = DIR_PATH + 'vesselobt.jpg'
	OUTPUT.append(imgName)
	cv2.imwrite(imgName,img)
	return chanVese,erosion,img

def BCOSFIRE(img_rgb, mask=[]):
	## Model configuration

	proto_symm = np.zeros(shape=(201,201)).astype(np.uint8)
	proto_symm[:,100] = 255

	subject = 255 - img_rgb[:,:,1]
	subject = subject/255
	
	cx, cy = (100,100)

	# Symmetrical filter
	cosfire_symm = c.COSFIRE(
		c.CircleStrategy(c.DoGFilter, (2.4, 1), prototype=proto_symm, center=(cx,cy), rhoList=range(0,9,2), sigma0=3,  alpha=0.7,
		rotationInvariance = np.arange(12)/12*np.pi)
	   ).fit()
	resp_symm = cosfire_symm.transform(subject)

	# Asymmetrical filter
	cosfire_asymm = c.COSFIRE(
			c.CircleStrategy(c.DoGFilter, (1.8, 1), prototype=proto_symm, center=(cx,cy), rhoList=range(0,23,2), sigma0=2,  alpha=0.1,
			rotationInvariance = np.arange(24)/12*np.pi)
		   ).fit()


	# Make asymmetrical
	asymmTuples = []
	for tupl in cosfire_asymm.strategy.tuples:
		if tupl[1] <= np.pi:
			asymmTuples.append(tupl)
	cosfire_asymm.strategy.tuples = asymmTuples
	resp_asymm = cosfire_asymm.transform(subject)


	resp = resp_symm + resp_asymm
	resp_symm = c.rescaleImage(resp_symm, 0, 255)
	resp_asymm = c.rescaleImage(resp_asymm, 0, 255)
	resp = np.multiply(resp, mask)
	resp = c.rescaleImage(resp, 0, 255)
	segresp = np.where(resp > 37, 255, 0)
	return resp,segresp

def vessel(sample_1_out,erosion):	
	img=sample_1_out
	img[img>1]=255
	shape=img.shape
	out = np.empty(shape=(shape[0],shape[1]))
	out.fill(0)

	erosion = cv2.cvtColor(erosion, cv2.COLOR_BGR2GRAY)
	img1 = erosion
	img[img1!=0]=0

	for i in range(shape[0]-4):
		for j in range(shape[1]-4):
			if(img[i][j]==255 and img[i][j+1]==255 and img[i][j+2]==255 and img[i][j+3]==255 and img[i][j+4]==255 and
				img[i+1][j]==255 and img[i+1][j+1]==255 and img[i+1][j+2]==255 and img[i+1][j+3]==255 and img[i+1][j+4]==255 and
				img[i+2][j]==255 and img[i+2][j+1]==255 and img[i+2][j+2]==255 and img[i+2][j+3]==255 and img[i+2][j+4]==255 and
				img[i+3][j]==255 and img[i+3][j+1]==255 and img[i+3][j+2]==255 and img[i+3][j+3]==255 and img[i+3][j+4]==255 and
				img[i+4][j]==255 and img[i+4][j+1]==255 and img[i+4][j+2]==255 and img[i+4][j+3]==255 and img[i+4][j+4]==255):
				out[i+2][j+2]=255

	out2 = np.empty(shape=(shape[0],shape[1]))
	out2.fill(0)

	for i in range(5,shape[0]-10):
		for j in range(5,shape[1]-10):
			tempi,tempj=i,j
			if(out[i][j]!=255):
				continue
			else:
				maxCount = 0 #length of vessel in max direction
				tempCount = 0 #length of vessel in each direction
				side = 0 #in which direction to proceed
				tempSide = 0 #side checking
				horiDir = 0 #no of pixel to move in horizontal direction
				VerDir = 0 #no. of pixel to move in vertical direction 
				lineBreak = 0 #if line breaks come out
				while(lineBreak==0):
					tempSide=0
					maxCount=0
					tempCount=0
					while(tempSide<16):
						horiDir,VerDir=findSide(tempSide)
						iteri=tempi+horiDir
						iterj=tempj+VerDir
						if(iteri<0 or iterj<0 or iteri>=shape[0] or iterj>=shape[1]):
							continue
						while(out[iteri][iterj]==255):
							tempCount+=1
							iteri+=horiDir
							iterj+=VerDir
							if(iteri<0 or iterj<0 or iteri>=shape[0] or iterj>=shape[1]):
								if(tempSide%2==1):
									tempCount+=tempCount
								if(tempCount>maxCount):
									maxCount=tempCount
									side=tempSide
								tempCount=0
								break
						if(tempSide%2==1):
							tempCount+=tempCount
						if(tempCount>maxCount):
							maxCount=tempCount
							side=tempSide
						tempCount=0
						tempSide+=1
					if(maxCount<4):
						lineBreak=1
						break
					else:
						tempi,tempj=drawLine(side,out2,maxCount,tempi,tempj,out)

	global DIR_PATH
	imgName = DIR_PATH + "outputVessel.jpg"
	OUTPUT.append(imgName)
	cv2.imwrite(imgName,out2)
	return out2

def checkFour(i,j,img):
    for value in range(2,5):
        for iterj in range(2*value+1):
            if(img[i+value][j-iterj+value]==255):
                return 1,i+value,j-iterj+value
        for iterj in range(2*value+1):
            if(img[i-value][j-iterj+value]==255):
                return 1,i-value,j-iterj+value
        for iteri in range(2*value-1):
            if(img[i-iteri+value-1][j+value]==255):
                return 1,i-iteri+value-1,j+value
        for iteri in range(2*value-1):
            if(img[i-iteri+value-1][j-value]==255):
                return 1,i-iteri+value-1,j-value
    return 0,0,0

def tccc(outputVessel):
	img=outputVessel
	shape=img.shape

	img[img>125]=255
	img[img<126]=0

	totalPerimeter,totalDistance,totalNOI=0,0,0
	perimeter=0
	distance=0
	noOfInfletion=0
	torsion=np.empty((0))
	for i in range(5,shape[0]-10):
		for j in range(5,shape[1]-10):
			if(img[i][j]==255):
				noOfInfletion=1
				img[i][j]=0
				flagDir=0
				newFlagDir=0
				lineBreak=0
				iteri,iterj=i,j
				perimeter,distance=0,0
				while(lineBreak==0 and iteri<shape[0]-5 and iteri>5 and iterj<shape[1]-5 and iterj>5 ):    
					if(img[iteri+1][iterj+1]==255):
						newFlagDir=1
						perimeter+=1.44
						img[iteri+1][iterj+1]=0
						iteri+=1
						iterj+=1
					elif(img[iteri][j+1]==255):
						newFlagDir=1
						perimeter+=1
						img[iteri][iterj+1]=0
						iterj+=1
					elif(img[iteri-1][iterj+1]==255):
						newFlagDir=1
						perimeter+=1.44
						img[iteri-1][iterj+1]=0
						iteri-=1
						iterj+=1
					elif(img[iteri+1][iterj-1]==255):
						newFlagDir=0
						perimeter+=1.44
						img[iteri+1][iterj-1]=0
						iteri+=1
						iterj-=1
					elif(img[iteri][iterj-1]==255):
						newFlagDir=0
						perimeter+=1
						img[iteri][iterj-1]=0
						iterj-=1
					elif(img[iteri-1][iterj-1]==255):
						newFlagDir=0
						perimeter+=1.44
						img[iteri-1][iterj-1]=0
						iteri-=1
						iterj-=1
					elif(img[iteri+1][iterj]==255):
						perimeter+=1
						img[iteri+1][iterj]=0
						iteri+=1
					elif(img[iteri-1][iterj]==255):
						perimeter+=1
						img[iteri-1][iterj]=0
						iteri-=1
					else:
						val,tempIteri,tempIterj=checkFour(iteri,iterj,img)
						if(val==1):
							if(tempIterj>iterj):
								newFlagDir=1
							elif(tempIterj<iterj):
								newFlagDir=0
							perimeter+=math.sqrt((tempIteri-iteri)**2+(tempIterj-iterj)**2)
							iteri,iterj=tempIteri,tempIterj
							img[iteri][iterj]=0
						else:
							lineBreak=1
					if(flagDir!=newFlagDir):
						noOfInfletion+=1
						flagDir=newFlagDir
				distance=math.sqrt((iteri-i)**2+(iterj-j)**2)
				if(distance==0):
					distance=1
				tempTorsion=(perimeter*(noOfInfletion+1))/(noOfInfletion*distance)
				totalPerimeter+=perimeter
				totalDistance+=distance
				totalNOI+=noOfInfletion
				torsion = np.append(torsion, tempTorsion) 
	# print("Torsion - V - ", torsion)
	torsionSum,subVessel=0,0
	for i in torsion:
		torsionSum+=i
		subVessel+=1
	# print(torsionSum/subVessel)
	normalisedTorsion=(totalPerimeter*(totalNOI+1))/(totalNOI*totalDistance+1)
	# normalisedTorsion=(totalPerimeter*(totalNOI+1))/(totalNOI*totalDistance) ----------------Added by Vinayak
	# print("Normalised Torsion : ",normalisedTorsion)
	return normalisedTorsion

def chan_veseAlgo(img):
	img_rgb = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
	img_rgb = np.pad(img_rgb,((20,20),(20,20),(0,0))) # Add some padding to get rid of the white edges in the output vessel image
	mask = np.ones(shape=img_rgb.shape[:-1])

	resp,segresp = BCOSFIRE(img_rgb,mask)
	resp = resp[20:-20,20:-20] # Remove padding
	p_vessel = resp/np.amax(resp)
	sample_1_out = (((p_vessel - p_vessel.min()) / (p_vessel.max() - p_vessel.min())) * 255.9).astype(np.uint8)
	return sample_1_out

def bloodGlucose(image1):
	# print("before Start")
	img1,img2,img3=start(image1)
	# print("After Start, Before Chan Vese")
	sample_1_out=chan_veseAlgo(img3)
	# print("After chan_vese, before vessel")
	img4=vessel(sample_1_out,img2)
	# print("After vessel, before tccc")
	torsion=tccc(img4)
	# print("After tccc")
	return img1,img2,img3,img4,torsion

def encodeImg(img):
	img_encode = cv2.imencode('.jpg', img)[1]

	data_encode = np.array(img_encode)
	str_encode = data_encode.tostring()
	return str_encode